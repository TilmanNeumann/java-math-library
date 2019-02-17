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
package de.tilman_neumann.jml.factor.hart;

import static de.tilman_neumann.jml.base.BigIntConstants.I_1;

import java.math.BigInteger;
import java.util.Arrays;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.jml.factor.TestNumberNature;
import de.tilman_neumann.jml.factor.TestsetGenerator;
import de.tilman_neumann.jml.gcd.Gcd63;
import de.tilman_neumann.util.ConfigUtil;

/**
 * Analyze the congruences best matching Hart's one-line factor algorithm when tested with 4kN values.
 * @author Tilman Neumann
 */
public class Hart_AnalyzeCongruences extends FactorAlgorithm {
	private static final Logger LOG = Logger.getLogger(Hart_AnalyzeCongruences.class);

	// algorithm options
	/** number of test numbers */
	private static final int N_COUNT = 100000;
	/** the bit size of N to start with */
	private static final int START_BITS = 30;
	/** the increment in bit size from test set to test set */
	private static final int INCR_BITS = 1;
	/** maximum number of bits to test (no maximum if null) */
	private static final Integer MAX_BITS = 63;

	/** This is a constant that is below 1 for rounding up double values to long. */
	private static final double ROUND_UP_DOUBLE = 0.9999999665;

	private static final int KMOD = 6;
	private static final int KNMOD = 8;
	private static final int AMOD = 8;

	private double[] sqrt;

	private final Gcd63 gcdEngine = new Gcd63();
	
	// dimensions: k%KMOD, (N+k)%KNMOD, a%AMOD, adjust%AMOD
	private int[][][][] counts;
	
	public Hart_AnalyzeCongruences() {
		// Precompute sqrts...
		final int kMax = 1<<25;
		sqrt = new double[kMax + 1];
		for (int i = 1; i < sqrt.length; i++) {
			final double sqrtI = Math.sqrt(i);
			sqrt[i] = sqrtI;
		}
	}
	
	@Override
	public String getName() {
		return "Hart_AnalyzeCongruences";
	}

	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		findSingleFactor(N.longValue());
		return null; // dummy
	}

	public void findSingleFactor(long N) {
		long fourN = N<<2;
		double sqrt4N = Math.sqrt(fourN);
		int kLimit = (int) Math.cbrt(N);
		for (int k = 1; k<kLimit; k++) {
			final long a0 = (long) (sqrt4N * sqrt[k] + ROUND_UP_DOUBLE);
			for (int adjust=0; adjust<AMOD; adjust++) {
				long a = a0 + adjust;
				final long test = a*a - k * fourN;
				final long b = (long) Math.sqrt(test);
				if (b*b == test) {
					long gcd = gcdEngine.gcd(a+b, N);
					if (gcd>1 && gcd<N) {
						counts[k%KMOD][(int)((k+N)%KNMOD)][(int)(a0%AMOD)][adjust]++;
						return; // removes the blur at even k!
					}
				}
			}
		}
	}
	
	private void testRange(int bits) {
		counts = new int[KMOD][KNMOD][AMOD][AMOD];
		
		BigInteger N_min = I_1.shiftLeft(bits-1);
		// find N-set for square tests
		BigInteger[] testNumbers = TestsetGenerator.generate(N_COUNT, bits, TestNumberNature.MODERATE_ODD_SEMIPRIMES);
		LOG.info("Test N with " + bits + " bits, i.e. N >= " + N_min);
		
		for (BigInteger N : testNumbers) {
			this.findSingleFactor(N);
		}
		
		for (int k=0; k<KMOD; k++) {
			for (int Nk=0; Nk<KNMOD; Nk++) {
				for (int a=0; a<AMOD; a++) {
					LOG.info("Successful adjusts for k%" + KMOD + "=" + k + ", (N+k)%" + KNMOD + "=" + Nk + ", a%" + AMOD + "=" + a + ": " + Arrays.toString(counts[k][Nk][a]));
				}
			}
		}
		LOG.info("");
	}

	public static void main(String[] args) {
    	ConfigUtil.initProject();
		int bits = START_BITS;
		while (true) {
			// test N with the given number of bits, i.e. 2^(bits-1) <= N <= (2^bits)-1
	    	Hart_AnalyzeCongruences testEngine = new Hart_AnalyzeCongruences();
			testEngine.testRange(bits);
			bits += INCR_BITS;
			if (MAX_BITS!=null && bits > MAX_BITS) break;
		}
	}
}