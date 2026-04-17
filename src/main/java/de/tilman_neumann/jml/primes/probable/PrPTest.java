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

/**
 * A probable prime test for arbitrary precision numbers.
 * 
 * For N<32 bit it does trial division. Otherwise it checks the residues of N % 30030, and then 
 * does a Miller-Rabin test with Sinclairs bases for N<64 bit, or a BPSW test for larger N.
 * 
 * Note that both the Sinclair-Miller-Rabin and BPSW test are considered deterministic prime tests for N<=2^64.
 * 
 * See http://en.wikipedia.org/wiki/Baillie-PSW_primality_test
 * and http://miller-rabin.appspot.com/.
 * 
 * @author Tilman Neumann
 */
public class PrPTest extends BPSWTest {
	/** The 7 Miller-Rabin bases found by Jim Sinclair */
	private static final BigInteger[] SINCLAIR_BASES = new BigInteger[] {I_2, BigInteger.valueOf(325), BigInteger.valueOf(9375), BigInteger.valueOf(28178), BigInteger.valueOf(450775), BigInteger.valueOf(9780504), BigInteger.valueOf(1795265022)};

    /**
     * Probable prime test for odd N >= 3.
     * @param N
     * @return true if N is a probable prime
     */
	@Override
    boolean isProbablePrimeCore(BigInteger N) {
        // For small N, trial division is much faster than BPSW
        int Nbits = N.bitLength();
        if (Nbits < 32) {
        	return tdiv.isPrime(N.intValue());
        }
        
		// Test residues % 30030. Note that N<30030 have been exclude by trial division above.
		if (!primeRestsMod30030.isPossiblyPrime(N)) return false;

        if (Nbits < 64) {
        	// For N<64 bit, a deterministic Miller-Rabin test is faster than BPSW.
        	// Note that the algorithm would fail for N = 3, 5, 13, 19, 73, 193, 407521, 299210837, 
        	// which are the factors of the bases. But all these N < 32 bit are tested by tdiv above,
        	// so no problem here...
    		return millerRabinTest.testBases(N, SINCLAIR_BASES);
        }

		// Do BPSW test: The Lucas test is not carried out if N fails the base 2 Miller-Rabin test.
        return millerRabinTest.testSingleBase(N, I_2) && lucasTest.isStrongProbablePrime(N);
    }
}
