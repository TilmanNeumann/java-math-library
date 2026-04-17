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
package de.tilman_neumann.jml.primes.probable;

import static de.tilman_neumann.jml.base.BigIntConstants.*;

import java.math.BigInteger;

import de.tilman_neumann.jml.primes.exact.LemireIntPrimeTest;
import de.tilman_neumann.jml.primes.util.PrimeRestsMod30030;

/**
 * BPSW probable prime test. The implementation starts checking the moduli of N % 30030 and then follows
 * [http://en.wikipedia.org/wiki/Baillie-PSW_primality_test] and references therein.
 * 
 * BPSW may be considered a deterministic prime test for N < 2^64.
 * 
 * Class PrPTest extends this approach by a faster test for 64-bit numbers.
 * 
 * @author Tilman Neumann
 */
public class BPSWTest {
	
	MillerRabinTest millerRabinTest = new MillerRabinTest();
	LucasTest lucasTest = new LucasTest();
	LemireIntPrimeTest tdiv = LemireIntPrimeTest.getInstance();
	PrimeRestsMod30030 primeRestsMod30030 = PrimeRestsMod30030.getInstance();

	// TODO rename to isPrime() ?
	// TODO the implementation in PrPTest is already quite ok but could be optimized
    public boolean isProbablePrime(long N) {
    	return isProbablePrime(BigInteger.valueOf(N));
    }

    public boolean isProbablePrime(BigInteger N) {
        N = N.abs(); // sign is irrelevant
        if (!N.testBit(0)) return N.equals(I_2); // even N>2 is not prime
        return isProbablePrimeCore(N);
    }

    /**
     * Probable prime test for odd N >= 3.
     * @param N
     * @return true if N is a probable prime
     */
    boolean isProbablePrimeCore(BigInteger N) {
        // For small N, trial division is much faster than BPSW
        if (N.bitLength() < 32) {
        	return tdiv.isPrime(N.intValue());
        }
        
		// Test residues % 30030. Note that N<30030 have been exclude by trial division above.
		if (!primeRestsMod30030.isPossiblyPrime(N)) return false;

		// The Lucas test is not carried out if N fails the base 2 Miller-Rabin test.
        return millerRabinTest.testSingleBase(N, I_2) && lucasTest.isStrongProbablePrime(N);
    }

    /**
     * @param N
     * @return first prime > N
     */
    public BigInteger nextProbablePrime(BigInteger N) {
    	// Java's built-in function using a sieve to identify prime candidates is stronger for N>=256 bit.
    	if (N.bitLength()>=256) return N.nextProbablePrime();
    	
        N = N.abs(); // sign is irrelevant
        if (N.bitLength()<=1) return I_2;
    	// skip argument and make even argument odd and >= 3
        N = N.testBit(0) ? N.add(I_2) : N.add(I_1);
    	
    	// loop to next prime
    	while (!isProbablePrimeCore(N)) N = N.add(I_2);
    	return N;
    }
}
