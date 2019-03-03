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

import java.math.BigInteger;
import java.util.Arrays;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.gcd.Gcd63;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;
import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.jml.factor.TestsetGenerator;
import de.tilman_neumann.jml.factor.tdiv.TDiv63Inverse;
import de.tilman_neumann.jml.factor.TestNumberNature;

/**
 * Analyze the frequency with which different k find a factor.
 * 
 * Some results:
 * -> k prime or k=2*prime are very bad
 * -> very smooth k are pretty good but the influence of the power of 2 in such k is strange and not well understood
 * -> N that are factored by many k are typically factored by k=k_base*{1^2, 3^2, 5^2, 7^2, ...}
 * 
 * @author Tilman Neumann
 */
public class Lehman_AnalyzeKStructure extends FactorAlgorithm {
	private static final Logger LOG = Logger.getLogger(Lehman_AnalyzeKStructure.class);

	// algorithm options
	/** number of test numbers */
	private static final int N_COUNT = 100000;

	/** This is a constant that is below 1 for rounding up double values to long. */
	private static final double ROUND_UP_DOUBLE = 0.9999999665;

	private long fourN;
	private double sqrt4N;
	private final Gcd63 gcdEngine = new Gcd63();
	private final TDiv63Inverse tdiv = new TDiv63Inverse(1<<21);

	/** The number of N's factored by the individual k values */
	private int[][] kFactorCounts;
	private int arrayIndex;
	
	@Override
	public String getName() {
		return "Lehman_AnalyzeKStructure";
	}

	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		return BigInteger.valueOf(findSingleFactor(N.longValue()));
	}
	
	public long findSingleFactor(long N) {
		final int cbrt = (int) Math.cbrt(N);
		fourN = N<<2;
		sqrt4N = Math.sqrt(fourN);
		
		final int kLimit = cbrt;
		final double sixthRootTerm = 0.25 * Math.pow(N, 1/6.0); // double precision is required for stability
		for (int k=1; k <= kLimit; k++) {
			double sqrtK = Math.sqrt(k);
			final double sqrt4kN = sqrt4N * sqrtK;
			// only use long values
			final long aStart = (long) (sqrt4kN + ROUND_UP_DOUBLE); // much faster than ceil()
			long aLimit = (long) (sqrt4kN + sixthRootTerm / sqrtK);
			long aStep;
			if ((k & 1) == 0) {
				// k even -> make sure aLimit is odd
				aLimit |= 1L;
				aStep = 2;
			} else {
				final long kPlusN = k + N;
				if ((kPlusN & 3) == 0) {
					aStep = 8;
					aLimit += ((kPlusN - aLimit) & 7);
				} else {
					aStep = 4; // stepping over both adjusts with step width 16 would be more exact but is not faster
					final long adjust1 = (kPlusN - aLimit) & 15;
					final long adjust2 = (-kPlusN - aLimit) & 15;
					aLimit += adjust1<adjust2 ? adjust1 : adjust2;
				}
			}

			// processing the a-loop top-down is faster than bottom-up
			final long fourkN = k * fourN;
			for (long a=aLimit; a >= aStart; a-=aStep) {
				final long test = a*a - fourkN;
				// Here test<0 is possible because of double to long cast errors in the 'a'-computation.
				// But then b = Math.sqrt(test) gives 0 (sic!) => 0*0 != test => no errors.
				final long b = (long) Math.sqrt(test);
				if (b*b == test) {
					long gcd = gcdEngine.gcd(a+b, N);
					if (gcd>1 && gcd<N) {
						kFactorCounts[k][arrayIndex]++;
					}
				}
			}
		}
		
		// If sqrt(4kN) is very near to an exact integer then the fast ceil() in the 'aStart'-computation
		// may have failed. Then we need a "correction loop":
		final int kTwoA = (((cbrt >> 6) + 6) / 6) * 6;
		for (int k=kTwoA + 1; k <= kLimit; k++) {
			long a = (long) (sqrt4N * Math.sqrt(k)+ ROUND_UP_DOUBLE) - 1;
			long test = a*a - k*fourN;
			long b = (long) Math.sqrt(test);
			if (b*b == test) {
				//return gcdEngine.gcd(a+b, N);
			}
	    }

		return 1; // fail
	}
	
	private void test() {
		// zero-init count arrays
		int kMax = (int) Math.cbrt(1L<<(39+1));
		kFactorCounts = new int[kMax][10];
		// test from 30 to 39 bits
		for (arrayIndex=0; arrayIndex<10; arrayIndex++) {
			int bits = arrayIndex+30;
			BigInteger[] testNumbers = TestsetGenerator.generate(N_COUNT, bits, TestNumberNature.RANDOM_ODD_COMPOSITES);
			LOG.info("Test N having " + bits + " bit");
			for (BigInteger N : testNumbers) {
				this.findSingleFactor(N);
			}
		}
		
		for (int k=1; k<kMax; k++) {
			SortedMultiset<BigInteger> factors = tdiv.factor(BigInteger.valueOf(k));
			LOG.info("k = " + k + " = " + factors + ": successes=" + Arrays.toString(kFactorCounts[k]));
		}
	}

	public static void main(String[] args) {
    	ConfigUtil.initProject();
		// test N with BITS bits
    	Lehman_AnalyzeKStructure testEngine = new Lehman_AnalyzeKStructure();
		testEngine.test();
	}
}
