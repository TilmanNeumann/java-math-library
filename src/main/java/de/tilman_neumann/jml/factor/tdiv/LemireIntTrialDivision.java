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
