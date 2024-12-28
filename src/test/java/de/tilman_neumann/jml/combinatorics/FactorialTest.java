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

import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;

import static de.tilman_neumann.jml.base.BigIntConstants.*;

public class FactorialTest {

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}
	
	@Test
	public void testSimpleFactorial() {
		assertEquals(I_1, Factorial.simpleProduct(0));
		assertEquals(I_1, Factorial.simpleProduct(1));
		assertEquals(I_24, Factorial.simpleProduct(4));
		assertEquals(new BigInteger("608281864034267560872252163321295376887552831379210240000000000"), Factorial.simpleProduct(49));
	}

	@Test
	public void testLuschnyFactorial() {
		assertEquals(I_1, Factorial.factorial(0));
		assertEquals(I_1, Factorial.factorial(1));
		assertEquals(I_24, Factorial.factorial(4));
		assertEquals(new BigInteger("608281864034267560872252163321295376887552831379210240000000000"), Factorial.factorial(49));
		
		BigInteger correctFactorial1000 = Factorial.simpleProduct(1000);
		BigInteger luschnyFactorial1000 = Factorial.factorial(1000);
		assertEquals(correctFactorial1000, luschnyFactorial1000);
	}
}
