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
package de.tilman_neumann.jml.factor.pollardRho;

import java.math.BigInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;

import static org.junit.Assert.assertEquals;

public class PollardRhoBrentTest {
	private static final Logger LOG = LogManager.getLogger(PollardRhoBrentTest.class);

	private static PollardRhoBrent pollardRho;

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		pollardRho = new PollardRhoBrent();
	}

	@Test
	public void testSomeInputs() {
		assertFactorizationSuccess("9223372036854775807", "7^2 * 73 * 127 * 337 * 92737 * 649657"); // Long.MAX_VALUE = 2^63-1 
		assertFactorizationSuccess("18446744073709551617",  "274177 * 67280421310721"); // F6
		assertFactorizationSuccess("5679148659138759837165981543", "3^3 * 466932157 * 450469808245315337");
		assertFactorizationSuccess("8225267468394993133669189614204532935183709603155231863020477010700542265332938919716662623",
				"1234567891 * 1234567907 * 1234567913 * 1234567927 * 1234567949 * 1234567967 * 1234567981 * 1234568021 * 1234568029 * 1234568047");
	}
	
	private void assertFactorizationSuccess(String oddNStr, String expectedPrimeFactorizationStr) {
		long t0, t1;
		t0 = System.currentTimeMillis();
		BigInteger N = new BigInteger(oddNStr);
		SortedMultiset<BigInteger> factorResult = pollardRho.factor(N);
		assertEquals(expectedPrimeFactorizationStr, factorResult.toString("*", "^"));
		t1 = System.currentTimeMillis();
		LOG.info("Factoring " + oddNStr + " took " + (t1-t0) + "ms");
	}
}
