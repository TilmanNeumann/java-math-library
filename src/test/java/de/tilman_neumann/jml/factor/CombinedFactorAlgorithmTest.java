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
package de.tilman_neumann.jml.factor;

import java.math.BigInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;

import static org.junit.Assert.assertEquals;

public class CombinedFactorAlgorithmTest {
	private static final Logger LOG = LogManager.getLogger(CombinedFactorAlgorithmTest.class);

	private boolean RUN_SLOW_TESTS_TOO = false;
	
	private static CombinedFactorAlgorithm factorizer;

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		int numThreads = 2; // be cautious regarding what github CI supports
		factorizer = new CombinedFactorAlgorithm(numThreads, null, true);
	}

	@Test
	public void testSomeNumbers() {
		assertFactorizationSuccess("15841065490425479923", "2604221509 * 6082841047"); // 64 bit
		assertFactorizationSuccess("11111111111111111111111111", "11 * 53 * 79 * 859 * 265371653 * 1058313049"); // 84 bit
		assertFactorizationSuccess("5679148659138759837165981543", "3^3 * 466932157 * 450469808245315337"); // 93 bit
		assertFactorizationSuccess("874186654300294020965320730991996026550891341278308", "2^2 * 3 * 471997 * 654743 * 2855761 * 79833227 * 982552477 * 1052328969055591"); // 170 bit
		assertFactorizationSuccess("11111111111111111111111111155555555555111111111111111", "67 * 157 * 1056289676880987842105819104055096069503860738769"); // 173 bit
		assertFactorizationSuccess("1388091470446411094380555803943760956023126025054082930201628998364642", "2 * 3 * 1907 * 1948073 * 1239974331653 * 50222487570895420624194712095309533522213376829"); // 230 bit
		if (RUN_SLOW_TESTS_TOO) {
			// 236 bit, takes ~ 8 seconds with 2 threads
			assertFactorizationSuccess("99999999999999999999999999999999999999999999999999999999999999999999999", "3^2 * 241573142393627673576957439049 * 45994811347886846310221728895223034301839");
			 // 263 bit bit, takes ~ 52 seconds with 2 threads
			assertFactorizationSuccess("10000000000000000000000000000000000000000000000000000000000000000000000000005923", "1333322076518899001350381760807974795003 * 7500063320115780212377802894180923803641");
			 // 280 bit bit, takes ~ 158 seconds with 2 threads
			assertFactorizationSuccess("1794577685365897117833870712928656282041295031283603412289229185967719140138841093599", "42181796536350966453737572957846241893933 * 42543889372264778301966140913837516662044603");
		}
		
		// this one only needs only trial division, very fast...
		assertFactorizationSuccess("2900608971182010301486951469292513060638582965350239259380273225053930627446289431038392125", "3^11 * 5^3 * 7^6 * 11^2 * 13^2 * 17^2 * 19 * 37 * 41 * 53 * 59 * 61 * 73 * 113 * 151 * 227^2 * 271 * 337 * 433 * 457 * 547 * 953 * 11113 * 11117 * 11119 * 33343 * 33347 * 33349 * 33353 * 33359"); // 301 bit
		// = 33333 * 33335 * 33337 * 33339 * 33341 * 33343 * 33345 * 33347 * 33349 * 33351 * 33353 * 33355 * 33357 * 33359 * 33361 * 33363 * 33367 * 33369 * 33369 * 33371
	}
	
	private void assertFactorizationSuccess(String oddNStr, String expectedPrimeFactorizationStr) {
		long t0, t1;
		t0 = System.currentTimeMillis();
		BigInteger N = new BigInteger(oddNStr);
		SortedMultiset<BigInteger> factors = factorizer.factor(N);
		assertEquals(expectedPrimeFactorizationStr, factors.toString("*", "^"));
		t1 = System.currentTimeMillis();
		LOG.info("Factoring " + oddNStr + " took " + (t1-t0) + "ms");
	}
}
