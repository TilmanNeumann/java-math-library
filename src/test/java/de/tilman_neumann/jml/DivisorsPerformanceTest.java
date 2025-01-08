/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2025 Tilman Neumann - tilman.neumann@web.de
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
import java.util.Random;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.SortedSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.combinatorics.Factorial;
import de.tilman_neumann.jml.factor.tdiv.TDiv;
import de.tilman_neumann.util.ConfigUtil;

/**
 * Performance tests for divisors computations.
 * 
 * @author Tilman Neumann
 */
public class DivisorsPerformanceTest {
	private static final Logger LOG = LogManager.getLogger(DivisorsPerformanceTest.class);

	private static final boolean TEST_SLOW = false;

	private static final TDiv tdiv = new TDiv().setTestLimit(65536); // 65536 is enough to factor factorials
	private static final Random RNG = new Random();

	/**
	 * Print the sum of divisors of factorials = sigma(n!) [A062569]:
	 * 1, 1, 3, 12, 60, 360, 2418, 19344, 159120, 1481040, 15334088, 184009056, ...
	 * 
	 * These are equal to (n+1)!/2 for 0<n<6, but less for bigger n...
	 */
	private static void testSumOfDivisors() {
		for (int n=0; n<=100; n++) {
			BigInteger fac = Factorial.factorial(n);
			BigInteger sigma;
			long t0, t1;
			
			if (TEST_SLOW) {
				// old version
				t0 = System.currentTimeMillis();
				sigma = Divisors.sumOfDivisors_v1(fac);
				t1 = System.currentTimeMillis();
				LOG.info("sumOfDivisors_v1(" + n + "!) = " + sigma + " computed in " + (t1-t0) + "ms");
				
				// second version is much faster
				t0 = System.currentTimeMillis();
				sigma = Divisors.sumOfDivisors/*_v2*/(fac);
				t1 = System.currentTimeMillis();
				LOG.info("sumOfDivisors_v2(" + n + "!) = " + sigma + " computed in " + (t1-t0) + "ms");
			}
			
			// third version gives A062569 at high speed
			t0 = System.currentTimeMillis();
			SortedMap<BigInteger, Integer> factors = tdiv.factor(fac); // tdiv is sufficient because fac is very smooth
			sigma = Divisors.sumOfDivisors(factors);
			t1 = System.currentTimeMillis();
			LOG.info("sumOfDivisors_v3(" + n + "!) = " + sigma + " computed in " + (t1-t0) + "ms");
		}
	}

	private static void testDivisorCounts() {
		for (int n=0; n<=100; n++) {
			BigInteger fac = Factorial.factorial(n);
			SortedMap<BigInteger, Integer> factors = tdiv.factor(fac); // tdiv is sufficient because fac is very smooth
			
			long t5 = System.currentTimeMillis();
			BigInteger divCount1 = Divisors.getDivisorCount(fac); // very fast
			
			long t6 = System.currentTimeMillis();
			BigInteger divCount2 = Divisors.getDivisorCount(factors); // very fast
			
			long t7 = System.currentTimeMillis();
			
			LOG.info("getDivisorCount_v1(" + n + "!) = " + divCount1 + " computed in " + (t6-t5) + "ms");
			LOG.info("getDivisorCount_v2(" + n + "!) = " + divCount2 + " computed in " + (t7-t6) + "ms");
			// this gives A027423 at high speed
		}
	}

	private static void testDivisors() {
		for (int n=0; n<=20; n++) {
			BigInteger fac = Factorial.factorial(n);
			SortedMap<BigInteger, Integer> factors = tdiv.factor(fac); // tdiv is sufficient because fac is very smooth

			ArrayList<BigInteger> divSet1, divSet2;
			long t0, t1;

			if (TEST_SLOW) {
				// v1 is very slow...
				t0 = System.currentTimeMillis();
				divSet1 = Divisors.getDivisors_v1(fac);
				
				// v2 is quite slow
				t1 = System.currentTimeMillis();
				divSet2 = Divisors.getDivisors_v2(fac);
			}
			
			long t2 = System.currentTimeMillis();
			SortedSet<BigInteger> divSet3 = Divisors.getDivisors/*_v3*/(fac);
			
			long t3 = System.currentTimeMillis();
			SortedSet<BigInteger> divSet4 = Divisors.getDivisorsTopDown(factors);
			
			long t4 = System.currentTimeMillis();
			SortedSet<BigInteger> divSet5 = Divisors.getDivisors/*BottomUp*/(factors); // slightly faster than top-down
			
			long t5 = System.currentTimeMillis();
			
			// The sets get too big for logging
			//LOG.info("divisors_v1(" + n + "!) = " + divSet1);
			//LOG.info("divisors_v2(" + n + "!) = " + divSet2);
			//LOG.info("divisors_v3(" + n + "!) = " + divSet3);
			//LOG.info("divisors_v4(" + n + "!) = " + divSet4);
			//LOG.info("divisors_v5(" + n + "!) = " + divSet5);
			
			if (TEST_SLOW) {
				LOG.info("divisors_v1(" + n + "!) found " + divSet1.size() + " divisors in " + (t1-t0) + "ms");
				LOG.info("divisors_v2(" + n + "!) found " + divSet2.size() + " divisors in " + (t2-t1) + "ms");
			}
			LOG.info("divisors_v3(" + n + "!) found " + divSet3.size() + " divisors in " + (t3-t2) + "ms");
			LOG.info("divisors_v4(" + n + "!) found " + divSet4.size() + " divisors in " + (t4-t3) + "ms");
			LOG.info("divisors_v5(" + n + "!) found " + divSet5.size() + " divisors in " + (t5-t4) + "ms");
		}
	}

	private static void testGetSmallDivisors() {
		for (int n=0; n<=18; n++) {
			BigInteger fac = Factorial.factorial(n);
			
			long t0 = System.currentTimeMillis();
			ArrayList<BigInteger> divSet1 = Divisors.getSmallDivisors_v1(fac);
			long t1 = System.currentTimeMillis();
			
			// Much much faster!
			SortedMap<BigInteger, Integer> factors = tdiv.factor(fac); // tdiv is sufficient because fac is very smooth
			SortedSet<BigInteger> divSet2 = Divisors.getSmallDivisors/*_v2*/(fac, factors);
			long t2 = System.currentTimeMillis();
			
//			LOG.info("smallDivisors_v1(" + n + "!) = " + divSet1);
//			LOG.info("smallDivisors_v2(" + n + "!) = " + divSet2);
			LOG.info("smallDivisors_v1(" + n + "!) found " + divSet1.size() + " divisors in " + (t1-t0) + "ms");
			LOG.info("smallDivisors_v2(" + n + "!) found " + divSet2.size() + " divisors in " + (t2-t1) + "ms");
			// The divisor count sequence for n! up to 38! is
			// 1, 1, 1, 2, 4, 8, 15, 30, 48, 80, 135, 270, 396, 792, 1296, 2016, 2688, 5376, 7344, 14688, 20520, 30400, 48000, 96000, 121440, 170016, 266112, 338688, 458640, 917280, 1166400, 2332800, 2764800, 3932160, 6082560, 8211456, 9797760, 19595520, 30233088
			// Since no factorial is a square, this is A027423(n) for n>1
		}
	}

	private static void testBiggestDivisorBelowSqrtN() {
		long t0, t1;
		int NCOUNT=10000;
		
		for (int bits = 15; bits<32; bits++) { // getBiggestDivisorBelowSqrtN_small() needs int arguments
			// create test set
			ArrayList<BigInteger> testSet = new ArrayList<>();
			for (int i=0; i<NCOUNT; ) {
				BigInteger n = new BigInteger(bits, RNG);
				if (n.compareTo(I_0)>0) {
					testSet.add(n);
					i++;
				}
			}
			t0 = System.currentTimeMillis();
			for (BigInteger nBig : testSet) {
				Divisors.getBiggestDivisorBelowSqrtN_small(nBig.intValue());
			}
			t1 = System.currentTimeMillis();
			LOG.info("getBiggestDivisorBelowSqrtN_small(" + bits + "bit) took " + (t1-t0) + "ms");
			
			t0 = System.currentTimeMillis();
			for (BigInteger nBig : testSet) {
				Divisors.getBiggestDivisorBelowSqrtN_big(nBig);
			}
			t1 = System.currentTimeMillis();
			LOG.info("getBiggestDivisorBelowSqrtN_big(" + bits + "bit) took " + (t1-t0) + "ms");
		}
	}
	
	/**
	 * Tests.
	 * 
	 * @param args Ignored.
	 */
	public static void main(String[] args) {
    	ConfigUtil.initProject();
    	
    	testSumOfDivisors();
    	testDivisorCounts();
    	testDivisors();
    	testGetSmallDivisors();
    	testBiggestDivisorBelowSqrtN();
	}
}
