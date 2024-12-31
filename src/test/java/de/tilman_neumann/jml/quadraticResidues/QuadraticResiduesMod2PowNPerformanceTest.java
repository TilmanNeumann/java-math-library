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
package de.tilman_neumann.jml.quadraticResidues;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Performance tests of quadratic residue computations modulo 2^n.
 * 
 * @author Tilman Neumann
 */
public class QuadraticResiduesMod2PowNPerformanceTest {
	
	private static final Logger LOG = LogManager.getLogger(QuadraticResiduesMod2PowNPerformanceTest.class);
	
	private static final boolean SHOW_ELEMENTS = false;

	/**
	 * Test.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		ConfigUtil.initProject();

		for (int n=0; ; n++) {
			long t0, t1;

			t0 = System.currentTimeMillis();
			List<BigInteger> quadraticResiduesMod2PowN_v0 = QuadraticResiduesMod2PowN.getQuadraticResiduesMod2PowN_testAll_big(n);
			t1 = System.currentTimeMillis();
			LOG.info("v0: n = " + n + " has " + quadraticResiduesMod2PowN_v0.size() + " quadratic residues" + (SHOW_ELEMENTS ? ": " + quadraticResiduesMod2PowN_v0 : "") + " -- duration = " + (t1-t0) + "ms");

			t0 = System.currentTimeMillis();
			List<Long> quadraticResiduesMod2PowN_v1 = QuadraticResiduesMod2PowN.getQuadraticResiduesMod2PowN_testAll(n);
			t1 = System.currentTimeMillis();
			LOG.info("v1: n = " + n + " has " + quadraticResiduesMod2PowN_v1.size() + " quadratic residues" + (SHOW_ELEMENTS ? ": " + quadraticResiduesMod2PowN_v1 : "") + " -- duration = " + (t1-t0) + "ms");

			t0 = System.currentTimeMillis();
			List<Long> quadraticResiduesMod2PowN_v2 = QuadraticResiduesMod2PowN.getQuadraticResiduesMod2PowN_testAll_v2(n);
			t1 = System.currentTimeMillis();
			LOG.info("v2: n = " + n + " has " + quadraticResiduesMod2PowN_v2.size() + " quadratic residues" + (SHOW_ELEMENTS ? ": " + quadraticResiduesMod2PowN_v2 : "") + " -- duration = " + (t1-t0) + "ms");

			t0 = System.currentTimeMillis();
			List<Long> quadraticResiduesMod2PowN_v3 = QuadraticResiduesMod2PowN.getQuadraticResiduesMod2PowN(n);
			t1 = System.currentTimeMillis();
			LOG.info("v3: n = " + n + " has " + quadraticResiduesMod2PowN_v3.size() + " quadratic residues" + (SHOW_ELEMENTS ? ": " + quadraticResiduesMod2PowN_v3 : "") + " -- duration = " + (t1-t0) + "ms");

			t0 = System.currentTimeMillis();
			long[] array = new long[((1<<n) / 6) + 6];
			int count = QuadraticResiduesMod2PowN.getQuadraticResiduesMod2PowN(n, array);
			t1 = System.currentTimeMillis();
			LOG.info("v4: n = " + n + " has " + count + " quadratic residues" + (SHOW_ELEMENTS ? ": " + Arrays.toString(array) : "") + " -- duration = " + (t1-t0) + "ms");
		}
	}
}
