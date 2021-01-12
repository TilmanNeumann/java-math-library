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

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.gcd.Gcd63;
import de.tilman_neumann.jml.quadraticResidues.QuadraticResiduesMod2PowN;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.jml.factor.TestsetGenerator;
import de.tilman_neumann.jml.factor.TestNumberNature;

/**
 * An attempt to find a way to factorizations by considering a^2 - 4kN (mod m) for m=2, 4, 8, 16, 32, 64,...
 * @author Tilman Neumann
 */
public class Lehman_AnalyzeModPowersOf2 {
	private static final Logger LOG = Logger.getLogger(Lehman_AnalyzeModPowersOf2.class);

	private static final int KNMOD = 64; // XXX 64 is chosen arbitrarily, we just need some good adjustment there
	
	private final Gcd63 gcdEngine = new Gcd63();

	public long findSingleFactor(long N) {
		LOG.info("factor N = " + N);
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
							LOG.info("  Found factor " + gcd + " from k=" + k + ", a=" + a);
							int n = 0;
							long mod = 1;
							// anything mod 1 is 0
							long aMod = 0, kMod = 0;
							while (true) {
								long lastMod = mod;
								
								n++;
								mod <<= 1;
								if (mod > N) {
									break;
								}
								
								// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
								long lastkMod = kMod;
								long lastaMod = aMod;
								kMod = k % mod;
								if (kMod < 0) kMod += mod;
								aMod = a % mod;
								if (aMod < 0) aMod += mod;
								// The kMod, aMod of k, a that give a factor are 
								// kMod=lastkMod or kMod=lastkMod+lastMod, and
								// aMod=lastaMod or aMod=lastaMod+lastMod !
								assertTrue(kMod == lastkMod || kMod == lastkMod+lastMod);
								assertTrue(aMod == lastaMod || aMod == lastaMod+lastMod);
								// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
								
								LOG.info("    MOD = 2^" + n + " = " + mod + ": k%" + mod + " = " + kMod + ", a%" + mod + " = " + aMod);

								long testMod = (a*a - fourKN) % mod;
								if (testMod < 0) testMod += mod;
								LOG.info("      (a*a - 4kN) % " + mod + " = " + testMod); // always a quadratic residue
								
								long fourKModNMod = 4*kMod*N % mod;
								long testMod2 = (aMod*aMod % mod) - (fourKModNMod % mod);
								if (testMod2 < 0) testMod2 += mod;
								//LOG.info("      ((a % " + mod + ")^2 % " + mod + ") - (4*(k % " + mod + ")*N % " + mod + ") = " + testMod2); // always a quadratic residue
								// For k, a that give a factor we have a^2 - 4kN (mod mod) == (a mod mod)^2 (mod mod) - 4*(k mod mod)*N (mod mod)
								// -> we only have to search up to a%mod, k%mod !
								assertEquals(testMod, testMod2);
								
								boolean isQuadraticResidue = QuadraticResiduesMod2PowN.isQuadraticResidueMod2PowN(BigInteger.valueOf(testMod2), n);
								//LOG.info("      isQuadraticResidue % " + mod + " = " + isQuadraticResidue);
								assertTrue(isQuadraticResidue);
								//LOG.info("      quadratic residues % " + mod + " = " + QuadraticResidues.getQuadraticResidues(mod));
								
								// check the four next possibilities
								test(N, n+1, mod<<1, kMod, aMod);
								test(N, n+1, mod<<1, kMod+mod, aMod);
								test(N, n+1, mod<<1, kMod, aMod+mod);
								test(N, n+1, mod<<1, kMod+mod, aMod+mod);
								// TODO Hmmm... All possibilities are always correct !?
								
								// TODO What if we do several of such steps ??
								// e.g. test(N, n+2, mod<<2, kMod+mod, aMod+mod+2*mod);
							}
							return gcd; // removes the blur at even k ?
						}
					}
				}
			}
	    }
		
		return 0; // factoring failed
	}
	
	private void test(long N, int n, long mod, long kMod, long aMod) {
		long test = (aMod*aMod - 4*kMod*N) % mod;
		if (test < 0) test += mod;
		boolean isQuadraticResidue = QuadraticResiduesMod2PowN.isQuadraticResidueMod2PowN(BigInteger.valueOf(test), n);
		LOG.info("      next: k%" + mod + " = " + kMod + ", a%" + mod + " = " + aMod + " -> test % " + mod + " = " + test + " is quadratic residue = " + isQuadraticResidue);
	}

	public static void main(String[] args) {
    	ConfigUtil.initProject();
    	
    	Lehman_AnalyzeModPowersOf2 analyzer = new Lehman_AnalyzeModPowersOf2();
		int bits = 30;
		BigInteger[] testNumbers = TestsetGenerator.generate(2000, bits, TestNumberNature.MODERATE_SEMIPRIMES);
		
		for (BigInteger N : testNumbers) {
			analyzer.findSingleFactor(N.longValue());
		}
	}
}
