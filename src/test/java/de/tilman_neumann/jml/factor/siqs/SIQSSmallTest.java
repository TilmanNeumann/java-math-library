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
package de.tilman_neumann.jml.factor.siqs;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.jml.factor.FactorTestBase;
import de.tilman_neumann.jml.factor.siqs.poly.SIQSPolyGenerator;
import de.tilman_neumann.util.ConfigUtil;

/**
 * Tests for SIQSSmall.
 * 
 * Typical SIQS implementations cannot factor small numbers (say with less than 60 or 50 bit).
 * A possible reason for this is that the space of possible a-parameters is too restricted.
 */
public class SIQSSmallTest extends FactorTestBase {

	public SIQSSmallTest() {
		super(new SIQSSmall(0.32F, 0.37F, null, new SIQSPolyGenerator(), 10, true));
	}

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}

	@Test
	// TODO On github CI this test may need up to 50 (!) seconds. Locally the test finishes in less than 100ms. 
	// It is possible that the test numbers are not very appropriate for the algorithm, but why the difference between local and github tests?
	public void testSomeInputs() {
		assertFullFactorizationSuccess("15841065490425479923", "2604221509 * 6082841047"); // 64 bit
		assertFullFactorizationSuccess("11111111111111111111111111", "11 * 53 * 79 * 859 * 265371653 * 1058313049"); // 84 bit
		assertFullFactorizationSuccess("5679148659138759837165981543", "3^3 * 466932157 * 450469808245315337"); // 93 bit
		
		// This number is already too big, it causes hanging github CI builds quite often
		//assertFactorizationSuccess("11111111111111111111111111155555555555111111111111111", "67 * 157 * 1056289676880987842105819104055096069503860738769");
	}
}
