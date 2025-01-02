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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.StringTokenizer;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.precision.Scale;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.TimeUtil;

/**
 * Implementation of the natural logarithm function for BigDecimals.
 * @author Tilman Neumann
 */
public class LnRunner {
	private static final Logger LOG = LogManager.getLogger(LnRunner.class);

	private static final boolean TEST_SLOW = false;

	private static void test(BigDecimal x, Scale maxScale) {
	    long t0, t1;
    	BigDecimal y=null; // result
	    
    	// compare ln2 implementations...
    	if (TEST_SLOW) {
	        t0 = System.currentTimeMillis();
	        for (Scale scale=Scale.valueOf(2); scale.compareTo(maxScale)<=0; scale = scale.add(1)) {
	            LOG.debug("ln2 series expansion(" + scale + ") = " + Ln.ln2SeriesExpansion(scale));
	        }
	        t1 = System.currentTimeMillis();
	        LOG.debug("Time of ln2 series expansion: " + TimeUtil.timeDiffStr(t0,t1));
		    
	        t0 = System.currentTimeMillis();
	        for (Scale scale=Scale.valueOf(2); scale.compareTo(maxScale)<=0; scale = scale.add(1)) {
	            LOG.debug("ln2 elementary series(" + scale + ") = " + Ln.ln2ElementarySeriesExpansion(scale));
	        }
	        t1 = System.currentTimeMillis();
	        LOG.debug("Time of ln2 elementary series: " + TimeUtil.timeDiffStr(t0,t1));
    	}
    	
        t0 = System.currentTimeMillis();
        for (Scale scale=Scale.valueOf(2); scale.compareTo(maxScale)<=0; scale = scale.add(1)) {
            LOG.debug("ln2 root reduction(" + scale + ") = " + Ln.ln2/*RootReduction*/(scale));
        }
        t1 = System.currentTimeMillis();
        LOG.debug("Time of ln2 root reduction: " + TimeUtil.timeDiffStr(t0,t1));
   
        Pi.pi(maxScale.multiply(2)); // do not count pi computation in performance test
        Ln.ln2(maxScale.multiply(2)); // do not count ln2 computation in performance test

        // compare ln(x) implementations...
    	if (TEST_SLOW) {
            // ln series expansion (very slow)
            t0 = System.currentTimeMillis();
            for (Scale scale=Scale.valueOf(2); scale.compareTo(maxScale)<=0; scale = scale.add(1)) {
                y = Ln.lnSeriesExpansion(x, scale);
                LOG.debug("lnSeriesExpansion(" + x + ", " + scale + ") = " + y);
            }
            t1 = System.currentTimeMillis();
            LOG.debug("Time of series expansion: " + TimeUtil.timeDiffStr(t0,t1));
    	}
    	
        // ln simple reduction:
        t0 = System.currentTimeMillis();
        for (Scale scale=Scale.valueOf(2); scale.compareTo(maxScale)<=0; scale = scale.add(1)) {
            y = Ln.lnSimpleReduction(x, scale);
            LOG.debug("lnSimpleReduction(" + x + ", " + scale + ") = " + y);
        }
        t1 = System.currentTimeMillis();
        LOG.debug("Time of simple reduction: " + TimeUtil.timeDiffStr(t0,t1));

        // ln reciprocal + simple reduction:
        t0 = System.currentTimeMillis();
        for (Scale scale=Scale.valueOf(2); scale.compareTo(maxScale)<=0; scale = scale.add(1)) {
            y = Ln.lnReciprocalSimpleReduction(x, scale);
            LOG.debug("lnReciprocalSimpleReduction(" + x + ", " + scale + ") = " + y);
        }
        t1 = System.currentTimeMillis();
        LOG.debug("Time of reciprocal simple reduction: " + TimeUtil.timeDiffStr(t0,t1));
  
        // ln AGM reduction
        t0 = System.currentTimeMillis();
        for (Scale scale=Scale.valueOf(2); scale.compareTo(maxScale)<=0; scale = scale.add(1)) {
            y = Ln.lnAgm(x, scale);
            LOG.debug("lnAgm(" + x + ", " + scale + ") = " + y);
        }
        t1 = System.currentTimeMillis();
        LOG.debug("Time of AGM reduction: " + TimeUtil.timeDiffStr(t0,t1));
        
        // ln simple + AGM reduction
        t0 = System.currentTimeMillis();
        for (Scale scale=Scale.valueOf(2); scale.compareTo(maxScale)<=0; scale = scale.add(1)) {
            y = Ln.lnSimplePlusAgmReduction(x, scale);
            LOG.debug("lnSimple+Agm(" + x + ", " + scale + ") = " + y);
        }
        t1 = System.currentTimeMillis();
        LOG.debug("Time of Simple+Agm reduction: " + TimeUtil.timeDiffStr(t0,t1));

        // ln reciprocal + simple + AGM reduction: The fastest algorithm!
        t0 = System.currentTimeMillis();
        for (Scale scale=Scale.valueOf(2); scale.compareTo(maxScale)<=0; scale = scale.add(1)) {
            y = Ln.ln/*ReciprocalSimplePlusAgmReduction*/(x, scale);
            LOG.debug("lnReciprocalSimplePlusAgmReduction(" + x + ", " + scale + ") = " + y);
        }
        t1 = System.currentTimeMillis();
        LOG.debug("Time of reciprocal simple + AGM reduction: " + TimeUtil.timeDiffStr(t0,t1));

    	if (TEST_SLOW) {
            // root reduction (slow)
            t0 = System.currentTimeMillis();
            for (Scale scale=Scale.valueOf(2); scale.compareTo(maxScale)<=0; scale = scale.add(1)) {
                y = Ln.lnRootReduction(x, scale);
                LOG.debug("lnRootReduction(" + x + ", " + scale + ") = " + y);
            }
            t1 = System.currentTimeMillis();
            LOG.debug("Time of root reduction: " + TimeUtil.timeDiffStr(t0,t1));
    	}
    	
        // ln simple + root reduction
        t0 = System.currentTimeMillis();
        for (Scale scale=Scale.valueOf(2); scale.compareTo(maxScale)<=0; scale = scale.add(1)) {
            y = Ln.lnSimplePlusRootReduction(x, scale);
            LOG.debug("lnSimple+root(" + x + ", " + scale + ") = " + y);
        }
        t1 = System.currentTimeMillis();
        LOG.debug("Time of Simple+root reduction: " + TimeUtil.timeDiffStr(t0,t1));

        // ln reciprocal + simple + root reduction:
        t0 = System.currentTimeMillis();
        for (Scale scale=Scale.valueOf(2); scale.compareTo(maxScale)<=0; scale = scale.add(1)) {
            y = Ln.lnReciprocalSimplePlusRootReduction(x, scale);
            LOG.debug("lnReciprocalSimplePlusRootReduction(" + x + ", " + scale + ") = " + y);
        }
        t1 = System.currentTimeMillis();
        LOG.debug("Time of reciprocal simple + root reduction: " + TimeUtil.timeDiffStr(t0,t1));
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
				//LOG.debug("input = >" + input + "<");
				StringTokenizer tok = new StringTokenizer(input);
				x = new BigDecimal(tok.nextToken());
				scale = Scale.valueOf(Integer.parseInt(tok.nextToken()));
			} catch (Exception e) {
				LOG.error("Error occurring on input: " + e.getMessage());
				continue;
			}

			// test various ln implementations
			test(x, scale);
		}
	}
}

