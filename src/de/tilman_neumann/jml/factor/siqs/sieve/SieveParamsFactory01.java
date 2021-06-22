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
 * Version 01 is based on an estimate of the maximal tdiv rest allowed for smooth relations. This has been adjusted by experiments.
 * 
 * @see [Contini]: Scott Patrick Contini, "Factoring Integers with the self-initializing quadraric sieve", Master thesis, University of Georgia, 1997
 * 
 * @author Tilman Neumann
 */
public class SieveParamsFactory01 {
	private static final Logger LOG = Logger.getLogger(SieveParamsFactory01.class);
	private static final boolean DEBUG = false;
	private static final double LN_SQRT_2 = Math.log(Math.sqrt(2)); // ~0.34657
	
	public static SieveParams create(double N_dbl, int NBits, BigInteger kN, int[] primeBase, int primeBaseSize, int sieveArraySize) {
		
		// compute biggest QRest admitted for a smooth relation
		double smoothBoundExponent = (NBits<=150) ? 0.16 : 0.16 + (NBits-150.0)/5250;
		double smoothBound = Math.pow(N_dbl, smoothBoundExponent);
		
		// pMinIndex ~ primeBaseSize^0.33 looks clearly better than primeBaseSize^0.3 or primeBaseSize^0.36.
		// We avoid p[0]==2 which is not used in several sieves.
		int pMinIndex = Math.max(1, (int) Math.cbrt(primeBaseSize));
		int pMin = primeBase[pMinIndex];
		int pMax = primeBase[primeBaseSize-1];
		
		// Compute ln(Q/(da)), the natural logarithm of maximal Q/(da)-values,
		// estimated for d=1 according to the papers of [Contini] (p.7), Pomerance and Silverman as ln(Q/a) = ln(M) + ln(kN)/2 - ln(sqrt(2)).
		// XXX use the correct size for d=2
		double lnQdivDaEstimate = Math.log(sieveArraySize) + Math.log(kN.doubleValue())/2 - LN_SQRT_2;
		
		// Compute the minimal sum of ln(p_i) values required for a Q to pass the sieve.
		double minLnPSum = lnQdivDaEstimate - Math.log(smoothBound);
		
		// convert the sieve bound from natural logarithm to the actual logBase:
		double lnLogBase = minLnPSum / 127; // normalizer to be used as a divisor for p_i values
		double minLogPSum = minLnPSum / lnLogBase;
		
		int tdivTestMinLogPSum = (int) minLogPSum; // floor, is typically 126 or 127
		int logQdivDaEstimate = (int) (lnQdivDaEstimate / lnLogBase);

		if (DEBUG) {
			float logBase = (float) Math.exp(lnLogBase);
			LOG.debug("logBase=" + logBase + ", lnLogBase=" + lnLogBase + ", minLnPSum = " + minLnPSum + ", minLogPSum = " + minLogPSum);
		}
		byte initializer = computeInitializerValue(primeBase, pMinIndex, minLogPSum, lnLogBase);
		float lnPMultiplier = (float) (1.0/lnLogBase); // normalizer to be used as a multiplier for p_i values (faster than a divisor)
		
		return new SieveParams(kN, pMinIndex, pMin, pMax, sieveArraySize, smoothBound, smoothBound, smoothBound, logQdivDaEstimate, tdivTestMinLogPSum, initializer, lnPMultiplier);
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
