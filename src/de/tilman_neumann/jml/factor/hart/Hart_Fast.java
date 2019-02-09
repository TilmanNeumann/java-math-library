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
 * Pretty simple yet fast variant of Hart's one line factorizer.
 * 
 * When called with doTDivFirst=false, this variant is marginally slower than Hart_Fast_HardSemiprimes
 * for hard semiprimes, but much better on random composites.
 * 
 * If test numbers are known to be random composites, then doTDivFirst=true will improve performance significantly.
 * 
 * @authors Thilo Harich & Tilman Neumann
 */
public class Hart_Fast extends FactorAlgorithm {
	private static final Logger LOG = Logger.getLogger(Hart_Fast.class);

	/**
	 * The biggest N supported by the algorithm.
	 * Larger values need a larger sqrt-table, which may become pretty big!
	 * Thus it is recommended to reduce this constant to the minimum required.
	 */
	private static final long MAX_N = 1L<<50;
	
	/**
	 * We only test k-values that are multiples of this constant.
	 * Best values for performance are 315, 45, 105, 15 and 3, in that order.
	 */
	private static final int K_MULT = 315;
	
	/**
	 * This constant seems sufficient for all N to compute kLimit = N^K_LIMIT_EXP for all N <= 53 bits.
	 */
	private static final double K_LIMIT_EXP = 0.38;
	
	/** This constant is used for fast rounding of double values to long. */
	private static final double ROUND_UP_DOUBLE = 0.9999999665;

	private static double[] sqrt;

	static {
		// Precompute sqrts for all k required for N <= MAX_N and multiplier K_MULT
		final int iMax = (int) Math.pow(MAX_N, K_LIMIT_EXP);
		sqrt = new double[iMax+1];
		for (int i = 1; i <= iMax; i++) {
			sqrt[i] = Math.sqrt(i*K_MULT);
		}
		System.out.println("Hart_Fast: Initialized sqrt array with " + iMax + " entries");
	}

	private static final TDiv63Inverse tdiv = new TDiv63Inverse((int) Math.cbrt(MAX_N));

	private boolean doTDivFirst;
	private final Gcd63 gcdEngine = new Gcd63();

	/**
	 * Full constructor.
	 * @param doTDivFirst If true then trial division is done before the Lehman loop.
	 * This is recommended if arguments N are known to have factors < cbrt(N) frequently.
	 */
	public Hart_Fast(boolean doTDivFirst) {
		this.doTDivFirst = doTDivFirst;
	}
	
	@Override
	public String getName() {
		return "Hart_Fast(" + doTDivFirst + ")";
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
		long factor;
		if (doTDivFirst) {
			// do trial division before the Hart loop until cbrt(N); great choice for random composites
			// avoid Exceptions when N > MAX_N
			int testLimit = (int) Math.cbrt(N<MAX_N ? N : MAX_N);
			tdiv.setTestLimit(testLimit);
			if ((factor = tdiv.findSingleFactor(N))>1) return factor;
		} else {
			// Hart needs a minimum amount of tdiv for random composites, at least up to primes p <= 2^(NBits-27)/2
			int NBits = 64-Long.numberOfLeadingZeros(N);
			int lowTDivLimit = NBits>30 ? (int) Math.sqrt(1L<<(NBits-27)) : 0;
			tdiv.setTestLimit(lowTDivLimit);
			if ((factor = tdiv.findSingleFactor(N))>1) return factor;
		}
		
		long fourN = N<<2;
		double sqrt4N = Math.sqrt(fourN);
		long a,b,test;
		int k = K_MULT;
		try {
			for (int i=1; ;) {
				// odd k -> adjust a mod 8
				a = (long) (sqrt4N * sqrt[i++] + ROUND_UP_DOUBLE);
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
				a = (long) (sqrt4N * sqrt[i++] + ROUND_UP_DOUBLE) | 1L;
				test = a*a - k * fourN;
				b = (long) Math.sqrt(test);
				if (b*b == test) {
					return gcdEngine.gcd(a+b, N);
				}
				k += K_MULT;
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// should never happen in this implementation; if it does then N > MAX_N
			LOG.error(this.getClass().getSimpleName() + " failed to factor N=" + N + ". Cause: " + e, e);
			return 0;
		}
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
				135902052523483L,
				1454149122259871L,
				5963992216323061L,
				26071073737844227L,
				8296707175249091L,
				35688516583284121L,
				//35245060305489557L, // too big for MAX_N
				//107563481071570333L, // too big for MAX_N
				//107326406641253893L, // too big for MAX_N
				//120459770277978457L, // too big for MAX_N
				
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
				16032993843L, // fine here
				26036808587L,
				41703657595L, // fine here
				68889614021L,
				197397887859L, // fine here
				
				2157195374713L,
				8370014680591L,
				22568765132167L,
				63088136564083L,
				
				// more test numbers with small factors
				// 30 bit
				712869263, // = 89 * 8009767
				386575807, // = 73 * 5295559
				569172749, // = 83 * 6857503
				// 40 bit
				624800360363L, // = 233 * 2681546611
				883246601513L, // = 251 * 3518910763
			};
		
		Hart_Fast holf = new Hart_Fast(false);
		for (long N : testNumbers) {
			long factor = holf.findSingleFactor(N);
			LOG.info("N=" + N + " has factor " + factor);
		}
	}
}
