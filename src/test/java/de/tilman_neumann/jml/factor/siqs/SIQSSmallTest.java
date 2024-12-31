/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2024 Tilman Neumann - tilman.neumann@web.de
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
package de.tilman_neumann.jml.factor.siqs;

import java.math.BigInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.jml.factor.siqs.poly.SIQSPolyGenerator;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;

import static org.junit.Assert.assertEquals;

public class SIQSSmallTest {
	private static final Logger LOG = LogManager.getLogger(SIQSSmallTest.class);

	private static SIQSSmall qs;

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		qs = new SIQSSmall(0.32F, 0.37F, null, new SIQSPolyGenerator(), 10, true);
	}

	@Test
	// TODO On github CI this test may need up to 13 seconds. Locally the test finishes in less than 100ms. 
	// It is possible that the test numbers are not very appropriate for the algorithm, but why the difference between local and github tests?
	public void testSomeInputs() {
		assertFactorizationSuccess("15841065490425479923", "2604221509 * 6082841047"); // 64 bit
		assertFactorizationSuccess("11111111111111111111111111", "11 * 53 * 79 * 859 * 265371653 * 1058313049"); // 84 bit
		assertFactorizationSuccess("5679148659138759837165981543", "3^3 * 466932157 * 450469808245315337"); // 93 bit
		
		// This number is already too big, it causes hanging github CI builds quite often
		//assertFactorizationSuccess("11111111111111111111111111155555555555111111111111111", "67 * 157 * 1056289676880987842105819104055096069503860738769");
	}
	
	private void assertFactorizationSuccess(String oddNStr, String expectedPrimeFactorizationStr) {
		long t0, t1;
		t0 = System.currentTimeMillis();
		BigInteger N = new BigInteger(oddNStr);
		SortedMultiset<BigInteger> factors = qs.factor(N);
		assertEquals(expectedPrimeFactorizationStr, factors.toString("*", "^"));
		t1 = System.currentTimeMillis();
		LOG.info("Factoring " + oddNStr + " took " + (t1-t0) + "ms");
	}
}
