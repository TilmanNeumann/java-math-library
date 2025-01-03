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
package de.tilman_neumann.jml.factor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.TimeUtil;
import de.tilman_neumann.util.Timer;

public class TestsetGeneratorTest {
	private static final Logger LOG = LogManager.getLogger(TestsetGeneratorTest.class);

	// the following parameters have been chosen to make the test run less than 10 seconds on github CI.
	private static final int NCOUNT = 10;
	private static final int MIN_BITS = 20;
	private static final int MAX_BITS = 1000;
	private static final int INCR_BITS = 20;

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}

	@Test
	public void testRandomComposites() {
		Timer timer = new Timer();
		for (int bits = MIN_BITS; bits<=MAX_BITS; bits+=INCR_BITS) {
			BigInteger[] testNumbers = TestsetGenerator.generate(NCOUNT, bits, TestNumberNature.RANDOM_COMPOSITES);
			for (BigInteger num : testNumbers) {
				assertEquals(bits, num.bitLength());
				assertTrue(num.signum() > 0);
			}
		}
		LOG.info("Computing random composites took " + TimeUtil.timeStr(timer.totalRuntime()));
	}

	@Test
	public void testRandomOddComposites() {
		Timer timer = new Timer();
		for (int bits = MIN_BITS; bits<=MAX_BITS; bits+=INCR_BITS) {
			BigInteger[] testNumbers = TestsetGenerator.generate(NCOUNT, bits, TestNumberNature.RANDOM_ODD_COMPOSITES);
			for (BigInteger num : testNumbers) {
				assertEquals(bits, num.bitLength());
				assertTrue(num.signum() > 0);
				assertEquals(num.intValue() & 1, 1);
			}
		}
		LOG.info("Computing random odd composites took " + TimeUtil.timeStr(timer.totalRuntime()));
	}

	@Test
	public void testModerateSemiprimes() {
		Timer timer = new Timer();
		for (int bits = MIN_BITS; bits<=MAX_BITS; bits+=INCR_BITS) {
			BigInteger[] testNumbers = TestsetGenerator.generate(NCOUNT, bits, TestNumberNature.MODERATE_SEMIPRIMES);
			for (BigInteger num : testNumbers) {
				assertEquals(bits, num.bitLength());
				assertTrue(num.signum() > 0);
				// we dont want to factor the numbers here to prove they are semiprimes
			}
		}
		LOG.info("Computing moderate semiprimes took " + TimeUtil.timeStr(timer.totalRuntime()));
	}

	@Test
	public void testHardSemiprimes() {
		Timer timer = new Timer();
		for (int bits = MIN_BITS; bits<=MAX_BITS; bits+=INCR_BITS) {
			BigInteger[] testNumbers = TestsetGenerator.generate(NCOUNT, bits, TestNumberNature.QUITE_HARD_SEMIPRIMES);
			for (BigInteger num : testNumbers) {
				assertEquals(bits, num.bitLength());
				assertTrue(num.signum() > 0);
				// we dont want to factor the numbers here to prove they are semiprimes
			}
		}
		LOG.info("Computing quite hard semiprimes took " + TimeUtil.timeStr(timer.totalRuntime()));
	}
}
