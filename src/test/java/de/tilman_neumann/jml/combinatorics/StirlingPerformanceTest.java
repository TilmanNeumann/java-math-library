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

public class StirlingPerformanceTest {
	private static final Logger LOG = LogManager.getLogger(StirlingPerformanceTest.class);

    private static void testStirling1Performance() {
    	int n=30;
    	long start;
    	// this one is just too slow, ~30 seconds for n=30 -> i=0..29
    	start = System.currentTimeMillis();
    	for (int i=0; i<n; i++) {
    		for (int k=0; k<=i; k++) {
    			Stirling.stirling1Recurrent(i,k);
    		}
    	}
    	LOG.info("stirling1Recurrent took " + (System.currentTimeMillis() - start) + " ms");
       	
    	// better but not too good for n>40
    	start = System.currentTimeMillis();
    	for (int i=0; i<n; i++) {
    		for (int k=0; k<=i; k++) {
    			Stirling.stirling1ByStirling2(i,k);
    		}
    	}
    	LOG.info("stirling1ByStirling2 took " + (System.currentTimeMillis() - start) + " ms");

    	// best without caching !
    	start = System.currentTimeMillis();
    	for (int i=0; i<n; i++) {
    		for (int k=0; k<=i; k++) {
    			Stirling.stirling1ByGF(i,k);
    		}
    	}
    	LOG.info("stirling1byGF took " + (System.currentTimeMillis() - start) + " ms");

    	// this is the fastest solution, but runs out of memory at 1000<n<2000 in a VM with 512MB heap space...
    	start = System.currentTimeMillis();
    	for (int i=0; i<n; i++) {
    		for (int k=0; k<=i; k++) {
    			Stirling.stirling1WithMemory(i,k);
    		}
    	}
    	LOG.info("stirling1WithMemory took " + (System.currentTimeMillis() - start) + " ms");

    	// also quite fast but with memory problems. In some test years ago I got an OutOfMemoryError at n=849.
    	start = System.currentTimeMillis();
    	for (int i=0; i<n; i++) {
    		for (int k=0; k<=i; k++) {
    			Stirling.stirling1WithHashedMemory(i,k);
    		}
    	}
    	LOG.info("stirling1WithHashedMemory took " + (System.currentTimeMillis() - start) + " ms");
    }
    
    /**
     * Tests.
     * @param args ignored
     */
    public static void main(String[] args) {
    	ConfigUtil.initProject();
    	testStirling1Performance();
    }
}
