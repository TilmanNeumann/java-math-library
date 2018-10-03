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
package de.tilman_neumann.math.base.bigint.combinatorics;

import java.math.BigInteger;

import de.tilman_neumann.math.base.bigint.BigIntConstants;

/**
 * Implementations for the number of variations.
 * @author Tilman Neumann
 */
public class Variations {

	/**
	 * Computes the number of variations of n1 and (n-n1) indistinguishable objects.
	 * @param n Total number of objects.
	 * @param n1 Number of indistinguishable objects of 1. kind.
	 * @return Variations of n1 and (n-n1) indistinguishable objects
	 * @throws IllegalArgumentException
	 */
	public static BigInteger bivariate(int n, int n1) throws IllegalArgumentException {
		if ((n < 0) || (n < n1)) {
			throw new IllegalArgumentException("Var(" + n + ", " + n1 + ")?? Both args need to be positive, and the selection must not be bigger than the base!");
		}

		return Factorial.withMemory(n).divide(Factorial.withMemory(n-n1));
	}
	
	public static BigInteger multivariate(int[] N, int[] n) {
		if (N.length != n.length) throw new IllegalArgumentException("both vectors need to be of the same dimension!");

		BigInteger ret = BigIntConstants.ONE;
		for (int i=0; i<N.length; i++)
		{	ret = ret.multiply(bivariate(N[i], n[i]));
		}

		return ret;
	}
}
