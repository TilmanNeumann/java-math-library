/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018 Tilman Neumann (www.tilman-neumann.de)
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.jml.factor.TestsetGenerator;

/**
 * Analyze the moduli of a-values that help the Lehman algorithm to find factors.
 * 
 * @author Tilman Neumann
 */
public class IsSqrt_Test {
	private static final Logger LOG = Logger.getLogger(IsSqrt_Test.class);

	// algorithm options
	/** number of test numbers */
	private static final int N_COUNT = 10000000;
	
	private void testRange(int bits) {
		BigInteger N_min = I_1.shiftLeft(bits-1);
		// find N-set for square tests
		ArrayList<BigInteger> NSet = TestsetGenerator.generate(bits, N_COUNT);
		LOG.info("Test N with " + bits + " bits, i.e. N >= " + N_min);
		Iterator<BigInteger> NIter = NSet.iterator();
		long[] NArray = new long[NSet.size()];
		for (int i=0; i<NSet.size(); i++) {
			NArray[i] = NIter.next().longValue();
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

		t0 = System.currentTimeMillis();
		sum = 0;
		for (long N : NArray) {
			sum += (int) Math.sqrt(N);
		}
		t1 = System.currentTimeMillis();
		LOG.info("Unguarded sqrt took " + (t1-t0) + "ms, sum=" + sum);
		LOG.info("Guarded sqrt took " + (t1-t0) + "ms, sum=" + sum);
	}

	public static void main(String[] args) {
    	ConfigUtil.initProject();
		int bits = 63;
		while (true) {
			// test N with the given number of bits, i.e. 2^(bits-1) <= N <= (2^bits)-1
	    	IsSqrt_Test testEngine = new IsSqrt_Test();
			testEngine.testRange(bits);
			bits--;
		}
	}
}
