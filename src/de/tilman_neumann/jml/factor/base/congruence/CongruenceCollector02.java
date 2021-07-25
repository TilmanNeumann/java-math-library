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
package de.tilman_neumann.jml.factor.base.congruence;

import static de.tilman_neumann.jml.factor.base.GlobalFactoringOptions.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.FactorException;
import de.tilman_neumann.jml.factor.base.matrixSolver.FactorTest;
import de.tilman_neumann.jml.factor.base.matrixSolver.MatrixSolver;
import de.tilman_neumann.util.Multiset;
import de.tilman_neumann.util.SortedMultiset_BottomUp;
import de.tilman_neumann.util.Timer;

/**
 * Collects smooth and partial congruences, uses cycle counting and finding algorithms instead of partial solver.
 * 
 * @author Tilman Neumann
 */
// TODO experimental, still buggy
public class CongruenceCollector02 implements CongruenceCollector {
	private static final Logger LOG = Logger.getLogger(CongruenceCollector02.class);
	private static final boolean DEBUG = false; // used for logs and asserts

	/** smooth congruences */
	private ArrayList<Smooth> smoothCongruences;

	/** factor tester */
	private FactorTest factorTest;
	/** cycle counter/finder (experimental) */
	private CycleFinder cycleFinder;
	
	// The number of congruences we need to find before we try to solve the smooth congruence equation system:
	// We want: #equations = #variables + some extra congruences
	private int requiredSmoothCongruenceCount;
	// extra congruences to have a bigger chance that the equation system solves. the likelihood is >= 1-2^-(extraCongruences+1)
	private int extraCongruences;
	
	private MatrixSolver matrixSolver;
	
	// Storing a found factor in this class permits it to be retrieved by multiple threads
	public BigInteger factor;

	// statistics
	private int perfectSmoothCount, smoothFromPartialsCount;
	private int[] smoothFromPartialCounts, partialCounts; // unused
	private Multiset<Integer>[] partialQRestSizes, partialBigFactorSizes; // unused
	private Multiset<Integer>[] smoothQRestSizes, smoothBigFactorSizes; // unused
	private int partialWithPositiveQCount; // unused
	private int smoothWithPositiveQCount;
	
	private Timer timer = new Timer();
	private long ccDuration, solverDuration;
	private int solverRunCount;

	/**
	 * Default constructor that expects 10 more equations than variables to run the matrix solver.
	 */
	public CongruenceCollector02() {
		this(10);
	}

	/**
	 * Full constructor.
	 * @param extraCongruences The difference #equations-#variables required before the solver is started.
	 */
	public CongruenceCollector02(int extraCongruences) {
		this.extraCongruences = extraCongruences;
	}
	
	@Override
	public void initialize(BigInteger N, int primeBaseSize, MatrixSolver matrixSolver, FactorTest factorTest) {
		this.initialize(N, factorTest);
		this.requiredSmoothCongruenceCount = primeBaseSize + extraCongruences;
		this.matrixSolver = matrixSolver;
		ccDuration = solverDuration = 0;
		solverRunCount = 0;
		factor = null;
	}

	@Override
	public void initialize(BigInteger N, FactorTest factorTest) {
		smoothCongruences = new ArrayList<Smooth>();
		this.factorTest = factorTest;
		cycleFinder = new CycleFinder(2);
		
		// statistics
		if (ANALYZE) {
			perfectSmoothCount = 0;
			// zero-initialized smoothFromPartialCounts: index 0 -> from 1-partials, index 1 -> from 2-partials, index 2 -> from 2-partials
			smoothFromPartialCounts = new int[3];
			partialCounts = new int[3];
		}
		if (ANALYZE_LARGE_FACTOR_SIZES) {
			// collected vs. useful big factor and QRest bit sizes distinguished by the number of large primes
			int maxLPCount = 5; // works up to 4LP
			smoothQRestSizes = createSizeCountsArray(maxLPCount);
			smoothBigFactorSizes = createSizeCountsArray(maxLPCount);
			partialQRestSizes = createSizeCountsArray(maxLPCount);
			partialBigFactorSizes = createSizeCountsArray(maxLPCount);
		}
		if (ANALYZE_Q_SIGNS) {
			// Q-analysis
			partialWithPositiveQCount = 0;
			smoothWithPositiveQCount = 0;
		}
	}
	
	@SuppressWarnings("unchecked")
	private SortedMultiset_BottomUp<Integer>[] createSizeCountsArray(int maxLPCount) {
		SortedMultiset_BottomUp<Integer>[] array = new SortedMultiset_BottomUp[maxLPCount];
		for (int i=0; i<maxLPCount; i++) {
			array[i] = new SortedMultiset_BottomUp<Integer>();
		}
		return array;
	}
	
	@Override
	public void collectAndProcessAQPairs(List<AQPair> aqPairs) {
		//LOG.debug("add " + aqPairs.size() + " new AQ-pairs to CC");
		try {
			// Add new data to the congruenceCollector and eventually run the matrix solver.
			if (ANALYZE) timer.capture();
			for (AQPair aqPair : aqPairs) {
				add(aqPair);
				int smoothCongruenceCount = getSmoothCongruenceCount() + smoothFromPartialsCount;
				if (smoothCongruenceCount >= requiredSmoothCongruenceCount) {
					// Try to solve equation system
					if (ANALYZE) {
						ccDuration += timer.capture();
						solverRunCount++;
						if (DEBUG) LOG.debug("Run " + solverRunCount + ": #smooths = " + smoothCongruenceCount + ", #requiredSmooths = " + requiredSmoothCongruenceCount);
					}
					ArrayList<Smooth> congruences = getSmoothCongruences();
					ArrayList<Smooth> smoothsFromPartials = cycleFinder.findIndependentCycles();
					smoothsFromPartials.addAll(congruences);
					// The matrix solver should also run synchronized, because blocking the other threads
					// means that the current thread can run at a higher clock rate.
					matrixSolver.solve(congruences); // throws FactorException
					if (ANALYZE) solverDuration += timer.capture();
					// Extend equation system and continue searching smooth congruences
					requiredSmoothCongruenceCount += extraCongruences;
				}
			}
			if (ANALYZE) ccDuration += timer.capture();
		} catch (FactorException fe) {
			factor = fe.getFactor();
			if (ANALYZE) solverDuration += timer.capture();
			return;
		}
	}
	
	@Override
	public boolean add(AQPair aqPair) throws FactorException {
		if (DEBUG) LOG.debug("new aqPair = " + aqPair);
		if (aqPair instanceof Smooth) {
			Smooth smooth = (Smooth) aqPair;
			boolean added = addSmooth(smooth);
			if (DEBUG) if (added) LOG.debug("Found new smooth congruence " + smooth + " --> #smooth = " + smoothCongruences.size() + ", #partials = " + getPartialCongruenceCount());
			if (ANALYZE) if (added) perfectSmoothCount++;
			return added;
		}
		
		// otherwise aqPair must be a partial with at least one large factor.
		Partial partial = (Partial) aqPair;
		int newCount = cycleFinder.addPartial(partial);
		boolean added = newCount > smoothFromPartialsCount;
		smoothFromPartialsCount = newCount;
		return added;
	}

	/**
	 * Add smooth congruence.
	 * @param smoothCongruence
	 * @return true if a smooth congruence was added
	 * @throws FactorException
	 */
	private boolean addSmooth(Smooth smoothCongruence) throws FactorException {
		if (smoothCongruence.isExactSquare()) {
			// We found a square congruence!
			factorTest.testForFactor(smoothCongruence.getAQPairs());
			// no FactorException -> the square congruence was improper -> drop it
			return false;
		}
		// No square -> add.
		// Here the same congruence may be added several times. This results in the need to test too many null vectors.
		// But avoiding such duplicates is asymptotically unfavourable because their likelihood decreases quickly.
		smoothCongruences.add(smoothCongruence);
		
		// Q-analysis
		if (ANALYZE_Q_SIGNS) if (smoothCongruence.getMatrixElements()[0] != -1) smoothWithPositiveQCount++;

		return true;
	}

	@Override
	public int getRequiredSmoothCongruenceCount() {
		return requiredSmoothCongruenceCount;
	}

	@Override
	public int getSmoothCongruenceCount() {
		return smoothCongruences.size();
	}

	@Override
	public ArrayList<Smooth> getSmoothCongruences() {
		return smoothCongruences;
	}
	
	@Override
	public int getPartialCongruenceCount() {
		return cycleFinder.getPartialCongruenceCount();
	}

	@Override
	public BigInteger getFactor() {
		return factor;
	}

	@Override
	public CongruenceCollectorReport getReport() {
		return new CongruenceCollectorReport(getPartialCongruenceCount(), smoothCongruences.size(), smoothFromPartialCounts, partialCounts, perfectSmoothCount,
											 partialQRestSizes, partialBigFactorSizes, smoothQRestSizes, smoothBigFactorSizes, partialWithPositiveQCount, smoothWithPositiveQCount);
	}
	
	@Override
	public long getCollectDuration() {
		return ccDuration;
	}
	
	@Override
	public long getSolverDuration() {
		return solverDuration;
	}
	
	@Override
	public int getSolverRunCount() {
		return solverRunCount;
	}

	@Override
	public void cleanUp() {
		smoothCongruences = null;
		factorTest = null;
	}
}