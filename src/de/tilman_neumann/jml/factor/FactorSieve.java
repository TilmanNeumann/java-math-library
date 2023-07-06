/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018 Tilman Neumann - tilman.neumann@web.de
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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.primes.exact.SegmentedSieve;
import de.tilman_neumann.jml.primes.exact.SieveCallback;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;
import de.tilman_neumann.util.SortedMultiset_BottomUp;

/**
 * A factor algorithm using a prime sieve to quickly compute the factorizations of many small numbers at once.
 * Factors the first one million numbers in less than a second on a Ryzen 3900X.
 * Factoring tens of millions of numbers will need a lot of memory and eventually produce an OutOfMemoryError.
 */
public class FactorSieve implements SieveCallback {
	private static final Logger LOG = Logger.getLogger(FactorSieve.class);
	private static final boolean DEBUG = false;
	
	private Map<Long, SortedMultiset<Long>> factorizations; // maybe an array would be better but then we couldn't use long indices
	private long limit;
	
	/**
	 * Full constructor.
	 * @param limit the biggest number to get factored
	 */
	public FactorSieve(long limit) {
		factorizations = new HashMap<>();
		this.limit = limit;
	}
	
	/**
	 * Run the sieve. Thread-safe.
	 */
	public synchronized void sieve() {
		SegmentedSieve segmentedSieve = new SegmentedSieve(this);
		segmentedSieve.sieve(limit);
	}

	/**
	 * Fallback method: Receives new primes from the sieve and adds them to prime factorizations
	 */
	@Override
	public void processPrime(long prime) {
		for (long n = prime; n<=limit; n+=prime) {
			Long nL = Long.valueOf(n);
			SortedMultiset<Long> factors = factorizations.get(nL);
			if (factors == null) {
				factors = new SortedMultiset_BottomUp<Long>();
				factorizations.put(nL, factors);
			}
			int exponent = 1;
			long rest = n/prime;
			while (rest % prime == 0) {
				rest /= prime;
				exponent++;
			}
			factors.add(Long.valueOf(prime), exponent);
		}
	}
	
	public SortedMultiset<Long> getFactorization(long n) {
		return factorizations.get(Long.valueOf(n));
	}
	
	private static long computeProduct(SortedMultiset<Long> factors) {
		long result = 1;
		for (Long p : factors.keySet()) {
			int exponent = factors.get(p);
			result *= Math.pow(p.longValue(), exponent);
		}
		return result;
	}
	
	public static void main(String[] args) {
		ConfigUtil.initProject();
		
		long limit = 10000;
		FactorSieve sieve = new FactorSieve(limit);
		long t0 = System.currentTimeMillis();
		sieve.sieve();
		long t1 = System.currentTimeMillis();
		LOG.info("Factoring the first " + limit + " numbers took " + (t1-t0) + " milleseconds.");
		if (DEBUG) {
			for (long n=2; n<=limit; n++) { // n==1 gives null factors
				SortedMultiset<Long> factors = sieve.getFactorization(n);
				LOG.info(n + " = " + factors);
				long test = computeProduct(factors);
				assertEquals(n, test);
			}
		}
	}
}
