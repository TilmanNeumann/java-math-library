package de.tilman_neumann.jml.factor.lehman;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

/**
 * This is an Interface which gives an {@link #factorization(long)} of a long number.
 * It has a default implementation for {@link #factorization(long)}, which calculates the
 * factorization depending if the class extends {@link FactorsFinder}, {@link SingleLongFactorFinder} or {@link SinglePrimeFactorFinder}.
 *
 * Created by Thilo Harich on 26.03.2018.
 */
public interface FactorizationOfLongs extends FactorFinder {


	//    /**
	//     * If the {@link SingleLongFactorFinder#findFactors(long, Collection)} of the underlying {@link #getImpl(long)}
	//     * always retuns prime Factors this method returns true.
	//     * It then fills the prime factors of the given collection only with prime factors or always retuns a prime
	//     * factor if no prime factors are passed over.
	//     * @return
	//     */
	//    default boolean returnsOnlyPrimeFactors(){
	//        return true;
	//    }

	/**
	 * returns a full prime factorization of the number.
	 * The factorization is given as a TreeMultiset of the prime factors.
	 * If the underlying algorithm returns only prime factors, we do not have to factorize the factors we have found.
	 *
	 * @param n
	 * @return
	 */
	default TreeMultiset<Long> factorization(long n) {
		// handle the powers of 2 separately, does it help????
		final TreeMultiset<Long> factors = TreeMultiset.create();
		n = addEvenPrimes(n, factors);
		if (n == 1) {
			return factors;
		}
		if (getImpl(n).findsPrimesOnly())
			return factorizationByPrimes(n, factors);
		if (getImpl(n).returnsCompositeOnly()) {
			factors.addAll(factorizationByCompositesOnly(n));
			return factors;
		}
		factors.addAll(factorizationByFactors(n));
		return factors;
	}




	/**
	 * prints out the factorizationByFactors in a nice ways starting with the lowest factors.
	 * @param n
	 * @return
	 */
	default String printFactorization(long n) {
		final TreeMultiset<Long> factors = factorization(n);

		final List<String> s = new ArrayList<>();
		for (final Multiset.Entry<Long> entry : factors.entrySet()) {
			final int exponent = entry.getCount();
			String part = "" + entry.getElement();
			part += exponent == 1 ? "" : "^" + exponent;
			s.add(part);
		}
		return String.join(" * ", s);
	}

	default TreeMultiset<Long> factorizationByPrimes(long n, TreeMultiset<Long> primes) {
		// try to find all prime factors
		final long remainder = getImpl(n).findFactors(n, primes);
		// if we do not find a trivial divisor add it; this should only be the case if n
		// without the powers of 2 is a prime
		if (remainder != 1){
			primes.add(remainder);
		}
		return primes;
	}


	/**
	 * This method returns a complete factorizationByFactors of n.
	 * It uses the implementation returned by {@link #getImpl(long)} and calls
	 * {@link SingleLongFactorFinder#findFactors(long, Collection)}. This will return a factor.
	 * This factor does not have to be a prime factor, and has to be factorized again by
	 * findFactors().
	 *
	 * @see #factorizationByPrimes if possible.
	 * @param n
	 * @param factors
	 * @param primeFactors a Multiset of primes
	 * @return
	 */
	default Collection<Long> factorizationByFactors(long n) {
		// if we have a prime return an empty set
		final TreeMultiset<Long> primes = TreeMultiset.create();
		// find one factor and decomposite this factor and n/factor
		final long compositeFactor = getImpl(n).findFactors(n, primes);
		// if we do not find a divisor it must be a prime factor just return it
		if (compositeFactor == n){
			return ImmutableMultiset.of(compositeFactor);
		}
		long compositeFactor2 = n/compositeFactor;
		for (final long prime : primes)
		{
			// TODO multiply instead of division?
			compositeFactor2 /= prime;
		}
		primes.addAll(factorizationByFactors(compositeFactor));
		if (compositeFactor2 != 1)
			primes.addAll(factorizationByFactors(compositeFactor2));

		return primes;
	}

	default Collection<Long> factorizationByCompositesOnly(long n){
		final TreeMultiset<Long> factorsEven = TreeMultiset.create();
		// find one factor and decomposite this factor and n/factor
		final long factor1 = getImpl(n).findFactors(n, null);
		// if we do not find a divisor it must be a prime factor just return it
		if (factor1 == n){
			return ImmutableMultiset.of(factor1);
		}
		//		factorsEven.add(factor1);
		// also divide out the prime factorsEven
		final long factor2 = n/factor1;
		final Collection<Long> subFactors1 = factorizationByCompositesOnly(factor1);
		final Collection<Long> subFactors2 = factorizationByCompositesOnly(factor2);
		factorsEven.addAll(subFactors1);
		factorsEven.addAll(subFactors2);
		return factorsEven;

	}

	default TreeMultiset<Long> factorizationBySinglePrime(long n, TreeMultiset<Long> primeFactors) {
		while(true) {
			final long primeFactor = getImpl(n).findFactors(n, null);
			primeFactors.add(primeFactor);
			if (primeFactor == n) {
				return primeFactors;
			}
			n = n/primeFactor;
		}
	}

	default long addEvenPrimes(long n, TreeMultiset<Long> primeFactors) {
		while ((n & 1) == 0)
		{
			primeFactors.add(2l);
			n = n >> 1;
		}
		return n;
	}

	/**
	 * return an implementation which perform good for the number n.
	 *
	 * @param n
	 * @return
	 */
	default FactorFinder getImpl(long n){
		return this;
	}

}
