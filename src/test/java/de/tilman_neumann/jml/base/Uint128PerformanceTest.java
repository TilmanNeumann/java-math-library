/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2026 Tilman Neumann - tilman.neumann@web.de
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
package de.tilman_neumann.jml.base;

import java.util.Arrays;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Test performance of 128 bit operations.
 * 
 * Performance tests of 2-argument methods are carried out in double loops over the same numbers.
 * Otherwise number creation would be much more expensive than testing the operations themselves.
 *
 * Despite some effort, the timings are still quite unreliable.
 * E.g. mul64_MH looks sometimes slightly faster than mul64 (which would be the expected result) but quite often notably slower.
 */
public class Uint128PerformanceTest {
	private static final Logger LOG = LogManager.getLogger(Uint128PerformanceTest.class);
	
	private static final int NCOUNT = 1000000;
	private static final int NCOUNT_ADD = 50000;
	private static final int NCOUNT_MUL = 10000;
	private static final int NCOUNT_DIV = 3000;
	private static final int REPEATS = 10;
	private static final int WARMUPS = 2;

	private static final Random RNG = new Random();

	private static void testPerformance() {
		
		// set up test numbers
		long[] a_arr = new long[NCOUNT];
		long[] b_arr = new long[NCOUNT];
		Uint128[] a128_arr =  new Uint128[NCOUNT];
		
		for (int i=0; i<NCOUNT; i++) {
			a_arr[i] = RNG.nextLong();
			b_arr[i] = RNG.nextLong();
			a128_arr[i] = new Uint128(a_arr[i], b_arr[i]);
		}
		
		// test performance of conversion
		
		long t0, t1, duration, totalDuration;
		long[] allDurations = new long[REPEATS];
		
		totalDuration = 0;
		for (int r=0; r<WARMUPS+REPEATS; r++) {
			t0 = System.currentTimeMillis();
			for (int i=0; i<NCOUNT; i++) {
				a128_arr[i].toBigInteger();
			}
			t1 = System.currentTimeMillis();
			duration = t1-t0;
			if (r >= WARMUPS) {
				totalDuration += duration;
				allDurations[r - WARMUPS] = duration;
			}
		}
		LOG.info("toBigInteger took " + totalDuration + "ms " + Arrays.toString(allDurations));

		totalDuration = 0;
		for (int r=0; r<WARMUPS+REPEATS; r++) {
			t0 = System.currentTimeMillis();
			for (int i=0; i<NCOUNT; i++) {
				a128_arr[i].toBigIntegerUnsigned();
			}
			t1 = System.currentTimeMillis();
			duration = t1-t0;
			if (r >= WARMUPS) {
				totalDuration += duration;
				allDurations[r - WARMUPS] = duration;
			}
		}
		LOG.info("toBigIntegerUnsigned took " + totalDuration + "ms " + Arrays.toString(allDurations));

		// test performance of add/subtract implementations

		totalDuration = 0;
		for (int r=0; r<WARMUPS+REPEATS; r++) {
			t0 = System.currentTimeMillis();
			for (int i=0; i<NCOUNT_ADD; i++) {
				for (int j=0; j<NCOUNT_ADD; j++) {
					a128_arr[i].add_v1(a128_arr[j]);
				}
			}
			t1 = System.currentTimeMillis();
			duration = t1-t0;
			if (r >= WARMUPS) {
				totalDuration += duration;
				allDurations[r - WARMUPS] = duration;
			}
		}
		LOG.info("add_v1 took " + totalDuration + "ms " + Arrays.toString(allDurations));

		totalDuration = 0;
		for (int r=0; r<WARMUPS+REPEATS; r++) {
			t0 = System.currentTimeMillis();
			for (int i=0; i<NCOUNT_ADD; i++) {
				for (int j=0; j<NCOUNT_ADD; j++) {
					a128_arr[i].add/*_v2*/(a128_arr[j]);
				}
			}
			t1 = System.currentTimeMillis();
			duration = t1-t0;
			if (r >= WARMUPS) {
				totalDuration += duration;
				allDurations[r - WARMUPS] = duration;
			}
		}
		LOG.info("add_v2 took " + totalDuration + "ms " + Arrays.toString(allDurations));

		totalDuration = 0;
		for (int r=0; r<WARMUPS+REPEATS; r++) {
			t0 = System.currentTimeMillis();
			for (int i=0; i<NCOUNT_ADD; i++) {
				for (int j=0; j<NCOUNT_ADD; j++) {
					a128_arr[i].add_v3(a128_arr[j]);
				}
			}
			t1 = System.currentTimeMillis();
			duration = t1-t0;
			if (r >= WARMUPS) {
				totalDuration += duration;
				allDurations[r - WARMUPS] = duration;
			}
		}
		LOG.info("add_v3 took " + totalDuration + "ms " + Arrays.toString(allDurations));

		totalDuration = 0;
		for (int r=0; r<WARMUPS+REPEATS; r++) {
			t0 = System.currentTimeMillis();
			for (int i=0; i<NCOUNT_ADD; i++) {
				for (int j=0; j<NCOUNT_ADD; j++) {
					a128_arr[i].subtract(a128_arr[j]);
				}
			}
			t1 = System.currentTimeMillis();
			duration = t1-t0;
			if (r >= WARMUPS) {
				totalDuration += duration;
				allDurations[r - WARMUPS] = duration;
			}
		}
		LOG.info("subtract took " + totalDuration + "ms " + Arrays.toString(allDurations));

		totalDuration = 0;
		for (int r=0; r<WARMUPS+REPEATS; r++) {
			t0 = System.currentTimeMillis();
			for (int i=0; i<NCOUNT_ADD; i++) {
				for (int j=0; j<NCOUNT_ADD; j++) {
					a128_arr[i].subtract_v2(a128_arr[j]);
				}
			}
			t1 = System.currentTimeMillis();
			duration = t1-t0;
			if (r >= WARMUPS) {
				totalDuration += duration;
				allDurations[r - WARMUPS] = duration;
			}
		}
		LOG.info("subtract_v2 took " + totalDuration + "ms " + Arrays.toString(allDurations));

		// Test performance of mul64 implementations:
		// Here we need to do something with the results to avoid the compiler optimizing thhe tests to nothing
		
		totalDuration = 0;
		for (int r=0; r<WARMUPS+REPEATS; r++) {
			long dummy = 0;
			t0 = System.currentTimeMillis();
			for (int i=0; i<NCOUNT_MUL; i++) {
				for (int j=0; j<NCOUNT_MUL; j++) {
					Uint128 result = Uint128.mul63(a_arr[i], a_arr[j]);
					dummy += result.getHigh() + result.getLow();
				}
			}
			t1 = System.currentTimeMillis();
			LOG.trace("dummy = " + dummy);
			duration = t1-t0;
			if (r >= WARMUPS) {
				totalDuration += duration;
				allDurations[r - WARMUPS] = duration;
			}
		}
		LOG.info("mul63 took " + totalDuration + "ms " + Arrays.toString(allDurations));

		totalDuration = 0;
		for (int r=0; r<WARMUPS+REPEATS; r++) {
			long dummy = 0;
			t0 = System.currentTimeMillis();
			for (int i=0; i<NCOUNT_MUL; i++) {
				for (int j=0; j<NCOUNT_MUL; j++) {
					Uint128 result = Uint128.mul64_v1(a_arr[i], a_arr[j]);
					dummy += result.getHigh() + result.getLow();
				}
			}
			t1 = System.currentTimeMillis();
			LOG.trace("dummy = " + dummy);
			duration = t1-t0;
			if (r >= WARMUPS) {
				totalDuration += duration;
				allDurations[r - WARMUPS] = duration;
			}
		}
		LOG.info("mul64_v1 took " + totalDuration + "ms " + Arrays.toString(allDurations));
		
		totalDuration = 0;
		for (int r=0; r<WARMUPS+REPEATS; r++) {
			long dummy = 0;
			t0 = System.currentTimeMillis();
			for (int i=0; i<NCOUNT_MUL; i++) {
				for (int j=0; j<NCOUNT_MUL; j++) {
					Uint128 result = Uint128.mul64/*_v2*/(a_arr[i], a_arr[j]);
					dummy += result.getHigh() + result.getLow();
				}
			}
			t1 = System.currentTimeMillis();
			LOG.trace("dummy = " + dummy);
			duration = t1-t0;
			if (r >= WARMUPS) {
				totalDuration += duration;
				allDurations[r - WARMUPS] = duration;
			}
		}
		LOG.info("mul64_v2 took " + totalDuration + "ms " + Arrays.toString(allDurations));

		totalDuration = 0;
		for (int r=0; r<WARMUPS+REPEATS; r++) {
			long dummy = 0;
			t0 = System.currentTimeMillis();
			for (int i=0; i<NCOUNT_MUL; i++) {
				for (int j=0; j<NCOUNT_MUL; j++) {
					Uint128 result = Uint128.mul64_v3(a_arr[i], a_arr[j]);
					dummy += result.getHigh() + result.getLow();
				}
			}
			t1 = System.currentTimeMillis();
			LOG.trace("dummy = " + dummy);
			duration = t1-t0;
			if (r >= WARMUPS) {
				totalDuration += duration;
				allDurations[r - WARMUPS] = duration;
			}
		}
		LOG.info("mul64_v3 took " + totalDuration + "ms " + Arrays.toString(allDurations));

		totalDuration = 0;
		for (int r=0; r<WARMUPS+REPEATS; r++) {
			long dummy = 0;
			t0 = System.currentTimeMillis();
			for (int i=0; i<NCOUNT_MUL; i++) {
				for (int j=0; j<NCOUNT_MUL; j++) {
					Uint128 result = Uint128.mul64_MH(a_arr[i], a_arr[j]);
					dummy += result.getHigh() + result.getLow();
				}
			}
			t1 = System.currentTimeMillis();
			LOG.trace("dummy = " + dummy);
			duration = t1-t0;
			if (r >= WARMUPS) {
				totalDuration += duration;
				allDurations[r - WARMUPS] = duration;
			}
		}
		LOG.info("mul64_MH took " + totalDuration + "ms " + Arrays.toString(allDurations));

		// test performance of 128 / 64 bit division and modulus
		
		totalDuration = 0;
		for (int r=0; r<WARMUPS+REPEATS; r++) {
			t0 = System.currentTimeMillis();
			for (int i=0; i<NCOUNT_DIV; i++) {
				for (int j=0; j<NCOUNT_DIV; j++) {
					Uint128.divide128by64Unsigned(a_arr[i], b_arr[i], a_arr[j]);
				}
			}
			t1 = System.currentTimeMillis();
			duration = t1-t0;
			if (r >= WARMUPS) {
				totalDuration += duration;
				allDurations[r - WARMUPS] = duration;
			}
		}
		LOG.info("divide128by64Unsigned took " + totalDuration + "ms " + Arrays.toString(allDurations));
		
		totalDuration = 0;
		for (int r=0; r<WARMUPS+REPEATS; r++) {
			t0 = System.currentTimeMillis();
			for (int i=0; i<NCOUNT_DIV; i++) {
				for (int j=0; j<NCOUNT_DIV; j++) {
					Uint128.mod128by64Unsigned(a_arr[i], b_arr[i], a_arr[j]);
				}
			}
			t1 = System.currentTimeMillis();
			duration = t1-t0;
			if (r >= WARMUPS) {
				totalDuration += duration;
				allDurations[r - WARMUPS] = duration;
			}
		}
		LOG.info("mod128by64Unsigned took " + totalDuration + "ms " + Arrays.toString(allDurations));
	}

	/**
	 * Test.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		ConfigUtil.initProject();
		testPerformance();
	}
}
