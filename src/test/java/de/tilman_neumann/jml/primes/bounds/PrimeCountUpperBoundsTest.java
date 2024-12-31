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
 * Test of upper bound estimates for the prime count function π(x).
 * 
 * @author Tilman Neumann
 */
public class PrimeCountUpperBoundsTest implements SieveCallback {
	private static final Logger LOG = LogManager.getLogger(PrimeCountUpperBoundsTest.class);
	
	private long n;

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}
	
	@Test
	public void testCombinedUpperBound() {
		PrimeCountUpperBoundsTest test = new PrimeCountUpperBoundsTest();
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
	 * Callback method: Receives new primes from the sieve and checks the upper bound estimates for the prime count function.
	 * 
	 * Changing the mod we can regulate the resolution of the data to look at.
	 * Searching in the results for something like "du65=-" lets us investigate in which ranges the particular algorithms work.
	 * 
	 * @param p the exact n.th prime
	 */
	@Override
	public void processPrime(long p) {
		n++; // update count (prime index)
		if (n%10000000==0) {
			String boundStr = "p_" + n + " = " + p + ": ";
			
			long ax11 = PrimeCountUpperBounds.Axler_1_1(p);
			boundStr += "ax11=" + (ax11-n);
			long ax13 = PrimeCountUpperBounds.Axler_1_3(p);
			boundStr += ", ax13=" + (ax13-n);
			long ax35a = PrimeCountUpperBounds.Axler_3_5a(p);
			boundStr += ", ax35a=" + (ax35a-n);
			long ax35b = PrimeCountUpperBounds.Axler_3_5b(p);
			boundStr += ", ax35b=" + (ax35b-n);
			long ax35c = PrimeCountUpperBounds.Axler_3_5c(p);
			boundStr += ", ax35c=" + (ax35c-n);
			long ax35d = PrimeCountUpperBounds.Axler_3_5d(p); // Fails for many x in [230389, 2634562561], but thats not the range where it is best
			boundStr += ", ax35d=" + (ax35d-n);
			long du65 = PrimeCountUpperBounds.Dusart2010_eq6_5(p);
			boundStr += ", du65=" + (du65-n);
			long du66 = PrimeCountUpperBounds.Dusart2010_eq6_6(p);
			boundStr += ", du66=" + (du66-n);
			long du67 = PrimeCountUpperBounds.Dusart2010_eq6_7(p);
			boundStr += ", du67=" + (du67-n);
			long rs = PrimeCountUpperBounds.Rosser_Schoenfeld(p);
			boundStr += ", rs=" + (rs-n);
			
			LOG.info(boundStr);
		}
		
		// Verifying individual estimates would need to consider them only in the range where they are best

		long combi = PrimeCountUpperBounds.combinedUpperBound(p);
		if (combi < n) {
			LOG.error("The combined upper bound estimate for the prime counting function π(x) " + combi + " is smaller than " + n + " for x = " + p + ", difference = " + (combi - n));
		}
		assertTrue(combi >= n);
		
		// Testing the tightness of the upper bound would need to compare it to the individual bounds in the range where they are best
	}
}
