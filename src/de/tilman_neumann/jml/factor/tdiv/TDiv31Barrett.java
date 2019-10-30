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

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.jml.primes.exact.AutoExpandingPrimesArray;
import de.tilman_neumann.util.SortedMultiset;
import de.tilman_neumann.util.SortedMultiset_BottomUp;

/**
 * Trial division using Barrett reduction,
 * see https://en.wikipedia.org/wiki/Barrett_reduction. 
 * 
 * Significantly faster than TDiv31Inverse.
 * 
 * @authors Tilman Neumann + Thilo Harich
 */
public class TDiv31Barrett extends FactorAlgorithm {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(TDiv31Barrett.class);

	private AutoExpandingPrimesArray SMALL_PRIMES = AutoExpandingPrimesArray.getInstance();	// "static" would be slightly slower

	private int[] primes;
	private long[] pinv;
	
	public TDiv31Barrett() {
		primes = new int[NUM_PRIMES_FOR_31_BIT_TDIV];
		pinv = new long[NUM_PRIMES_FOR_31_BIT_TDIV];
		for (int i=0; i<NUM_PRIMES_FOR_31_BIT_TDIV; i++) {
			int p = SMALL_PRIMES.getPrime(i);
			primes[i] = p;
			pinv[i] = (1L<<32)/p;
		}
	}
	
	@Override
	public String getName() {
		return "TDiv31Barrett";
	}

	@Override
	public SortedMultiset<BigInteger> factor(BigInteger Nbig) {
		SortedMultiset<BigInteger> primeFactors = new SortedMultiset_BottomUp<>();
		int N = Nbig.intValue();
		
		int q;
		for (int i=0; ; i++) {
			final long r = pinv[i];
			final int p = primes[i];
			int exp = 0;
			while ((q = (1 + (int) ((N*r)>>32))) * p == N) {
				exp++;
				N = q;
			}
			if (exp>0) {
				primeFactors.add(BigInteger.valueOf(p), exp);
			}
			if (p*(long)p > N) {
				break;
			}
		}
		
		if (N>1) {
			// either N is prime, or we could not find all factors with p<=pLimit -> add the rest to the result
			primeFactors.add(BigInteger.valueOf(N));
		}
		return primeFactors;
	}

	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		if (N.bitLength() > 31) throw new IllegalArgumentException("TDiv31Barrett.findSingleFactor() does not work for N>31 bit, but N=" + N);
		return BigInteger.valueOf(findSingleFactor(N.intValue()));
	}
	
	public int findSingleFactor(int N) {
		if (N<0) N = -N; // sign does not matter
		if (N<4) return 1; // prime
		if ((N&1)==0) return 2; // N even

		// if N is odd and composite then the loop runs maximally up to prime = floor(sqrt(N))
		// unroll the loop
		int i=1;
		int unrolledLimit = NUM_PRIMES_FOR_31_BIT_TDIV-8;
		for ( ; i<unrolledLimit; i++) {
			if ((1 + (int) ((N*pinv[i])>>32)) * primes[i] == N) return primes[i];
			if ((1 + (int) ((N*pinv[++i])>>32)) * primes[i] == N) return primes[i];
			if ((1 + (int) ((N*pinv[++i])>>32)) * primes[i] == N) return primes[i];
			if ((1 + (int) ((N*pinv[++i])>>32)) * primes[i] == N) return primes[i];
			if ((1 + (int) ((N*pinv[++i])>>32)) * primes[i] == N) return primes[i];
			if ((1 + (int) ((N*pinv[++i])>>32)) * primes[i] == N) return primes[i];
			if ((1 + (int) ((N*pinv[++i])>>32)) * primes[i] == N) return primes[i];
			if ((1 + (int) ((N*pinv[++i])>>32)) * primes[i] == N) return primes[i];
		}
		for ( ; i<NUM_PRIMES_FOR_31_BIT_TDIV; i++) {
			if ((1 + (int) ((N*pinv[i])>>32)) * primes[i] == N) return primes[i];
		}
		// otherwise N is prime
		return 1;
	}
}
