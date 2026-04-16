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

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Tests correctness of prime tests for N<2^31.
 * 
 * Comparing the results of (established reference algorithm) BARRETT_TEST.isPrime_v1(n) 
 * with (currently fastest) LEMIRE_INT_TEST.isPrimeUnrolled(n)
 * for all odd N<2^31 takes less than 10 minutes and shows no errors.
 */
public class TDivPrimeTestTest {
	private static final Logger LOG = LogManager.getLogger(TDivPrimeTestTest.class);

	private static final BarrettPrimeTest BARRETT_TEST = BarrettPrimeTest.getInstance();
	private static final LemirePrimeTest LEMIRE_TEST = LemirePrimeTest.getInstance();
	private static final LemireIntPrimeTest LEMIRE_INT_TEST = LemireIntPrimeTest.getInstance();
	
	@Before
	public void setup() {
		ConfigUtil.initProject();
	}

	@Test()
	public void testTDivPrimeTest() {
		// test some even numbers
		test(2);
		test(256);
		test(2565572);
		
		// test many odd numbers
		for (int n=3; n <= 10000000 /* Integer.MAX_VALUE */; n+=2) {
			test(n);
		}
		
		// test end of int range
		for (int i=100; i>=0; i--) {
			test(Integer.MAX_VALUE - i);
		}
	}
	
	private void test(int n) {
		boolean barrettResult = BARRETT_TEST.isPrime_v1(n);
		boolean barrettResult_v2 = BARRETT_TEST.isPrime_v2(n);
		boolean barrettResult_unrolled = BARRETT_TEST.isPrimeUnrolled(n);
		boolean lemireResult = LEMIRE_TEST.isPrime_v1(n);
		boolean lemireResult_v2 = LEMIRE_TEST.isPrime_v2(n);
		boolean lemireResult_unrolled = LEMIRE_TEST.isPrime/*Unrolled*/(n);
		boolean lemireIntResult = LEMIRE_INT_TEST.isPrime_v1(n);
		boolean lemireIntResult_v2 = LEMIRE_INT_TEST.isPrime_v2(n);
		boolean lemireIntResult_unrolled = LEMIRE_INT_TEST.isPrime/*Unrolled*/(n);
		if (barrettResult != barrettResult_v2) {
			LOG.error("n=" + n + ": barrettResult = " + barrettResult + ", barrettResult_v2 = " + barrettResult_v2);
		}
		if (barrettResult != barrettResult_unrolled) {
			LOG.error("n=" + n + ": barrettResult = " + barrettResult + ", barrettResult_unrolled = " + barrettResult_unrolled);
		}
		if (barrettResult != lemireResult) {
			LOG.error("n=" + n + ": barrettResult = " + barrettResult + ", lemireResult = " + lemireResult);
		}
		if (barrettResult != lemireResult_v2) {
			LOG.error("n=" + n + ": barrettResult = " + barrettResult + ", lemireResult_v2 = " + lemireResult_v2);
		}
		if (barrettResult != lemireResult_unrolled) {
			LOG.error("n=" + n + ": barrettResult = " + barrettResult + ", lemireResult_unrolled = " + lemireResult_unrolled);
		}
		if (barrettResult != lemireIntResult) {
			LOG.error("n=" + n + ": barrettResult = " + barrettResult + ", lemireIntResult = " + lemireIntResult);
		}
		if (barrettResult != lemireIntResult_v2) {
			LOG.error("n=" + n + ": barrettResult = " + barrettResult + ", lemireIntResult_v2 = " + lemireIntResult_v2);
		}
		if (barrettResult != lemireIntResult_unrolled) {
			LOG.error("n=" + n + ": barrettResult = " + barrettResult + ", lemireIntResult_unrolled = " + lemireIntResult_unrolled);
		}
		assertEquals(barrettResult, barrettResult_v2);
		assertEquals(barrettResult, barrettResult_unrolled);
		assertEquals(barrettResult, lemireResult);
		assertEquals(barrettResult, lemireResult_v2);
		assertEquals(barrettResult, lemireResult_unrolled);
		assertEquals(barrettResult, lemireIntResult);
		assertEquals(barrettResult, lemireIntResult_v2);
		assertEquals(barrettResult, lemireIntResult_unrolled);
	}
}
