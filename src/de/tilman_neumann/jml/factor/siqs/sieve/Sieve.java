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
package de.tilman_neumann.jml.factor.siqs.sieve;

import java.math.BigInteger;
import java.util.List;

import de.tilman_neumann.jml.factor.siqs.data.SolutionArrays;

/**
 * Interface for sieve algorithms.
 * @author Tilman Neumann
 */
public interface Sieve {
	
	/**
	 * @return the name of this sieve algorithm
	 */
	String getName();
	
	/**
	 * Initialize for a new N. In PSIQS, this method is called for each thread, so things that can be computed before should be computed before.
	 * @param sieveParams basic sieve parameters for a new N
	 * @param primesArray the prime base before filtering the elements of the a-parameter
	 * @param mergedBaseSize size of prime/power base after merging, before filtering
	 */
	void initializeForN(SieveParams sieveParams, int[] primesArray, int mergedBaseSize);
	
	/**
	 * Set (filtered) prime base and smallest x1, x2 solutions for a new a-parameter.
	 * @param d 2 if (kN == 1) (mod 8), 1 otherwise
	 * @param daParam d*a, where 'a' is the a-parameter and d=2 if kN==1 (mod 8), d=1 else
	 * @param primeSolutions
	 * @param filteredBaseSize number of primes and powers use for sieving
	 */
	void initializeForAParameter(int d, BigInteger daParam, SolutionArrays primeSolutions, int filteredBaseSize);

	/**
	 * Pass the b-parameter to the sieve.
	 * @param b
	 */
	public void setBParameter(BigInteger b);

	/**
	 * Sieve for a new set of x1, x2 solutions.
	 * @return list of sieve locations x where Q(x) is smooth enough to be passed to trial division
	 */
	List<SmoothCandidate> sieve();

	/**
	 * @return description of the durations of the individual sub-phases
	 */
	SieveReport getReport();
	
	/**
	 * Release memory after a factorization.
	 */
	void cleanUp();
}
