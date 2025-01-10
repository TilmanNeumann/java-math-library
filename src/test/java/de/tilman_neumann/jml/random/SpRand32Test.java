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

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Tests of the SpRand32 random number generator.
 * 
 * @author Tilman Neumann
 */
public class SpRand32Test {
	private static final Logger LOG = LogManager.getLogger(SpRand32Test.class);

	private static final int NCOUNT = 1000000;
	private static final int LOWER = 1<<10;
	private static final int UPPER = 1<<25;
	
	private static final SpRand32 rng = new SpRand32();
	
	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}
	
	@Test
	public void testNextIntNoArgs() {
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		for (int i=0; i<NCOUNT; i++) {
			int n = rng.nextInt();
			if (n<min) min = n;
			if (n>max) max = n;
		}
		LOG.debug(NCOUNT + " numbers from " + rng.getClass().getSimpleName() + ".nextInt() gave min = " + min + ", max = " + max);
		assertTrue(min >= 0);
	}
	
	@Test
	public void testNextIntUpperBound() {
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		for (int i=0; i<NCOUNT; i++) {
			int n = rng.nextInt(UPPER);
			if (n<min) min = n;
			if (n>max) max = n;
		}
		LOG.debug(NCOUNT + " numbers from " + rng.getClass().getSimpleName() + ".nextInt(" + UPPER + ") gave min = " + min + ", max = " + max);
		assertTrue(min >= 0);
		assertTrue(max <= UPPER);
	}
	
	@Test
	public void testNextIntLowerUpperBound() {
		int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
		for (int i=0; i<NCOUNT; i++) {
			int n = rng.nextInt(LOWER, UPPER);
			if (n<min) min = n;
			if (n>max) max = n;
		}
		LOG.debug(NCOUNT + " numbers from " + rng.getClass().getSimpleName() + ".nextInt(" + LOWER + ", " + UPPER + ") gave min = " + min + ", max = " + max);
		assertTrue(min >= LOWER);
		assertTrue(max <= UPPER);
	}
}
