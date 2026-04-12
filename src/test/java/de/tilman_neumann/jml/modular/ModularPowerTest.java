/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2026 Tilman Neumann - tilman.neumann@web.de
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
package de.tilman_neumann.jml.modular;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

public class ModularPowerTest {
	private static final Logger LOG = LogManager.getLogger(ModularPowerTest.class);
	
	private static final int NCOUNT = 100000;
	
	private static final Random RNG = new Random();
	
	private static long[] a_arr = new long[NCOUNT];
	private static long[] b_arr = new long[NCOUNT];
	private static long[] c_arr = new long[NCOUNT];
	
	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		
		for (int i=0; i<NCOUNT; i++) {
			a_arr[i] = getPositiveRandomLong();
			b_arr[i] = getPositiveRandomLong();
			c_arr[i] = getPositiveRandomLong();
		}
	}

	private static long getPositiveRandomLong() {
		long n = RNG.nextLong();
		while (n == 0) n = RNG.nextLong();
		return Math.abs(n);
	}
	
	@Test
	public void testModularPowerWithLongModulus() {
		ModularPower modPow = new ModularPower();
		
		for (int i=0; i<NCOUNT; i++) {
			long a = a_arr[i];
			long b = b_arr[i];
			long c = c_arr[i];
			long p = modPow.modPow(a, b, c);
			BigInteger pCorrect = BigInteger.valueOf(a).modPow(BigInteger.valueOf(b), BigInteger.valueOf(c));
			if (!pCorrect.equals(BigInteger.valueOf(p))) {
				LOG.error("Expected " + a + " ^ " + b + "(mod " + c + ") = " + pCorrect + ", but it was " + p);
			}
			assertEquals(pCorrect, BigInteger.valueOf(p));
		}
	}
}
