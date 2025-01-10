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
 * Tests of the LehmerRng64MH random number generator.
 * 
 * @author Tilman Neumann
 */
public class LehmerRng64MHTest {
	private static final Logger LOG = LogManager.getLogger(LehmerRng64MHTest.class);

	private static final int NCOUNT = 1000000;
	
	private static final LehmerRng64MH rng = new LehmerRng64MH();
	
	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}
	
	@Test
	public void testNextLongNoArgs() {
		long min = Long.MAX_VALUE, max = Long.MIN_VALUE;
		boolean generatesEven = false, generatesOdd = false;
		for (int i=0; i<NCOUNT; i++) {
			long n = rng.nextLong();
			if (n<min) min = n;
			if (n>max) max = n;
			if ((n & 1) == 1) {
				generatesOdd = true;
			} else {
				generatesEven = true;
			}
		}
		LOG.debug(NCOUNT + " numbers from " + rng.getClass().getSimpleName() + ".nextLong() gave min = " + min + ", max = " + max);
		assertTrue(min < 0);
		assertTrue(generatesEven);
		assertTrue(generatesOdd);
	}
}
