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

import java.math.BigDecimal;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.precision.Scale;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.TimeUtil;

/**
 * Computations of Pi = 3.1415... to arbitrary precision.
 * @author Tilman Neumann
 */
public class PiPerformanceTest {
	private static final Logger LOG = LogManager.getLogger(PiPerformanceTest.class);

	private static final boolean ALL_SCALES = false;

	private static void testPi(Scale maxScale) {
        long t0 = System.currentTimeMillis();
        if (ALL_SCALES) {
	        for (Scale scale=Scale.valueOf(2); scale.compareTo(maxScale)<0; scale = scale.add(1)) {
	        	BigDecimal pi = Pi.pi(scale);
	            LOG.debug("pi(" + scale + ") = " + pi);
	        }
        }
        LOG.info("pi(" + maxScale + ") = " + Pi.pi(maxScale));
        long t1 = System.currentTimeMillis();
        LOG.info("Time of pi computation: " + TimeUtil.timeDiffStr(t0,t1));
	}

	/**
	 * Test.
	 * 
	 * @param argv command line arguments, ignored
	 */
	public static void main(String[] argv) {
    	ConfigUtil.initProject();
        testPi(Scale.valueOf(1000));
        testPi(Scale.valueOf(10000));
	}
}
