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
 * A variant of Hard_Fast2Mult using Math.fma().
 * Very slow if Math.fma() is not supported by hardware and intrinsics.
 * Support requires an Intel Haswell or AMD Piledriver CPU or later, and typically Java 10+ (depends on the VM).
 * If supported we may see only a minor speedup or none at all on more recent hardware (Ryzen 3900X).
 *
 * @authors Thilo Harich & Tilman Neumann
 */
public class Hart_Fast2Mult_FMA extends FactorAlgorithm {
	private static final Logger LOG = Logger.getLogger(Hart_Fast2Mult_FMA.class);

	// k multipliers.
	private static final long K_MULT1 = 3465;
	private static final long K_MULT2 = 315;

	/** 
	 * Size of arrays: this is around 4*n^1/3.
	 * 2^21 should work for all number n up to 2^52.
	 */
	private static final int I_MAX = 1<<21;

	/** This constant is used for fast rounding of double values to long. */
	private static final double ROUND_UP_DOUBLE = 0.9999999665;

	private final boolean doTDivFirst;
	private final double[] sqrt1;
	private final double[] sqrt2;
	private final TDiv63Inverse tdiv = new TDiv63Inverse(I_MAX);
	private final Gcd63 gcdEngine = new Gcd63();

	/**
	 * Full constructor.
	 * @param doTDivFirst If true then trial division is done before the Lehman loop.
	 * This is recommended if arguments N are known to have factors < cbrt(N) frequently.
	 * With doTDivFirst=false, this implementation is pretty fast for hard semiprimes.
	 * But the smaller possible factors get, it will become slower and slower.
	 */
	public Hart_Fast2Mult_FMA(boolean doTDivFirst) {
		this.doTDivFirst = doTDivFirst;
		// Precompute sqrts for all k < I_MAX
		sqrt1 = new double[I_MAX];
		sqrt2 = new double[I_MAX];
		for (int i=1; i<I_MAX; i++) {
			sqrt1[i] = Math.sqrt(i*K_MULT1);
			if ((i*K_MULT2) % K_MULT1 != 0) {
				sqrt2[i] = Math.sqrt(i*K_MULT2);
			}
		}
	}

	@Override
	public String getName() {
		return "Hart_Fast2Mult_FMA(" + doTDivFirst + ")";
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
		if (doTDivFirst) {
			// do trial division before the Hart loop
			tdiv.setTestLimit((int) Math.cbrt(N));
			final long factor = tdiv.findSingleFactor(N);
			if (factor > 1) return factor;
		}
		
		// test for exact squares
		final double sqrtN = Math.sqrt(N);
		final long floorSqrtN = (long) sqrtN;
		if (floorSqrtN*floorSqrtN == N) return floorSqrtN;

		final long fourN = N<<2;
		final double sqrt4N = sqrtN*2;
		long a, b, test, gcd;
		long k1 = K_MULT1;
		long k2 = K_MULT2;
		try {
			for (int i=1; ; i++, k1 += K_MULT1, k2 += K_MULT2) {
				// using the fusedMultiplyAdd operation defined in IEEE 754-2008 may give a small speedup if supported
				a = adjustA(N, (long) Math.fma(sqrt4N, sqrt1[i], ROUND_UP_DOUBLE), k1);
				test = a*a - k1 * fourN;
				b = (long) Math.sqrt(test);
				if (b*b == test && (gcd = gcdEngine.gcd(a+b, N))>1 && gcd<N) {
					return gcd;
				}
				// the second parallel 45 * i loop gives ~4 % speedup if we
				// avoid that we hit the same values as in the first 315 * i case
				if (sqrt2[i] > Double.MIN_VALUE) {
					a = adjustA(N, (long) Math.fma(sqrt4N, sqrt2[i], ROUND_UP_DOUBLE), k2);
					test = a*a - k2 * fourN;
					b = (long) Math.sqrt(test);
					if (b*b == test && (gcd = gcdEngine.gcd(a+b, N))>1 && gcd<N) {
						return gcd;
					}
				}
			}
		} catch (final ArrayIndexOutOfBoundsException e) {
			LOG.error("Hart_Fast2Mult: Failed to factor N=" + N + ". Either it has factors < cbrt(N) needing trial division, or the arrays are too small.");
			return 1;
		}
	}
	
	/**
	 * Increases x to return the next possible solution for x for x^2 - 4kn = b^2.
	 * Due to performance reasons we give back solutions for this equations modulo a
	 * power of 2, since we can determine the solutions just by additions and binary
	 * operations.
	 *
	 * if k is even x must be odd.
	 * if k*n == 3 mod 4 -> x = k*n+1 mod 8
	 * if k*n == 1 mod 8 -> x = k*n+1 mod 16 or -k*n+1 mod 16
	 * if k*n == 5 mod 8 -> x = k*n+1 mod 32 or -k*n+1 mod 32
	 *
	 * @param N
	 * @param x
	 * @param k
	 * @return
	 */
	private long adjustA(long N, long x, long k) {
		if ((k&1)==0) return x | 1;
		
		final long kNp1 = k*N+1;
		if ((kNp1 & 3) == 0) return x + ((kNp1 - x) & 7);
		
		if ((kNp1 & 7) == 2) {
			final long adjust1 = ( kNp1 - x) & 15;
			final long adjust2 = (-kNp1 - x) & 15;
			return x + (adjust1 < adjust2 ? adjust1 : adjust2);
		}
		
		final long adjust1 = ( kNp1 - x) & 31;
		final long adjust2 = (-kNp1 - x) & 31;
		return x + (adjust1 < adjust2 ? adjust1 : adjust2);
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
				
				// test numbers that required large arrays
				135902052523483L,
				1454149122259871L,
				5963992216323061L,
				26071073737844227L,
				8296707175249091L,
				35688516583284121L,
				//35245060305489557L, // too big for I_MAX
				//107563481071570333L, // too big for I_MAX
				//107326406641253893L, // too big for I_MAX
				//120459770277978457L, // too big for I_MAX
				
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
				16032993843L, // = 3 * 5344331281 (34 bit number), FAILS with doTDivFirst==false
				26036808587L,
				41703657595L, // = 5 * 8340731519 (36 bit number), FAILS with doTDivFirst==false
				68889614021L,
				197397887859L, // = 3^2 * 21933098651 (38 bit number), FAILS with doTDivFirst==false
				
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
				
				// problems found by Thilo
				35184372094495L,
				893, // works
				35, // works
				9, // works
				
				// squares
				100140049,
				10000600009L,
				1000006000009L,
				6250045000081L,
				// with doTDivFirst==false, the following N require an explicit square test
				10890006600001L,
				14062507500001L,
				25000110000121L,
				100000380000361L,
				10000001400000049L,
				1000000014000000049L,
				
				// Previous fails cured with larger arrays
				17977882519205951L, // 54 bit
				57410188984551071L, // 56 bit
				708198179721093877L, // 60 bit
				4085731848127832849L, // 62 bit
				
				// TODO Current fails
				873351084013120721L, // 60 bit
				3608228875180849937L, // 62 bit
				7355428158429213199L, // 63 bit
				7836704265571283783L, // 63 bit
				8940500625246794041L, // 63 bit
				9170754184293724117L, // 63 bit
			};
		
		Hart_Fast2Mult_FMA holf = new Hart_Fast2Mult_FMA(false);
		for (long N : testNumbers) {
			long factor = holf.findSingleFactor(N);
			LOG.info("N=" + N + " has factor " + factor);
		}
	}
}
