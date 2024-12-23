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
package de.tilman_neumann.jml.factor.siqs.powers;

import java.math.BigInteger;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.BinarySearch;
import de.tilman_neumann.jml.base.UnsignedBigInt;
import de.tilman_neumann.jml.factor.siqs.sieve.SieveParams;
import de.tilman_neumann.jml.modular.JacobiSymbol;
import de.tilman_neumann.jml.modular.ModularSqrt31;
import de.tilman_neumann.util.Assert;

/**
 * Algorithm that finds all powers in [pMin, pMax].
 * @author Tilman Neumann
 */
public class AllPowerFinder extends SomePowerFinder {
	private static final Logger LOG = Logger.getLogger(AllPowerFinder.class);
	private static final boolean DEBUG = false;

	private BinarySearch binarySearch = new BinarySearch();
	private ModularSqrt31 modularSqrtEngine = new ModularSqrt31();

	@Override
	public String getName() {
		return "allPowers";
	}

	/**
	 * Find all powers with pMin < power < pMax.
	 * 
	 * @param kN
	 * @param primes
	 * @param tArray
	 * @param primeBaseSize
	 * @param sieveParams
	 * 
	 * @return powers sorted bottom-up by p
	 */
	@Override
	public TreeSet<PowerEntry> findPowers(BigInteger kN, int[] primes, int[] tArray, int primeBaseSize, SieveParams sieveParams) {
		final UnsignedBigInt kN_UBI = new UnsignedBigInt(kN);
		final int pMin = sieveParams.pMin;
		final int pMax = sieveParams.pMax;
		final float lnPMultiplier = sieveParams.lnPMultiplier;
		
		TreeSet<PowerEntry> powerEntries = new TreeSet<PowerEntry>();
		// exclude powers of primes > sqrt(pMax)
		int sqrtPMaxIndex = binarySearch.getInsertPosition(primes, primeBaseSize, (int)Math.sqrt(pMax));
		for (int pIndex=1; pIndex<sqrtPMaxIndex; pIndex++) { // p[0]==2 never has 2 x-solutions
			// [https://members.loria.fr/EThome/old/MPRI/cours_02.pdf, page 11]: Solutions of Q(x) == 0 (mod p^exponent), Q(x) = (a*x+b)^2-kN, exponent>1
			// exist only if Q(x) == 0 (mod p) has two distinct solutions. p must not divide disc(Q).
			if (tArray[pIndex] != 0) {
				int p = primes[pIndex];
				if (DEBUG) {
					// since we only use odd p with x1!=x2, t!=0, p not dividing k, we strictly have Legendre(kN|p) > 0
					int jacobi = new JacobiSymbol().jacobiSymbol(kN, p);
					Assert.assertGreater(jacobi, 0);
				}
				long power_long = p; // long required to avoid int overflow before size check
				for (int exponent=2; ; exponent++) {
					int last_power = (int) power_long;
					power_long *= p;
					if (power_long>pMax) {
						if (DEBUG) LOG.debug("power = " + p + "^" + exponent + " = " + power_long + " > pMax=" + pMax + ", not added");
						break;
					}
					int power = (int) power_long;
					if (DEBUG) {
						// The powers strictly have Jacobi(kN|p) > 0, too
						int jacobi = new JacobiSymbol().jacobiSymbol(kN, (int)power);
						Assert.assertGreater(jacobi, 0);
					}
					if (power>pMin) {
						if (DEBUG) LOG.debug("Add power = " + p + "^" + exponent + " = " + power);
						byte logPower;
						if (last_power > pMin) {
							// reduced contribution
							logPower = (byte) (Math.log(p) * lnPMultiplier + 0.5F);
						} else {
							// full contribution
							logPower = (byte) (Math.log(power) * lnPMultiplier + 0.5F);
						}

						// We need the solution t of t^2 == kN (mod p) to compute the solution u of u^2 == kN (mod p^exponent)
						int u = modularSqrtEngine.modularSqrtModPower(kN_UBI.mod(power), power, last_power, tArray[pIndex]);
						powerEntries.add(new PowerEntry(p, exponent, power, u, logPower));
					} else {
						if (DEBUG) LOG.debug("power = " + p + "^" + exponent + " = " + power + " < pMin=" + pMin + ", not added");
					}
				}
			}
		}
		if (DEBUG) LOG.info("Found " + powerEntries.size() + " powers");
		return powerEntries;
	}
}
