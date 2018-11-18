/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018 Tilman Neumann (www.tilman-neumann.de)
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
package de.tilman_neumann.jml.transcendental;

import java.math.BigDecimal;

import de.tilman_neumann.jml.precision.Scale;
import de.tilman_neumann.test.junit.ClassTest;

import static de.tilman_neumann.jml.base.BigDecimalConstants.*;

/**
 * Test class for floating point Exp function
 *
 * @author Tilman Neumann
 */
public class ExpTest extends ClassTest {

	public void testExpZero() {
		// exp(0) with 10 digits precision
		BigDecimal one = Exp.exp(F_0, Scale.valueOf(10));
		assertEquals(F_1.setScale(10), one);
	}
}
