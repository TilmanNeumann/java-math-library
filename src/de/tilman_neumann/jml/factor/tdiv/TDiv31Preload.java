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
package de.tilman_neumann.jml.factor.tdiv;

import java.math.BigInteger;

import de.tilman_neumann.jml.factor.FactorAlgorithmBase;
import de.tilman_neumann.jml.primes.exact.AutoExpandingPrimesArray;

/**
 * Trial division factor algorithm preloading all primes <= sqrt(Integer.MAX_VALUE).
 * 
 * sqrt(Integer.MAX_VALUE) = sqrt(2^31 - 1) = sqrt(2147483647) = 46340.95
 * -> we need to preload all primes < 46340.
 * -> there are 4793 such primes...
 * 
 * @author Tilman Neumann
 */
public class TDiv31Preload extends FactorAlgorithmBase {

	// the number of primes needed to factor any int <= 2^31 - 1 using trial division
	private static final int NUM_PRIMES = 4793;
	
	private static AutoExpandingPrimesArray SMALL_PRIMES = AutoExpandingPrimesArray.get().ensurePrimeCount(NUM_PRIMES);

	@Override
	public String getName() {
		return "TDiv31Preload";
	}

	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		return BigInteger.valueOf(findSingleFactor(N.intValue()));
	}
	
	public long findSingleFactor(long N) {
		return (long) findSingleFactor( (int) N);
	}
	
	public int findSingleFactor(int N) {
		// if N is odd and composite then the loop runs maximally up to test = floor(sqrt(N))
		for (int i=0; i<NUM_PRIMES; i++) {
			int p = SMALL_PRIMES.getPrime(i);
			if (N%p==0) return p;
		}
		// otherwise N is prime!
		throw new IllegalArgumentException("N = " + N + " is prime!");
	}
	
	/**
	 * Test if N has a factor <= maxTestNumber.
	 * @param N
	 * @param maxTestNumber
	 * @return small factor, or null if N has no factor <= maxTestNumber
	 */
	public BigInteger findSmallFactor(BigInteger N, BigInteger maxTestNumber) {
		int n = N.intValue();
		int maxTestNumberL = maxTestNumber.intValue();
		// test until maxTestNumber or until a factor is found
		for (int i=0; i<NUM_PRIMES; i++) {
			int p = SMALL_PRIMES.getPrime(i);
			if (p>maxTestNumberL) return null;
			if (n%p==0) return BigInteger.valueOf(p);
		}
		return null;
	}
}
