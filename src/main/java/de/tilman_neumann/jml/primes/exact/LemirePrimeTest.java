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
package de.tilman_neumann.jml.primes.exact;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.BinarySearch;

/**
 * A deterministic prime test for N < 32 bit using Lemire division.
 * 
 * Implements the singleton pattern so that the resources will not be allocated twice no
 * matter how often the class is used.
 * 
 * @author Tilman Neumann
 */
public class LemirePrimeTest {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(LemirePrimeTest.class);
	
	private static final int MAX_N_BITS = 36;
	private static final int MAX_PRIME_FACTOR = 1 << ((MAX_N_BITS+1)/2);

	private static final BinarySearch binarySeach = new BinarySearch();
	
	private static int MAX_INDEX; // for N<32 bit this would be 4792

    private int[] primes;
    private long[] modularInverse;
    private long[] limits;
	
	// lazy-initialized singleton
	private static LemirePrimeTest the_instance = null;

	/**
	 * @return the only TDivPrimeTest instance (singleton)
	 */
	public static synchronized final LemirePrimeTest getInstance() {
		if (the_instance == null) {
			the_instance = new LemirePrimeTest();
		}
		return the_instance;
	}

	private LemirePrimeTest() {
        primes = SmallPrimes.generatePrimes(MAX_PRIME_FACTOR);
        MAX_INDEX = primes.length - 1;
        modularInverse = new long[primes.length];
        limits = new long[primes.length];
        for (int i = 0; i < primes.length; i++) {
            long p = primes[i];
            // compute modular inverses of p (mod 2^64) using Newton's method
            long inverse = modularInverse(p);
            modularInverse[i] = inverse;
            // limit = (2^64 - 1) / p (unsigned)
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
	
	// not public because isPrimeUnrolled() is faster
	boolean isPrime_v1(int N) {
		if (N==1) return false;
		if ((N&1)==0) return N==2;
		
		int pmax = (int) Math.sqrt(N);
		
        for (int i = 1; primes[i]<=pmax; i++) {
            // for hard numbers like big semiprimes finding a factor (early) is unlikely and JIT predicts that
            // the return branch is unlikely -> always the same data processing; preloading the arrays
            if (factorFound (N, i)) return false;
        }
        
        return true;
    }
	
	// not public because isPrimeUnrolled() is faster
	boolean isPrime_v2(int N) {
		if (N==1) return false;
		if ((N&1)==0) return N==2;
		
		int pmax = (int) Math.sqrt(N);
		int imax = binarySeach.getInsertPosition(primes, MAX_INDEX, pmax);
		
        for (int i = 1; i<=imax; i++) {
            // for hard numbers like big semiprimes finding a factor (early) is unlikely and JIT predicts that
            // the return branch is unlikely -> always the same data processing; preloading the arrays
            if (factorFound (N, i)) return false;
        }
        
        return true;
    }
	
	public boolean isPrime/*Unrolled*/(int N) {
		if (N==1) return false;
		if ((N&1)==0) return N==2;
		
		int pmax = (int) Math.sqrt(N);
		int imax = binarySeach.getInsertPosition(primes, MAX_INDEX, pmax);
		
		int i=1;
		int unrolledLimit = imax-8;
		for ( ; i<unrolledLimit; i++) {
            if (factorFound (N, i)) return false;
            if (factorFound (N, ++i)) return false;
            if (factorFound (N, ++i)) return false;
            if (factorFound (N, ++i)) return false;
            if (factorFound (N, ++i)) return false;
            if (factorFound (N, ++i)) return false;
            if (factorFound (N, ++i)) return false;
            if (factorFound (N, ++i)) return false;
		}
		for ( ; i<imax; i++) {
            if (factorFound (N, i)) return false;
		}
        
        return true;
    }

    private boolean factorFound(long N, int i) {
        // 1. get pre-computed inverse and limit
        long inv = modularInverse[i];
        long limit = limits[i];
        
        // 2. multiply number * inverse (overflow is intended!)
        long product = N * inv;

        // 3. if the (unsigned) product is less than or equal to the limit, then primes[i] divides N without rest.
         return  Long.compareUnsigned(product, limit) <= 0;
    }
}
