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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.precision.Scale;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.TimeUtil;

/**
 * Test performance of the computation of Euler's gamma constant.
 */
public class EulerConstantPerformanceTest {
	private static final Logger LOG = LogManager.getLogger(EulerConstantPerformanceTest.class);

	/** it feels as if the compiler optimizes the computation away if we do not log the result... */
	private static final boolean DEBUG = true;
	
	/**
	 * @param args ignored
	 */
	public static void main(String[] args) {
    	ConfigUtil.initProject();
    	long t0;
    	
    	Scale maxScale = Scale.valueOf(200);
    	
        t0 = System.currentTimeMillis();
        for (Scale scale=Scale.valueOf(2); scale.compareTo(maxScale)<0; scale = scale.add(1)) {
            if (DEBUG) LOG.debug("gamma_v1(" + scale + ") = " + EulerConstant.gamma_v1(scale));
        }
        LOG.info("gamma_v1(" + maxScale + ") = " + EulerConstant.gamma_v1(maxScale));
        String duration_v1 = TimeUtil.timeDiffStr(t0, System.currentTimeMillis());
    	
        t0 = System.currentTimeMillis();
        for (Scale scale=Scale.valueOf(2); scale.compareTo(maxScale)<0; scale = scale.add(1)) {
        	if (DEBUG) LOG.debug("gamma_v2(" + scale + ") = " + EulerConstant.gamma_v2(scale));
        }
        LOG.info("gamma_v2(" + maxScale + ") = " + EulerConstant.gamma_v2(maxScale));
        String duration_v2 = TimeUtil.timeDiffStr(t0, System.currentTimeMillis());
        
        LOG.info("Time for gamma_v1: " + duration_v1);
        LOG.info("Time for gamma_v2: " + duration_v2);
	}
}
