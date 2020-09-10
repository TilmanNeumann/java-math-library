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

import static de.tilman_neumann.jml.base.BigIntConstants.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.gcd.Gcd63;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.jml.factor.TestsetGenerator;
import de.tilman_neumann.jml.factor.TestNumberNature;

/**
 * Analyze the moduli of a-values that help the Lehman algorithm to find factors.
 * 
 * Congruences a == kN (mod 2^s) are slightly more discriminative
 * than Lehman's original congruences a == (k+N) (mod 2^s), s = 1, 2, 3, ...
 * 
 * @author Tilman Neumann
 */
public class Lehman_AnalyzeCongruences2 {
	private static final Logger LOG = Logger.getLogger(Lehman_AnalyzeCongruences2.class);
	
	private static final boolean DEBUG = false;
	
	/** Use congruences a==kN mod 2^s if true, congruences a==(k+N) mod 2^s if false */
	private static final boolean USE_kN_CONGRUENCES = true;

	/** number of test numbers */
	private static final int N_COUNT = 1000000;
	/** the bit size of N to start with */
	private static final int START_BITS = 30;
	/** the increment in bit size from test set to test set */
	private static final int INCR_BITS = 1;
	/** maximum number of bits to test (no maximum if null) */
	private static final Integer MAX_BITS = 63;

	private static final int KMOD = 6;
	private static final int KNMOD = 8;
	private static final int AMOD = 8; // AMOD > KNMOD makes no sense ?

	private final Gcd63 gcdEngine = new Gcd63();
	
	// dimensions: k%KMOD, kN%KNMOD, a%AMOD, adjust%AMOD
	private int[][][][] counts;
	
	public long findSingleFactor(long N) {
		int cbrt = (int) Math.ceil(Math.cbrt(N));
		double sixthRoot = Math.pow(N, 1/6.0); // double precision is required for stability
		for (int k=1; k <= cbrt; k++) {
			long fourKN = k*N<<2;
			double fourSqrtK = Math.sqrt(k<<4);
			long sqrt4kN = (long) Math.ceil(Math.sqrt(fourKN));
			long limit = (long) (sqrt4kN + sixthRoot / fourSqrtK);
			for (long a0 = sqrt4kN; a0 <= limit; a0++) {
				for (int adjust=0; adjust<AMOD; adjust++) {
					long a = a0 + adjust;
					final long test = a*a - fourKN;
					final long b = (long) Math.sqrt(test);
					if (b*b == test) {
						long gcd = gcdEngine.gcd(a+b, N);
						if (gcd>1 && gcd<N) {
							if (USE_kN_CONGRUENCES) {
								counts[k%KMOD][(int)((k*N)%KNMOD)][(int)(a0%AMOD)][adjust]++;
							} else {
								counts[k%KMOD][(int)((k+N)%KNMOD)][(int)(a0%AMOD)][adjust]++;
							}
							return gcd; // removes the blur at even k!
						}
					}
				}
			}
	    }
		
		return 0; // Fail
	}
	
	private void testRange(int bits) {
		counts = new int[KMOD][KNMOD][AMOD][AMOD];
		
		BigInteger N_min = I_1.shiftLeft(bits-1);
		BigInteger[] testNumbers = TestsetGenerator.generate(N_COUNT, bits, TestNumberNature.MODERATE_SEMIPRIMES);
		LOG.info("Test N with " + bits + " bits, i.e. N >= " + N_min);
		
		for (BigInteger N : testNumbers) {
			if (N.mod(I_6).equals(I_1))
			this.findSingleFactor(N.longValue());
		}
		
		String kNStr = USE_kN_CONGRUENCES ? "kN" : "k+N";
		for (int k=0; k<KMOD; k++) {
			for (int kN=0; kN<KNMOD; kN++) {
				int[][] a0_adjust_counts = counts[k][kN];
				if (DEBUG) {
					for (int a0=0; a0<AMOD; a0++) {
						LOG.info("Successful adjusts for k%" + KMOD + "=" + k + ", (" + kNStr + ")%" + KNMOD + "=" + kN + ", a0%" + AMOD + "=" + a0 + ": " + Arrays.toString(a0_adjust_counts[a0]));
					}
					LOG.info("");
				}
				
				// a0_adjust_counts[][] contains the counts of (a0, adjust) pairs that led to successful factorizations.
				// An antidiagonal of that table means that a0 + adjust is fixed, i.e. each antidiagonal identifies a
				// particular a == (a0 + adjust) % AMOD !
				int knkCount = 0;
				List<Integer> aList = new ArrayList<>();
				for (int a=0; a<AMOD; a++) {
					int cnt = computeAntiDiagonalCount(a0_adjust_counts, a);
					if (cnt > 0) {
						knkCount += cnt;
						aList.add(a);
					}
				}
				if (knkCount > 0) {
					int avgAntidiagonalSuccesses = knkCount/aList.size(); // avg. factoring successes per antidiagonal
					LOG.info("k%" + KMOD + "=" + k + ", kN%" + KNMOD + "=" + kN + ": successful a = " + aList + " (mod " + AMOD + "), avg hits = " + avgAntidiagonalSuccesses);
				}
			}
		}
		LOG.info("");
	}

	/**
	 * Compute the sum of the antidiagonal entries of array[i][j] with i+j (mod AMOD) == a.
	 * @param array
	 * @param a
	 * @return sum of the antidiagonal given by 'a'
	 */
	private int computeAntiDiagonalCount(int[][] array, int a) {
		int result = 0;
		for (int i=0; i<AMOD; i++) {
			int adjust = a-i;
			if (adjust<0) adjust += AMOD;
			result += array[i][adjust];
		}
		return result;
	}
	
	public static void main(String[] args) {
    	ConfigUtil.initProject();
		int bits = START_BITS;
		while (true) {
			// test N with the given number of bits, i.e. 2^(bits-1) <= N <= (2^bits)-1
	    	Lehman_AnalyzeCongruences2 testEngine = new Lehman_AnalyzeCongruences2();
			testEngine.testRange(bits);
			bits += INCR_BITS;
			if (MAX_BITS!=null && bits > MAX_BITS) break;
		}
	}
}
