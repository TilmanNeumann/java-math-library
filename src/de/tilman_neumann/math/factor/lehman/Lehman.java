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
package de.tilman_neumann.math.factor.lehman;

import java.math.BigInteger;

import de.tilman_neumann.math.base.bigint.Gcd63;
import de.tilman_neumann.math.base.bigint.primes.exact.SieveFacade;
import de.tilman_neumann.math.factor.FactorAlgorithmBase;

/**
 * Naive implementation of Lehmans factor algorithm following https://programmingpraxis.com/2017/08/22/lehmans-factoring-algorithm/
 * Works for N <= 45 bit.
 * 
 * @author Tilman Neumann
 */
public class Lehman extends FactorAlgorithmBase {
	private static final int NUM_PRIMES = 4793;
	
	private int[] primes = SieveFacade.get().ensurePrimeCount(NUM_PRIMES).getPrimes().array;

	private final Gcd63 gcdEngine = new Gcd63();
	
	@Override
	public String getName() {
		return "Lehman";
	}

	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		return BigInteger.valueOf(findSingleFactor(N.longValue()));
	}
	
	public long findSingleFactor(long N) {
		// 1. Check via trial division whether N has a nontrivial divisor d <= cbrt(N), and if so, return d.
		int cbrt = (int) Math.ceil(Math.cbrt(N));
		int i=0, p;
		while ((p = primes[i++]) <= cbrt) {
			if (N%p==0) return p;
		}
		
		// 2. Main loop
		double sixthRoot = Math.pow(N, 1/6.0); // double precision is required for stability
		for (int k=1; k <= cbrt; k++) {
			long fourKN = k*N<<2;
			double fourSqrtK = Math.sqrt(k<<4);
			int sqrt4kN = (int) Math.ceil(Math.sqrt(fourKN)); // ceil() is required for stability
			int limit = (int) (sqrt4kN + sixthRoot / fourSqrtK);
			for (int a = sqrt4kN; a <= limit; a++) {
				long test = a*(long)a - fourKN;
				long b = (long) Math.sqrt(test);
				if (b*b == test) {
					return gcdEngine.gcd(a+b, N);
				}
			}
	    }
		
		// Nothing found. Either N is prime or the implementation is buggy. For N > 45 bit it won't work.
		return 0;
	}
}
