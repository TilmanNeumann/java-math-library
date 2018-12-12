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

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.FactorAlgorithmBase;
import de.tilman_neumann.jml.gcd.Gcd63;

/**
 * Faster implementation of Lehmans factor algorithm following https://programmingpraxis.com/2017/08/22/lehmans-factoring-algorithm/.
 * Improvements inspired by Thilo Harich (https://github.com/ThiloHarich/factoring.git).
 * Works for N <= 45 bit.
 * 
 * @author Tilman Neumann
 */
public class Lehman extends FactorAlgorithmBase {
	private static final Logger LOG = Logger.getLogger(Lehman.class);
	private static final boolean DEBUG = false;

	private double[] sqrt;
	
    private static final boolean[] isSquareMod1024 = isSquareMod1024();

    private static boolean[] isSquareMod1024() {
    	boolean[] isSquareMod_1024 = new boolean[1024];
        for (int i = 0; i < 1024; i++) {
        	isSquareMod_1024[(i * i) & 1023] = true;
        }
        return isSquareMod_1024;
    }

    /**
     * A multiplicative constant to adjust the limit of trial division and k-loop.
     * Older implementations of the algorithm correspond to kLimitMultiplier = 1.
     */
	private float kLimitMultiplier;
	
	private final Gcd63 gcdEngine = new Gcd63();

    public Lehman(float kLimitMultiplier) {
    	this.kLimitMultiplier = kLimitMultiplier;
    	SMALL_PRIMES.ensurePrimeCount(10000); // for kLimitMultiplier ~ 2 we need more than 4793 primes
    	initSqrts();
    }
    
	private void initSqrts() {
		// precompte sqrts for all possible k. Requires ~ (kLimitMultiplier*2^15) entries.
		int kMax = (int) (kLimitMultiplier*Math.cbrt(1L<<45) + 1);
		//LOG.debug("kMax = " + kMax);
		
		sqrt = new double[kMax + 1];
		for (int i = 1; i < sqrt.length; i++) {
			final double sqrtI = Math.sqrt(i);
			sqrt[i] = sqrtI;
		}
		LOG.info("Lehman: Built sqrt table for multiplier " + kLimitMultiplier + " with " + sqrt.length + " entries");
	}

	@Override
	public String getName() {
		return "Lehman(" + kLimitMultiplier + ")";
	}

	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		return BigInteger.valueOf(findSingleFactor(N.longValue()));
	}
	
	public long findSingleFactor(long N) {
		// 1. Check via trial division whether N has a nontrivial divisor d <= cbrt(N), and if so, return d.
		double cbrt = Math.ceil(Math.cbrt(N));
		int tDivLimit = (int) (kLimitMultiplier*cbrt);
		int i=0, p;
		while ((p = SMALL_PRIMES.getPrime(i++)) <= tDivLimit) {
			if (N%p==0) return p;
		}
		
		// 2. Main loop
		int kLimit = tDivLimit;
		//LOG.debug("kLimit = " + kLimit);
		long N4 = N<<2;
		double sqrt4N = Math.sqrt(N4);
		double sixthRootTerm = 0.25 * Math.pow(N, 1/6.0); // double precision is required for stability
		for (int k=1; k <= kLimit; k++) {
			int sqrt4kN = (int) Math.ceil(sqrt4N * sqrt[k]); // ceil() is required for stability
			// The above statement may give too small results for 4kN >= 55 bit, and then we'ld get
			// test<0 below. This problem appears first at N with 41 bit (4kN ~ 55 bit) and becomes
			// inevitable when N reaches 46 bit (4kN >= 63 bit). Fix it:
			long fourKN = k*N4;
			while (sqrt4kN*(long)sqrt4kN < fourKN) {
				if (DEBUG) LOG.debug("fourKN=" + fourKN + " (" + bitLength(fourKN) + " bit), sqrt4kN=" + sqrt4kN + " (" + bitLength(sqrt4kN) + " bit)");
				sqrt4kN++;
			}
			final int aLimit = (int) (sqrt4kN + sixthRootTerm / sqrt[k]);
			int aStart, aStep;
			if ((k&1)==1) {
				// k is odd
				aStart = sqrt4kN;
				aStep = 1;
				// XXX unsuccessful improvement attempt following https://de.wikipedia.org/wiki/Faktorisierungsmethode_von_Lehman
				//final int m = (k+Nmod4-sqrt4kN)&3;
				//aStart = m<0 ? sqrt4kN + m + 4 : sqrt4kN + m;
				//aStep = 4;
			} else {
				// k even -> make sure aStart is odd
				aStart = sqrt4kN | 1;
				aStep = 2;
			}
			for (int a=aStart; a <= aLimit; a+=aStep) {
				long test = a*(long)a - fourKN;
		        if (isSquareMod1024[(int) (test & 1023)]) {
		        	long b = (long) Math.sqrt(test);
		        	if (b*b == test) {
		        		return gcdEngine.gcd(a+b, N);
		        	}
				}
			}
	    }
		
		// Nothing found. Either N is prime or the algorithm didn't work because N > 45 bit.
		return 0;
	}

	private static int bitLength(long arg) {
		return 64 - Long.numberOfLeadingZeros(arg);
	}
}
