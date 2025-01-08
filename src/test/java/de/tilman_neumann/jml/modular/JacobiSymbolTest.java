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
package de.tilman_neumann.jml.modular;

import java.math.BigInteger;
import java.util.Random;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.primes.probable.BPSWTest;
import de.tilman_neumann.util.ConfigUtil;

import static de.tilman_neumann.jml.base.BigIntConstants.*;
import static org.junit.Assert.assertEquals;

/**
 * Test of Legendre and Jacobi symbol.
 * 
 * @author Tilman Neumann
 */
public class JacobiSymbolTest {
	private static final Logger LOG = LogManager.getLogger(JacobiSymbolTest.class);
	
	private static final int NCOUNT = 200;
	private static final int MAX_BITS = 100;
	
	private static final BPSWTest bpsw = new BPSWTest();
	private static final Random RNG = new Random();
	private static final LegendreSymbol legendreEngine = new LegendreSymbol();
	private static final JacobiSymbol jacobiEngine = new JacobiSymbol();;
	
	@SuppressWarnings("unchecked")
	private static ArrayList<BigInteger>[] aListsForBitSize = new ArrayList[MAX_BITS];
	@SuppressWarnings("unchecked")
	private static ArrayList<BigInteger>[] pListsForBitSize = new ArrayList[MAX_BITS];
	
	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		
		// generate test numbers
		for (int bits=10; bits<MAX_BITS; bits+=10) {
			ArrayList<BigInteger> aList = new ArrayList<BigInteger>();
			for (int i=0; i<NCOUNT; i++) {
				aList.add(new BigInteger(bits, RNG));
			}
			aListsForBitSize[bits] = aList;
			
			int i=0;
			ArrayList<BigInteger> pList = new ArrayList<BigInteger>();
			while (i<NCOUNT) {
				// the p must be odd, and to allow comparison with the Legendre symbol it should be odd primes
				BigInteger p = bpsw.nextProbablePrime(new BigInteger(bits, RNG));
				if (p.and(I_1).intValue()==1) {
					pList.add(p);
					i++;
				}
			}
			pListsForBitSize[bits] = pList;
		}
	}

	@Test
	public void testBigABigP() {
		for (int bits=10; bits<MAX_BITS; bits+=10) {
			ArrayList<BigInteger> aList = aListsForBitSize[bits];
			ArrayList<BigInteger> pList = pListsForBitSize[bits];
			
			LOG.info("test Legendre(a|p) and Jacobi(a|p) for BigInteger a, p with " + bits + " bit.");
			
			for (BigInteger a : aList) {
				for (BigInteger p : pList) {
					int correct = jacobiEngine.jacobiSymbol_v01(a, p);
					int jacobi02 = jacobiEngine.jacobiSymbol_v02(a, p);
					int jacobi03 = jacobiEngine.jacobiSymbol/*_v03*/(a, p);
					int legendre = legendreEngine.EulerFormula(a, p);
					assertEquals(correct, jacobi02);
					assertEquals(correct, jacobi03);
					assertEquals(correct, legendre);
				}
			}
		}
	}

	@Test
	public void testBigASmallP() {
		for (int bits=10; bits<MAX_BITS; bits+=10) {
			ArrayList<BigInteger> aList = aListsForBitSize[bits];
			int pBits = Math.min(bits, 30);
			ArrayList<BigInteger> pList = pListsForBitSize[pBits];
			
			LOG.info("test Legendre(a|p) and Jacobi(a|p) for BigInteger a with " + bits + " bit and int p with " + pBits + " bit.");
			
			for (BigInteger a : aList) {
				for (BigInteger p : pList) {
					int pInt = p.intValue();
					int correct = jacobiEngine.jacobiSymbol_v01(a, p);
					int jacobi03 = jacobiEngine.jacobiSymbol/*_v03*/(a, pInt);
					int legendre = legendreEngine.EulerFormula(a, pInt);
					assertEquals(correct, jacobi03);
					assertEquals(correct, legendre);
				}
			}
		}
	}

	@Test
	public void testSmallABigP() {
		// we have no Euler formula implementation with small a and big p, so here only Jacobi is tested
		for (int bits=10; bits<MAX_BITS; bits+=10) {
			int aBits = Math.min(bits, 30);
			ArrayList<BigInteger> aList = aListsForBitSize[aBits];
			ArrayList<BigInteger> pList = pListsForBitSize[bits];
			
			LOG.info("test Jacobi(a|p) for int a with " + aBits + " bit and BigInteger p with " + bits + " bit.");
			
			for (BigInteger a : aList) {
				int aInt = a.intValue();
				for (BigInteger p : pList) {
					int correct = jacobiEngine.jacobiSymbol_v01(a, p);
					int jacobi03 = jacobiEngine.jacobiSymbol/*_v03*/(aInt, p);
					assertEquals(correct, jacobi03);
				}
			}
		}
	}

	@Test
	public void testSmallASmallP() {
		for (int bits=10; bits<=30; bits+=10) {
			ArrayList<BigInteger> aList = aListsForBitSize[bits];
			ArrayList<BigInteger> pList = pListsForBitSize[bits];
			
			LOG.info("test Legendre(a|p) and Jacobi(a|p) for int a, p with " + bits + " bits");
			
			for (BigInteger a : aList) {
				int aInt = a.intValue();
				for (BigInteger p : pList) {
					int pInt = p.intValue();
					int correct = jacobiEngine.jacobiSymbol_v01(a, p);
					int jacobi03 = jacobiEngine.jacobiSymbol/*_v03*/(aInt, pInt);
					int legendre = legendreEngine.EulerFormula(aInt, pInt);
					assertEquals(correct, jacobi03);
					assertEquals(correct, legendre);
				}
			}
		}
	}
}
