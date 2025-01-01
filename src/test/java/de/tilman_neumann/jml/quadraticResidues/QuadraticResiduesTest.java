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
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Tests for class QuadraticResidues.
 * 
 * @author Tilman Neumann
 */
public class QuadraticResiduesTest {
	
	private static final Logger LOG = LogManager.getLogger(QuadraticResiduesTest.class);
	
	private static final boolean DEBUG = false;
	
	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}

	@Test
	public void testSetOfQuadraticResidues() {
		TreeSet<Long> quadraticResiduesMod100 = QuadraticResidues.getQuadraticResidues(100);
		LOG.info("m = 100 has " + quadraticResiduesMod100.size() + " quadratic residues: " + quadraticResiduesMod100);
		assertEquals("[0, 1, 4, 9, 16, 21, 24, 25, 29, 36, 41, 44, 49, 56, 61, 64, 69, 76, 81, 84, 89, 96]", quadraticResiduesMod100.toString());
	}

	/**
	 * Test the number of quadratic residues modulo 2^n for n = 0, 1, 2, ...
	 * This gives number sequence A023105(n) = 1, 2, 2, 3, 4, 7, 12, 23, 44, 87, 172, 343, ...	 */
	@Test
	public void testQuadraticResiduesMod2PowNCounts() {
		ArrayList<Integer> counts = new ArrayList<Integer>();
		for (int n=0; n<20; n++) {
			int m = 1<<n;
			TreeSet<Long> quadraticResiduesMod2PowN = QuadraticResidues.getQuadraticResidues(m);
			if (DEBUG) LOG.debug("m = " + m + " has " + quadraticResiduesMod2PowN.size() + " quadratic residues: " + quadraticResiduesMod2PowN);
			counts.add(quadraticResiduesMod2PowN.size());
		}
		LOG.info("counts = " + counts);
		assertEquals("[1, 2, 2, 3, 4, 7, 12, 23, 44, 87, 172, 343, 684, 1367, 2732, 5463, 10924, 21847, 43692, 87383]", counts.toString());
	}

	/**
	 * Test the number of even quadratic residues modulo 2^n for n = 0, 1, 2, ...
	 * This gives number sequence a(n) = {1, 1} + A023105(n-2) = 1, 1, 1, 2, 2, 3, 4, 7, 12, 23, 44, 87, 172, 343, 684, 1367, 2732, 5463, 10924, 21847, ...
	 */
	@Test
	public void testEvenQuadraticResiduesMod2PowNCounts() {
		ArrayList<Integer> evenCounts = new ArrayList<Integer>();
		for (int n=0; n<20; n++) {
			int m = 1<<n;
			TreeSet<Long> evenQuadraticResiduesMod2PowN = QuadraticResidues.getEvenQuadraticResidues(m);
			if (DEBUG) LOG.debug("m = " + m + " has " + evenQuadraticResiduesMod2PowN.size() + " 'even' quadratic residues: " + evenQuadraticResiduesMod2PowN);
			evenCounts.add(evenQuadraticResiduesMod2PowN.size());
		}
		LOG.info("evenCounts = " + evenCounts);
		assertEquals("[1, 1, 1, 2, 2, 3, 4, 7, 12, 23, 44, 87, 172, 343, 684, 1367, 2732, 5463, 10924, 21847]", evenCounts.toString());
	}
}
