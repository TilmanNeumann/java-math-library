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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.log4j.Logger;

/**
 * Factory to compute some basic parameters for the sieve.
 * Version 02 differentiates between the maximum bounds for sieve hits, smooth candidates, and smooths relations.
 * Version 03 increases the tdivTestExponent to find partials with 3LP.
 * 
 * @see [Contini]: Scott Patrick Contini, "Factoring Integers with the self-initializing quadraric sieve", Master thesis, University of Georgia, 1997
 * 
 * @author Tilman Neumann
 */
// XXX experimental, still needs adjustment
public class SieveParamsFactory03 {
	private static final Logger LOG = Logger.getLogger(SieveParamsFactory03.class);
	private static final boolean DEBUG = false;
	
	/** A small degree of freedom. It hardly matters which small value we choose, e.g. 0, 1 or 8 seem to perform very similar */
	private static final int FREE_UNITS = 1;
	
	public static SieveParams create(double N_dbl, int NBits, BigInteger kN, int d, int[] primeBase, int primeBaseSize, int sieveArraySize, int qCount, double best_q) {
		
		// Compute biggest QRest admitted for a smooth relation.
		// The triples (sieveHitExp, tdivTestExp, smoothBoundExponent) are important tuning parameters.
		double progessivePart = (NBits<=150) ? 0 : (NBits-150.0)*0.045/150;
		double sieveHitExponent = 0.21 + progessivePart;
		double tdivTestExponent = (NBits<250) ? 0.12 : (NBits<300) ? 0.16 : 0.20;
		// at 250 bit 2LP kick in
		// with tdivTestExponent=0.2 we get the first 3.partials at 300 bit // XXX adjust
		double smoothBoundExponent = tdivTestExponent;
		
		double sieveHitBound = Math.pow(N_dbl, sieveHitExponent);
		double tdivTestBound = Math.pow(N_dbl, tdivTestExponent);
		double smoothBound = Math.pow(N_dbl, smoothBoundExponent);
		if (DEBUG) {
			LOG.debug("sieveHitBound = " + sieveHitBound + ", tdivTestBound = " + tdivTestBound + ", smoothBound = " + smoothBound);
			int sieveHitBits = BigDecimal.valueOf(sieveHitBound).toBigInteger().bitLength();
			int tdivTestBits = BigDecimal.valueOf(tdivTestBound).toBigInteger().bitLength();
			int smoothBoundBits = BigDecimal.valueOf(smoothBound).toBigInteger().bitLength();
			LOG.debug("sieveHitBits = " + sieveHitBits + ", tdivTestBits = " + tdivTestBits + ", smoothBoundBits = " + smoothBoundBits);
		}
		
		// pMinIndex/pMin is an important tuning parameter. After the correction of sieve hit scores introduced in Sieve03hU, the exponent could be raised from 0.33 to 0.43.
		// We avoid p[0]==2 which is not used in several sieves.
		int pMinIndex = Math.max(1, (int) Math.pow(primeBaseSize, 0.47));
		int pMin = primeBase[pMinIndex];
		int pMax = primeBase[primeBaseSize-1];
		
		// Compute ln(Q/(da)), the natural logarithm of (theoretically) maximal Q/(da)-values:
		// ln(Q/a)    = ln(M) + ln(kN)/2 - ln(sqrt(2)) if d=1, as proposed by [Contini] (p.7), Pomerance and Silverman,
		// ln(Q/(2a)) = ln(M) + ln(kN)/2 - ln(sqrt(8)) if d=2.
		double lnQdivDaEstimate = Math.log(sieveArraySize * Math.sqrt(kN.doubleValue()/2) / d);
		
		// Compute the minimal sum of ln(p_i) values required for a Q to pass the sieve.
		double minLnPSum = lnQdivDaEstimate - Math.log(sieveHitBound);
		
		// convert the sieve bound from natural logarithm to the actual logBase:
		float lnPMultiplier = (float) ((128.0-FREE_UNITS)/minLnPSum); // multiplier to convert natural logs to scaled logs (where we get a sieve hit if scaled log >= 128)
		
		double tdivTestMinLnPSum = lnQdivDaEstimate - Math.log(tdivTestBound);
		int tdivTestMinLogPSum = (int) (tdivTestMinLnPSum * lnPMultiplier);
		int logQdivDaEstimate = (int) (lnQdivDaEstimate * lnPMultiplier);

		if (DEBUG) {
			double lnLogBase = 1.0 / lnPMultiplier; // normalizer to be used as a divisor for p_i values
			float logBase = (float) Math.exp(lnLogBase);
			LOG.debug("logBase=" + logBase + ", lnLogBase=" + lnLogBase + ", minLnPSum = " + minLnPSum + ", minLogPSum = 127");
		}
		byte initializer = computeInitializerValue(primeBase, pMinIndex, lnPMultiplier, qCount, best_q);
		
		return new SieveParams(kN, pMinIndex, pMin, pMax, sieveArraySize, sieveHitBound, tdivTestBound, smoothBound, logQdivDaEstimate, tdivTestMinLogPSum, initializer, lnPMultiplier);
	}
	
	/**
	 * Compute the initializer value.
	 * @param primesArray prime base
	 * @param pMinIndex the index of the first prime used for sieving
	 * @param lnPMultiplier multiplier to convert natural log to scaled log
	 * @param qCount the number of q-parameters whose product gives the a-parameter
	 * @param best_q the estimated optimal size of q-parameters
	 * @return initializer byte value
	 */
	private static byte computeInitializerValue(int[] primesArray, int pMinIndex, double lnPMultiplier, int qCount, double best_q) {
		// compute contribution of small primes in nats
		double unsievedLnPSum = 0;
		for (int i=pMinIndex-1; i>=0; i--) {
			int p = primesArray[i];
			unsievedLnPSum += Math.log(p) / p;
		}
		// also add the contribution of the q-params (they are not sieved with either)
		unsievedLnPSum += qCount * Math.log(best_q) / best_q;
		
		// convert value from base e to wanted log base
		double unsievedLogPSum = unsievedLnPSum * lnPMultiplier;
		// compute initializerValue, rounded
		byte initializerValue = (byte) (unsievedLogPSum + FREE_UNITS + 0.5);
		if (DEBUG) LOG.debug("initializerValue = " + initializerValue);
		return initializerValue;
	}
}