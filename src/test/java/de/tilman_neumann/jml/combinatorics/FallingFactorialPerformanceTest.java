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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

public class FallingFactorialPerformanceTest {

	private static final Logger LOG = LogManager.getLogger(FallingFactorialPerformanceTest.class);

	private static void testPerformance() {
    	int limit=1000;
    	
    	long t0, t1;
    	t0 = System.currentTimeMillis();
    	for (int n=0; n<limit; n++) {
    		for (int k=0; k<=n; k++) {
    			FallingFactorial.fallingFactorial(n, k);
    		}
    	}
    	t1 = System.currentTimeMillis();
    	LOG.info("fallingFactorial() took " + (t1-t0) + "ms");

    	t0 = System.currentTimeMillis();
    	for (int n=0; n<limit; n++) {
    		for (int k=0; k<=n; k++) {
    			FallingFactorial.byFactorials(n, k);
    		}
    	}
    	t1 = System.currentTimeMillis();
    	LOG.info("byFactorials() took " + (t1-t0) + "ms");

    	t0 = System.currentTimeMillis();
    	for (int n=0; n<limit; n++) {
    		for (int k=0; k<=n; k++) {
    			FallingFactorial.simpleProduct(n, k);
    		}
    	}
    	t1 = System.currentTimeMillis();
    	LOG.info("simpleProduct() took " + (t1-t0) + "ms");
    }

    /**
     * Test
     * @param args ignored
     */
    public static void main(String[] args) {
    	ConfigUtil.initProject();
    	testPerformance();
    }

}
