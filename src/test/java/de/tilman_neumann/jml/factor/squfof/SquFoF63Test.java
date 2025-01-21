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
package de.tilman_neumann.jml.factor.squfof;

import static org.junit.Assert.assertEquals;
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
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;

import static de.tilman_neumann.jml.base.BigIntConstants.I_1;

public class SquFoF63Test {

	private static final Logger LOG = LogManager.getLogger(SquFoF63Test.class);

	private static final SquFoF63 squfof = new SquFoF63();
	private static final FactorAlgorithm verificationFactorizer = FactorAlgorithm.getDefault();

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}
	
	@Test
	public void testSmallestComposites() {
		List<Integer> fails = FactorTestInfrastructure.testSmallComposites(100000, squfof);
		assertEquals("Failed to factor n = " + fails, 0, fails.size());
	}

	@Test
	public void testSomeParticularNumbers() {
		assertFactorizationSuccess(1099511627970L, "2 * 3 * 5 * 7 * 23 * 227642159"); // 41 bit
	}

	/**
	 * Tests smaller random composite numbers.
	 */
	@Test
	public void testSmallRandomComposites() {
		testRange(50, 69, 1000);
	}

	/**
	 * Tests random composite numbers in the upper range where SquFoF63 works stable, i.e. up to 87 bit.
	 */
	@Test
	public void testLargerRandomComposites() {
		testRange(70, 86, 100);
		testRange(87, 87, 1000);
	}

	private void testRange(int minBits, int maxBits, int count) {
		for (int bits=minBits; bits<=maxBits; bits++) {
			BigInteger[] testNumbers = TestsetGenerator.generate(count, bits, TestNumberNature.RANDOM_ODD_COMPOSITES);
			LOG.info("Testing " + count + " random numbers with " + bits + " bit...");
			int failCount = 0;
			for (int i=0; i<count; i++) {
				BigInteger N = testNumbers[i];
				BigInteger squfofFactor = squfof.findSingleFactor(N);
				if (squfofFactor.compareTo(I_1) <= 0) {
					BigInteger correctFactor = verificationFactorizer.findSingleFactor(N);
					if (correctFactor.compareTo(I_1)>0 && correctFactor.compareTo(N)<0) {
						LOG.debug("N=" + N + ": SquFoF63 failed to find factor " + correctFactor);
						failCount++;
					} else {
						LOG.error("The reference factorizer failed to factor N=" + N + " !");
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

	private void assertFactorizationSuccess(long N, String expectedPrimeFactorizationStr) {
		BigInteger NBig = BigInteger.valueOf(N);
		LOG.info("Test " + N + " (" + NBig.bitLength() + " bit)");
		SortedMultiset<BigInteger> factors = squfof.factor(NBig);
		LOG.info(N + " = " + factors.toString("*", "^"));
		assertEquals(expectedPrimeFactorizationStr, factors.toString("*", "^"));
	}
}
