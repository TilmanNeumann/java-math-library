/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2026 Tilman Neumann - tilman.neumann@web.de
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

import java.math.BigInteger;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;

public class Int128Test {

	private static final Logger LOG = LogManager.getLogger(Int128Test.class);

	private static final int NCOUNT = 1000;
	
	private static BigInteger[] hi_big = new BigInteger[NCOUNT];
	private static BigInteger[] lo_big = new BigInteger[NCOUNT];
	private static long[] hi = new long[NCOUNT];
	private static long[] lo = new long[NCOUNT];

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		
		Random RNG = new Random();
		
		for (int i=0; i<NCOUNT; i++) {
			hi_big[i] = new BigInteger(63, RNG);
			lo_big[i] = new BigInteger(64, RNG);
			
			hi[i] = hi_big[i].longValue();
			lo[i] = lo_big[i].longValue();
		}
	}

	@Test
	public void testAdd() {
		for (int i=0; i<NCOUNT; i++) {
			BigInteger a_hi_big = hi_big[i];
			BigInteger a_lo_big = lo_big[i];
			long a_hi = hi[i];
			long a_lo = lo[i];
			
			for (int j=0; j<NCOUNT; j++) {
				BigInteger b_hi_big = hi_big[j];
				BigInteger b_lo_big = lo_big[j];
				long b_hi = hi[j];
				long b_lo = lo[j];
				
				// add two 127 bit integers
				Int128 a128 = new Int128(a_hi, a_lo);
				Int128 b128 = new Int128(b_hi, b_lo);
				Int128 sum128 = a128.add(b128);
				// compute correct result in bigIntegers and compare
				BigInteger a128Big = a_hi_big.shiftLeft(64).add(a_lo_big);
				BigInteger b128Big = b_hi_big.shiftLeft(64).add(b_lo_big);
				BigInteger correctSum = a128Big.add(b128Big);
				Assert.assertEquals(correctSum, sum128.toBigIntegerUnsigned());
			}
		}
	}

	@Test
	public void testSubtract() {
		for (int i=0; i<NCOUNT; i++) {
			BigInteger a_hi_big = hi_big[i];
			BigInteger a_lo_big = lo_big[i];
			long a_hi = hi[i];
			long a_lo = lo[i];
			
			for (int j=0; j<NCOUNT; j++) {
				BigInteger b_hi_big = hi_big[j];
				BigInteger b_lo_big = lo_big[j];
				long b_hi = hi[j];
				long b_lo = lo[j];
				
				// subtract two 127 bit integers
				Int128 a128 = new Int128(a_hi, a_lo);
				Int128 b128 = new Int128(b_hi, b_lo);
				Int128 diff128 = a128.subtract(b128);
				// compute correct result in bigIntegers and compare
				BigInteger a128Big = a_hi_big.shiftLeft(64).add(a_lo_big);
				BigInteger b128Big = b_hi_big.shiftLeft(64).add(b_lo_big);
				BigInteger correctDiff = a128Big.subtract(b128Big);
				Assert.assertEquals(correctDiff, diff128.toBigInteger()); // here we need signed toBigInteger() conversion
			}
		}
	}

	@Test
	public void testMul63Unsigned() {
		
		// here we only use the "hi" numbers which are 63 bit
		
		for (int i=0; i<NCOUNT; i++) {
			BigInteger a_hi_big = hi_big[i];
			long a_hi = hi[i];
			
			for (int j=0; j<NCOUNT; j++) {
				BigInteger b_hi_big = hi_big[j];
				long b_hi = hi[j];
	
				Int128 prod128 = Int128.mul63Unsigned(a_hi, b_hi);
				BigInteger prod128Big = prod128.toBigIntegerUnsigned();
				BigInteger correctProd = a_hi_big.multiply(b_hi_big);
				Assert.assertEquals(correctProd, prod128Big);
			}
		}
	}

	@Test
	public void testMul64Unsigned() {
		for (int i=0; i<NCOUNT; i++) {
			BigInteger a_lo_big = lo_big[i];
			long a_lo = lo[i];
			
			for (int j=0; j<NCOUNT; j++) {
				BigInteger b_lo_big = lo_big[j];
				long b_lo = lo[j];
	
				Int128 prod128 = Int128.mul64Unsigned(a_lo, b_lo);
				BigInteger prod128Big = prod128.toBigIntegerUnsigned();
				BigInteger correctProd = a_lo_big.multiply(b_lo_big);
				if (!correctProd.equals(prod128Big)) {
					LOG.error("mul64Unsigned: " + a_lo_big + "*" + b_lo_big + ": correct = " + correctProd + " but result = " + prod128Big);
				}
				Assert.assertEquals(correctProd, prod128Big);
			}
		}
	}

	@Test
	public void testMul64UnsignedMH() {
		for (int i=0; i<NCOUNT; i++) {
			BigInteger a_lo_big = lo_big[i];
			long a_lo = lo[i];
			
			for (int j=0; j<NCOUNT; j++) {
				BigInteger b_lo_big = lo_big[j];
				long b_lo = lo[j];
				
				Int128 prod128 = Int128.mul64UnsignedMH(a_lo, b_lo);
				BigInteger prod128Big = prod128.toBigIntegerUnsigned();
				BigInteger correctProd = a_lo_big.multiply(b_lo_big);
				if (!correctProd.equals(prod128Big)) {
					LOG.error("mul64UnsignedMH: " + a_lo_big + "*" + b_lo_big + ": correct = " + correctProd + " but result = " + prod128Big);
				}
				Assert.assertEquals(correctProd, prod128Big);
			}
		}
	}

	@Test
	public void testDivide128by64Unsigned() {
		for (int i=0; i<NCOUNT; i++) {
			Int128 a = new Int128(hi[i], lo[i]);
			BigInteger aBig = a.toBigIntegerUnsigned();

			for (int j=0; j<NCOUNT; j++) {
				BigInteger bBig = hi_big[j];
				long b = hi[j];
				
				long[] result = Int128.divide128by64Unsigned(hi[i], lo[i], b);
				BigInteger quot = new Int128(result[0], result[1]).toBigIntegerUnsigned();
				BigInteger rem = BigInteger.valueOf(result[2]);
				BigInteger[] correctResult = aBig.divideAndRemainder(bBig);
				BigInteger correctQuot = correctResult[0];
				BigInteger correctRem = correctResult[1];
				if (!correctQuot.equals(quot) || !correctRem.equals(rem) ) {
					LOG.error("divide128by64Unsigned: " + a + " / " + b + ": correct = " + correctQuot + " rem " + correctRem + ", but result = " + quot + " rem " + rem);
				}
				Assert.assertEquals(correctQuot, quot);
				Assert.assertEquals(correctRem, rem);
			}
		}
	}

	@Test
	public void testMod128by64Unsigned() {
		for (int i=0; i<NCOUNT; i++) {
			Int128 a = new Int128(hi[i], lo[i]);
			BigInteger aBig = a.toBigIntegerUnsigned();

			for (int j=0; j<NCOUNT; j++) {
				BigInteger bBig = hi_big[j];
				long b = hi[j];
				
				long result = Int128.mod128by64Unsigned(hi[i], lo[i], b);
				BigInteger rem = BigInteger.valueOf(result);
				BigInteger correctRem = aBig.mod(bBig);
				if (!correctRem.equals(rem) ) {
					LOG.error("mod128by64Unsigned: " + a + " % " + b + ": correct = " + correctRem + ", but result = " + rem);
				}
				Assert.assertEquals(correctRem, rem);
			}
		}
	}
}
