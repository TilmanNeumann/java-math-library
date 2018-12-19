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

import static de.tilman_neumann.jml.base.BigIntConstants.I_1;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.gcd.Gcd63;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.jml.factor.TestsetGenerator;

/**
 * Analysis of Lehman's algorithm:
 * 1. We need (k+N) and a in the same rest classes (%12, %24, %30, ?)
 * 2. If k is even then a must be odd and vice versa
 * 
 * @author Tilman Neumann
 */
public class Lehman_Analyzer2 {
	private static final Logger LOG = Logger.getLogger(Lehman_Analyzer2.class);

	// algorithm options
	/** number of test numbers */
	private static final int N_COUNT = 100000;
	/** the bit size of N to start with */
	private static final int START_BITS = 30;
	/** the increment in bit size from test set to test set */
	private static final int INCR_BITS = 1;
	/** maximum number of bits to test (no maximum if null) */
	private static final Integer MAX_BITS = 63;
	
	private final Gcd63 gcdEngine = new Gcd63();
	
	private Set<Integer>[] aValuesEvenK;
	private Set<Integer>[] aValuesOddK;
	
	private static final int MOD = 12;
	
	public Lehman_Analyzer2() {
		aValuesEvenK = new SortedSet[MOD];
		aValuesOddK = new SortedSet[MOD];
		for (int i=0; i<MOD; i++) {
			aValuesEvenK[i] = new TreeSet<Integer>();
			aValuesOddK[i] = new TreeSet<Integer>();
		}
	}
	
	public BigInteger findSingleFactor(BigInteger N) {
		return BigInteger.valueOf(findSingleFactor(N.longValue()));
	}
	
	public long findSingleFactor(long N) {
		int cbrt = (int) Math.ceil(Math.cbrt(N));
		double sixthRoot = Math.pow(N, 1/6.0); // double precision is required for stability
		for (int k=1; k <= cbrt; k++) {
			long fourKN = k*N<<2;
			double fourSqrtK = Math.sqrt(k<<4);
			int sqrt4kN = (int) Math.ceil(Math.sqrt(fourKN)); // ceil() is required for stability
			int limit = (int) (sqrt4kN + sixthRoot / fourSqrtK);
			for (int a = sqrt4kN; a <= limit; a++) {
				long test = a*(long)a - fourKN;
				long b = (long) Math.sqrt(test);
				if (b*b == test) {
					if ((k&1)==0) {
						aValuesEvenK[(int)((N+k)%MOD)].add(a%MOD);
					} else {
						aValuesOddK[(int)((N+k)%MOD)].add(a%MOD);
					}
					return gcdEngine.gcd(a+b, N);
				}
			}
	    }
		
		// Nothing found. Either N is prime or the implementation is buggy. For N > 45 bit it won't work.
		return 0;
	}
	
	private void testRange(int bits) {
		BigInteger N_min = I_1.shiftLeft(bits-1);
		// find N-set for square tests
		ArrayList<BigInteger> NSet = TestsetGenerator.generate(bits, N_COUNT);
		LOG.info("Test N with " + bits + " bits, i.e. N >= " + N_min);
		
		for (BigInteger N : NSet) {
			this.findSingleFactor(N);
		}
		
		boolean logged = false;
		for (int i=0; i<MOD; i++) {
			if (aValuesEvenK[i].size() > 0) {
				LOG.info("Success a-values for (N+k)%" + MOD + "==" + i + " with even k = " + aValuesEvenK[i]);
				logged = true;
			}
		}
		if (logged) LOG.info("");
		
		logged = false;
		for (int i=0; i<MOD; i++) {
			if (aValuesOddK[i].size() > 0) {
				LOG.info("Success a-values for (N+k)%" + MOD + "==" + i + " with odd k = " + aValuesOddK[i]);
				logged = true;
			}
		}
		if (logged) LOG.info("");
	}

	public static void main(String[] args) {
    	ConfigUtil.initProject();
		int bits = START_BITS;
		while (true) {
			// test N with the given number of bits, i.e. 2^(bits-1) <= N <= (2^bits)-1
	    	Lehman_Analyzer2 testEngine = new Lehman_Analyzer2();
			testEngine.testRange(bits);
			bits += INCR_BITS;
			if (MAX_BITS!=null && bits > MAX_BITS) break;
		}
	}
}
