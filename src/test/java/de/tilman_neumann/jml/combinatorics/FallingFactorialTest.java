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
import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.jml.base.BigIntGrid;
import de.tilman_neumann.util.ConfigUtil;

public class FallingFactorialTest {
	private static final Logger LOG = LogManager.getLogger(FallingFactorialTest.class);

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		
		// log some
    	int max = 10;
    	BigIntGrid grid = new BigIntGrid("n", -max, "k", 0); // negative k not supported, results would be rational
    	for (int n=-max; n<=max; n++) {
    		ArrayList<BigInteger> row = new ArrayList<>();
        	for (int k=0; k<=max; k++) {
        		row.add(FallingFactorial.fallingFactorial(n,k));
        	}
        	grid.add(row);
    	}
    	LOG.info("fallingFactorials:\n" + grid);
	}
	
	@Test
	public void testAboutZeroArguments() {
		assertEquals(I_1, FallingFactorial.fallingFactorial(-1,0));
		assertEquals(I_1, FallingFactorial.fallingFactorial(0,0));
		assertEquals(I_1, FallingFactorial.fallingFactorial(1,0));
		
		assertEquals(I_MINUS_1, FallingFactorial.fallingFactorial(-1,1));
		assertEquals(I_0, FallingFactorial.fallingFactorial(0,1));
		assertEquals(I_1, FallingFactorial.fallingFactorial(1,1));

		assertEquals(I_1, FallingFactorial.fallingFactorial(-10, 0));
		assertEquals(I_1, FallingFactorial.fallingFactorial(10, 0));
	}
	
	@Test
	public void testNegativeN() {
		assertEquals(I_10.negate(), FallingFactorial.fallingFactorial(-10, 1));
		assertEquals(BigInteger.valueOf(-240240), FallingFactorial.fallingFactorial(-10, 5));
		assertEquals(BigInteger.valueOf(335221286400L), FallingFactorial.fallingFactorial(-10, 10));
		assertEquals(BigInteger.valueOf(-726485760), FallingFactorial.fallingFactorial(-6, 9));
	}
	
	@Test
	public void testPositiveN() {
		assertEquals(I_10, FallingFactorial.fallingFactorial(10, 1));
		assertEquals(BigInteger.valueOf(30240), FallingFactorial.fallingFactorial(10, 5));
		assertEquals(BigInteger.valueOf(3628800), FallingFactorial.fallingFactorial(10, 10));
		assertEquals(I_0, FallingFactorial.fallingFactorial(6, 9));
	}
}
