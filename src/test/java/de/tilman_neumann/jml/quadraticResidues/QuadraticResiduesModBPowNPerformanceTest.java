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

import java.util.List;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Test performance of quadratic residue computations modulo p^n.
 * 
 * @author Tilman Neumann
 */
public class QuadraticResiduesModBPowNPerformanceTest {
	
	private static final Logger LOG = LogManager.getLogger(QuadraticResiduesModBPowNPerformanceTest.class);
	
	/**
	 * Test.
	 * @param args ignored
	 */
	private static void testPerformance(int p, int nMax) {
		ConfigUtil.initProject();

		for (int n=0; n<=nMax; n++) {
			long m = (long) Math.pow(p, n);
			
			long t0, t1;
			t0 = System.currentTimeMillis();
			TreeSet<Long> quadraticResidues_v1 = QuadraticResidues.getQuadraticResidues(m);
			t1 = System.currentTimeMillis();
			LOG.info("v1: n = " + n + ": Computed " + quadraticResidues_v1.size() + " quadratic residues mod " + p + "^" + n + " in " + (t1-t0) + "ms");
			
			t0 = System.currentTimeMillis();
			List<Long> quadraticResidues_v2 = QuadraticResiduesModBPowN.getQuadraticResiduesModBPowN(p, n);
			t1 = System.currentTimeMillis();
			LOG.info("v2: n = " + n + ": Computed " + quadraticResidues_v2.size() + " quadratic residues mod " + p + "^" + n + " in " + (t1-t0) + "ms");
			
			t0 = System.currentTimeMillis();
			List<Long> quadraticResidues_v3 = QuadraticResiduesModBPowN.getQuadraticResiduesModBPowN_testAll(p, n);
			t1 = System.currentTimeMillis();
			LOG.info("v3: n = " + n + ": Computed " + quadraticResidues_v3.size() + " quadratic residues mod " + p + "^" + n + " in " + (t1-t0) + "ms");
			
			t0 = System.currentTimeMillis();
			List<Long> quadraticResidues_v4 = QuadraticResiduesModBPowN.getQuadraticResiduesModBPowN_testAll_v2(p, n);
			t1 = System.currentTimeMillis();
			LOG.info("v4: n = " + n + ": Computed " + quadraticResidues_v4.size() + " quadratic residues mod " + p + "^" + n + " in " + (t1-t0) + "ms");
		}
	}
	
	/**
	 * Test.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		ConfigUtil.initProject();
		testPerformance(3, 16);
		testPerformance(5, 11);
		testPerformance(7, 9);
	}
}
