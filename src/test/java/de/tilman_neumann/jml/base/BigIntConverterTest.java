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
package de.tilman_neumann.jml.base;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;

import static de.tilman_neumann.jml.base.BigIntConstants.*;
import static org.junit.Assert.assertEquals;

public class BigIntConverterTest {

	@Before
	public void setup() {
		ConfigUtil.initProject();
	}

	@Test
	public void testFromDouble() {
		assertEquals(I_2, BigIntConverter.fromDouble(2));
		assertEquals(I_3, BigIntConverter.fromDouble(3));
		assertEquals(I_3, BigIntConverter.fromDouble(Math.PI));
		assertEquals(I_5, BigIntConverter.fromDouble(5.99));
		assertEquals(I_6, BigIntConverter.fromDouble(6.0001));
		assertEquals(I_6.negate(), BigIntConverter.fromDouble(-6.0001));
	}

	@Test
	public void testFromDoubleMulPow2() {
		assertEquals(BigInteger.valueOf(-101), BigIntConverter.fromDoubleMulPow2(-6.333, 4));
		assertEquals(I_6, BigIntConverter.fromDoubleMulPow2(101.333, -4));
	}
}
