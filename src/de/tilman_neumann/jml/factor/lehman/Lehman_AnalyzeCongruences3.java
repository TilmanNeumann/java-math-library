/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018 Tilman Neumann (www.tilman-neumann.de)
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
package de.tilman_neumann.jml.factor.lehman;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.gcd.Gcd63;
import de.tilman_neumann.jml.quadraticResidues.QuadraticResiduesMod2PowN;
import de.tilman_neumann.jml.roots.SqrtExact;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.jml.factor.TestsetGenerator;
import de.tilman_neumann.jml.factor.TestNumberNature;

/**
 * Test hypotheses on the successful a-values in Lehman's factorin algorithm.
 * 
 * @author Tilman Neumann
 */
public class Lehman_AnalyzeCongruences3 {
	private static final Logger LOG = Logger.getLogger(Lehman_AnalyzeCongruences3.class);
	
	private static final boolean DEBUG = true;

	/** Use congruences a==kN mod 2^s if true, congruences a==(k+N) mod 2^s if false */
	private static final boolean USE_kN_CONGRUENCES = true;

	private final Gcd63 gcdEngine = new Gcd63();
	
	private int[][] counts; // dimensions: kN%KNMOD, a%KNMOD
	
	private List<Long> qr;
	private TreeSet<Long> qrComplements;
	private TreeSet<Integer> offsets;
	
	ArrayList<Integer>[] aForKN = null;

	public long findSingleFactor(long N, int KNMOD) {
		int cbrt = (int) Math.ceil(Math.cbrt(N));
		double sixthRoot = Math.pow(N, 1/6.0); // double precision is required for stability
		for (int k=1; k <= cbrt; k++) {
			long kN = k*N;
			long fourKN = k*N<<2;
			double fourSqrtK = Math.sqrt(k<<4);
			long sqrt4kN = (long) Math.ceil(Math.sqrt(fourKN));
			long limit = (long) (sqrt4kN + sixthRoot / fourSqrtK);
			for (long a0 = sqrt4kN; a0 <= limit; a0++) {
				for (int adjust=0; adjust<KNMOD; adjust++) {
					long a = a0 + adjust;
					final long test = a*a - fourKN;
					final long b = (long) Math.sqrt(test);
					if (b*b == test) {
						long gcd = gcdEngine.gcd(a+b, N);
						if (gcd>1 && gcd<N) {
							// congruences are the same for all odd k
							if ((k & 1) == 1) {
								long kNTerm = USE_kN_CONGRUENCES ? k*N : k+N;
								int kNMod = (int) (kNTerm%KNMOD);
								int aMod = (int) (a%KNMOD);
								if ((kNMod & 7) == 1) {
									// test some hypotheses...
									long offset = (kN+1 - (a*a)/4) % KNMOD;
									if (offset<0) offset += KNMOD;
									long offset2 = (offset-1)/16;
									// LOG.debug("kN = " + kNMod + ", aMod=" + aMod + ": offset = " + offset + ", offset2 = " + offset2);
									offsets.add((int) offset2);
									assertTrue(qrComplements.contains(Long.valueOf(offset2)));
									// Uuuuh. The former means that test == 64 * (some quadratic residue) (mod KNMOD) for not too small KNMODs !
									if (KNMOD>4) {
										assertEquals(0, test % 64);
										long testMod = (test/64) % KNMOD;
										if (DEBUG) LOG.debug("test/64 = " + (test/64) + ", testMod = " + testMod + ", qr = " + qr);
										assertTrue(qr.contains(testMod));
										// test/64 is always a square, so then is test !
										long lowerSqrt = SqrtExact.exactSqrt(BigInteger.valueOf(test/64)).longValue();
										assertEquals(test/64, lowerSqrt*lowerSqrt);
									}
								}
								
								// We know that all elements of an antidiagonal (a0, adjust) with a0 + adjust == a (mod KNMOD)
								// represent the same "successful a". Thus we only need to store results for "a" !
								counts[kNMod][aMod]++;
							}
							return gcd; // removes the blur at even k!
						}
					}
				}
			}
	    }
		
		return 0; // Fail
	}
	
	private void test() {
		for (int KNMOD = 2; ; KNMOD<<=1) {
			int n = KNMOD>0 ? Integer.numberOfTrailingZeros(KNMOD) : 0;
			LOG.info("Test KNMOD = " + KNMOD + ", n = " + n + " ...");
			
			counts = new int[KNMOD][KNMOD];
			offsets = new TreeSet<Integer>();
			
			int bits = 15;
			BigInteger[] testNumbers = TestsetGenerator.generate(KNMOD*100, bits, TestNumberNature.MODERATE_SEMIPRIMES);
			
			qr = QuadraticResiduesMod2PowN.getQuadraticResiduesMod2PowN(n);
			
			qrComplements = QuadraticResiduesMod2PowN.getComplementOfQuadraticResiduesMod2PowN(n-4);
			LOG.info("qrComplements = " + qrComplements);

			for (BigInteger N : testNumbers) {
				//if (N.mod(I_6).equals(I_5)) // makes no difference
				this.findSingleFactor(N.longValue(), KNMOD); // this is the expensive part
			}
			
			LOG.info("Found offsets = " + offsets);
			LOG.info("");
		}
	}

	public static void main(String[] args) {
    	ConfigUtil.initProject();
    	
    	new Lehman_AnalyzeCongruences3().test();
	}
}
