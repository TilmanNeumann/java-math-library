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


import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.gcd.Gcd63;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.jml.factor.TestsetGenerator;
import de.tilman_neumann.jml.factor.TestNumberNature;

/**
 * Analyze the moduli of a-values that help the Lehman algorithm to find factors.
 * 
 * If we analyze the data in terms of (a0, adjust) pairs, we notice that we always get antidiagonals, each of
 * them representing a "successful a", because a == (a0 + adjust) (mod KNMOD). So all we need to investigate is "a".
 * 
 * Congruences a == kN (mod 2^s) are slightly more discriminative
 * than Lehman's original congruences a == (k+N) (mod 2^s), s = 1, 2, 3, ...
 * 
 * Version 3 doubles KNMOD step-by-step and analyzes or allows to analyze incremental changes.
 * Only odd k are analyzed, because the result for even k is trivial (we need all odd "a"-values).
 * 
 * Successful a from k*N congruences are 1, 2, 6, 16, 56, 192, 736, 2816, 11136, 44014, ... (not found in OEIS)
 * Successful a from k+N congruences are 1, 2, 6, 16, 64, 256, 1024, ... (last improvement at KNMOD = 16)
 * 
 * @author Tilman Neumann
 */
public class Lehman_AnalyzeCongruences2 {
	private static final Logger LOG = Logger.getLogger(Lehman_AnalyzeCongruences2.class);
	
	/** Use congruences a==kN mod 2^s if true, congruences a==(k+N) mod 2^s if false */
	private static final boolean USE_kN_CONGRUENCES = true;
	private static final boolean PRINT_LAST_SUCCESSFUL_A = false;

	private final Gcd63 gcdEngine = new Gcd63();
	
	private int[][] counts; // dimensions: kN%KNMOD, a%KNMOD
	ArrayList<Integer>[] aForKN = null;

	public long findSingleFactor(long N, int KNMOD) {
		int cbrt = (int) Math.ceil(Math.cbrt(N));
		double sixthRoot = Math.pow(N, 1/6.0); // double precision is required for stability
		for (int k=1; k <= cbrt; k++) {
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
								// We know that all elements of an antidiagonal (a0, adjust) with a0 + adjust == a (mod KNMOD)
								// represent the same "successful a". Thus we only need to store results for "a" !
								long kNTerm = USE_kN_CONGRUENCES ? k*N : k+N;
								counts[(int)(kNTerm%KNMOD)][(int)(a%KNMOD)]++;
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
			LOG.info("Test KNMOD = " + KNMOD + " ...");
			
			counts = new int[KNMOD][KNMOD];
			
			int bits = 30;
			BigInteger[] testNumbers = TestsetGenerator.generate(KNMOD*2000, bits, TestNumberNature.MODERATE_SEMIPRIMES);
			
			for (BigInteger N : testNumbers) {
				//if (N.mod(I_6).equals(I_5)) // makes no difference
				this.findSingleFactor(N.longValue(), KNMOD); // this is the expensive part
			}
			
			// extrapolate last KNMOD results
			List<Integer>[] lastAForKN = null;
			if (aForKN != null) {
				lastAForKN = new List[KNMOD];
				int kN=0;
				for (; kN<KNMOD/2; kN++) {
					// copy old a-values
					ArrayList<Integer> lastAList = aForKN[kN];
					lastAForKN[kN] = new ArrayList<>(lastAList); // copy
					if (lastAList != null && !lastAList.isEmpty()) {
						// extend on horizontal axis
						if (lastAList.get(0) == 0) {
							lastAForKN[kN].add(KNMOD/2);
							lastAList.remove(0);
						}
						Collections.reverse(lastAList);
						for (int a : lastAList) {
							int elem = KNMOD - a;
							lastAForKN[kN].add(elem);
						}
					}
				}
				// extend on vertical axis
				for (; kN<KNMOD; kN++) {
					lastAForKN[kN] = new ArrayList<>(lastAForKN[kN-(KNMOD>>1)]);
				}
			}
			// now we have extrapolated the last a-values for [KNMOD/2][KNMOD/2] to [KNMOD][KNMOD]
			
			LOG.debug("Compute a-lists...");
			String kNStr = USE_kN_CONGRUENCES ? "kN" : "k+N";
			aForKN = new ArrayList[KNMOD];
			int totalACount = 0;
			
			for (int kN=0; kN<KNMOD; kN++) {
				int[] aSuccessCounts = counts[kN];
				int knSuccessCount = 0;
				ArrayList<Integer> aList = new ArrayList<>();
				for (int a=0; a<KNMOD; a++) {
					if (aSuccessCounts[a] > 0) {
						knSuccessCount += aSuccessCounts[a];
						aList.add(a);
					}
				}
				if (knSuccessCount > 0) {
					int avgASuccessCount = knSuccessCount/aList.size(); // avg. factoring successes per "a"
					LOG.info("(" + kNStr + ")%" + KNMOD + "=" + kN + ": successful a = " + aList + " (mod " + KNMOD + "), avg hits = " + avgASuccessCount);
				}
				// collect data plot for odd k (results are equal for all odd k)
				aForKN[kN] = aList;
				totalACount += aList.size();
			}
			LOG.info("");

			// the following block is only for debugging
			if (PRINT_LAST_SUCCESSFUL_A) {
				if (lastAForKN != null) {
					int knStart = USE_kN_CONGRUENCES ? 1 : 0;
					for (int kN=knStart; kN<KNMOD; kN+=2) {
						String row = "";
						int i=0;
						for (int a : lastAForKN[kN]) {
							while (i++<a) row += " ";
							row += "x";
						}
						LOG.info(row);
					}
				}
				LOG.info("");
			}

			// compute the "a" that have changed from successful at last KNMOD to unsuccessful at current KNMOD
			int totalDroppedACount = 0;
			int knStart = USE_kN_CONGRUENCES ? 1 : 0;
			if (lastAForKN != null) {
				for (int kN=knStart; kN<KNMOD; kN+=2) {
					List<Integer> droppedAList = new ArrayList<Integer>();
					for (int a : lastAForKN[kN]) {
						if (!aForKN[kN].contains(a)) {
							droppedAList.add(a);
						}
					}
					if (!droppedAList.isEmpty()) {
						LOG.info("(" + kNStr + ")%" + KNMOD + "=" + kN + ": dropped a = " + droppedAList + " (mod " + KNMOD + ")");
						totalDroppedACount += droppedAList.size();
					}
				}
				LOG.info("");
			}
			
			// create data plot for odd k
			for (int kN=knStart; kN<KNMOD; kN+=2) {
				String row = "";
				int i=0;
				if (lastAForKN == null) {
					for (int a : aForKN[kN]) {
						while (i++<a) row += " ";
						row += "x";
					}
				} else {
					for (int a : lastAForKN[kN]) {
						while (i++<a) row += " ";
						row += aForKN[kN].contains(a) ? "x" : ".";
					}
				}
				LOG.info(row);
			}

			LOG.info("");
			LOG.info("totalACount = " + totalACount);
			LOG.info("totalDroppedACount = " + totalDroppedACount);
			LOG.info("");
		}
	}
	
	public static void main(String[] args) {
    	ConfigUtil.initProject();
    	new Lehman_AnalyzeCongruences2().test();
	}
}
