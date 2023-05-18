/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2022 Tilman Neumann - tilman.neumann@web.de
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

import static de.tilman_neumann.jml.base.BigIntConstants.*;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;

public class BigRationalTest {

	@Before
	public void setup() {
		ConfigUtil.initProject();
	}
	
	@Test
	public void testRound() {
		assertEquals(I_0, new BigRational(I_1, I_2).round());
		assertEquals(I_1, new BigRational(I_3, I_2).round());
		
		assertEquals(I_1.negate(), new BigRational(I_1.negate(), I_2).round());
		assertEquals(I_2.negate(), new BigRational(I_3.negate(), I_2).round());
		
		assertEquals(I_1.negate(), new BigRational(I_1, I_2.negate()).round());
		assertEquals(I_2.negate(), new BigRational(I_3, I_2.negate()).round());

		assertEquals(I_0, new BigRational(I_1.negate(), I_2.negate()).round());
		assertEquals(I_1, new BigRational(I_3.negate(), I_2.negate()).round());
	}
}
