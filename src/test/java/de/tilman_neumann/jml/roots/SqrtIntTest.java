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

import static de.tilman_neumann.jml.base.BigIntConstants.*;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Random;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

/**
 * Test sqrt() computation with integer solutions.
 * 
 * @author Tilman Neumann
 */
public class SqrtIntTest {
	private static final Logger LOG = LogManager.getLogger(SqrtIntTest.class);
	private static final Random RNG = new Random();
	private static final int NCOUNT = 10000;
	
   	/**
   	 * create test set for performance test: random ints with random bit length < 1000
   	 * @param nCount
   	 * @return
   	 */
	private static ArrayList<BigInteger> createTestSet(int nCount, int bits) {
	   	ArrayList<BigInteger> testSet = new ArrayList<BigInteger>();
	   	for (int i=0; i<nCount;) {
	   		BigInteger testNum = new BigInteger(bits, RNG);
	   		if (testNum.bitLength()<bits) continue; // not exact size, skip
	   		testSet.add(testNum);
	   		i++;
	   	}
	   	return testSet;
	}
	
	@Test
	public void testCorrectness() {
		for (int bits = 10; bits<=1000; bits+=10) {
			LOG.info("test correctness of sqrt() implementations for " + bits + "-bit numbers...");
			ArrayList<BigInteger> testSet = createTestSet(NCOUNT, bits);
		   	for (BigInteger testNum : testSet) {
		   		testCorrectness(testNum, bits, SqrtInt.iSqrt/*_v01*/(testNum), "v01");
		   	}
		}
	}

	private static void testCorrectness(BigInteger testNum, int bits, BigInteger[] result, String algStr) {
   		BigInteger lower = result[0];
   		BigInteger lowerSquare = lower.multiply(lower);
   		if (lowerSquare.compareTo(testNum) > 0) LOG.error(algStr + ": ERROR at " + bits + " bits: lower bound of sqrt(" + testNum + ") = " + lower + " is too big");
   		BigInteger upper = result[1];
   		BigInteger upperSquare = upper.multiply(upper);
   		if (upperSquare.compareTo(testNum) < 0) LOG.error(algStr + ": ERROR at " + bits + " bits: upper bound of sqrt(" + testNum + ") = " + upper + " is too small");
   		
   		if (lowerSquare.equals(testNum) || upperSquare.equals(testNum)) {
   			if (!lower.equals(upper)) LOG.error(algStr + ": ERROR at " + bits + " bits: sqrt(" + testNum + ") is exact, but the computed bounds = [" + lower + ", " + upper + "] are different!");
   			assertEquals(lower, upper);
   		} else {
   			if (upper.subtract(lower).compareTo(I_1)>0) LOG.error(algStr + ": ERROR at " + bits + " bits: lower and upper bound of sqrt(" + testNum + ") = [" + lower + ", " + upper + "] differ by more than 1");
   			assertEquals(upper.subtract(lower), I_1);
   		}
	}
}
