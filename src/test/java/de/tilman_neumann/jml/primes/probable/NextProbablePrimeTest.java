/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2025 Tilman Neumann - tilman.neumann@web.de
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
package de.tilman_neumann.jml.primes.probable;

import java.math.BigInteger;
import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

import static de.tilman_neumann.jml.base.BigIntConstants.*;
import static org.junit.Assert.assertEquals;

/**
 * Test of BPSW.nextProbablePrime().
 * 
 * @author Tilman Neumann
 */
public class NextProbablePrimeTest {
	private static final Logger LOG = LogManager.getLogger(NextProbablePrimeTest.class);
	private static final Random RNG = new Random();
	
	private static final int NCOUNT = 1000;
	private static final int MAX_BITS = 150;

	private static final BPSWTest bpsw = new BPSWTest();
	
	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}

	@Test
	public void testNextProbablePrime() {
		for (int nBits = 20; nBits<=MAX_BITS; nBits+=10) {
			LOG.info("Test correctness of " + NCOUNT + " N with " + nBits + " bits:");
			int i = 0;
			while (i < NCOUNT) {
				BigInteger n = new BigInteger(nBits, RNG);
				if (n.equals(I_0)) continue; // exclude 0 from test set
				
				BigInteger correctValue = n.nextProbablePrime();
				BigInteger bpswValue = bpsw.nextProbablePrime(n);
				assertEquals(correctValue, bpswValue);

				i++;
			}
			LOG.info("    Tested " + NCOUNT + " next probable primes...");
		}
	}
}
