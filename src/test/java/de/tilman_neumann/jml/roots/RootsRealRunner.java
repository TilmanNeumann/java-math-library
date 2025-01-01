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
package de.tilman_neumann.jml.roots;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.precision.Scale;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.TimeUtil;

/**
 * Test i.th root of floating point numbers by user input.
 *  
 * @author Tilman Neumann
 */
public class RootsRealRunner {
	private static final Logger LOG = LogManager.getLogger(RootsRealRunner.class);
  
	/**
	 * Test.
	 * @param argv command line arguments
	 */
	public static void main(String[] argv) {
    	ConfigUtil.initProject();
    	
	   	while (true) {
			try {
				LOG.info("\nPlease insert <x> <maximal scale>:");
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				String line = in.readLine();
				String[] splitted = line.split("\\s+");
				BigDecimal x = new BigDecimal(splitted[0]);
				Integer maxScaleInput = Integer.parseInt(splitted[1]);
			    Scale maxScale = Scale.valueOf(maxScaleInput);
		        long t0, t1;
		        
		        t0 = System.currentTimeMillis();
		    	for (int i=2; i<10; i++) {
		    		for (Scale scale = Scale.valueOf(2); scale.compareTo(maxScale)<=0; scale = scale.add(1)) {
		        		LOG.debug(i + ".th root(" + x  + ", " + scale + ")=" + RootsReal.ithRoot(x, i, scale));
		        	}
		        }
		        t1 = System.currentTimeMillis();
		        LOG.debug("Time of root computations: " + TimeUtil.timeDiffStr(t0,t1));
			} catch (Exception ex) {
				LOG.error("Error " + ex, ex);
			}
		}
	}
}
