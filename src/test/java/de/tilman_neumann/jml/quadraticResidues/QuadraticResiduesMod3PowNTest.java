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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * QA tests for quadratic residue computations modulo 3^n.
 * The counts give A039300(n) = 1, 2, 4, 11, 31, 92, 274, 821, 2461, 7382, 22144, 66431, 199291, 597872, 1793614, 5380841, 16142521...
 * 
 * @author Tilman Neumann
 */
public class QuadraticResiduesMod3PowNTest {
	
	private static final Logger LOG = LogManager.getLogger(QuadraticResiduesMod3PowNTest.class);

	private static final int NCOUNT = 15;
	
	private static final boolean DEBUG = false;
	private static final boolean SHOW_ELEMENTS = false;
	
	private static ArrayList<Integer> correctCounts;

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		
		// reference computation is brute force
		correctCounts = new ArrayList<Integer>();
		for (int n=0; n<NCOUNT; n++) {
			long m = (long) Math.pow(3, n);
			TreeSet<Long> quadraticResidue = QuadraticResidues.getQuadraticResidues(m);
			LOG.info("n = " + n + " has " + quadraticResidue.size() + " quadratic residues modulo 3^" + n + (SHOW_ELEMENTS ? ": " + quadraticResidue : ""));
			correctCounts.add(quadraticResidue.size());
		}
		LOG.info("correctCounts = " + correctCounts);
	}

	@Test
	public void testV2() {
		ArrayList<Integer> counts = new ArrayList<Integer>();
		for (int n=0; n<NCOUNT; n++) {
			List<Long> quadraticResidue = QuadraticResiduesMod3PowN.getQuadraticResiduesMod3PowN(n);
			if (DEBUG) LOG.debug("v2: n = " + n + " has " + quadraticResidue.size() + " quadratic residues modulo 3^" + n + (SHOW_ELEMENTS ? ": " + quadraticResidue : ""));
			counts.add(quadraticResidue.size());
		}
		LOG.info("v2 counts = " + counts);
		assertEquals(correctCounts, counts);
	}

	@Test
	public void testV3() {
		ArrayList<Integer> counts = new ArrayList<Integer>();
		for (int n=0; n<NCOUNT; n++) {
			List<Long> quadraticResidue = QuadraticResiduesMod3PowN.getQuadraticResiduesMod3PowN_testAll(n);
			if (DEBUG) LOG.debug("v3: n = " + n + " has " + quadraticResidue.size() + " quadratic residues modulo 3^" + n+ (SHOW_ELEMENTS ? ": " + quadraticResidue : ""));
			counts.add(quadraticResidue.size());
		}
		LOG.info("v3 counts = " + counts);
		assertEquals(correctCounts, counts);
	}
}
