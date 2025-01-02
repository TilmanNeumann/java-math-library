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
package de.tilman_neumann.jml.squareSums;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.quadraticResidues.QuadraticResiduesMod2PowN;
import de.tilman_neumann.util.ConfigUtil;

/**
 * Test.
 * 
 * @author Tilman Neumann
 */
public class SumOf4SquaresTest {

	private static final Logger LOG = LogManager.getLogger(SumOf4SquaresTest.class);

	private static final boolean SHOW_ELEMENTS = false;

	@Before
	public void setup() {
		ConfigUtil.initProject();
	}

	@Test
	public void testComputeNumbersThatAreNoLessThan4NonzeroSquares() {
		ArrayList<Integer> quadraticResidueCounts = new ArrayList<Integer>();
		ArrayList<Integer> a004215EntryCounts = new ArrayList<Integer>();
		ArrayList<Integer> a004215EntryCounts_v2 = new ArrayList<Integer>();
		ArrayList<Integer> a004215EntryCounts_v3 = new ArrayList<Integer>();
		
		for (int n=0; n<23; n++) {
			LOG.info("n = " + n + ":");
			long m = 1L<<n;

			List<Long> quadraticResiduesMod2PowN = QuadraticResiduesMod2PowN.getQuadraticResiduesMod2PowN(n);
			LOG.info("There are " + quadraticResiduesMod2PowN.size() + " quadratic residues % " + m + (SHOW_ELEMENTS ? ": " + quadraticResiduesMod2PowN : ""));
			quadraticResidueCounts.add(quadraticResiduesMod2PowN.size());

			TreeSet<Long> a004215Entries = SumOf4Squares.computeNumbersThatAreNoLessThan4NonzeroSquares_v1(m);
			LOG.info("v1: There are " + a004215Entries.size() + " A004215 entries < " + m + (SHOW_ELEMENTS ? ": " + a004215Entries : ""));
			a004215EntryCounts.add(a004215Entries.size());
			
			TreeSet<Long> a004215Entries_v2 = SumOf4Squares.computeNumbersThatAreNoLessThan4NonzeroSquares_v2(n);
			LOG.info("v2: There are " + a004215Entries_v2.size() + " A004215 entries < " + m + (SHOW_ELEMENTS ? ": " + a004215Entries_v2 : ""));
			a004215EntryCounts_v2.add(a004215Entries_v2.size());
			
			long[] a004215Array_v3 = new long[((1<<n) / 6) + 4]; // #{A004215(k) | k<m} is always near to m/6
			int count = SumOf4Squares.computeNumbersThatAreNoLessThan4NonzeroSquares/*_v3*/(n, a004215Array_v3);
			LOG.info("v3: There are " + count + " A004215 entries < " + m + (SHOW_ELEMENTS ? ": " + Arrays.toString(a004215Array_v3) : ""));
			a004215EntryCounts_v3.add(count);
			
			assertEquals(a004215Entries, a004215Entries_v2);
			
			// to compare v3 we need to convert the long array into a TreeSet
			TreeSet<Long> a004215Entries_v3 = new TreeSet<>();
			for (int j=0; j<count; j++) {
				a004215Entries_v3.add(a004215Array_v3[j]);
			}
			assertEquals(a004215Entries, a004215Entries_v3);
			
			if (n>0) {
				// there is a close relationship with quadratic residues modulo 2^n
				assertEquals(quadraticResiduesMod2PowN.size(), a004215Entries.size() + 2);
			}
		}

		// A023105(n) = 1, 2, 2, 3, 4, 7, 12, 23, 44, 87, 172, 343, ...
		LOG.info("quadraticResidueCounts = " + quadraticResidueCounts);

		// The entry counts for n = 0, 1, 2, 3, ... are 
		// 0, 0, 0, 1, 2, 5, 10, 21, 42, 85, 170, 341, 682, 1365, 2730, 5461, ...
		// that is just 2 less than the corresponding A023105 entries (except for n==0)
		LOG.info("A004215 entry counts = " + a004215EntryCounts);
	}
}
