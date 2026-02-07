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
 * Lemire division for int arguments.
 * 
 * @author Thilo Harich
 */
public class LemireIntTrialDivision extends FactorAlgorithm {

	private static final int MAX_PRIME_FACTOR = 1<<16; // sufficient for numbers to factor with 32 bits
	
    private static int[] primes;
    private static int[] modularInverse;
    private static int[] limits;

    static {
        primes = SmallPrimes.generatePrimes(MAX_PRIME_FACTOR);
        modularInverse = new int[primes.length];
        limits = new int[primes.length];
        for (int i = 0; i < primes.length; i++) {
            int prime = primes[i];
            // Modulare Inverse für 32-bit (Newton-Verfahren)
            int inv = modularInverseInt(prime);
            modularInverse[i] = inv;
            // Limit = (2^32 - 1) / prime (Unsigned)
            // In Java: Integer.divideUnsigned(-1, prime)
            limits[i] = Integer.divideUnsigned(-1, prime);
        }
    }

    private static int modularInverseInt(int n) {
        int inverse = n;
        for (int i = 0; i < 4; i++) { // 4 Iterationen reichen für 32-bit
            inverse *= 2 - n * inverse;
        }
        return inverse;
    }

	@Override
	public String getName() {
		return "LemireIntTrialDivision";
	}

	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		if (N.bitLength() > 32) throw new IllegalArgumentException("LemireIntTrialDivision.findSingleFactor() does not work for N>32 bit, but N=" + N + " has " + N.bitLength() + " bits.");
		return BigInteger.valueOf(findSingleFactor(N.longValue()));
	}

    public int findSingleFactor(long numberToFactorize) {
        // Lemire can not handle even numbers
        if ((numberToFactorize & 1) == 0) return 2;

        for (int i = 1; i < primes.length; i++) {
            // for hard numbers like big semiprimes finding a factor (early) is unlikely and JIT predicts that
            // the return branch is unlikely -> always the same data processing; preloading the arrays
            if (factorFound (numberToFactorize, i)) return primes[i];
        }
        
        return -1;
    }

    private boolean factorFound(long numberToFactorize, int i) {
        int nInt = (int) numberToFactorize;
        int product = nInt * modularInverse[i];
        return Integer.compareUnsigned (product, limits[i]) <= 0;
    }
}
