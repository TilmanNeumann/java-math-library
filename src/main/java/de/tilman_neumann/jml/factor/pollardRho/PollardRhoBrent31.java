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
 * Brents's improvement of Pollard's Rho algorithm, following [Richard P. Brent: An improved Monte Carlo Factorization Algorithm, 1980].
 * 
 * 31 bit version.
 * 
 * @author Tilman Neumann
 */
public class PollardRhoBrent31 extends FactorAlgorithm {
	private static final Logger LOG = LogManager.getLogger(PollardRhoBrent31.class);
	private static final boolean DEBUG = false;
	private static final SecureRandom RNG = new SecureRandom();

	private int n;

	private Gcd31 gcd = new Gcd31();
	
	@Override
	public String getName() {
		return "PollardRhoBrent31";
	}
	
	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		if (N.bitLength() > 31) { // this check should be negligible in terms of performance
			throw new IllegalArgumentException("N = " + N + " has " + N.bitLength() + " bit, but PollardRho31 only supports arguments <= 31 bit");
		}
		int factorInt = findSingleFactor(N.intValue());
        return BigInteger.valueOf(factorInt);
	}
	
	public int findSingleFactor(int nOriginal) {
		this.n = nOriginal<0 ? -nOriginal : nOriginal; // RNG.nextInt(n) below would crash for negative arguments
		int G;
		int ys, x;
        do {
	        // start with random x0, c from [0, N-1]
        	int c = RNG.nextInt(n);
            int x0 = RNG.nextInt(n);
            int y = x0;

            // Brent: "The probability of the algorithm failing because q_i=0 increases, so it is best not to choose m too large"
        	final int m = 100;
        	int r = 1;
        	int q = 1;
        	do {
	    	    x = y;
	    	    for (int i=1; i<=r; i++) {
    	            y = addModN(squareModN(y), c);
	    	    }
	    	    int k = 0;
	    	    do {
	    	        ys = y;
	    	        final int iMax = Math.min(m, r-k);
	    	        for (int i=1; i<=iMax; i++) {
	    	            y = addModN(squareModN(y), c);
	    	            final long diff = x<y ? y-x : x-y;
	    	            q = (int) ((diff*q) % n);
	    	        }
	    	        G = gcd.gcd(q, n);
	    	        // if q==0 then G==N -> the loop will be left and restarted with new x0, c
	    	        k += m;
	    	        if (DEBUG) LOG.debug("r = " + r + ", k = " + k);
	    	    } while (k<r && G==1);
	    	    r <<= 1;
	    	    if (DEBUG) LOG.debug("r = " + r + ", G = " + G);
	    	} while (G==1);
	    	if (G==n) {
	    	    do {
    	            ys = addModN(squareModN(ys), c);
    	            int diff = x<ys ? ys-x : x-ys;
    	            G = gcd.gcd(diff, n);
	    	    } while (G==1);
	    	    if (DEBUG) LOG.debug("G = " + G);
	    	}
        } while (G==n);
        if (DEBUG) LOG.debug("Found factor of " + nOriginal + " = " + G);
        return G;
	}

	/**
	 * Addition modulo N, with <code>a, b < N</code>.
	 * @param a
	 * @param b
	 * @return (a+b) mod N
	 */
	private int addModN(int a, int b) {
		long sum = a + (long)b; // long is needed for the addition of 31 bit numbers
		return (int) (sum<n ? sum : sum-n);
	}

	/**
	 * x^2 modulo N.
	 * @param x
	 * @return
	 */
	private int squareModN(long x) {
		return (int) ((x * x) % n);
	}
}
