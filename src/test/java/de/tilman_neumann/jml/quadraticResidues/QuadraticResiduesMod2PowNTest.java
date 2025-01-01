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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * QA tests for quadratic residue computations modulo 2^n.
 * The counts give A023105(n) = 1, 2, 2, 3, 4, 7, 12, 23, 44, 87, 172, 343, 684, 1367, 2732, 5463, 10924, 21847, 43692, 87383, ...
 * 
 * @author Tilman Neumann
 */
public class QuadraticResiduesMod2PowNTest {
	
	private static final Logger LOG = LogManager.getLogger(QuadraticResiduesMod2PowNTest.class);

	private static final int NCOUNT = 20;
	
	private static final boolean DEBUG = false;
	private static final boolean SHOW_ELEMENTS = false;
	
	private static ArrayList<Integer> correctCounts;

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		
		// reference computation is brute force
		correctCounts = new ArrayList<Integer>();
		for (int n=0; n<NCOUNT; n++) {
			int m = 1<<n;
			TreeSet<Long> quadraticResidues = QuadraticResidues.getQuadraticResidues(m);
			LOG.info("n = " + n + " has " + quadraticResidues.size() + " quadratic residues modulo 2^" + n + (SHOW_ELEMENTS ? ": " + quadraticResidues : ""));
			correctCounts.add(quadraticResidues.size());
		}
		LOG.info("correctCounts = " + correctCounts);
	}

	@Test
	public void testV0() {
		ArrayList<Integer> counts = new ArrayList<Integer>();
		for (int n=0; n<NCOUNT; n++) {
			List<BigInteger> quadraticResidues = QuadraticResiduesMod2PowN.getQuadraticResiduesMod2PowN_testAll_big(n);
			if (DEBUG) LOG.debug("v0: n = " + n + " has " + quadraticResidues.size() + " quadratic residues modulo 2^" + n + (SHOW_ELEMENTS ? ": " + quadraticResidues : ""));
			counts.add(quadraticResidues.size());
		}
		LOG.info("v0 counts = " + counts);
		assertEquals(correctCounts, counts);
	}

	@Test
	public void testV1() {
		ArrayList<Integer> counts = new ArrayList<Integer>();
		for (int n=0; n<NCOUNT; n++) {
			List<Long> quadraticResidues = QuadraticResiduesMod2PowN.getQuadraticResiduesMod2PowN_testAll(n);
			if (DEBUG) LOG.debug("v1: n = " + n + " has " + quadraticResidues.size() + " quadratic residues modulo 2^" + n + (SHOW_ELEMENTS ? ": " + quadraticResidues : ""));
			counts.add(quadraticResidues.size());
		}
		LOG.info("v1 counts = " + counts);
		assertEquals(correctCounts, counts);
	}

	@Test
	public void testV2() {
		ArrayList<Integer> counts = new ArrayList<Integer>();
		for (int n=0; n<NCOUNT; n++) {
			List<Long> quadraticResidues = QuadraticResiduesMod2PowN.getQuadraticResiduesMod2PowN_testAll_v2(n);
			if (DEBUG) LOG.debug("v2: n = " + n + " has " + quadraticResidues.size() + " quadratic residues modulo 2^" + n + (SHOW_ELEMENTS ? ": " + quadraticResidues : ""));
			counts.add(quadraticResidues.size());
		}
		LOG.info("v2 counts = " + counts);
		assertEquals(correctCounts, counts);
	}

	@Test
	public void testV3() {
		ArrayList<Integer> counts = new ArrayList<Integer>();
		for (int n=0; n<NCOUNT; n++) {
			List<Long> quadraticResidues = QuadraticResiduesMod2PowN.getQuadraticResiduesMod2PowN(n);
			if (DEBUG) LOG.debug("v3: n = " + n + " has " + quadraticResidues.size() + " quadratic residues modulo 2^" + n + (SHOW_ELEMENTS ? ": " + quadraticResidues : ""));
			counts.add(quadraticResidues.size());
		}
		LOG.info("v3 counts = " + counts);
		assertEquals(correctCounts, counts);
	}

	@Test
	public void testV4() {
		ArrayList<Integer> counts = new ArrayList<Integer>();
		for (int n=0; n<NCOUNT; n++) {
			long[] array = new long[((1<<n) / 6) + 6];
			int count = QuadraticResiduesMod2PowN.getQuadraticResiduesMod2PowN(n, array);
			if (DEBUG) LOG.debug("v4: n = " + n + " has " + count + " quadratic residues modulo 2^" + n + (SHOW_ELEMENTS ? ": " + Arrays.toString(array) : ""));
			counts.add(count);
		}
		LOG.info("v4 counts = " + counts);
		assertEquals(correctCounts, counts);
	}
}
