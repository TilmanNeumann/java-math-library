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

import static de.tilman_neumann.jml.base.BigIntConstants.I_1;
import static de.tilman_neumann.jml.factor.base.GlobalFactoringOptions.*;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.FactorException;
import de.tilman_neumann.jml.factor.base.matrixSolver.FactorTest;
import de.tilman_neumann.jml.factor.base.matrixSolver.MatrixSolver;
import de.tilman_neumann.util.Multiset;
import de.tilman_neumann.util.SortedMultiset_BottomUp;
import de.tilman_neumann.util.Timer;

/**
 * First congruence collector using a cycle counter for 3LP.
 * With 3LP, the cycle count from the cycle counting algorithm is an upper bound of the true number of cycles.
 * So if it indicates that we might have found a new cycle, then the partial solver must be run to check that and resolve the smooth congruence.
 * 
 * @author Tilman Neumann
 */
// XXX needs more testing
public class CongruenceCollector03 implements CongruenceCollector {
	private static final Logger LOG = Logger.getLogger(CongruenceCollector03.class);
	private static final boolean DEBUG = false; // used for logs and asserts

	/** smooth congruences */
	private ArrayList<Smooth> smoothCongruences;
	/** 
	 * A map from big factors with odd exp to partial congruences.
	 * Here we need a 1:n relation because one partial can have several big factors;
	 * thus one big factor may be contained in many distinct partials.
	 */
	private HashMap<Long, ArrayList<Partial>> largeFactors_2_partials; // rbp !
	
	/** cycle counter */
	private CycleCounter cycleCounter = new CycleCounter3LP();
	
	/** A solver used to create smooth congruences from partials */
	private PartialSolver partialSolver = new PartialSolver01();
	
	/** factor tester */
	private FactorTest factorTest;
	
	// The number of congruences we need to find before we try to solve the smooth congruence equation system:
	// We want: #equations = #variables + some extra congruences
	private int requiredSmoothCongruenceCount;
	// extra congruences to have a bigger chance that the equation system solves. the likelihood is >= 1-2^-(extraCongruences+1)
	private int extraCongruences;
	
	private MatrixSolver matrixSolver;
	
	// Storing a found factor in this class permits it to be retrieved by multiple threads
	public BigInteger factor;

	// statistics
	private int totalPartialCount; // standard
	private int perfectSmoothCount; // needs ANALYZE
	private int cycleCount;
	private int[] smoothFromPartialCounts, partialCounts;
	private Multiset<Integer>[] partialQRestSizes, partialBigFactorSizes;
	private Multiset<Integer>[] smoothQRestSizes, smoothBigFactorSizes;
	private int partialWithPositiveQCount, smoothWithPositiveQCount;
	
	private Timer timer = new Timer();
	private long ccDuration, solverDuration;
	private int solverRunCount, testedNullVectorCount;

	/**
	 * Default constructor that expects 10 more equations than variables to run the matrix solver.
	 */
	public CongruenceCollector03() {
		this(10);
	}

	/**
	 * Full constructor.
	 * @param extraCongruences The difference #equations-#variables required before the solver is started.
	 */
	public CongruenceCollector03(int extraCongruences) {
		this.extraCongruences = extraCongruences;
	}
	
	@Override
	public void initialize(BigInteger N, int primeBaseSize, MatrixSolver matrixSolver, FactorTest factorTest) {
		this.initialize(N, factorTest);
		this.requiredSmoothCongruenceCount = primeBaseSize + extraCongruences;
		this.matrixSolver = matrixSolver;
		ccDuration = solverDuration = 0;
		solverRunCount = testedNullVectorCount = 0;
		factor = null;
	}

	@Override
	public void initialize(BigInteger N, FactorTest factorTest) {
		smoothCongruences = new ArrayList<Smooth>();
		largeFactors_2_partials = new HashMap<Long, ArrayList<Partial>>();
		this.factorTest = factorTest;
		
		// statistics
		cycleCount = 0;
		totalPartialCount = 0;
		if (ANALYZE) {
			perfectSmoothCount = 0;
			// zero-initialized smoothFromPartialCounts: index 0 -> from 1-partials, index 1 -> from 2-partials, index 2 -> from 3-partials
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
				boolean addedSmooth = add(aqPair);
				if (addedSmooth) {
					int smoothCongruenceCount = getSmoothCongruenceCount();
					if (smoothCongruenceCount >= requiredSmoothCongruenceCount) {
						// Try to solve equation system
						if (ANALYZE) {
							ccDuration += timer.capture();
							solverRunCount++;
							if (DEBUG) LOG.debug("Run " + solverRunCount + ": #smooths = " + smoothCongruenceCount + ", #requiredSmooths = " + requiredSmoothCongruenceCount);
						}
						ArrayList<Smooth> congruences = getSmoothCongruences();
						// The matrix solver should also run synchronized, because blocking the other threads
						// means that the current thread can run at a higher clock rate.
						matrixSolver.solve(congruences); // throws FactorException
						if (ANALYZE) {
							testedNullVectorCount += matrixSolver.getTestedNullVectorCount();
							solverDuration += timer.capture();
						}
						// Extend equation system and continue searching smooth congruences
						requiredSmoothCongruenceCount += extraCongruences;
					}
				}
			}
			if (ANALYZE) ccDuration += timer.capture();
		} catch (FactorException fe) {
			factor = fe.getFactor();
			if (ANALYZE) {
				testedNullVectorCount += matrixSolver.getTestedNullVectorCount();
				solverDuration += timer.capture();
			}
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
		final Long[] bigFactors = partial.getLargeFactorsWithOddExponent();
		if (DEBUG) {
			LOG.debug("bigFactors = " + Arrays.toString(bigFactors));
			assertTrue(bigFactors.length > 0);
		}
		
		int lastCycleCount = cycleCount;
		cycleCount = cycleCounter.addPartial(partial, /* dummy values, no debugging in this class yet*/ -123456789, null);
		if (cycleCount > lastCycleCount) {
			// The cycle counter suggests that there might be a new smooth. Use the partial solver to check that
			// we use Long[] and not long[] for bigFactors, because in the following they will be used a lot in Collections
			
			// Check if the partial helps to assemble a smooth congruence:
			// First collect all partials that are somehow related to the new partial via big factors:
			HashSet<Partial> relatedPartials = findRelatedPartials(bigFactors); // bigFactors is not modified in the method
			if (DEBUG) LOG.debug("#relatedPartials = " + relatedPartials.size());
			if (relatedPartials.size()>0) {
				// We found some "old" partials that share at least one big factor with the new partial.
				// Since relatedPartials is a set, we can not get duplicate AQ-pairs.
				relatedPartials.add(partial);
				// Solve partial congruence equation system
				Smooth foundSmooth = partialSolver.solve(relatedPartials); // throws FactorException
				if (foundSmooth != null) {
					// We found a smooth from the new partial
					boolean added = addSmooth(foundSmooth);
					if (ANALYZE) {
						if (added) {
							// count kind of partials that helped to find smooths
							int maxLargeFactorCount = 0;
							for (AQPair aqPairFromSmooth : foundSmooth.getAQPairs()) {
								int largeFactorCount = aqPairFromSmooth.getNumberOfLargeQFactors();
								if (largeFactorCount > maxLargeFactorCount) maxLargeFactorCount = largeFactorCount;
							}
							smoothFromPartialCounts[maxLargeFactorCount-1]++;
							if (DEBUG) {
								LOG.debug("Found smooth congruence from " + maxLargeFactorCount + "-partial --> #smooth = " + smoothCongruences.size() + ", #partials = " + getPartialCongruenceCount());
								//for (Partial par : relatedPartials) {
								//	LOG.debug("    related partial has large factors " + Arrays.toString(par.getLargeFactorsWithOddExponent()));
								//}
							}
						}
					}
					if (ANALYZE_LARGE_FACTOR_SIZES) {
						if (added) {
							// register size of large factors that helped to find smooths
							BigInteger prod = I_1;
							for (Long bigFactor : bigFactors) {
								int bigFactorBits = 64 - Long.numberOfLeadingZeros(bigFactor);
								smoothBigFactorSizes[bigFactors.length].add(bigFactorBits);
								prod = prod.multiply(BigInteger.valueOf(bigFactor));
							}
							smoothQRestSizes[bigFactors.length].add(prod.bitLength());
						}
					}
					return added;
					// Not adding the new partial is sufficient to keep the old partials linear independent,
					// which is required to avoid duplicate solutions.
				}
			}
		}

		// The new partial did not yield a smooth congruence, so just keep it:
		addPartial(partial, bigFactors);
		totalPartialCount++;
		if (DEBUG) LOG.debug("Found new partial relation " + aqPair + " --> #smooth = " + smoothCongruences.size() + ", #partials = " + totalPartialCount);
		if (ANALYZE) partialCounts[bigFactors.length-1]++;
		return false; // no smooth added
	}
	
	/**
	 * Find "old" partials related to a new partial.
	 * The large factors of the new partial remain unaltered.
	 * 
	 * @param largeFactorsOfPartial the large factors with odd exponent of the new partial
	 * @return set of related partial congruences
	 */
	private HashSet<Partial> findRelatedPartials(Long[] largeFactorsOfPartial) {
		HashSet<Long> processedLargeFactors = new HashSet<>();
		HashSet<Partial> relatedPartials = new HashSet<>(); // we need a set to avoid adding the same partial more than once
		ArrayList<Long> currentLargeFactors = new ArrayList<>();
		for (Long largeFactor : largeFactorsOfPartial) {
			currentLargeFactors.add(largeFactor);
		}
		while (currentLargeFactors.size()>0) {
			if (DEBUG) LOG.debug("currentLargeFactors = " + currentLargeFactors);
			ArrayList<Long> nextLargeFactors = new ArrayList<>(); // no Set required, ArrayList has faster iteration
			for (Long largeFactor : currentLargeFactors) {
				processedLargeFactors.add(largeFactor);
				ArrayList<Partial> partialList = largeFactors_2_partials.get(largeFactor);
				if (partialList!=null && partialList.size()>0) {
					for (Partial relatedPartial : partialList) {
						relatedPartials.add(relatedPartial);
						for (Long nextLargeFactor : relatedPartial.getLargeFactorsWithOddExponent()) {
							if (!processedLargeFactors.contains(nextLargeFactor)) nextLargeFactors.add(nextLargeFactor);
						}
					}
				}
			}
			currentLargeFactors = nextLargeFactors;
		}
		return relatedPartials;
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
	
	private void addPartial(Partial newPartial, Long[] bigFactors) {
		for (Long bigFactor : bigFactors) {
			ArrayList<Partial> partialCongruenceList = largeFactors_2_partials.get(bigFactor);
			// For large N, most large factors appear only once. Therefore we create an ArrayList with initialCapacity=1 to safe memory.
			// Even less memory would be needed if we had a HashMap<Long, Object> bigFactors_2_partialCongruences
			// and store AQPairs or AQPair[] in the Object part. But I do not want to break the generics...
			if (partialCongruenceList==null) partialCongruenceList = new ArrayList<Partial>(1);
			partialCongruenceList.add(newPartial);
			largeFactors_2_partials.put(bigFactor, partialCongruenceList);
		}
		
		if (ANALYZE_LARGE_FACTOR_SIZES) {
			BigInteger prod = I_1;
			for (Long bigFactor : bigFactors) {
				int bigFactorBits = 64 - Long.numberOfLeadingZeros(bigFactor);
				partialBigFactorSizes[bigFactors.length].add(bigFactorBits);
				prod = prod.multiply(BigInteger.valueOf(bigFactor));
			}
			partialQRestSizes[bigFactors.length].add(prod.bitLength());
		}

		if (ANALYZE_Q_SIGNS) {
			if (newPartial.smallFactors[0] != -1 || (newPartial.smallFactorExponents[0]&1) == 0) partialWithPositiveQCount++;
		}
	}
	
	@SuppressWarnings("unused")
	private void dropPartial(Partial partial, Long[] bigFactors) {
		for (Long bigFactor : bigFactors) {
			ArrayList<Partial> partialCongruenceList = largeFactors_2_partials.get(bigFactor);
			partialCongruenceList.remove(partial);
			if (partialCongruenceList.size()==0) {
				// partialCongruenceList is empty now -> drop the whole entry
				largeFactors_2_partials.remove(bigFactor);
			}
		}
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
		return totalPartialCount;
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
	public int getTestedNullVectorCount() {
		return testedNullVectorCount;
	}
	
	@Override
	public void cleanUp() {
		smoothCongruences = null;
		largeFactors_2_partials = null;
		factorTest = null;
		partialSolver.cleanUp();
	}
}