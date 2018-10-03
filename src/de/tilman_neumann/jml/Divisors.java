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
package de.tilman_neumann.jml;

import static de.tilman_neumann.jml.base.BigIntConstants.*;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.combinatorics.Factorial;
import de.tilman_neumann.jml.factor.CombinedFactorAlgorithm;
import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.jml.primes.exact.SieveFacade;
import de.tilman_neumann.jml.roots.SqrtInt;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;
import de.tilman_neumann.util.SortedMultiset_BottomUp;

/**
 * Implementations for finding all divisors of an integer.
 * 
 * @author Tilman Neumann
 */
public class Divisors {
	private static final Logger LOG = Logger.getLogger(Divisors.class);

	private Divisors() {
		// static class, not instantiable
	}

	/**
	 * Delivers the set of divisors of the argument, including 1 and n.
	 * Naive, very slow implementation.
	 * 
	 * @param n Argument.
	 * @return The set of divisors of n, sorted smallest first.
	 */
	private static ArrayList<BigInteger> getDivisors_v1(BigInteger n) {
		ArrayList<BigInteger> divisors = new ArrayList<BigInteger>();
		divisors.add(ONE);
		BigInteger test = TWO;
		while (test.compareTo(n) < 0) {
			if (n.mod(test).equals(ZERO)) {
				divisors.add(test);
			}
			test = test.add(ONE);
		}
		if (n.compareTo(ONE)>0) {
			// avoid double entry
			divisors.add(n);
		}
		return divisors;
	}

	/**
	 * Delivers the set of divisors of the argument, including 1 and n.
	 * Faster than the first version because trial division is only done up to sqrt(n).
	 * 
	 * @param n Argument.
	 * @return The set of divisors of n, sorted smallest first.
	 */
	private static ArrayList<BigInteger> getDivisors_v2(BigInteger n) {
		// all divisors <= sqrt(n)
		ArrayList<BigInteger> smallDivisors = getSmallDivisors_v1(n);
		if (n.equals(ONE)) return smallDivisors; // avoid double entry of 1
		
		// copy small divisors
		ArrayList<BigInteger> allDivisors = new ArrayList<BigInteger>(smallDivisors);
		// reverse iteration, add N/smallDivisor for all small divisors
		int i = smallDivisors.size()-1;
		if (i>-1) {
			BigInteger biggestSmallDivisor = smallDivisors.get(i);
			if (!biggestSmallDivisor.pow(2).equals(n)) allDivisors.add(n.divide(biggestSmallDivisor)); // avoid double entry of sqrt(n)
			i--;
			while (i>=0) {
				BigInteger smallDivisor = smallDivisors.get(i);
				allDivisors.add(n.divide(smallDivisor));
				i--;
			}
		}
		return allDivisors;
	}

	/**
	 * Delivers the set of divisors of the argument, including 1 and n.
	 * 
	 * @param n Argument.
	 * @return The set of divisors of n, sorted smallest first.
	 */
	public static SortedSet<BigInteger> getDivisors/*_v3*/(BigInteger n) {
		FactorAlgorithm factorizer = new CombinedFactorAlgorithm(1, false); // permit multiple threads?
		SortedMultiset<BigInteger> factors = factorizer.factor(n);
		return getDivisors(factors);
	}
	
	/**
	 * Delivers the set of divisors of the argument from a given prime factorization.
	 * The bottom-up implementation is slightly faster.
	 * 
	 * @param factors prime factorization
	 * @return The set of divisors of n, sorted smallest first.
	 */
	// TODO the current algorithm creates much more duplicates of divisors than actual divisors -> use a PowerSet?
	private static SortedSet<BigInteger> getDivisorsTopDown(SortedMultiset<BigInteger> factors) {
		ArrayList<BigInteger> primes = new ArrayList<>();
		ArrayList<Integer> powers = new ArrayList<>();
		for (Map.Entry<BigInteger, Integer> entry : factors.entrySet()) {
			primes.add(entry.getKey());
			powers.add(entry.getValue());
		}

		TreeSet<BigInteger> divisors = new TreeSet<BigInteger>();
		if (primes.size()==0 || (primes.size()==1 && primes.get(0).equals(ZERO))) {
			return divisors;
		}
		
		Stack<ArrayList<Integer>> stack = new Stack<ArrayList<Integer>>();
		stack.push(powers);
		while (!stack.isEmpty()) {
			powers = stack.pop();
			BigInteger divisor = ONE;
			for (int i=0; i<powers.size(); i++) {
				int power = powers.get(i);
				if (power > 0) {
					// multiply entry to divisor
					divisor = divisor.multiply(primes.get(i).pow(power));
				}
			}
			if (divisors.add(divisor)) {
				for (int i=0; i<powers.size(); i++) {
					int power = powers.get(i);
					// create new entry
					ArrayList<Integer> reducedPowers = new ArrayList<Integer>(powers); // copy
					reducedPowers.set(i, power-1);
					stack.push(reducedPowers);
				}
			}
		}
		return divisors;
	}
	
	/**
	 * Bottom-up divisors construction algorithm. Slightly faster than top-down.
	 * @param factors
	 * @return
	 */
	// TODO the current algorithm creates much more duplicates of divisors than actual divisors -> use a PowerSet?
	public static SortedSet<BigInteger> getDivisors/*BottomUp*/(SortedMultiset<BigInteger> factors) {
		ArrayList<BigInteger> primes = new ArrayList<>();
		ArrayList<Integer> maxPowers = new ArrayList<>();
		for (Map.Entry<BigInteger, Integer> entry : factors.entrySet()) {
			primes.add(entry.getKey());
			maxPowers.add(entry.getValue());
		}

		TreeSet<BigInteger> divisors = new TreeSet<BigInteger>();
		if (primes.size()==0 || (primes.size()==1 && primes.get(0).equals(ZERO))) {
			return divisors;
		}
		
		Stack<ArrayList<Integer>> stack = new Stack<ArrayList<Integer>>();
		ArrayList<Integer> emptyPowers = new ArrayList<Integer>();
		for (int i=0; i<maxPowers.size(); i++) {
			emptyPowers.add(0);
		}
		stack.push(emptyPowers);
		
		while (!stack.isEmpty()) {
			ArrayList<Integer> powers = stack.pop();
			// compute divisor from stack element
			BigInteger divisor = ONE;
			for (int i=0; i<powers.size(); i++) {
				int power = powers.get(i);
				if (power > 0) {
					// multiply entry to divisor
					divisor = divisor.multiply(primes.get(i).pow(power));
				}
			}
			if (divisors.add(divisor)) {
				for (int i=0; i<maxPowers.size(); i++) {
					int maxPower = maxPowers.get(i);
					int power = powers.get(i);
					if (power < maxPower) {
						// create new entry
						ArrayList<Integer> enhancedPowers = new ArrayList<Integer>(powers); // copy
						enhancedPowers.set(i, power+1);
						stack.push(enhancedPowers);
					}
				}
			}
		}
		return divisors;
	}

	/**
	 * Delivers the set of divisors of the argument except for 1 and n.
	 * 
	 * @param n Argument.
	 * @return The set of divisors of n.
	 */
	public static SortedSet<BigInteger> getDivisorsWithoutOneAndX(BigInteger n) {
		// find divisors of n
		SortedSet<BigInteger> divisors = getDivisors(n);
		// remove 1 and n
		divisors.remove(ONE);
		divisors.remove(n);
		// done
		return divisors;
	}

	/**
	 * Compute all positive divisors d of n with d <= lower(sqrt(n)).
	 * Naive, slow implementation.
	 * 
	 * @return all divisors d of n with d <= lower(sqrt(n)).
	 */
	public static ArrayList<BigInteger> getSmallDivisors_v1(BigInteger n) {
		BigInteger d_max = SqrtInt.iSqrt(n)[0];
		//LOG.debug("n=" + n + ", d_max=" + d_max);
		ArrayList<BigInteger> smallDivisors = new ArrayList<BigInteger>();
		for (BigInteger d=ONE; d.compareTo(d_max)<=0; d=d.add(ONE)) {
			if (n.mod(d).equals(ZERO)) {
				// found small divisor
				smallDivisors.add(d);
			}
		}
		return smallDivisors;
	}
	
	public static SortedSet<BigInteger> getSmallDivisors/*_v2*/(BigInteger n) {
		FactorAlgorithm factorizer = new CombinedFactorAlgorithm(1, false); // permit multiple threads?
		SortedMultiset<BigInteger> factors = factorizer.factor(n);
		return getSmallDivisors/*_v2*/(n, factors);
	}

	/**
	 * Bottom-up divisors construction algorithm for all divisor <= sqrt(n).
	 * 
	 * @param factors
	 * @return all divisor <= sqrt(n)
	 */
	// TODO the current algorithm creates much more duplicates of divisors than actual divisors -> use a PowerSet?
	public static SortedSet<BigInteger> getSmallDivisors/*_v2*/(BigInteger n, SortedMultiset<BigInteger> factors) {
		BigInteger d_max = SqrtInt.iSqrt(n)[0];
		
		ArrayList<BigInteger> primes = new ArrayList<>();
		ArrayList<Integer> maxPowers = new ArrayList<>();
		for (Map.Entry<BigInteger, Integer> entry : factors.entrySet()) {
			primes.add(entry.getKey());
			maxPowers.add(entry.getValue());
		}

		TreeSet<BigInteger> divisors = new TreeSet<BigInteger>();
		if (primes.size()==0 || (primes.size()==1 && primes.get(0).equals(ZERO))) {
			return divisors;
		}
		
		Stack<ArrayList<Integer>> stack = new Stack<ArrayList<Integer>>();
		ArrayList<Integer> emptyPowers = new ArrayList<Integer>();
		for (int i=0; i<maxPowers.size(); i++) {
			emptyPowers.add(0);
		}
		stack.push(emptyPowers);
		
		while (!stack.isEmpty()) {
			ArrayList<Integer> powers = stack.pop();
			// compute divisor from stack element
			BigInteger divisor = ONE;
			for (int i=0; i<powers.size(); i++) {
				int power = powers.get(i);
				if (power > 0) {
					// multiply entry to divisor
					divisor = divisor.multiply(primes.get(i).pow(power));
				}
			}
			if (divisor.compareTo(d_max) <= 0) {
				if (divisors.add(divisor)) {
					for (int i=0; i<maxPowers.size(); i++) {
						int maxPower = maxPowers.get(i);
						int power = powers.get(i);
						if (power < maxPower) {
							// create new entry
							ArrayList<Integer> enhancedPowers = new ArrayList<Integer>(powers); // copy
							enhancedPowers.set(i, power+1);
							stack.push(enhancedPowers);
						}
					}
				}
			}
		}
		return divisors;
	}

    /**
     * Compute the sum of divisors of x.
     * Naive, slow implementation.
     * 
     * @return The sum of all numbers 1<=d<=x that divide x.
     * E.g. sumOfDivisors(6) = 1+2+3+6 = 12.
     */
	private static BigInteger sumOfDivisors_v1(BigInteger x) {
    	BigInteger sum = BigInteger.ZERO;
    	for (BigInteger d : getDivisors(x)) {
    		sum = sum.add(d);
    	}
    	return sum;
    }
	
    /**
     * @return The sum of all numbers 1<=d<=x that divide x.
     * Faster implementation for general arguments.
     * 
     * E.g. sumOfDivisors(6) = 1+2+3+6 = 12.
     */
    public static BigInteger sumOfDivisors/*_v2*/(BigInteger x) {
		FactorAlgorithm factorizer = new CombinedFactorAlgorithm(1, false); // permit multiple threads?
		SortedMultiset<BigInteger> factors = factorizer.factor(x);
    	return sumOfDivisors(factors);
    }

	/**
	 * Fast sum of divisors when the prime factorization is known.
	 * See https://www.math.upenn.edu/~deturck/m170/wk3/lecture/sumdiv.html.
	 * @param factors
	 * @return sum of divisors
	 */
	public static BigInteger sumOfDivisors(SortedMultiset<BigInteger> factors) {
		ArrayList<BigInteger> entrySums = new ArrayList<BigInteger>();
		
		for (Map.Entry<BigInteger, Integer> entry : factors.entrySet()) {
			// entry sum: if the entry is 3^4, then the entry sum is S(3,4) = 3^0 + 3^1 + 3^2 + 3^3 + 3^4.
			BigInteger p = entry.getKey();
			int exp = entry.getValue();
			BigInteger entrySum = ONE; // p^0
			BigInteger summand = ONE;
			for (int i=1; i<=exp; i++) {
				summand = summand.multiply(p);
				entrySum = entrySum.add(summand);
			}
			entrySums.add(entrySum);
		}
		BigInteger productOfEntrySums = ONE;
		for (BigInteger entrySum : entrySums) {
			productOfEntrySums = productOfEntrySums.multiply(entrySum);
		}
		return productOfEntrySums;
	}

	/**
	 * Computes the number of positive divisors of the given argument.
	 * @param n
	 * @return
	 */
	public static BigInteger getDivisorCount(BigInteger n) {
		FactorAlgorithm factorizer = new CombinedFactorAlgorithm(1, false); // permit multiple threads?
		SortedMultiset<BigInteger> factors = factorizer.factor(n);
    	return getDivisorCount(factors);
	}

	/**
	 * Computes the number of positive divisors given the prime factorizaton of a number.
	 * @param factors
	 * @return
	 */
	public static BigInteger getDivisorCount(SortedMultiset<BigInteger> factors) {
		BigInteger count = ONE;
		for (Map.Entry<BigInteger, Integer> entry : factors.entrySet()) {
			count = count.multiply(BigInteger.valueOf(entry.getValue()+1));
		}
		return count;
	}
	
    /**
     * Find the biggest "second factor" of n, i.e. the biggest divisor d with d<=n/d.
     * 
     * @param n
     * @return biggest "second factor" of n (1 for primes)
     */
    public static BigInteger getBiggestSecondFactor(BigInteger n) {
    	if (n.bitLength() < 30) {
    		return getBiggestSecondFactorOfSmallArguments(n.intValue());
    	}
    	return getBiggestSecondFactorOfLargeArguments(n);
    }

    /**
     * Find the biggest "second factor" of n, i.e. the biggest divisor d with d<=n/d.
     * This implementation does trial division from sqrt(n) downwards; this is fast for arguments < 30 bit.
     * 
     * @param n
     * @return biggest "second factor" of n (1 for primes)
     */
    private static BigInteger getBiggestSecondFactorOfSmallArguments(int n) {
		// The biggest second factor must be <= lower(sqrt(n));
		// we start there and return the first (biggest) divisor that we find.
		for (int test = (int) Math.sqrt(n); test > 1; test--) {
			if (n%test == 0) {
				// found divisor
				return BigInteger.valueOf(test);
			}
		}
		return ONE; // prime
    }
	
    /**
     * Find the biggest "second factor" of n, i.e. the biggest divisor d with d<=n/d.
     * This implementation finds the prime factorization first, computes all divisors <= sqrt(n) and returns the largest of them.
     * 
     * @param n
     * @return biggest "second factor" of n (1 for primes)
     */
    private static BigInteger getBiggestSecondFactorOfLargeArguments(BigInteger n) {
		FactorAlgorithm factorizer = new CombinedFactorAlgorithm(1, false); // permit multiple threads?
		SortedMultiset<BigInteger> factors = factorizer.factor(n);
		return getBiggestSecondFactor(n, factors);
    }

    /**
     * Find the biggest "second factor" of n given its prime factorization.
     * 
     * @param n
     * @return biggest "second factor" of n (1 for primes)
     */
    public static BigInteger getBiggestSecondFactor(BigInteger n, SortedMultiset<BigInteger> factors) {
    	SortedSet<BigInteger> smallDivisors = getSmallDivisors(n, factors);
    	return smallDivisors.last();
    }

	/**
	 * Find the factors of a smooth number using trial division.
	 * @param n
	 * @return
	 */
	public static SortedMultiset<BigInteger> factor(BigInteger n) {
		SortedMultiset<BigInteger> factors = new SortedMultiset_BottomUp<>();
		SieveFacade sieve = new SieveFacade().ensureLimit(1000);
		
		for (int i=0; ; i++) {
			int p_i = sieve.getPrime(i);
			BigInteger p_i_big = BigInteger.valueOf(p_i);
			BigInteger[] div;
			while (true) {
				div = n.divideAndRemainder(p_i_big);
				if (!div[1].equals(ZERO)) break;
				
				// p_i divides n
				factors.add(p_i_big);
				n = div[0];
			}
			if (n.equals(ONE)) break;
		}
		return factors;
	}

	private static void testSumOfDivisorsForSmallIntegers() {
		BigInteger n = ZERO;
		while (n.compareTo(TEN_THOUSAND) <= 0) {
			LOG.info("sumOfDivisors(" + n + ") = " + sumOfDivisors_v1(n));
			n = n.add(ONE);
		}
	}
	
	/**
	 * Print the sum of divisors of factorials = sigma(n!) [A062569]:
	 * 1, 1, 3, 12, 60, 360, 2418, 19344, 159120, 1481040, 15334088, 184009056, ...
	 * 
	 * These are equal to (n+1)!/2 for 0<n<6, but less for bigger n...
	 */
	private static void testSumOfDivisorsForFactorials() {
		for (int n=0; ; n++) {
			// old version
			BigInteger fac = Factorial.withMemory(n);
			BigInteger sigma;
			
//			long t0 = System.currentTimeMillis();
//			sigma = sumOfDivisors_v1(fac);
			long t1 = System.currentTimeMillis();
//			LOG.info("sumOfDivisors_v1(" + n + "!) = " + sigma + " computed in " + (t1-t0) + "ms");
			
			// second version is much faster
//			sigma = sumOfDivisors/*_v2*/(fac);
			long t2 = System.currentTimeMillis();
//			LOG.info("sumOfDivisors_v2(" + n + "!) = " + sigma + " computed in " + (t2-t1) + "ms");
			
			// third version gives A062569 at hight speed
			SortedMultiset<BigInteger> factors = factor(fac);
			sigma = sumOfDivisors(factors);
			long t3 = System.currentTimeMillis();
			LOG.info("sumOfDivisors_v3(" + n + "!) = " + sigma + " computed in " + (t3-t2) + "ms");
		}
	}

	private static void testDivisors() {
		for (int n=0; ; n++) {
			BigInteger fac = Factorial.withMemory(n);
			SortedMultiset<BigInteger> factors;
			
			long t0 = System.currentTimeMillis();
			ArrayList<BigInteger> divSet1 = getDivisors_v1(fac);
			long t1 = System.currentTimeMillis();
			ArrayList<BigInteger> divSet2 = getDivisors_v2(fac);
			long t2 = System.currentTimeMillis();
			SortedSet<BigInteger> divSet3 = getDivisors/*_v3*/(fac);
			long t3 = System.currentTimeMillis();
			factors = factor(fac);
			SortedSet<BigInteger> divSet4 = getDivisorsTopDown(factors);
			long t4 = System.currentTimeMillis();
			factors = factor(fac);
			SortedSet<BigInteger> divSet5 = getDivisors/*BottomUp*/(factors); // slightly faster than top-down
			long t5 = System.currentTimeMillis();
			BigInteger divCount1 = getDivisorCount(fac); // slowed down by factorization
			long t6 = System.currentTimeMillis();
			factors = factor(fac);
			BigInteger divCount2 = getDivisorCount(factors);
			long t7 = System.currentTimeMillis();
			LOG.info("divisors_v1(" + n + "!) = " + divSet1);
			LOG.info("divisors_v2(" + n + "!) = " + divSet2);
			LOG.info("divisors_v3(" + n + "!) = " + divSet3);
			LOG.info("divisors_v4(" + n + "!) = " + divSet4);
			LOG.info("divisors_v5(" + n + "!) = " + divSet5);
			LOG.info("divisors_v1(" + n + "!) found " + divSet1.size() + " divisors in " + (t1-t0) + "ms");
			LOG.info("divisors_v2(" + n + "!) found " + divSet2.size() + " divisors in " + (t2-t1) + "ms");
			LOG.info("divisors_v3(" + n + "!) found " + divSet3.size() + " divisors in " + (t3-t2) + "ms");
			LOG.info("divisors_v4(" + n + "!) found " + divSet4.size() + " divisors in " + (t4-t3) + "ms");
			LOG.info("divisors_v5(" + n + "!) found " + divSet5.size() + " divisors in " + (t5-t4) + "ms");
			LOG.info("getDivisorCount_v1(" + n + "!) = " + divCount1 + " computed in " + (t6-t5) + "ms");
			LOG.info("getDivisorCount_v2(" + n + "!) = " + divCount2 + " computed in " + (t7-t6) + "ms");
			// this gives A027423 at high speed
		}
	}

	private static void testSmallDivisors() {
		for (int n=0; ; n++) {
			BigInteger fac = Factorial.withMemory(n);
			SortedMultiset<BigInteger> factors;
			
//			long t0 = System.currentTimeMillis();
//			ArrayList<BigInteger> divSet1 = getSmallDivisors_v1(fac);
			long t1 = System.currentTimeMillis();
			
			// Much much faster!
			factors = factor(fac);
			SortedSet<BigInteger> divSet2 = getSmallDivisors/*_v2*/(fac, factors);
			long t2 = System.currentTimeMillis();
			
//			LOG.info("smallDivisors_v1(" + n + "!) = " + divSet1);
//			LOG.info("smallDivisors_v2(" + n + "!) = " + divSet2);
//			LOG.info("smallDivisors_v1(" + n + "!) found " + divSet1.size() + " divisors in " + (t1-t0) + "ms");
			LOG.info("smallDivisors_v2(" + n + "!) found " + divSet2.size() + " divisors in " + (t2-t1) + "ms");
			// The divisor count sequence for n! up to 38! is
			// 1, 1, 1, 2, 4, 8, 15, 30, 48, 80, 135, 270, 396, 792, 1296, 2016, 2688, 5376, 7344, 14688, 20520, 30400, 48000, 96000, 121440, 170016, 266112, 338688, 458640, 917280, 1166400, 2332800, 2764800, 3932160, 6082560, 8211456, 9797760, 19595520, 30233088
			// Since no factorial is a square, this is A027423(n) for n>1
		}
	}

	private static void testBiggestSecondFactor() {
		long t0, t1;
		SecureRandom rng = new SecureRandom();
		int NCOUNT=100000;
		
		for (int bits = 15; ; bits++) {
			// create test set
			ArrayList<BigInteger> testSet = new ArrayList<>();
			for (int i=0; i<NCOUNT; ) {
				BigInteger n = new BigInteger(bits, rng);
				if (n.compareTo(ZERO)>0) {
					testSet.add(n);
					i++;
				}
			}
			t0 = System.currentTimeMillis();
			for (BigInteger nBig : testSet) {
				BigInteger biggestSecondFactor = getBiggestSecondFactorOfSmallArguments(nBig.intValue());
				//LOG.info("getBiggestSecondFactorOfSmallArguments(" + n + ") = " + biggestSecondFactor);
			}
			t1 = System.currentTimeMillis();
			LOG.info("getBiggestSecondFactorOfSmallArguments(" + bits + "bit) took " + (t1-t0) + "ms");
			
			t0 = System.currentTimeMillis();
			for (BigInteger nBig : testSet) {
				BigInteger biggestSecondFactor = getBiggestSecondFactorOfLargeArguments(nBig);
				//LOG.info("getBiggestSecondFactorOfLargeArguments(" + n + ") = " + biggestSecondFactor);
			}
			t1 = System.currentTimeMillis();
			LOG.info("getBiggestSecondFactorOfLargeArguments(" + bits + "bit) took " + (t1-t0) + "ms");
		}
	}
	
	/**
	 * Tests.
	 * 
	 * @param args Ignored.
	 */
	public static void main(String[] args) {
    	ConfigUtil.initProject();
//    	testDivisors();
//    	testSmallDivisors();
    	testBiggestSecondFactor();
//    	testSumOfDivisorsForSmallIntegers();
//    	testSumOfDivisorsForFactorials();
	}
}