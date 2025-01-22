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
import de.tilman_neumann.jml.factor.base.matrixSolver.MatrixSolverBlockLanczos;
import de.tilman_neumann.jml.factor.siqs.poly.SIQSPolyGenerator;
import de.tilman_neumann.jml.factor.siqs.powers.NoPowerFinder;
import de.tilman_neumann.jml.factor.siqs.sieve.Sieve03hU;
import de.tilman_neumann.jml.factor.siqs.tdiv.TDiv_QS_2LP;
import de.tilman_neumann.util.ConfigUtil;

/**
 * SIQS tests.
 * 
 * Typical SIQS implementations cannot factor small numbers (say with less than 60 or 50 bit).
 * A possible reason for this is that the space of possible a-parameters is too restricted.
 * 
 * It it already quite a wonder that the complete factorizations in <code>testSomeInputs()</code> work reliably,
 * because they may need intermediate facorizations of very small numbers.
 */
public class SIQSTest extends FactorTestBase {

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
		setFactorizer(new SIQS(0.31F, 0.37F, null, new NoPowerFinder(), new SIQSPolyGenerator(), new Sieve03hU(), new TDiv_QS_2LP(true), 10, new MatrixSolverBlockLanczos()));
	}

	@Test
	public void testSomeInputs() {
		assertFullFactorizationSuccess("15841065490425479923", "2604221509 * 6082841047"); // 64 bit
		assertFullFactorizationSuccess("11111111111111111111111111", "11 * 53 * 79 * 859 * 265371653 * 1058313049"); // 84 bit
		assertFullFactorizationSuccess("5679148659138759837165981543", "3^3 * 466932157 * 450469808245315337"); // 93 bit
		assertFullFactorizationSuccess("11111111111111111111111111155555555555111111111111111", "67 * 157 * 1056289676880987842105819104055096069503860738769");
	}
}
