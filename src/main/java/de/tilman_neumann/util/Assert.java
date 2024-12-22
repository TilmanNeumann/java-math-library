/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2024 Tilman Neumann - tilman.neumann@web.de
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
package de.tilman_neumann.util;

import java.math.BigInteger;

/**
 * Assertions for quality tests in production code.<br><br>
 * 
 * <strong>
 * These checks should only be used wrapped into simple if-statements that can be evaluated at compile-time,
 * like <code>if (DEBUG)</code>, where <code>DEBUG</code> is a Java boolean constant.
 * </strong>
 */
public class Assert {
	
	// boolean comparison
	
	public static void assertEquals(boolean left, boolean right) {
		if (! (left == right)) {
			throw new AssertionError("Assertion failed: " + left + "==" + right);
		}
	}

	// byte/short/int/long comparisons
	
	public static void assertSmaller(long left, long right) {
		if (! (left < right)) {
			throw new AssertionError("Assertion failed: " + left + "<" + right);
		}
	}
	
	public static void assertSmallerEquals(long left, long right) {
		if (! (left <= right)) {
			throw new AssertionError("Assertion failed: " + left + "<=" + right);
		}
	}
	
	public static void assertEquals(long left, long right) {
		if (! (left == right)) {
			throw new AssertionError("Assertion failed: " + left + "==" + right);
		}
	}
	
	public static void assertGreaterEquals(long left, long right) {
		if (! (left >= right)) {
			throw new AssertionError("Assertion failed: " + left + ">=" + right);
		}
	}
	
	public static void assertGreater(long left, long right) {
		if (! (left > right)) {
			throw new AssertionError("Assertion failed: " + left + ">" + right);
		}
	}
	
	// BigInteger comparison
	
	public static void assertEquals(BigInteger left, BigInteger right) {
		if (left == right) {
			// works for null==null as well as having the same object
			return;
		}
		if (left == null || ! (left.equals(right))) {
			throw new AssertionError("Assertion failed: " + left + "==" + right);
		}
	}
}
