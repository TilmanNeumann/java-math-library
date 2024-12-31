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
package de.tilman_neumann.jml.primes.probable;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Performance test of nextProbablePrime() implementations (currently only Java vs. BPSW).
 * 
 * @author Tilman Neumann
 */
public class NextProbablePrimePerformanceTest {
	private static final Logger LOG = LogManager.getLogger(NextProbablePrimePerformanceTest.class);
	private static final SecureRandom RNG = new SecureRandom();
	
	/** Number of test numbers for the performance test. */
	private static final int NCOUNT = 1000;

	private static final BPSWTest bpsw = new BPSWTest();

	/**
	 * Performance test.
	 * Each algorithm is test with the same NCOUNT test numbers in random order.
	 */
	private static void testPerformance() {
		for (int nBits = 20; ; nBits+=10) {
			LOG.info("Test performance of " + NCOUNT + " N with " + nBits + " bits:");
			// create test set with NCOUNT n having nBits bits.
			int i = 0;
			BigInteger[] testSet = new BigInteger[NCOUNT];
			while (i < NCOUNT) {
				testSet[i] = new BigInteger(nBits, RNG);
				if (testSet[i].signum()>0 && testSet[i].bitLength()==nBits) i++; // exclude 0 from test set, assure correct bit size
			}

			long startMillis, duration;
			TreeMap<Long, List<String>> duration_2_algLists = new TreeMap<Long, List<String>>();

			// test BPSW
			startMillis = System.currentTimeMillis();
			for (BigInteger n : testSet) {
				bpsw.nextProbablePrime(n);
			}
			duration = System.currentTimeMillis() - startMillis;
			addToMap(duration_2_algLists, duration, "BPSW");

			// test built-in method
			startMillis = System.currentTimeMillis();
			for (BigInteger n : testSet) {
				@SuppressWarnings("unused")
				BigInteger result = n.nextProbablePrime();
			}
			duration = System.currentTimeMillis() - startMillis;
			addToMap(duration_2_algLists, duration, "Java");

			// results for nBits
			logMap(duration_2_algLists);
		}
	}
	
	private static void addToMap(TreeMap<Long, List<String>> duration_2_algLists, Long duration, String algStr) {
		List<String> algList = duration_2_algLists.get(duration);
		if (algList==null) algList = new ArrayList<String>();
		algList.add(algStr);
		duration_2_algLists.put(duration, algList);
	}
	
	private static void logMap(TreeMap<Long, List<String>> duration_2_algLists) {
		int rank = 1;
		for (long duration : duration_2_algLists.keySet()) {
			int count=0;
			List<String> algList = duration_2_algLists.get(duration);
			for (String algStr : algList) {
				LOG.info("#"+rank + ": " + algStr + " took " + duration + "ms");
				count++;
			}
			rank += count;
		}
	}
	
	/**
	 * Stand-alone test.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		ConfigUtil.initProject();
		testPerformance();
	}
}
