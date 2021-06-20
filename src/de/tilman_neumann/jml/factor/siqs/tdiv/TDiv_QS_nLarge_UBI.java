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
package de.tilman_neumann.jml.factor.siqs.tdiv;

import static de.tilman_neumann.jml.factor.base.GlobalFactoringOptions.*;
import static de.tilman_neumann.jml.base.BigIntConstants.*;
import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.base.UnsignedBigInt;
import de.tilman_neumann.jml.factor.base.SortedIntegerArray;
import de.tilman_neumann.jml.factor.base.SortedLongArray;
import de.tilman_neumann.jml.factor.base.congruence.AQPair;
import de.tilman_neumann.jml.factor.base.congruence.AQPairFactory;
import de.tilman_neumann.jml.factor.base.congruence.Smooth_Perfect;
import de.tilman_neumann.jml.factor.hart.Hart_TDiv_Race;
import de.tilman_neumann.jml.factor.pollardRho.PollardRhoBrentMontgomery64;
import de.tilman_neumann.jml.factor.pollardRho.PollardRhoBrentMontgomeryR64Mul63;
import de.tilman_neumann.jml.factor.siqs.SIQS_Small;
import de.tilman_neumann.jml.factor.siqs.data.SolutionArrays;
import de.tilman_neumann.jml.factor.siqs.poly.SIQSPolyGenerator;
import de.tilman_neumann.jml.factor.siqs.sieve.SmoothCandidate;
import de.tilman_neumann.jml.primes.probable.PrPTest;
import de.tilman_neumann.util.Multiset;
import de.tilman_neumann.util.SortedMultiset;
import de.tilman_neumann.util.SortedMultiset_BottomUp;
import de.tilman_neumann.util.Timer;

/**
 * A trial division engine where partials can have several large factors.
 * 
 * Division is carried out in two stages:
 * Stage 1 identifies prime factors of Q, applying long-valued Barrett reduction
 * Stage 2 does the actual division using UnsignedBigInt; this way less intermediate objects are created.
 * 
 * Faster than 1Large for approximately N>=220 bit.
 * 
 * @author Tilman Neumann
 */
// TODO PSIQS does not work correctly with 3LP relations yet.
// Up to 1917309933207159193848096265773632935599188083482002960344787838591765195842266925748265126622930670869363 (350 bit) everything seems to work fine,
// and indeed already 1605 smooth relations involving 3-partials were found on that test number.
// But for larger numbers, it seems that PSIQS does not terminate at all or needs much more time than expected. This happened for
// * N = 2066866710502282532505449833332089238312935374491943311610655981052809337971646998610592281303116906350078621 (360 bit)
//       factoring run canceled after 4h:20m (expected was ~ 2h:35m)
// * N = 2329368635231975676293761934643288685426331453276223657636089483987557954943374857353742871103536418615759037457 (370 bit)
//       factoring run canceled after ~16h (expected was ~ 5h:30m)
// This happened regardless of the solver used (BlockKanczos or PGauss), thus something seems to be wrong with the relations created, or even during the process of creating the relations.
// I didn't investigate the issue any further yet.
public class TDiv_QS_nLarge_UBI implements TDiv_QS {
	private static final Logger LOG = Logger.getLogger(TDiv_QS_nLarge_UBI.class);
	private static final boolean DEBUG = false;

	// factor argument and polynomial parameters
	private BigInteger kN;
	private BigInteger da; // d*a with d = 1 or 2 depending on kN % 8
	private int d; // the d-value;

	/** Q is sufficiently smooth if the unfactored Q_rest is smaller than this bound depending on N */
	private double smoothBound;

	// prime base
	private int[] primes;
	private int[] exponents;
	private int[] pArray;
	private long[] pinvArrayL;
	private int baseSize;
	private int pMax;
	private BigInteger pMaxSquare;
	private int[] unsievedBaseElements;

	/** buffers for trial division engine. */
	private UnsignedBigInt Q_rest_UBI = new UnsignedBigInt(new int[50]);
	private UnsignedBigInt quotient_UBI = new UnsignedBigInt(new int[50]);

	/** the indices of the primes found to divide Q in pass 1 */
	private int[] pass2Primes = new int[100];
	private int[] pass2Powers = new int[100];
	private int[] pass2Exponents = new int[100];

	private PrPTest prpTest = new PrPTest();
	
	private Hart_TDiv_Race hart = new Hart_TDiv_Race();
	private PollardRhoBrentMontgomeryR64Mul63 pollardRhoR64Mul63 = new PollardRhoBrentMontgomeryR64Mul63();
	private PollardRhoBrentMontgomery64 pollardRho64 = new PollardRhoBrentMontgomery64();
	// Nested SIQS is required only for approximately N>310 bit.
	private SIQS_Small qsInternal;
	
	// smallest solutions of Q(x) == A(x)^2 (mod p)
	private int[] x1Array, x2Array;

	// result: two arrays that are reused, their content is _copied_ to AQ-pairs
	private SortedIntegerArray smallFactors = new SortedIntegerArray();
	private SortedLongArray bigFactors = new SortedLongArray();
	private AQPairFactory aqPairFactory = new AQPairFactory();

	// statistics
	private Timer timer = new Timer();
	private long testCount, sufficientSmoothCount;
	private long aqDuration, pass1Duration, pass2Duration, primeTestDuration, factorDuration;
	private Multiset<Integer> qRestSizes;

	/**
	 * Full constructor.
	 * @param permitUnsafeUsage if true then SIQS_Small (which is used for N > 310 bit to factor Q-rests) uses a sieve exploiting sun.misc.Unsafe features.
	 */
	public TDiv_QS_nLarge_UBI(boolean permitUnsafeUsage) {
		qsInternal = new SIQS_Small(0.305F, 0.37F, null, new SIQSPolyGenerator(), 10, permitUnsafeUsage);
	}

	@Override
	public String getName() {
		return "TDiv_nL_UBI";
	}

	@Override
	public void initializeForN(double N_dbl, BigInteger kN, double smoothBound) {
		// the biggest unfactored rest where some Q is considered smooth enough for a congruence.
		this.smoothBound = smoothBound;
		if (DEBUG) LOG.debug("smoothBound = " + smoothBound + " (" + (64 - Long.numberOfLeadingZeros((long)smoothBound)) + " bits)");
		this.kN = kN;
		// statistics
		if (ANALYZE) testCount = sufficientSmoothCount = 0;
		if (ANALYZE) aqDuration = pass1Duration = pass2Duration = primeTestDuration = factorDuration = 0;
		if (ANALYZE_LARGE_FACTOR_SIZES) qRestSizes = new SortedMultiset_BottomUp<>();
	}

	@Override
	public void initializeForAParameter(BigInteger da, int d, BigInteger b, SolutionArrays solutionArrays, int filteredBaseSize, int[] unsievedBaseElements) {
		this.da = da;
		this.d = d;
		primes = solutionArrays.primes;
		exponents = solutionArrays.exponents;
		pArray = solutionArrays.pArray;
		pinvArrayL = solutionArrays.pinvArrayL;
		baseSize = filteredBaseSize;
		x1Array = solutionArrays.x1Array;
		x2Array = solutionArrays.x2Array;
		pMax = primes[baseSize-1];
		pMaxSquare = BigInteger.valueOf(pMax * (long) pMax);
		this.unsievedBaseElements = unsievedBaseElements;
	}

	@Override
	public List<AQPair> testList(List<SmoothCandidate> smoothCandidates) {
		if (ANALYZE) timer.capture();

		// do trial division with sieve result
		ArrayList<AQPair> aqPairs = new ArrayList<AQPair>();
		for (SmoothCandidate smoothCandidate : smoothCandidates) {
			int x = smoothCandidate.x;
			BigInteger A = smoothCandidate.A;
			BigInteger Qdiva = smoothCandidate.QdivA;
			smallFactors.reset();
			bigFactors.reset();
			if (ANALYZE) {
				testCount++;
				aqDuration += timer.capture();
			}
			
			// Find factorization of Q(x) = A(x)^2 - kN. But the complete Q(x) is not required here,
			// using the smaller Q(x)/da = da*x^2 + 2bx + c instead speeds up tdiv pass 2. 
			// Note that test finds all factors of Q(x) nonetheless.
			// Note also that unlike in MPQS, in SIQS we cannot continue working with Q(x)/da in later stages, because da is not a square
			// and thus we could not combine relations from different a-parameters.
			AQPair aqPair = test(A, Qdiva, x);
			if (ANALYZE) factorDuration += timer.capture();
			
			if (aqPair != null) {
				// Q(x) was found sufficiently smooth to be considered a (partial) congruence
				aqPairs.add(aqPair);
				if (ANALYZE) sufficientSmoothCount++;
				if (DEBUG) {
					LOG.debug("Found congruence " + aqPair);
					BigInteger Q = A.multiply(A).subtract(kN); // Q(x) = A(x)^2 - kN
					assertEquals(Q, Qdiva.multiply(da));
					assertEquals(A.multiply(A).mod(kN), Q.mod(kN));
					// make sure that the product of factors gives Q
					SortedMultiset<Long> allQFactors = aqPair.getAllQFactors();
					BigInteger testProduct = I_1;
					for (Map.Entry<Long, Integer> entry : allQFactors.entrySet()) {
						BigInteger prime = BigInteger.valueOf(entry.getKey());
						int exponent = entry.getValue();
						testProduct = testProduct.multiply(prime.pow(exponent));
					}
					assertEquals(Q, testProduct);
				}
			}
		}
		if (ANALYZE) aqDuration += timer.capture();
		return aqPairs;
	}
	
	private AQPair test(BigInteger A, BigInteger Q, int x) {
		// sign
		BigInteger Q_rest = Q;
		if (Q.signum() < 0) {
			smallFactors.add(-1);
			Q_rest = Q.negate();
		}
		
		// Remove multiples of 2
		int lsb = Q_rest.getLowestSetBit();
		if (lsb > 0) {
			smallFactors.add(2, (short)lsb);
			Q_rest = Q_rest.shiftRight(lsb);
		}

		// Unsieved prime base elements are added directly to pass 2.
		int pass2Count = 0;
		for (; pass2Count<unsievedBaseElements.length; pass2Count++) {
			pass2Primes[pass2Count] = unsievedBaseElements[pass2Count];
			pass2Powers[pass2Count] = unsievedBaseElements[pass2Count];
			pass2Exponents[pass2Count] = 1;
		}
		
		// Pass 1: Test solution arrays.
		// IMPORTANT: Java gives x % p = x for |x| < p, and we have many p bigger than any sieve array entry.
		// IMPORTANT: Not computing the modulus in these cases improves performance by almost factor 2!
		final int xAbs = x<0 ? -x : x;
		for (int pIndex = baseSize-1; pIndex > 0; pIndex--) { // p[0]=2 was already tested
			int p = pArray[pIndex];
			int xModP;
			if (xAbs<p) {
				xModP = x<0 ? x+p : x;
			} else {
				// Compute x%p using long-valued Barrett reduction, see https://en.wikipedia.org/wiki/Barrett_reduction.
				// We can use the long-variant here because x*m will never overflow positive long values.
				final long m = pinvArrayL[pIndex];
				final long q = ((x*m)>>>32);
				xModP = (int) (x - q * p);
				if (xModP<0) xModP += p;
				else if (xModP>=p) xModP -= p;
				if (DEBUG) {
					assertTrue(0<=xModP && xModP<p);
					int xModP2 = x % p;
					if (xModP2<0) xModP2 += p;
					if (xModP != xModP2) LOG.debug("x=" + x + ", p=" + p + ": xModP=" + xModP + ", but xModP2=" + xModP2);
					assertEquals(xModP2, xModP);
				}
			}
			if (xModP==x1Array[pIndex] || xModP==x2Array[pIndex]) {
				pass2Primes[pass2Count] = primes[pIndex];
				pass2Exponents[pass2Count] = exponents[pIndex];
				pass2Powers[pass2Count++] = p;
				// for some reasons I do not understand it is faster to divide Q by p in pass 2 only, not here
			}
		}
		if (ANALYZE) pass1Duration += timer.capture();

		// Pass 2: Reduce Q by the pass2Primes and collect small factors
		Q_rest_UBI.set(Q_rest);
		for (int pass2Index = 0; pass2Index < pass2Count; pass2Index++) {
			int p = pass2Powers[pass2Index];
			int rem;
			while ((rem = Q_rest_UBI.divideAndRemainder(p, quotient_UBI)) == 0) {
				// the division was exact. assign quotient to Q_rest and add p to factors
				UnsignedBigInt tmp = Q_rest_UBI;
				Q_rest_UBI = quotient_UBI;
				quotient_UBI = tmp;
				smallFactors.add(pass2Primes[pass2Index], (short)pass2Exponents[pass2Index]);
				if (DEBUG) {
					BigInteger pBig = BigInteger.valueOf(p);
					BigInteger[] div = Q_rest.divideAndRemainder(pBig);
					assertEquals(div[1].intValue(), rem);
					Q_rest = div[0];
				}
			}
		}
		if (ANALYZE) pass2Duration += timer.capture();
		if (Q_rest_UBI.isOne()) {
			addCommonFactorsToSmallFactors();
			return new Smooth_Perfect(A, smallFactors);
		}
		Q_rest = Q_rest_UBI.toBigInteger();
		
		// Division by all p<=pMax was not sufficient to factor Q completely.
		// The remaining Q_rest is either a prime > pMax, or a composite > pMax^2.
		if (Q_rest.doubleValue() >= smoothBound) return null; // Q is not sufficiently smooth
		
		// now we consider Q as sufficiently smooth. then we want to know all prime factors, as long as we do not find one that is too big to be useful.
		if (DEBUG) LOG.debug("test(): pMax=" + pMax + " < Q_rest=" + Q_rest + " < smoothBound=" + smoothBound + " -> resolve all factors");
		boolean isSmooth = factor_recurrent(Q_rest);
		if (DEBUG) if (bigFactors.size()>2) LOG.debug("Found " + bigFactors.size() + " distinct big factors!"); // 3LP start at ~330 bit with current settings
		if (isSmooth) {
			addCommonFactorsToSmallFactors();
			return aqPairFactory.create(A, smallFactors, bigFactors);
		}
		return null;
	}

	private boolean factor_recurrent(BigInteger Q_rest) {
		// Here we need a prime test, because factor algorithms may not return when called with a prime argument.
		boolean restIsPrime = Q_rest.compareTo(pMaxSquare)<0 || prpTest.isProbablePrime(Q_rest);
		if (ANALYZE) primeTestDuration += timer.capture();
		if (restIsPrime) {
			// Check that the simple prime test using pMaxSquare is correct
			if (DEBUG) assertTrue(prpTest.isProbablePrime(Q_rest));
			if (Q_rest.bitLength() > 31) return false;
			bigFactors.add(Q_rest.longValue());
			return true;
		} // else: Q_rest is surely not prime
		
		// Find a factor of Q_rest, where Q_rest is odd and has two+ factors, each greater than pMax.
		// This starts to happen at N >= 200 bit where we have pMax ~ 17 bit, thus Q_rest >= 34 bit
		// -> trial division is no help here.
		BigInteger factor1;
		int Q_rest_bits = Q_rest.bitLength();
		if (ANALYZE_LARGE_FACTOR_SIZES) qRestSizes.add(Q_rest_bits);
		if (Q_rest_bits<50) {
			if (DEBUG) LOG.debug("factor_recurrent(): pMax^2 = " + pMaxSquare + ", Q_rest = " + Q_rest + " (" + Q_rest_bits + " bits) not prime -> use hart");
			factor1 = hart.findSingleFactor(Q_rest);
		} else if (Q_rest_bits<57) {
			if (DEBUG) LOG.debug("factor_recurrent(): pMax^2 = " + pMaxSquare + ", Q_rest = " + Q_rest + " (" + Q_rest_bits + " bits) not prime -> use pollardRhoR64Mul63");
			factor1 = pollardRhoR64Mul63.findSingleFactor(Q_rest);
		} else if (Q_rest_bits<63) {
			if (DEBUG) LOG.debug("factor_recurrent(): pMax^2 = " + pMaxSquare + ", Q_rest = " + Q_rest + " (" + Q_rest_bits + " bits) not prime -> use pollardRho64");
			factor1 = pollardRho64.findSingleFactor(Q_rest);
		} else {
			if (DEBUG) LOG.debug("factor_recurrent(): pMax^2 = " + pMaxSquare + ", Q_rest = " + Q_rest + " (" + Q_rest_bits + " bits) not prime -> use qsInternal");
			factor1 = qsInternal.findSingleFactor(Q_rest);
		}
		if (ANALYZE) factorDuration += timer.capture();
		// Here we can not exclude factors > 31 bit because they may have 2 prime factors themselves.
		BigInteger factor2 = Q_rest.divide(factor1);
		if (DEBUG) LOG.debug("factor_recurrent(): Q_rest = " + Q_rest + " (" + Q_rest_bits + " bits) = " + factor1 + " * " + factor2);
		return factor_recurrent(factor1) && factor_recurrent(factor2);
	}
	
	/**
	 * Add factors that all Q(x) for the same a-parameter have in common.
	 * These are the q-values whose product gives the a-parameter and 2 if d==2.
	 */
	private void addCommonFactorsToSmallFactors() {
		if (d==2) {
			smallFactors.add(2);
		}
		for (int i=0; i<unsievedBaseElements.length; i++) {
			smallFactors.add(unsievedBaseElements[i]);
		}
	}

	@Override
	public TDivReport getReport() {
		return new TDivReport(testCount, sufficientSmoothCount, aqDuration, pass1Duration, pass2Duration, primeTestDuration, factorDuration, qRestSizes);
	}
	
	@Override
	public void cleanUp() {
		primes = null;
		unsievedBaseElements = null;
		x1Array = null;
		x2Array = null;
		qsInternal.cleanUp();
	}
}
