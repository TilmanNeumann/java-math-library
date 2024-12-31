/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2019-2024 Tilman Neumann - tilman.neumann@web.de
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

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import de.tilman_neumann.jml.primes.bounds.NthPrimeUpperBounds;
import de.tilman_neumann.util.ConfigUtil;

/**
 * Test performance and correctness of results of prime sieves.
 * @author Tilman Neumann
 */
public class SieveTest {
	private static final int NCOUNT = 10000000; // 100m is feasible, but array-storing algorithms will fail soon above that
	
	private static long nthPrimeUpperBound;
	private static int correctCount;
	private static int[] correctResult;
	
	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		// get correct data
		CollectingCallback correctCallback = new CollectingCallback(NCOUNT);
    	SimpleSieve correctSieve = new SimpleSieve(correctCallback);
		nthPrimeUpperBound = NthPrimeUpperBounds.combinedUpperBound(NCOUNT);
    	correctSieve.sieve(nthPrimeUpperBound);
    	correctCount = correctCallback.count;
    	correctResult = correctCallback.array;
    	assertEquals(NCOUNT, correctCount);
    	assertEquals(2, correctResult[0]);
    	assertEquals(3, correctResult[1]);
    	assertEquals(5, correctResult[2]);
	}

	@Test
	public void testSegmentedSieve() {
		CollectingCallback segmentedCallback = new CollectingCallback(NCOUNT);
    	SegmentedSieve segmentedSieve = new SegmentedSieve(segmentedCallback);
		segmentedSieve.sieve(nthPrimeUpperBound);
		int[] segmentedResult = segmentedCallback.array;
		assertEquals(NCOUNT, segmentedCallback.count);
    	for (int i=0; i<NCOUNT; i++) {
    		assertEquals(correctResult[i], segmentedResult[i]);
    	}
	}

	@Test
	public void testAutoExpandingPrimesArray() {
    	AutoExpandingPrimesArray primesArray = AutoExpandingPrimesArray.get().ensurePrimeCount(NCOUNT);
    	for (int i=0; i<NCOUNT; i++) {
    		assertEquals(correctResult[i], primesArray.getPrime(i));
    	}
	}
}
