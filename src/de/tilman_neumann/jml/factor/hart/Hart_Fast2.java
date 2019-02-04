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

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.jml.factor.tdiv.TDiv63Inverse;
import de.tilman_neumann.jml.gcd.Gcd63;
import de.tilman_neumann.util.ConfigUtil;

/**
 * Fast implementation of Hart's one line factor algorithm.
 * @see <a href="http://wrap.warwick.ac.uk/54707/">http://wrap.warwick.ac.uk/54707/</a>
 * 
 * Not as good as the Lehman implementation Thilo Harich and me developed, but getting close.
 * Some of the improvements above the simple version:
 * -> work with 4N instead of N
 * -> adjust a-values by congruences mod 4 rsp. mod 8
 * -> sort running over k-values by mod 6 residues
 * -> correction loop
 * 
 * This variant does trial division after the Hart loops. This requires that the gcd is checked
 * to be 1 < gcd < N before returning it as a factor.
 * 
 * @authors Tilman Neumann
 */
public class Hart_Fast2 extends FactorAlgorithm {
	private static final Logger LOG = Logger.getLogger(Hart_Fast2.class);

	/**
	 * The biggest bit length of N supported by the algorithm.
	 * Larger values require a larger sqrt-table, which may be pretty big like 78 mio. doubles for 60 bit numbers.
	 */
	private static final int MAX_N_BITS = 57; // some test numbers have 57 bit
	/** This constant seems sufficient for all N to compute kLimit = N^K_LIMIT_EXP. 0.436 was not sufficient. */
	private static final double K_LIMIT_EXP = 0.437;
	
	/** This constant is used for fast rounding of double values to long. */
	private static final double ROUND_UP_DOUBLE = 0.9999999665;

	private static final TDiv63Inverse tdiv = new TDiv63Inverse(1<<19); // some test numbers have 57 bit

	private static double[] sqrt;

	private long N, fourN;
	private int kLimit;
	private double sqrt4N;
	
	static {
		// Precompute sqrts for all k required for N <= MAX_N_BITS bit.
		final int kMax = (int) Math.pow(2, MAX_N_BITS*K_LIMIT_EXP);
		sqrt = new double[kMax + 1];
		for (int i = 1; i < sqrt.length; i++) {
			final double sqrtI = Math.sqrt(i);
			sqrt[i] = sqrtI;
		}
	}

	private final Gcd63 gcdEngine = new Gcd63();

	@Override
	public String getName() {
		return "Hart_Fast2";
	}

	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		return BigInteger.valueOf(findSingleFactor(N.longValue()));
	}

	public long findSingleFactor(long N) {
		this.N = N;

		fourN = N<<2;
		sqrt4N = Math.sqrt(fourN);
		kLimit = (int) Math.pow(N, K_LIMIT_EXP);
		long factor;
		if ((factor=testEvenK(6)) > 1) return factor;
		if ((factor=testOddK(3)) > 1) return factor;
		if ((factor=testEvenK(2)) > 1) return factor;
		if ((factor=testOddK(5)) > 1) return factor;
		if ((factor=testEvenK(4)) > 1) return factor;
		if ((factor=testOddK(1)) > 1) return factor;
		
		// For some reason, this implementation needs trial division, too (Hart_Simple does not).
		// And it _must_ be carried out before the Hart loops...
		int tdivLimit = (int) (Math.pow(N, 1/3.0));
		tdiv.setTestLimit(tdivLimit);
		if ((factor = tdiv.findSingleFactor(N))>1) return factor;

		// If sqrt(4kN) is very near to an exact integer then the fast ceil() in the 'aStart'-computation
		// may have failed. Then we need a "correction loop":
		for (int k=1; k <= kLimit; k++) {
			long a = (long) (sqrt4N * sqrt[k] + ROUND_UP_DOUBLE) - 1;
			long test = a*a - k*fourN;
			long b = (long) Math.sqrt(test);
			if (b*b == test) {
				return gcdEngine.gcd(a+b, N);
			}
	    }

		return 1; // fail
	}
	
	private long testEvenK(int kStart) {
		for (int k = kStart; k<kLimit; k+=6) {
			// k even -> a must be odd
			long a = (long) (sqrt4N * sqrt[k] + ROUND_UP_DOUBLE) | 1;
			final long test = a*a - k * fourN;
			final long b = (long) Math.sqrt(test);
			if (b*b == test) {
				long gcd = gcdEngine.gcd(a+b, N);
				if (gcd>1 && gcd<N) return gcd;
			}
		}
		return 0;
	}

	private long testOddK(int kStart) {
		for (int k = kStart; k<kLimit; k+=6) {
			// k odd -> a-congruences depend of k+N
			long a = (long) (sqrt4N * sqrt[k] + ROUND_UP_DOUBLE);
			final long kPlusN = k + N;
			if ((kPlusN & 3) == 0) {
				a += ((kPlusN - a) & 7);
			} else {
				a += ((kPlusN - a) & 3);
			}
			final long test = a*a - k * fourN;
			final long b = (long) Math.sqrt(test);
			if (b*b == test) {
				long gcd = gcdEngine.gcd(a+b, N);
				if (gcd>1 && gcd<N) return gcd;
			}
		}
		return 0;
	}
	
	/**
	 * Test.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		ConfigUtil.initProject();

		// These test number were too hard for previous versions:
		long[] testNumbers = new long[] {
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
				
				// test numbers that required large K_LIMIT_EXP values
				135902052523483L, // needs kLimit > N^0.42096; 0.42097 works
				1454149122259871L, // needs kLimit > N^0.421
				5963992216323061L, // needs kLimit > N^0.423
				26071073737844227L, // needs kLimit > N^0.428; 0.4285 works
				8296707175249091L, // kLimit >= N^0.4303 works
				35688516583284121L, // kLimit >= N^0.43 works
				35245060305489557L, // kLimit >= N^0.431 works
				107563481071570333L, // kLimit >= N^0.434 works
				107326406641253893L, // 0.436 works, 0.435 is not enough
				120459770277978457L, // 0.437 works, 0.436 is not enough
				
				// failures with random odd composites
				949443, // = 3 * 11 * 28771
				996433, // = 31 * 32143
				1340465, // = 5 * 7 * 38299
				1979435, // = 5 * 395887
				2514615, // = 3 * 5 * 167641
				5226867, // =  3^2 * 580763
				10518047, // = 61 * 172427
				30783267, // = 3^3 * 1140121
				62230739, // = 67 * 928817
				84836647, // = 7 * 17 * 712913
				94602505,
				258555555,
				436396385,
				612066705,
				2017001503,
				3084734169L,
				6700794123L,
				16032993843L,
				26036808587L,
				41703657595L,
				68889614021L,
				197397887859L
			};
		
		Hart_Fast2 holf = new Hart_Fast2();
		for (long N : testNumbers) {
			long factor = holf.findSingleFactor(N);
			LOG.info("N=" + N + " has factor " + factor);
		}
	}
}
