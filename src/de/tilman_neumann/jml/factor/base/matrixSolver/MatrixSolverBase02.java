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
package de.tilman_neumann.jml.factor.base.matrixSolver;

import static de.tilman_neumann.jml.factor.base.GlobalFactoringOptions.ANALYZE;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.FactorException;
import de.tilman_neumann.jml.factor.base.congruence.AQPair;
import de.tilman_neumann.jml.factor.base.congruence.Smooth;

/**
 * Base implementation for a congruence equation system (the "LinAlg phase matrix") solver.
 * Much faster than the first version due to great improvements by Dave McGuigan.
 * 
 * @author Tilman Neumann, Dave McGuigan
 */
abstract public class MatrixSolverBase02 extends MatrixSolverBase01 {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(MatrixSolverBase02.class);

	/** factor tester */
	private FactorTest factorTest;

	// for debugging only
	private int testedNullVectorCount;
	
	public abstract String getName();
	
	/**
	 * Initialize for a new N.
	 * @param N
	 * @param factorTest
	 */
	public void initialize(BigInteger N, FactorTest factorTest) {
		this.factorTest = factorTest;
		if (ANALYZE) this.testedNullVectorCount = 0;
	}

	private class IntHolder {
		int value;
		
		public IntHolder() {
			value = 1;
		}
		
		public void increment() {
			value++;
		}
	}
	
	public class CompareEntry implements Comparator<Object> {

		@Override
		public int compare(Object o1, Object o2) {
			@SuppressWarnings("unchecked")
			Entry<Integer,IntHolder> e1 = (Entry<Integer,IntHolder>)o1;
			@SuppressWarnings("unchecked")
			Entry<Integer,IntHolder> e2 = (Entry<Integer,IntHolder>)o2;
			return e2.getValue().value-e1.getValue().value;
		}
		
	}
	/**
	 * Main method to solve a congruence equation system.
	 * @param congruences the congruences forming the equation system
	 * @throws FactorException if a factor of N was found
	 */
	public void solve(Collection<? extends Smooth> congruences) throws FactorException {
		//LOG.debug("#congruences = " + congruences.size());
		
		// 1. remove singletons
		List<Smooth> noSingletons = new ArrayList<Smooth>(congruences.size());
		int nextPrimeIndex = 0;
		Map<Integer,Integer> primeIndexMap = new HashMap<Integer,Integer>(congruences.size());
		for(Smooth congruence : congruences) {
			for(Integer p : congruence.getMatrixElements()) {
				if(!primeIndexMap.containsKey(p)) {
					primeIndexMap.put(p, nextPrimeIndex++);
				}
			}
		}
		
		// When removing, it may be better to leave a few singletons vs. the cost of removing when the 
		// number of congruences gets large.
		final int DELTA = 0;
		int lastSize = congruences.size();
		noSingletons = removeSingletons(congruences, primeIndexMap);
		while(lastSize-noSingletons.size()>DELTA) {
			lastSize = noSingletons.size();
			noSingletons = removeSingletons(noSingletons, primeIndexMap);
		}

		// 2. Re-map odd-exp-elements to column indices and sort if appropriate.		
		Map<Integer, Integer> factors_2_indices;
		if(sortIndices()) {
			Map<Integer,IntHolder> oddExpFactors = new HashMap<Integer,IntHolder>(primeIndexMap.size());
			for (Smooth congruence : noSingletons) {
				for(int f : congruence.getMatrixElements()) {
					IntHolder h = oddExpFactors.get(f);
					if(h == null) {
						oddExpFactors.put(f, new IntHolder());
					} else {
						h.increment();
					}
				}
			}
			
			factors_2_indices= new HashMap<Integer,Integer>(oddExpFactors.size());
			Set<Map.Entry<Integer,IntHolder>> l = oddExpFactors.entrySet();
			Object[] sorted = l.toArray();
			Arrays.sort(sorted,new CompareEntry());
			for(int index=0; index<sorted.length; index++) {
				@SuppressWarnings("unchecked")
				Entry<Integer,IntHolder> e = (Entry<Integer,IntHolder>)sorted[index];
				factors_2_indices.put(e.getKey(), index);
			}
		} else {
			Set<Integer> oddExpFactors = new HashSet<Integer>(primeIndexMap.size());
			for (Smooth congruence : noSingletons) {
				for(int f : congruence.getMatrixElements()) {
					oddExpFactors.add(f);
				}
			}
			
			factors_2_indices= new HashMap<Integer,Integer>(oddExpFactors.size());
			int index = 0;
			for(Integer f : oddExpFactors) {
				factors_2_indices.put(f, index++);
			}
			
		}

		// 4+5. Create & solve matrix
		try {
		solve(noSingletons, factors_2_indices);
		} catch (FactorException e) {
			throw e;
		} catch (Exception ee) {
			ee.printStackTrace();
			throw ee;
		}
	}
	
	private class StackEntry {
		public Smooth congruence;
		public int currentPrimeIndex;
		public StackEntry(Smooth congruence, int currentPrimeIndex) {
			this.congruence = congruence;
			this.currentPrimeIndex = currentPrimeIndex;
		}	
	}
	

	
	/**
	 * Remove singletons by maintaining a structure of what primes have been seen
	 * multiple times. When a prime is first seen the congurence is held as a 
	 * possible singleton. When a prime has been matched, processing of the current 
	 * congruence and held congruence can proceed. Any other congruences with that
	 * prime seen after matching can just proceed.
	 * @param congruences - collecting to be reduced
	 * @param primeIndexMap - Map of primes to unique indexes
	 * @return list of entries with singletons removed.
	 */
	public List<Smooth> removeSingletons(Collection<? extends Smooth> congruences, Map<Integer,Integer> primeIndexMap) {
		List<Smooth> noSingles = new ArrayList<Smooth>(congruences.size());
		LinkedList<StackEntry> stack = new LinkedList<StackEntry>();
		StackEntry[] onHold = new StackEntry[primeIndexMap.size()];
		boolean[] haveMatch = new boolean[primeIndexMap.size()];
		for(Smooth congruence : congruences) {
			StackEntry entry = new StackEntry(congruence,0);
			while(entry != null) {
				Smooth currentCongruence = entry.congruence;
				int factor = currentCongruence.getMatrixElements()[entry.currentPrimeIndex];
				int ci = primeIndexMap.get(factor);
				if(haveMatch[ci]) {
					// This prime has been seen multiple times, just check the next prime.
					entry.currentPrimeIndex++;
				} else {
					// This prime hasn't been matched yet.
					if(onHold[ci] == null) {
						// First time seeing this prime. Hang on to the congruence.
						onHold[ci] = entry;
						entry = null;
					} else {
						// Second time seeing this prime. It's now matched. 
						// Stack the held congurence for further processing.
						haveMatch[ci] = true;
						StackEntry held = onHold[ci];
						held.currentPrimeIndex++;
						if(held.currentPrimeIndex>=held.congruence.getMatrixElements().length) {
							noSingles.add(held.congruence);
						} else {
							stack.addFirst(held);
						}
						entry.currentPrimeIndex++;
					}
				}
				// the current entry may have examined all it's primes
				if(entry != null) {
					if(entry.currentPrimeIndex>=entry.congruence.getMatrixElements().length) {
						// every factor seen at least twice
						noSingles.add(entry.congruence);
						entry = null;						
					}
				}
				// if teh current entry is complete, see it there's more in the stack to do.
				if(entry == null) {
					if(stack.size()>0) {
						entry = stack.removeFirst();
					}
				}
			}
		}
		
		return noSingles;
	}

	/**
	 * Returns true if this solver benefits from sorting the prime indices
	 */
	public abstract boolean sortIndices();
	

	/**
	 * Create the matrix from the pre-processed congruences and solve it.
	 * @param congruences
	 * @param factors_2_columnIndices map from factors to matrix column indices
	 * @throws FactorException 
	 */
	abstract protected void solve(List<Smooth> congruences, Map<Integer, Integer> factors_2_columnIndices) throws FactorException;

	public void processNullVector(Set<AQPair> aqPairs) throws FactorException {
		// found square congruence -> check for factor
		if (ANALYZE) testedNullVectorCount++;
		factorTest.testForFactor(aqPairs);
		// no factor exception -> drop improper square congruence
	}
	
	/**
	 * @return the number of solver runs needed (so far). Is not populated (i.e. 0) if ANALYZE_SOLVER_RUNS==false.
	 */
	public int getTestedNullVectorCount() {
		return testedNullVectorCount;
	}
	
	/**
	 * Release memory after a factorization.
	 */
	public void cleanUp() {
		factorTest = null;
	}
}
