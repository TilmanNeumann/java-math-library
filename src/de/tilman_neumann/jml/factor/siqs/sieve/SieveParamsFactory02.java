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
package de.tilman_neumann.jml.factor.siqs.sieve;

import java.math.BigInteger;

import org.apache.log4j.Logger;

/**
 * Factory to compute some basic parameters for the sieve.
 * Version 02 differentiates between the maximum bounds for sieve hits, smooth candidates, and smooths relations.
 * 
 * @see [Contini]: Scott Patrick Contini, "Factoring Integers with the self-initializing quadraric sieve", Master thesis, University of Georgia, 1997
 * 
 * @author Tilman Neumann
 */
public class SieveParamsFactory02 {
	private static final Logger LOG = Logger.getLogger(SieveParamsFactory02.class);
	private static final boolean DEBUG = false;
	
	public static SieveParams create(double N_dbl, int NBits, BigInteger kN, int d, int[] primeBase, int primeBaseSize, int sieveArraySize) {
		
		// Compute biggest QRest admitted for a smooth relation.
		// The following (sieveHitExp, tdivTestExp, smoothBoundExponent) constant triples have been tested so far:
		// (0.17 , 0.155, 0.14) : best until 330 bit
		// (0.165, 0.165, 0.15) : not so good
		// (0.165, 0.155, 0.145): third best for not too large N
		// (0.165, 0.15 , 0.14) : overall good, best at a 340 bit test
		// (0.16,  0.16,  0.14 ): ok at large N
		// (0.16,  0.145, 0.13 ): not so good
		double progessivePart = (NBits<=150) ? 0 : (NBits-150.0)/5250;
		double sieveHitExp = 0.165 + progessivePart;
		double tdivTestExp = 0.15 + progessivePart;
		double smoothBoundExponent = 0.14 + progessivePart;
		
		double sieveHitBound = Math.pow(N_dbl, sieveHitExp);
		double tdivTestBound = Math.pow(N_dbl, tdivTestExp);
		double smoothBound = Math.pow(N_dbl, smoothBoundExponent);
		if (DEBUG) LOG.debug("sieveHitBound = " + sieveHitBound + ", tdivTestBound = " + tdivTestBound + ", smoothBound = " + smoothBound);
		
		// pMinIndex ~ primeBaseSize^0.33 looks clearly better than primeBaseSize^0.3 or primeBaseSize^0.36.
		// We avoid p[0]==2 which is not used in several sieves.
		int pMinIndex = Math.max(1, (int) Math.cbrt(primeBaseSize));
		int pMin = primeBase[pMinIndex];
		int pMax = primeBase[primeBaseSize-1];
		
		// Compute ln(Q/(da)), the natural logarithm of (theoretically) maximal Q/(da)-values:
		// ln(Q/a)    = ln(M) + ln(kN)/2 - ln(sqrt(2)) if d=1, as proposed by [Contini] (p.7), Pomerance and Silverman,
		// ln(Q/(2a)) = ln(M) + ln(kN)/2 - ln(sqrt(8)) if d=2.
		double lnQdivDaEstimate = Math.log(sieveArraySize * Math.sqrt(kN.doubleValue()/2) / d);
		
		// Compute the minimal sum of ln(p_i) values required for a Q to pass the sieve.
		double minLnPSum = lnQdivDaEstimate - Math.log(sieveHitBound);
		
		// convert the sieve bound from natural logarithm to the actual logBase:
		double lnLogBase = minLnPSum / 127; // normalizer to be used as a divisor for p_i values
		double minLogPSum = minLnPSum / lnLogBase; // ~ 127
		
		double tdivTestMinLnPSum = lnQdivDaEstimate - Math.log(tdivTestBound);
		int tdivTestMinLogPSum = (int) (tdivTestMinLnPSum / lnLogBase);
		int logQdivDaEstimate = (int) (lnQdivDaEstimate / lnLogBase);

		if (DEBUG) {
			float logBase = (float) Math.exp(lnLogBase);
			LOG.debug("logBase=" + logBase + ", lnLogBase=" + lnLogBase + ", minLnPSum = " + minLnPSum + ", minLogPSum = " + minLogPSum);
		}
		byte initializer = computeInitializerValue(primeBase, pMinIndex, minLogPSum, lnLogBase);
		float lnPMultiplier = (float) (1.0/lnLogBase); // normalizer to be used as a multiplier for p_i values (faster than a divisor)
		
		return new SieveParams(kN, pMinIndex, pMin, pMax, sieveArraySize, sieveHitBound, tdivTestBound, smoothBound, logQdivDaEstimate, tdivTestMinLogPSum, initializer, lnPMultiplier);
	}
	
	/**
	 * Compute the initializer value.
	 * @param primesArray prime base
	 * @param pMinIndex the index of the first prime used for sieving
	 * @param minLogPSum
	 * @param lnLogBase
	 * @return initializer byte value
	 */
	private static byte computeInitializerValue(int[] primesArray, int pMinIndex, double minLogPSum, double lnLogBase) {
		// compute contribution of small primes in nats
		double lnSmallPSum = 0;
		for (int i=pMinIndex-1; i>=0; i--) {
			int p = primesArray[i];
			lnSmallPSum += Math.log(p) / p;
		}
		// XXX also add the contribution of the q-params? (they are not sieved with either)
		
		// convert value from base e to wanted log base
		double logSmallPSum = lnSmallPSum / lnLogBase;
		// compute initializerValue, rounded; combining doubles is more accurate than combining ints
		byte initializerValue = (byte) (128 - minLogPSum + logSmallPSum + 0.5);
		if (DEBUG) LOG.debug("initializerValue = " + initializerValue);
		return initializerValue;
	}
}