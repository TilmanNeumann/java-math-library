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
package de.tilman_neumann.jml.base;

import static de.tilman_neumann.jml.base.BigIntConstants.*;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;

public class UnsignedBigIntTest {
	private static final Logger LOG = LogManager.getLogger(UnsignedBigIntTest.class);
	
	private static final SecureRandom RNG = new SecureRandom();
	
	private static final int BITS_MIN = 100;
	private static final int BITS_INCREMENT = 100;
	private static final int BITS_MAX = 1000;

	private static final int NCOUNT = 100000; // test numbers per test set

	private static Map<Integer, ArrayList<BigInteger>> testSets;
	
	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		createTestSets();
	}
	
   	/**
   	 * create test set for performance test: random ints with random bit length < 1000
   	 * @param nCount
   	 * @return
   	 */
	private static void createTestSets() {
		testSets = new HashMap<Integer, ArrayList<BigInteger>>();
		for (int bits = BITS_MIN; bits<=BITS_MAX; bits+=BITS_INCREMENT) {
		   	ArrayList<BigInteger> testSet = new ArrayList<BigInteger>();
		   	for (int j=0; j<NCOUNT;) {
		   		BigInteger testNum = new BigInteger(bits, RNG);
		   		if (testNum.equals(I_0)) continue;
		   		testSet.add(testNum);
		   		j++;
		   	}
		   	testSets.put(Integer.valueOf(bits), testSet);
		}
	}
	
	@Test
	public void testConversion() {
		for (int bits = BITS_MIN; bits<=BITS_MAX; bits+=BITS_INCREMENT) {
			ArrayList<BigInteger> testSet = testSets.get(Integer.valueOf(bits));
		   	for (BigInteger testNum : testSet) {
		   		UnsignedBigInt N = new UnsignedBigInt(testNum);
				BigInteger reverse = N.toBigInteger();
				if (!testNum.equals(reverse)) {
		   			LOG.error("ERROR: conversion of " + testNum + " to UnsignedBigInt and back gave " + reverse);
				}
				Assert.assertEquals(testNum, reverse);
		   	}
		}
	}

	@Test
	public void testDivision() {
		UnsignedBigInt quotient = new UnsignedBigInt(new int[32]); // buffer big enough for 1000 bits
		
		for (int bits = BITS_MIN; bits<=BITS_MAX; bits+=BITS_INCREMENT) {
			ArrayList<BigInteger> testSet = testSets.get(Integer.valueOf(bits));
		   	for (BigInteger testNum : testSet) {
		   		int divisor = Math.max(2, RNG.nextInt(Integer.MAX_VALUE-2));
		   		BigInteger[] referenceResult = testNum.divideAndRemainder(BigInteger.valueOf(divisor));
		   		int remainder = new UnsignedBigInt(testNum).divideAndRemainder(divisor, quotient);
		   		if (!quotient.toBigInteger().equals(referenceResult[0])) {
		   			LOG.error("ERROR: divide(" + testNum + ", " + divisor + "): correct quotient = " + referenceResult[0] + ", UnsignedBigInt result = " + quotient);
		   		}
		   		if (remainder != referenceResult[1].intValue()) {
		   			LOG.error("ERROR: divide(" + testNum + ", " + divisor + "): correct remainder = " + referenceResult[1] + ", UnsignedBigInt result = " + remainder);
		   		}
				Assert.assertEquals(referenceResult[0], quotient.toBigInteger());
				Assert.assertEquals(referenceResult[1].intValue(), remainder);
		   	}
		}
	}

	@Test
	public void testModulus() {
		for (int bits = BITS_MIN; bits<=BITS_MAX; bits+=BITS_INCREMENT) {
			ArrayList<BigInteger> testSet = testSets.get(Integer.valueOf(bits));
		   	for (BigInteger testNum : testSet) {
		   		int divisor = Math.max(2, RNG.nextInt(Integer.MAX_VALUE-2));
		   		int correctRemainder = testNum.mod(BigInteger.valueOf(divisor)).intValue();
		   		int remainder = new UnsignedBigInt(testNum).mod(divisor);
		   		if (remainder != correctRemainder) {
		   			LOG.error("ERROR: mod(" + testNum + ", " + divisor + "): correct remainder = " + correctRemainder + ", UnsignedBigInt result = " + remainder);
		   		}
				Assert.assertEquals(correctRemainder, remainder);
		   	}
		}
	}
}
