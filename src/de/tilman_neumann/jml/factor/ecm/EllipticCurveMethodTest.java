/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2019 Tilman Neumann (www.tilman-neumann.de)
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
import java.security.SecureRandom;
import java.util.Arrays;

import org.apache.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

public class EllipticCurveMethodTest {
	
	static class SpecialTest {
		BigInteger N;
		int testBitLength;
		
		/**
		 * @param N
		 * @param testBitLength the test bit size for which N was created
		 */
		public SpecialTest(BigInteger N, int testBitLength) {
			this.N = N;
			this.testBitLength = testBitLength;
		}
	}
	
	private static final Logger LOG = Logger.getLogger(EllipticCurveMethodTest.class);

	private static final int N_COUNT = 1000000;

	private final SecureRandom RNG = new SecureRandom();
	
	private final EllipticCurveMethod ecm = new EllipticCurveMethod();
	
	private final int[] a31 = new int[EllipticCurveMethod.NLen];
	private final int[] b31 = new int[EllipticCurveMethod.NLen];
	private final int[] c31 = new int[EllipticCurveMethod.NLen];
	
	private final long[] a32 = new long[EllipticCurveMethod.NLen];
	private final long[] b32 = new long[EllipticCurveMethod.NLen];
	private final long[] c32 = new long[EllipticCurveMethod.NLen];
	
	public EllipticCurveMethodTest() {
		RNG.setSeed(42); // make test results repeatible
	}
	
	private void setNumberLength(int NumberLength) {
		ecm.NumberLength = NumberLength; 
	}
	
	private void testInOutConversion(BigInteger N, int NumberLength) {
		try {
			ecm.BigNbrToBigInt(N, a31, NumberLength);
			BigInteger Nout = ecm.BigIntToBigNbr(a31);
			if (!N.equals(Nout)) {
				ecm.Convert31To32Bits(a31, a32);
				BigInteger testResult32 = ecm.BigIntToBigNbr(a32);
				LOG.error("In-out-conversion failure: N=" + N + ", Nout = " + Nout + ", testResult32 = " + testResult32 + ", NumberLength = " + ecm.NumberLength);
			}
		} catch (Exception e) {
			LOG.error("Conversion of N=" + N + " caused " + e, e);
		}
	}
	
	private void testToStringConversion(BigInteger N, int NumberLength) {
		String NStr = N.toString();
		ecm.BigNbrToBigInt(N, a31, NumberLength);
		String big31Str = ecm.BigNbrToString(a31);
		if (!NStr.equals(big31Str)) {
			LOG.error("toString-conversion failure: correct=" + NStr + ", big31Str = " + big31Str);
		}
	}
	
	private void testRandomNumbers() {
		for (int bits = 10; bits<1000; bits += 1) {
			int NumberLength = ecm.NumberLength = EllipticCurveMethod.computeNumberLength(bits);
			
			LOG.debug("Create " + N_COUNT + " N with " + bits + " bit...");
			BigInteger[] NArray = new BigInteger[N_COUNT];
			for (int i=0; i<N_COUNT; i++) {
				NArray[i] = new BigInteger(bits, RNG)/*.negate()*/; // XXX test neg args, too
			}
			
			// in-out-conversion
			LOG.debug("Test in-out-conversion of N with " + bits + " bit...");
			for (int i=0; i<N_COUNT; i++) {
				testInOutConversion(NArray[i], NumberLength);
			}
			
			// toString
			LOG.debug("Test toString conversion of N with " + bits + " bit...");
			for (int i=0; i<N_COUNT; i++) {
				BigInteger N = NArray[i];
				String NStr = N.toString();
				ecm.BigNbrToBigInt(N, a31, NumberLength);
				String big31Str = ecm.BigNbrToString(a31);
				if (!NStr.equals(big31Str)) {
					LOG.error("toString-conversion failure: correct=" + NStr + ", big31Str = " + big31Str);
				}
			}
			
			if (bits < 64) {
				// long conversion
				LOG.debug("Test long conversion of N with " + bits + " bit...");
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
				}
			}
			
			// add32
			LOG.debug("Test add32 of N with " + bits + " bit...");
			int imax = (int) Math.sqrt(N_COUNT);
			int jmin = N_COUNT - (int) Math.sqrt(N_COUNT);
			for (int i=0; i<imax; i++) {
				BigInteger a = NArray[i];
				ecm.BigNbrToBigInt(a, a32);
				for (int j=jmin; j<N_COUNT; j++) {
					BigInteger b = NArray[j];
					ecm.BigNbrToBigInt(b, b32);
					BigInteger correctResult = a.add(b);
					ecm.AddBigNbr32(a32, b32, c32);
					BigInteger testResult = ecm.BigIntToBigNbr(c32);
					if (!correctResult.equals(testResult)) {
						LOG.error("add32 failure: " + a + " + " + b + ": correct = " + correctResult + ", add32 = " + testResult);
					}
				}
			}

			// add31
			LOG.debug("Test add31 of N with " + bits + " bit...");
			imax = (int) Math.sqrt(N_COUNT);
			jmin = N_COUNT - (int) Math.sqrt(N_COUNT);
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
				}
			}
			
			// subtract32
			LOG.debug("Test subtract32 of N with " + bits + " bit...");
			imax = (int) Math.sqrt(N_COUNT);
			jmin = N_COUNT - (int) Math.sqrt(N_COUNT);
			for (int i=0; i<imax; i++) {
				BigInteger a = NArray[i];
				ecm.BigNbrToBigInt(a, a32);
				for (int j=jmin; j<N_COUNT; j++) {
					BigInteger b = NArray[j];
					ecm.BigNbrToBigInt(b, b32);
					BigInteger correctResult = a.subtract(b);
					ecm.SubtractBigNbr32(a32, b32, c32);
					BigInteger testResult = ecm.BigIntToBigNbr(c32);
					if (!correctResult.equals(testResult)) {
						LOG.error("subtract32 failure: " + a + " - " + b + ": correct = " + correctResult + ", subtract32 = " + testResult);
					}
				}
			}
			
			// subtract31
			LOG.debug("Test subtract31 of N with " + bits + " bit...");
			imax = (int) Math.sqrt(N_COUNT);
			jmin = N_COUNT - (int) Math.sqrt(N_COUNT);
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
				}
			}
		} // end_for bits
	}
	
	public static void main(String[] args) {
		ConfigUtil.initProject();
		
		SpecialTest[] tests = new SpecialTest[] {
			// TODO Some rare failures of in-out- and toString conversion in random test
			// The N have in common that their actual bit size is ~20 bit smaller than the generation target was
			new SpecialTest(new BigInteger("-9047107805356617574821419177374462823988751688943829812044231389676"), 248),
			new SpecialTest(new BigInteger("-810645591595199570650496051345328669857441632357541306927499776869083777701428362211927875931927"), 341),
			new SpecialTest(new BigInteger("-1661507549698651677347352443872649283631112234579374113959724008640719132113623333818063620959056420710153"), 371),
			new SpecialTest(new BigInteger("-3879725014912056720725365884026092090047847467519283535255468693493067092968062030773638553790318583416720"), 371),
			new SpecialTest(new BigInteger("-81238486828807671926156945485493053897286271079236717483584339941912993848458748988378027450406814055929"), 371),
		};
		
		EllipticCurveMethodTest ecmTest = new EllipticCurveMethodTest();
		
		for (SpecialTest test : tests) {
			BigInteger N = test.N;
			LOG.debug("N=" + N + " has " + N.bitLength() + " bit");
			LOG.debug("byte array=" + Arrays.toString(N.toByteArray()));
			LOG.debug("binary=" + N.toString(2));
			int NumberLength = EllipticCurveMethod.computeNumberLength(test.testBitLength);
			ecmTest.setNumberLength(NumberLength);
			ecmTest.testInOutConversion(N, NumberLength);
			ecmTest.testToStringConversion(N, NumberLength);
		}
		
		ecmTest.testRandomNumbers();
	}
}
