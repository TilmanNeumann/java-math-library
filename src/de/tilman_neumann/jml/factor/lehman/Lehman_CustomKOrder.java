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
import java.util.Map;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.BinarySearch;
import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.jml.gcd.Gcd63;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;
import de.tilman_neumann.jml.factor.tdiv.TDiv63Inverse;

import static de.tilman_neumann.jml.base.BigIntConstants.*;

/**
 * A variant of Lehman's algorithm that allows to arrange the k's in arrays of different priorities.
 * Testing multiples of 15 first, followed by multiples of 3, then the rest works not so bad...
 * 
 * @authors Tilman Neumann + Thilo Harich
 */
public class Lehman_CustomKOrder extends FactorAlgorithm {
	private static final Logger LOG = Logger.getLogger(Lehman_CustomKOrder.class);

	/** This is a constant that is below 1 for rounding up double values to long. */
	private static final double ROUND_UP_DOUBLE = 0.9999999665;

	private static final int K_MAX = 1<<20;
	
	private static final TDiv63Inverse tdiv = new TDiv63Inverse(K_MAX);
	private static final BinarySearch binarySearch = new BinarySearch();

	private static final double[][] sqrts = new double[3][K_MAX+1];
	private static final double[][] sqrtInvs = new double[3][K_MAX+1];
	private static final int[][] kArrays = new int[3][K_MAX+1];
	private static int[] counts = new int[3];

	static {
		addToArray(1, 2);
		for (int k = 2; k <= K_MAX; k++) {
			SortedMultiset<BigInteger> factors = tdiv.factor(BigInteger.valueOf(k));
			if (factors.get(I_3)!=null && factors.get(I_5)!=null) {
				// multiples of 15
				addToArray(k, 0);
			} else if (factors.get(I_3)!=null) {
				addToArray(k, 1);
			} else {
				addToArray(k, 2);
			}
		}
	}

	private static void addToArray(int k, int arrayIndex) {
		final double sqrtK = Math.sqrt(k);
		int count = counts[arrayIndex];
		kArrays[arrayIndex][count] = k;
		sqrts[arrayIndex][count] = sqrtK;
		sqrtInvs[arrayIndex][count] = 1.0/sqrtK;
		counts[arrayIndex]++;
	}

	private static boolean isSquare(SortedMultiset<BigInteger> factors) {
		for (Map.Entry<BigInteger, Integer> entry : factors.entrySet()) {
			int mul = entry.getValue();
			if ((mul&1)==1) return false;
		}
		return true;
	}

	private static boolean isSquareFree(SortedMultiset<BigInteger> factors) {
		for (Map.Entry<BigInteger, Integer> entry : factors.entrySet()) {
			int mul = entry.getValue();
			if ((mul&1)==0) return false;
		}
		return true;
	}

	private long N;
	private long fourN;
	private double sqrt4N;
	private boolean doTDivFirst;
	private final Gcd63 gcdEngine = new Gcd63();
	
	/**
	 * Full constructor.
	 * @param doTDivFirst If true then trial division is done before the Lehman loop.
	 * This is recommended if arguments N are known to have factors < cbrt(N) frequently.
	 */
	public Lehman_CustomKOrder(boolean doTDivFirst) {
		this.doTDivFirst = doTDivFirst;
	}

	@Override
	public String getName() {
		return "Lehman_CustomKOrder(" + doTDivFirst + ")";
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

		// kLimit must be 0 mod 6, since we also want to search above of it
		final int kLimit = ((cbrt + 6) / 6) * 6;
		// For kTwoA = kLimit / 64 the range for a is at most 2. We make it 0 mod 6, too.
		final int kTwoA = (((cbrt >> 6) + 6) / 6) * 6;
		
		// We are investigating solutions of a^2 - sqrt(k*n) = y^2 in two k-ranges:
		// * The "small range" is 1 <= k < kTwoA, where we may have more than two 'a'-solutions per k.
		//   Thus, an inner 'a'-loop is required.
		// * The "big range" is kTwoA <= k < kLimit, where we have at most two possible 'a' values per k.

		int[] iTwoAs = new int[3];
		int[] iLimits = new int[3];
		for (int i=0; i<3; i++) {
			iTwoAs[i] = binarySearch.getInsertPosition(kArrays[i], counts[i], kTwoA);
			iLimits[i] = binarySearch.getInsertPosition(kArrays[i], counts[i], kLimit);
			if (counts[i]<iLimits[i]) iLimits[i] = counts[i];
		}

		// start with big range for multiples of 15 and 3
		if ((factor = testBig(iTwoAs[0], iLimits[0], kArrays[0], sqrts[0])) > 1) return factor;
		if ((factor = testBig(iTwoAs[1], iLimits[1], kArrays[1], sqrts[1])) > 1) return factor;
		
		// small range for multiples of 15 and 3
		final double sixthRootTerm = 0.25 * Math.pow(N, 1/6.0); // double precision is required for stability
		if ((factor = testSmall(iTwoAs[0], kArrays[0], sqrts[0], sqrtInvs[0], sixthRootTerm)) > 1) return factor;
		if ((factor = testSmall(iTwoAs[1], kArrays[1], sqrts[1], sqrtInvs[1], sixthRootTerm)) > 1) return factor;

		// do trial division now?
		if (!doTDivFirst && (factor = tdiv.findSingleFactor(N))>1) return factor;

		// finish Lehman loops
		if ((factor = testBig(iTwoAs[2], iLimits[2], kArrays[2], sqrts[2])) > 1) return factor;
		if ((factor = testSmall(iTwoAs[2], kArrays[2], sqrts[2], sqrtInvs[2], sixthRootTerm)) > 1) return factor;
		
		// If sqrt(4kN) is very near to an exact integer then the fast ceil() in the 'aStart'-computation
		// may have failed. Then we need a "correction loop":
		if ((factor = correctionLoop(iLimits[0], kArrays[0], sqrts[0])) > 1) return factor;
		if ((factor = correctionLoop(iLimits[1], kArrays[1], sqrts[1])) > 1) return factor;
		if ((factor = correctionLoop(iLimits[2], kArrays[2], sqrts[2])) > 1) return factor;
		
		return 1; // fail
	}

	private long testSmall(int iTwoA, int[] kArray, double[] sqrts, double[] sqrtInvs, double sixthRootTerm) {
		long aLimit, aStart, aStep;
		int k;
		for (int i=0; i<iTwoA; i++) {
			k = kArray[i];
			final double sqrt4kN = sqrt4N * sqrts[i];
			aStart = (long) (sqrt4kN + ROUND_UP_DOUBLE); // much faster than ceil() !
			aLimit = (long) (sqrt4kN + sixthRootTerm * sqrtInvs[i]);
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
					return gcdEngine.gcd(a+b, N);
				}
			}
		}
		return 1;
	}

	private long testBig(int iTwoA, int iLimit, int[] kArray, double[] sqrts) {
		int k;
		for (int i=iTwoA; i<iLimit; i++) {
			k = kArray[i];
			long a = (long) (sqrt4N * sqrts[i] + ROUND_UP_DOUBLE);
			if ((k & 1) == 0) {
				// k even -> make sure aLimit is odd
				a |= 1L;
			} else {
				final long kPlusN = k + N;
				if ((kPlusN & 3) == 0) {
					a += ((kPlusN - a) & 7);
				} else {
					final long adjust1 = (kPlusN - a) & 15;
					final long adjust2 = (-kPlusN - a) & 15;
					a += adjust1<adjust2 ? adjust1 : adjust2;
				}
			}
			final long test = a*a - k * fourN;
			final long b = (long) Math.sqrt(test);
			if (b*b == test) {
				return gcdEngine.gcd(a+b, N);
			}
		}
		return 1;
	}
	
	private long correctionLoop(int iLimit, int[] kArray, double[] sqrts) {
		int i=0, k;
		for (; i<iLimit; i++) {
			k = kArray[i];
			long a = (long) (sqrt4N * sqrts[i] + ROUND_UP_DOUBLE) - 1;
			long test = a*a - k*fourN;
			long b = (long) Math.sqrt(test);
			if (b*b == test) {
				return gcdEngine.gcd(a+b, N);
			}
	    }
		return 1;
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
		
		Lehman_CustomKOrder lehman = new Lehman_CustomKOrder(false);
		for (long N : testNumbers) {
			long factor = lehman.findSingleFactor(N);
			LOG.info("N=" + N + " has factor " + factor);
		}
	}
}
