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
 * QA tests for quadratic residue computations modulo p^n for odd primes p.
 * 
 * Resulting sequences:
 * n= 3: 1, 2, 4, 11, 31, 92, 274, 821, 2461, 7382, 22144, 66431, 199291, 597872, 1793614, ...
 *       = A039300(n), Number of distinct quadratic residues mod 3^n.
 * n= 5: 1, 3, 11, 53, 261, 1303, 6511, 32553, 162761, 813803, 4069011, ...
 *       = A039302(n), Number of distinct quadratic residues mod 5^n.
 * n= 7: 1, 4, 22, 151, 1051, 7354, 51472, 360301, ...
 *       = A039304(n), Number of distinct quadratic residues mod 7^n.
 * n=11: 1, 6, 56, 611, 6711, 73816, 811966, ... (not in OEIS)
 * 
 * @author Tilman Neumann
 */
public class QuadraticResiduesModPPowNTest {
	
	private static final Logger LOG = LogManager.getLogger(QuadraticResiduesModPPowNTest.class);
	
	private static final boolean DEBUG = false;
	private static final boolean SHOW_ELEMENTS = false;
	
	// the prime bases to test
	private int[] pArray = new int[] {3, 5, 7, 11};
	
	private static final int[] nMaxForP = new int[20];
	
	@SuppressWarnings("unchecked")
	private static ArrayList<Integer>[] correctCountsForP = new ArrayList[20];

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		
		computeCorrectCounts(3, 13);
		computeCorrectCounts(5, 9);
		computeCorrectCounts(7, 7);
		computeCorrectCounts(11, 6);
	}

	private static void computeCorrectCounts(int p, int nMax) {
		nMaxForP[p] = nMax;
		// reference computation is brute force
		ArrayList<Integer> correctCounts = new ArrayList<Integer>();
		for (int n=0; n<=nMax; n++) {
			long m = (long) Math.pow(p, n);
			TreeSet<Long> quadraticResidue = QuadraticResidues.getQuadraticResidues(m);
			if (DEBUG) LOG.info("n = " + n + " has " + quadraticResidue.size() + " quadratic residues modulo " + p + "^" + n + (SHOW_ELEMENTS ? ": " + quadraticResidue : ""));
			correctCounts.add(quadraticResidue.size());
		}
		LOG.info("correctCounts modulo " + p + "^n = " + correctCounts);
		correctCountsForP[p] = correctCounts;
	}
	
	@Test
	public void testV2() {
		for (int p : pArray) {
			ArrayList<Integer> counts = new ArrayList<Integer>();
			for (int n=0; n<=nMaxForP[p]; n++) {
				List<Long> quadraticResidue = QuadraticResiduesModPPowN.getQuadraticResiduesModPPowN(p, n);
				if (DEBUG) LOG.debug("v2: n = " + n + " has " + quadraticResidue.size() + " quadratic residues modulo " + p + "^" + n + (SHOW_ELEMENTS ? ": " + quadraticResidue : ""));
				counts.add(quadraticResidue.size());
			}
			LOG.info("v2 counts modulo " + p + "^n =  " + counts);
			assertEquals(correctCountsForP[p], counts);
		}
	}

	@Test
	public void testV3() {
		for (int p : pArray) {
			ArrayList<Integer> counts = new ArrayList<Integer>();
			for (int n=0; n<=nMaxForP[p]; n++) {
				List<Long> quadraticResidue = QuadraticResiduesModPPowN.getQuadraticResiduesModPPowN_testAll(p, n);
				if (DEBUG) LOG.debug("v3: n = " + n + " has " + quadraticResidue.size() + " quadratic residues modulo " + p + "^" + n + (SHOW_ELEMENTS ? ": " + quadraticResidue : ""));
				counts.add(quadraticResidue.size());
			}
			LOG.info("v3 counts modulo " + p + "^n =  " + counts);
			assertEquals(correctCountsForP[p], counts);
		}
	}

	@Test
	public void testV4() {
		for (int p : pArray) {
			ArrayList<Integer> counts = new ArrayList<Integer>();
			for (int n=0; n<=nMaxForP[p]; n++) {
				List<Long> quadraticResidue = QuadraticResiduesModPPowN.getQuadraticResiduesModPPowN_testAll_v2(p, n);
				if (DEBUG) LOG.debug("v3: n = " + n + " has " + quadraticResidue.size() + " quadratic residues modulo 3^" + n+ (SHOW_ELEMENTS ? ": " + quadraticResidue : ""));
				counts.add(quadraticResidue.size());
			}
			LOG.info("v4 counts modulo " + p + "^n =  " + counts);
			assertEquals(correctCountsForP[p], counts);
		}
	}
}
