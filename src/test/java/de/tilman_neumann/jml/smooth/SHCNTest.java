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
package de.tilman_neumann.jml.smooth;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import de.tilman_neumann.jml.precision.Magnitude;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.Ensure;
import de.tilman_neumann.util.TimeUtil;

/**
 * Tests for superior highly composite numbers (SHCNs).
 * @author Tilman Neumann
 */
public class SHCNTest {
	private static final Logger LOG = LogManager.getLogger(SHCNTest.class);

	@Before
	public void setup() {
		ConfigUtil.initProject();
	}

	@Test
	public void testSHCNSequence() {
		ArrayList<BigInteger> firstComputedSHCNs = new ArrayList<>();

		long startTimeMillis = System.currentTimeMillis();

		SHCNIterator shcnIter = new SHCNIterator();
		Double lastX = null;
		for (int n=1; n<=1000; n++) {
			SHCNEntry entry = shcnIter.next();
			Ensure.ensureEquals(n, entry.getExponentSum());
			double x = entry.getX();
			Double xDiff = lastX!=null ? x-lastX : null;
			BigInteger shcn = entry.getSHCN();
			int digits = Magnitude.of(shcn);
			LOG.info("n=" + n + ": x=" + x + ", xDiff=" + xDiff + ", " + digits + " digits SHCN = " + entry.getSHCN());
			lastX = x;
			if (n<=21) firstComputedSHCNs.add(shcn);
		}
		TreeMap<Integer, SHCNEntry> exponentSum_2_shcnEntries = shcnIter.getExponentSum2SHCNEntries();
		LOG.info(exponentSum_2_shcnEntries.size() + " remaining precomputed SHCNs with exponentSums " + exponentSum_2_shcnEntries.keySet());

		long endTimeMillis = System.currentTimeMillis();
		String durationStr = TimeUtil.timeDiffStr(startTimeMillis, endTimeMillis);
		LOG.info("Computation took " + durationStr);
		
		// reference data from https://oeis.org/A002201
		assertEquals("[2, 6, 12, 60, 120, 360, 2520, 5040, 55440, 720720, 1441440, 4324320, 21621600, 367567200, 6983776800, 13967553600, 321253732800, 2248776129600, 65214507758400, 195643523275200, 6064949221531200]", firstComputedSHCNs.toString());
	}
}
