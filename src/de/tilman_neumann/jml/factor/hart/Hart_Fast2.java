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

import java.math.BigInteger;

import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.jml.factor.tdiv.TDiv63Inverse;
import de.tilman_neumann.jml.gcd.Gcd63;

/**
 * Pretty simple yet fast variant of Hart's one line factorizer.
 * 
 * This version does not _need_ trial division, but it improves performance for test numbers having small
 * factors frequently.
 * 
 * @authors Thilo Harich & Tilman Neumann
 */
public class Hart_Fast2 extends FactorAlgorithm {
	/**
	 * The biggest N supported by the algorithm.
	 * Larger values need a larger sqrt-table, which may become pretty big!
	 * Thus it is recommended to reduce this constant to the minimum required.
	 */
	private static final long MAX_N = 1L<<50;
	
	/**
	 * We only test k-values that are multiples of this constant.
	 * Best values for performance are 45, 105, 15 and 3, in that order.
	 * The size of the sqrt array depends proportionally on this constant, so do not choose it too big.
	 */
	private static final int K_MULT = 45;
	
	/** This constant seems sufficient for all N to compute kLimit = N^K_LIMIT_EXP. 0.436 was not sufficient. */
	private static final double K_LIMIT_EXP = 0.38;
	
	/** This constant is used for fast rounding of double values to long. */
	private static final double ROUND_UP_DOUBLE = 0.9999999665;

	private static double[] sqrt;

	static {
		// Precompute sqrts for all k required for N <= MAX_N and multiplier K_MULT
		final int kMax = K_MULT * (int) Math.pow(MAX_N, K_LIMIT_EXP);
		sqrt = new double[kMax + 1];
		for (int i = 1; i <= kMax; i++) {
			sqrt[i] = Math.sqrt(i);
		}
		System.out.println("Hart_Fast3: Initialized sqrt array with " + kMax + " entries");
	}

	private static final TDiv63Inverse tdiv = new TDiv63Inverse((int) Math.cbrt(MAX_N));

	private boolean doTDivFirst;
	private final Gcd63 gcdEngine = new Gcd63();

	/**
	 * Full constructor.
	 * @param doTDivFirst If true then trial division is done before the Lehman loop.
	 * This is recommended if arguments N are known to have factors < cbrt(N) frequently.
	 */
	public Hart_Fast2(boolean doTDivFirst) {
		this.doTDivFirst = doTDivFirst;
	}
	
	@Override
	public String getName() {
		return "Hart_Fast2(" + doTDivFirst + ")";
	}

	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		return BigInteger.valueOf(findSingleFactor(N.longValue()));
	}

	/**
	 * Find a factor of long N.
	 * @param N
	 * @return factor of N
	 */
	public long findSingleFactor(long N) {
		// do trial division before the Hart loop ?
		long factor;
		if (doTDivFirst) {
			tdiv.setTestLimit((int) Math.cbrt(N));
			if ((factor = tdiv.findSingleFactor(N))>1) return factor;
		}
		
		long fourN = N<<2;
		double sqrt4N = Math.sqrt(fourN);
		long a,b,test;
		int k = K_MULT;
		for (; ;) {
			// odd k -> adjust a mod 8
			a = (long) (sqrt4N * sqrt[k] + ROUND_UP_DOUBLE);
			final long kPlusN = k + N;
			if ((kPlusN & 3) == 0) {
				a += ((kPlusN - a) & 7);
			} else {
				a += ((kPlusN - a) & 3);
			}
			test = a*a - k * fourN;
			b = (long) Math.sqrt(test);
			if (b*b == test) {
				return gcdEngine.gcd(a+b, N);
			}
			k += K_MULT;
			
			// even k -> a must be odd
			a = (long) (sqrt4N * sqrt[k] + ROUND_UP_DOUBLE) | 1L;
			test = a*a - k * fourN;
			b = (long) Math.sqrt(test);
			if (b*b == test) {
				return gcdEngine.gcd(a+b, N);
			}
			k += K_MULT;
		}
	}
}
