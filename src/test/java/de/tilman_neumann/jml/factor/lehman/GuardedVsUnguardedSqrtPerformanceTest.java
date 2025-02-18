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
package de.tilman_neumann.jml.factor.lehman;

import static de.tilman_neumann.jml.base.BigIntConstants.I_1;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.jml.factor.TestsetGenerator;
import de.tilman_neumann.jml.factor.TestNumberNature;

/**
 * Compare the performance of a "guarded sqrt" vs. the usual sqrt computations for long numbers.
 * 
 * The question about how they compare arised when the second sqrt in Lehman or Hart algorithms showed no sign of getting faster by "guarding".
 * This test confirms that there is little difference.
 * 
 * @author Tilman Neumann
 */
public class GuardedVsUnguardedSqrtPerformanceTest {
	private static final Logger LOG = LogManager.getLogger(GuardedVsUnguardedSqrtPerformanceTest.class);

	/** number of test numbers */
	private static final int N_COUNT = 1000000; // bigger values required for useful timings
	
	private void testRange(int bits) {
		BigInteger N_min = I_1.shiftLeft(bits-1);
		BigInteger[] testNumbers = TestsetGenerator.generate(N_COUNT, bits, TestNumberNature.MODERATE_SEMIPRIMES);
		assertEquals(N_COUNT, testNumbers.length);
		LOG.info("Test N with " + bits + " bits, i.e. N >= " + N_min);
		long[] NArray = new long[N_COUNT];
		for (int i=0; i<N_COUNT; i++) {
			NArray[i] = testNumbers[i].longValue();
		}
		long t0, t1, sum;
		t0 = System.currentTimeMillis();
		sum = 0;
		for (long N : NArray) {
			int m = (int) (N & 127);
			if (((m*0x8bc40d7dL) & (m*0xa1e2f5d1L) & 0x14020aL) == 0) {
				sum += (int) Math.sqrt(N);
			}
		}
		t1 = System.currentTimeMillis();
		LOG.info("Guarded sqrt took " + (t1-t0) + "ms, sum=" + sum);
		// the sum is only logged to avoid compiler optimizations
		
		t0 = System.currentTimeMillis();
		sum = 0;
		for (long N : NArray) {
			sum += (int) Math.sqrt(N);
		}
		t1 = System.currentTimeMillis();
		LOG.info("Unguarded sqrt took " + (t1-t0) + "ms, sum=" + sum);
	}

	public static void main(String[] args) {
    	ConfigUtil.initProject();
		int bits = 63;
		while (true) {
			// test N with the given number of bits, i.e. 2^(bits-1) <= N <= (2^bits)-1
	    	GuardedVsUnguardedSqrtPerformanceTest testEngine = new GuardedVsUnguardedSqrtPerformanceTest();
			testEngine.testRange(bits);
			bits--;
		}
	}
}
