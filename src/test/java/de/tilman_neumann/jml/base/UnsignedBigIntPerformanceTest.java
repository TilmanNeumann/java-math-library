/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2020-2025 Tilman Neumann - tilman.neumann@web.de
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
package de.tilman_neumann.jml.base;

import static de.tilman_neumann.jml.base.BigIntConstants.I_0;

import java.math.BigInteger;
import java.util.Random;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

public class UnsignedBigIntPerformanceTest {
	private static final Logger LOG = LogManager.getLogger(UnsignedBigIntPerformanceTest.class);
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
	   		if (testNum.equals(I_0)) continue;
	   		testSet.add(testNum);
	   		i++;
	   	}
	   	return testSet;
	}

	private static void testPerformance(int nCount) {
		// test division performance
		UnsignedBigInt quotient = new UnsignedBigInt(new int[32]); // buffer big enough for 1000 bits
		int[] divisors = new int[1000];
		BigInteger[] divisors_big = new BigInteger[1000];
		for (int i=0; i<1000; i++) {
	   		divisors[i] = Math.max(2, RNG.nextInt(Integer.MAX_VALUE-2));
	   		divisors_big[i] = BigInteger.valueOf(divisors[i]);
		}
		for (int bits = 100; bits<=1000; bits+=100) {
			ArrayList<BigInteger> testSet = createTestSet(nCount, bits);
			ArrayList<UnsignedBigInt> testSet_UBI = new ArrayList<UnsignedBigInt>();
			for (BigInteger testNum : testSet) {
				testSet_UBI.add(new UnsignedBigInt(testNum));
			}
			
			long t0, t1;
			LOG.info("Test division performance of " + nCount + " " + bits + "-bit numbers:");
		   	t0 = System.currentTimeMillis();
			for (BigInteger divisor_big : divisors_big) {
			   	for (BigInteger testNum : testSet) {
			   		@SuppressWarnings("unused")
					BigInteger result = testNum.mod(divisor_big);
			   	}
			}
		   	t1 = System.currentTimeMillis();
			LOG.info("   Java's mod() took " + (t1-t0) + " ms");
			
		   	t0 = System.currentTimeMillis();
			for (int divisor : divisors) {
			   	for (UnsignedBigInt testNum : testSet_UBI) {
			   		testNum.mod(divisor);
			   	}
			}
		   	t1 = System.currentTimeMillis();
			LOG.info("   UnsignedBigInt's mod() took " + (t1-t0) + " ms");
			
		   	t0 = System.currentTimeMillis();
			for (BigInteger divisor_big : divisors_big) {
			   	for (BigInteger testNum : testSet) {
			   		@SuppressWarnings("unused")
					BigInteger[] result = testNum.divideAndRemainder(divisor_big);
			   	}
			}
		   	t1 = System.currentTimeMillis();
			LOG.info("   Java's divide() took " + (t1-t0) + " ms");
			
			// divide(): slightly better than Java
		   	t0 = System.currentTimeMillis();
			for (int divisor : divisors) {
			   	for (UnsignedBigInt testNum : testSet_UBI) {
			   		testNum.divideAndRemainder(divisor, quotient);
			   	}
			}
		   	t1 = System.currentTimeMillis();
			LOG.info("   UnsignedBigInt's divide() took " + (t1-t0) + " ms");
		}
	}
	
	/**
	 * Test.
	 * @param args ignored
	 */
	public static void main(String[] args) {
	   	ConfigUtil.initProject();
	   	testPerformance(10000);
	}
}
