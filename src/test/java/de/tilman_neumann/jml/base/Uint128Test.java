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
package de.tilman_neumann.jml.base;

import java.math.BigInteger;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;

public class Uint128Test {

	private static final Logger LOG = LogManager.getLogger(Uint128Test.class);

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
	public void testAdd_v1() {
		
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
				Uint128 a128 = new Uint128(a_hi, a_lo);
				Uint128 b128 = new Uint128(b_hi, b_lo);
				Uint128 sum128 = a128.add_v1(b128);
				// compute correct result in bigIntegers and compare
				BigInteger a128Big = a_hi_big.shiftLeft(64).add(a_lo_big);
				BigInteger b128Big = b_hi_big.shiftLeft(64).add(b_lo_big);
				BigInteger correctSum = a128Big.add(b128Big);
				Assert.assertEquals(correctSum, sum128.toBigInteger());
			}
		}
	}

	@Test
	public void testAdd_v2() {
		
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
				Uint128 a128 = new Uint128(a_hi, a_lo);
				Uint128 b128 = new Uint128(b_hi, b_lo);
				Uint128 sum128 = a128.add/*_v2*/(b128);
				// compute correct result in bigIntegers and compare
				BigInteger a128Big = a_hi_big.shiftLeft(64).add(a_lo_big);
				BigInteger b128Big = b_hi_big.shiftLeft(64).add(b_lo_big);
				BigInteger correctSum = a128Big.add(b128Big);
				Assert.assertEquals(correctSum, sum128.toBigInteger());
			}
		}
	}

	@Test
	public void testMul63() {
		
		// here we only use the "hi" numbers which are 63 bit
		
		for (int i=0; i<NCOUNT; i++) {
			BigInteger a_hi_big = hi_big[i];
			long a_hi = hi[i];
			
			for (int j=0; j<NCOUNT; j++) {
				BigInteger b_hi_big = hi_big[j];
				long b_hi = hi[j];
	
				Uint128 prod128 = Uint128.mul63(a_hi, b_hi);
				BigInteger prod128Big = prod128.toBigInteger();
				BigInteger correctProd = a_hi_big.multiply(b_hi_big);
				Assert.assertEquals(correctProd, prod128Big);
			}
		}
	}

	@Test
	public void testMul64_v1() {
		
		// here we only use the "lo" numbers which are 64 bit

		for (int i=0; i<NCOUNT; i++) {
			BigInteger a_lo_big = lo_big[i];
			long a_lo = lo[i];
			
			for (int j=0; j<NCOUNT; j++) {
				BigInteger b_lo_big = lo_big[j];
				long b_lo = lo[j];
	
				Uint128 prod128 =  Uint128.mul64_v1(a_lo, b_lo);
				BigInteger prod128Big = prod128.toBigInteger();
				BigInteger correctProd = a_lo_big.multiply(b_lo_big);
				if (!correctProd.equals(prod128Big)) {
					LOG.error("mul64_v1: " + a_lo_big + "*" + b_lo_big + ": correct = " + correctProd + " but result = " + prod128Big);
				}
				Assert.assertEquals(correctProd, prod128Big);
			}
		}
	}

	@Test
	public void testMul64_v2() {
		
		for (int i=0; i<NCOUNT; i++) {
			BigInteger a_lo_big = lo_big[i];
			long a_lo = lo[i];
			
			for (int j=0; j<NCOUNT; j++) {
				BigInteger b_lo_big = lo_big[j];
				long b_lo = lo[j];
	
				Uint128 prod128 = Uint128. mul64/*_v2*/(a_lo, b_lo);
				BigInteger prod128Big = prod128.toBigInteger();
				BigInteger correctProd = a_lo_big.multiply(b_lo_big);
				if (!correctProd.equals(prod128Big)) {
					LOG.error("mul64_v2: " + a_lo_big + "*" + b_lo_big + ": correct = " + correctProd + " but result = " + prod128Big);
				}
				Assert.assertEquals(correctProd, prod128Big);
			}
		}
	}

	@Test
	public void testMul64_MH() {
		
		for (int i=0; i<NCOUNT; i++) {
			BigInteger a_lo_big = lo_big[i];
			long a_lo = lo[i];
			
			for (int j=0; j<NCOUNT; j++) {
				BigInteger b_lo_big = lo_big[j];
				long b_lo = lo[j];
				
				Uint128 prod128 = Uint128. mul64_MH(a_lo, b_lo);
				BigInteger prod128Big = prod128.toBigInteger();
				BigInteger correctProd = a_lo_big.multiply(b_lo_big);
				if (!correctProd.equals(prod128Big)) {
					LOG.error("mul64_MH: " + a_lo_big + "*" + b_lo_big + ": correct = " + correctProd + " but result = " + prod128Big);
				}
				Assert.assertEquals(correctProd, prod128Big);
			}
		}
	}
	
	// we do not test spMul64_MH() here because we know that it is wrong in general
}
