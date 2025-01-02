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

import static de.tilman_neumann.jml.base.BigIntConstants.*;
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
import de.tilman_neumann.util.TimeUtil;

/**
 * Tests for colossally abundant numbers (CANs).
 * @author Tilman Neumann
 */
public class CANTest {
	private static final Logger LOG = LogManager.getLogger(CANTest.class);

	@Before
	public void setup() {
		ConfigUtil.initProject();
	}

	@Test
	public void testCANFromEpsilon() {
		assertCanEntrySuccessfull(1.0, 0, I_1);
		assertCanEntrySuccessfull(0.5, 1, I_2);
		assertCanEntrySuccessfull(0.25, 2, I_6);
		assertCanEntrySuccessfull(0.20, 3, I_12);
		assertCanEntrySuccessfull(0.10, 4, I_60);
		assertCanEntrySuccessfull(0.09, 5, BigInteger.valueOf(120));
		assertCanEntrySuccessfull(0.08, 5, BigInteger.valueOf(120));
		assertCanEntrySuccessfull(0.07, 6, BigInteger.valueOf(360));
		assertCanEntrySuccessfull(0.05, 7, BigInteger.valueOf(2520));
		assertCanEntrySuccessfull(0.04, 8, BigInteger.valueOf(5040));
	}
	
	private void assertCanEntrySuccessfull(double epsilon, int n, BigInteger can) {
		CANEntry entry = CANEntry.computeCAN(epsilon);
		assertEquals(n, entry.getExponentSum());
		assertEquals(can, entry.getCAN());
	}

	@Test
	public void testCANSequence() {
		ArrayList<BigInteger> firstComputedCANs = new ArrayList<>();

		long startTimeMillis = System.currentTimeMillis();

		CANIterator canIter = new CANIterator();
		Double lastEpsilon = null;
		for (int n=1; n<=1000; n++) {
			CANEntry entry = canIter.next();
			assertEquals(n, entry.getExponentSum());
			double epsilon = entry.getEpsilon();
			Double epsilonQuot = lastEpsilon!=null ? lastEpsilon/epsilon : null;
			BigInteger can = entry.getCAN();
			int digits = Magnitude.of(can);
			LOG.info("n=" + n + ": epsilon=" + epsilon + ", epsilonQuot=" + epsilonQuot + ", " + digits + " digits CAN = " + entry.getCAN());
			lastEpsilon = epsilon;
			if (n<=22) firstComputedCANs.add(can);
		}
		TreeMap<Integer, CANEntry> exponentSum_2_canEntries = canIter.getExponentSum2CanEntries();
		LOG.info(exponentSum_2_canEntries.size() + " remaining precomputed CANs with exponentSums " + exponentSum_2_canEntries.keySet());

		long endTimeMillis = System.currentTimeMillis();
		String durationStr = TimeUtil.timeDiffStr(startTimeMillis, endTimeMillis);
		LOG.info("Computation took " + durationStr);
		
		// reference data from https://oeis.org/A004490
		assertEquals("[2, 6, 12, 60, 120, 360, 2520, 5040, 55440, 720720, 1441440, 4324320, 21621600, 367567200, 6983776800, 160626866400, 321253732800, 9316358251200, 288807105787200, 2021649740510400, 6064949221531200, 224403121196654400]", firstComputedCANs.toString());
	}
}
