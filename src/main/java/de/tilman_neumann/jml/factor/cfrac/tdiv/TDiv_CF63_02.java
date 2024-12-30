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
package de.tilman_neumann.jml.factor.cfrac.tdiv;

import java.math.BigInteger;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.factor.base.SortedIntegerArray;
import de.tilman_neumann.jml.factor.base.SortedLongArray;
import de.tilman_neumann.jml.factor.base.congruence.AQPair;
import de.tilman_neumann.jml.factor.base.congruence.AQPairFactory;
import de.tilman_neumann.jml.factor.base.congruence.SmoothPerfect;
import de.tilman_neumann.jml.factor.hart.HartTDivRace;
import de.tilman_neumann.jml.factor.pollardRho.PollardRhoBrentMontgomery64;
import de.tilman_neumann.jml.factor.pollardRho.PollardRhoBrentMontgomeryR64Mul63;
import de.tilman_neumann.jml.factor.tdiv.TDiv31Inverse;
import de.tilman_neumann.jml.primes.probable.PrPTest;
import de.tilman_neumann.util.Ensure;

/**
 * Auxiliary factor algorithm to find smooth decompositions of Q's.
 * 
 * Version 02:
 * Uses trial division first, complete factorization if Q is considered sufficiently smooth.
 * 
 * @author Tilman Neumann
 */
public class TDiv_CF63_02 implements TDiv_CF63 {
	private static final Logger LOG = LogManager.getLogger(TDiv_CF63_02.class);
	private static final boolean DEBUG = false;

	private int primeBaseSize;
	private int[] primesArray;
	private int pMax;
	private long pMaxSquare;

	/** Q is sufficiently smooth if the unfactored Q_rest is smaller than this bound depending on N */
	private double smoothBound;

	private TDiv31Inverse tDiv31 = new TDiv31Inverse();
	private HartTDivRace hart = new HartTDivRace();
	private PollardRhoBrentMontgomeryR64Mul63 pollardRhoR64Mul63 = new PollardRhoBrentMontgomeryR64Mul63();
	private PollardRhoBrentMontgomery64 pollardRho64 = new PollardRhoBrentMontgomery64();
	
	private PrPTest prpTest = new PrPTest();

	// result: two arrays that are reused, their content is _copied_ to AQ-pairs
	private SortedIntegerArray smallFactors = new SortedIntegerArray();
	private SortedLongArray bigFactors = new SortedLongArray();
	private AQPairFactory aqPairFactory = new AQPairFactory();

	@Override
	public String getName() {
		return "TDiv63-02";
	}

	public void initialize(BigInteger N, double smoothBound) {
		this.smoothBound = smoothBound;
	}

	public void initialize(BigInteger kN, int primeBaseSize, int[] primesArray) {
		this.primeBaseSize = primeBaseSize;
		this.primesArray = primesArray;
		this.pMax = primesArray[primeBaseSize-1];
		this.pMaxSquare = pMax * (long) pMax;
	}

	public AQPair test(BigInteger A, long Q) {
		smallFactors.reset();
		bigFactors.reset();
		
		// sign
		long Q_rest = Q;
		if (Q < 0) {
			smallFactors.add(-1);
			Q_rest = -Q;
		}
		// Remove multiples of 2
		int lsb = Long.numberOfTrailingZeros(Q_rest);
		if (lsb > 0) {
			smallFactors.add(2, (short)lsb);
			Q_rest = Q_rest>>lsb;
		}

		// Trial division chain:
		// -> first do it in long, then in int.
		// -> (small or probabilistic) prime tests during trial division just slow it down.
		// -> running indices bottom-up is faster because small dividends are more likely to reduce the size of Q_rest.
		int trialDivIndex = 1; // p[0]=2 has already been tested
		int Q_rest_bits = 64 - Long.numberOfLeadingZeros(Q_rest);
		if (Q_rest_bits>31) {
			// do trial division in long
			while (trialDivIndex < primeBaseSize) {
				int p = primesArray[trialDivIndex];
				if (Q_rest % p == 0) {
					// no remainder -> exact division -> small factor
					smallFactors.add(p);
					Q_rest /= p;
					// After division by a prime base element (typically < 20 bit), Q_rest is 12..61 bits.
					Q_rest_bits = 64 - Long.numberOfLeadingZeros(Q_rest);
					if (Q_rest_bits<32) break; // continue with int
					// trialDivIndex must remain as it is to find the same p more than once
				} else {
					trialDivIndex++;
				}
			} // end while (trialDivIndex < primeBaseSize)
		}
		if (DEBUG) Ensure.ensureGreater(Q_rest, 1);
		if (Q_rest_bits<32) {
			int Q_rest_int = (int) Q_rest;
			while (trialDivIndex < primeBaseSize) {
				// continue trial division in int
				int p = primesArray[trialDivIndex];
				while (Q_rest_int % p == 0) { // in the last loop, a while pays out!
					// no remainder -> exact division -> small factor
					smallFactors.add(p);
					Q_rest_int /= p;
				}
				trialDivIndex++;
			} // end while (trialDivIndex < primeBaseSize)
			if (Q_rest_int==1) return new SmoothPerfect(A, smallFactors);
			Q_rest = (long) Q_rest_int; // keep Q_rest up-to-date
		}

		// trial division was not sufficient to factor Q completely.
		// the remaining Q is either a prime > pMax, or a composite > pMax^2.
		if (Q_rest > smoothBound) return null; // Q is not sufficiently smooth
		 
		// now we consider Q as sufficiently smooth. then we want to know all prime factors, as long as we do not find one that is too big to be useful.
		//LOG.debug("before factor_recurrent()");
		boolean isSmooth = factor_recurrent(Q_rest);
		if (DEBUG) if (bigFactors.size()>2) LOG.debug("Found " + bigFactors.size() + " distinct big factors: " + bigFactors);
		return isSmooth ? aqPairFactory.create(A, smallFactors, bigFactors) : null;
	}

	private boolean factor_recurrent(long Q_rest) {
		if (Q_rest < pMaxSquare) {
			// we divided Q_rest by all primes <= pMax and the rest is < pMax^2 -> it must be prime
			if (DEBUG) Ensure.ensureTrue(prpTest.isProbablePrime(Q_rest));
			if (bitLength(Q_rest) > 31) return false;
			bigFactors.add((int)Q_rest);
			return true;
		}
		// here we can not do without isProbablePrime(), because calling findSingleFactor() may not return when called with a prime argument
		if (prpTest.isProbablePrime(Q_rest)) {
			// Q_rest is (probable) prime -> end of recurrence
			if (bitLength(Q_rest) > 31) return false;
			bigFactors.add((int)Q_rest);
			return true;
		} // else: Q_rest is surely not prime

		// Find a factor of Q_rest, where Q_rest is pMax < Q_rest <= smoothBound, composite and odd.
		long factor1;
		int Q_rest_bits = bitLength(Q_rest);
		if (Q_rest_bits < 25) {
			factor1 = tDiv31.findSingleFactor((int) Q_rest);
		} else if (Q_rest_bits < 50) {
			factor1 = hart.findSingleFactor(Q_rest);
		} else if (Q_rest_bits < 57) {
			factor1 = pollardRhoR64Mul63.findSingleFactor(Q_rest);
		} else { // max Q_rest_bits is 63, pollardRho64 works only until 62 bit, but that should be ok
			factor1 = pollardRho64.findSingleFactor(Q_rest);
		}
		// Recurrence: Is it possible to further decompose the parts?
		// Here we can not exclude factors > 31 bit because they may have 2 prime factors themselves.
		return factor_recurrent(factor1) && factor_recurrent(Q_rest / factor1);
	}
	
	private int bitLength(long n) {
		return 64 - Long.numberOfLeadingZeros(n);
	}
}
