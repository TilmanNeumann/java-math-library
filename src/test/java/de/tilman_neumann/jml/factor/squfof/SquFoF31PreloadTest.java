/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2024 Tilman Neumann - tilman.neumann@web.de
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
import java.security.SecureRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;

public class SquFoF31PreloadTest {

	private static final Logger LOG = LogManager.getLogger(SquFoF31PreloadTest.class);

	private static SquFoF31Preload squfof31;
	private static FactorAlgorithm testFactorizer;

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		squfof31 = new SquFoF31Preload();
		testFactorizer = FactorAlgorithm.getDefault();
	}

	@Test
	public void testSomeParticularNumbers() {
		assertFactorizationSuccess(1099511627970L, "2 * 3 * 5 * 7 * 23 * 227642159"); // 41 bit
	}

	/**
	 * Tests random composite numbers in the range where SquFoF31Preload should work correctly, i.e. up to 52 or 53 bit.
	 */
	@Test
	public void testRandomComposites() {
		SecureRandom RNG = new SecureRandom();
		int count = 10000;
		for (int bits=40; bits<54; bits++) {
			LOG.info("Testing " + count + " random numbers with " + bits + " bit...");
			int failCount = 0;
			for (int i=0; i<count; i++) {
				long N = 0;
				while (true) {
					BigInteger N_big = new BigInteger(bits, RNG);
					N = N_big.longValue();
					if (N>2 && !N_big.isProbablePrime(20)) break;
				}
				long squfofFactor = squfof31.findSingleFactor(N);
				if (squfofFactor < 2) {
					long correctFactor = testFactorizer.findSingleFactor(BigInteger.valueOf(N)).longValue();
					if (correctFactor > 1 && correctFactor<N) {
						LOG.debug("N=" + N + ": SquFoF31 failed to find factor " + correctFactor);
						failCount++;
					} else {
						LOG.error("The reference factorizer failed to factor N=" + N + " !");
						fail();
					}
				} else {
					if (N % squfofFactor != 0) {
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
		SortedMultiset<BigInteger> factors = squfof31.factor(NBig);
		LOG.info(N + " = " + factors.toString("*", "^"));
		assertEquals(expectedPrimeFactorizationStr, factors.toString("*", "^"));
	}
}
