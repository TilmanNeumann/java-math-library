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
package de.tilman_neumann.jml.roots;

import java.math.BigDecimal;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.precision.Scale;
import de.tilman_neumann.test.junit.ClassTest;

/**
 * @author Tilman Neumann
 */
public class SqrtTest extends ClassTest {
	private static final Logger LOG = Logger.getLogger(SqrtTest.class);
	
	public void testSpecialCases() {
		BigDecimal input = new BigDecimal("36.0000090000");
		BigDecimal sqrt = SqrtReal.sqrt(input, Scale.valueOf(5));
		LOG.debug("sqrt(" + input + ") = " + sqrt);
	}
	
	public void testBigArgs() {
		BigDecimal a = new BigDecimal("1312596401028278160");
		BigDecimal sqrt = SqrtReal.sqrt(a, Scale.valueOf(5));
		LOG.debug("sqrt(" + a + ") = " + sqrt);
	}
}
