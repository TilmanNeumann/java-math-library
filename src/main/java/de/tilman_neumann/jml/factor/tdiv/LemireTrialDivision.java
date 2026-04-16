/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2026 Tilman Neumann - tilman.neumann@web.de
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

import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.jml.primes.exact.SmallPrimes;

/**
 * Lemire division for long arguments.
 * 
 * @author Thilo Harich
 */
public class LemireTrialDivision extends FactorAlgorithm {

	private static final int MAX_N_BITS = 36;
	private static final int MAX_PRIME_FACTOR = 1 << ((MAX_N_BITS+1)/2);
	
    private static int[] primes;
    private static long[] modularInverses;
    private static long[] limits;

    static {
        primes = SmallPrimes.generatePrimes(MAX_PRIME_FACTOR);
        modularInverses = new long[primes.length];
        limits = new long[primes.length];
        for (int i = 0; i < primes.length; i++) {
            long p = primes[i];
            // compute modular inverse of p (mod 2^64) using Newton's method
            modularInverses[i] = modularInverse(p);
            // limit = (2^64 - 1) / prime (unsigned)
            limits[i] = Long.divideUnsigned(-1L, p);
        }
    }

    private static long modularInverse(long n) {
        long inverse = n; // initial estimate
        for (int i = 0; i < 5; i++) { // 5 iterations are sufficient for 64 bit numbers
            inverse *= 2 - n * inverse;
        }
        return inverse;
    }

	@Override
	public String getName() {
		return "LemireTrialDivision";
	}

	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		if (N.bitLength() > MAX_N_BITS) throw new IllegalArgumentException("LemireTrialDivision.findSingleFactor() does not work for N>" + MAX_N_BITS + " bit, but N=" + N + " has " + N.bitLength() + " bits.");
		return BigInteger.valueOf(findSingleFactor(N.longValue()));
	}

    public int findSingleFactor(long N) {
        // Lemire can not handle even numbers
        if ((N & 1) == 0) return 2;

        for (int i = 1; i < primes.length; i++) {
            // for hard numbers like big semiprimes finding a factor (early) is unlikely and JIT predicts that
            // the return branch is unlikely -> always the same data processing; preloading the arrays
            if (factorFound (N, i)) return primes[i];
        }
        
        return -1;
    }

    private boolean factorFound(long N, int i) {
        // 1. get pre-computed inverse and limit
        long inv = modularInverses[i];
        long limit = limits[i];

        // 2. multiply number * inverse (overflow is intended!)
        long product = N * inv;

        // 3. if the (unsigned) product is less than or equal to the limit, then primes[i] divides N without rest.
        return Long.compareUnsigned(product, limit) <= 0;
    }
}
