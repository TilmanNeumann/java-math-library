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
package de.tilman_neumann.jml.modular;

import java.math.BigInteger;
import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Test performance of modular power implementations.
 * 
 * My implementation with long modulus is 4 times slower than BigInteger :-(
 * But at least the version with int modulus is faster.
 */
public class ModularPowerPerformanceTest {
	private static final Logger LOG = LogManager.getLogger(ModularPowerPerformanceTest.class);
	
	private static final int NCOUNT = 100000;
	
	private static final Random RNG = new Random();
	private static final ModularPower modPow = new ModularPower();
	
	private static void testPerformance() {
		
		// set up test numbers
		
		long[] a_arr = new long[NCOUNT];
		long[] b_arr = new long[NCOUNT];
		long[] c_arr = new long[NCOUNT];
		int[] cInt_arr = new int[NCOUNT];
		BigInteger[] aBig_arr = new BigInteger[NCOUNT];
		BigInteger[] bBig_arr = new BigInteger[NCOUNT];
		BigInteger[] cBig_arr = new BigInteger[NCOUNT];
		BigInteger[] cIntBig_arr = new BigInteger[NCOUNT];

		for (int i=0; i<NCOUNT; i++) {
			a_arr[i] = getPositiveRandomLong();
			b_arr[i] = getPositiveRandomLong();
			c_arr[i] = getPositiveRandomLong();
			cInt_arr[i] = (int) (c_arr[i] & Integer.MAX_VALUE);
			
			aBig_arr[i] = BigInteger.valueOf(a_arr[i]);
			bBig_arr[i] = BigInteger.valueOf(b_arr[i]);
			cBig_arr[i] = BigInteger.valueOf(c_arr[i]);
			cIntBig_arr[i] = BigInteger.valueOf(cInt_arr[i]);
		}
		
		long t0, t1;

		// test BigInteger with long modulus
		t0 = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			aBig_arr[i].modPow(bBig_arr[i], cBig_arr[i]);
		}
		t1 = System.currentTimeMillis();
		LOG.info("BigInteger.modPow(long modulus) took " + (t1-t0) + " ms");
		
		// test my implementation with long modulus
		t0 = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			modPow.modPow(a_arr[i], b_arr[i], c_arr[i]);
		}
		t1 = System.currentTimeMillis();
		LOG.info("ModularPower.modPow(long modulus) took " + (t1-t0) + " ms");

		// test BigInteger with int modulus
		t0 = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			aBig_arr[i].modPow(bBig_arr[i], cIntBig_arr[i]);
		}
		t1 = System.currentTimeMillis();
		LOG.info("BigInteger.modPow(long modulus) took " + (t1-t0) + " ms");

		// test my implementation with int modulus
		t0 = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			modPow.modPow(a_arr[i], b_arr[i], cInt_arr[i]);
		}
		t1 = System.currentTimeMillis();
		LOG.info("ModularPower.modPow(int modulus) took " + (t1-t0) + " ms");
	}

	private static long getPositiveRandomLong() {
		long n = RNG.nextLong();
		while (n == 0) n = RNG.nextLong();
		return Math.abs(n);
	}
	
	public static void main(String[] args) {
		ConfigUtil.initProject();
		testPerformance();
	}
}
