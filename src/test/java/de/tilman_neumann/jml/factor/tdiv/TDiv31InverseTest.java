/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2025 Tilman Neumann - tilman.neumann@web.de
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
package de.tilman_neumann.jml.factor.tdiv;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.jml.factor.FactorTestBase;
import de.tilman_neumann.jml.factor.squfof.SquFoF63;
import de.tilman_neumann.util.ConfigUtil;

public class TDiv31InverseTest extends FactorTestBase {

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		// don't use CombinedFactorAlgorithm as verificationFactorizer because Tdiv is part of it
		setFactorizer(new TDiv31Inverse(), new SquFoF63());
	}
	
	@Test
	public void testSmallestComposites() {
		List<Integer> fails = testFullFactorizationOfComposites(100000);
		assertEquals("Failed to factor n = " + fails, 0, fails.size());
	}

	@Test
	public void testSomeParticularNumbers() {
		assertFullFactorizationSuccess(621887327L, "853 * 729059"); // 30 bit
		assertFullFactorizationSuccess(676762483L, "877 * 771679"); // 30 bit
	}

	@Test
	public void testFindSingleFactorForRandomCompositesNearUpperBound() {
		testFindSingleFactorForRandomOddComposites(20, 31, 10000);
	}
}
