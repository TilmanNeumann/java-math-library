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

import static de.tilman_neumann.jml.base.BigIntConstants.*;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import de.tilman_neumann.jml.base.BigIntGrid;
import de.tilman_neumann.util.ConfigUtil;

public class BinomialTest {
	private static final Logger LOG = LogManager.getLogger(BinomialTest.class);

	@Before
	public void setup() {
		ConfigUtil.initProject();
		
		// log some
		int max = 10;
		BigIntGrid grid = new BigIntGrid("n", -max, "k", -max); // works for negative k, too
		for (int n=-max; n<=max; n++) {
			ArrayList<BigInteger> row = new ArrayList<>();
	    	for (int k=-max; k<=max; k++) {
	    		row.add(Binomial.binomial(n,k));
	    	}
	    	grid.add(row);
		}
		LOG.info("binomial coefficients computed by class Binomial:\n" + grid);
	}
	
	@Test
	public void testAboutZeroArguments() {
		assertEquals(I_1, Binomial.binomial(-1,0));
		assertEquals(I_1, Binomial.binomial(0,0));
		assertEquals(I_1, Binomial.binomial(1,0));
		
		assertEquals(I_1, Binomial.binomial(-1,-1));
		assertEquals(I_0, Binomial.binomial(0,-1));
		assertEquals(I_0, Binomial.binomial(1,-1));
		
		assertEquals(I_MINUS_1, Binomial.binomial(-1,1));
		assertEquals(I_0, Binomial.binomial(0,1));
		assertEquals(I_1, Binomial.binomial(1,1));

		assertEquals(I_1, Binomial.binomial(-10, 0));
		assertEquals(I_1, Binomial.binomial(10, 0));
	}
	
	@Test
	public void testNegativeNAndNegativeK() {
		assertEquals(I_0, Binomial.binomial(-10,-1));
		assertEquals(I_0, Binomial.binomial(-10,-5));
		assertEquals(I_1, Binomial.binomial(-10,-10));
		assertEquals(BigInteger.valueOf(-56), Binomial.binomial(-6,-9));
	}
	
	@Test
	public void testNegativeNAndPositiveK() {
		assertEquals(I_10.negate(), Binomial.binomial(-10,1));
		assertEquals(BigInteger.valueOf(-2002), Binomial.binomial(-10,5));
		assertEquals(BigInteger.valueOf(92378), Binomial.binomial(-10,10));
		assertEquals(BigInteger.valueOf(-2002), Binomial.binomial(-6,9));
	}
	
	@Test
	public void testPositiveNAndNegativeK() {
		assertEquals(I_0, Binomial.binomial(10,-1));
		assertEquals(I_0, Binomial.binomial(10,-5));
		assertEquals(I_0, Binomial.binomial(10,-10));
		assertEquals(I_0, Binomial.binomial(6,-9));
	}
	
	@Test
	public void testPositiveNAndPositiveK() {
		assertEquals(I_10, Binomial.binomial(10,1));
		assertEquals(BigInteger.valueOf(252), Binomial.binomial(10,5));
		assertEquals(I_1, Binomial.binomial(10,10));
		assertEquals(I_0, Binomial.binomial(6,9));
		assertEquals(I_6, Binomial.binomial(4,2));
		assertEquals(BigInteger.valueOf(35), Binomial.binomial(7,4));
		assertEquals(BigInteger.valueOf(70), Binomial.binomial(8,4));
	}
}
