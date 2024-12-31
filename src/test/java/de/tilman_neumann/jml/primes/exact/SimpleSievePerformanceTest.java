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
package de.tilman_neumann.jml.primes.exact;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Performance test for the simple sieve of Eratosthenes.
 * 
 * @author Tilman Neumann
 */
public class SimpleSievePerformanceTest {
	private static final Logger LOG = LogManager.getLogger(SimpleSievePerformanceTest.class);
	
	/**
	 * Test performance without load caused by processPrime().
	 * @param args ignored
	 */
	public static void main(String[] args) {
    	ConfigUtil.initProject();
		long limit = 1000000;
		while (limit < Integer.MAX_VALUE) {
			long start = System.nanoTime();
			CountingCallback callback = new CountingCallback(); // initialize count=0 for each limit
			SimpleSieve sieve = new SimpleSieve(callback);
			sieve.sieve(limit);
			LOG.info("Sieving x <= " + limit + " found " + callback.getCount() + " primes in " + ((System.nanoTime()-start) / 1000000) + " ms");
			limit *= 10;
		}
	}
}
