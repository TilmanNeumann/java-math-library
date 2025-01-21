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
package de.tilman_neumann.jml.factor;

import static de.tilman_neumann.jml.base.BigIntConstants.I_1;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import de.tilman_neumann.jml.primes.probable.BPSWTest;
import de.tilman_neumann.util.SortedMultiset;

/**
 * Infrastructure to simplify unit tests of factoring algorithms.
 * 
 * @author Tilman Neumann
 */
public class FactorTestInfrastructure {
	
	private static BPSWTest bpsw = new BPSWTest();

	/**
	 * Test factorization of all small composites <= nMax by the given factorAlgorithm.<br/><br/>
	 * 
	 * This test could be made more efficient using a composites sieve.
	 * 
	 * @param nMax
	 * @param factorAlgorithm
	 * @return list of the factor arguments that were not factored correctly
	 */
	public static List<Integer> testSmallComposites(int nMax, FactorAlgorithm factorAlgorithm) {
		ArrayList<Integer> fails = new ArrayList<>();
		for (int n=4; n<=nMax; n++) {
			if (bpsw.isProbablePrime(n)) continue; // skip primes
			BigInteger nBig = BigInteger.valueOf(n);
			SortedMultiset<BigInteger> factors = factorAlgorithm.factor(nBig);
			boolean isFail = false;
			BigInteger testProd = I_1;
			for (BigInteger factor : factors.keySet()) {
				if (!bpsw.isProbablePrime(factor)) {
					isFail = true;
					break;
				}
				int exp = factors.get(factor).intValue();
				BigInteger pow = factor.pow(exp);
				testProd = testProd.multiply(pow);
			}
			if (!testProd.equals(nBig)) {
				isFail = true;
			}
			if (isFail) {
				fails.add(n);
			}
		}
		return fails;
	}
}
