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
 * For N < 32 bit, this class seems to slightly faster than LemirePrimeTest.
 * 
 * @author Thilo Harich, Tilman Neumann
 */
public class LemireIntPrimeTest {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(LemireIntPrimeTest.class);
	
	private static final int MAX_PRIME_FACTOR = 1<<16; // sufficient for numbers to factor with 32 bits

	private static final BinarySearch binarySeach = new BinarySearch();
	
	private static int MAX_INDEX; // for N<32 bit this would be 4792

    private static int[] primes;
    private static int[] modularInverses;
    private static int[] limits;
	
	// lazy-initialized singleton
	private static LemireIntPrimeTest the_instance = null;

	/**
	 * @return the only TDivPrimeTest instance (singleton)
	 */
	public static synchronized final LemireIntPrimeTest getInstance() {
		if (the_instance == null) {
			the_instance = new LemireIntPrimeTest();
		}
		return the_instance;
	}

	private LemireIntPrimeTest() {
        primes = SmallPrimes.generatePrimes(MAX_PRIME_FACTOR);
        MAX_INDEX = primes.length - 1;
        modularInverses = new int[primes.length];
        limits = new int[primes.length];
        for (int i = 0; i < primes.length; i++) {
            int prime = primes[i];
            // compute modular inverses of p (mod 2^32) using Newton's method
            modularInverses[i] = modularInverse(prime);
            // limit = (2^32 - 1) / prime (unsigned)
            limits[i] = Integer.divideUnsigned(-1, prime);
        }
    }

    private static int modularInverse(int n) {
        int inverse = n; // initial estimate
        for (int i = 0; i < 4; i++) { // 4 iterations are sufficient for 32 bit numbers
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

    private boolean factorFound(int N, int i) {
        // 1. get pre-computed inverse and limit
        int inv = modularInverses[i];
        int limit = limits[i];
        
        // 2. multiply number * inverse (overflow is intended!)
        int product = N * inv;

        // 3. if the (unsigned) product is less than or equal to the limit, then primes[i] divides N without rest.
        return Integer.compareUnsigned(product, limit) <= 0;
    }
}
