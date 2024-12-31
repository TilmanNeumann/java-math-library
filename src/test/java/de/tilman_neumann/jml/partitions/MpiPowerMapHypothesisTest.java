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
package de.tilman_neumann.jml.partitions;

import java.math.BigInteger;
import java.util.SortedSet;

import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.Divisors;
import de.tilman_neumann.util.ConfigUtil;

import static org.junit.Assert.assertEquals;

public class MpiPowerMapHypothesisTest {
	
	private static final Logger LOG = LogManager.getLogger(MpiPowerMapHypothesisTest.class);
	private static final boolean LOG_ALL = false;
	private static final boolean LOG_FEW = false;
	
	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}
	
	/**
	 * Test a relationship between the set of divisors of some N and the "MPI powermap" for that N.
	 * The hypothesis is: The number of divisors of N == the number of entries in the powermap + the number of unit entries (prime factors) + 1 (for the empty set)
	 * 
	 * I confirmed the hypothesis for N from 0 to 60.000.000 in a 30-minute-run on a Ryzen 3900X.
	 * 
	 * The sequence of the number of divisors is A000005(n) = the number of divisors of n = 
	 * (0,) 1, 2, 2, 3, 2, 4, 2, 4, 3, 4, 2, 6, 2, 4, 4, 5, 2, 6, 2, 6, 4, 4, 2, 8, 3, 4, 4, 6, 2, 8, 2, 6, 4, 4, 4, 9, 2, 4, 4, 8, 2, 8, 2, 6, 6, 4, 2, 10, 3, 6, ...
	 * and the sequence of power map entries is  A055212(n) = Number of composite divisors of n =
	 * (0,) 0, 0, 0, 1, 0, 1, 0, 2, 1, 1, 0, 3, 0, 1, 1, 3, 0, 3, 0, 3, 1, 1, 0, 5, 1, 1, 2, 3, 0, 4, 0, 4, 1, 1, 1, 6, 0, 1, 1, 5, 0, 4, 0, 3, 3, 1, 0,  7, 1, 3, ...
	 */
	@Test
	public void testPowerMapHypothesis() {
		ConfigUtil.initProject();
		for (int n=0; n<100000; n++) {
			BigInteger bigN = BigInteger.valueOf(n);
			SortedSet<BigInteger> divisors = Divisors.getDivisors(bigN);
			int numberOfDivisors = divisors.size();
			PrimePowers primePowers = PrimePowers_DefaultImpl.valueOf(bigN);
			MpiPowerMap powerMap = MpiPowerMap.create(primePowers);
			int powerMapSize = powerMap.size();
			if (LOG_ALL) {
				LOG.debug("n=" + n + " has " + numberOfDivisors + " divisors, and power map has " + powerMapSize + " entries");
			} else if (LOG_FEW) {
				if (n%10000 == 0) LOG.debug("n=" + n + " has " + numberOfDivisors + " divisors, and power map has " + powerMapSize + " entries");
			}
			int correctedPowerMapSize = n>0 ? powerMapSize + primePowers.getDim() + 1 : 0;
			// the power map is missing the unit entries (only one prime) and the empty entry!
			if (numberOfDivisors != correctedPowerMapSize) {
				LOG.error("n = " + n + " failed powerMap hypothesis: numberOfDivisors = " + numberOfDivisors + ", correctedPowerMapSize = " + correctedPowerMapSize);
				LOG.error("divisors = " + divisors);
				LOG.error("powerMap = " + powerMap);
			}
			assertEquals(numberOfDivisors, correctedPowerMapSize);
		}
	}
}
