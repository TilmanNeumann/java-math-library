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
	
	private static final int LOWER_INT = 1<<10;
	private static final int UPPER_INT = 1<<25;
	private static final long LOWER_LONG = 1L<<20;
	private static final long UPPER_LONG = 1L<<50;

	private static final SecureRandom secureRandom = new SecureRandom();
	private static final Random random = new Random();
	private static final Rng rng = new Rng();
	private static final SpRand32 spRand32 = new SpRand32();
	private static final Xorshf32 xorshf32 = new Xorshf32();
	private static final Xorshf32b xorshf32b = new Xorshf32b();
	private static final LehmerRng64 lehmer64 = new LehmerRng64();
	private static final LehmerRng64MH lehmer64MH = new LehmerRng64MH();
	private static final Xorshf64 xorshf64 = new Xorshf64();
	private static final SplitMix64 splitMix64 = new SplitMix64();

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
		for (int i=0; i<NCOUNT; i++) {
			xorshf32b.nextInt();
		}
		LOG.debug("Xorshf32b.nextInt() took " + timer.capture() + " ms");
		
		// test nextInt(int upper)
		if (TEST_SLOW) {
			for (int i=0; i<NCOUNT; i++) {
				secureRandom.nextInt(UPPER_INT);
			}
			LOG.debug("SecureRandom.nextInt(" + UPPER_INT + ") took " + timer.capture() + " ms");
		}
		for (int i=0; i<NCOUNT; i++) {
			random.nextInt(UPPER_INT);
		}
		LOG.debug("Random.nextInt(" + UPPER_INT + ") took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			rng.nextInt(UPPER_INT);
		}
		LOG.debug("Rng.nextInt(" + UPPER_INT + ") took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			spRand32.nextInt(UPPER_INT);
		}
		LOG.debug("SpRand32.nextInt(" + UPPER_INT + ") took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			xorshf32.nextInt(UPPER_INT);
		}
		LOG.debug("Xorshf32.nextInt(" + UPPER_INT + ") took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			xorshf32b.nextInt(UPPER_INT);
		}
		LOG.debug("Xorshf32b.nextInt(" + UPPER_INT + ") took " + timer.capture() + " ms");
		
		// test nextInt(int lower, int upper)
		/*
		if (TEST_SLOW) {
			for (int i=0; i<NCOUNT; i++) {
				secureRandom.nextInt(LOWER_INT, UPPER_INT); // XXX which Java version is required ?
			}
			LOG.debug("SecureRandom.nextInt(" + LOWER_INT + ", " + UPPER_INT + ") took " + timer.capture() + " ms");
		}
		for (int i=0; i<NCOUNT; i++) {
			random.nextInt(LOWER_INT, UPPER_INT); // XXX which Java version is required ?
		}
		LOG.debug("Random.nextInt(" + LOWER_INT + ", " + UPPER_INT + ") took " + timer.capture() + " ms");
		*/
		for (int i=0; i<NCOUNT; i++) {
			rng.nextInt(LOWER_INT, UPPER_INT);
		}
		LOG.debug("Rng.nextInt(" + LOWER_INT + ", " + UPPER_INT + ") took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			spRand32.nextInt(LOWER_INT, UPPER_INT);
		}
		LOG.debug("SpRand32.nextInt(" + LOWER_INT + ", " + UPPER_INT + ") took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			xorshf32.nextInt(LOWER_INT, UPPER_INT);
		}
		LOG.debug("Xorshf32.nextInt(" + LOWER_INT + ", " + UPPER_INT + ") took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			xorshf32b.nextInt(LOWER_INT, UPPER_INT);
		}
		LOG.debug("Xorshf32b.nextInt(" + LOWER_INT + ", " + UPPER_INT + ") took " + timer.capture() + " ms");
		
		// test nextLong()
		if (TEST_SLOW) {
			for (int i=0; i<NCOUNT; i++) {
				secureRandom.nextLong();
			}
			LOG.debug("SecureRandom.nextLong() took " + timer.capture() + " ms");
		}
		for (int i=0; i<NCOUNT; i++) {
			random.nextLong();
		}
		LOG.debug("Random.nextLong() took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			rng.nextLong();
		}
		LOG.debug("Rng.nextLong() took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			lehmer64.nextLong();
		}
		LOG.debug("LehmerRng64.nextLong() took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			lehmer64MH.nextLong();
		}
		LOG.debug("LehmerRng64MH.nextLong() took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			xorshf64.nextLong();
		}
		LOG.debug("Xorshf64.nextLong() took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			splitMix64.nextLong();
		}
		LOG.debug("SplitMix64.nextLong() took " + timer.capture() + " ms");
		
		// test nextLong(long upper)
		for (int i=0; i<NCOUNT; i++) {
			xorshf64.nextLong(UPPER_LONG);
		}
		LOG.debug("Xorshf64.nextLong(" + UPPER_LONG + ") took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			splitMix64.nextLong(UPPER_LONG);
		}
		LOG.debug("SplitMix64.nextLong(" + UPPER_LONG + ") took " + timer.capture() + " ms");

		// test nextLong(long lower, long upper)
		for (int i=0; i<NCOUNT; i++) {
			xorshf64.nextLong(LOWER_LONG, UPPER_LONG);
		}
		LOG.debug("Xorshf64.nextLong(" + LOWER_LONG + ", " + UPPER_LONG + ") took " + timer.capture() + " ms");
		for (int i=0; i<NCOUNT; i++) {
			splitMix64.nextLong(LOWER_LONG, UPPER_LONG);
		}
		LOG.debug("SplitMix64.nextLong(" + LOWER_LONG + ", " + UPPER_LONG + ") took " + timer.capture() + " ms");
	}
}
