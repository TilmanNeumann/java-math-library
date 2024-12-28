/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2024 Tilman Neumann - tilman.neumann@web.de
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

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.jml.gcd.Gcd63;
import de.tilman_neumann.jml.primes.exact.AutoExpandingPrimesArray;
import de.tilman_neumann.jml.primes.probable.BPSWTest;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;
import de.tilman_neumann.util.SortedMultiset_BottomUp;

/**
 * This program generates good k-multiplier sets for the Hart and Lehman factoring algorithms.
 * 
 * It applies the Lehman/Hart core factor test routine to all pairs (k, N) of (quite big) sets of k and N,
 * and then collects round by round the k that factors most of the yet unfactored N.
 * 
 * For k in [1, sqrt(Nmax)] this algorithm is capable to run up to N with >=28 bit (but needs something like 10GB memory at 28 bit)
 * 
 * Key take-aways:
 * 1) If k in [1, Nmax], then every N is factored by at least one k.
 * 2) If k in [1, sqrt(Nmax)], then we need additional trial division up to N^(1/4) to factor all N.
 *    If k in [1, cbrt(Nmax)], then we need additional trial division up to N^(1/3) to factor all N.
 *    (the latter was possibly Lehman's key insight to create an O(n^1/3) complexity factoring algorithm)
 */
public class HartMultiplierChainGenerator {
	private static final Logger LOG = LogManager.getLogger(HartMultiplierChainGenerator.class);

	private static final boolean DEBUG = false;
	
	/** This constant is used for fast rounding of double values to long. */
	private static final double ROUND_UP_DOUBLE = 0.9999999665;
	
	private final Gcd63 gcdEngine = new Gcd63();

	private final BPSWTest bpsw = new BPSWTest();
	
	private static final FactorAlgorithm factorizer = FactorAlgorithm.getDefault();

	private static final AutoExpandingPrimesArray autoPrimes = AutoExpandingPrimesArray.get().ensureLimit(1<<20);

	public List<Integer> compute(long Nmin, long Nmax, int kMax) {
		
		// We do many iterations over remainingKSet and a single removal operation. Thus an ArrayLKist is much better than any Set class
		ArrayList<Integer> remainingKSet = new ArrayList<>();
		for (int k=1; k<kMax; k++) {
			remainingKSet.add(k);
		}
		
		LOG.info("#N in range = " + (Nmax-Nmin+1));
		
		// using an ArrayList here is very bad for N>=22 bit
		Set<Long> remainingNSet = new LinkedHashSet<>();
		for (long N=Nmin; N<Nmax; N++) {
			if (!bpsw.isProbablePrime(N)) {
				remainingNSet.add(N);
			}
		}
		LOG.info("#N after removing primes = " + remainingNSet.size());
		
		// remove all N not factored by any k; analyze on-the-fly how much trial division is required for those N
		int maxTDivPrime = 0;
		Iterator<Long> nIter = remainingNSet.iterator();
		while(nIter.hasNext()) {
			long N = nIter.next();
			boolean isFactored = false;
			for (int k : remainingKSet) {
				if (test(k, N)) {
					isFactored = true;
					break;
				}
			}
			if (!isFactored) {
				nIter.remove();
				// check required trial division
				for (int i=0; ; i++) {
					int p = autoPrimes.getPrime(i);
					if ((N%p) == 0) {
						// N is divisible by p
						if (p > maxTDivPrime) {
							maxTDivPrime = p;
						}
						break;
					}
				}
			}
		}
		LOG.info("#N after removing unfactorable N = " + remainingNSet.size());
		LOG.info("Unfactorable N need trial division up to " + maxTDivPrime);

		// compute the number of N factored by each k
		int[] factoredNCounts = new int[kMax];
		for (int k=1; k<kMax; k++) {
			int Ncount = 0;
			for (long N : remainingNSet) {
				if (test(k, N)) {
					Ncount++;
				}
			}
			factoredNCounts[k] = Ncount;
		}

		LOG.info("Test " + remainingKSet.size() + " k * " + remainingNSet.size() + " N...");

		// select multipliers round by round
		long totalFactoredNCount = 0;
		List<Integer> chain = new ArrayList<>();
		for (long round = 0; remainingKSet.size()>0; round++) {
			// new round: find some k dividing most of the remaining N
			int bestK = 0;
			long bestFactoredNCount = 0;
			ArrayList<Integer> topKSet = new ArrayList<>(); // all k with equal number of factored N in round
			for (int k : remainingKSet) {
				int factoredNCount = factoredNCounts[k];
				if (factoredNCount <= bestFactoredNCount) {
					// no need to test k right now
					if (factoredNCount == bestFactoredNCount) {
						topKSet.add(k);
					}
					continue;
				}
				
				// now we know that k will be a new record
				if (DEBUG) LOG.debug("Round " + round + ": New record k = " + k + " with " + factoredNCount + " factored N");
				bestFactoredNCount = factoredNCount;
				bestK = k;
				topKSet.clear();
				topKSet.add(k);
			}

			// already done?
			if (bestFactoredNCount == 0) {
				return chain;
			}

			// now we only need to find the N factored by the _best_ k. This is quite fast.
			ArrayList<Long> bestFactoredNSet = new ArrayList<>();
			for (long N : remainingNSet) {
				if (test(bestK, N)) {
					bestFactoredNSet.add(N);
				}
			}

			// add best k to chain, remove from available set
			chain.add(bestK);
			totalFactoredNCount += bestFactoredNCount;
			remainingKSet.remove(Integer.valueOf(bestK));
			remainingNSet.removeAll(bestFactoredNSet);
			
			// Update factoredNCounts.
			// This is quite fast because only a small portion of the Ns is considered.
			// It is also considerably faster to have the N-loop outside.
			for (long N : bestFactoredNSet) {
				for (int k : remainingKSet) {
					if (test(k, N)) {
						factoredNCounts[k]--;
					}
				}
			}
			
			// trim remainingKSet
			Iterator<Integer> kIter = remainingKSet.iterator();
			while(kIter.hasNext()) {
				int k = kIter.next();
				if (factoredNCounts[k] == 0) {
					kIter.remove();
				}
			}
			
			String roundResult = "Round " + round + ": Selected k = " + bestK + " = " + factorizer.factor(BigInteger.valueOf(bestK)) + " with accumulated number of factored N = " + totalFactoredNCount + ".";
			if (topKSet.size() > 11) {
				topKSet.remove(Integer.valueOf(bestK));
				roundResult += " There are " + topKSet.size() + " other k with the same quality, e.g. " + topKSet.subList(0, 9);
			} else if (topKSet.size() > 1) {
				topKSet.remove(Integer.valueOf(bestK));
				roundResult += " There are " + topKSet.size() + " other k with the same quality: " + topKSet;
			}
			LOG.info(roundResult);
		}
		return chain;
	}
	
	private boolean test(long k, long N) {
		long a;
		long kN = k*N;
		long fourkN = kN<<2;
		if ((k & 1) == 0) {
			// even k
			a = (long) (Math.sqrt(fourkN) + ROUND_UP_DOUBLE) | 1L;
		} else {
			// odd k -> adjust a mod 8, 16, 32
			a = (long) (Math.sqrt(fourkN) + ROUND_UP_DOUBLE);
			a = adjustAForOddK(a, kN+1);
		}
		long test = a*a - fourkN;
		long b = (long) Math.sqrt(test);
		if (b*b == test) {
			long gcd = gcdEngine.gcd(a+b, N);
			if (gcd>1 && gcd<N) {
				if (DEBUG) assertEquals(N % gcd, 0);
				return true;
			}
		}
		return false;
	}
	
	private long adjustAForOddK(long a, long kNp1) {
		if ((kNp1 & 3) == 0) {
			a += (kNp1 - a) & 7;
		} else if ((kNp1 & 7) == 6) {
			final long adjust1 = (kNp1 - a) & 31;
			final long adjust2 = (-kNp1 - a) & 31;
			a += adjust1<adjust2 ? adjust1 : adjust2;
		} else { // (kN+1) == 2 (mod 8)
			final long adjust1 = (kNp1 - a) & 15;
			final long adjust2 = (-kNp1 - a) & 15;
			a += adjust1<adjust2 ? adjust1 : adjust2;
		}
		return a;
	}
	
	private static void computeFactorChains() {
		for (int bits = 3; bits<64; bits++) { // there are no composites with less than 3 bit
			long Nmin = 1L<<(bits-1);
			long Nmax = (1L<<bits) - 1;
			//long Nmax = Math.min((1L<<bits) - 1, Nmin+1000000-1); // limit number of N per bitsize to 1 Mio.
			
			//int kMax = (int) (Math.cbrt(Nmax) + 1); // factors about 60% of all N
			int kMax = (int) (Math.sqrt(Nmax) + 1); // factors about 2/3 of all N
			//int kMax = (int) (Math.pow(Nmax, 2/3.0) + 1); // factors about 6/7 of all N
			//int kMax = (int) Nmax; // factors all N
			
			LOG.debug(bits + " bit: N = " + Nmin + "..." + Nmax + ", kMax = " + kMax);
			long t0 = System.currentTimeMillis();
			List<Integer> chain = new HartMultiplierChainGenerator().compute(Nmin, Nmax, kMax);
			long t1 = System.currentTimeMillis();
			LOG.info("Computing the " + bits + "-bit multiplier chain took " + (t1-t0) + " milliseconds.");
			
			if (DEBUG) LOG.info("The multiplier chain has " + chain.size() + " elements:");
			SortedMultiset<BigInteger> allFactorsSet = new SortedMultiset_BottomUp<>();
			for (long k : chain) {
				SortedMultiset<BigInteger> factors = factorizer.factor(BigInteger.valueOf(k));
				if (DEBUG) LOG.info("    k = " + k + " = " + factors);
				allFactorsSet.addAll(factors);
			}
			LOG.info("Found " + chain.size() + " multipliers = " + chain);
			LOG.info("All factors in the multiplier set = " + allFactorsSet);
			// the distribution of factors follows approximately P(p) ~ 1/(p*ln(p))
			// at p=2 is always missing a 2 in the exponent because it is already integrated in the algorithm
		}
	}
	
	public static void main(String[] args) {
		ConfigUtil.initProject();
		computeFactorChains();
	}
}
