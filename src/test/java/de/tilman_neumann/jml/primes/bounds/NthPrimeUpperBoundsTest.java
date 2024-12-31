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
package de.tilman_neumann.jml.primes.bounds;

import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.primes.exact.SegmentedSieve;
import de.tilman_neumann.jml.primes.exact.SieveCallback;
import de.tilman_neumann.util.ConfigUtil;

/**
 * Test of upper bound estimates for the n.th prime function p(n).
 * 
 * @author Tilman Neumann
 */
public class NthPrimeUpperBoundsTest implements SieveCallback {
	private static final Logger LOG = LogManager.getLogger(NthPrimeUpperBoundsTest.class);
	
	private long n;

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}
	
	@Test
	public void testCombinedUpperBound() {
		NthPrimeUpperBoundsTest test = new NthPrimeUpperBoundsTest();
		// The maximum p to test has been chosen such that the runtime in github CI is moderate.
		// For local tests, it could be 1000 times bigger.
		test.run(1000000000L);
	}

	/**
	 * Run the sieve.
	 * @param limit maximum value to be checked for being prime.
	 */
	private void run(long limit) {
		n = 0;
		SegmentedSieve segmentedSieve = new SegmentedSieve(this);
		segmentedSieve.sieve(limit);
	}

	/**
	 * Callback method: Receives new primes from the sieve and checks the upper bound estimates for the n.th prime p(n).
	 * 
	 * Changing the mod we can regulate the resolution of the data to look at.
	 * Searching in the results for something like "rs01=-" lets us investigate in which ranges the particular algorithms work.
	 * 
	 * @param p the exact n.th prime
	 */
	@Override
	public void processPrime(long p) {
		n++; // update count (prime index)
		if (n%10000000==0) {
			String boundStr = "p_" + n + " = " + p + ": ";
			
			long rs01 = NthPrimeUpperBounds.RosserSchoenfeld01(n);
			boundStr += "rs01=" + (rs01-p);
			long rs02 = NthPrimeUpperBounds.RosserSchoenfeld02(n);
			boundStr += ", rs02=" + (rs02-p);
			long roj = NthPrimeUpperBounds.RobinJacobsen(n);
			boundStr += ", roj=" + (roj-p);
			long rob = NthPrimeUpperBounds.Robin1983(n);
			boundStr += ", rob=" + (rob-p);
			long du99 = NthPrimeUpperBounds.Dusart1999(n);
			boundStr += ", du99=" + (du99-p);
			long du10p7 = NthPrimeUpperBounds.Dusart2010p7(n);
			boundStr += ", du10p7=" + (du10p7-p);
			long du10p8 = NthPrimeUpperBounds.Dusart2010p8(n);
			boundStr += ", du10p8=" + (du10p8-p);
			long ax13 = NthPrimeUpperBounds.Axler2013(n);
			boundStr += ", ax13=" + (ax13-p);
			
			LOG.info(boundStr);
		}
		
		// Verifying individual estimates would need to consider them only in the range where they are best
		
		long combi = NthPrimeUpperBounds.combinedUpperBound(p);
		if (combi < p) {
			LOG.error("The combined upper bound estimate for n.th prime p(n) " + combi + " is smaller than p_" + n + " = " + p + ", difference = " + (combi - p));
		}
		assertTrue(combi >= p);
		
		// Testing the tightness of the upper bound would need to compare it to the individual bounds in the range where they are best
	}
}
