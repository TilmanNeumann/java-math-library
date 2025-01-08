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
package de.tilman_neumann.jml.roots;

import java.math.BigInteger;
import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

import static org.junit.Assert.assertEquals;

/**
 * Test i.th roots of integers.
 * 
 * @author Tilman Neumann
 */
public class RootsTest {
	private static final Logger LOG = LogManager.getLogger(RootsTest.class);
	private static final Random RNG = new Random();

	private static final int NCOUNT = 10000;
	private static final int MIN_BITS = 100;
	private static final int MAX_BITS = 1000;
	private static final int INCR_BITS = 100;
	
	private BigInteger[][] testSets = new BigInteger[MAX_BITS + 1][];
	
	private int[] roots = new int[NCOUNT];
	
	@Before
	public void setup() {
		ConfigUtil.initProject();
		// create test sets
		for (int bits = MIN_BITS; bits<=MAX_BITS; bits+=INCR_BITS) {
			testSets[bits] = createTestSet(NCOUNT, bits);
		}
		// create roots
		for (int i=0; i<NCOUNT; i++) {
			roots[i] = 2 + RNG.nextInt(48);
		}
	}

   	/**
   	 * create test set for performance test: random ints with random bit length < 1000
   	 * @param nCount
   	 * @return
   	 */
	private static BigInteger[] createTestSet(int nCount, int bits) {
		BigInteger[] testSet = new BigInteger[nCount];
	   	for (int i=0; i<nCount;) {
	   		BigInteger testNum = new BigInteger(bits, RNG);
	   		if (testNum.bitLength()<bits) continue; // not exact size, skip
	   		testSet[i] = testNum;
	   		i++;
	   	}
	   	return testSet;
	}
	
	@Test
	public void testHeron1() {
		for (int bits = MIN_BITS; bits<=MAX_BITS; bits+=INCR_BITS) {
			BigInteger[] testSet = testSets[bits];
			for (int i=0; i<NCOUNT; i++) {
		   		BigInteger testNum = testSet[i];
		   		int root = roots[i];
		   		BigInteger[] linResult = Roots.ithRoot_bitwise(testNum, root);
		   		BigInteger[] heronResult = Roots.ithRoot_Heron1(testNum, root);
		   		if (!linResult[0].equals(heronResult[0])) {
		   			LOG.error("ERROR: Heron1: lower bound of " + root + ".th root(" + testNum + "): linear algorithm -> " + linResult[0] + ", Heron1 -> " + heronResult[0]);
		   		}
		   		assertEquals(linResult[0], heronResult[0]);
		   		if (!linResult[1].equals(heronResult[1])) {
		   			LOG.error("ERROR: Heron1: upper bound of " + root + ".th root(" + testNum + "): linear algorithm -> " + linResult[1] + ", Heron1 -> " + heronResult[1]);
		   		}
		   		assertEquals(linResult[1], heronResult[1]);
		   	}
		}
	}
	
	@Test
	public void testHeron2() {
		for (int bits = MIN_BITS; bits<=MAX_BITS; bits+=INCR_BITS) {
			BigInteger[] testSet = testSets[bits];
			for (int i=0; i<NCOUNT; i++) {
		   		BigInteger testNum = testSet[i];
		   		int root = roots[i];
		   		BigInteger[] linResult = Roots.ithRoot_bitwise(testNum, root);
		   		BigInteger[] heronResult = Roots.ithRoot_Heron2(testNum, root);
		   		if (!linResult[0].equals(heronResult[0])) {
		   			LOG.error("ERROR: Heron2: lower bound of " + root + ".th root(" + testNum + "): linear algorithm -> " + linResult[0] + ", Heron2 -> " + heronResult[0]);
		   		}
		   		assertEquals(linResult[0], heronResult[0]);
		   		if (!linResult[1].equals(heronResult[1])) {
		   			LOG.error("ERROR: Heron2: upper bound of " + root + ".th root(" + testNum + "): linear algorithm -> " + linResult[1] + ", Heron2 -> " + heronResult[1]);
		   		}
		   		assertEquals(linResult[1], heronResult[1]);
		   	}
		}
	}
}
