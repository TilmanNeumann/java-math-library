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
package de.tilman_neumann.jml;

import static de.tilman_neumann.jml.base.BigIntConstants.*;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Implementations for finding all divisors of an integer.
 * 
 * @author Tilman Neumann
 */
public class DivisorsTest {

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}

	@Test
	public void testSumOfDivisorsForSmallIntegers() {
		assertEquals(I_0, Divisors.sumOfDivisors_v1(I_0));
		// reference data from https://oeis.org/A000203, starts at n=1
		List<Integer> reference = List.of(1, 3, 4, 7, 6, 12, 8, 15, 13, 18, 12, 28, 14, 24, 24, 31, 18, 39, 20, 42, 32, 36, 24, 60, 31, 42, 40, 56, 30, 72, 32, 63, 48, 54, 48, 91, 38, 60, 56, 90, 42, 96, 44, 84, 78, 72, 48, 124, 57, 93, 72, 98, 54, 120, 72, 120, 80, 90, 60, 168, 62, 96, 104, 127, 84, 144, 68, 126, 96, 144);
		for (int i=0; i<reference.size(); i++) {
			BigInteger n = BigInteger.valueOf(i+1);
			assertEquals(BigInteger.valueOf(reference.get(i)), Divisors.sumOfDivisors_v1(n));
		}
	}
}
