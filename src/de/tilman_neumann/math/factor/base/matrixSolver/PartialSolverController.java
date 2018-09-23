/*
 * PSIQS 4.0 is a Java library for integer factorization, including a parallel self-initializing quadratic sieve (SIQS).
 * Copyright (C) 2018  Tilman Neumann (www.tilman-neumann.de)
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
package de.tilman_neumann.math.factor.base.matrixSolver;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import de.tilman_neumann.math.factor.FactorException;
import de.tilman_neumann.math.factor.base.congruence.AQPair;
import de.tilman_neumann.math.factor.base.congruence.Congruence;
import de.tilman_neumann.math.factor.base.congruence.Smooth;
import de.tilman_neumann.math.factor.base.congruence.Smooth_Composite;

/**
 * A controller for the matrix solver used for partial congruence equations systems.
 * Solving such a system may give smooth congruences.
 * The controller pattern allows to have distinct matrix solver implementations, and a fallback procedure
 * that applies when a solver run gives only improper solutions.
 * 
 * @author Tilman Neumann
 */
public class PartialSolverController implements NullVectorProcessor {
	
	private MatrixSolver matrixSolver;

	private ArrayList<Smooth> foundSmoothCongruences;

	public PartialSolverController(MatrixSolver matrixSolver) {
		// this hook would be a memory leak if we'ld create many pairs of controller and solver objects;
		// but we have only 1 object pair in the whole application run time
		matrixSolver.setNullVectorProcessor(this);
		this.matrixSolver = matrixSolver;
	}
	
	public void initialize(BigInteger N, FactorTest factorTest) {
		// nothing
	}
	
	/**
	 * Solve a partial congruence equation system.
	 * @param rawCongruences the partial congruence equation system
	 * @return list of smooth congruences found
	 * @throws FactorException if a factor of N was found
	 */
	public ArrayList<Smooth> solve(Collection<? extends Congruence> rawCongruences) throws FactorException {
		foundSmoothCongruences = new ArrayList<Smooth>();
		matrixSolver.solve(rawCongruences);
		return foundSmoothCongruences;
	}
	
	@Override
	public void processNullVector(Set<AQPair> aqPairs) throws FactorException {
		// We found a smooth congruence from partials.
		// Checking for exact squares is done in CongenuenceCollector.addSMooth(), no need to do it here again...
		Smooth smoothCongruence = new Smooth_Composite(aqPairs);
		foundSmoothCongruences.add(smoothCongruence);
	}
	
	/**
	 * Release memory after a factorization.
	 */
	public void cleanUp() {
		foundSmoothCongruences = null;
	}
}
