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
package de.tilman_neumann.jml.factor.siqs.poly;

import static de.tilman_neumann.jml.base.BigIntConstants.I_0;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;

import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.factor.siqs.ModularSqrtsEngine;
import de.tilman_neumann.util.ConfigUtil;

/**
 * A test of the b-computation numbers reported by [Contini, p.10].
 * 
 * This is no good QA test yet, because most of the tested code is no production code, but a copy of it inlined here.
 * The reason for this shortcoming is that the data given by Contini would not be easy to reproduce in a real-world factoring example.
 * 
 * @author Tilman Neumann
 * 
 * @see [Contini]: Scott Patrick Contini, "Factoring Integers with the self-initializing quadraric sieve", Master thesis, University of Georgia, 1997
 */
public class BParamTest {
	private static final Logger LOG = LogManager.getLogger(BParamTest.class);
	private static final boolean DEBUG = true;
	
	private BigInteger a = BigInteger.valueOf(385); // 5*7*11
	private BigInteger b;
	private int qCount = 3;
	private int[] qArray = new int[] {5, 7, 11};
	private int[] qIndexArray = new int[] {0, 1, 2};
	private BigInteger kN = BigInteger.valueOf(291);
	private int[] tArray = new int[3]; // computed later
	private BigInteger[] B2Array = new BigInteger[3];
	private int bIndex;
	
	// tArray calculator
	private ModularSqrtsEngine modularSqrtsEngine = new ModularSqrtsEngine();

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}
	
	@Test
	public void testBParamComputation() {
		// compute and log modular sqrt's
		computeTArray();
		for (int i=0; i<qCount; i++) {
			LOG.info("t[" + i + "] = " + tArray[i]);
		}
		
		// compute first b and test B-values reported by [Contini p. 10]
		int[] correct_B_values = new int[] {154, 110, 70};
		computeFirstBParameter();
		for (int i=0; i<qCount; i++) {
			int B = B2Array[i].intValue()/2;
			LOG.info("B[" + i + "] = " + B);
			assertEquals(correct_B_values[i], B);
		}
		
		// compute and test next b-values
		int[] correct_b_values = new int[] {334, 26, -194, 114};
		bIndex = 1;
		LOG.info("b[0] = " + b);
		assertEquals(correct_b_values[0], b.intValue());
		for (int i=1; i<4; i++) {
			computeNextBParameter();
			LOG.info("b[" + i + "] = " + b);
			assertEquals(correct_b_values[i], b.intValue());
		}
	}

	private void computeTArray() {
		tArray = modularSqrtsEngine.computeTArray(qArray, 3, kN);
	}
	
	/**
	 * Compute the B-array and the first b-parameter.
	 */
	private void computeFirstBParameter() {
		// compute 2*B_l[] and the first b; the notation is mostly following [Contini97]
		this.b = I_0;
		for (int l=0; l<qCount; l++) {
			int ql = qArray[l];
			int qIndex = qIndexArray[l];
			int t = tArray[qIndex];
			BigInteger ql_big = BigInteger.valueOf(ql);
			BigInteger a_div_ql = a.divide(ql_big); // exact
			// the modular inverse is small enough to fit into int, but for the product below we need long precision
			long a_div_ql_modInv_ql = a_div_ql.modInverse(ql_big).longValue();
			int gamma = (int) ((t*a_div_ql_modInv_ql) % ql);
			if (gamma > (ql>>1)) gamma = ql - gamma; // take the smaller choice of gamma
			BigInteger Bl = a_div_ql.multiply(BigInteger.valueOf(gamma));
			if (DEBUG) {
				assertTrue(Bl.compareTo(I_0) >= 0);
				assertEquals(I_0, Bl.multiply(Bl).subtract(kN).mod(ql_big));
			}
			B2Array[l] = Bl.shiftLeft(1); // store 2 * B_l in B2[0]...B2[s-1] (the last one is only required to compute b below)
			// WARNING: In contrast to the description in [Contini p.10, 2nd paragraph],
			// WARNING: b must not be computed (mod a) !
			b = b.add(Bl);
		}
		if (DEBUG) {
			// initial b are positive (50% of "next" b's are not)
			assertTrue(b.signum() >= 0);
			// we have b^2 == kN (mod a)
			assertEquals(I_0, b.multiply(b).subtract(kN).mod(a));
		}
	}

	void computeNextBParameter() {
		int v = Integer.numberOfTrailingZeros(bIndex<<1);
		LOG.info("v = " + v);
		// bIndex/2^v is a half-integer. Therefore we have ceilTerm = ceil(bIndex/2^v) = bIndex/2^v + 1.
		// If ceilTerm is even, then (-1)^ceilTerm is positive and B_l[v] must be added.
		// Slightly faster is: if ceilTerm-1 = bIndex/2^v is odd then B_l[v] must be added.
		boolean bParameterNeedsAddition = (((bIndex/(1<<v)) & 1) == 1);
		// WARNING: In contrast to the description in [Contini p.10, 2nd paragraph],
		// WARNING: b must not be computed (mod a) !
		b = bParameterNeedsAddition ? b.add(B2Array[v-1]) : b.subtract(B2Array[v-1]);
		bIndex++;
	}
}
