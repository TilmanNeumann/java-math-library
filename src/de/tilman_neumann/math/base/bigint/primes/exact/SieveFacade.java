/*
 * PSIQS 4.0 is a Java library for integer factorization, including a parallel self-initializing quadratic sieve (SIQS).
 * Copyright (C) 2018  Tilman Neumann (www.tilman-neumann.de)
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
package de.tilman_neumann.math.base.bigint.primes.exact;

import org.apache.log4j.Logger;

import de.tilman_neumann.math.base.bigint.IntArray;
import de.tilman_neumann.math.base.bigint.primes.bounds.NthPrimeUpperBounds;
import de.tilman_neumann.math.base.bigint.primes.bounds.PrimeCountUpperBounds;

/**
 * An auto-expanding facade for the segmented sieve of Eratosthenes.
 * 
 * Singleton implementation to avoid spending too much memory on the primes in different instances.
 * 
 * @author Tilman Neumann
 */
public class SieveFacade implements SieveCallback {
	private static final Logger LOG = Logger.getLogger(SieveFacade.class);
	private static final boolean DEBUG = false;
	
	// variables must be initialized to avoid exceptions in ensureMaxPrime()
	private int[] array = new int[] {2}; // the array to store the primes
	private int count = 1; // actual element count
	private int capacity = 1; // maximal array capacity
	
	// singleton
	private static final SieveFacade THE_SIEVE = new SieveFacade();

	public static SieveFacade get() {
		return THE_SIEVE;
	}
	
	/**
	 * Ensures that the array contains at least the first 'desiredCount' primes.
	 * @param desiredCount
	 * @return PrimeGenerator
	 */
	public SieveFacade ensurePrimeCount(int desiredCount) {
		if (count < desiredCount) {
			// Current primes array is to small -> expansion needed.
			// Compute (tight) bound such that there are at least count primes in (0, nthPrimeUpperBound]
			long nthPrimeUpperBound = NthPrimeUpperBounds.combinedUpperBound(desiredCount);
			fetchPrimes(desiredCount, nthPrimeUpperBound);
		}
		return this;
	}

	/**
	 * Ensures that the array contains all primes <= x.
	 * @param x
	 * @return PrimeGenerator
	 */
	public SieveFacade ensureLimit(int x) {
		if (array[count-1] < x) {
			// Compute upper bound for the number of primes in (0, x]
			int countUpperBound = (int) PrimeCountUpperBounds.combinedUpperBound(x);
			fetchPrimes(countUpperBound, x);
			if (DEBUG) LOG.debug("pMax = " + array[count-1] + ", x = " + x);
		}
		return this;
	}

	/**
	 * Get the primes computed so far.
	 * This method is not auto-expanding; to have a guaranteed number of primes, one of the ensure-methods must be called before.
	 * @return primes array
	 */
	public IntArray getPrimes() {
		return new IntArray(array, count);
	}

	/**
	 * Get the n.th prime, e.g. p[0]=2.
	 * This method is auto-expanding the prime array: Save but not as fast as possible.
	 * 
	 * @param n
	 * @return n.th prime, where n starts at 0, e.g. p[0] = 2
	 */
	public int getPrime(int n) {
		if (count <= n) {
			int nextCount = 3*count; // trade-off between speed and memory waste
			// Compute (tight) bound such that there are at least count primes in (0, nthPrimeUpperBound]
			long nthPrimeUpperBound = NthPrimeUpperBounds.combinedUpperBound(nextCount);
			fetchPrimes(nextCount, nthPrimeUpperBound);
		}
		return array[n];
	}
	
	/**
	 * Trigger the sieve.
	 * @param desiredCount wanted number of primes
	 * @param limit maximum value to be checked for being prime.
	 */
	private void fetchPrimes(int desiredCount, long limit) {
		array = new int[desiredCount];
		capacity = desiredCount;
		count = 0;
		SegmentedSieve segmentedSieve = new SegmentedSieve(this);
		segmentedSieve.sieve(limit);
	}

	/**
	 * Fallback method: Receives new primes from the sieve and stores them in the array.
	 */
	@Override
	public void processPrime(long prime) {
		if (count == capacity) return; // array is full
		array[count++] = (int) prime;
	}
}
