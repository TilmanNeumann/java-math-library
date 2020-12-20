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
package de.tilman_neumann.jml.factor;

import static de.tilman_neumann.jml.base.BigIntConstants.*;

import java.math.BigInteger;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.base.FactorArguments;
import de.tilman_neumann.jml.factor.base.FactorResult;
import de.tilman_neumann.jml.factor.tdiv.TDiv;
import de.tilman_neumann.jml.primes.probable.BPSWTest;
import de.tilman_neumann.util.SortedMultiset;
import de.tilman_neumann.util.SortedMultiset_BottomUp;

/**
 * Abstraction of integer factorization algorithms.
 * This class provides a framework to find the complete prime factorization of N,
 * requiring only to implement the method findSingleFactor(BigInteger).
 * 
 * @author Tilman Neumann
 */
abstract public class FactorAlgorithm {
	private static final Logger LOG = Logger.getLogger(FactorAlgorithm.class);
	
	private static final boolean DEBUG = false;
	
	/** The best available single-threaded factor algorithm. (multi-threading may not always be wanted) */
	public static FactorAlgorithm DEFAULT = new CombinedFactorAlgorithm(1);

	/** the number of primes needed to factor any int <= 2^31 - 1 using trial division */
	protected static final int NUM_PRIMES_FOR_31_BIT_TDIV = 4793;

	private BPSWTest bpsw = new BPSWTest();
	private TDiv tdiv = new TDiv();
	
	protected Integer tdivLimit;
	
	public FactorAlgorithm() {
		tdivLimit = null; // automatic determination based on experimental results
	}
	
	public FactorAlgorithm(Integer tdivLimit) {
		this.tdivLimit = tdivLimit;
	}

	/**
	 * @return The name of the algorithm, possibly including important parameters.
	 */
	abstract public String getName();

	/**
	 * Decomposes the argument N into prime factors.
	 * The result is a multiset of BigIntegers, sorted bottom-up.
	 * @param N Number to factor.
	 * @return The prime factorization of N
	 */
	public SortedMultiset<BigInteger> factor(BigInteger N) {
		boolean searchSmallFactors = true; // XXX should be a method parameter
		
		SortedMultiset<BigInteger> primeFactors = new SortedMultiset_BottomUp<BigInteger>();
		// first get rid of case |N|<=1:
		if (N.abs().compareTo(I_1)<=0) {
			// https://oeis.org/wiki/Empty_product#Prime_factorization_of_1:
			// "the set of prime factors of 1 is the empty set"
			if (!N.equals(I_1)) {
				primeFactors.add(N);
			}
			return primeFactors;
		}
		// make N positive:
		if (N.signum()<0) {
			primeFactors.add(I_MINUS_1);
			N = N.abs();
		}
		// Remove multiples of 2:
		int lsb = N.getLowestSetBit();
		if (lsb > 0) {
			primeFactors.add(I_2, lsb);
			N = N.shiftRight(lsb);
		}
		if (N.equals(I_1)) {
			// N was a power of 2
			return primeFactors;
		}

		/* TODO move the following block to appropriate algorithms
		int Nbits = N.bitLength();
		if (Nbits > 62) {
			// "Small" algorithms like trial division, Lehman or Pollard-Rho are very good themselves
			// at finding small factors, but for larger N we do some trial division.
			// This will help "big" algorithms to factor smooth numbers much faster.
			int actualTdivLimit;
			if (tdivLimit != null) {
				// use "dictated" limit
				actualTdivLimit = tdivLimit.intValue();
			} else {
				// adjust tdivLimit=2^e by experimental results
				final double e = 10 + (Nbits-45)*0.07407407407; // constant 0.07.. = 10/135
				actualTdivLimit = (int) Math.min(1<<20, Math.pow(2, e)); // upper bound 2^20
			}

			N = tdiv.findSmallOddFactors(N, actualTdivLimit, primeFactors);
			// TODO add tdiv duration to final report
			
			if (N.equals(I_1)) {
				// N was "easy"
				return primeFactors;
			}
		}
		*/
		
		// N contains larger factors...
		long smallestPossibleFactor = 3;
		FactorResult factorResult = new FactorResult(primeFactors, new SortedMultiset_BottomUp<BigInteger>(), new SortedMultiset_BottomUp<BigInteger>(), smallestPossibleFactor);
		SortedMultiset<BigInteger> untestedFactors = factorResult.untestedFactors; // ArrayList would be faster
		untestedFactors.add(N);
		while (true) {
			// resolve untested factors
			while (untestedFactors.size()>0) {
				BigInteger untestedFactor = untestedFactors.firstKey();
				int exp = untestedFactors.get(untestedFactor);
				untestedFactors.removeAll(untestedFactor);
				if (bpsw.isProbablePrime(untestedFactor)) {
					// The untestedFactor is probable prime. In exceptional cases this prediction may be wrong and untestedFactor composite
					// -> then we would falsely predict untestedFactor to be prime. BPSW is known to be exact for arguments <= 64 bit.
					//LOG.debug(untestedFactor + " is probable prime.");
					factorResult.primeFactors.add(untestedFactor, exp);
				} else {
					factorResult.compositeFactors.add(untestedFactor, exp);
				}
			}
			// now untestedFactors is empty
			
			// factor composite factors; iteration needs to be fail-fast against element addition and removal
			while (true) {
				if (factorResult.compositeFactors.isEmpty()) {
					// all factors are prime factors now
					return factorResult.primeFactors;
				}
				
				BigInteger compositeFactor = factorResult.compositeFactors.keySet().iterator().next();
				int exp = factorResult.compositeFactors.get(compositeFactor);
				factorResult.compositeFactors.removeAll(compositeFactor);
				
				FactorArguments args = new FactorArguments(compositeFactor, exp, searchSmallFactors, smallestPossibleFactor);
				boolean foundFactor = searchFactors(args, factorResult);
				if (!foundFactor) {
					if (DEBUG) LOG.error("Factor algorithm " + getName() + " failed to find a factor of composite " + compositeFactor);
					factorResult.primeFactors.add(compositeFactor, exp); // emergency response ;-)
				} // else: found factors have been added to factorResult.untestedFactors with the given exponent
				
				smallestPossibleFactor = Math.max(smallestPossibleFactor, factorResult.smallestPossibleFactorRemaining);
			}
		}
	}
	
	/**
	 * Find a single factor of the given N, which is composite and odd.
	 * @param N
	 * @return factor
	 * @deprecated not general enough for all cases (find small factors before real algorithm, ECM); this method may be removed in a future release.
	 */
	@Deprecated
	abstract public BigInteger findSingleFactor(BigInteger N);
	
	/**
	 * Try to find at least one factor of the given args.N, which is composite and odd.
	 * @param args
	 * @param result the result of the factoring attempt. Should be initialized only once by the caller to reduce overhead.
	 * @return found a factor ?
	 * 
	 * TODO Overwrite this method appropriately in sub-algorithms!
	 */
	public boolean searchFactors(FactorArguments args, FactorResult result) {
		BigInteger N = args.N;
		BigInteger factor1 = findSingleFactor(N);
		if (factor1.compareTo(I_1) > 0 && factor1.compareTo(N) < 0) {
			// We found a factor, but here we cannot know if it is prime or composite
			result.untestedFactors.add(factor1, args.exp);
			result.untestedFactors.add(N.divide(factor1), args.exp);
			return true;
		}

		return false; // nothing found
	}
}
