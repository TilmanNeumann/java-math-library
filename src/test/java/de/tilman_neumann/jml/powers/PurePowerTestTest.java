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
package de.tilman_neumann.jml.powers;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.powers.PurePowerTest.Result;
import de.tilman_neumann.util.ConfigUtil;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the pure power test.
 * 
 * @author Tilman Neumann
 */
// TODO: A p-adic implementation like in gmp would be much faster for large numbers (with thousands of digits)
public class PurePowerTestTest {
	private static final Logger LOG = LogManager.getLogger(PurePowerTestTest.class);

	private static final int NCOUNT = 100000;
	
	private static final PurePowerTest powTest = new PurePowerTest();
	private static final SecureRandom rng = new SecureRandom();

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}

	@Test
	public void testRandomNumbers() {
	   	for (int bits=10; bits<=50; bits+=5) {
		   	// create test set
	   		LOG.info("Test correctness with " + NCOUNT + " " + bits + "-bit numbers");
		   	ArrayList<BigInteger> testSet = new ArrayList<BigInteger>();
		   	for (int i=0; i<NCOUNT; i++) {
		   		testSet.add(new BigInteger(bits, rng));
		   	}
		   	
		   	// pure powers are not unique, e.g. 3^9 == 27^3, thus we can only check if the final result is correct
		   	for (BigInteger testNum : testSet) {
		   		Result r1 = powTest.test_v01(testNum);
	   			if (r1!=null) assertEquals(testNum, r1.base.pow(r1.exponent));
	   			
		   		Result r2 = powTest.test/*_v02*/(testNum);
		   		assertEquals(r1==null, r2==null);
	   			if (r2!=null) assertEquals(testNum, r2.base.pow(r2.exponent));
		   	}
	   	}
	}
}
