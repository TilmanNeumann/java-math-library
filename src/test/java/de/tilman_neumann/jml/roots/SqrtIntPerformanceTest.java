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
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Performance test for sqrt() computation with integer solutions.
 * 
 * @author Tilman Neumann
 */
public class SqrtIntPerformanceTest {
	private static final Logger LOG = LogManager.getLogger(SqrtIntPerformanceTest.class);
	private static final Random RNG = new Random();
	
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

	private static void testPerformance(int nCount) {
		for (int bits = 10; ; bits += 10) {
			ArrayList<BigInteger> testSet = createTestSet(nCount, bits);
			LOG.info("test sqrt of " + bits + "-bit numbers:");
			long t0, t1;
		   	t0 = System.currentTimeMillis();
		   	for (BigInteger testNum : testSet) {
		   		SqrtInt.iSqrt/*_v01*/(testNum);
		   	}
		   	t1 = System.currentTimeMillis();
			LOG.info("   v01 sqrt with " + nCount + " numbers took " + (t1-t0) + " ms");
		}
	}

	/**
	 * Test.
	 * @param args ignored
	 */
	public static void main(String[] args) {
	   	ConfigUtil.initProject();
	   	testPerformance(1000000);
	}
}
