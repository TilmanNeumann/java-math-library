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
import java.util.Map;

import org.apache.log4j.Logger;

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
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(FactorAlgorithm.class);
	
	/** The best available single-threaded factor algorithm. (multi-threading may not always be wanted) */
	public static FactorAlgorithm DEFAULT = new CombinedFactorAlgorithm(1, false);

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
		SortedMultiset<BigInteger> factors = new SortedMultiset_BottomUp<BigInteger>();
		// first get rid of case |N|<=1:
		if (N.abs().compareTo(I_1)<=0) {
			// https://oeis.org/wiki/Empty_product#Prime_factorization_of_1:
			// "the set of prime factors of 1 is the empty set"
			if (!N.equals(I_1)) {
				factors.add(N);
			}
			return factors;
		}
		// make N positive:
		if (N.signum()<0) {
			factors.add(I_MINUS_1);
			N = N.abs();
		}
		// Remove multiples of 2:
		int lsb = N.getLowestSetBit();
		if (lsb > 0) {
			factors.add(I_2, lsb);
			N = N.shiftRight(lsb);
		}
		if (N.equals(I_1)) {
			// N was a power of 2
			return factors;
		}

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

			N = tdiv.findSmallOddFactors(N, actualTdivLimit, factors);
			
			if (N.equals(I_1)) {
				// N was "easy"
				return factors;
			}
			
			// TODO For large N with many "middle-size" prime factors, an advanced small factor test
			// like ECM or parallel Pollard-Rho would be nice here.
		}
		
		// N contains larger factors...
		factor_recurrent(N, factors);
		//LOG.debug(this.factorAlg + ": => all factors = " + factors);
		return factors;
	}
	
	private void factor_recurrent(BigInteger N, SortedMultiset<BigInteger> primeFactors) {
		if (bpsw.isProbablePrime(N)) { // TODO exploit tdiv done so far
			// N is probable prime. In exceptional cases this prediction may be wrong and N composite
			// -> then we would falsely predict N to be prime. BPSW is known to be exact for N <= 64 bit.
			//LOG.debug(N + " is probable prime.");
			primeFactors.add(N);
			return;
		} // else: N is surely not prime

		// The expensive part: Find a factor of N, where N is composite and has no 2s and undergone some tdiv
		BigInteger factor1 = findSingleFactor(N);
		BigInteger factor2 = N.divide(factor1);
		// Is it possible to further decompose the parts?
		//LOG.debug("Found factorization of N = " + N + " = " + factor1 + " * " + factor2);
		factor_recurrent(factor1, primeFactors);
		factor_recurrent(factor2, primeFactors);
		//LOG.debug("primeFactors after resolving factor1 = " + factor1 + " and factor2 = " + factor2 + " : " + primeFactors);
	}
	
	/**
	 * Find a single factor of the given N, which is composite and odd.
	 * @param N
	 * @return factor
	 */
	abstract public BigInteger findSingleFactor(BigInteger N);

	/**
	 * Returns a product-like representation of the given factorization,
	 * with distinct keys separated by "*" and the multiplicity indicated by "^".
	 * 
	 * @param factorization a prime factorization
	 * @return string representation
	 */
	public String getPrettyFactorString(SortedMultiset<BigInteger> factorization) {
		if (factorization.size()>0) {
			// Implementation note: Is faster with String than with StringBuffer!
			String factorStr = "";
			for (Map.Entry<BigInteger, Integer> entry : factorization.entrySet()) {
				factorStr += entry.getKey();
				Integer multiplicity = entry.getValue();
				if (multiplicity.intValue() > 1) {
					factorStr += "^" + multiplicity;
				}
				factorStr += " * ";
			}
			// remove the last ", "
			return factorStr.substring(0, factorStr.length()-3);
		}
		
		// no elements
		return "1";
	}
}
