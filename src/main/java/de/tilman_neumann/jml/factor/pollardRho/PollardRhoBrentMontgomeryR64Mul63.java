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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.base.Rng;
import de.tilman_neumann.jml.base.Uint128;
import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.jml.gcd.Gcd63;
import de.tilman_neumann.util.Ensure;

/**
 * Brents's improvement of Pollard's Rho algorithm using Montgomery multiplication.
 * 
 * This version combines the Montgomery reducer R=2^64 with mul63(). It is significantly faster than
 * PollardRhoBrentMontgomery64 for N < 58 bits, because until that size the performance advantage of 
 * mul63() vs. mul64() predominates the performance loss caused by errors in Montgomery multiplication.
 * 
 * Another small performance gain stems from the choice of polynomials:
 * x_(n+1) = x_n*(x_n + 1) is slightly faster than x_(n+1) = (x_n)^2 - c
 * because it does not need another reduction (mod N) after subtracting c.
 * 
 * @see [Richard P. Brent: An improved Monte Carlo Factorization Algorithm, 1980]
 * @see [http://projecteuler.chat/viewtopic.php?t=3776]
 * @see [http://coliru.stacked-crooked.com/a/f57f11426d06acd8]
 * 
 * @author Tilman Neumann
 */
public class PollardRhoBrentMontgomeryR64Mul63 extends FactorAlgorithm {
	private static final Logger LOG = LogManager.getLogger(PollardRhoBrentMontgomeryR64Mul63.class);
	private static final boolean DEBUG = false;

	private static final Rng RNG = new Rng();

	// The reducer R is 2^64, but the only constant still required is the half of it.
	private static final long R_HALF = 1L << 63;

	private long n;

	private long minusNInvModR;	// (-1/N) mod R, required for Montgomery multiplication
	
	private Gcd63 gcd = new Gcd63();

	@Override
	public String getName() {
		return "PollardRhoBrentMontgomeryR64Mul63";
	}
	
	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		// there is a complication with this check: The algorithm works for some 63-bit numbers and there are tests for it, but there may be 63-bit numbers where it fails
		if (N.bitLength() > 63) { // this check should be negligible in terms of performance
			throw new IllegalArgumentException("N = " + N + " has " + N.bitLength() + " bit, but " + getName() + " only supports arguments <= 63 bit");
		}
		long factorLong = findSingleFactor(N.longValue());
        return BigInteger.valueOf(factorLong);
	}
	
	public long findSingleFactor(long nOriginal) {
		this.n = nOriginal<0 ? -nOriginal : nOriginal; // RNG.nextLong(n) below would crash for negative arguments
		// n==9 would require to check if the gcd is 1 < gcd < n before returning it as a factor
		if (n==9) return 3;
		
        long G, x, ys;
        
		setUpMontgomeryMult();

		// number of iterations before gcd tests.
        // Brent: "The probability of the algorithm failing because q_i=0 increases, so it is best not to choose m too large"
		final int Nbits = 64 - Long.numberOfLeadingZeros(n);
    	final int m = 2*Nbits;

        do {
	        // start with random y from [0, n)
            long y = RNG.nextLong(n);

        	int r = 1;
        	long q = 1;
        	do {
	    	    x = y;
	    	    for (int i=r; i>0; i--) {
	    	        y = montMul63(y, y+1, n, minusNInvModR);
	    	    }
	    	    int k = 0;
	    	    do {
	    	        ys = y;
	    	        final int iMax = Math.min(m, r-k);
	    	        for (int i=iMax; i>0; i--) {
	    	            y = montMul63(y, y+1, n, minusNInvModR);
	    	            final long diff = x<y ? y-x : x-y;
	    	            q = montMul63(diff, q, n, minusNInvModR);
	    	        }
	    	        G = gcd.gcd(q, n);
	    	        // if q==0 then G==n -> the loop will be left and restarted with new y
	    	        k += m;
	    	        if (DEBUG) LOG.debug("r = " + r + ", k = " + k);
	    	    } while (k<r && G==1);
	    	    r <<= 1;
	    	    if (DEBUG) LOG.debug("r = " + r + ", G = " + G);
	    	} while (G==1);
	    	if (G==n) {
	    	    do {
	    	        ys = montMul63(ys, ys+1, n, minusNInvModR);
    	            final long diff = x<ys ? ys-x : x-ys;
	    	        G = gcd.gcd(diff, n);
	    	    } while (G==1);
	    	    if (DEBUG) LOG.debug("G = " + G);
	    	}
        } while (G==n);
        if (DEBUG) LOG.debug("Found factor " + G + " of N=" + nOriginal);
        return G;
	}
	
	/**
	 * Finds (1/R) mod N and (-1/N) mod R for odd N and R=2^64.
	 * 
	 * As before, EEA63 would not work for R=2^64, but with a minor modification
	 * the algorithm from http://coliru.stacked-crooked.com/a/f57f11426d06acd8
	 * still works for R=2^64.
	 */
	private void setUpMontgomeryMult() {
		// initialization
	    long a = R_HALF;
	    long u = 1;
	    long v = 0;
	    
	    while (a != 0) { // modification
	        a >>>= 1;
	        if ((u & 1) == 0) {
	            u >>>= 1;
	    	    v >>>= 1;
	        } else {
	            u = ((u ^ n) >>> 1) + (u & n);
	            v = (v >>> 1) + R_HALF;
	        }
	    }

	    // u = (1/R) mod N and v = (-1/N) mod R. We only need the latter.
	    minusNInvModR = v;
	}

	/**
	 * Montgomery multiplication of a*b mod n with regard to R=2^63. ("mulredc63x" in Yafu)
	 * @param a
	 * @param b
	 * @param N
	 * @param Nhat complement of N mod 2^63
	 * @return Montgomery multiplication of a*b mod n
	 */
	public static long montMul63(long a, long b, long N, long Nhat) {
		// Step 1: Compute a*b
		Uint128 ab = Uint128.mul63(a, b);
		// Step 2: Compute t = ab * (-1/N) mod R
		// Since R=2^64, "x mod R" just means to get the low part of x.
		// That would give t = Uint128.mul64(ab.getLow(), minusNInvModR).getLow();
		// but even better, the long product just gives the low part -> we can get rid of one expensive mul64().
		long t = ab.getLow() * Nhat;
		// Step 3: Compute r = (a*b + t*N) / R
		// Since R=2^64, "x / R" just means to get the high part of x.
		long r = ab.add_getHigh(Uint128.mul63(t, N));
		// If the correct result is c, then now r==c or r==c+N.
		// This is fine for this factoring algorithm, because r will 
		// * either be subjected to another Montgomery multiplication mod N,
		// * or to a gcd(r, N), where it doesn't matter if we test gcd(c, N) or gcd(c+N, N).
		
		if (DEBUG) {
			LOG.debug(a + " * " + b + " = " + r);
			// 0 <= a < N
			Ensure.ensureSmallerEquals(0, a);
			Ensure.ensureSmaller(a, N);
			// 0 <= b < N
			Ensure.ensureSmallerEquals(0, b);
			Ensure.ensureSmaller(b, N);
			
			// In a general Montgomery multiplication we would still have to check
			r = r<N ? r : r-N;
			// to satisfy 0 <= r < N
			Ensure.ensureSmallerEquals(0, r);
			Ensure.ensureSmaller(r, N);
		}
		
		return r;
	}
}
