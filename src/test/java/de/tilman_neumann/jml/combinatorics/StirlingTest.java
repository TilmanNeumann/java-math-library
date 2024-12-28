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
package de.tilman_neumann.jml.combinatorics;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.jml.base.BigIntCollectionUtil;
import de.tilman_neumann.util.ConfigUtil;

import static de.tilman_neumann.jml.base.BigIntConstants.*;

public class StirlingTest {
	private static final Logger LOG = LogManager.getLogger(StirlingTest.class);

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		printFirstStirling1s();
	}

	private static void printFirstStirling1s() {
		for (int n=0; n<=10; n++) {
			ArrayList<BigInteger> stirlings1 = new ArrayList<>();
			for (int i=1; i<=n; i++) {
				BigInteger stirling1 = Stirling.stirling1(n, i);
				stirlings1.add(stirling1);
			}
			LOG.info("1st stirling numbers (" + n + ", i=1..n) = " + stirlings1);
			LOG.info("sum of row elements = " + BigIntCollectionUtil.sum(stirlings1));
			LOG.info("sum of absolute row elements = " + BigIntCollectionUtil.absSum(stirlings1));
		}
    }

	@Test
	public void testStirling1Recurrent() {
		assertEquals(I_1, Stirling.stirling1Recurrent(0, 0));
		assertEquals(I_0, Stirling.stirling1Recurrent(1, 0));
		
		assertEquals(I_0, Stirling.stirling1Recurrent(4, 0));
		assertEquals(I_6.negate(), Stirling.stirling1Recurrent(4, 1));
		assertEquals(I_11, Stirling.stirling1Recurrent(4, 2));
		assertEquals(I_6.negate(), Stirling.stirling1Recurrent(4, 3));
		assertEquals(I_1, Stirling.stirling1Recurrent(4, 4));
		
		assertEquals(BigInteger.valueOf(-8409500), Stirling.stirling1Recurrent(11, 4));
	}
	
	@Test
	public void testStirling1ByStirling2() {
		assertEquals(I_1, Stirling.stirling1ByStirling2(0, 0));
		assertEquals(I_0, Stirling.stirling1ByStirling2(1, 0));
		
		assertEquals(I_0, Stirling.stirling1ByStirling2(4, 0));
		assertEquals(I_6.negate(), Stirling.stirling1ByStirling2(4, 1));
		assertEquals(I_11, Stirling.stirling1ByStirling2(4, 2));
		assertEquals(I_6.negate(), Stirling.stirling1ByStirling2(4, 3));
		assertEquals(I_1, Stirling.stirling1ByStirling2(4, 4));
		
		assertEquals(BigInteger.valueOf(-8409500), Stirling.stirling1ByStirling2(11, 4));
	}
	
	@Test
	public void testStirling1ByGF() {
		assertEquals(I_1, Stirling.stirling1ByGF(0, 0));
		assertEquals(I_0, Stirling.stirling1ByGF(1, 0));
		
		assertEquals(I_0, Stirling.stirling1ByGF(4, 0));
		assertEquals(I_6.negate(), Stirling.stirling1ByGF(4, 1));
		assertEquals(I_11, Stirling.stirling1ByGF(4, 2));
		assertEquals(I_6.negate(), Stirling.stirling1ByGF(4, 3));
		assertEquals(I_1, Stirling.stirling1ByGF(4, 4));
		
		assertEquals(BigInteger.valueOf(-8409500), Stirling.stirling1ByGF(11, 4));
	}
	
	@Test
	public void testStirling1WithMemory() {
		assertEquals(I_1, Stirling.stirling1WithMemory(0, 0));
		assertEquals(I_0, Stirling.stirling1WithMemory(1, 0));
		
		assertEquals(I_0, Stirling.stirling1WithMemory(4, 0));
		assertEquals(I_6.negate(), Stirling.stirling1WithMemory(4, 1));
		assertEquals(I_11, Stirling.stirling1WithMemory(4, 2));
		assertEquals(I_6.negate(), Stirling.stirling1WithMemory(4, 3));
		assertEquals(I_1, Stirling.stirling1WithMemory(4, 4));
		
		assertEquals(BigInteger.valueOf(-8409500), Stirling.stirling1WithMemory(11, 4));
	}
	
	@Test
	public void testStirling2() {
		assertEquals(I_0, Stirling.stirling2(0, 0));
		assertEquals(I_0, Stirling.stirling2(1, 0));
		
		assertEquals(I_0, Stirling.stirling2(4, 0));
		assertEquals(I_1, Stirling.stirling2(4, 1));
		assertEquals(I_7, Stirling.stirling2(4, 2));
		assertEquals(I_6, Stirling.stirling2(4, 3));
		assertEquals(I_1, Stirling.stirling2(4, 4));
		
		assertEquals(BigInteger.valueOf(145750), Stirling.stirling2(11, 4));
	}
}
