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
package de.tilman_neumann.jml;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Tests of the Moebius function implementation.
 * 
 * @author Tilman Neumann
 */
public class MoebiusFunctionTest {
	private static final Logger LOG = LogManager.getLogger(MoebiusFunctionTest.class);
	
	@Before
	public void setup() {
		ConfigUtil.initProject();
	}

	@Test
	public void testSmall() {
		// reference results from https://oeis.org/A008683
    	List<Integer> reference = List.of(1, -1, -1, 0, -1, 1, -1, 0, 0, 1, -1, 0, -1, 1, 1, 0, -1, 0, -1, 0, 1, 1, -1, 0, 0, 1, 0, 0, -1, -1, -1, 0, 1, 1, 1, 0, -1, 1, 1, 0, -1, -1, -1, 0, 0, 1, -1, 0, 0, 0, 1, 0, -1, 0, 1, 0, 1, 1, -1, 0, -1, 1, 0, 0, 1, -1, -1, 0, 1, -1, -1, 0, -1, 1, 0, 0, 1, -1);

		ArrayList<Integer> computedSequence = new ArrayList<>();
		for (int n=1; n<=reference.size(); n++) {
			BigInteger bigN = BigInteger.valueOf(n);
			computedSequence.add(MoebiusFunction.moebius(bigN));
		}
		LOG.info("Moebius function = " + computedSequence);
		assertEquals(reference, computedSequence);
	}
}
