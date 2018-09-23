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

import java.util.Set;

import de.tilman_neumann.math.factor.FactorException;
import de.tilman_neumann.math.factor.base.congruence.AQPair;

/**
 * Simple interface to decouple congruence solver classes.
 * @author Tilman Neumann
 */
public interface NullVectorProcessor {
	/**
	 * Process a null vector found by a matrix solver.
	 * @param aqPairs the AQ-pairs of the null vector.
	 * @throws FactorException
	 */
	void processNullVector(Set<AQPair> aqPairs) throws FactorException;
}
