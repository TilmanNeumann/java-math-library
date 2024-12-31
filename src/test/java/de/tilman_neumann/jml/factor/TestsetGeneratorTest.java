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

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

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
	private static final int INCR_BITS = 10;

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}

	@Test
	public void testGeneratedNumberSize() {
		Timer timer = new Timer();
		for (int bits = MIN_BITS; bits<=MAX_BITS; bits+=INCR_BITS) {
			long start = timer.capture();
			BigInteger[] testNumbers = TestsetGenerator.generate(NCOUNT, bits, TestNumberNature.MODERATE_SEMIPRIMES);
			long end = timer.capture();
			// Collect the true bit lengths
			Map<Integer, Integer> sizeCounts = new TreeMap<>();
			for (BigInteger num : testNumbers) {
				int bitlen = num.bitLength();
				Integer count = sizeCounts.get(bitlen);
				count = (count==null) ? Integer.valueOf(1) : count.intValue()+1;
				sizeCounts.put(bitlen, count);
			}
			String generatedBitLengths = "";
			for (int bitlen : sizeCounts.keySet()) {
				generatedBitLengths += sizeCounts.get(bitlen) + "x" + bitlen + ", ";
			}
			generatedBitLengths = generatedBitLengths.substring(0, generatedBitLengths.length()-2);
			LOG.info("Requesting " + NCOUNT + " " + bits + "-numbers took " + TimeUtil.timeDiffStr(start, end) + " ms and generated the following bit lengths: " + generatedBitLengths);
			// all generated test numbers have the requested bit length
			assertEquals(NCOUNT + "x" + bits, generatedBitLengths);
		}
	}
}
