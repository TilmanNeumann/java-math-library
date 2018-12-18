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
package de.tilman_neumann.jml.factor.lehman;

import java.math.BigInteger;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.FactorAlgorithmBase;
import de.tilman_neumann.jml.gcd.Gcd63;

/**
 * Faster implementation of Lehmans factor algorithm following https://programmingpraxis.com/2017/08/22/lehmans-factoring-algorithm/.
 * Most improvements by Thilo Harich (https://github.com/ThiloHarich/factoring.git).
 * Works for N <= 45 bit.
 *
 * This version does trial division after the main loop.
 *
 * @author Tilman Neumann
 */
public class Lehman_Advanced extends FactorAlgorithmBase {
	private static final Logger LOG = Logger.getLogger(Lehman_Advanced.class);

	/** This is a constant that is below 1 for rounding up double values to long. */
	private static final double ROUND_UP_DOUBLE = 0.9999999665;

	private final Gcd63 gcdEngine = new Gcd63();

	private static double[] sqrt, sqrtInv;

	static {
		// Precompute sqrts for all possible k. 2^22 entries are enough for N~2^66.
		final int kMax = (int) (1<<22);
		//LOG.debug("kMax = " + kMax);

		sqrt = new double[kMax + 1];
		sqrtInv = new double[kMax + 1];
		for (int i = 1; i < sqrt.length; i++) {
			final double sqrtI = Math.sqrt(i);
			sqrt[i] = sqrtI;
			sqrtInv[i] = 1.0/sqrtI;
		}
		LOG.info("Lehman: Built sqrt tables with " + sqrt.length + " entries");
	}

	@Override
	public String getName() {
		return "Lehman_Advanced";
	}

	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		return BigInteger.valueOf(findSingleFactor(N.longValue()));
	}

	public long findSingleFactor(long N) {
		final int limit = (int) Math.ceil(Math.cbrt(N));

		// 1. Main loop for small k, where we can have more than 2 a-values per k.
		// For kLimit / 64 the range for a is at most 2, this is what we can ensure.
		int maxKWithARangeGreater2 = Math.max(((limit >> 6) - 1), 0) | 1; // make it odd
		maxKWithARangeGreater2 = (maxKWithARangeGreater2 / 6) * 6 + 5;
		final long fourN = N<<2;
		final double sqrt4N = Math.sqrt(fourN);
		final double sixthRootTerm = 0.25 * Math.pow(N, 1/6.0); // double precision is required for stability
		int k=1;
		for (; k <= maxKWithARangeGreater2; k++) {
			final double sqrt4kN = sqrt4N * sqrt[k];
			// only use long values
			final long aStart = (long) (sqrt4kN + ROUND_UP_DOUBLE); // much faster than ceil()
			long aLimit = (long) (sqrt4kN + sixthRootTerm * sqrtInv[k]);
			long aStep;
			if ((k & 1) == 0) {
				// k even -> make sure aLimit is odd
				aLimit |= 1l;
				aStep = 2;
			} else {
				final long kn = k*N;
				// this extra case gives ~ 5 %
				if ((kn & 3) == 3) {
					aStep = 8;
					aLimit += ((7 - kn - aLimit) & 7);
				} else {
					aStep = 4;
					aLimit += ((k + N - aLimit) & 3);
				}
			}

			// processing the a-loop top-down is faster than bottom-up
			final long fourKN = k * fourN;
			for (long a=aLimit; a >= aStart; a-=aStep) {
				final long test = a*a - fourKN;
				final long b = (long) Math.sqrt(test);
				if (b*b == test) {
					return gcdEngine.gcd(a+b, N);
				}
			}
		}
		
		// 2. continue main loop for larger even k, where we can have at most 2 a-values per k
		for (int i = 0; i < 3; i++) {
			for (int k1 = k + i*2; k1 <= limit; k1 += 6) {
				final long a = (long) (sqrt4N * sqrt[k1] + ROUND_UP_DOUBLE) | 1;
				final long test = a*a - k1 * fourN;
				final long b = (long) Math.sqrt(test);
				if (b*b == test) {
					return gcdEngine.gcd(a+b, N);
				}
			}
		}

		// 3. continue main loop for larger odd k
		int k2 = k + 1;
		for ( ; k2 <= limit; k2 += 2) {
			long a = (long) (sqrt4N * sqrt[k2] + ROUND_UP_DOUBLE);
			a += (k2 + N - a) & 3;
			final long test = a*a - k2 * fourN;
			final long b = (long) Math.sqrt(test);
			if (b*b == test) {
				return gcdEngine.gcd(a+b, N);
			}
		}

		// 4. Check via trial division whether N has a nontrivial divisor d <= cbrt(N), and if so, return d.
		int i=0, p;
		while ((p = SMALL_PRIMES.getPrime(i++)) <= limit) {
			if (N%p==0) return p;
		}

		// Nothing found. Either N is prime or the algorithm didn't work because N > 45 bit.
		return 0;
	}
}
