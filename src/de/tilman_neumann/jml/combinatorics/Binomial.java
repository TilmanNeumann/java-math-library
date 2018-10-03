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
package de.tilman_neumann.jml.combinatorics;

import java.math.BigInteger;

import de.tilman_neumann.jml.base.BigIntConstants;

/**
 * Implementation of the binomial coefficient.
 * @author Tilman Neumann
 */
public class Binomial {
    /**
     * Compute binomial coefficient n choose k using three factorials.
	 * 
	 * @param n total number of objects
	 * @param k objects of first kind
	 * @return binomial coefficient n choose k
	 * @throws IllegalArgumentException
     */
    private static BigInteger simple(int n, int k) {
	    if (k>0 && n>k) {
	        // Compute denominator:
	        return Factorial.withMemory(n).divide(Factorial.withMemory(k).multiply(Factorial.withMemory((n-k))));
	    } else if (k==0 || n==k)
	        return BigIntConstants.ONE;
	    else {
	    	throw new IllegalArgumentException("Unadmissible parameters for binomial coefficient: " + n + ", " + k + " !! Exit...");
	    }
	}

	/**
	 * Simple implementation of the binomial coefficient n choose k
	 * using the variations of n choose k.
	 * 
	 * @param n total number of objects
	 * @param k objects of first kind
	 * @return binomial coefficient n choose k
	 * @throws IllegalArgumentException
	 */
    // TODO: has a problem with arguments (0, 2) ?
    private static BigInteger withVariations(int n, int k) throws IllegalArgumentException {
		return Variations.bivariate(n, k).divide(Factorial.withMemory(k));
	}
	
    /**
     * Returns a BigInteger whose value is the binomial coefficient (n choose k).
     * 
	 * Adapted from http://www.jonelo.de by Johann Nepomuk Loefflmann (jonelo@jonelo.de),
	 * published under GNU General Public License.
	 * 
     * @param n parameter in n choose k
     * @param k parameter in n choose k
     * @throws ArithmeticException if n is negative
     * @return a BigInteger whose value is the binomial coefficient (n choose k)
     */
    // iterative solution, recursive is overkill!
    // applies "fruehes Kuerzen"
    public static final BigInteger nk(int n, int k) throws ArithmeticException {
        if ((n < 0) || (k < 0)) throw new ArithmeticException("arguments need to be non-negative, but n=" + n + " and k=" + k);
        if (k > n) return BigInteger.ZERO;
        if ((n==k) || (k==0)) return BigInteger.ONE;

        BigInteger result = BigInteger.ONE;
        // initialize with 1. factor in numerator
        BigInteger zaehler = BigInteger.valueOf(n-k+1);
        BigInteger nenner = BigInteger.ONE;
        long i=0;

        while (k > i) {
            result = (result.multiply(zaehler)).divide(nenner);
            zaehler=zaehler.add(BigInteger.ONE);
            nenner=nenner.add(BigInteger.ONE);
            i++;
        }
        return result;
    }
}
