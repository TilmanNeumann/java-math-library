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

import static de.tilman_neumann.jml.base.GaussianIntegerConstants.*;
import static de.tilman_neumann.jml.base.BigIntConstants.*;

import java.math.BigInteger;
import java.util.Random;

import org.junit.Test;

import de.tilman_neumann.test.junit.ClassTest;

public class GaussianIntegerTest extends ClassTest {

	private static final Random RNG = new Random(42);

	@Test
	public void testNormVsMultiplicationWithConjugate() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleNormVsMultiplicationWithConjugate(getRandomNonzeroGaussianInteger(10));
		for (int i=0; i<1000; i++) testSingleNormVsMultiplicationWithConjugate(getRandomNonzeroGaussianInteger(20));
		for (int i=0; i<1000; i++) testSingleNormVsMultiplicationWithConjugate(getRandomNonzeroGaussianInteger(200));
	}
	
	private static void testSingleNormVsMultiplicationWithConjugate(GaussianInteger a) {
		BigInteger norm = a.norm();
		GaussianInteger productWithConjugate = a.multiply(a.conjugate());
		assertEquals("Norm of (" + a + ") = " + norm + " does not equal multiplication with conjugate result = " + productWithConjugate, new GaussianInteger(norm, I_0), productWithConjugate);
	}

	@Test
	public void testMultiplicationCommutativity() {
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleMultiplication(getRandomNonzeroGaussianInteger(10), getRandomNonzeroGaussianInteger(10));
		for (int i=0; i<1000; i++) testSingleMultiplication(getRandomNonzeroGaussianInteger(20), getRandomNonzeroGaussianInteger(10));
		for (int i=0; i<1000; i++) testSingleMultiplication(getRandomNonzeroGaussianInteger(200), getRandomNonzeroGaussianInteger(40));
	}
	
	private static void testSingleMultiplication(GaussianInteger a, GaussianInteger b) {
		assertEquals("Multiplication of (" + a + "), (" + b + ") is not commutative", a.multiply(b), b.multiply(a));
	}

	@Test
	public void testDivision() {
		GaussianInteger a = new GaussianInteger(I_28, I_7);
		GaussianInteger b = new GaussianInteger(I_7, I_0);
		GaussianInteger[] divRem = a.divide(b);
		assertEquals(new GaussianInteger(I_4, I_1), divRem[0]); // quotient
		assertEquals(new GaussianInteger(I_0, I_0), divRem[1]); // remainder
		
		testSingleDivision(new GaussianInteger(I_28, I_7), new GaussianInteger(I_7, I_0));
		
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleDivision(getRandomNonzeroGaussianInteger(10), getRandomNonzeroGaussianInteger(10));
		for (int i=0; i<1000; i++) testSingleDivision(getRandomNonzeroGaussianInteger(20), getRandomNonzeroGaussianInteger(10));
		for (int i=0; i<1000; i++) testSingleDivision(getRandomNonzeroGaussianInteger(200), getRandomNonzeroGaussianInteger(40));
	}
	
	private static void testSingleDivision(GaussianInteger a, GaussianInteger b) {
		GaussianInteger[] divRem = a.divide(b);
		assertEquals("Error computing (" + a + ") / (" + b + "): ", a, b.multiply(divRem[0]).add(divRem[1]));
		assertTrue("Error computing (" + a + ") / (" + b + "): N(d)=" + divRem[1].norm() + " is not smaller than N(b)=" + b.norm(), divRem[1].norm().compareTo(b.norm()) < 0);
	}
	
	@Test
	public void testGcd() {
		GaussianInteger a, b, gcd;
		a = new GaussianInteger(I_28, I_7);
		b = new GaussianInteger(I_4, I_1);
		gcd = a.gcd(b);
		assertEquals(b, gcd);
		
		a = new GaussianInteger(I_2, I_1);
		b = new GaussianInteger(I_0, I_1);
		gcd = a.gcd(b);
		assertEquals(GI_1, gcd);
		
		// mass tests in various ranges
		for (int i=0; i<1000; i++) testSingleGcd(getRandomNonzeroGaussianInteger(10), getRandomNonzeroGaussianInteger(10));
		for (int i=0; i<1000; i++) testSingleGcd(getRandomNonzeroGaussianInteger(20), getRandomNonzeroGaussianInteger(10));
		for (int i=0; i<1000; i++) testSingleGcd(getRandomNonzeroGaussianInteger(200), getRandomNonzeroGaussianInteger(40));
	}	
	
	private static void testSingleGcd(GaussianInteger a, GaussianInteger b) {
		GaussianInteger gcd = a.gcd(b);
		assertTrue("Error computing gcd(" + a + ", " + b + "): gcd = " + gcd + ", a%gcd=" + a.divide(gcd)[1] + " is not zero", a.divide(gcd)[1].isZero());
		assertTrue("Error computing gcd(" + a + ", " + b + "): gcd = " + gcd + ", b%gcd=" + b.divide(gcd)[1] + " is not zero", b.divide(gcd)[1].isZero());
	}
	
	private static GaussianInteger getRandomNonzeroGaussianInteger(int bits) {
		BigInteger x, y;
		do {
			x = new BigInteger(bits, RNG);
			y = new BigInteger(bits, RNG);
		} while (x.equals(I_0) && y.equals(I_0));
		
		// randomize sign
		if ((RNG.nextInt() % 2) == 1) x = x.negate();
		if ((RNG.nextInt() % 2 == 1)) y = y.negate();
		
		return new GaussianInteger(x, y);
	}
}
