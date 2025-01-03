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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;

import static de.tilman_neumann.jml.base.BigIntConstants.*;
import static org.junit.Assert.assertEquals;

/**
 * Tests for Chebyshev polynomials.
 * @author Tilman Neumann
 */
public class ChebyshevPolynomialsTest {

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}

	@Test
	public void testSmall() {
		for (int n=0; n<=4; n++) {
			for (BigInteger x=I_0; x.compareTo(I_10)<0; x=x.add(I_1)) {
				BigDecimal T_n_of_x = ChebyshevPolynomials.ChebyshevT(n, new BigDecimal(x));
				BigDecimal closed = ChebyshevPolynomials.ChebyshevTClosed(n, new BigDecimal(x));
				assertEquals(T_n_of_x, closed);
			}
		}
		for (int n=0; n<=4; n++) {
			for (BigInteger x=I_0; x.compareTo(I_10)<0; x=x.add(I_1)) {
				BigDecimal U_n_of_x = ChebyshevPolynomials.ChebyshevU(n, new BigDecimal(x));
				BigDecimal closed = ChebyshevPolynomials.ChebyshevUClosed(n, new BigDecimal(x));
				assertEquals(U_n_of_x, closed);
			}
		}
	}
}
