/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2019 Tilman Neumann (www.tilman-neumann.de)
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
package de.tilman_neumann.jml.primes.exact;

import org.apache.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Monolithic sieve of Eratosthenes, working only for limits < Integer.MAX_VALUE = 2^31 - 1.
 * Used for quality tests only.
 * 
 * @author Tilman Neumann
 */
public class SimpleSieve {
	private static final Logger LOG = Logger.getLogger(SimpleSieve.class);

	private SieveCallback clientCallback;

	public SimpleSieve(SieveCallback clientCallback) {
		this.clientCallback = clientCallback;
	}
	
	/**
	 * Generate primes.
	 * @param limit0 biggest number to test for prime
	 */
	public void sieve(long limit0) {
		// small primes not delivered by the sieve below
		clientCallback.processPrime(2);
		
		if (limit0 > Integer.MAX_VALUE) throw new IllegalArgumentException("limit " + limit0 + " exceeds Integer.MAX_VALUE = " + Integer.MAX_VALUE);
		int limit = (int) limit0;

		// Sieve
		boolean[] isComposite = new boolean[limit+1]; // initialized with false
		for (int i=2; i*i <= limit; i++) {
			if (!isComposite[i]) {
				for (int j = i*i; j <= limit; j+=i) {
					isComposite[j] = true;
				}
			}
		}
			
		// Collect
		int n = 3;
		for ( ; n<=limit; n+=2) {
			if (!isComposite[n]) {
				clientCallback.processPrime(n);
			}
		}
	}
	
	/**
	 * Test performance without load caused by processPrime().
	 * @param args ignored
	 */
	public static void main(String[] args) {
    	ConfigUtil.initProject();
		CountingCallback callback = new CountingCallback();
		long limit = 1000000;
		while (limit < Integer.MAX_VALUE) {
			long start = System.nanoTime();
			SimpleSieve sieve = new SimpleSieve(callback);
			sieve.sieve(limit);
			LOG.info("Sieving x <= " + limit + " found " + callback.getCount() + " primes in " + ((System.nanoTime()-start) / 1000000) + " ms");
			limit *=10;
		}
	}
}
