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

import org.apache.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

public class EllipticCurveMethodTest {
	private static final Logger LOG = Logger.getLogger(EllipticCurveMethodTest.class);
	
	private static final SecureRandom RNG = new SecureRandom();

	private static final int N_COUNT = 100000;
	
	public static void main(String[] args) {
		ConfigUtil.initProject();
		
		EllipticCurveMethod ecm = new EllipticCurveMethod();
		
		int[] a31 = new int[EllipticCurveMethod.NLen];
		int[] b31 = new int[EllipticCurveMethod.NLen];
		int[] c31 = new int[EllipticCurveMethod.NLen];
		
		long[] a32 = new long[EllipticCurveMethod.NLen];
		long[] b32 = new long[EllipticCurveMethod.NLen];
		long[] c32 = new long[EllipticCurveMethod.NLen];
		
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
				BigInteger Nin = NArray[i];
				try {
					ecm.BigNbrToBigInt(Nin, a31, NumberLength);
					BigInteger Nout = ecm.BigIntToBigNbr(a31);
					if (!Nin.equals(Nout)) {
						ecm.Convert31To32Bits(a31, a32);
						BigInteger testResult32 = ecm.BigIntToBigNbr(a32);
						LOG.error("In-out-conversion failure: Nin=" + Nin + ", Nout = " + Nout + ", testResult32 = " + testResult32 + ", NumberLength = " + ecm.NumberLength);
					}
				} catch (Exception e) {
					LOG.error("Conversion of N=" + Nin + " caused " + e, e);
				}
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
}
