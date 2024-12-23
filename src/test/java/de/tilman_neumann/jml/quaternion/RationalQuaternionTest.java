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
package de.tilman_neumann.jml.quaternion;

import static de.tilman_neumann.jml.base.BigIntConstants.*;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;

import de.tilman_neumann.jml.base.BigRational;
import de.tilman_neumann.util.ConfigUtil;

public class RationalQuaternionTest {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(RationalQuaternionTest.class);

	private static final Random RNG = new Random(42);

	@Before
	public void setup() {
		ConfigUtil.initProject();
	}

	@Test
	public void testAddSubtract() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleAddSubtract(getRandomNonzeroRationalQuaternion(10), getRandomNonzeroRationalQuaternion(10));
		for (int i=0; i<1000; i++) testSingleAddSubtract(getRandomNonzeroRationalQuaternion(20), getRandomNonzeroRationalQuaternion(10));
		for (int i=0; i<1000; i++) testSingleAddSubtract(getRandomNonzeroRationalQuaternion(200), getRandomNonzeroRationalQuaternion(40));
	}
	
	private static void testSingleAddSubtract(RationalQuaternion a, RationalQuaternion b) {
		// here we test the asserts in add(), subtract() as well if RationalQuaternion.DEBUG is enabled
		assertEquals(a, a.subtract(b).add(b)); // a = (a-b) + b
		assertEquals(a, a.add(b).subtract(b)); // a = (a+b) - b
		assertEquals(b, a.subtract(b).negate().add(a)); // b = -(a-b) + a
	}

	@Test
	public void testNormVsMultiplicationWithConjugate() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleNormVsMultiplicationWithConjugate(getRandomNonzeroRationalQuaternion(10));
		for (int i=0; i<1000; i++) testSingleNormVsMultiplicationWithConjugate(getRandomNonzeroRationalQuaternion(20));
		for (int i=0; i<1000; i++) testSingleNormVsMultiplicationWithConjugate(getRandomNonzeroRationalQuaternion(200));
	}
	
	private static void testSingleNormVsMultiplicationWithConjugate(RationalQuaternion a) {
		BigRational norm = a.norm();
		RationalQuaternion rightProduct = a.multiply(a.conjugate());
		assertEquals("Norm of (" + a + ") = " + norm + " does not equal right multiplication with conjugate result = " + rightProduct, new RationalQuaternion(norm, BigRational.ZERO, BigRational.ZERO, BigRational.ZERO), rightProduct);
		RationalQuaternion leftProduct = a.conjugate().multiply(a);
		assertEquals("Norm of (" + a + ") = " + norm + " does not equal left multiplication with conjugate result = " + leftProduct, new RationalQuaternion(norm, BigRational.ZERO, BigRational.ZERO, BigRational.ZERO), leftProduct);
	}

	@Test
	public void testMultiplicationByRational() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleMultiplicationByInteger(getRandomNonzeroRationalQuaternion(10), getRandomBigRational(10));
		for (int i=0; i<1000; i++) testSingleMultiplicationByInteger(getRandomNonzeroRationalQuaternion(20), getRandomBigRational(10));
		for (int i=0; i<1000; i++) testSingleMultiplicationByInteger(getRandomNonzeroRationalQuaternion(200), getRandomBigRational(40));
	}
	
	private static void testSingleMultiplicationByInteger(RationalQuaternion a, BigRational b) {
		assertEquals(a.multiply(new RationalQuaternion(b, BigRational.ZERO, BigRational.ZERO, BigRational.ZERO)), a.multiply(b));
	}

	@Test
	public void testMultiplication() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleMultiplication(getRandomNonzeroRationalQuaternion(10), getRandomNonzeroRationalQuaternion(10));
		for (int i=0; i<1000; i++) testSingleMultiplication(getRandomNonzeroRationalQuaternion(20), getRandomNonzeroRationalQuaternion(10));
		for (int i=0; i<1000; i++) testSingleMultiplication(getRandomNonzeroRationalQuaternion(200), getRandomNonzeroRationalQuaternion(40));
	}
	
	private static void testSingleMultiplication(RationalQuaternion a, RationalQuaternion b) {
		// multiplication is not commutative; so we only test asserts in multiply() if RationalQuaternion.DEBUG is enabled
		a.multiply(b);
		b.multiply(a);
	}

	@Test
	public void testSquare() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleSquare(getRandomNonzeroRationalQuaternion(5));
		for (int i=0; i<1000; i++) testSingleSquare(getRandomNonzeroRationalQuaternion(20));
		for (int i=0; i<1000; i++) testSingleSquare(getRandomNonzeroRationalQuaternion(200));
	}
	
	private static void testSingleSquare(RationalQuaternion a) {
		assertEquals(a.multiply(a), a.square());
	}

	@Test
	public void testLeftDivision() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleLeftDivision(getRandomNonzeroRationalQuaternion(10), getRandomNonzeroRationalQuaternion(10));
		for (int i=0; i<1000; i++) testSingleLeftDivision(getRandomNonzeroRationalQuaternion(20), getRandomNonzeroRationalQuaternion(10));
		for (int i=0; i<1000; i++) testSingleLeftDivision(getRandomNonzeroRationalQuaternion(200), getRandomNonzeroRationalQuaternion(40));
	}
	
	private static void testSingleLeftDivision(RationalQuaternion a, RationalQuaternion b) {
		RationalQuaternion quotient = a.leftDivide(b); // no remainder!
		assertEquals("Error computing (" + a + ") / (" + b + "): ", a.normalize(), b.multiply(quotient).normalize()); // quotient must be _right_ factor in test multiplication
	}

	@Test
	public void testRightDivision() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleRightDivision(getRandomNonzeroRationalQuaternion(10), getRandomNonzeroRationalQuaternion(10));
		for (int i=0; i<1000; i++) testSingleRightDivision(getRandomNonzeroRationalQuaternion(20), getRandomNonzeroRationalQuaternion(10));
		for (int i=0; i<1000; i++) testSingleRightDivision(getRandomNonzeroRationalQuaternion(200), getRandomNonzeroRationalQuaternion(40));
	}
	
	private static void testSingleRightDivision(RationalQuaternion a, RationalQuaternion b) {
		RationalQuaternion quotient = a.rightDivide(b); // no remainder!
		assertEquals("Error computing (" + a + ") / (" + b + "): ", a.normalize(), quotient.multiply(b).normalize()); // quotient must be _left_ factor in test multiplication
	}
	
	private static RationalQuaternion getRandomNonzeroRationalQuaternion(int bits) {
		BigRational a, b, c, d;
		do {
			a = getRandomBigRational(bits);
			b = getRandomBigRational(bits);
			c = getRandomBigRational(bits);
			d = getRandomBigRational(bits);
		} while (a.equals(I_0) && b.equals(I_0) && c.equals(I_0) && d.equals(I_0));
		
		return new RationalQuaternion(a, b, c, d);
	}
	
	@SuppressWarnings("unused")
	private static BigRational getRandomNonzeroBigRational(int bits) {
		BigInteger a, b;
		do {
			a = new BigInteger(bits, RNG);
			b = new BigInteger(bits, RNG);
		} while (a.equals(I_0) || b.equals(I_0));
		
		// randomize sign
		// XXX the randomness of RNG.nextInt() % 2 is surprisingly bad, most numbers seem to be even
		if ((RNG.nextInt() % 2) == 1) a = a.negate();
		if ((RNG.nextInt() % 2 == 1)) b = b.negate();

		return new BigRational(a, b);
	}
	
	private static BigRational getRandomBigRational(int bits) {
		BigInteger a = new BigInteger(bits, RNG); //  zero allowed
		BigInteger b;
		do {
			b = new BigInteger(bits, RNG);
		} while (b.equals(I_0));
		
		// randomize sign
		// XXX the randomness of RNG.nextInt() % 2 is surprisingly bad, most numbers seem to be even
		if ((RNG.nextInt() % 2) == 1) a = a.negate();
		if ((RNG.nextInt() % 2 == 1)) b = b.negate();

		return new BigRational(a, b);
	}
}
