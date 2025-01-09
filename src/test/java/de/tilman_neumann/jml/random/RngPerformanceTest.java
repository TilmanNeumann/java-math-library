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

import java.security.SecureRandom;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.Timer;

/**
 * Test performance of different random generators.
 * 
 * SecureRandom is in the order of 100 slower than the others.
 * Random and Rng are 5 times slower than SpRand32 and Xorshf32.
 * 
 * @author Tilman Neumann
 */
public class RngPerformanceTest {
	
	private static final Logger LOG = LogManager.getLogger(RngPerformanceTest.class);
	
	private static final boolean TEST_SLOW = false;
	
	private static final int NCOUNT = 1000000000;
	
	private static final SecureRandom secureRandom = new SecureRandom();
	private static final Random random = new Random();
	private static final Rng rng = new Rng();
	private static final SpRand32 spRand32 = new SpRand32();
	private static final Xorshf32 xorshf32 = new Xorshf32();
	
	/**
	 * Test.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		ConfigUtil.initProject();

		// test nextInt()
		Timer timer = new Timer();
		if (TEST_SLOW) {
			for (int i=0; i<NCOUNT; i++) {
				secureRandom.nextInt();
			}
			LOG.debug("SecureRandom.nextInt() took " + timer.capture() + " ms");
		}
		for (int i=0; i<NCOUNT; i++) {
			random.nextInt();
		}
		LOG.debug("Random.nextInt() took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			rng.nextInt();
		}
		LOG.debug("Rng.nextInt() took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			spRand32.nextInt();
		}
		LOG.debug("SpRand32.nextInt() took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			xorshf32.nextInt();
		}
		LOG.debug("Xorshf32.nextInt() took " + timer.capture() + " ms");
		
		// test nextInt(int upper)
		int upper = 1<<30 + 29;
		if (TEST_SLOW) {
			for (int i=0; i<NCOUNT; i++) {
				secureRandom.nextInt(upper);
			}
			LOG.debug("SecureRandom.nextInt(upper) took " + timer.capture() + " ms");
		}
		for (int i=0; i<NCOUNT; i++) {
			random.nextInt(upper);
		}
		LOG.debug("Random.nextInt(upper) took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			rng.nextInt(upper);
		}
		LOG.debug("Rng.nextInt(upper) took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			spRand32.nextInt(upper);
		}
		LOG.debug("SpRand32.nextInt(upper) took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			xorshf32.nextInt(upper);
		}
		LOG.debug("Xorshf32.nextInt(upper) took " + timer.capture() + " ms");
		
		// test nextInt(int lower, int upper)
		int lower = 12345;
		if (TEST_SLOW) {
			for (int i=0; i<NCOUNT; i++) {
				secureRandom.nextInt(lower, upper);
			}
			LOG.debug("SecureRandom.nextInt(lower, upper) took " + timer.capture() + " ms");
		}
		for (int i=0; i<NCOUNT; i++) {
			random.nextInt(lower, upper);
		}
		LOG.debug("Random.nextInt(lower, upper) took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			rng.nextInt(lower, upper);
		}
		LOG.debug("Rng.nextInt(lower, upper) took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			spRand32.nextInt(lower, upper);
		}
		LOG.debug("SpRand32.nextInt(lower, upper) took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			xorshf32.nextInt(lower, upper);
		}
		LOG.debug("Xorshf32.nextInt(lower, upper) took " + timer.capture() + " ms");
	}
}
