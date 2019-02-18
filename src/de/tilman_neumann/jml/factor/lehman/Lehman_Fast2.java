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

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.jml.gcd.Gcd63;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.jml.factor.tdiv.TDiv63Inverse_NoDoubleCheck_Unroll;

/**
 * Fast implementation of Lehman's factor algorithm.
 * Works flawlessly for N up to 60 bit.<br><br>
 * 
 * It is quite surprising that the exact sqrt test of <code>test = a^2 - 4kN</code> works for N >= 45 bit.
 * At that size, both a^2 and 4kN start to overflow Long.MAX_VALUE.
 * But the error - comparing correct results vs. long results - is just the same for both a^2 and 4kN
 * (and a multiple of 2^64).
 *  Thus <code>test</code> is correct and <code>b</code> is correct, too. <code>a</code> is correct anyway.
 * 
 * @authors Tilman Neumann + Thilo Harich
 */
public class Lehman_Fast2 extends FactorAlgorithm {
	private static final Logger LOG = Logger.getLogger(Lehman_Fast2.class);

	/** This is a constant that is below 1 for rounding up double values to long. */
	private static final double ROUND_UP_DOUBLE = 0.9999999665;
	
	private long N;
	private long fourN;
	private double sqrt4N;
	private boolean doTDivFirst;
	private double[] sqrt, sqrtInv;
	private final Gcd63 gcdEngine = new Gcd63();
	private final TDiv63Inverse_NoDoubleCheck_Unroll tdiv = new TDiv63Inverse_NoDoubleCheck_Unroll(1<<20);

	/**
	 * Full constructor.
	 * @param doTDivFirst If true then trial division is done before the Lehman loop.
	 * This is recommended if arguments N are known to have factors < cbrt(N) frequently.
	 */
	public Lehman_Fast2(boolean doTDivFirst) {
		this.doTDivFirst = doTDivFirst;
		// Precompute sqrts for all possible k. 2^20 entries are enough for N~2^60.
		final int kMax = 1<<20;
		sqrt = new double[kMax + 1];
		sqrtInv = new double[kMax + 1];
		for (int i=1; i <= kMax; i++) {
			final double sqrtI = Math.sqrt(i);
			sqrt[i] = sqrtI;
			sqrtInv[i] = 1.0/sqrtI;
		}
	}

	@Override
	public String getName() {
		return "Lehman_Fast2(" + doTDivFirst + ")";
	}

	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		return BigInteger.valueOf(findSingleFactor(N.longValue()));
	}

	public long findSingleFactor(long N) {
		// N==9 would require to check if the gcd is 1 < gcd < N before returning it as a factor
		if (N==9) return 3;
				
		this.N = N;
		final int cbrt = (int) Math.cbrt(N);

		// do trial division before Lehman loop ?
		long factor;
		tdiv.setTestLimit(cbrt);
		if (doTDivFirst && (factor = tdiv.findSingleFactor(N))>1) return factor;

		fourN = N<<2;
		sqrt4N = Math.sqrt(fourN);

		// k%3 is a very good discriminator for the number of factors found by different k.
		// k with k%3==0 have the biggest probability to find a factor, so we want to test them first.
		// Adjusting kTwoA and kLimit to 0 (mod 6) allows us to start with even k in testLongTail(kTwoA).
		final int kLimit = ((cbrt + 6) / 6) * 6;
		// For kTwoA = kLimit / 64 the a-range is <= 2. The congruences mod 4 imply that then there is only 1 choice for a.
		final int kTwoA = (((cbrt >> 6) + 6) / 6) * 6;

		// We are investigating solutions of a^2 - sqrt(k*n) = y^2 in three k-ranges:
		// * The "small range" is 1 <= k < kTwoA, where we may have more than two 'a'-solutions per k.
		//   Thus, an inner 'a'-loop is required.
		// * The "middle range" is kTwoA <= k < kLimit, where we have at most two possible 'a' values per k.
		// * The "high range" is kLimit <= k < 2*kLimit. This range is not required for the correctness
		//   of the algorithm, but investigating it for some k==0 (mod 3) improves performance.
		
		// We start with the middle range case k == 0 (mod 3), which has the highest chance to find a factor.
		if ((factor = testLongTail(kTwoA, kLimit)) > 1) return factor;

		// Now investigate the small range
		final double sixthRootTerm = 0.25 * Math.pow(N, 1/6.0); // double precision is required for stability
		for (int k=1; k < kTwoA; k++) {
			final double sqrt4kN = sqrt4N * sqrt[k];
			final long aStart = (long) (sqrt4kN + ROUND_UP_DOUBLE); // much faster than ceil()
			long aLimit = (long) (sqrt4kN + sixthRootTerm * sqrtInv[k]);
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
					aStep = 4; // XXX use step 8 or 16 ?
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
					return gcdEngine.gcd(a+b, N);
				}
			}
		}

		// Checking the high range for k== 0 (mod 3) improves performance
		if ((factor = testLongTail(kLimit, kLimit << 1)) > 1) return factor;

		// do trial division after Lehman loop ?
		if (!doTDivFirst && (factor = tdiv.findSingleFactor(N))>1) return factor;

		// Complete middle range
		if ((factor = testLongTail(kTwoA + 1, kLimit)) > 1) return factor;
		if ((factor = testLongTail(kTwoA + 2, kLimit)) > 1) return factor;
		
		// If sqrt(4kN) is very near to an exact integer then the fast ceil() in the 'aStart'-computation
		// may have failed. Then we need a "correction loop":
		for (int k=kTwoA + 1; k <= kLimit; k++) {
			long a = (long) (sqrt4N * sqrt[k] + ROUND_UP_DOUBLE) - 1;
			long test = a*a - k*fourN;
			long b = (long) Math.sqrt(test);
			if (b*b == test) {
				return gcdEngine.gcd(a+b, N);
			}
	    }
		
		return 0; // fail
	}

	private long testLongTail(int kBegin, final int kLimit) {
		int k = kBegin;
		if ((k&1) == 0) {
			// k even -> a must be odd
			final long a = (long) (sqrt4N * sqrt[k] + ROUND_UP_DOUBLE) | 1L;
			final long test = a*a - k * fourN;
			final long b = (long) Math.sqrt(test);
			if (b*b == test) {
				return gcdEngine.gcd(a+b, N);
			}
			k += 3;
		}
		for (; k <= kLimit; k += 3) {
			// k odd
			long a = (long) (sqrt4N * sqrt[k] + ROUND_UP_DOUBLE);
			// make a == (k+N) (mod 4, 8, 16)
			final long kPlusN = k + N;
			if ((kPlusN & 3) == 0) {
				a += ((kPlusN - a) & 7);
			} else {
				final long adjust1 = (kPlusN - a) & 15;
				final long adjust2 = (-kPlusN - a) & 15;
				a += adjust1<adjust2 ? adjust1 : adjust2;
			}
			long test = a*a - k * fourN;
			long b = (long) Math.sqrt(test);
			if (b*b == test) {
				return gcdEngine.gcd(a+b, N);
			}
			k += 3;
			
			// k even -> a must be odd
			a = (long) (sqrt4N * sqrt[k] + ROUND_UP_DOUBLE) | 1L;
			test = a*a - k * fourN;
			b = (long) Math.sqrt(test);
			if (b*b == test) {
				return gcdEngine.gcd(a+b, N);
			}
		}
		return -1;
	}
	
	/**
	 * Test.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		ConfigUtil.initProject();

		// These test number were too hard for previous versions:
		long[] testNumbers = new long[] {
				// odd semiprimes
				5640012124823L,
				7336014366011L,
				19699548984827L,
				52199161732031L,
				73891306919159L,
				112454098638991L,
				
				32427229648727L,
				87008511088033L,
				92295512906873L,
				338719143795073L,
				346425669865991L,
				1058244082458461L,
				1773019201473077L,
				6150742154616377L,

				44843649362329L,
				67954151927287L,
				134170056884573L,
				198589283218993L,
				737091621253457L,
				1112268234497993L,
				2986396307326613L,
				
				26275638086419L,
				62246008190941L,
				209195243701823L,
				290236682491211L,
				485069046631849L,
				1239671094365611L,
				2815471543494793L,
				5682546780292609L,
				
				// special case
				9,
			};
		
		Lehman_Fast2 lehman = new Lehman_Fast2(false);
		for (long N : testNumbers) {
			long factor = lehman.findSingleFactor(N);
			LOG.info("N=" + N + " has factor " + factor);
		}
	}
}
