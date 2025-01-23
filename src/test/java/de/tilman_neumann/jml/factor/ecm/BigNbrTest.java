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
package de.tilman_neumann.jml.factor.ecm;

import java.math.BigInteger;
import java.util.Random;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the BigNbr operations in Dario Alperns ECM code.<br><br>
 */
// TODO test multiplication, too
public class BigNbrTest {

	private static final Logger LOG = LogManager.getLogger(BigNbrTest.class);

	private static final boolean LOG_SPOILERS = false;
	
	/** number of test numbers per bit size for random tests */
	private static final int N_COUNT = 10000;
	/** maximum bit size for random tests */
	private static final int MAX_BITS = 1000;

	private static EllipticCurveMethod ecm;

	private static final Random RNG = new Random();

	private static int[] a31;
	private static int[] b31;
	private static int[] c31;
	
	private static long[] a32;
	private static long[] b32;
	private static long[] c32;

	private static BigInteger[][] testNumbersForBitSize = new BigInteger[MAX_BITS][];
	
	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		ecm = new EllipticCurveMethod(0);
		
		// BigNbrs
		a31 = new int[EllipticCurveMethod.NLen];
		b31 = new int[EllipticCurveMethod.NLen];
		c31 = new int[EllipticCurveMethod.NLen];
		a32 = new long[EllipticCurveMethod.NLen];
		b32 = new long[EllipticCurveMethod.NLen];
		c32 = new long[EllipticCurveMethod.NLen];
		
		// test numbers for random tests
		for (int bits = 10; bits<MAX_BITS; bits += 1) {
			if (LOG_SPOILERS) LOG.debug("Create " + N_COUNT + " N with " + bits + " bit...");
			BigInteger[] NArray = new BigInteger[N_COUNT];
			for (int i=0; i<N_COUNT; i++) {
				// create both positive and negative test numbers
				BigInteger N = new BigInteger(bits, RNG);
				NArray[i] = (i&1)==0 ? N : N.negate();
			}
			testNumbersForBitSize[bits] = NArray;
		}
	}
	
	/**
	 * These numbers used to fail in-out-conversion31.
	 * The N have in common that<br>
	 * a) they are negative, and<br>
	 * b) their actual bit size is ~20 bit smaller than the generation target was.<br>
	 */
	@Test
	public void testInOutConversion31ForSpecialNumbers() {
		testInOutConversion31(new BigInteger("-9047107"), 80); // NumberLength=3
		testInOutConversion31(new BigInteger("-9047107805356617"), 80); // NumberLength=3
		testInOutConversion31(new BigInteger("-9047107805356617574821419177374462823988751688943829812044231389676"), 248);
		testInOutConversion31(new BigInteger("-810645591595199570650496051345328669857441632357541306927499776869083777701428362211927875931927"), 341);
		testInOutConversion31(new BigInteger("-1661507549698651677347352443872649283631112234579374113959724008640719132113623333818063620959056420710153"), 371);
		testInOutConversion31(new BigInteger("-3879725014912056720725365884026092090047847467519283535255468693493067092968062030773638553790318583416720"), 371);
		testInOutConversion31(new BigInteger("-81238486828807671926156945485493053897286271079236717483584339941912993848458748988378027450406814055929"), 371);
	}
	
	@Test
	public void testInOutConversion31ForRandomNumbers() {
		for (int bits = 10; bits<MAX_BITS; bits += 1) {
			BigInteger[] NArray = testNumbersForBitSize[bits];
			if (LOG_SPOILERS) LOG.debug("Test in-out-conversion31 of N with " + bits + " bit...");
			for (int i=0; i<N_COUNT; i++) {
				testInOutConversion31(NArray[i], bits);
			}
		}
	}
	
	private void testInOutConversion31(BigInteger N, int bitLengthGenerationTarget) {
		BigInteger Nout = inOutConversion31(N, bitLengthGenerationTarget);
		assertEquals(N, Nout);
	}

	private BigInteger inOutConversion31(BigInteger N, int bitLengthGenerationTarget) {
		try {
			int NumberLength = EllipticCurveMethod.computeNumberLength(bitLengthGenerationTarget);
			ecm.NumberLength = NumberLength; 
			ecm.BigNbrToBigInt(N, a31, NumberLength);
			BigInteger N31 = ecm.BigIntToBigNbr(a31);
			if (!N.equals(N31)) {
				LOG.error("inOut31 failure:");
				LOG.debug("    NumberLength " + ecm.NumberLength);
				LOG.debug("    N   = " + N + " (" + N.bitLength() + " bit)");
				LOG.debug("    N31 = " + N31);
				LOG.debug("    N.toByteArray()   = " + Arrays.toString(N.toByteArray()));
				LOG.debug("    a31 = " +  Arrays.toString(a31));
				LOG.debug("    N31.toByteArray() = " + Arrays.toString(N31.toByteArray()));
			}
			return N31;
		} catch (Exception e) {
			LOG.error("Conversion of N=" + N + " caused " + e, e);
			return null;
		}
	}

	/**
	 * 32-bit conversion works for the same numbers...
	 */
	@Test
	public void testInOutConversion32ForSpecialNumbers() {
		testInOutConversion32(new BigInteger("-9047107805356617574821419177374462823988751688943829812044231389676"), 248);
		testInOutConversion32(new BigInteger("-810645591595199570650496051345328669857441632357541306927499776869083777701428362211927875931927"), 341);
		testInOutConversion32(new BigInteger("-1661507549698651677347352443872649283631112234579374113959724008640719132113623333818063620959056420710153"), 371);
		testInOutConversion32(new BigInteger("-3879725014912056720725365884026092090047847467519283535255468693493067092968062030773638553790318583416720"), 371);
		testInOutConversion32(new BigInteger("-81238486828807671926156945485493053897286271079236717483584339941912993848458748988378027450406814055929"), 371);
	}

	@Test
	public void testInOutConversion32ForRandomNumbers() {
		for (int bits = 10; bits<MAX_BITS; bits += 1) {
			BigInteger[] NArray = testNumbersForBitSize[bits];
			if (LOG_SPOILERS) LOG.debug("Test in-out-conversion32 of N with " + bits + " bit...");
			for (int i=0; i<N_COUNT; i++) {
				testInOutConversion32(NArray[i], bits);
			}
		}
	}
	
	private void testInOutConversion32(BigInteger N, int bitLengthGenerationTarget) {
		BigInteger Nout = inOutConversion32(N, bitLengthGenerationTarget);
		assertEquals(N, Nout);
	}
	
	private BigInteger inOutConversion32(BigInteger N, int bitLengthGenerationTarget) {
		try {
			int NumberLength = EllipticCurveMethod.computeNumberLength(bitLengthGenerationTarget);
			ecm.NumberLength = NumberLength; 
			ecm.BigNbrToBigInt(N, a32, NumberLength);
			BigInteger N32 = ecm.BigIntToBigNbr(a32);
			if (!N.equals(N32)) {
				LOG.error("inOut32 failure:");
				LOG.debug("    NumberLength " + ecm.NumberLength);
				LOG.debug("    N   = " + N + " (" + N.bitLength() + " bit)");
				LOG.debug("    N32 = " + N32);
				LOG.debug("    N.toByteArray()   = " + Arrays.toString(N.toByteArray()));
				LOG.debug("    a32 = " +  Arrays.toString(a32));
				LOG.debug("    N32.toByteArray() = " + Arrays.toString(N32.toByteArray()));
			}
			return N32;
		} catch (Exception e) {
			LOG.error("Conversion of N=" + N + " caused " + e, e);
			return null;
		}
	}

	@Test
	public void testLongConversionForRandomNumbers() {
		for (int bits = 10; bits<64; bits += 1) {
			int NumberLength = EllipticCurveMethod.computeNumberLength(bits);
			ecm.NumberLength = NumberLength; 
			BigInteger[] NArray = testNumbersForBitSize[bits];
			if (LOG_SPOILERS) LOG.debug("Test long conversion of N with " + bits + " bit...");
			for (int i=0; i<N_COUNT; i++) {
				BigInteger N = NArray[i];
				ecm.BigNbrToBigInt(N, a31, NumberLength);
				String a31Str = ecm.BigNbrToString(a31);
				ecm.LongToBigNbr(N.longValue(), b31);
				String b31Str = ecm.BigNbrToString(b31);
				if (!a31Str.equals(b31Str)) {
					ecm.SubtractBigNbr(a31, b31, c31);
					String diffStr = ecm.BigNbrToString(c31);
					LOG.error("long conversion failure: a31Str=" + a31Str + ", b31Str = " + b31Str + ", diff = " + diffStr + ", NumberLength = " + ecm.NumberLength);
				}
				assertEquals(a31Str, b31Str);
			}
		}
	}
	
	@Test
	public void testAdd31ForRandomNumbers() {
		for (int bits = 10; bits<MAX_BITS; bits += 1) {
			int NumberLength = EllipticCurveMethod.computeNumberLength(bits);
			ecm.NumberLength = NumberLength; 
			BigInteger[] NArray = testNumbersForBitSize[bits];
			if (LOG_SPOILERS) LOG.debug("Test add31 of N with " + bits + " bit...");
			int imax = (int) Math.sqrt(N_COUNT);
			int jmin = N_COUNT - (int) Math.sqrt(N_COUNT);
			for (int i=0; i<imax; i++) {
				BigInteger a = NArray[i];
				ecm.BigNbrToBigInt(a, a31, NumberLength);
				for (int j=jmin; j<N_COUNT; j++) {
					BigInteger b = NArray[j];
					ecm.BigNbrToBigInt(b, b31, NumberLength);
					BigInteger correctResult = a.add(b);
					ecm.AddBigNbr(a31, b31, c31);
					BigInteger testResult = ecm.BigIntToBigNbr(c31);
					if (!correctResult.equals(testResult)) {
						ecm.Convert31To32Bits(c31, c32);
						BigInteger testResult32 = ecm.BigIntToBigNbr(c32);
						LOG.error("add31 failure: " + a + " + " + b + ": correct = " + correctResult + ", add31 = " + testResult + ", testResult32 = " + testResult32);
					}
					assertEquals(correctResult, testResult);
				}
			}
		}
	}
	
	@Test
	public void testAdd32ForRandomNumbers() {
		for (int bits = 10; bits<MAX_BITS; bits += 1) {
			int NumberLength = EllipticCurveMethod.computeNumberLength(bits);
			ecm.NumberLength = NumberLength; 
			BigInteger[] NArray = testNumbersForBitSize[bits];
			if (LOG_SPOILERS) LOG.debug("Test add32 of N with " + bits + " bit...");
			int imax = (int) Math.sqrt(N_COUNT);
			int jmin = N_COUNT - (int) Math.sqrt(N_COUNT);
			for (int i=0; i<imax; i++) {
				BigInteger a = NArray[i];
				ecm.BigNbrToBigInt(a, a32, NumberLength);
				for (int j=jmin; j<N_COUNT; j++) {
					BigInteger b = NArray[j];
					ecm.BigNbrToBigInt(b, b32, NumberLength);
					BigInteger correctResult = a.add(b);
					ecm.AddBigNbr32(a32, b32, c32);
					BigInteger testResult = ecm.BigIntToBigNbr(c32);
					if (!correctResult.equals(testResult)) {
						LOG.error("add32 failure: " + a + " + " + b + ": correct = " + correctResult + ", add32 = " + testResult);
					}
					assertEquals(correctResult, testResult);
				}
			}
		}
	}
	
	@Test
	public void testSubtract31ForRandomNumbers() {
		for (int bits = 10; bits<MAX_BITS; bits += 1) {
			int NumberLength = EllipticCurveMethod.computeNumberLength(bits);
			ecm.NumberLength = NumberLength; 
			BigInteger[] NArray = testNumbersForBitSize[bits];
			if (LOG_SPOILERS) LOG.debug("Test subtract31 of N with " + bits + " bit...");
			int imax = (int) Math.sqrt(N_COUNT);
			int jmin = N_COUNT - (int) Math.sqrt(N_COUNT);
			for (int i=0; i<imax; i++) {
				BigInteger a = NArray[i];
				ecm.BigNbrToBigInt(a, a31, NumberLength);
				for (int j=jmin; j<N_COUNT; j++) {
					BigInteger b = NArray[j];
					ecm.BigNbrToBigInt(b, b31, NumberLength);
					BigInteger correctResult = a.subtract(b);
					ecm.SubtractBigNbr(a31, b31, c31);
					BigInteger testResult = ecm.BigIntToBigNbr(c31);
					if (!correctResult.equals(testResult)) {
						ecm.Convert31To32Bits(c31, c32);
						BigInteger testResult32 = ecm.BigIntToBigNbr(c32);
						LOG.error("subtract31 failure: " + a + " - " + b + ": correct = " + correctResult + ", subtract31 = " + testResult + ", testResult32 = " + testResult32 + ", NumberLength = " + ecm.NumberLength);
					}
					assertEquals(correctResult, testResult);
				}
			}
		}
	}
	
	@Test
	public void testSubtract32ForRandomNumbers() {
		for (int bits = 10; bits<MAX_BITS; bits += 1) {
			int NumberLength = EllipticCurveMethod.computeNumberLength(bits);
			ecm.NumberLength = NumberLength; 
			BigInteger[] NArray = testNumbersForBitSize[bits];
			if (LOG_SPOILERS) LOG.debug("Test subtract32 of N with " + bits + " bit...");
			int imax = (int) Math.sqrt(N_COUNT);
			int jmin = N_COUNT - (int) Math.sqrt(N_COUNT);
			for (int i=0; i<imax; i++) {
				BigInteger a = NArray[i];
				ecm.BigNbrToBigInt(a, a32, NumberLength);
				for (int j=jmin; j<N_COUNT; j++) {
					BigInteger b = NArray[j];
					ecm.BigNbrToBigInt(b, b32, NumberLength);
					BigInteger correctResult = a.subtract(b);
					ecm.SubtractBigNbr32(a32, b32, c32);
					BigInteger testResult = ecm.BigIntToBigNbr(c32);
					if (!correctResult.equals(testResult)) {
						LOG.error("subtract32 failure: " + a + " - " + b + ": correct = " + correctResult + ", subtract32 = " + testResult);
					}
					assertEquals(correctResult, testResult);
				}
			}
			
		}
	}
}
