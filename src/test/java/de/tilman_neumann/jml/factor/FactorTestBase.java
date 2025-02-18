/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2025 Tilman Neumann - tilman.neumann@web.de
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
package de.tilman_neumann.jml.factor;

import static de.tilman_neumann.jml.base.BigIntConstants.I_1;
import static de.tilman_neumann.jml.base.BigIntConstants.I_4;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tilman_neumann.jml.primes.probable.BPSWTest;
import de.tilman_neumann.util.SortedMultiset;

/**
 * Infrastructure to simplify unit tests of factoring algorithms.
 * 
 * @author Tilman Neumann
 */
public class FactorTestBase {

	private static final Logger LOG = LogManager.getLogger(FactorTestBase.class);

	private static final BPSWTest bpsw = new BPSWTest();
	
	private static FactorAlgorithm factorizer;
	private static FactorAlgorithm verificationFactorizer;

	protected static void setFactorizer(FactorAlgorithm factorizer) {
		FactorTestBase.setFactorizer(factorizer, FactorAlgorithm.getDefault());
	}

	protected static void setFactorizer(FactorAlgorithm factorizer, FactorAlgorithm verificationFactorizer) {
		FactorTestBase.factorizer = factorizer;
		FactorTestBase.verificationFactorizer = verificationFactorizer;
	}
	
	/**
	 * Tests the full factorization of all composites in the range [4, nMax] by the given factorAlgorithm.<br/><br/>
	 * 
	 * @param nMax
	 * @param factorAlgorithm
	 * @return list of the factor arguments that were not factored correctly
	 */
	protected List<BigInteger> testFullFactorizationOfComposites(long nMax) {
		return testFullFactorizationOfComposites(I_4, BigInteger.valueOf(nMax));
	}

	/**
	 * Tests the full factorization of all composites in the range [nMin, nMax] by the given factorAlgorithm.<br/><br/>
	 * 
	 * @param nMin
	 * @param nMax
	 * @param factorAlgorithm
	 * @return list of the factor arguments that were not factored correctly
	 */
	// XXX This test could be made more efficient using a prime or composites sieve
	protected List<BigInteger> testFullFactorizationOfComposites(BigInteger nMin, BigInteger nMax) {
		ArrayList<BigInteger> fails = new ArrayList<>();
		for (BigInteger n=nMin; n.compareTo(nMax)<=0; n=n.add(I_1)) {
			if (bpsw.isProbablePrime(n)) continue; // skip primes
			SortedMultiset<BigInteger> factors = factorizer.factor(n);
			boolean isFail = false;
			BigInteger testProd = I_1;
			for (BigInteger factor : factors.keySet()) {
				if (!bpsw.isProbablePrime(factor)) {
					isFail = true;
					break;
				}
				int exp = factors.get(factor).intValue();
				BigInteger pow = factor.pow(exp);
				testProd = testProd.multiply(pow);
			}
			if (!testProd.equals(n)) {
				isFail = true;
			}
			if (isFail) {
				fails.add(n);
			}
		}
		return fails;
	}

	/**
	 * Tests finding a single factor of all semiprimes in the range [nMin, nMax] by the given factorAlgorithm.<br/><br/>
	 * 
	 * @param nMin
	 * @param nMax
	 * @param factorAlgorithm
	 * @return list of the factor arguments that were not factored correctly
	 */
	protected List<BigInteger> testFindSingleFactorForSemiprimes(BigInteger nMin, BigInteger nMax) {
		ArrayList<BigInteger> fails = new ArrayList<>();
		for (BigInteger n=nMin; n.compareTo(nMax)<=0; n=n.add(I_1)) {
			if (!isSemiprime(n)) continue;
			
			BigInteger factor = factorizer.findSingleFactor(n).abs();
			if (factor==null || factor.compareTo(I_1) <= 0 || factor.compareTo(n) >= 0 || !factor.multiply(n.divide(factor)).equals(n)) {
				fails.add(n);
			}
		}
		return fails;
	}
	
	private boolean isSemiprime(BigInteger N) {
		if (bpsw.isProbablePrime(N)) return false;
		
		BigInteger factor1 = verificationFactorizer.findSingleFactor(N);
		assertTrue(factor1.compareTo(I_1) > 0 && factor1.compareTo(N) < 0); // otherwise the verificationFactorizer failed
		BigInteger factor2 = N.divide(factor1);
		return bpsw.isProbablePrime(factor1) && bpsw.isProbablePrime(factor2);
	}

	/**
	 * Tests <code>findSingleFactor()</code> for <code>count</code> random odd composites for each bit size in the range of [minBits, maxBits] bits.
	 * @param minBits
	 * @param maxBits
	 * @param count
	 */
	protected void testFindSingleFactorForRandomOddComposites(int minBits, int maxBits, int count) {
		for (int bits=minBits; bits<=maxBits; bits++) {
			BigInteger[] testNumbers = TestsetGenerator.generate(count, bits, TestNumberNature.RANDOM_ODD_COMPOSITES);
			LOG.info("Testing " + count + " random numbers with " + bits + " bit...");
			int failCount = 0;
			for (int i=0; i<count; i++) {
				BigInteger N = testNumbers[i];
				BigInteger squfofFactor = factorizer.findSingleFactor(N);
				if (squfofFactor.compareTo(I_1) <= 0) {
					BigInteger correctFactor = verificationFactorizer.findSingleFactor(N);
					if (correctFactor.compareTo(I_1)>0 && correctFactor.compareTo(N)<0) {
						LOG.debug("N=" + N + ": " + factorizer.getName() + " failed to find factor " + correctFactor);
						failCount++;
					} else {
						LOG.error("The verification factorizer " + verificationFactorizer.getName() + " failed to factor N=" + N + " !");
						fail();
					}
				} else {
					if (N.mod(squfofFactor).signum() != 0) {
						failCount++;
					}
				}
			}
			LOG.info("    #fails = " + failCount);
			assertEquals(failCount, 0);
		}
	}

	/**
	 * Assert the correctness of a full prime factorization.
	 * @param N the integer to factor (should be odd)
	 * @param expectedPrimeFactorizationStr string representation of the expected full prime factorization
	 */
	protected void assertFullFactorizationSuccess(long N, String expectedPrimeFactorizationStr) {
		BigInteger NBig = BigInteger.valueOf(N);
		SortedMultiset<BigInteger> factors = factorizer.factor(NBig);
		LOG.info(N + " (" + NBig.bitLength() + " bit) = " + factors.toString("*", "^"));
		assertEquals(expectedPrimeFactorizationStr, factors.toString("*", "^"));
	}

	/**
	 * Assert the failure of a full prime factorization.
	 * @param N the integer to factor (should be odd)
	 * @param expectedPrimeFactorizationStr string representation of the correct full prime factorization
	 */
	protected void assertFullFactorizationFailure(long N, String expectedPrimeFactorizationStr) {
		BigInteger NBig = BigInteger.valueOf(N);
		SortedMultiset<BigInteger> factors = factorizer.factor(NBig);
		LOG.info(N + " (" + NBig.bitLength() + " bit) = " + factors.toString("*", "^"));
		assertNotEquals(expectedPrimeFactorizationStr, factors.toString("*", "^"));
	}

	/**
	 * Assert the correctness of a full prime factorization.
	 * @param NStr string representation of the integer to factor (should be odd)
	 * @param expectedPrimeFactorizationStr string representation of the expected full prime factorization
	 */
	protected void assertFullFactorizationSuccess(String NStr, String expectedPrimeFactorizationStr) {
		BigInteger N = new BigInteger(NStr);
		long t0 = System.currentTimeMillis();
		SortedMultiset<BigInteger> factors = factorizer.factor(N);
		long t1 = System.currentTimeMillis();
		LOG.info(N + " (" + N.bitLength() + " bit) = " + factors.toString("*", "^") + " computed in " + (t1-t0) + "ms");
		assertEquals(expectedPrimeFactorizationStr, factors.toString("*", "^"));
	}

	/**
	 * Assert the failure of a full prime factorization.
	 * @param NStr string representation of the integer to factor (should be odd)
	 * @param expectedPrimeFactorizationStr string representation of the correct full prime factorization
	 */
	protected void assertFullFactorizationFailure(String NStr, String expectedPrimeFactorizationStr) {
		BigInteger N = new BigInteger(NStr);
		long t0 = System.currentTimeMillis();
		SortedMultiset<BigInteger> factors = factorizer.factor(N);
		long t1 = System.currentTimeMillis();
		LOG.info(N + " (" + N.bitLength() + " bit) = " + factors.toString("*", "^") + " computed in " + (t1-t0) + "ms");
		assertNotEquals(expectedPrimeFactorizationStr, factors.toString("*", "^"));
	}
}
