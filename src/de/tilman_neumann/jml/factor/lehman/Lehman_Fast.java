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

import de.tilman_neumann.jml.factor.FactorAlgorithmBase;
import de.tilman_neumann.jml.gcd.Gcd63;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.jml.factor.lehman.TrialInvFact;

/**
 * Faster implementation of Lehman's factor algorithm.
 * Works flawlessly for N <= 56 bit.
 *
 * This version will do trial division before or after the main loop depending on factorSemiprimes.
 *
 * @authors Tilman Neumann + Thilo Harich
 */
public class Lehman_Fast extends FactorAlgorithmBase {
	private static final Logger LOG = Logger.getLogger(Lehman_Fast.class);

	/** This is a constant that is below 1 for rounding up double values to long. */
	private static final double ROUND_UP_DOUBLE = 0.9999999665;

	private static final TrialInvFact smallFact = new TrialInvFact(1 << 22);

	private static double[] sqrt, sqrtInv;

	static {
		// Precompute sqrts for all possible k. 2^22 entries are enough for N~2^66.
		final int kMax = 1<<22;
		sqrt = new double[kMax + 1];
		sqrtInv = new double[kMax + 1];
		for (int i = 1; i < sqrt.length; i++) {
			final double sqrtI = Math.sqrt(i);
			sqrt[i] = sqrtI;
			sqrtInv[i] = 1.0/sqrtI;
		}
	}

	long N;
	long fourN;
	double sqrt4N;
	boolean factorSemiprimes;
	private final Gcd63 gcdEngine = new Gcd63();

	/**
	 * Only constructor.
	 * @param factorSemiprimes if the number to be factored might be a semiprimes were each factor is greater then n^1/3
	 * then set this to true. The algorithm will always work. But this might have a very positive effect on the performance.
	 */
	public Lehman_Fast(boolean factorSemiprimes) {
		this.factorSemiprimes = factorSemiprimes;
	}

	@Override
	public String getName() {
		return "Lehman_Fast";
	}

	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		return BigInteger.valueOf(findSingleFactor(N.longValue()));
	}

	public long findSingleFactor(long N) {
		this.N = N;
		final int cbrt = (int) Math.cbrt(N);

		long factor;
		smallFact.setMaxFactor(cbrt);
		if (!factorSemiprimes && (factor = smallFact.findFactors(N, null)) != N)
			return factor;

		// limit for must be 0 mod 6, since we also want to search above of it
		final int kLimit = (cbrt + 6) / 6 * 6;
		// For kLimit / 64 the range for a is at most 2, this is what we can ensure
		int kTwoA = (cbrt >> 6);
		// twoA = 0 mod 6
		kTwoA = ((kTwoA + 6)/ 6) * 6;
		fourN = N<<2;
		sqrt4N = Math.sqrt(fourN);
		final double sixthRootTerm = 0.25 * Math.pow(N, 1/6.0); // double precision is required for stability

		// first investigate in solutions a^2 - sqrt(k*n) = y^2 were we only have two possible 'a' values per k
		// but do not go to far, since there we have a lower chance to finda factor
		// here we only inspect k = 6*i since they much more likely have a solution x^2 - sqrt(k*n) = y^2
		// this is we use a multiplier of 6
		factor = lehmanEven(kTwoA, kLimit);
		if (factor>1) return factor;

		// investigate in solution a^2 - sqrt(k*n) = y^2 were we might have more then two solutions 'a'
		for (int k=1 ; k < kTwoA; k++) {
			final double sqrt4kN = sqrt4N * sqrt[k];
			// only use long values
			final long aStart = (long) (sqrt4kN + ROUND_UP_DOUBLE); // much faster than ceil() !
			long aLimit = (long) (sqrt4kN + sixthRootTerm * sqrtInv[k]);
			long aStep;
			if ((k & 1) == 0) {
				// k even -> make sure aLimit is odd
				aLimit |= 1l;
				aStep = 2;
			} else {
				final long kPlusN = k + N;
				if ((kPlusN & 3) == 0) {
					aStep = 8;
					aLimit += ((kPlusN - aLimit) & 7);
				} else {
					aStep = 4;
					aLimit += ((kPlusN - aLimit) & 3);
				}
			}

			// processing the a-loop top-down is faster than bottom-up
			for (long a=aLimit; a >= aStart; a-=aStep) {
				final long test = a*a - k * fourN;
				final long b = (long) Math.sqrt(test);
				if (b*b == test) {
					return gcdEngine.gcd(a+b, N);
				}
			}
		}

		// additional loop for k = 3 mod 6 in the middle range
		// this loop has fewer possible a's, then k = 0 mod 6 and therefore gives less often factors
		if ((factor = lehmanOdd(kTwoA + 3, kLimit)) > 1)
			return factor;

		// continue even loop, because we are looking at very high numbers this now done after the k = 3 mod 6 loop
		if ((factor = lehmanEven(kLimit, kLimit << 1)) > 1)
			return factor;

		// we now have loops for offset 0,3 -> missing 1,2,4,5
		// this code will be executed very rarely, but to be sure we did not miss factors
		// from the lehman argument we have to execute it.
		if ((factor = lehmanOdd(kTwoA + 1, kLimit)) > 1)
			return factor;
		if ((factor = lehmanEven(kTwoA + 2, kLimit)) > 1)
			return factor;
		if ((factor = lehmanEven(kTwoA + 4, kLimit)) > 1)
			return factor;
		if ((factor = lehmanOdd(kTwoA + 5, kLimit)) > 1)
			return factor;

		// Check via trial division whether N has a nontrivial divisor d <= cbrt(N).
		//LOG.debug("entering tdiv...");
		factor = smallFact.findFactors(N, null);
		if (factor>1 && factor<N) return factor;
		
		// if sqrt(4kN) is an exact integer then the previous fast ceil operations may have failed.
		// This can only happen for k%6=1 or k%6=5 and fixing one of both seems to be sufficient.
		for (int k=kTwoA + 1; k <= kLimit; k++) {
			long fourKN = k*N<<2;
			long a = (long) (sqrt4N * sqrt[k] + ROUND_UP_DOUBLE) - 1;
			long test = a*a - fourKN;
			long b = (long) Math.sqrt(test);
			if (b*b == test) {
				long gcd = gcdEngine.gcd(a+b, N);
				if (gcd>1 && gcd<N) {
					return gcd;
				}
			}
	    }
		
		return 0; // fail
	}

	private long lehmanOdd(int kBegin, final int kLimit) {
		for (int k = kBegin; k <= kLimit; k += 6) {
			long a = (long) (sqrt4N * sqrt[k] + ROUND_UP_DOUBLE);
			// for k = 0 mod 6 a must be even and k + n + a = 0 mod 4
			final long kPlusN = k + N;
			if ((kPlusN & 3) == 0) {
				a += ((kPlusN - a) & 7);
			} else
			{
				a += ((kPlusN - a) & 3);
			}
			final long test = a*a - k * fourN;
			final long b = (long) Math.sqrt(test);
			if (b*b == test) {
				return gcdEngine.gcd(a+b, N);
			}
		}
		return -1;
	}

	private long lehmanEven(int kBegin, final int kEnd) {
		for (int k = kBegin ; k <= kEnd; k += 6) {
			// for k = 0 mod 6 a must be odd
			final long a = (long) (sqrt4N * sqrt[k] + ROUND_UP_DOUBLE) | 1;
			final long test = a*a - k * fourN;
			final long b = (long) Math.sqrt(test);
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
				// stuff Lehman_TillSimple3 did not like
				5640012124823L,
				7336014366011L,
				19699548984827L,
				52199161732031L,
				73891306919159L,
				112454098638991L,
				
				// stuff Lehman_TillSimple3_2 did not like
				32427229648727L,
				87008511088033L,
				92295512906873L,
				338719143795073L,
				346425669865991L,
				1058244082458461L,
				1773019201473077L,
				6150742154616377L,

				// stuff Lehman_Fast2, Lehman_Fast3 did not like
				44843649362329L,
				67954151927287L,
				134170056884573L,
				198589283218993L,
				737091621253457L,
				1112268234497993L,
				2986396307326613L,
				
				// stuff Lehman_TillSimple3_3 did not like
				26275638086419L,
				62246008190941L,
				209195243701823L,
				290236682491211L,
				485069046631849L,
				1239671094365611L,
				2815471543494793L,
				5682546780292609L,
			};
		
		Lehman_Fast lehman = new Lehman_Fast(true);
		for (long N : testNumbers) {
			long factor = lehman.findSingleFactor(N);
			LOG.info("N=" + N + " has factor " + factor);
		}
	}
}
