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

/**
 * Tests for trial division with BigInteger arguments.
 * This test is quite expensive for github CI because of the SMALL_PRIMES initialization;
 * but since it it used in CombinedFactorAlgorithm, we keep it.
 * 
 * @author Tilman Neumann
 */
public class TDivTest extends FactorTestBase {

	public TDivTest() {
		// don't use CombinedFactorAlgorithm as verificationFactorizer because Tdiv is part of it
		super(new TDiv(), new SquFoF63());
	}

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
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
		assertFullFactorizationSuccess(2947524803L, "1433 * 2056891"); // 32 bit
		assertFullFactorizationSuccess(5616540799L, "1777 * 3160687"); // 33 bit
		assertFullFactorizationSuccess(35936505149L, "3299 * 10893151"); // 36 bit
		assertFullFactorizationSuccess(145682871839L, "5261 * 27691099"); // 38 bit
		assertFullFactorizationSuccess(317756737253L, "6823 * 46571411"); // 39 bit
		assertFullFactorizationSuccess(1099511627970L, "2 * 3 * 5 * 7 * 23 * 227642159"); // 41 bit
		assertFullFactorizationSuccess(3294635112749L, "14879 * 221428531"); // 42 bit
		assertFullFactorizationSuccess(13293477682249L, "398077 * 33394237"); // 44 bit
		assertFullFactorizationSuccess(24596491225651L, "3311299 * 7428049"); // 45 bit
		assertFullFactorizationSuccess(44579405690563L, "930889 * 47889067"); // 46 bit
		assertFullFactorizationSuccess(67915439339311L, "2061599 * 32943089"); // 46 bit
		assertFullFactorizationSuccess(72795445155721L, "83459 * 872230019"); // 47 bit
		assertFullFactorizationSuccess(155209074377713L, "361909 * 428862157"); // 48 bit
		assertFullFactorizationSuccess(293851765137859L, "11736397 * 25037647"); // 49 bit
	}

	@Test
	public void testFindSingleFactorForRandomCompositesNearUpperBound() {
		testFindSingleFactorForRandomOddComposites(30, 42, 10000);
	}
}
