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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.FactorException;
import de.tilman_neumann.jml.factor.base.congruence.Smooth;
import de.tilman_neumann.jml.factor.base.matrixSolver.util.CompareCongruence;
import de.tilman_neumann.jml.factor.base.matrixSolver.util.CompareEntry;
import de.tilman_neumann.jml.factor.base.matrixSolver.util.IntHolder;

/**
 * Base implementation for a congruence equation system (the "LinAlg phase matrix") solver.
 * Much faster than the first version due to great improvements by Dave McGuigan.
 * 
 * @author Tilman Neumann, Dave McGuigan
 */
abstract public class MatrixSolverBase03 extends MatrixSolverBase02 {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(MatrixSolverBase03.class);
	
	/**
	 * Main method to solve a congruence equation system.
	 * @param congruences the congruences forming the equation system
	 * @throws FactorException if a factor of N was found
	 */
	public void solve(Collection<? extends Smooth> congruences) throws FactorException {
		//LOG.debug("#congruences = " + congruences.size());
		
		// 1. remove singletons
		int nextPrimeIndex = 0;
		Map<Integer,Integer> primeIndexMap = new HashMap<Integer,Integer>(congruences.size());
		for (Smooth congruence : congruences) {
			for (Integer p : congruence.getMatrixElements()) {
				if (!primeIndexMap.containsKey(p)) {
					primeIndexMap.put(p, nextPrimeIndex++);
				}
			}
		}
		
		// When removing, it may be better to leave a few singletons vs. the cost of removing when the 
		// number of congruences gets large.
		final int DELTA = 0;
		int lastSize = congruences.size();
		List<Smooth> noSingletons = removeSingletons(congruences, primeIndexMap);
		while (lastSize-noSingletons.size()>DELTA) {
			lastSize = noSingletons.size();
			noSingletons = removeSingletons(noSingletons, primeIndexMap);
		}
		
		Object[] ns = noSingletons.toArray();
		Arrays.sort(ns, new CompareCongruence());
		noSingletons.clear();
		for (Object o : ns) {
			Smooth s = (Smooth)o;
			noSingletons.add(s);
		}

		// 2. Re-map odd-exp-elements to column indices and sort if appropriate.		
		Map<Integer, Integer> factors_2_indices;
		if (sortIndices()) {
			Map<Integer,IntHolder> oddExpFactors = new HashMap<Integer,IntHolder>(primeIndexMap.size());
			for (Smooth congruence : noSingletons) {
				for (int f : congruence.getMatrixElements()) {
					IntHolder h = oddExpFactors.get(f);
					if (h == null) {
						oddExpFactors.put(f, new IntHolder());
					} else {
						h.increment();
					}
				}
			}
			
			factors_2_indices= new HashMap<Integer,Integer>(oddExpFactors.size());
			Set<Map.Entry<Integer,IntHolder>> l = oddExpFactors.entrySet();
			Object[] sorted = l.toArray();
			Arrays.sort(sorted, new CompareEntry());
			for (int index=0; index<sorted.length; index++) {
				@SuppressWarnings("unchecked")
				Entry<Integer,IntHolder> e = (Entry<Integer,IntHolder>)sorted[index];
				factors_2_indices.put(e.getKey(), index);
			}
		} else {
			Set<Integer> oddExpFactors = new HashSet<Integer>(primeIndexMap.size());
			for (Smooth congruence : noSingletons) {
				for (int f : congruence.getMatrixElements()) {
					oddExpFactors.add(f);
				}
			}
			
			factors_2_indices = new HashMap<Integer,Integer>(oddExpFactors.size());
			int index = 0;
			for (Integer f : oddExpFactors) {
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
}
