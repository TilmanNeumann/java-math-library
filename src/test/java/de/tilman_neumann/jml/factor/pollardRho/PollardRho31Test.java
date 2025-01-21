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
package de.tilman_neumann.jml.factor.pollardRho;

import java.math.BigInteger;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.jml.factor.FactorTestInfrastructure;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PollardRho31Test {
	private static final Logger LOG = LogManager.getLogger(PollardRho31Test.class);
	
	private static final PollardRho31 pollardRho = new PollardRho31();

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}
	
	@Test
	public void testSmallestComposites() {
		List<Integer> fails = FactorTestInfrastructure.testSmallComposites(100000, pollardRho);
		assertEquals("Failed to factor n = " + fails, 0, fails.size());
	}

	@Test
	public void testCompositesWithSmallFactors() {
		assertFactorizationSuccess(949443, "3 * 11 * 28771"); // 20 bit
		assertFactorizationSuccess(996433, "31 * 32143"); // 20 bit
		assertFactorizationSuccess(1340465, "5 * 7 * 38299"); // 21 bit
		assertFactorizationSuccess(1979435, "5 * 395887"); // 21 bit
		assertFactorizationSuccess(2514615, "3 * 5 * 167641"); // 22 bit
		assertFactorizationSuccess(5226867, "3^2 * 580763"); // 23 bit
		assertFactorizationSuccess(10518047, "61 * 172427"); // 24 bit
		assertFactorizationSuccess(30783267, "3^3 * 1140121"); // 25 bit
		assertFactorizationSuccess(62230739, "67 * 928817"); // 26 bit
		assertFactorizationSuccess(84836647, "7 * 17 * 712913"); // 27 bit
		assertFactorizationSuccess(94602505, "5 * 18920501"); // 27 bit
		assertFactorizationSuccess(258555555, "3^2 * 5 * 5745679"); // 28 bit
		assertFactorizationSuccess(436396385, "5 * 87279277"); // 29 bit
		assertFactorizationSuccess(612066705, "3 * 5 * 40804447"); // 30 bit
		assertFactorizationSuccess(2017001503, "11 * 183363773"); // 31 bit
		assertFactorizationSuccess(712869263, "89 * 8009767"); // 30 bit
		assertFactorizationSuccess(386575807, "73 * 5295559"); // 29 bit
		assertFactorizationSuccess(569172749, "83 * 6857503"); // 30 bit
	}
	
	@Test
	public void testSquares() {
		assertFactorizationSuccess(100140049, "10007^2"); // 27 bit
	}

	private void assertFactorizationSuccess(long N, String expectedPrimeFactorizationStr) {
		BigInteger NBig = BigInteger.valueOf(N);
		LOG.info("Test " + N + " (" + NBig.bitLength() + " bit)");
		SortedMultiset<BigInteger> factors = pollardRho.factor(NBig);
		LOG.info(N + " = " + factors.toString("*", "^"));
		assertEquals(expectedPrimeFactorizationStr, factors.toString("*", "^"));
	}

	@SuppressWarnings("unused")
	private void assertFactorizationError(long N, String expectedPrimeFactorizationStr) {
		BigInteger NBig = BigInteger.valueOf(N);
		LOG.info("Test " + N + " (" + NBig.bitLength() + " bit)");
		SortedMultiset<BigInteger> factors = pollardRho.factor(NBig);
		LOG.info(N + " = " + factors.toString("*", "^"));
		assertNotEquals(expectedPrimeFactorizationStr, factors.toString("*", "^"));
	}
}
