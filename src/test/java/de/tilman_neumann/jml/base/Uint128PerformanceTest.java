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
package de.tilman_neumann.jml.base;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

public class Uint128PerformanceTest {
	private static final Logger LOG = LogManager.getLogger(Uint128PerformanceTest.class);

	private static final Random RNG = new Random();

	private static void testPerformance() {
		// Performance tests are carried out in double loops over the same numbers.
		// Otherwise number creation is much more expensive than testing the operations themselves.
		int NCOUNT = 300000;
		
		// set up test numbers
		long[] a_arr = new long[NCOUNT];
		Uint128[] a128_arr =  new Uint128[NCOUNT];
		
		for (int i=0; i<NCOUNT; i++) {
			a_arr[i] = RNG.nextLong();
			a128_arr[i] = new Uint128(a_arr[i], RNG.nextLong());
		}
		
		// test performance of add implementations
		long t0 = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			for (int j=0; j<NCOUNT; j++) {
				a128_arr[i].add_v1(a128_arr[j]);
			}
		}
		long t1 = System.currentTimeMillis();
		LOG.info("add_v1 took " + (t1-t0) + "ms");

		t0 = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			for (int j=0; j<NCOUNT; j++) {
				a128_arr[i].add/*_v2*/(a128_arr[j]);
			}
		}
		t1 = System.currentTimeMillis();
		LOG.info("add_v2 took " + (t1-t0) + "ms");
		
		// test performance of mul64 implementations
		t0 = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			for (int j=0; j<NCOUNT; j++) {
				Uint128.mul64_v1(a_arr[i], a_arr[j]);
			}
		}
		t1 = System.currentTimeMillis();
		LOG.info("mul64_v1 took " + (t1-t0) + "ms");
		
		t0 = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			for (int j=0; j<NCOUNT; j++) {
				Uint128.mul64/*_v2*/(a_arr[i], a_arr[j]);
			}
		}
		t1 = System.currentTimeMillis();
		LOG.info("mul64_v2 took " + (t1-t0) + "ms");
		
		t0 = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			for (int j=0; j<NCOUNT; j++) {
				Uint128.mul64_v3(a_arr[i], a_arr[j]);
			}
		}
		t1 = System.currentTimeMillis();
		LOG.info("mul64_v3 took " + (t1-t0) + "ms");

		t0 = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			for (int j=0; j<NCOUNT; j++) {
				Uint128.mul64_MH(a_arr[i], a_arr[j]);
			}
		}
		t1 = System.currentTimeMillis();
		LOG.info("mul64_MH took " + (t1-t0) + "ms");
		
		t0 = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			for (int j=0; j<NCOUNT; j++) {
				Uint128.spMul64_MH(a_arr[i], a_arr[j]);
			}
		}
		t1 = System.currentTimeMillis();
		LOG.info("spMul64_MH took " + (t1-t0) + "ms");
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
