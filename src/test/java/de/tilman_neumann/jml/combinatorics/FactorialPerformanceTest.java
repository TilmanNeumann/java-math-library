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
package de.tilman_neumann.jml.combinatorics;

import java.math.BigInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

public class FactorialPerformanceTest {

	private static final Logger LOG = LogManager.getLogger(FactorialPerformanceTest.class);

	/**
	 * Test.
	 * @param args Ignored.
	 */
	public static void main(String[] args) {
    	ConfigUtil.initProject();
    	
    	// compute 1000! ten thousand times
    	int n=1000;
    	int numberOfTests = 10000;
    	
    	long start = System.currentTimeMillis();
    	@SuppressWarnings("unused")
		BigInteger result = null;
    	for (int i=0; i<numberOfTests; i++) {
    		result = Factorial.simpleProduct(n);
    	}
    	long end = System.currentTimeMillis();
    	LOG.info("simpleProduct(" + n + ") took " + (end-start) + "ms");
    	
    	start = System.currentTimeMillis();
    	@SuppressWarnings("unused")
		BigInteger resultLuschny = null;
    	for (int i=0; i<numberOfTests; i++) {
    		resultLuschny = Factorial.factorial/*Luschny*/(n);
    	}
    	end = System.currentTimeMillis();
    	LOG.info("factorialLuschny(" + n + ") took " + (end-start) + "ms");
	}

}
