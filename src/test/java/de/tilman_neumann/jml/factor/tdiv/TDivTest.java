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
package de.tilman_neumann.jml.factor.tdiv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.jml.factor.FactorTestInfrastructure;
import de.tilman_neumann.jml.factor.TestNumberNature;
import de.tilman_neumann.jml.factor.TestsetGenerator;
import de.tilman_neumann.jml.factor.squfof.SquFoF63;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;

import static de.tilman_neumann.jml.base.BigIntConstants.*;

/**
 * Tests for trial division with BigInteger arguments.
 * This test is quite expensive for github CI because of the SMALL_PRIMES initialization;
 * but since it it used in CombinedFactorAlgorithm, we keep it.
 * 
 * @author Tilman Neumann
 */
public class TDivTest {

	private static final Logger LOG = LogManager.getLogger(TDivTest.class);

	private static final TDiv tdiv = new TDiv();
	// don't use CombinedFactorAlgorithm as verificationFactorizer because Tdiv is part of it
	private static final FactorAlgorithm verificationFactorizer = new SquFoF63();

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}
	
	@Test
	public void testSmallestComposites() {
		List<Integer> fails = FactorTestInfrastructure.testSmallComposites(100000, tdiv);
		assertEquals("Failed to factor n = " + fails, 0, fails.size());
	}

	@Test
	public void testSomeParticularNumbers() {
		assertFactorizationSuccess(621887327L, "853 * 729059"); // 30 bit
		assertFactorizationSuccess(676762483L, "877 * 771679"); // 30 bit
		assertFactorizationSuccess(2947524803L, "1433 * 2056891"); // 32 bit
		assertFactorizationSuccess(5616540799L, "1777 * 3160687"); // 33 bit
		assertFactorizationSuccess(35936505149L, "3299 * 10893151"); // 36 bit
		assertFactorizationSuccess(145682871839L, "5261 * 27691099"); // 38 bit
		assertFactorizationSuccess(317756737253L, "6823 * 46571411"); // 39 bit
		assertFactorizationSuccess(1099511627970L, "2 * 3 * 5 * 7 * 23 * 227642159"); // 41 bit
		assertFactorizationSuccess(3294635112749L, "14879 * 221428531"); // 42 bit
		assertFactorizationSuccess(13293477682249L, "398077 * 33394237"); // 44 bit
		assertFactorizationSuccess(24596491225651L, "3311299 * 7428049"); // 45 bit
		assertFactorizationSuccess(44579405690563L, "930889 * 47889067"); // 46 bit
		assertFactorizationSuccess(67915439339311L, "2061599 * 32943089"); // 46 bit
		assertFactorizationSuccess(72795445155721L, "83459 * 872230019"); // 47 bit
		assertFactorizationSuccess(155209074377713L, "361909 * 428862157"); // 48 bit
		assertFactorizationSuccess(293851765137859L, "11736397 * 25037647"); // 49 bit
	}

	/**
	 * Tests random composite numbers in the range where TDiv63Test should work correctly, i.e. with factorLimit=2^21 up to 42 bit.
	 */
	@Test
	public void testRandomComposites() {
		int count = 10000;
		for (int bits=30; bits<43; bits++) {
			BigInteger[] testNumbers = TestsetGenerator.generate(count, bits, TestNumberNature.RANDOM_ODD_COMPOSITES);
			LOG.info("Testing " + count + " random numbers with " + bits + " bit...");
			int failCount = 0;
			for (int i=0; i<count; i++) {
				BigInteger N = testNumbers[i];
				BigInteger tdivFactor = tdiv.findSingleFactor(N);
				if (tdivFactor.compareTo(I_1) <= 0) {
					BigInteger correctFactor = verificationFactorizer.findSingleFactor(N);
					if (correctFactor.compareTo(I_1) > 0 && correctFactor.compareTo(N) < 0) {
						LOG.debug("N=" + N + ": TDiv63Inverse failed to find factor " + correctFactor);
						failCount++;
					} else {
						LOG.error("The reference factorizer failed to factor N=" + N + " !");
						fail();
					}
				} else {
					BigInteger mod = N.mod(tdivFactor);
					if (mod.signum() != 0) {
						failCount++;
					}
				}
			}
			LOG.info("    #fails = " + failCount);
			assertEquals(failCount, 0);
		}
	}

	private void assertFactorizationSuccess(long N, String expectedPrimeFactorizationStr) {
		BigInteger NBig = BigInteger.valueOf(N);
		LOG.info("Test " + N + " (" + NBig.bitLength() + " bit)");
		SortedMultiset<BigInteger> factors = tdiv.factor(NBig);
		LOG.info(N + " = " + factors.toString("*", "^"));
		assertEquals(expectedPrimeFactorizationStr, factors.toString("*", "^"));
	}

	@SuppressWarnings("unused")
	private void assertFactorizationError(long N, String expectedPrimeFactorizationStr) {
		BigInteger NBig = BigInteger.valueOf(N);
		LOG.info("Test " + N + " (" + NBig.bitLength() + " bit)");
		SortedMultiset<BigInteger> factors = tdiv.factor(NBig);
		LOG.info(N + " = " + factors.toString("*", "^"));
		assertNotEquals(expectedPrimeFactorizationStr, factors.toString("*", "^"));
	}
}
