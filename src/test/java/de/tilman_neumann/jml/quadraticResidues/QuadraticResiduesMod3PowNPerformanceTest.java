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
 * Test performance of quadratic residue computations modulo 3^n.
 * 
 * @author Tilman Neumann
 */
public class QuadraticResiduesMod3PowNPerformanceTest {
	
	private static final Logger LOG = LogManager.getLogger(QuadraticResiduesMod3PowNPerformanceTest.class);

	/**
	 * Test.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		ConfigUtil.initProject();
		
		for (int n=0; n<17; n++) {
			long t0, t1;
			long m = (long) Math.pow(3, n);
			
			t0 = System.currentTimeMillis();
			TreeSet<Long> quadraticResidues_v1 = QuadraticResidues.getQuadraticResidues(m);
			t1 = System.currentTimeMillis();
			LOG.info("v1: n = " + n + ": Computed " + quadraticResidues_v1.size() + " quadratic residues mod 3^" + n + " in " + (t1-t0) + "ms");
			
			t0 = System.currentTimeMillis();
			List<Long> quadraticResidues_v2 = QuadraticResiduesMod3PowN.getQuadraticResiduesMod3PowN(n);
			t1 = System.currentTimeMillis();
			LOG.info("v2: n = " + n + ": Computed " + quadraticResidues_v2.size() + " quadratic residues mod 3^" + n + " in " + (t1-t0) + "ms");
			
			t0 = System.currentTimeMillis();
			List<Long> quadraticResidues_v3 = QuadraticResiduesMod3PowN.getQuadraticResiduesMod3PowN_testAll(n);
			t1 = System.currentTimeMillis();
			LOG.info("v3: n = " + n + ": Computed " + quadraticResidues_v3.size() + " quadratic residues mod 3^" + n + " in " + (t1-t0) + "ms");
		}
	}
}
