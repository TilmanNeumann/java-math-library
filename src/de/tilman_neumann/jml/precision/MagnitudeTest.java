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
package de.tilman_neumann.jml.precision;

import java.math.BigDecimal;
import java.math.BigInteger;

import de.tilman_neumann.test.junit.ClassTest;

import static de.tilman_neumann.jml.base.BigIntConstants.*;
import static de.tilman_neumann.jml.base.BigDecimalConstants.*;

public class MagnitudeTest extends ClassTest {
	
	public void testBigIntegerBitOperations() {
		assertEquals(0, I_0.bitLength());
		assertEquals(1, I_1.bitLength());
		assertEquals(2, I_2.bitLength());
		assertEquals(2, I_3.bitLength());
		assertEquals(3, I_4.bitLength());
		
		assertEquals(0, I_0.bitCount());
		assertEquals(1, I_1.bitCount());
		assertEquals(1, I_2.bitCount());
		assertEquals(2, I_3.bitCount());
		assertEquals(1, I_4.bitCount());
		// bitCount counts the number of set bits
		
		assertEquals(-1, I_0.getLowestSetBit()); // !
		assertEquals(0, I_1.getLowestSetBit());
		assertEquals(1, I_2.getLowestSetBit());
		assertEquals(0, I_3.getLowestSetBit());
		assertEquals(2, I_4.getLowestSetBit());
		// getLowestSetBit() gives 0 for odd numbers, -1 for zero
	}
	
	public void testDigits() {
		assertEquals(0, Magnitude.of(I_0));
		assertEquals(0, Magnitude.of(I_0.negate()));
		assertEquals(1, Magnitude.of(I_1));
		assertEquals(1, Magnitude.of(I_1.negate()));
		assertEquals(1, Magnitude.of(I_9));
		assertEquals(1, Magnitude.of(I_9.negate()));
		assertEquals(2, Magnitude.of(I_10));
		assertEquals(2, Magnitude.of(I_10.negate()));
		assertEquals(3, Magnitude.of(BigInteger.valueOf(999)));
		assertEquals(3, Magnitude.of(BigInteger.valueOf(-999)));
		assertEquals(4, Magnitude.of(I_1E3));
		assertEquals(4, Magnitude.of(I_1E3.negate()));
	}
	
	public void testBits() {
		assertEquals(0, Magnitude.bitsOf(I_0));
		assertEquals(0, Magnitude.bitsOf(I_0.negate()));
		assertEquals(1, Magnitude.bitsOf(I_1));
		assertEquals(1, Magnitude.bitsOf(I_1.negate()));
		assertEquals(2, Magnitude.bitsOf(I_2));
		assertEquals(2, Magnitude.bitsOf(I_2.negate()));
		assertEquals(2, Magnitude.bitsOf(I_3));
		assertEquals(2, Magnitude.bitsOf(I_3.negate()));
		assertEquals(3, Magnitude.bitsOf(I_4));
		assertEquals(3, Magnitude.bitsOf(I_4.negate()));
	}
	
	public void testZero() {
		assertEquals(I_0, F_0.unscaledValue());
		assertEquals(0, F_0.scale());
		assertEquals(10, F_0.setScale(10).scale());
		assertEquals(-10, F_0.setScale(-10).scale());
		// it is possible to set the scale of 0 to arbitrary values
	}

	public void testMagnitude() {
		assertEquals(8, Magnitude.of(BigDecimal.valueOf(23450000.000)));
		assertEquals(4, Magnitude.of(BigDecimal.valueOf(2345.456)));
		assertEquals(1, Magnitude.of(BigDecimal.valueOf(7.0456)));
		assertEquals(-1, Magnitude.of(BigDecimal.valueOf(0.0456)));
		assertEquals(0, Magnitude.of(F_0));
		assertEquals(-10, Magnitude.of(F_0.setScale(10)));
		assertEquals(14, Magnitude.of(new BigDecimal("12345678901234.001")));
		assertEquals(4, Magnitude.of(new BigDecimal("2323.001")));
		assertEquals(1, Magnitude.of(new BigDecimal("1.000000001")));
		assertEquals(1, Magnitude.of(F_1));
		assertEquals(-2, Magnitude.of(new BigDecimal("0.001")));
		assertEquals(-2, Magnitude.of(new BigDecimal("-0.001")));
		assertEquals(3, Magnitude.of(new BigDecimal("-232.001")));
	}
}
