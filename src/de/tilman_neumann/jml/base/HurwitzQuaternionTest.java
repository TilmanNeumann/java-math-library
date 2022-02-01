/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2022 Tilman Neumann (www.tilman-neumann.de)
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

import java.math.BigInteger;
import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.Test;

import de.tilman_neumann.test.junit.ClassTest;

public class HurwitzQuaternionTest extends ClassTest {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(HurwitzQuaternionTest.class);

	private static final Random RNG = new Random(42);

	@Test
	public void testQuaternionConversion() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleQuaternionConversion(getRandomNonzeroHurwitzQuaternion(10));
		for (int i=0; i<1000; i++) testSingleQuaternionConversion(getRandomNonzeroHurwitzQuaternion(20));
		for (int i=0; i<1000; i++) testSingleQuaternionConversion(getRandomNonzeroHurwitzQuaternion(200));
	}
	
	private static void testSingleQuaternionConversion(HurwitzQuaternion a) {
		assertEquals(a, new HurwitzQuaternion(a.toRationalQuaternion()));
	}

	@Test
	public void testAddSubtract() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleAddSubtract(getRandomNonzeroHurwitzQuaternion(10), getRandomNonzeroHurwitzQuaternion(10));
		for (int i=0; i<1000; i++) testSingleAddSubtract(getRandomNonzeroHurwitzQuaternion(20), getRandomNonzeroHurwitzQuaternion(10));
		for (int i=0; i<1000; i++) testSingleAddSubtract(getRandomNonzeroHurwitzQuaternion(200), getRandomNonzeroHurwitzQuaternion(40));
	}
	
	private static void testSingleAddSubtract(HurwitzQuaternion a, HurwitzQuaternion b) {
		// here we test the asserts in add(), subtract() as well if HurwitzQuaternion.DEBUG is enabled
		assertEquals(a, a.subtract(b).add(b)); // a = (a-b) + b
		assertEquals(a, a.add(b).subtract(b)); // a = (a+b) - b
		assertEquals(b, a.subtract(b).negate().add(a)); // b = -(a-b) + a
	}

	@Test
	public void testNormVsMultiplicationWithConjugate() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleNormVsMultiplicationWithConjugate(getRandomNonzeroHurwitzQuaternion(10));
		for (int i=0; i<1000; i++) testSingleNormVsMultiplicationWithConjugate(getRandomNonzeroHurwitzQuaternion(20));
		for (int i=0; i<1000; i++) testSingleNormVsMultiplicationWithConjugate(getRandomNonzeroHurwitzQuaternion(200));
	}
	
	private static void testSingleNormVsMultiplicationWithConjugate(HurwitzQuaternion a) {
		BigInteger norm = a.norm();
		HurwitzQuaternion rightProduct = a.multiply(a.conjugate());
		assertEquals("Norm of (" + a + ") = " + norm + " does not equal right multiplication with conjugate result = " + rightProduct, new HurwitzQuaternion(norm, true), rightProduct);
		HurwitzQuaternion leftProduct = a.conjugate().multiply(a);
		assertEquals("Norm of (" + a + ") = " + norm + " does not equal left multiplication with conjugate result = " + leftProduct, new HurwitzQuaternion(norm, true), leftProduct);
	}

	@Test
	public void testMultiplicationByInteger() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleMultiplicationByInteger(getRandomNonzeroHurwitzQuaternion(10), new BigInteger(10, RNG));
		for (int i=0; i<1000; i++) testSingleMultiplicationByInteger(getRandomNonzeroHurwitzQuaternion(20), new BigInteger(10, RNG));
		for (int i=0; i<1000; i++) testSingleMultiplicationByInteger(getRandomNonzeroHurwitzQuaternion(200), new BigInteger(40, RNG));
	}
	
	private static void testSingleMultiplicationByInteger(HurwitzQuaternion a, BigInteger b) {
		assertEquals(a.multiply(new HurwitzQuaternion(b, true)), a.multiply(b));
	}

	@Test
	public void testMultiplication() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleMultiplication(getRandomNonzeroHurwitzQuaternion(10), getRandomNonzeroHurwitzQuaternion(10));
		for (int i=0; i<1000; i++) testSingleMultiplication(getRandomNonzeroHurwitzQuaternion(20), getRandomNonzeroHurwitzQuaternion(10));
		for (int i=0; i<1000; i++) testSingleMultiplication(getRandomNonzeroHurwitzQuaternion(200), getRandomNonzeroHurwitzQuaternion(40));
	}
	
	private static void testSingleMultiplication(HurwitzQuaternion a, HurwitzQuaternion b) {
		// multiplication is not commutative; so we only test asserts in multiply() if HurwitzQuaternion.DEBUG is enabled
		a.multiply(b);
		b.multiply(a);
	}

	@Test
	public void testSquare() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleSquare(getRandomNonzeroHurwitzQuaternion(10));
		for (int i=0; i<1000; i++) testSingleSquare(getRandomNonzeroHurwitzQuaternion(20));
		for (int i=0; i<1000; i++) testSingleSquare(getRandomNonzeroHurwitzQuaternion(200));
	}
	
	private static void testSingleSquare(HurwitzQuaternion a) {
		assertEquals(a.multiply(a), a.square());
	}

	@Test
	public void testLeftDivision() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleLeftDivision(getRandomNonzeroHurwitzQuaternion(10), getRandomNonzeroHurwitzQuaternion(10));
		for (int i=0; i<1000; i++) testSingleLeftDivision(getRandomNonzeroHurwitzQuaternion(20), getRandomNonzeroHurwitzQuaternion(10));
		for (int i=0; i<1000; i++) testSingleLeftDivision(getRandomNonzeroHurwitzQuaternion(200), getRandomNonzeroHurwitzQuaternion(40));
	}
	
	private static void testSingleLeftDivision(HurwitzQuaternion a, HurwitzQuaternion b) {
		HurwitzQuaternion[] divRem = a.leftDivide(b);
		assertEquals("Error computing (" + a + ") / (" + b + "): ", a, b.multiply(divRem[0]).add(divRem[1])); // quotient must be _right_ factor in test multiplication
		assertTrue("Error computing (" + a + ") / (" + b + "): N(d)=" + divRem[1].norm() + " is not smaller than N(b)=" + b.norm(), divRem[1].norm().compareTo(b.norm()) < 0);
	}

	@Test
	public void testRightDivision() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleRightDivision(getRandomNonzeroHurwitzQuaternion(10), getRandomNonzeroHurwitzQuaternion(10));
		for (int i=0; i<1000; i++) testSingleRightDivision(getRandomNonzeroHurwitzQuaternion(20), getRandomNonzeroHurwitzQuaternion(10));
		for (int i=0; i<1000; i++) testSingleRightDivision(getRandomNonzeroHurwitzQuaternion(200), getRandomNonzeroHurwitzQuaternion(40));
	}
	
	private static void testSingleRightDivision(HurwitzQuaternion a, HurwitzQuaternion b) {
		HurwitzQuaternion[] divRem = a.rightDivide(b);
		assertEquals("Error computing (" + a + ") / (" + b + "): ", a, divRem[0].multiply(b).add(divRem[1])); // quotient must be _left_ factor in test multiplication
		assertTrue("Error computing (" + a + ") / (" + b + "): N(d)=" + divRem[1].norm() + " is not smaller than N(b)=" + b.norm(), divRem[1].norm().compareTo(b.norm()) < 0);
	}
	
	@Test
	public void testLeftGcd() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleLeftGcd(getRandomNonzeroHurwitzQuaternion(10), getRandomNonzeroHurwitzQuaternion(10));
		for (int i=0; i<1000; i++) testSingleLeftGcd(getRandomNonzeroHurwitzQuaternion(20), getRandomNonzeroHurwitzQuaternion(10));
		for (int i=0; i<1000; i++) testSingleLeftGcd(getRandomNonzeroHurwitzQuaternion(200), getRandomNonzeroHurwitzQuaternion(40));
	}	
	
	private static void testSingleLeftGcd(HurwitzQuaternion a, HurwitzQuaternion b) {
		HurwitzQuaternion gcd = a.leftGcd(b);
		assertTrue("Error computing gcd(" + a + ", " + b + "): gcd = " + gcd + ", a%gcd=" + a.leftDivide(gcd)[1] + " is not zero", a.leftDivide(gcd)[1].isZero());
		assertTrue("Error computing gcd(" + a + ", " + b + "): gcd = " + gcd + ", b%gcd=" + b.leftDivide(gcd)[1] + " is not zero", b.leftDivide(gcd)[1].isZero());
	}
	
	@Test
	public void testRightGcd() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleRightGcd(getRandomNonzeroHurwitzQuaternion(10), getRandomNonzeroHurwitzQuaternion(10));
		for (int i=0; i<1000; i++) testSingleRightGcd(getRandomNonzeroHurwitzQuaternion(20), getRandomNonzeroHurwitzQuaternion(10));
		for (int i=0; i<1000; i++) testSingleRightGcd(getRandomNonzeroHurwitzQuaternion(200), getRandomNonzeroHurwitzQuaternion(40));
	}	
	
	private static void testSingleRightGcd(HurwitzQuaternion a, HurwitzQuaternion b) {
		HurwitzQuaternion gcd = a.rightGcd(b);
		assertTrue("Error computing gcd(" + a + ", " + b + "): gcd = " + gcd + ", a%gcd=" + a.rightDivide(gcd)[1] + " is not zero", a.rightDivide(gcd)[1].isZero());
		assertTrue("Error computing gcd(" + a + ", " + b + "): gcd = " + gcd + ", b%gcd=" + b.rightDivide(gcd)[1] + " is not zero", b.rightDivide(gcd)[1].isZero());
	}

	private static HurwitzQuaternion getRandomNonzeroHurwitzQuaternion(int bits) {
		BigInteger a, b, c, d;
		do {
			a = new BigInteger(bits, RNG);
			b = new BigInteger(bits, RNG);
			c = new BigInteger(bits, RNG);
			d = new BigInteger(bits, RNG);
		} while (a.equals(I_0) && b.equals(I_0) && c.equals(I_0) && d.equals(I_0));
		
		// randomize sign
		// XXX the randomness of RNG.nextInt() % 2 is surprisingly bad, most numbers seem to be even
		if ((RNG.nextInt() % 2) == 1) a = a.negate();
		if ((RNG.nextInt() % 2 == 1)) b = b.negate();
		if ((RNG.nextInt() % 2) == 1) c = c.negate();
		if ((RNG.nextInt() % 2 == 1)) d = d.negate();
		
		boolean isLipschitz = (RNG.nextInt() % 2) == 0; // randomized
		if (!isLipschitz) {
			// make sure that all coefficients are half-integers
			if (!a.testBit(0)) a = a.add(I_1);
			if (!b.testBit(0)) b = b.add(I_1);
			if (!c.testBit(0)) c = c.add(I_1);
			if (!d.testBit(0)) d = d.add(I_1);
		}
		
		return new HurwitzQuaternion(a, b, c, d, isLipschitz);
	}
}
