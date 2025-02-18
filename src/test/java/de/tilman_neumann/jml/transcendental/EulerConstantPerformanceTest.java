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

import java.math.BigDecimal;

import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.precision.Scale;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.TimeUtil;

/**
 * Test performance of the computation of Euler's gamma constant.
 */
public class EulerConstantPerformanceTest {
	private static final Logger LOG = LogManager.getLogger(EulerConstantPerformanceTest.class);

	private static final boolean ALL_SCALES = false;
	
	private static void testGamma(Scale maxScale) {
    	long t0;
    	
        t0 = System.currentTimeMillis();
        if (ALL_SCALES) {
	        for (Scale scale=Scale.valueOf(2); scale.compareTo(maxScale)<0; scale = scale.add(1)) {
	        	BigDecimal gamma = EulerConstant.gamma_v1(scale);
	            LOG.debug("gamma_v1(" + scale + ") = " + gamma);
	        }
        }
        LOG.info("gamma_v1(" + maxScale + ") = " + EulerConstant.gamma_v1(maxScale));
        String duration_v1 = TimeUtil.timeDiffStr(t0, System.currentTimeMillis());
    	
        t0 = System.currentTimeMillis();
        if (ALL_SCALES) {
        	for (Scale scale=Scale.valueOf(2); scale.compareTo(maxScale)<0; scale = scale.add(1)) {
	        	BigDecimal gamma = EulerConstant.gamma_v2(scale);
	        	LOG.debug("gamma_v2(" + scale + ") = " + gamma);
        	}
        }
        LOG.info("gamma_v2(" + maxScale + ") = " + EulerConstant.gamma_v2(maxScale));
        String duration_v2 = TimeUtil.timeDiffStr(t0, System.currentTimeMillis());
        
        LOG.info("Time for gamma_v1: " + duration_v1);
        LOG.info("Time for gamma_v2: " + duration_v2);
	}
	
	/**
	 * @param args ignored
	 */
	public static void main(String[] args) {
    	ConfigUtil.initProject();
    	testGamma(Scale.valueOf(200));
    	testGamma(Scale.valueOf(400));
    	testGamma(Scale.valueOf(600));
 	}
}
