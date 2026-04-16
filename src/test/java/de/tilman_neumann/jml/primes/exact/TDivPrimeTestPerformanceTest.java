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
package de.tilman_neumann.jml.primes.exact;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Compare performance of prime tests for N<2^31.
 * 
 * Currently fastest is LEMIRE_INT_TEST.isPrimeUnrolled(n).
 * 
 * But since there are just 105.097.565 primes < 2^31 (Integer.MAX_VALUE = 2^31 - 1 being the biggest)
 * and 203.280.221 primes < 2^32, maybe soon we will simply store them and look then up...
 */
public class TDivPrimeTestPerformanceTest {
	private static final Logger LOG = LogManager.getLogger(TDivPrimeTestPerformanceTest.class);

	private static final int NMAX = 100000000;
	
	private static final BarrettPrimeTest BARRETT_TEST = BarrettPrimeTest.getInstance();
	private static final LemirePrimeTest LEMIRE_TEST = LemirePrimeTest.getInstance();
	private static final LemireIntPrimeTest LEMIRE_INT_TEST = LemireIntPrimeTest.getInstance();
	private static final Random RNG = new Random();

	// here we need random test numbers, otherwise the compiler would over-optimize the tests
	private static final int[] TEST_NUMBERS = setupTestNumbers();
	
	private static int[] setupTestNumbers() {
		int[] result = new int[NMAX];
		int i=0;
		while (i < NMAX) {
			int n = RNG.nextInt();
			if ((n&1) == 0) n++;
			if (n == Integer.MIN_VALUE) continue;
			n = Math.abs(n);
			if (n < 3) continue;
			result[i++] = n;
		};
		return result;
	}
	
	private static void test() {
		long t0, t1;
		
		t0 = System.nanoTime();
		for (int n : TEST_NUMBERS) {
			BARRETT_TEST.isPrime_v1(n);
		}
		t1 = System.nanoTime();
		LOG.info("BARRETT_TEST.isPrime_v1 took " + (t1-t0) + " ns");
		
		t0 = System.nanoTime();
		for (int n : TEST_NUMBERS) {
			BARRETT_TEST.isPrime_v2(n);
		}
		t1 = System.nanoTime();
		LOG.info("BARRETT_TEST.isPrime_v2 took " + (t1-t0) + " ns");
		
		t0 = System.nanoTime();
		for (int n : TEST_NUMBERS) {
			BARRETT_TEST.isPrimeUnrolled(n);
		}
		t1 = System.nanoTime();
		LOG.info("BARRETT_TEST.isPrimeUnrolled took " + (t1-t0) + " ns");

		t0 = System.nanoTime();
		for (int n : TEST_NUMBERS) {
			LEMIRE_TEST.isPrime_v1(n);
		}
		t1 = System.nanoTime();
		LOG.info("LEMIRE_TEST.isPrime_v1 took " + (t1-t0) + " ns");
		
		t0 = System.nanoTime();
		for (int n : TEST_NUMBERS) {
			LEMIRE_TEST.isPrime_v2(n);
		}
		t1 = System.nanoTime();
		LOG.info("LEMIRE_TEST.isPrime_v2 took " + (t1-t0) + " ns");
		
		t0 = System.nanoTime();
		for (int n : TEST_NUMBERS) {
			LEMIRE_TEST.isPrime/*Unrolled*/(n);
		}
		t1 = System.nanoTime();
		LOG.info("LEMIRE_TEST.isPrimeUnrolled took " + (t1-t0) + " ns"); // champion

		t0 = System.nanoTime();
		for (int n : TEST_NUMBERS) {
			LEMIRE_INT_TEST.isPrime_v1(n);
		}
		t1 = System.nanoTime();
		LOG.info("LEMIRE_INT_TEST.isPrime_v1 took " + (t1-t0) + " ns");
		
		t0 = System.nanoTime();
		for (int n : TEST_NUMBERS) {
			LEMIRE_INT_TEST.isPrime_v2(n);
		}
		t1 = System.nanoTime();
		LOG.info("LEMIRE_INT_TEST.isPrime_v2 took " + (t1-t0) + " ns");
		
		t0 = System.nanoTime();
		for (int n : TEST_NUMBERS) {
			LEMIRE_INT_TEST.isPrime/*Unrolled*/(n);
		}
		t1 = System.nanoTime();
		LOG.info("LEMIRE_INT_TEST.isPrimeUnrolled took " + (t1-t0) + " ns"); // champion
	}
	
	public static void main(String[] args) {
		ConfigUtil.initProject();
		test();
	}
}
