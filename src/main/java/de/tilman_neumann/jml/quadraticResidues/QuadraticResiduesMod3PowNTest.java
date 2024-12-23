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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Tests of quadratic residue computations modulo 3^n.
 * 
 * @author Tilman Neumann
 */
public class QuadraticResiduesMod3PowNTest {
	
	private static final Logger LOG = LogManager.getLogger(QuadraticResiduesMod3PowNTest.class);

	private static final boolean SHOW_ELEMENTS = false;

	/**
	 * Test.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		ConfigUtil.initProject();
		
		LOG.debug("residues % 3 = {0, 1}");
		
		ArrayList<Integer> counts = new ArrayList<Integer>();
		ArrayList<Integer> counts_v2 = new ArrayList<Integer>();
		ArrayList<Integer> counts_v3 = new ArrayList<Integer>();
		
		for (int n=0; n<17; n++) {
			long m = (long) Math.pow(3, n);
			
			TreeSet<Long> quadraticResiduesModPow = QuadraticResidues.getQuadraticResidues(m);
			LOG.info("v1: n = " + n + ": There are " + quadraticResiduesModPow.size() + " quadratic residues mod 3^" + n + (SHOW_ELEMENTS ? ": " + quadraticResiduesModPow : ""));
			counts.add(quadraticResiduesModPow.size());
			
			List<Long> quadraticResiduesModPow_v2 = QuadraticResiduesMod3PowN.getQuadraticResiduesMod3PowN(n);
			LOG.info("v2: n = " + n + ": There are " + quadraticResiduesModPow_v2.size() + " quadratic residues mod 3^" + n + (SHOW_ELEMENTS ? ": " + quadraticResiduesModPow_v2 : ""));
			counts_v2.add(quadraticResiduesModPow_v2.size());
			
			List<Long> quadraticResiduesModPow_v3 = QuadraticResiduesMod3PowN.getQuadraticResiduesMod3PowN_testAll(n);
			LOG.info("v3: n = " + n + ": There are " + quadraticResiduesModPow_v3.size() + " quadratic residues mod 3^" + n + (SHOW_ELEMENTS ? ": " + quadraticResiduesModPow_v3 : ""));
			counts_v3.add(quadraticResiduesModPow_v3.size());
		}
		
		LOG.info("");
		LOG.info("v1: counts = " + counts);
		LOG.info("v2: counts = " + counts_v2);
		LOG.info("v3: counts = " + counts_v3);
		// A039300(n) = 1, 2, 4, 11, 31, 92, 274, 821, 2461, 7382, 22144, 66431, 199291, 597872, 1793614, 5380841, 16142521...
	}
}
