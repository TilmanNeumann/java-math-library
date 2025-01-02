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
package de.tilman_neumann.jml.transcendental;

import java.util.StringTokenizer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.precision.Scale;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.TimeUtil;

/**
 * Run user input tests for exp() implementations.
 * @author Tilman Neumann
 */
public class ExpRunner {
	private static final Logger LOG = LogManager.getLogger(ExpRunner.class);

	private static final boolean TEST_SLOW = true;
	
	private static void test(BigDecimal x, Scale maxOutScale) {
    	BigDecimal y = null; // the result
	    Scale outScale;
	    long t0, t1;
        
        // Performance measure 1: Series expansion:
	    if (TEST_SLOW) {
	        t0 = System.currentTimeMillis();
	        for (outScale=Scale.valueOf(2); outScale.compareTo(maxOutScale)<=0; outScale = outScale.add(1)) {
	            y = Exp.expSeriesExpansion(x, outScale);
	            LOG.debug("expSeriesExpansion(" + x + ", " + outScale + ") = " + y);
	        }
	        t1 = System.currentTimeMillis();
	        LOG.debug("Time of series expansion: " + TimeUtil.timeDiffStr(t0,t1));
	    }
	    
        // Performance measure 2: Simple reduction:
        t0 = System.currentTimeMillis();
        for (outScale=Scale.valueOf(2); outScale.compareTo(maxOutScale)<=0; outScale = outScale.add(1)) {
            y = Exp.expSimpleReduction(x, outScale);
            LOG.debug("expSimpleReduction(" + x + ", " + outScale + ") = " + y);
        }
        t1 = System.currentTimeMillis();
        LOG.debug("Time of simple argument reduction: " + TimeUtil.timeDiffStr(t0,t1));

        // Performance measure 3: Power reduction
        t0 = System.currentTimeMillis();
        for (outScale=Scale.valueOf(2); outScale.compareTo(maxOutScale)<=0; outScale = outScale.add(1)) {
        	y = Exp.exp/*PowerReduction*/(x, outScale);
            LOG.debug("expPowerReduction(" + x + ", " + outScale + ") = " + y);
        }
        t1 = System.currentTimeMillis();
        LOG.debug("Time of power reduction: " + TimeUtil.timeDiffStr(t0,t1));

        // Performance measure 4: Brents formula
        t0 = System.currentTimeMillis();
        for (outScale=Scale.valueOf(2); outScale.compareTo(maxOutScale)<=0; outScale = outScale.add(1)) {
            y = Exp.expBrent(x, outScale);
            LOG.debug("expBrent(" + x + ", " + outScale + ") = " + y);
        }
        t1 = System.currentTimeMillis();
        LOG.debug("Time of Brents formula: " + TimeUtil.timeDiffStr(t0,t1));
	}

	/**
	 * Test.
	 * 
	 * @param args ignored
	 */
	public static void main(String[] args) {
    	ConfigUtil.initProject();
	    
		while(true) {
			String input;
			BigDecimal x;
			Scale scale;
			try {
				LOG.info("Insert <x> <scale>:");
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				String line = in.readLine();
				input = line.trim();
				StringTokenizer tok = new StringTokenizer(input);
				x = new BigDecimal(tok.nextToken());
				scale = Scale.valueOf(Integer.parseInt(tok.nextToken()));
			} catch (Exception e) {
				LOG.error("Error occurring on input: " + e.getMessage());
				continue;
			}

			// test various exp implementations
			test(x, scale);
		}
	}
}
