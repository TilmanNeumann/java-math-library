/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2024 Tilman Neumann - tilman.neumann@web.de
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
package de.tilman_neumann.jml.factor.pollardRho;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.jml.gcd.Gcd31;

/**
 * 31-bit implementation of Pollard's Rho method.
 * 
 * @author Tilman Neumann
 */
public class PollardRho31 extends FactorAlgorithm {
	private static final Logger LOG = LogManager.getLogger(PollardRho31.class);
	private static final boolean DEBUG = false;
	private static final SecureRandom RNG = new SecureRandom();

	private Gcd31 gcdEngine = new Gcd31();
	
	/** absolute value of the number to factor */
	private int n;
	
	@Override
	public String getName() {
		return "PollardRho31";
	}
	
	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		if (N.bitLength() > 31) { // this check should be negligible in terms of performance
			throw new IllegalArgumentException("N = " + N + " has " + N.bitLength() + " bit, but " + getName() + " only supports arguments <= 31 bit");
		}
		int factorInt = findSingleFactor(N.intValue());
        return BigInteger.valueOf(factorInt);
	}
	
	public int findSingleFactor(int nOriginal) {
		this.n = nOriginal<0 ? -nOriginal : nOriginal; // RNG.nextInt(n) below would crash for negative arguments
		
        int gcd;
        long x = RNG.nextInt(n); // uniform random int from [0, n)
        long xx = x;
        do {
        	int c = RNG.nextInt(n); // uniform random int from [0, n)
	        do {
	            x  = addModN(squareModN(x), c);
	            xx = addModN(squareModN(xx), c);
	            xx = addModN(squareModN(xx), c);
	            gcd = gcdEngine.gcd((int)(x-xx), n);
	        } while(gcd==1);
        } while (gcd==n); // leave loop if factor found; otherwise continue with a new random c
        if (DEBUG) LOG.debug("Found factor of " + nOriginal + " = " + gcd);
        return gcd;
	}

	/**
	 * Addition modulo N, with <code>a, b < N</code>.
	 * @param a
	 * @param b
	 * @return (a+b) mod N
	 */
	private long addModN(long a, int b) {
		long sum = a + b;
		return sum<n ? sum : sum-n;
	}

	/**
	 * x^2 modulo N.
	 * @param x
	 * @return
	 */
	private long squareModN(long x) {
		return (x * x) % n;
	}
}
