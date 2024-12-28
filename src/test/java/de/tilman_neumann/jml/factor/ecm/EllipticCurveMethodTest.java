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

import de.tilman_neumann.jml.factor.base.FactorArguments;
import de.tilman_neumann.jml.factor.base.FactorResult;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset_BottomUp;

import static org.junit.Assert.assertEquals;

public class EllipticCurveMethodTest {
	private static final Logger LOG = LogManager.getLogger(EllipticCurveMethodTest.class);

	private static EllipticCurveMethod ecm;

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		ecm = new EllipticCurveMethod(-1);
	}

	@Test
	public void testSomeInputs() {
		testSingleNumber("8225267468394993133669189614204532935183709603155231863020477010700542265332938919716662623", 
				"1234567891 * 1234567907 * 1234567913 * 1234567927 * 1234567949 * 1234567967 * 1234567981 * 1234568021 * 1234568029 * 1234568047");
		testSingleNumber("101546450935661953908994991437690198927080333663460351836152986526126114727314353555755712261904130976988029406423152881932996637460315302992884162068350429", 
				"123456789012419 * 123456789012421 * 123456789012437 * 123456789012439 * 123456789012463 * 123456789012521 * 123456789012523 * 123456789012533 * 123456789012577 * 123456789012629 * 123456789012637");
		testSingleNumber("1593332576170570774181606244493046197050984933692181475920784855223341",
				"17 * 1210508704285703 * 2568160569265616473 * 30148619026320753545829271787156467");
		testSingleNumber("856483652537814883803418179972154563054077", 
				"42665052615296697659 * 20074595014814065252903");
		// very hard for ECM, better suited for SIQS
		//testSingleNumber("1794577685365897117833870712928656282041295031283603412289229185967719140138841093599",
		//		"42181796536350966453737572957846241893933 * 42543889372264778301966140913837516662044603");
	}
	
	private void testSingleNumber(String oddNStr, String expectedPrimeFactorizationStr) {
		long t0, t1;
		t0 = System.currentTimeMillis();
		BigInteger N = new BigInteger(oddNStr);
		FactorArguments factorArgs = new FactorArguments(N, 1);
		FactorResult factorResult = new FactorResult(new SortedMultiset_BottomUp<BigInteger>(), new SortedMultiset_BottomUp<BigInteger>(), new SortedMultiset_BottomUp<BigInteger>(), 3);
		ecm.searchFactors(factorArgs, factorResult);
		assertEquals(factorResult.untestedFactors.size(), 0);
		assertEquals(factorResult.compositeFactors.size(), 0);
		assertEquals(expectedPrimeFactorizationStr, factorResult.primeFactors.toString("*", "^"));
		t1 = System.currentTimeMillis();
		LOG.info("Factoring " + oddNStr + " took " + (t1-t0) + "ms");
	}
}
