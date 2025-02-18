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
package de.tilman_neumann.jml.random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;

public final class SplitMix64Test {
	private static final Logger LOG = LogManager.getLogger(SplitMix64Test.class);

	private static final int NCOUNT = 1000000;
	private static final long LOWER = 1L<<20;
	private static final long UPPER = 1L<<50;

	private static final SplitMix64 rng = new SplitMix64();
	
	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}

	@Test
	public void testRosettacodeExamples() {
		SplitMix64 random = new SplitMix64(1234567);
		assertEquals(6457827717110365317L, random.nextLong());
		assertEquals(3203168211198807973L, random.nextLong());
		assertEquals(-8629252141511181193L, random.nextLong()); // 9817491932198370423 - 2^64
		assertEquals(4593380528125082431L, random.nextLong());
		assertEquals(-2037821214251327795L, random.nextLong()); // 16408922859458223821 - 2^64
    }
	
	@Test
	public void testNextLongNoArgs() {
		long[] firstElements = new long[100];
		long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
		boolean generatesEven = false, generatesOdd = false;
		for (int i=0; i<NCOUNT; i++) {
			long n = rng.nextLong();
			if (i<100) firstElements[i] = n;
			if (n<min) min = n;
			if (n>max) max = n;
			if ((n & 1) == 1) generatesOdd = true; else generatesEven = true;
		}
		LOG.debug("First 100 elements: " + Arrays.toString(firstElements));
		LOG.debug(NCOUNT + " numbers from " + rng.getClass().getSimpleName() + ".nextLong() gave min = " + min + ", max = " + max);
		assertTrue(min < 0);
		assertTrue(generatesEven);
		assertTrue(generatesOdd);
	}
	
	@Test
	public void testNextLongUpperBound() {
		long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
		boolean generatesEven = false, generatesOdd = false;
		for (int i=0; i<NCOUNT; i++) {
			long n = rng.nextLong(UPPER);
			if (n<min) min = n;
			if (n>max) max = n;
			if ((n & 1) == 1) generatesOdd = true; else generatesEven = true;
		}
		LOG.debug(NCOUNT + " numbers from " + rng.getClass().getSimpleName() + ".nextLong(" + UPPER + ") gave min = " + min + ", max = " + max);
		assertTrue(min >= 0);
		assertTrue(max <= UPPER);
		assertTrue(generatesEven);
		assertTrue(generatesOdd);
	}
	
	@Test
	public void testNextLongLowerUpperBound() {
		long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
		boolean generatesEven = false, generatesOdd = false;
		for (int i=0; i<NCOUNT; i++) {
			long n = rng.nextLong(LOWER, UPPER);
			if (n<min) min = n;
			if (n>max) max = n;
			if ((n & 1) == 1) generatesOdd = true; else generatesEven = true;
		}
		LOG.debug(NCOUNT + " numbers from " + rng.getClass().getSimpleName() + ".nextLong(" + LOWER + ", " + UPPER + ") gave min = " + min + ", max = " + max);
		assertTrue(min >= LOWER);
		assertTrue("Expected " + max + " <= " + UPPER, max <= UPPER);
		assertTrue(generatesEven);
		assertTrue(generatesOdd);
	}
}
