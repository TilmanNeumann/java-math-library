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
package de.tilman_neumann.jml.factor;

import java.math.BigInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.Ensure;
import de.tilman_neumann.util.SortedMultiset;

public class FactorSieveDemo {
	private static final Logger LOG = LogManager.getLogger(FactorSieveDemo.class);
	private static final boolean DEBUG = false;

	public static void main(String[] args) {
		ConfigUtil.initProject();
		
		long start = 99000000, limit = 100000000;
		FactorSieve sieve = new FactorSieve(start, limit);
		long t0 = System.currentTimeMillis();
		sieve.sieve();
		long t1 = System.currentTimeMillis();
		LOG.info("Factoring all numbers from " + start + " to " + limit + " using the sieve took " + (t1-t0) + " milliseconds.");
		if (DEBUG) {
			for (long n=start; n<=limit; n++) { // n==1 gives null factors
				SortedMultiset<Long> factors = sieve.getFactorization(n);
				LOG.info(n + " = " + factors);
				if (n>1) {
					long test = FactorSieve.computeProduct(factors);
					Ensure.ensureEquals(n, test);
				}
			}
		}
		
		// without batch
		FactorAlgorithm factorizer = FactorAlgorithm.getDefault();
		long t2 = System.currentTimeMillis();
		for (long n=start; n<=limit; n++) {
			factorizer.factor(BigInteger.valueOf(n));
		}
		long t3 = System.currentTimeMillis();
		LOG.info("Factoring all numbers from " + start + " to " + limit + " individually took " + (t3-t2) + " milliseconds.");
	}
}
