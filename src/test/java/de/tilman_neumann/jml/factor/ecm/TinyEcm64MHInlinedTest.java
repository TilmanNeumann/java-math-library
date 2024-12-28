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
package de.tilman_neumann.jml.factor.ecm;

import java.math.BigInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;

import static org.junit.Assert.assertEquals;

public class TinyEcm64MHInlinedTest {
	private static final Logger LOG = LogManager.getLogger(TinyEcm64MHInlinedTest.class);

	private static TinyEcm64MHInlined tinyEcm;

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		tinyEcm = new TinyEcm64MHInlined();
	}

	@Test
	public void testSemiprimes() {
		testFullFactorization(1234577*12345701L, "1234577 * 12345701");
		// Failures before map[] fix
		testFullFactorization(1253586675305333L, "6030247 * 207883139");
		testFullFactorization(1139151196120601L, "232987 * 4889333723");
		testFullFactorization(1553712951089947L, "29184373 * 53237839");
		testFullFactorization(2235885271339597L, "38826707 * 57586271");
		testFullFactorization(1586929215386303L, "4605361 * 344583023");
		// Failures before spRand() fix
		testFullFactorization(930705057210221L, "25325941 * 36749081");
		testFullFactorization(1067332898136023L, "26165441 * 40791703");
		testFullFactorization(8311092540494299L, "67613999 * 122919701");
		testFullFactorization(23603982383629381L, "118493549 * 199200569");
		testFullFactorization(58725827789610857L, "103129121 * 569439817");
		testFullFactorization(369313815090910177L, "354877001 * 1040681177");
		// Failures before int to long cast fix
		testFullFactorization(41382606407163353L, "69696377 * 593755489");
		testFullFactorization(306358296309770459L, "209066843 * 1465360513");
		// Failures because #curves was too small
		testFullFactorization(474315852287951L, "15390191 * 30819361");
		testFullFactorization(9400170223537253L, "47701151 * 197063803");
		testFullFactorization(35239016917581299L, "82794071 * 425622469");
		testFullFactorization(37915240075398767L, "16632857 * 2279538631");
		testFullFactorization(459926431465210403L, "631315747 * 728520449");
		testFullFactorization(752882545886305349L, "460178669 * 1636065721");
		testFullFactorization(179503729521451L, "7420397 * 24190583");
		testFullFactorization(1059150637518581L, "22339673 * 47411197");
		testFullFactorization(3209190314462729L, "24571021 * 130608749");
		testFullFactorization(17586811742837503L, "105611027 * 166524389");
		testFullFactorization(13745855671622359L, "50960113 * 269737543");
		testFullFactorization(15727038894518533L, "55460939 * 283569647");
		testFullFactorization(66804960995707271L, "125296429 * 533175299");
		testFullFactorization(38704493646912997L, "31501721 * 1228646957");
		testFullFactorization(56025872236672099L, "53839061 * 1040617559");
		testFullFactorization(57675022504187287L, "172307477 * 334721531");
		testFullFactorization(69916262762899909L, "72133751 * 969258659");
		testFullFactorization(51113648728234999L, "49275467 * 1037304197");
		testFullFactorization(55878279398722441L, "131149259 * 426066299");
	}

	@Test
	public void testNumbersWithManyFactors() {
		testFullFactorization(1096954293075013905L, "3 * 5 * 7^2 * 169681 * 8795650783");
		testFullFactorization(1100087778366101931L, "3 * 7 * 43 * 89 * 199 * 263 * 307 * 881 * 967"); // Fibonacci(88)
	}

	private void testFullFactorization(long N, String expectedPrimeFactorizationStr) {
		SortedMultiset<BigInteger> factors = tinyEcm.factor(BigInteger.valueOf(N));
		LOG.info(N + " = " + factors.toString("*", "^"));
		assertEquals(expectedPrimeFactorizationStr, factors.toString("*", "^"));
	}
}
