package de.tilman_neumann.jml.factor.lehman;

import java.util.Collection;

/**
 * This is a generic interface for classes which can find some factors of a number up to a certain factor.
 * This maximal factor can be given by {@link #setMaxFactor(int)}.
 * The implementation can either return prime or composite factors of the number.
 * It can also choose if it only gives back one factor per call to {@link #findFactors(long, Collection)} as a return value,
 * or stores every factor it finds in the collection.
 *
 * @author thiloharich
 *
 */
public interface FactorFinder {

	/**
	 * Sets the maximalfactor the FactorFinder should look for factors.
	 * @param maximalFactor
	 */
	default void setMaxFactor(int maximalFactor)
	{
	}

	//	/**
	//	 * Get the maximal factor the FactorFinder searches for.
	//	 * @return
	//	 */
	//	default int getMaxFactor() {
	//		return -1;
	//	}

	/**
	 * Gives back at least one factor of the number n, if there is any.
	 * If {@link #setMaxFactor(int) maxFactor} is called, it will only check for numbers below maxFactor.
	 * If {@link #returnsCompositeOnly()} is true, the factor will be returned by the return of the function. In this case the prime factors
	 * parameter will not be used. If {@link #returnsCompositeOnly()} is false the prime factors can be stored in the prime factors collection.
	 * If {@link #findsPrimesOnly()} is true the implementation should return only prime factors (either as return value or in the factors collection).
	 * If {@link #setMaxFactor(int)} is called,
	 *
	 * @param n the number to be factorized.
	 * @param primeFactors Holds the prime factors below maxFactor,
	 * Only If {@link #returnsCompositeOnly()} is true the prime factors collection will not be used. In this case factors can be null.
	 * It will not necessary add all prime factors in this collection.
	 * @return a factor of the number n.
	 * If {@link #returnsCompositeOnly()} is true it will return a factor of the number n.
	 * If {@link #returnsCompositeOnly()} is false, the return is n divided by the prime factors of n added to the prime factors collection.
	 * If n is prime n will be returned. If 1 is returned the number is factorized completely.
	 */
	long findFactors (long n, Collection<Long> primeFactors);

	/**
	 * Indicates that the implementation will only return prime factors of the number in {@link #findFactors(long, Collection)}
	 * as return value and in the prime factor collection as well. The prime factors in the collection and the prime factor returned
	 * multiply up to n.
	 *
	 * @return
	 */
	default boolean findsPrimesOnly() {
		return true;
	}

	/**
	 * If the implementation will only return one factor as return value of {@link #findFactors(long, Collection)} this
	 * function returns true. In this case it will not use the prime factor collection to store primes.
	 * If this returns false, in {@link #findFactors(long, Collection)} the possible prime factors will be stored in the second parameter.
	 *
	 * @return
	 */
	default boolean returnsCompositeOnly() {
		return false;
	}



}
