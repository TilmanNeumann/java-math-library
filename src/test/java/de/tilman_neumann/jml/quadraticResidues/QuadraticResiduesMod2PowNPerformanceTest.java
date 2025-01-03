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
package de.tilman_neumann.jml.quadraticResidues;

import java.math.BigInteger;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Test performance of quadratic residue computations modulo 2^n.
 * 
 * @author Tilman Neumann
 */
public class QuadraticResiduesMod2PowNPerformanceTest {
	
	private static final Logger LOG = LogManager.getLogger(QuadraticResiduesMod2PowNPerformanceTest.class);

	/**
	 * Test.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		ConfigUtil.initProject();

		for (int n=0; n<34; n++) {
			long t0, t1;

			if (n<25) { // otherwise too slow
				t0 = System.currentTimeMillis();
				List<BigInteger> quadraticResidues = QuadraticResiduesMod2PowN.getQuadraticResiduesMod2PowN_testAll_big(n);
				t1 = System.currentTimeMillis();
				LOG.info("v0: n = " + n + ": Computed " + quadraticResidues.size() + " quadratic residues modulo 2^" + n + " in " + (t1-t0) + "ms");
			}
			
			if (n<29) { // avoid OutOfMemoryError
				t0 = System.currentTimeMillis();
				List<Long> quadraticResidues = QuadraticResiduesMod2PowN.getQuadraticResiduesMod2PowN_testAll(n);
				t1 = System.currentTimeMillis();
				LOG.info("v1: n = " + n + ": Computed " + quadraticResidues.size() + " quadratic residues modulo 2^" + n + " in " + (t1-t0) + "ms");
			}
			
			if (n<31) { // avoid OutOfMemoryError
				t0 = System.currentTimeMillis();
				List<Long> quadraticResidues = QuadraticResiduesMod2PowN.getQuadraticResiduesMod2PowN_testAll_v2(n);
				t1 = System.currentTimeMillis();
				LOG.info("v2: n = " + n + ": Computed " + quadraticResidues.size() + " quadratic residues modulo 2^" + n + " in " + (t1-t0) + "ms");
			}
			
			if (n<30) { // avoid OutOfMemoryError
				t0 = System.currentTimeMillis();
				List<Long> quadraticResidues = QuadraticResiduesMod2PowN.getQuadraticResiduesMod2PowN(n);
				t1 = System.currentTimeMillis();
				LOG.info("v3: n = " + n + ": Computed " + quadraticResidues.size() + " quadratic residues modulo 2^" + n + " in " + (t1-t0) + "ms");
			}
			
			if (n<33) { // avoid OutOfMemoryError
				t0 = System.currentTimeMillis();
				int arraySize = (int) ((1L<<n) / 6) + 6; // long conversion gives us 2 more n without "NegativeArraySizeException"
				long[] array = new long[arraySize];
				int count = QuadraticResiduesMod2PowN.getQuadraticResiduesMod2PowN(n, array);
				t1 = System.currentTimeMillis();
				LOG.info("v4: n = " + n + ": Computed " + count + " quadratic residues modulo 2^" + n + " in " + (t1-t0) + "ms");
			}
		}
	}
}
