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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.FactorException;
import de.tilman_neumann.jml.factor.base.congruence.AQPair;
import de.tilman_neumann.jml.factor.base.congruence.Smooth;

/**
 * Base implementation for a congruence equation system (the "LinAlg phase matrix") solver.
 * 
 * @author Tilman Neumann
 */
abstract public class MatrixSolverBase01 {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(MatrixSolverBase01.class);

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

	/**
	 * Main method to solve a congruence equation system.
	 * @param congruences the congruences forming the equation system
	 * @throws FactorException if a factor of N was found
	 */
	public void solve(Collection<? extends Smooth> congruences) throws FactorException {
		// 1. Create
		// a) a copy of the congruences list, to avoid that the original list is modified during singleton removal.
		// b) a map from (primes with odd power) to congruences. A sorted TreeMap would be nice because then
		//    small primes get small indices in step 4, but experiments showed that HashMap is faster.
		//LOG.debug("#congruences = " + congruences.size());
		@SuppressWarnings({ "unchecked", "rawtypes" })
		ArrayList<Smooth> congruencesCopy = new ArrayList(congruences.size());
		Map<Integer, ArrayList<Smooth>> oddExpFactors_2_congruences = new HashMap<Integer, ArrayList<Smooth>>();
		for (Smooth congruence : congruences) {
			congruencesCopy.add(congruence);
			addToColumn2RowMap(congruence, oddExpFactors_2_congruences);
		}
		// 2. remove singletons
		removeSingletons(congruencesCopy, oddExpFactors_2_congruences);
		// 3. Map odd-exp-elements to column indices. Sorting is not required.
		Map<Integer, Integer> factors_2_indices = createFactor2ColumnIndexMap(oddExpFactors_2_congruences);
		// 4+5. Create & solve matrix
		solve(congruencesCopy, factors_2_indices);
	}
	
	/**
	 * Remove singletons from <code>congruences</code>.
	 * This can reduce the size of the equation system; actually it never diminishes the difference (#eqs - #vars).
	 * It is very fast, too - like 60ms for a matrix for which solution via Gauss elimination takes 1 minute.
	 * 
	 * @param congruences 
	 * @param oddExpFactors_2_congruences 
	 */
	protected void removeSingletons(List<Smooth> congruences, Map<Integer, ArrayList<Smooth>> oddExpFactors_2_congruences) {
		// Parse all congruences as long as we find a singleton in a complete pass
		boolean foundSingleton;
		do {
			foundSingleton = false;
			Iterator<? extends Smooth> congruenceIter = congruences.iterator();
			while (congruenceIter.hasNext()) {
				Smooth congruence = congruenceIter.next();
				Integer[] oddExpFactors = congruence.getMatrixElements();
				for (Integer oddExpFactor : oddExpFactors) {
					if (oddExpFactors_2_congruences.get(oddExpFactor).size()==1) {
						// found singleton -> remove from list
						//LOG.debug("Found singleton -> remove " + congruence);
						congruenceIter.remove();
						// remove from oddExpFactors_2_congruences so we can detect further singletons
						removeFromColumn2RowMap(congruence, oddExpFactors_2_congruences);
						foundSingleton = true;
						break;
					}
				}
			} // one pass finished
		} while (foundSingleton && congruences.size()>0);
		// now all singletons have been removed from congruences.
		//LOG.debug("#congruences after removing singletons: " + congruences.size());
	}
	
	private void addToColumn2RowMap(Smooth congruence, Map<Integer, ArrayList<Smooth>> oddExpFactors_2_congruences) {
		for (Integer factor : congruence.getMatrixElements()) {
			ArrayList<Smooth> congruenceList = oddExpFactors_2_congruences.get(factor);
			if (congruenceList == null) {
				congruenceList = new ArrayList<Smooth>();
				oddExpFactors_2_congruences.put(factor, congruenceList);
			}
			congruenceList.add(congruence);
		}
	}
	
	private void removeFromColumn2RowMap(Smooth congruence, Map<Integer, ArrayList<Smooth>> oddExpFactors_2_congruences) {
		for (Integer factor : congruence.getMatrixElements()) {
			ArrayList<Smooth> congruenceList = oddExpFactors_2_congruences.get(factor);
			congruenceList.remove(congruence);
			if (congruenceList.size()==0) {
				// there are no more congruences with the current factor
				oddExpFactors_2_congruences.remove(factor);
			}
		}
	}

	/**
	 * Create a map from odd-exp-elements to matrix column indices.
	 * 
	 * @param oddExpFactors_2_congruences unsorted map from factors to the congruences in which they appear with odd exponent
	 * @return map from factors to column indices
	 */
	protected Map<Integer, Integer> createFactor2ColumnIndexMap(Map<Integer, ArrayList<Smooth>> oddExpFactors_2_congruences) {
		int index = 0;
		Map<Integer, Integer> factors_2_columnIndices = new HashMap<Integer, Integer>();
		for (Integer factor : oddExpFactors_2_congruences.keySet()) {
			factors_2_columnIndices.put(factor, index++);
		}
		return factors_2_columnIndices;
	}

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
