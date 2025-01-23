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
package de.tilman_neumann.jml.roots;

import java.math.BigInteger;
import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

import static org.junit.Assert.*;

/**
 * Test of SqrtExact.
 * 
 * @author Tilman Neumann
 */
public class SqrtExactTest {
	private static final Logger LOG = LogManager.getLogger(SqrtExactTest.class);
	private static final Random RNG = new Random();
	
	
	@Before
	public void setup() {
		ConfigUtil.initProject();
	}
	
	@Test
	public void testRandomSqrts() {
		int testCount=1000;
		for (int bits=10; bits<=200; bits++) {
			for (int i=0; i<testCount; i++) {
				BigInteger sqrt = new BigInteger(bits, RNG);
				BigInteger n = sqrt.multiply(sqrt);
	
				BigInteger result = SqrtExact.exactSqrt(n);
				assertNotNull(result);
				assertEquals(sqrt, result);
			}
			LOG.info("tested " + testCount + " squares with " + bits*2 + " bits");
		}
	}
}
