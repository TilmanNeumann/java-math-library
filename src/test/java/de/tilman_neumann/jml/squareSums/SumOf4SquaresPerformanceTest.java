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

import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Performance test.
 * 
 * @author Tilman Neumann
 */
public class SumOf4SquaresPerformanceTest {

	private static final Logger LOG = LogManager.getLogger(SumOf4SquaresPerformanceTest.class);

	public static void main(String[] args) {
		ConfigUtil.initProject();
		
		for (int n=0; n<=30; n++) {
			LOG.info("n = " + n + ":");
			long m = 1L<<n;
			
			long t0, t1;
		
			t0 = System.currentTimeMillis();
			TreeSet<Long> a004215Entries = SumOf4Squares.computeNumbersThatAreNoLessThan4NonzeroSquares_v1(m);
			t1 = System.currentTimeMillis();
			LOG.info("v1: Computed " + a004215Entries.size() + " A004215 entries < " + m + " in " + (t1-t0) + "ms");
			
			t0 = System.currentTimeMillis();
			TreeSet<Long> a004215Entries_v2 = SumOf4Squares.computeNumbersThatAreNoLessThan4NonzeroSquares_v2(n);
			t1 = System.currentTimeMillis();
			LOG.info("v2: Computed " + a004215Entries_v2.size() + " A004215 entries < " + m + " in " + (t1-t0) + "ms");
			
			t0 = System.currentTimeMillis();
			long[] a004215Entries_v3 = new long[((1<<n) / 6) + 4]; // #{A004215(k) | k<m} is always near to m/6
			int count = SumOf4Squares.computeNumbersThatAreNoLessThan4NonzeroSquares/*_v3*/(n, a004215Entries_v3);
			t1 = System.currentTimeMillis();
			LOG.info("v3: Computed " + count + " A004215 entries < " + m + " in " + (t1-t0) + "ms");
		}
	}
}
