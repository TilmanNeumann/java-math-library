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

import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.primes.probable.BPSWTest;
import de.tilman_neumann.util.ConfigUtil;

public class ModularSqrtPerformanceTest {
	private static final Logger LOG = LogManager.getLogger(ModularSqrtPerformanceTest.class);
	
	private static final int NCOUNT = 100000;

	private static final Random RNG = new Random();
	private static final BPSWTest bpsw = new BPSWTest();
	private static final JacobiSymbol jacobiEngine = new JacobiSymbol();
	
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
	
	private static void testPerformance() {
		long t;
		ModularSqrt31 mse31 = new ModularSqrt31();
		LOG.info("Test performance with " + NCOUNT + " test numbers:");
		
		// test Tonelli-Shanks for p==1 (mod 8)
		int[] pArray = createPArray(1, NCOUNT);
		int[] nArray = createNArray(pArray);
		t = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			mse31.Tonelli_Shanks(nArray[i], pArray[i]);
		}
		LOG.info("p==1 (mod 8): Tonelli-Shanks took " + (System.currentTimeMillis()-t) + "ms");
		
		// test Tonelli-Shanks for p==3 (mod 8)
		pArray = createPArray(3, NCOUNT);
		nArray = createNArray(pArray);
		t = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			try {
				mse31.Tonelli_Shanks(nArray[i], pArray[i]);
			} catch (Exception e) {
				LOG.error("Tonelli-Shanks failed for p%8==3: a = " + nArray[i] + ", p = " + pArray[i] + " -> " + e, e);
			}
		}
		LOG.info("p==3 (mod 8): Tonelli-Shanks took " + (System.currentTimeMillis()-t) + "ms");

		// test Lagrange for p==3 (mod 8)
		t = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			mse31.Lagrange(nArray[i], pArray[i]);
		}
		LOG.info("p==3 (mod 8): Lagrange took " + (System.currentTimeMillis()-t) + "ms");

		// test Tonelli-Shanks for p==5 (mod 8)
		pArray = createPArray(5, NCOUNT);
		nArray = createNArray(pArray);
		t = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			mse31.Tonelli_Shanks(nArray[i], pArray[i]);
		}
		LOG.info("p==5 (mod 8): Tonelli-Shanks took " + (System.currentTimeMillis()-t) + "ms");
		
		// test case5Mod8() for p==5 (mod 8)
		t = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			mse31.case5Mod8(nArray[i], pArray[i]);
		}
		LOG.info("p==5 (mod 8): case5Mod8 took " + (System.currentTimeMillis()-t) + "ms");
		
		pArray = createPArray(7, NCOUNT);
		nArray = createNArray(pArray);
		// test Lagrange for p==3 (mod 8)
		t = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			mse31.Lagrange(nArray[i], pArray[i]);
		}
		LOG.info("p==7 (mod 8): Lagrange took " + (System.currentTimeMillis()-t) + "ms");
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
