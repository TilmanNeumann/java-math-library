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

public class AgmRunner {
	private static final Logger LOG = LogManager.getLogger(AgmRunner.class);

	// Test inputs:
	// 1 2 100
	// 1 0.00000000000000000001 100
	// 10 20 100
	// 
	// Negative arguments are not allowed; the result will be 0 if one of the arguments is 0.
	//
	public static void main(String[] args) {
    	ConfigUtil.initProject();
		while(true) {
			String input;
			BigDecimal a, b;
			Scale maxScale;
			try {
				LOG.info("Insert <a> <b> <scale>:");
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				String line = in.readLine();
				input = line.trim();
				//LOG.debug("input = >" + input + "<");
				StringTokenizer tok = new StringTokenizer(input);
				a = new BigDecimal(tok.nextToken());
				b = new BigDecimal(tok.nextToken());
				maxScale = Scale.valueOf(Integer.parseInt(tok.nextToken()));
			} catch (Exception e) {
				LOG.error("Error occurring on input: " + e.getMessage());
				continue;
			}
			
			long t0, t1;
			BigDecimal agm;

	        t0 = System.currentTimeMillis();
	        for (Scale scale=Scale.valueOf(2); scale.compareTo(maxScale)<=0; scale = scale.add(1)) {
	        	agm = Agm.agm/*_intCore*/(a, b, scale);
	            LOG.info("agm(" + a + ", " + b + ", " + scale + ") = " + agm);
	        }
	        t1 = System.currentTimeMillis();
	        LOG.debug("Computation took " + TimeUtil.timeDiffStr(t0,t1));
		}
	}
}
