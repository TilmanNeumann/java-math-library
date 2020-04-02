/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018 Tilman Neumann (www.tilman-neumann.de)
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 */
package de.tilman_neumann.jml.factor.psiqs;

import static de.tilman_neumann.jml.factor.base.AnalysisOptions.*;
import static de.tilman_neumann.jml.base.BigIntConstants.*;

import java.math.BigInteger;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.jml.factor.FactorException;
import de.tilman_neumann.jml.factor.base.PrimeBaseGenerator;
import de.tilman_neumann.jml.factor.base.congruence.AQPair;
import de.tilman_neumann.jml.factor.base.congruence.CongruenceCollector;
import de.tilman_neumann.jml.factor.base.congruence.CongruenceCollectorReport;
import de.tilman_neumann.jml.factor.base.matrixSolver.FactorTest;
import de.tilman_neumann.jml.factor.base.matrixSolver.FactorTest01;
import de.tilman_neumann.jml.factor.base.matrixSolver.MatrixSolver;
import de.tilman_neumann.jml.factor.siqs.KnuthSchroeppel;
import de.tilman_neumann.jml.factor.siqs.ModularSqrtsEngine;
import de.tilman_neumann.jml.factor.siqs.data.BaseArrays;
import de.tilman_neumann.jml.factor.siqs.poly.AParamGenerator;
import de.tilman_neumann.jml.factor.siqs.poly.PolyReport;
import de.tilman_neumann.jml.factor.siqs.powers.PowerFinder;
import de.tilman_neumann.jml.factor.siqs.sieve.SieveParams;
import de.tilman_neumann.jml.factor.siqs.sieve.SieveReport;
import de.tilman_neumann.jml.factor.siqs.tdiv.TDivReport;
import de.tilman_neumann.jml.powers.PurePowerTest;
import de.tilman_neumann.util.TimeUtil;
import de.tilman_neumann.util.Timer;

/**
 * Multi-threaded SIQS, the fastest factor algorithm in this project.
 * 
 * @author Tilman Neumann
 */
abstract public class PSIQSBase extends FactorAlgorithm {
	private static final Logger LOG = Logger.getLogger(PSIQSBase.class);
	private static final boolean DEBUG = false;

	protected int numberOfThreads;
	private Integer d0;
	private int d;
	
	// pure power test
	private PurePowerTest powerTest = new PurePowerTest();

	// Knuth-Schroeppel algorithm to compute the multiplier k
	protected KnuthSchroeppel multiplierFinder;

	// prime base configuration
	private PrimeBaseGenerator primeBaseBuilder = new PrimeBaseGenerator();
	protected float Cmult;

	// a-param generator configuration
	protected AParamGenerator apg;

	// engine to compute the modular sqrt's t with t^2==kN (mod p) for all p
	private ModularSqrtsEngine modularSqrtsEngine = new ModularSqrtsEngine();

	// sieve configuration
	protected float Mmult;
	private Float maxQRestExponent0;
	protected float maxQRestExponent;
	
	// collects the congruences we find
	private CongruenceCollector congruenceCollector;
	// extra congruences to have a bigger chance that the equation system solves. the likelihood is >= 1-2^(extraCongruences+1)
	private int extraCongruences;
	/** The solver used for smooth congruence equation systems. */
	protected MatrixSolver matrixSolver;

	protected PowerFinder powerFinder;
	
	// statistics
	private Timer timer = new Timer();
	private long powerTestDuration, initNDuration, createThreadDuration, ccDuration, solverDuration;
	private int solverRunCount;
	
	/**
	 * Standard constructor.
	 * @param Cmult multiplier for prime base size
	 * @param Mmult multiplier for sieve array size
	 * @param maxQRestExponent A Q with unfactored rest QRest is considered smooth if QRest <= N^maxQRestExponent.
	 *                         Good values are 0.16..0.19; null means that it is determined automatically.
	 * @param numberOfThreads
	 * @param d the d-parameter of quadratic polynomials Q(x) = (d*a*x + b)^2 - kN; may be null for automatic derivation
	 * @param powerFinder algorithm to add powers to the primes used for sieving
	 * @param matrixSolver solver for smooth congruences matrix
	 * @param apg a-parameter generator
	 */
	public PSIQSBase(
			float Cmult, float Mmult, Float maxQRestExponent, int numberOfThreads, Integer d,
			PowerFinder powerFinder, MatrixSolver matrixSolver, AParamGenerator apg) {
		
		this.Cmult = Cmult;
		this.Mmult = Mmult;
		this.maxQRestExponent0 = maxQRestExponent;
		this.numberOfThreads = numberOfThreads;
		this.d0 = d;
		this.powerFinder = powerFinder;
		this.congruenceCollector = new CongruenceCollector();
		this.extraCongruences = 10;
		this.matrixSolver = matrixSolver;
		this.apg = apg;
		this.multiplierFinder = new KnuthSchroeppel();
	}

	abstract public String getName();
	
	/**
	 * Test the current N.
	 * @return factor, or null if no factor was found.
	 */
	public BigInteger findSingleFactor(BigInteger N) {
		if (ANALYZE) {
			timer.start();
			powerTestDuration = initNDuration = createThreadDuration = ccDuration = solverDuration = 0;
			solverRunCount = 0;
		}

		// the quadratic sieve does not work for pure powers; check that first:
		PurePowerTest.Result purePower = powerTest.test(N);
		if (purePower!=null) {
			// N is indeed a pure power -> return a factor that is about sqrt(N)
			return purePower.base.pow(purePower.exponent>>1);
		} // else: no pure power, run quadratic sieve
		if (ANALYZE) powerTestDuration += timer.capture();

		// Compute prime base size:
		// http://www.mersenneforum.org/showthread.php?s=3087aa210d8d7f1852c690a45f22d2e5&t=11116&page=2:
		// "A rough estimate of the largest prime in the factor base is exp(0.5 * sqrt(ln(N) * ln(ln(N))))".
		// Here we estimate the number of entries instead of the largest element in the prime base,
		// with Cmult instead of the constant 0.5. A good value is Cmult = 0.32.
		int NBits = N.bitLength();
		double N_dbl = N.doubleValue();
		double lnN = Math.log(N_dbl);
		double lnTerm = Math.sqrt(lnN * Math.log(lnN)); // (lnN)^0.5 * (lnlnN)^(1-0.5)
		double primeBaseSize_dbl = Math.exp(Cmult * lnTerm);
		if (primeBaseSize_dbl > Integer.MAX_VALUE) {
			LOG.error("N=" + N + " (" + NBits + " bits) is too big for SIQS!");
			return null;
		}
		int primeBaseSize = Math.max(30, (int) primeBaseSize_dbl); // min. size for very small N
		int[] primesArray = new int[primeBaseSize];

		// The number of congruences we need to find before we try to solve the smooth congruence equation system:
		// We want: #equations = #variables + some extra congruences
		int requiredSmoothCongruenceCount = primeBaseSize + extraCongruences;

		// compute Knuth-Schroppel multiplier
		int k = multiplierFinder.computeMultiplier(N);
		BigInteger kN = N; // iff k==1
		if (k>1) {
			BigInteger kBig = BigInteger.valueOf(k);
			// avoid square kN without square N; that would lead to an infinite loop in trial division
			if (N.mod(kBig).equals(I_0)) return kBig;
			kN = kBig.multiply(N);
		}
		
		// is the d-parameter preset or do we have to compute it from kN ?
		if (d0 == null) {
			// Choose d=2 if kN % 8 == 1, otherwise d=1. For the modulus we only need the lowest three bits
			d = ((kN.intValue() & 7) == 1) ? 2 : 1;
		} else {
			d = d0;
		}
		if (DEBUG) LOG.debug("d = " + d);
		
		// Create the reduced prime base for kN:
		primeBaseBuilder.computeReducedPrimeBase(kN, primeBaseSize, primesArray);
		
		// Compute the t with t^2 == kN (mod p) for all p: Throws a FactorException if some p divides N
		int[] tArray = modularSqrtsEngine.computeTArray(primesArray, primeBaseSize, kN);

		// compute sieve array size, a multiple of 256
		int pMax = primesArray[primeBaseSize-1];
		long proposedSieveArraySize = 6144 + (long) Math.exp(Mmult * lnTerm); // 6144 = best experimental result for small N
		if (proposedSieveArraySize+pMax > Integer.MAX_VALUE) { // this might happen at N with ~ 650 bit or later
			LOG.error("N=" + N + " (" + NBits + " bits) is too big for SIQS!");
			return null;
		}
		int adjustedSieveArraySize = (int) (proposedSieveArraySize & 0x7FFFFF00);
		if (DEBUG) LOG.debug("N=" + N + ", k=" + k + ": pMax=" + pMax + ", sieve array size was adjusted from " + proposedSieveArraySize + " to " + adjustedSieveArraySize);
		
		// compute biggest QRest admitted for a smooth relation
		if (maxQRestExponent0 != null) {
			maxQRestExponent = maxQRestExponent0;
		} else {
			maxQRestExponent = (NBits<=150) ? 0.16F : 0.16F + (NBits-150.0F)/5250;
		}
		double maxQRest = Math.pow(N_dbl, maxQRestExponent);

		// initialize sub-algorithms for new N
		apg.initialize(k, N, kN, d, primeBaseSize, primesArray, tArray, adjustedSieveArraySize); // must be done before polyGenerator initialization where qCount is required
		FactorTest factorTest = new FactorTest01(N);
		congruenceCollector.initialize(N, factorTest);
		matrixSolver.initialize(N, factorTest);

		// create empty synchronized AQ-pair buffer, used to pass AQ-pairs from "sieve threads" to the main thread
		AQPairBuffer aqPairBuffer = new AQPairBuffer();

		// compute some basic parameters for N
		SieveParams sieveParams = new SieveParams(kN, primesArray, primeBaseSize, adjustedSieveArraySize, maxQRest, 127);
		// compute logP array
		byte[] logPArray = computeLogPArray(primesArray, primeBaseSize, sieveParams.lnPMultiplier);
		// compute reciprocals of primes
		double[] pinvArrayD = new double[primeBaseSize];
		long[] pinvArrayL = new long[primeBaseSize];
		for (int i=0; i<primeBaseSize; i++) {
			pinvArrayD[i] = 1.0 / primesArray[i];
			pinvArrayL[i] = (1L<<32) / primesArray[i];
		}

		// Find and add powers to the prime base
		BaseArrays baseArrays = powerFinder.addPowers(kN, primesArray, tArray, logPArray, pinvArrayD, pinvArrayL, primeBaseSize, sieveParams);
		if (ANALYZE) initNDuration += timer.capture();

		// Create and run threads: This is among the most expensive parts for N<=180 bit,
		// much more expensive than all the other initializations for a new N.
		PSIQSThreadBase[] threadArray = new PSIQSThreadBase[numberOfThreads];
		for (int threadIndex=0; threadIndex<numberOfThreads; threadIndex++) {
			threadArray[threadIndex] = createThread(k, N, kN, d, sieveParams, baseArrays, apg, aqPairBuffer, threadIndex);
			threadArray[threadIndex].start();
		}
		if (ANALYZE) createThreadDuration += timer.capture();

		try {
			while (true) { // as long as we didn't find a factor
				// wait for new data
				ArrayList<AQPair> aqPairs = aqPairBuffer.collectAQPairs();
				
				//LOG.debug("add " + aqPairs.size() + " new AQ-pairs to CC");
				// Add new data to the congruenceCollector and eventually run the matrix solver.
				if (ANALYZE) timer.capture();
				for (AQPair aqPair : aqPairs) {
					boolean addedSmooth = congruenceCollector.add(aqPair);
					if (addedSmooth) {
						int smoothCongruenceCount = congruenceCollector.getSmoothCongruenceCount();
						if (smoothCongruenceCount >= requiredSmoothCongruenceCount) {
							// Try to solve equation system
							if (ANALYZE) {
								ccDuration += timer.capture();
								solverRunCount++;
								if (DEBUG) LOG.debug("Run " + solverRunCount + ": #smooths = " + smoothCongruenceCount + ", #requiredSmooths = " + requiredSmoothCongruenceCount);
							}
							// It is faster to block the other threads while the solver is running,
							// because on modern CPUs a single thread runs at a higher clock rate.
							synchronized (aqPairBuffer) {
								matrixSolver.solve(congruenceCollector.getSmoothCongruences()); // throws FactorException
							}
								
							if (ANALYZE) solverDuration += timer.capture();
							// Extend equation system and continue searching smooth congruences
							requiredSmoothCongruenceCount += extraCongruences;
						}
					}
				}
				if (ANALYZE) ccDuration += timer.capture();
			}
		} catch (FactorException fe) {
			// now we have found a factor.
			BigInteger factor = fe.getFactor();
			if (ANALYZE) {
				solverDuration += timer.capture();
				// assemble reports from all threads
				PolyReport polyReport = threadArray[0].getPolyReport();
				SieveReport sieveReport = threadArray[0].getSieveReport();
				TDivReport tdivReport = threadArray[0].getTDivReport();
				for (int threadIndex=1; threadIndex<numberOfThreads; threadIndex++) {
					polyReport.add(threadArray[threadIndex].getPolyReport());
					sieveReport.add(threadArray[threadIndex].getSieveReport());
					tdivReport.add(threadArray[threadIndex].getTDivReport());
				}
				CongruenceCollectorReport ccReport = congruenceCollector.getReport();
				// solverReport is not urgently needed
				
				long initPolyDuration = polyReport.getTotalDuration(numberOfThreads);
				long sieveDuration = sieveReport.getTotalDuration(numberOfThreads);
				long tdivDuration = tdivReport.getTotalDuration(numberOfThreads);
				
				// report results
				LOG.info(getName() + ":");
				LOG.info("Found factor " + factor + " (" + factor.bitLength() + " bits) of N=" + N + " in " + TimeUtil.timeStr(timer.totalRuntime()));
				int pMaxBits = 32 - Integer.numberOfLeadingZeros(pMax);
				LOG.info("    multiplier k = " + k + ", kN%8 = " + kN.mod(I_8) + ", primeBaseSize = " + primeBaseSize + ", pMax = " + pMax + " (" + pMaxBits + " bits), sieveArraySize = " + adjustedSieveArraySize);
				LOG.info("    polyGenerator: " + polyReport.getOperationDetails());
				LOG.info("    tDiv: " + tdivReport.getOperationDetails());
				if (ANALYZE_LARGE_FACTOR_SIZES) LOG.info("        " +  tdivReport.getQRestSizes());
				LOG.info("    cc: " + ccReport.getOperationDetails());
				if (ANALYZE_LARGE_FACTOR_SIZES) {
					LOG.info("        " + ccReport.getPartialBigFactorSizes());
					LOG.info("        " + ccReport.getSmoothBigFactorSizes());
					LOG.info("        " + ccReport.getSmoothBigFactorPercentiles());
					LOG.info("        " + ccReport.getNonIntFactorPercentages());
				}
				if (ANALYZE_Q_SIGNS) {
					LOG.info("        " + ccReport.getPartialQSignCounts());
					LOG.info("        " + ccReport.getSmoothQSignCounts());
				}
				LOG.info("    #solverRuns = " + solverRunCount + ", #tested null vectors = " + matrixSolver.getTestedNullVectorCount());
				LOG.info("    Approximate phase timings: powerTest=" + powerTestDuration + "ms, initN=" + initNDuration + "ms, createThreads=" + createThreadDuration + "ms, initPoly=" + initPolyDuration + "ms, sieve=" + sieveDuration + "ms, tdiv=" + tdivDuration + "ms, cc=" + ccDuration + "ms, solver=" + solverDuration + "ms");
				LOG.info("    -> initPoly sub-timings: " + polyReport.getPhaseTimings(numberOfThreads));
				LOG.info("    -> sieve sub-timings: " + sieveReport.getPhaseTimings(numberOfThreads));
				LOG.info("    -> tdiv sub-timings: " + tdivReport.getPhaseTimings(numberOfThreads));
				// CC and solver have no sub-timings yet
			}
			
			// kill all threads & release memory
			long killStart = System.currentTimeMillis();
			for (int threadIndex=0; threadIndex<numberOfThreads; threadIndex++) {
				killThread(threadArray[threadIndex]);
				threadArray[threadIndex].cleanUp(); // e.g. let sieve release native memory !
				threadArray[threadIndex] = null;
			}
			if (DEBUG) LOG.debug("Killing threads took " + (System.currentTimeMillis()-killStart) + "ms"); // usually 0-16 ms, no problem
			apg.cleanUp();
			congruenceCollector.cleanUp();
			matrixSolver.cleanUp();
			// done
			return factor;
		}
	}

	private byte[] computeLogPArray(int[] primesArray, int primeBaseSize, float lnPMultiplier) {
		byte[] logPArray = new byte[primeBaseSize];
		for (int i=primeBaseSize-1; i>=0; i--) {
			logPArray[i] = (byte) ((float) Math.log(primesArray[i]) * lnPMultiplier + 0.5F);
		}
		return logPArray;
	}

	abstract protected PSIQSThreadBase createThread(
			int k, BigInteger N, BigInteger kN, int d, SieveParams sieveParams, BaseArrays baseArrays,
			AParamGenerator apg, AQPairBuffer aqPairBuffer, int threadIndex);
	
	private void killThread(PSIQSThreadBase t) {
    	while (t.isAlive()) {
    		if (DEBUG) LOG.debug("request to kill thread " + t.getName() + " ...");
    		// Thread.interrupt() is unsafe, it may block the program when the thread is just aquiring a lock
    		// It is safer to set a flag and let the thread check it outside any locks.
			t.setFinishNow();
			if (DEBUG) {
				StackTraceElement[] stackTrace = t.getStackTrace();
				if (stackTrace.length>0) {
	    			LOG.debug("thread " + t.getName() + " is in");
					for (StackTraceElement elem : stackTrace) LOG.debug(elem.toString());
				} else {
	    			LOG.debug("thread " + t.getName() + " has empty stack trace");
				}
			}
    		try {
    			t.join();
    			// XXX: Joining a thread may fail when it is in a synchronized block.
    			// If I remember well, this is a problem with unsafe atomic operations in Java.
    			// It happens most likely for small N.
    			if (DEBUG) LOG.debug("thread " + t.getName() + " joined");
    		} catch (InterruptedException e) {
    			if (DEBUG) LOG.debug("thread " + t.getName() + " interrupted main thread");
    		}
     	}
   		if (DEBUG) LOG.debug("thread " + t.getName() + " has been killed.");
	}
}
