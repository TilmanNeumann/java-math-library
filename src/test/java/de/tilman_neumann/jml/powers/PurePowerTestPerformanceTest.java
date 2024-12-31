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
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Test performance of pure power tests.
 * 
 * @author Tilman Neumann
 */
public class PurePowerTestPerformanceTest {
	private static final Logger LOG = LogManager.getLogger(PurePowerTestPerformanceTest.class);

	private static void testPerformance(int nCount) {
	   	PurePowerTest powTest = new PurePowerTest();

	   	// create test set for performance test
	   	SecureRandom rng = new SecureRandom();
	   	for (int bits=50; ; bits+=50) {
		   	ArrayList<BigInteger> testSet = new ArrayList<BigInteger>();
		   	for (int i=0; i<nCount; i++) {
		   		testSet.add(new BigInteger(bits, rng));
		   	}
	
		   	// test performance
		   	long t0, t1;
		   	t0 = System.currentTimeMillis();
		   	for (BigInteger testNum : testSet) {
		   		powTest.test_v01(testNum);
		   	}
		   	t1 = System.currentTimeMillis();
			LOG.info("v01: Testing " + nCount + " " + bits + "-bit numbers took " + (t1-t0) + " ms");

		   	t0 = System.currentTimeMillis();
		   	for (BigInteger testNum : testSet) {
		   		powTest.test/*_v02*/(testNum);
		   	}
		   	t1 = System.currentTimeMillis();
			LOG.info("v02: Testing " + nCount + " " + bits + "-bit numbers took " + (t1-t0) + " ms");
			LOG.info("");
	   	}
	}

	/**
	 * Test.
	 * @param args ignored
	 */
	public static void main(String[] args) {
	   	ConfigUtil.initProject();
	   	testPerformance(1000000);
	}
}
