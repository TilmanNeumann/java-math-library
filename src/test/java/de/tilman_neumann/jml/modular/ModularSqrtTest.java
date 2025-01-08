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
package de.tilman_neumann.jml.modular;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.primes.probable.BPSWTest;
import de.tilman_neumann.util.ConfigUtil;

public class ModularSqrtTest {
	private static final Logger LOG = LogManager.getLogger(ModularSqrtTest.class);
	
	private static final int NCOUNT = 100000;
	
	private static final Random RNG = new Random();
	private static final BPSWTest bpsw = new BPSWTest();
	private static final JacobiSymbol jacobiEngine = new JacobiSymbol();
	private static final ModularSqrt31 mse31 = new ModularSqrt31();
	
	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}

	@Test
	public void testCase5Mod8() {
		LOG.info("Test correctness of " + NCOUNT + " p with p%8==5");
		int[] pArray = createPArray(5, NCOUNT);
		int[] nArray = createNArray(pArray);
		for (int i=0; i<NCOUNT; i++) {
			int a = nArray[i];
			int p = pArray[i];
			int tonelli = mse31.Tonelli_Shanks(a, p);
			assertEquals((tonelli * (long)tonelli) % p, a%p);
			
			int case5Mod8 = mse31.case5Mod8(a, p);
			assertEquals((case5Mod8 * (long)case5Mod8) % p, a%p);
			assertEquals(tonelli, case5Mod8); // both returned the smaller sqrt
		}
	}

	private static int[] createPArray(int wantedPMod8, int count) {
		int[] pArray = new int[count];
		int i = 0;
		while (i<count) {
			// get non-negative random n
			int n = RNG.nextInt(Integer.MAX_VALUE);
			// add n to the test set if it is an odd prime with the wanted modulus mod 8
			if (n>2 && (n&7) == wantedPMod8 && bpsw.isProbablePrime(n)) {
				pArray[i] = n;
				i++;
			}
		}
		return pArray;
	}

	/**
	 * Create positive n having Jacobi(n|p) == 1 for all p in pArray.
	 * @param pList
	 * @return
	 */
	private static int[] createNArray(int[] pList) {
		int count = pList.length;
		int[] nArray = new int[count];
		int i = 0;
		while (i<count) {
			// get non-negative random n
			int n = RNG.nextInt(Integer.MAX_VALUE);
			// add n if it has Jacobi(n|p) = 1
			int p = pList[i];
			if (jacobiEngine.jacobiSymbol(n, p) == 1) {
				nArray[i] = n;
				i++;
			}
		}
		return nArray;
	}
}
