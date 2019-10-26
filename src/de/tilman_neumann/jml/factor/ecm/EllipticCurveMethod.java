/*
 * Elliptic Curve Method (ECM) Prime Factorization
 *
 * Written by Dario Alejandro Alpern (Buenos Aires - Argentina)
 * Based in Yuji Kida's implementation for UBASIC interpreter.
 * Some code "borrowed" from Paul Zimmermann's ECM4C.
 * Modified for the Symja project by Axel Kramer.
 * Further refactorings by Tilman Neumann.
 * 
 * Big thanks to Dario Alpern for his permission to use this piece of software under the GPL3 license.
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
package de.tilman_neumann.jml.factor.ecm;

import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.jml.factor.tdiv.TDiv;
import de.tilman_neumann.jml.powers.PurePowerTest;
import de.tilman_neumann.jml.primes.exact.AutoExpandingPrimesArray;
import de.tilman_neumann.jml.primes.probable.BPSWTest;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;
import de.tilman_neumann.util.SortedMultiset_BottomUp;

import static de.tilman_neumann.jml.base.BigIntConstants.I_1;
import static org.junit.Assert.assertFalse;

/**
 * <p>Use Elliptic Curve Method to find the prime number factors of a given BigInteger.</p>
 *
 * <p>
 * See <a href="https://en.wikipedia.org/wiki/Lenstra_elliptic_curve_factorization"> Wikipedia: Lenstra elliptic curve
 * factorization </a>
 * </p>
 */
public class EllipticCurveMethod extends FactorAlgorithm {
	private static final Logger LOG = Logger.getLogger(EllipticCurveMethod.class);

	private static final int NLen = 1200;
	private static final long DosALa32 = (long) 1 << 32;
	private static final long DosALa31 = (long) 1 << 31;
	private static final double dDosALa31 = DosALa31;
	private static final double dDosALa62 = dDosALa31 * dDosALa31;
	private static final long Mi = 1000000000;
	private static final int ADD = 6; // number of multiplications in an addition
	private static final int DUP = 5; //number of multiplications in a duplicate

	/** 1 as "BigNbr" */
	private static final long[] BigNbr1 = new long[NLen];

	/** maximum number of elliptic curves tested for 30, 35, ..., 85, 90 digits */
	private static final int[] limits = { 5, 8, 15, 25, 27, 32, 43, 70, 150, 300, 350, 600, 1500 };
	
	/** Primes < 5000 */
	private static final int[] SmallPrime = new int[670]; // p_669 = 4999;

	private static final BPSWTest bpsw = new BPSWTest();
	private static final PurePowerTest powerTest = new PurePowerTest();
	private static final TDiv tdiv = new TDiv();

	private static final double[] v =
		{ 1.61803398875, 1.72360679775, 1.618347119656, 1.617914406529, 1.612429949509,
		  1.632839806089, 1.620181980807, 1.580178728295, 1.617214616534, 1.38196601125 };

	private final long[] biTmp = new long[NLen];

	// Used inside GCD calculations in multiple precision numbers
	private final long[] CalcAuxGcdU = new long[NLen];
	private final long[] CalcAuxGcdV = new long[NLen];
	private final long[] CalcAuxGcdT = new long[NLen];
	private final long[] TestNbr = new long[NLen];
	private final long[] GcdAccumulated = new long[NLen];
	private final long[] MontgomeryMultR1 = new long[NLen];
	private final long[] MontgomeryMultR2 = new long[NLen];
	private final long[] MontgomeryMultAfterInv = new long[NLen];

	/** Length of multiple precision numbers. */
	private int NumberLength;

	/** Elliptic Curve number */
	private int EC;
	
	private long[] fieldAA, fieldTX, fieldTZ, fieldUX, fieldUZ;
	private long[] fieldAux1, fieldAux2, fieldAux3, fieldAux4;
	private double dN;
	private long MontgomeryMultN;

	static {
		BigNbr1[0] = 1;
		for (int i = 1; i < NLen; i++) {
			BigNbr1[i] = 0;
		}
		
		final AutoExpandingPrimesArray autoPrimes = AutoExpandingPrimesArray.get().ensureLimit(5000);
		SmallPrime[0] = 2;
		for (int indexM = 1; indexM < SmallPrime.length; indexM++) {
			SmallPrime[indexM] = autoPrimes.getPrime(indexM);
		}
	}

	@Override
	public String getName() {
		return "ECM";
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Note that the curve limit of this method for finding a single factor is the same as that of factor()
	 * and factorize() for finding all prime factors of N.
	 */
	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		EC = 1;
		return fnECM(N);
	}

	@Override
	public SortedMultiset<BigInteger> factor(BigInteger N) {
		SortedMultiset<BigInteger> allFactors = new SortedMultiset_BottomUp<>();
		SortedMultiset<BigInteger> compositeFactors = factorize(N, allFactors);
		allFactors.addAll(compositeFactors);
		return allFactors;
	}
	
	/**
	 * Find small factors of some N. Returns found factors in <code>primeFactors</code> and eventually some
	 * unfactored composites as return value.
	 * 
	 * @param N the number to factor
	 * @param primeFactors the found prime factors.
	 * @return unfactored composites left after stopping ECM, empty map if N has been factored completely
	 */
	public SortedMultiset<BigInteger> factorize(BigInteger N, SortedMap<BigInteger, Integer> primeFactors) {
		// set up new N
		EC = 1;
		
		// Do trial division by all primes < 131072.
		SortedMultiset<BigInteger> unresolvedComposites = new SortedMultiset_BottomUp<>();
		N = tdiv.findSmallFactors(N, 131072, primeFactors); // TODO do outside ECM?
		if (N.equals(I_1)) {
			return unresolvedComposites;
		}
		
		// There are factors greater than 131071, and they may be prime or composite.
		if (isProbablePrime(N)) {
			addToMap(N, 1, primeFactors);
			return unresolvedComposites;
		}
		
		// N is composite -> do ECM
		TreeMap<BigInteger, Integer> compositesToTest = new TreeMap<>();
		compositesToTest.put(N, 1);
		while (!compositesToTest.isEmpty()) {
			// get next composite to test
			Entry<BigInteger, Integer> compositeEntry = compositesToTest.pollLastEntry();
			N = compositeEntry.getKey();
			int exp = compositeEntry.getValue();
			
			// pure power?
			PurePowerTest.Result r = powerTest.test(N);
			if (r != null) {
				// N is a pure power!
				addToMapDependingOnPrimeTest(r.base, exp*r.exponent, primeFactors, compositesToTest);
				continue; // test next composite
			}

			// ECM
			final BigInteger NN = fnECM(N);
			if (NN.equals(I_1)) {
				// N is composite but could not be resolved
				addToMap(N, exp, unresolvedComposites);
				continue;
			}
			// NN is a factor of N
			addToMapDependingOnPrimeTest(NN, exp, primeFactors, compositesToTest);
			addToMapDependingOnPrimeTest(N.divide(NN), exp, primeFactors, compositesToTest);
		}
		return unresolvedComposites;
	}

	private boolean isProbablePrime(BigInteger N) {
		// XXX The 33-bit "guard" is only safe if we did tdiv for all p <= sqrt(2^33) before
		return N.bitLength() <= 33 || bpsw.isProbablePrime(N);
	}

	private void addToMapDependingOnPrimeTest(BigInteger factor, int exp, SortedMap<BigInteger, Integer> primeFactors, SortedMap<BigInteger, Integer> compositeFactors) {
		addToMap(factor, exp, isProbablePrime(factor) ? primeFactors : compositeFactors);
	}
	
	private void addToMap(BigInteger N, int exp, SortedMap<BigInteger, Integer> map) {
		Integer oldExp = map.get(N);
		// old entry is replaced if oldExp!=null
		map.put(N, (oldExp == null) ? exp : oldExp+exp);
	}
	
	private BigInteger fnECM(BigInteger N) {
		int I, J, Pass, Qaux;
		long L1, L2, LS, P, IP;
		long[] A0 = new long[NLen];
		long[] A02 = new long[NLen];
		long[] A03 = new long[NLen];
		long[] AA = new long[NLen];
		long[] DX = new long[NLen]; // zero-init required
		long[] DZ = new long[NLen]; // zero-init required
		long[] GD = new long[NLen]; // zero-init required
		long[] M = new long[NLen]; // zero-init required
		long[] TX = new long[NLen];
		fieldTX = TX;
		long[] TZ = new long[NLen];
		fieldTZ = TZ;
		long[] UX = new long[NLen];
		fieldUX = UX;
		long[] UZ = new long[NLen];
		fieldUZ = UZ;
		long[] W1 = new long[NLen];
		long[] W2 = new long[NLen];
		long[] W3 = new long[NLen]; // zero-init required
		long[] W4 = new long[NLen]; // zero-init required
		long[] WX = new long[NLen];
		long[] WZ = new long[NLen];
		long[] X = new long[NLen];
		long[] Z = new long[NLen];
		long[] Aux1 = new long[NLen];
		fieldAux1 = Aux1;
		long[] Aux2 = new long[NLen];
		fieldAux2 = Aux2;
		long[] Aux3 = new long[NLen];
		fieldAux3 = Aux3;
		long[] Aux4 = new long[NLen];
		fieldAux4 = Aux4;
		long[] Xaux = new long[NLen];
		long[] Zaux = new long[NLen];
		long[][] root = new long[480][NLen];
		byte[] sieve = new byte[23100];
		byte[] sieve2310 = new byte[2310];
		int[] sieveidx = new int[480];
		int i, j, u;

		fieldAA = AA;
		BigNbrToBigInt(N); // converts BigInteger N into TestNbr and NumberLength
		GetMontgomeryParms();
		
		// it seems to be faster not to repeat previously tested curves for new factors
		EC--;

		do {
			new_curve: do {
				EC++;
				int digitsOfN = N.toString().length(); // Get number of digits.
				if (digitsOfN > 30 && digitsOfN <= 90) { // If between 30 and 90 digits...
					int limit = limits[((int) digitsOfN - 31) / 5];
					if (EC >= limit) {
						EC += 1;
						return I_1; // stop ECM
					}
				}
				L1 = 2000;
				L2 = 200000;
				LS = 45;
				/* Number of primes less than 2000 */
				if (EC > 25) {
					if (EC < 326) {
						L1 = 50000;
						L2 = 5000000;
						LS = 224;
						/* Number of primes less than 50000 */
					} else if (EC < 2000) {
						L1 = 1000000;
						L2 = 100000000;
						LS = 1001;
						/* Number of primes less than 1000000 */
					} else {
						L1 = 11000000;
						L2 = 1100000000;
						LS = 3316;
						/* Number of primes less than 11000000 */
					}
				}

				// System.out.println(primalityString + EC + "\n" + UpperLine + "\n" + LowerLine);

				LongToBigNbr(2 * (EC + 1), Aux1);
				LongToBigNbr(3 * (EC + 1) * (EC + 1) - 1, Aux2);
				ModInvBigNbr(Aux2, Aux2, TestNbr);
				MultBigNbrModN(Aux1, Aux2, Aux3);
				MultBigNbrModN(Aux3, MontgomeryMultR1, A0);
				MontgomeryMult(A0, A0, A02);
				MontgomeryMult(A02, A0, A03);
				SubtractBigNbrModN(A03, A0, Aux1);
				MultBigNbrByLongModN(A02, 9, Aux2);
				SubtractBigNbrModN(Aux2, MontgomeryMultR1, Aux2);
				MontgomeryMult(Aux1, Aux2, Aux3);
				if (BigNbrIsZero(Aux3)) {
					continue;
				}
				MultBigNbrByLongModN(A0, 4, Z);
				MultBigNbrByLongModN(A02, 6, Aux1);
				SubtractBigNbrModN(MontgomeryMultR1, Aux1, Aux1);
				MontgomeryMult(A02, A02, Aux2);
				MultBigNbrByLongModN(Aux2, 3, Aux2);
				SubtractBigNbrModN(Aux1, Aux2, Aux1);
				MultBigNbrByLongModN(A03, 4, Aux2);
				ModInvBigNbr(Aux2, Aux2, TestNbr);
				MontgomeryMult(Aux2, MontgomeryMultAfterInv, Aux3);
				MontgomeryMult(Aux1, Aux3, A0);
				AddBigNbrModN(A0, MontgomeryMultR2, Aux1);
				LongToBigNbr(4, Aux2);
				ModInvBigNbr(Aux2, Aux3, TestNbr);
				MultBigNbrModN(Aux3, MontgomeryMultR1, Aux2);
				MontgomeryMult(Aux1, Aux2, AA);
				MultBigNbrByLongModN(A02, 3, Aux1);
				AddBigNbrModN(Aux1, MontgomeryMultR1, X);
				
				/**************/
				/* First step */
				/**************/
				System.arraycopy(X, 0, Xaux, 0, NumberLength);
				System.arraycopy(Z, 0, Zaux, 0, NumberLength);
				System.arraycopy(MontgomeryMultR1, 0, GcdAccumulated, 0, NumberLength);
				for (Pass = 0; Pass < 2; Pass++) {
					/* For powers of 2 */
					for (I = 1; I <= L1; I <<= 1) {
						duplicate(X, Z, X, Z);
					}
					for (I = 3; I <= L1; I *= 3) {
						duplicate(W1, W2, X, Z);
						add3(X, Z, X, Z, W1, W2, X, Z);
					}

					if (Pass == 0) {
						MontgomeryMult(GcdAccumulated, Z, Aux1);
						System.arraycopy(Aux1, 0, GcdAccumulated, 0, NumberLength);
					} else {
						GcdBigNbr(Z, TestNbr, GD);
						if (BigNbrAreEqual(GD, BigNbr1) == false) {
							break new_curve; // found factor, exit
						}
					}

					/* for powers of odd primes */
					int indexM = 1;
					do {
						P = SmallPrime[indexM++];
						for (IP = P; IP <= L1; IP *= P) {
							prac((int) P, X, Z, W1, W2, W3, W4);
						}
						if (Pass == 0) {
							MontgomeryMult(GcdAccumulated, Z, Aux1);
							System.arraycopy(Aux1, 0, GcdAccumulated, 0, NumberLength);
						} else {
							GcdBigNbr(Z, TestNbr, GD);
							if (BigNbrAreEqual(GD, BigNbr1) == false) {
								break new_curve; // found factor, exit
							}
						}
					} while (P <= LS);
					P += 2;

					/* Initialize sieve2310[n]: 1 if gcd(P+2n,2310) > 1, 0 otherwise */
					u = (int) P;
					for (i = 0; i < 2310; i++) {
						sieve2310[i] = (u % 3 == 0 || u % 5 == 0 || u % 7 == 0 || u % 11 == 0 ? (byte) 1 : (byte) 0);
						u += 2;
					}
					do {
						/* Generate sieve */
						GenerateSieve((int) P, sieve, sieve2310, SmallPrime);

						/* Walk through sieve */
						for (i = 0; i < 23100; i++) {
							if (sieve[i] != 0)
								continue; /* Do not process composites */
							if (P + 2 * i > L1)
								break;
							prac((int) (P + 2 * i), X, Z, W1, W2, W3, W4);
							if (Pass == 0) {
								MontgomeryMult(GcdAccumulated, Z, Aux1);
								System.arraycopy(Aux1, 0, GcdAccumulated, 0, NumberLength);
							} else {
								GcdBigNbr(Z, TestNbr, GD);
								if (BigNbrAreEqual(GD, BigNbr1) == false) {
									break new_curve; // found factor, exit
								}
							}
						}
						P += 46200;
					} while (P < L1);
					if (Pass == 0) {
						if (BigNbrIsZero(GcdAccumulated)) { // If GcdAccumulated is...
							System.arraycopy(Xaux, 0, X, 0, NumberLength);
							System.arraycopy(Zaux, 0, Z, 0, NumberLength);
							continue; // ... a multiple of TestNbr, continue.
						}
						GcdBigNbr(GcdAccumulated, TestNbr, GD);
						if (BigNbrAreEqual(GD, BigNbr1) == false) {
							break new_curve; // found factor, exit
						}
						break;
					}
				} /* end for Pass */

				/******************************************************/
				/* Second step (using improved standard continuation) */
				/******************************************************/
				j = 0;
				for (u = 1; u < 2310; u += 2) {
					if (u % 3 == 0 || u % 5 == 0 || u % 7 == 0 || u % 11 == 0) {
						sieve2310[u / 2] = (byte) 1;
					} else {
						sieve2310[(sieveidx[j++] = u / 2)] = (byte) 0;
					}
				}
				System.arraycopy(sieve2310, 0, sieve2310, 1155, 1155);
				System.arraycopy(X, 0, Xaux, 0, NumberLength);
				System.arraycopy(Z, 0, Zaux, 0, NumberLength); // (X:Z) -> Q (output from step 1)
				
				for (Pass = 0; Pass < 2; Pass++) {
					System.arraycopy(MontgomeryMultR1, 0, GcdAccumulated, 0, NumberLength);
					System.arraycopy(X, 0, UX, 0, NumberLength);
					System.arraycopy(Z, 0, UZ, 0, NumberLength); // (UX:UZ) -> Q
					ModInvBigNbr(Z, Aux2, TestNbr);
					MontgomeryMult(Aux2, MontgomeryMultAfterInv, Aux1);
					MontgomeryMult(Aux1, X, root[0]); // root[0] <- X/Z (Q)
					J = 0;
					AddBigNbrModN(X, Z, Aux1);
					MontgomeryMult(Aux1, Aux1, W1);
					SubtractBigNbrModN(X, Z, Aux1);
					MontgomeryMult(Aux1, Aux1, W2);
					MontgomeryMult(W1, W2, TX);
					SubtractBigNbrModN(W1, W2, Aux1);
					MontgomeryMult(Aux1, AA, Aux2);
					AddBigNbrModN(Aux2, W2, Aux3);
					MontgomeryMult(Aux1, Aux3, TZ); // (TX:TZ) -> 2Q
					SubtractBigNbrModN(X, Z, Aux1);
					AddBigNbrModN(TX, TZ, Aux2);
					MontgomeryMult(Aux1, Aux2, W1);
					AddBigNbrModN(X, Z, Aux1);
					SubtractBigNbrModN(TX, TZ, Aux2);
					MontgomeryMult(Aux1, Aux2, W2);
					AddBigNbrModN(W1, W2, Aux1);
					MontgomeryMult(Aux1, Aux1, Aux2);
					MontgomeryMult(Aux2, UZ, X);
					SubtractBigNbrModN(W1, W2, Aux1);
					MontgomeryMult(Aux1, Aux1, Aux2);
					MontgomeryMult(Aux2, UX, Z); // (X:Z) -> 3Q
					for (I = 5; I < 2310; I += 2) {
						System.arraycopy(X, 0, WX, 0, NumberLength);
						System.arraycopy(Z, 0, WZ, 0, NumberLength);
						SubtractBigNbrModN(X, Z, Aux1);
						AddBigNbrModN(TX, TZ, Aux2);
						MontgomeryMult(Aux1, Aux2, W1);
						AddBigNbrModN(X, Z, Aux1);
						SubtractBigNbrModN(TX, TZ, Aux2);
						MontgomeryMult(Aux1, Aux2, W2);
						AddBigNbrModN(W1, W2, Aux1);
						MontgomeryMult(Aux1, Aux1, Aux2);
						MontgomeryMult(Aux2, UZ, X);
						SubtractBigNbrModN(W1, W2, Aux1);
						MontgomeryMult(Aux1, Aux1, Aux2);
						MontgomeryMult(Aux2, UX, Z); // (X:Z) -> 5Q, 7Q, ...
						if (Pass == 0) {
							MontgomeryMult(GcdAccumulated, Aux1, Aux2);
							System.arraycopy(Aux2, 0, GcdAccumulated, 0, NumberLength);
						} else {
							GcdBigNbr(Aux1, TestNbr, GD);
							if (BigNbrAreEqual(GD, BigNbr1) == false) {
								break new_curve; // found factor, exit
							}
						}
						if (I == 1155) {
							System.arraycopy(X, 0, DX, 0, NumberLength);
							System.arraycopy(Z, 0, DZ, 0, NumberLength); // (DX:DZ) -> 1155Q
						}
						if (I % 3 != 0 && I % 5 != 0 && I % 7 != 0 && I % 11 != 0) {
							J++;
							ModInvBigNbr(Z, Aux2, TestNbr);
							MontgomeryMult(Aux2, MontgomeryMultAfterInv, Aux1);
							MontgomeryMult(Aux1, X, root[J]); // root[J] <- X/Z
						}
						System.arraycopy(WX, 0, UX, 0, NumberLength);
						System.arraycopy(WZ, 0, UZ, 0, NumberLength); // (UX:UZ) <- Previous (X:Z)
					} /* end for I */
					AddBigNbrModN(DX, DZ, Aux1);
					MontgomeryMult(Aux1, Aux1, W1);
					SubtractBigNbrModN(DX, DZ, Aux1);
					MontgomeryMult(Aux1, Aux1, W2);
					MontgomeryMult(W1, W2, X);
					SubtractBigNbrModN(W1, W2, Aux1);
					MontgomeryMult(Aux1, AA, Aux2);
					AddBigNbrModN(Aux2, W2, Aux3);
					MontgomeryMult(Aux1, Aux3, Z);
					System.arraycopy(X, 0, UX, 0, NumberLength);
					System.arraycopy(Z, 0, UZ, 0, NumberLength); // (UX:UZ) -> 2310Q
					AddBigNbrModN(X, Z, Aux1);
					MontgomeryMult(Aux1, Aux1, W1);
					SubtractBigNbrModN(X, Z, Aux1);
					MontgomeryMult(Aux1, Aux1, W2);
					MontgomeryMult(W1, W2, TX);
					SubtractBigNbrModN(W1, W2, Aux1);
					MontgomeryMult(Aux1, AA, Aux2);
					AddBigNbrModN(Aux2, W2, Aux3);
					MontgomeryMult(Aux1, Aux3, TZ); // (TX:TZ) -> 2*2310Q
					SubtractBigNbrModN(X, Z, Aux1);
					AddBigNbrModN(TX, TZ, Aux2);
					MontgomeryMult(Aux1, Aux2, W1);
					AddBigNbrModN(X, Z, Aux1);
					SubtractBigNbrModN(TX, TZ, Aux2);
					MontgomeryMult(Aux1, Aux2, W2);
					AddBigNbrModN(W1, W2, Aux1);
					MontgomeryMult(Aux1, Aux1, Aux2);
					MontgomeryMult(Aux2, UZ, X);
					SubtractBigNbrModN(W1, W2, Aux1);
					MontgomeryMult(Aux1, Aux1, Aux2);
					MontgomeryMult(Aux2, UX, Z); // (X:Z) -> 3*2310Q
					Qaux = (int) (L1 / 4620);
					int maxIndexM = (int) (L2 / 4620);
					for (int indexM = 0; indexM <= maxIndexM; indexM++) {
						if (indexM >= Qaux) { // If inside step 2 range...
							if (indexM == 0) {
								ModInvBigNbr(UZ, Aux2, TestNbr);
								MontgomeryMult(Aux2, MontgomeryMultAfterInv, Aux3);
								MontgomeryMult(UX, Aux3, Aux1); // Aux1 <- X/Z (2310Q)
							} else {
								ModInvBigNbr(Z, Aux2, TestNbr);
								MontgomeryMult(Aux2, MontgomeryMultAfterInv, Aux3);
								MontgomeryMult(X, Aux3, Aux1); // Aux1 <- X/Z (3,5,* 2310Q)
							}

							/* Generate sieve */
							if (indexM % 10 == 0 || indexM == Qaux) {
								GenerateSieve(indexM / 10 * 46200 + 1, sieve, sieve2310, SmallPrime);
							}
							/* Walk through sieve */
							J = 1155 + (indexM % 10) * 2310;
							for (i = 0; i < 480; i++) {
								j = sieveidx[i]; // 0 < J < 1155
								if (sieve[J + j] != 0 && sieve[J - 1 - j] != 0) {
									continue; // Do not process if both are composite numbers.
								}
								SubtractBigNbrModN(Aux1, root[i], M);
								MontgomeryMult(GcdAccumulated, M, Aux2);
								System.arraycopy(Aux2, 0, GcdAccumulated, 0, NumberLength);
							}
							if (Pass != 0) {
								GcdBigNbr(GcdAccumulated, TestNbr, GD);
								if (BigNbrAreEqual(GD, BigNbr1) == false) {
									break new_curve; // found factor, exit
								}
							}
						}
						if (indexM != 0) { // Update (X:Z)
							System.arraycopy(X, 0, WX, 0, NumberLength);
							System.arraycopy(Z, 0, WZ, 0, NumberLength);
							SubtractBigNbrModN(X, Z, Aux1);
							AddBigNbrModN(TX, TZ, Aux2);
							MontgomeryMult(Aux1, Aux2, W1);
							AddBigNbrModN(X, Z, Aux1);
							SubtractBigNbrModN(TX, TZ, Aux2);
							MontgomeryMult(Aux1, Aux2, W2);
							AddBigNbrModN(W1, W2, Aux1);
							MontgomeryMult(Aux1, Aux1, Aux2);
							MontgomeryMult(Aux2, UZ, X);
							SubtractBigNbrModN(W1, W2, Aux1);
							MontgomeryMult(Aux1, Aux1, Aux2);
							MontgomeryMult(Aux2, UX, Z);
							System.arraycopy(WX, 0, UX, 0, NumberLength);
							System.arraycopy(WZ, 0, UZ, 0, NumberLength);
						}
					} // end for Q
					if (Pass == 0) {
						if (BigNbrIsZero(GcdAccumulated)) { // If GcdAccumulated is...
							System.arraycopy(Xaux, 0, X, 0, NumberLength);
							System.arraycopy(Zaux, 0, Z, 0, NumberLength);
							continue; // ... a multiple of TestNbr, continue.
						}
						GcdBigNbr(GcdAccumulated, TestNbr, GD);
						if (BigNbrAreEqual(GD, TestNbr) == true) {
							break;
						}
						if (BigNbrAreEqual(GD, BigNbr1) == false) {
							break new_curve; // found factor, exit
						}
						break;
					}
				} /* end for Pass */
			} while (true); /* end curve calculation */
			assertFalse(BigNbrAreEqual(GD, TestNbr));
		} while (BigNbrAreEqual(GD, TestNbr) == true);
		// System.out.println("");
		// StepECM = 0; /* do not show pass number on screen */
		return BigIntToBigNbr(GD);
	}

	private void GetMontgomeryParms() {
		int N, x, j;

		dN = TestNbr[NumberLength - 1];
		if (NumberLength > 1) {
			dN += TestNbr[NumberLength - 2] / dDosALa31;
		}
		if (NumberLength > 2) {
			dN += TestNbr[NumberLength - 3] / dDosALa62;
		}

		x = N = (int) TestNbr[0]; // 2 least significant bits of inverse correct.
		x = x * (2 - N * x); // 4 least significant bits of inverse correct.
		x = x * (2 - N * x); // 8 least significant bits of inverse correct.
		x = x * (2 - N * x); // 16 least significant bits of inverse correct.
		x = x * (2 - N * x); // 32 least significant bits of inverse correct.
		MontgomeryMultN = (-x) & 0x7FFFFFFF;
		j = NumberLength;
		MontgomeryMultR1[j] = 1;
		do {
			MontgomeryMultR1[--j] = 0;
		} while (j > 0);
		AdjustModN(MontgomeryMultR1);
		MultBigNbrModN(MontgomeryMultR1, MontgomeryMultR1, MontgomeryMultR2);
		MontgomeryMult(MontgomeryMultR2, MontgomeryMultR2, MontgomeryMultAfterInv);
		AddBigNbrModN(MontgomeryMultR1, MontgomeryMultR1, MontgomeryMultR2);
	}

	// This "kind of over-optimized" method yields a 10-20% performance gain compared
	// to a flat j=0..NumberLength-1 loop implementation. So we keep it as it is...
	private void MontgomeryMult(long[] Nbr1, long[] Nbr2, long[] Prod) {
		int i, j;
		long MaxUInt = 0x7FFFFFFFl;
		long Pr, Nbr;
		long Prod0, Prod1, Prod2, Prod3, Prod4, Prod5, Prod6, Prod7;
		long Prod8, Prod9, Prod10;
		long TestNbr0, TestNbr1, TestNbr2, TestNbr3, TestNbr4, TestNbr5, TestNbr6, TestNbr7;
		long TestNbr8, TestNbr9, TestNbr10;
		long Nbr2_0, Nbr2_1, Nbr2_2, Nbr2_3, Nbr2_4, Nbr2_5, Nbr2_6, Nbr2_7;
		long Nbr2_8, Nbr2_9, Nbr2_10;
		long MontDig;
		int MontgomeryMultN = (int) this.MontgomeryMultN;
		long[] TestNbr = this.TestNbr;
		int NumberLength = this.NumberLength;

		TestNbr0 = TestNbr[0];
		TestNbr1 = TestNbr[1];
		Nbr2_0 = Nbr2[0];
		Nbr2_1 = Nbr2[1];
		switch (NumberLength) {
		case 2:
			Prod0 = Prod1 = 0;
			i = 0;
			do {
				Pr = (Nbr = Nbr1[i]) * Nbr2_0 + Prod0;
				MontDig = ((int) Pr * MontgomeryMultN) & MaxUInt;
				Prod0 = (Pr = ((MontDig * TestNbr0 + Pr) >>> 31) + MontDig * TestNbr1 + Nbr * Nbr2_1 + Prod1)
						& 0x7FFFFFFFl;
				Prod1 = Pr >>> 31;
				i++;
			} while (i < 2);
			if (Prod1 > TestNbr1 || Prod1 == TestNbr1 && (Prod0 >= TestNbr0)) {
				Prod0 = (Pr = Prod0 - TestNbr0) & MaxUInt;
				Prod1 = ((Pr >> 31) + Prod1 - TestNbr1) & MaxUInt;
			}
			Prod[0] = Prod0;
			Prod[1] = Prod1;
			break;

		case 3:
			Prod0 = Prod1 = Prod2 = 0;
			TestNbr2 = TestNbr[2];
			Nbr2_2 = Nbr2[2];
			i = 0;
			do {
				Pr = (Nbr = Nbr1[i]) * Nbr2_0 + Prod0;
				MontDig = ((int) Pr * MontgomeryMultN) & MaxUInt;
				Prod0 = (Pr = ((MontDig * TestNbr0 + Pr) >>> 31) + MontDig * TestNbr1 + Nbr * Nbr2_1 + Prod1)
						& 0x7FFFFFFFl;
				Prod1 = (Pr = (Pr >>> 31) + MontDig * TestNbr2 + Nbr * Nbr2_2 + Prod2) & 0x7FFFFFFFl;
				Prod2 = Pr >>> 31;
				i++;
			} while (i < 3);
			if (Prod2 > TestNbr2
					|| Prod2 == TestNbr2 && (Prod1 > TestNbr1 || Prod1 == TestNbr1 && (Prod0 >= TestNbr0))) {
				Prod0 = (Pr = Prod0 - TestNbr0) & MaxUInt;
				Prod1 = (Pr = (Pr >> 31) + Prod1 - TestNbr1) & MaxUInt;
				Prod2 = ((Pr >> 31) + Prod2 - TestNbr2) & MaxUInt;
			}
			Prod[0] = Prod0;
			Prod[1] = Prod1;
			Prod[2] = Prod2;
			break;

		case 4:
			Prod0 = Prod1 = Prod2 = Prod3 = 0;
			TestNbr2 = TestNbr[2];
			TestNbr3 = TestNbr[3];
			Nbr2_2 = Nbr2[2];
			Nbr2_3 = Nbr2[3];
			i = 0;
			do {
				Pr = (Nbr = Nbr1[i]) * Nbr2_0 + Prod0;
				MontDig = ((int) Pr * MontgomeryMultN) & MaxUInt;
				Prod0 = (Pr = ((MontDig * TestNbr0 + Pr) >>> 31) + MontDig * TestNbr1 + Nbr * Nbr2_1 + Prod1)
						& 0x7FFFFFFFl;
				Prod1 = (Pr = (Pr >>> 31) + MontDig * TestNbr2 + Nbr * Nbr2_2 + Prod2) & 0x7FFFFFFFl;
				Prod2 = (Pr = (Pr >>> 31) + MontDig * TestNbr3 + Nbr * Nbr2_3 + Prod3) & 0x7FFFFFFFl;
				Prod3 = Pr >>> 31;
				i++;
			} while (i < 4);
			if (Prod3 > TestNbr3 || Prod3 == TestNbr3 && (Prod2 > TestNbr2
					|| Prod2 == TestNbr2 && (Prod1 > TestNbr1 || Prod1 == TestNbr1 && (Prod0 >= TestNbr0)))) {
				Prod0 = (Pr = Prod0 - TestNbr0) & MaxUInt;
				Prod1 = (Pr = (Pr >> 31) + Prod1 - TestNbr1) & MaxUInt;
				Prod2 = (Pr = (Pr >> 31) + Prod2 - TestNbr2) & MaxUInt;
				Prod3 = ((Pr >> 31) + Prod3 - TestNbr3) & MaxUInt;
			}
			Prod[0] = Prod0;
			Prod[1] = Prod1;
			Prod[2] = Prod2;
			Prod[3] = Prod3;
			break;

		case 5:
			Prod0 = Prod1 = Prod2 = Prod3 = Prod4 = 0;
			TestNbr2 = TestNbr[2];
			TestNbr3 = TestNbr[3];
			TestNbr4 = TestNbr[4];
			Nbr2_2 = Nbr2[2];
			Nbr2_3 = Nbr2[3];
			Nbr2_4 = Nbr2[4];
			i = 0;
			do {
				Pr = (Nbr = Nbr1[i]) * Nbr2_0 + Prod0;
				MontDig = ((int) Pr * MontgomeryMultN) & MaxUInt;
				Prod0 = (Pr = ((MontDig * TestNbr0 + Pr) >>> 31) + MontDig * TestNbr1 + Nbr * Nbr2_1 + Prod1)
						& 0x7FFFFFFFl;
				Prod1 = (Pr = (Pr >>> 31) + MontDig * TestNbr2 + Nbr * Nbr2_2 + Prod2) & 0x7FFFFFFFl;
				Prod2 = (Pr = (Pr >>> 31) + MontDig * TestNbr3 + Nbr * Nbr2_3 + Prod3) & 0x7FFFFFFFl;
				Prod3 = (Pr = (Pr >>> 31) + MontDig * TestNbr4 + Nbr * Nbr2_4 + Prod4) & 0x7FFFFFFFl;
				Prod4 = Pr >>> 31;
				i++;
			} while (i < 5);
			if (Prod4 > TestNbr4 || Prod4 == TestNbr4 && (Prod3 > TestNbr3 || Prod3 == TestNbr3 && (Prod2 > TestNbr2
					|| Prod2 == TestNbr2 && (Prod1 > TestNbr1 || Prod1 == TestNbr1 && (Prod0 >= TestNbr0))))) {
				Prod0 = (Pr = Prod0 - TestNbr0) & MaxUInt;
				Prod1 = (Pr = (Pr >> 31) + Prod1 - TestNbr1) & MaxUInt;
				Prod2 = (Pr = (Pr >> 31) + Prod2 - TestNbr2) & MaxUInt;
				Prod3 = (Pr = (Pr >> 31) + Prod3 - TestNbr3) & MaxUInt;
				Prod4 = ((Pr >> 31) + Prod4 - TestNbr4) & MaxUInt;
			}
			Prod[0] = Prod0;
			Prod[1] = Prod1;
			Prod[2] = Prod2;
			Prod[3] = Prod3;
			Prod[4] = Prod4;
			break;

		case 6:
			Prod0 = Prod1 = Prod2 = Prod3 = Prod4 = Prod5 = 0;
			TestNbr2 = TestNbr[2];
			TestNbr3 = TestNbr[3];
			TestNbr4 = TestNbr[4];
			TestNbr5 = TestNbr[5];
			Nbr2_2 = Nbr2[2];
			Nbr2_3 = Nbr2[3];
			Nbr2_4 = Nbr2[4];
			Nbr2_5 = Nbr2[5];
			i = 0;
			do {
				Pr = (Nbr = Nbr1[i]) * Nbr2_0 + Prod0;
				MontDig = ((int) Pr * MontgomeryMultN) & MaxUInt;
				Prod0 = (Pr = ((MontDig * TestNbr0 + Pr) >>> 31) + MontDig * TestNbr1 + Nbr * Nbr2_1 + Prod1)
						& 0x7FFFFFFFl;
				Prod1 = (Pr = (Pr >>> 31) + MontDig * TestNbr2 + Nbr * Nbr2_2 + Prod2) & 0x7FFFFFFFl;
				Prod2 = (Pr = (Pr >>> 31) + MontDig * TestNbr3 + Nbr * Nbr2_3 + Prod3) & 0x7FFFFFFFl;
				Prod3 = (Pr = (Pr >>> 31) + MontDig * TestNbr4 + Nbr * Nbr2_4 + Prod4) & 0x7FFFFFFFl;
				Prod4 = (Pr = (Pr >>> 31) + MontDig * TestNbr5 + Nbr * Nbr2_5 + Prod5) & 0x7FFFFFFFl;
				Prod5 = Pr >>> 31;
				i++;
			} while (i < 6);
			if (Prod5 > TestNbr5 || Prod5 == TestNbr5 && (Prod4 > TestNbr4
					|| Prod4 == TestNbr4 && (Prod3 > TestNbr3 || Prod3 == TestNbr3 && (Prod2 > TestNbr2
							|| Prod2 == TestNbr2 && (Prod1 > TestNbr1 || Prod1 == TestNbr1 && (Prod0 >= TestNbr0)))))) {
				Prod0 = (Pr = Prod0 - TestNbr0) & MaxUInt;
				Prod1 = (Pr = (Pr >> 31) + Prod1 - TestNbr1) & MaxUInt;
				Prod2 = (Pr = (Pr >> 31) + Prod2 - TestNbr2) & MaxUInt;
				Prod3 = (Pr = (Pr >> 31) + Prod3 - TestNbr3) & MaxUInt;
				Prod4 = (Pr = (Pr >> 31) + Prod4 - TestNbr4) & MaxUInt;
				Prod5 = ((Pr >> 31) + Prod5 - TestNbr5) & MaxUInt;
			}
			Prod[0] = Prod0;
			Prod[1] = Prod1;
			Prod[2] = Prod2;
			Prod[3] = Prod3;
			Prod[4] = Prod4;
			Prod[5] = Prod5;
			break;

		case 7:
			Prod0 = Prod1 = Prod2 = Prod3 = Prod4 = Prod5 = Prod6 = 0;
			TestNbr2 = TestNbr[2];
			TestNbr3 = TestNbr[3];
			TestNbr4 = TestNbr[4];
			TestNbr5 = TestNbr[5];
			TestNbr6 = TestNbr[6];
			Nbr2_2 = Nbr2[2];
			Nbr2_3 = Nbr2[3];
			Nbr2_4 = Nbr2[4];
			Nbr2_5 = Nbr2[5];
			Nbr2_6 = Nbr2[6];
			i = 0;
			do {
				Pr = (Nbr = Nbr1[i]) * Nbr2_0 + Prod0;
				MontDig = ((int) Pr * MontgomeryMultN) & MaxUInt;
				Prod0 = (Pr = ((MontDig * TestNbr0 + Pr) >>> 31) + MontDig * TestNbr1 + Nbr * Nbr2_1 + Prod1)
						& 0x7FFFFFFFl;
				Prod1 = (Pr = (Pr >>> 31) + MontDig * TestNbr2 + Nbr * Nbr2_2 + Prod2) & 0x7FFFFFFFl;
				Prod2 = (Pr = (Pr >>> 31) + MontDig * TestNbr3 + Nbr * Nbr2_3 + Prod3) & 0x7FFFFFFFl;
				Prod3 = (Pr = (Pr >>> 31) + MontDig * TestNbr4 + Nbr * Nbr2_4 + Prod4) & 0x7FFFFFFFl;
				Prod4 = (Pr = (Pr >>> 31) + MontDig * TestNbr5 + Nbr * Nbr2_5 + Prod5) & 0x7FFFFFFFl;
				Prod5 = (Pr = (Pr >>> 31) + MontDig * TestNbr6 + Nbr * Nbr2_6 + Prod6) & 0x7FFFFFFFl;
				Prod6 = Pr >>> 31;
				i++;
			} while (i < 7);
			if (Prod6 > TestNbr6 || Prod6 == TestNbr6
					&& (Prod5 > TestNbr5 || Prod5 == TestNbr5 && (Prod4 > TestNbr4 || Prod4 == TestNbr4
							&& (Prod3 > TestNbr3 || Prod3 == TestNbr3 && (Prod2 > TestNbr2 || Prod2 == TestNbr2
									&& (Prod1 > TestNbr1 || Prod1 == TestNbr1 && (Prod0 >= TestNbr0))))))) {
				Prod0 = (Pr = Prod0 - TestNbr0) & MaxUInt;
				Prod1 = (Pr = (Pr >> 31) + Prod1 - TestNbr1) & MaxUInt;
				Prod2 = (Pr = (Pr >> 31) + Prod2 - TestNbr2) & MaxUInt;
				Prod3 = (Pr = (Pr >> 31) + Prod3 - TestNbr3) & MaxUInt;
				Prod4 = (Pr = (Pr >> 31) + Prod4 - TestNbr4) & MaxUInt;
				Prod5 = (Pr = (Pr >> 31) + Prod5 - TestNbr5) & MaxUInt;
				Prod6 = ((Pr >> 31) + Prod6 - TestNbr6) & MaxUInt;
			}
			Prod[0] = Prod0;
			Prod[1] = Prod1;
			Prod[2] = Prod2;
			Prod[3] = Prod3;
			Prod[4] = Prod4;
			Prod[5] = Prod5;
			Prod[6] = Prod6;
			break;

		case 8:
			Prod0 = Prod1 = Prod2 = Prod3 = Prod4 = Prod5 = Prod6 = Prod7 = 0;
			TestNbr2 = TestNbr[2];
			TestNbr3 = TestNbr[3];
			TestNbr4 = TestNbr[4];
			TestNbr5 = TestNbr[5];
			TestNbr6 = TestNbr[6];
			TestNbr7 = TestNbr[7];
			Nbr2_2 = Nbr2[2];
			Nbr2_3 = Nbr2[3];
			Nbr2_4 = Nbr2[4];
			Nbr2_5 = Nbr2[5];
			Nbr2_6 = Nbr2[6];
			Nbr2_7 = Nbr2[7];
			i = 0;
			do {
				Pr = (Nbr = Nbr1[i]) * Nbr2_0 + Prod0;
				MontDig = ((int) Pr * MontgomeryMultN) & MaxUInt;
				Prod0 = (Pr = ((MontDig * TestNbr0 + Pr) >>> 31) + MontDig * TestNbr1 + Nbr * Nbr2_1 + Prod1)
						& 0x7FFFFFFFl;
				Prod1 = (Pr = (Pr >>> 31) + MontDig * TestNbr2 + Nbr * Nbr2_2 + Prod2) & 0x7FFFFFFFl;
				Prod2 = (Pr = (Pr >>> 31) + MontDig * TestNbr3 + Nbr * Nbr2_3 + Prod3) & 0x7FFFFFFFl;
				Prod3 = (Pr = (Pr >>> 31) + MontDig * TestNbr4 + Nbr * Nbr2_4 + Prod4) & 0x7FFFFFFFl;
				Prod4 = (Pr = (Pr >>> 31) + MontDig * TestNbr5 + Nbr * Nbr2_5 + Prod5) & 0x7FFFFFFFl;
				Prod5 = (Pr = (Pr >>> 31) + MontDig * TestNbr6 + Nbr * Nbr2_6 + Prod6) & 0x7FFFFFFFl;
				Prod6 = (Pr = (Pr >>> 31) + MontDig * TestNbr7 + Nbr * Nbr2_7 + Prod7) & 0x7FFFFFFFl;
				Prod7 = Pr >>> 31;
				i++;
			} while (i < 8);
			if (Prod7 > TestNbr7 || Prod7 == TestNbr7 && (Prod6 > TestNbr6 || Prod6 == TestNbr6
					&& (Prod5 > TestNbr5 || Prod5 == TestNbr5 && (Prod4 > TestNbr4 || Prod4 == TestNbr4
							&& (Prod3 > TestNbr3 || Prod3 == TestNbr3 && (Prod2 > TestNbr2 || Prod2 == TestNbr2
									&& (Prod1 > TestNbr1 || Prod1 == TestNbr1 && (Prod0 >= TestNbr0)))))))) {
				Prod0 = (Pr = Prod0 - TestNbr0) & MaxUInt;
				Prod1 = (Pr = (Pr >> 31) + Prod1 - TestNbr1) & MaxUInt;
				Prod2 = (Pr = (Pr >> 31) + Prod2 - TestNbr2) & MaxUInt;
				Prod3 = (Pr = (Pr >> 31) + Prod3 - TestNbr3) & MaxUInt;
				Prod4 = (Pr = (Pr >> 31) + Prod4 - TestNbr4) & MaxUInt;
				Prod5 = (Pr = (Pr >> 31) + Prod5 - TestNbr5) & MaxUInt;
				Prod6 = (Pr = (Pr >> 31) + Prod6 - TestNbr6) & MaxUInt;
				Prod7 = ((Pr >> 31) + Prod7 - TestNbr7) & MaxUInt;
			}
			Prod[0] = Prod0;
			Prod[1] = Prod1;
			Prod[2] = Prod2;
			Prod[3] = Prod3;
			Prod[4] = Prod4;
			Prod[5] = Prod5;
			Prod[6] = Prod6;
			Prod[7] = Prod7;
			break;

		case 9:
			Prod0 = Prod1 = Prod2 = Prod3 = Prod4 = Prod5 = Prod6 = Prod7 = Prod8 = 0;
			TestNbr2 = TestNbr[2];
			TestNbr3 = TestNbr[3];
			TestNbr4 = TestNbr[4];
			TestNbr5 = TestNbr[5];
			TestNbr6 = TestNbr[6];
			TestNbr7 = TestNbr[7];
			TestNbr8 = TestNbr[8];
			Nbr2_2 = Nbr2[2];
			Nbr2_3 = Nbr2[3];
			Nbr2_4 = Nbr2[4];
			Nbr2_5 = Nbr2[5];
			Nbr2_6 = Nbr2[6];
			Nbr2_7 = Nbr2[7];
			Nbr2_8 = Nbr2[8];
			i = 0;
			do {
				Pr = (Nbr = Nbr1[i]) * Nbr2_0 + Prod0;
				MontDig = ((int) Pr * MontgomeryMultN) & MaxUInt;
				Prod0 = (Pr = ((MontDig * TestNbr0 + Pr) >>> 31) + MontDig * TestNbr1 + Nbr * Nbr2_1 + Prod1)
						& 0x7FFFFFFFl;
				Prod1 = (Pr = (Pr >>> 31) + MontDig * TestNbr2 + Nbr * Nbr2_2 + Prod2) & 0x7FFFFFFFl;
				Prod2 = (Pr = (Pr >>> 31) + MontDig * TestNbr3 + Nbr * Nbr2_3 + Prod3) & 0x7FFFFFFFl;
				Prod3 = (Pr = (Pr >>> 31) + MontDig * TestNbr4 + Nbr * Nbr2_4 + Prod4) & 0x7FFFFFFFl;
				Prod4 = (Pr = (Pr >>> 31) + MontDig * TestNbr5 + Nbr * Nbr2_5 + Prod5) & 0x7FFFFFFFl;
				Prod5 = (Pr = (Pr >>> 31) + MontDig * TestNbr6 + Nbr * Nbr2_6 + Prod6) & 0x7FFFFFFFl;
				Prod6 = (Pr = (Pr >>> 31) + MontDig * TestNbr7 + Nbr * Nbr2_7 + Prod7) & 0x7FFFFFFFl;
				Prod7 = (Pr = (Pr >>> 31) + MontDig * TestNbr8 + Nbr * Nbr2_8 + Prod8) & 0x7FFFFFFFl;
				Prod8 = Pr >>> 31;
				i++;
			} while (i < 9);
			if (Prod8 > TestNbr8 || Prod8 == TestNbr8
					&& (Prod7 > TestNbr7 || Prod7 == TestNbr7 && (Prod6 > TestNbr6 || Prod6 == TestNbr6
							&& (Prod5 > TestNbr5 || Prod5 == TestNbr5 && (Prod4 > TestNbr4 || Prod4 == TestNbr4
									&& (Prod3 > TestNbr3 || Prod3 == TestNbr3 && (Prod2 > TestNbr2 || Prod2 == TestNbr2
											&& (Prod1 > TestNbr1 || Prod1 == TestNbr1 && (Prod0 >= TestNbr0))))))))) {
				Prod0 = (Pr = Prod0 - TestNbr0) & MaxUInt;
				Prod1 = (Pr = (Pr >> 31) + Prod1 - TestNbr1) & MaxUInt;
				Prod2 = (Pr = (Pr >> 31) + Prod2 - TestNbr2) & MaxUInt;
				Prod3 = (Pr = (Pr >> 31) + Prod3 - TestNbr3) & MaxUInt;
				Prod4 = (Pr = (Pr >> 31) + Prod4 - TestNbr4) & MaxUInt;
				Prod5 = (Pr = (Pr >> 31) + Prod5 - TestNbr5) & MaxUInt;
				Prod6 = (Pr = (Pr >> 31) + Prod6 - TestNbr6) & MaxUInt;
				Prod7 = (Pr = (Pr >> 31) + Prod7 - TestNbr7) & MaxUInt;
				Prod8 = ((Pr >> 31) + Prod8 - TestNbr8) & MaxUInt;
			}
			Prod[0] = Prod0;
			Prod[1] = Prod1;
			Prod[2] = Prod2;
			Prod[3] = Prod3;
			Prod[4] = Prod4;
			Prod[5] = Prod5;
			Prod[6] = Prod6;
			Prod[7] = Prod7;
			Prod[8] = Prod8;
			break;

		case 10:
			Prod0 = Prod1 = Prod2 = Prod3 = Prod4 = Prod5 = Prod6 = Prod7 = Prod8 = Prod9 = 0;
			TestNbr2 = TestNbr[2];
			TestNbr3 = TestNbr[3];
			TestNbr4 = TestNbr[4];
			TestNbr5 = TestNbr[5];
			TestNbr6 = TestNbr[6];
			TestNbr7 = TestNbr[7];
			TestNbr8 = TestNbr[8];
			TestNbr9 = TestNbr[9];
			Nbr2_2 = Nbr2[2];
			Nbr2_3 = Nbr2[3];
			Nbr2_4 = Nbr2[4];
			Nbr2_5 = Nbr2[5];
			Nbr2_6 = Nbr2[6];
			Nbr2_7 = Nbr2[7];
			Nbr2_8 = Nbr2[8];
			Nbr2_9 = Nbr2[9];
			i = 0;
			do {
				Pr = (Nbr = Nbr1[i]) * Nbr2_0 + Prod0;
				MontDig = ((int) Pr * MontgomeryMultN) & MaxUInt;
				Prod0 = (Pr = ((MontDig * TestNbr0 + Pr) >>> 31) + MontDig * TestNbr1 + Nbr * Nbr2_1 + Prod1)
						& 0x7FFFFFFFl;
				Prod1 = (Pr = (Pr >>> 31) + MontDig * TestNbr2 + Nbr * Nbr2_2 + Prod2) & 0x7FFFFFFFl;
				Prod2 = (Pr = (Pr >>> 31) + MontDig * TestNbr3 + Nbr * Nbr2_3 + Prod3) & 0x7FFFFFFFl;
				Prod3 = (Pr = (Pr >>> 31) + MontDig * TestNbr4 + Nbr * Nbr2_4 + Prod4) & 0x7FFFFFFFl;
				Prod4 = (Pr = (Pr >>> 31) + MontDig * TestNbr5 + Nbr * Nbr2_5 + Prod5) & 0x7FFFFFFFl;
				Prod5 = (Pr = (Pr >>> 31) + MontDig * TestNbr6 + Nbr * Nbr2_6 + Prod6) & 0x7FFFFFFFl;
				Prod6 = (Pr = (Pr >>> 31) + MontDig * TestNbr7 + Nbr * Nbr2_7 + Prod7) & 0x7FFFFFFFl;
				Prod7 = (Pr = (Pr >>> 31) + MontDig * TestNbr8 + Nbr * Nbr2_8 + Prod8) & 0x7FFFFFFFl;
				Prod8 = (Pr = (Pr >>> 31) + MontDig * TestNbr9 + Nbr * Nbr2_9 + Prod9) & 0x7FFFFFFFl;
				Prod9 = Pr >>> 31;
				i++;
			} while (i < 10);
			if (Prod9 > TestNbr9 || Prod9 == TestNbr9 && (Prod8 > TestNbr8 || Prod8 == TestNbr8
					&& (Prod7 > TestNbr7 || Prod7 == TestNbr7 && (Prod6 > TestNbr6 || Prod6 == TestNbr6
							&& (Prod5 > TestNbr5 || Prod5 == TestNbr5 && (Prod4 > TestNbr4 || Prod4 == TestNbr4
									&& (Prod3 > TestNbr3 || Prod3 == TestNbr3 && (Prod2 > TestNbr2 || Prod2 == TestNbr2
											&& (Prod1 > TestNbr1 || Prod1 == TestNbr1 && (Prod0 >= TestNbr0)))))))))) {
				Prod0 = (Pr = Prod0 - TestNbr0) & MaxUInt;
				Prod1 = (Pr = (Pr >> 31) + Prod1 - TestNbr1) & MaxUInt;
				Prod2 = (Pr = (Pr >> 31) + Prod2 - TestNbr2) & MaxUInt;
				Prod3 = (Pr = (Pr >> 31) + Prod3 - TestNbr3) & MaxUInt;
				Prod4 = (Pr = (Pr >> 31) + Prod4 - TestNbr4) & MaxUInt;
				Prod5 = (Pr = (Pr >> 31) + Prod5 - TestNbr5) & MaxUInt;
				Prod6 = (Pr = (Pr >> 31) + Prod6 - TestNbr6) & MaxUInt;
				Prod7 = (Pr = (Pr >> 31) + Prod7 - TestNbr7) & MaxUInt;
				Prod8 = (Pr = (Pr >> 31) + Prod8 - TestNbr8) & MaxUInt;
				Prod9 = ((Pr >> 31) + Prod9 - TestNbr9) & MaxUInt;
			}
			Prod[0] = Prod0;
			Prod[1] = Prod1;
			Prod[2] = Prod2;
			Prod[3] = Prod3;
			Prod[4] = Prod4;
			Prod[5] = Prod5;
			Prod[6] = Prod6;
			Prod[7] = Prod7;
			Prod[8] = Prod8;
			Prod[9] = Prod9;
			break;

		case 11:
			Prod0 = Prod1 = Prod2 = Prod3 = Prod4 = Prod5 = Prod6 = Prod7 = Prod8 = Prod9 = Prod10 = 0;
			TestNbr2 = TestNbr[2];
			TestNbr3 = TestNbr[3];
			TestNbr4 = TestNbr[4];
			TestNbr5 = TestNbr[5];
			TestNbr6 = TestNbr[6];
			TestNbr7 = TestNbr[7];
			TestNbr8 = TestNbr[8];
			TestNbr9 = TestNbr[9];
			TestNbr10 = TestNbr[10];
			Nbr2_2 = Nbr2[2];
			Nbr2_3 = Nbr2[3];
			Nbr2_4 = Nbr2[4];
			Nbr2_5 = Nbr2[5];
			Nbr2_6 = Nbr2[6];
			Nbr2_7 = Nbr2[7];
			Nbr2_8 = Nbr2[8];
			Nbr2_9 = Nbr2[9];
			Nbr2_10 = Nbr2[10];
			i = 0;
			do {
				Pr = (Nbr = Nbr1[i]) * Nbr2_0 + Prod0;
				MontDig = ((int) Pr * MontgomeryMultN) & MaxUInt;
				Prod0 = (Pr = ((MontDig * TestNbr0 + Pr) >>> 31) + MontDig * TestNbr1 + Nbr * Nbr2_1 + Prod1)
						& 0x7FFFFFFFl;
				Prod1 = (Pr = (Pr >>> 31) + MontDig * TestNbr2 + Nbr * Nbr2_2 + Prod2) & 0x7FFFFFFFl;
				Prod2 = (Pr = (Pr >>> 31) + MontDig * TestNbr3 + Nbr * Nbr2_3 + Prod3) & 0x7FFFFFFFl;
				Prod3 = (Pr = (Pr >>> 31) + MontDig * TestNbr4 + Nbr * Nbr2_4 + Prod4) & 0x7FFFFFFFl;
				Prod4 = (Pr = (Pr >>> 31) + MontDig * TestNbr5 + Nbr * Nbr2_5 + Prod5) & 0x7FFFFFFFl;
				Prod5 = (Pr = (Pr >>> 31) + MontDig * TestNbr6 + Nbr * Nbr2_6 + Prod6) & 0x7FFFFFFFl;
				Prod6 = (Pr = (Pr >>> 31) + MontDig * TestNbr7 + Nbr * Nbr2_7 + Prod7) & 0x7FFFFFFFl;
				Prod7 = (Pr = (Pr >>> 31) + MontDig * TestNbr8 + Nbr * Nbr2_8 + Prod8) & 0x7FFFFFFFl;
				Prod8 = (Pr = (Pr >>> 31) + MontDig * TestNbr9 + Nbr * Nbr2_9 + Prod9) & 0x7FFFFFFFl;
				Prod9 = (Pr = (Pr >>> 31) + MontDig * TestNbr10 + Nbr * Nbr2_10 + Prod10) & 0x7FFFFFFFl;
				Prod10 = Pr >>> 31;
				i++;
			} while (i < 11);
			if (Prod10 > TestNbr10 || Prod10 == TestNbr10 && (Prod9 > TestNbr9 || Prod9 == TestNbr9
					&& (Prod8 > TestNbr8 || Prod8 == TestNbr8 && (Prod7 > TestNbr7 || Prod7 == TestNbr7
							&& (Prod6 > TestNbr6 || Prod6 == TestNbr6 && (Prod5 > TestNbr5 || Prod5 == TestNbr5
									&& (Prod4 > TestNbr4 || Prod4 == TestNbr4 && (Prod3 > TestNbr3 || Prod3 == TestNbr3
											&& (Prod2 > TestNbr2 || Prod2 == TestNbr2 && (Prod1 > TestNbr1
													|| Prod1 == TestNbr1 && (Prod0 >= TestNbr0))))))))))) {
				Prod0 = (Pr = Prod0 - TestNbr0) & MaxUInt;
				Prod1 = (Pr = (Pr >> 31) + Prod1 - TestNbr1) & MaxUInt;
				Prod2 = (Pr = (Pr >> 31) + Prod2 - TestNbr2) & MaxUInt;
				Prod3 = (Pr = (Pr >> 31) + Prod3 - TestNbr3) & MaxUInt;
				Prod4 = (Pr = (Pr >> 31) + Prod4 - TestNbr4) & MaxUInt;
				Prod5 = (Pr = (Pr >> 31) + Prod5 - TestNbr5) & MaxUInt;
				Prod6 = (Pr = (Pr >> 31) + Prod6 - TestNbr6) & MaxUInt;
				Prod7 = (Pr = (Pr >> 31) + Prod7 - TestNbr7) & MaxUInt;
				Prod8 = (Pr = (Pr >> 31) + Prod8 - TestNbr8) & MaxUInt;
				Prod9 = (Pr = (Pr >> 31) + Prod9 - TestNbr9) & MaxUInt;
				Prod10 = ((Pr >> 31) + Prod10 - TestNbr10) & MaxUInt;
			}
			Prod[0] = Prod0;
			Prod[1] = Prod1;
			Prod[2] = Prod2;
			Prod[3] = Prod3;
			Prod[4] = Prod4;
			Prod[5] = Prod5;
			Prod[6] = Prod6;
			Prod[7] = Prod7;
			Prod[8] = Prod8;
			Prod[9] = Prod9;
			Prod[10] = Prod10;
			break;

		default:
			Prod0 = Prod1 = Prod2 = Prod3 = Prod4 = Prod5 = Prod6 = Prod7 = Prod8 = Prod9 = Prod10 = 0;
			TestNbr2 = TestNbr[2];
			TestNbr3 = TestNbr[3];
			TestNbr4 = TestNbr[4];
			TestNbr5 = TestNbr[5];
			TestNbr6 = TestNbr[6];
			TestNbr7 = TestNbr[7];
			TestNbr8 = TestNbr[8];
			TestNbr9 = TestNbr[9];
			TestNbr10 = TestNbr[10];
			Nbr2_2 = Nbr2[2];
			Nbr2_3 = Nbr2[3];
			Nbr2_4 = Nbr2[4];
			Nbr2_5 = Nbr2[5];
			Nbr2_6 = Nbr2[6];
			Nbr2_7 = Nbr2[7];
			Nbr2_8 = Nbr2[8];
			Nbr2_9 = Nbr2[9];
			Nbr2_10 = Nbr2[10];
			for (j = 11; j < NumberLength; j++) {
				Prod[j] = 0;
			}
			i = 0;
			do {
				Pr = (Nbr = Nbr1[i]) * Nbr2_0 + Prod0;
				MontDig = ((int) Pr * MontgomeryMultN) & MaxUInt;
				Prod0 = (Pr = ((MontDig * TestNbr0 + Pr) >>> 31) + MontDig * TestNbr1 + Nbr * Nbr2_1 + Prod1)
						& 0x7FFFFFFFl;
				Prod1 = (Pr = (Pr >>> 31) + MontDig * TestNbr2 + Nbr * Nbr2_2 + Prod2) & 0x7FFFFFFFl;
				Prod2 = (Pr = (Pr >>> 31) + MontDig * TestNbr3 + Nbr * Nbr2_3 + Prod3) & 0x7FFFFFFFl;
				Prod3 = (Pr = (Pr >>> 31) + MontDig * TestNbr4 + Nbr * Nbr2_4 + Prod4) & 0x7FFFFFFFl;
				Prod4 = (Pr = (Pr >>> 31) + MontDig * TestNbr5 + Nbr * Nbr2_5 + Prod5) & 0x7FFFFFFFl;
				Prod5 = (Pr = (Pr >>> 31) + MontDig * TestNbr6 + Nbr * Nbr2_6 + Prod6) & 0x7FFFFFFFl;
				Prod6 = (Pr = (Pr >>> 31) + MontDig * TestNbr7 + Nbr * Nbr2_7 + Prod7) & 0x7FFFFFFFl;
				Prod7 = (Pr = (Pr >>> 31) + MontDig * TestNbr8 + Nbr * Nbr2_8 + Prod8) & 0x7FFFFFFFl;
				Prod8 = (Pr = (Pr >>> 31) + MontDig * TestNbr9 + Nbr * Nbr2_9 + Prod9) & 0x7FFFFFFFl;
				Prod9 = (Pr = (Pr >>> 31) + MontDig * TestNbr10 + Nbr * Nbr2_10 + Prod10) & 0x7FFFFFFFl;
				Prod10 = (Pr = (Pr >>> 31) + MontDig * TestNbr[11] + Nbr * Nbr2[11] + Prod[11]) & 0x7FFFFFFFl;
				for (j = 12; j < NumberLength; j++) {
					Prod[j - 1] = (Pr = (Pr >>> 31) + MontDig * TestNbr[j] + Nbr * Nbr2[j] + Prod[j]) & 0x7FFFFFFFl;
				}
				Prod[j - 1] = (Pr >>> 31);
				i++;
			} while (i < NumberLength);
			Prod[0] = Prod0;
			Prod[1] = Prod1;
			Prod[2] = Prod2;
			Prod[3] = Prod3;
			Prod[4] = Prod4;
			Prod[5] = Prod5;
			Prod[6] = Prod6;
			Prod[7] = Prod7;
			Prod[8] = Prod8;
			Prod[9] = Prod9;
			Prod[10] = Prod10;
			for (j = NumberLength - 1; j >= 0; j--) {
				if (Prod[j] != TestNbr[j]) {
					break;
				}
			}
			if (Prod[j] >= TestNbr[j]) {
				Pr = 0;
				for (j = 0; j < NumberLength; j++) {
					Prod[j] = (Pr = (Pr >> 31) + Prod[j] - TestNbr[j]) & MaxUInt;
				}
			}
		} /* end switch */
	}

	private static void GenerateSieve(int initial, byte[] sieve, byte[] sieve2310, int[] SmallPrime) {
		int i, j, Q;
		for (i = 0; i < 23100; i += 2310) {
			System.arraycopy(sieve2310, 0, sieve, i, 2310);
		}
		j = 5;
		Q = 13; /* Point to prime 13 */
		do {
			if (initial > Q * Q) {
				for (i = (int) (((long) initial * ((Q - 1) / 2)) % Q); i < 23100; i += Q) {
					sieve[i] = 1; /* Composite */
				}
			} else {
				i = Q * Q - initial;
				if (i < 46200) {
					for (i = i / 2; i < 23100; i += Q) {
						sieve[i] = 1; /* Composite */
					}
				} else {
					break;
				}
			}
			Q = SmallPrime[++j];
		} while (Q < 5000);
	}

	/** returns the number of modular multiplications */
	private static int lucas_cost(int n, double v) {
		int c, d, e, r;

		d = n;
		r = (int) (d / v + 0.5);
		if (r >= n)
			return (ADD * n);
		d = n - r;
		e = 2 * r - n;
		c = DUP + ADD; /* initial duplicate and final addition */
		while (d != e) {
			if (d < e) {
				r = d;
				d = e;
				e = r;
			}
			if (4 * d <= 5 * e && ((d + e) % 3) == 0) { /* condition 1 */
				r = (2 * d - e) / 3;
				e = (2 * e - d) / 3;
				d = r;
				c += 3 * ADD; /* 3 additions */
			} else if (4 * d <= 5 * e && (d - e) % 6 == 0) { /* condition 2 */
				d = (d - e) / 2;
				c += ADD + DUP; /* one addition, one duplicate */
			} else if (d <= (4 * e)) { /* condition 3 */
				d -= e;
				c += ADD; /* one addition */
			} else if ((d + e) % 2 == 0) { /* condition 4 */
				d = (d - e) / 2;
				c += ADD + DUP; /* one addition, one duplicate */
			} else if (d % 2 == 0) { /* condition 5 */
				d /= 2;
				c += ADD + DUP; /* one addition, one duplicate */
			} else if (d % 3 == 0) { /* condition 6 */
				d = d / 3 - e;
				c += 3 * ADD + DUP; /* three additions, one duplicate */
			} else if ((d + e) % 3 == 0) { /* condition 7 */
				d = (d - 2 * e) / 3;
				c += 3 * ADD + DUP; /* three additions, one duplicate */
			} else if ((d - e) % 3 == 0) { /* condition 8 */
				d = (d - e) / 3;
				c += 3 * ADD + DUP; /* three additions, one duplicate */
			} else if (e % 2 == 0) { /* condition 9 */
				e /= 2;
				c += ADD + DUP; /* one addition, one duplicate */
			}
		}
		return (c);
	}

	/**
	 * Adds Q=(x2:z2) and R=(x1:z1) and puts the result in (x3:z3), using 5/6 mul, 6 add/sub and 6 mod. One assumes that
	 * Q-R=P or R-Q=P where P=(x:z). Uses the following global variables: - n : number to factor - x, z : coordinates of
	 * P - u, v, w : auxiliary variables Modifies: x3, z3, u, v, w. (x3,z3) may be identical to (x2,z2) and to (x,z)
	 */
	private void add3(long[] x3, long[] z3, long[] x2, long[] z2, long[] x1, long[] z1, long[] x, long[] z) {
		long[] t = fieldTX;
		long[] u = fieldTZ;
		long[] v = fieldUX;
		long[] w = fieldUZ;
		SubtractBigNbrModN(x2, z2, v); // v = x2-z2
		AddBigNbrModN(x1, z1, w); // w = x1+z1
		MontgomeryMult(v, w, u); // u = (x2-z2)*(x1+z1)
		AddBigNbrModN(x2, z2, w); // w = x2+z2
		SubtractBigNbrModN(x1, z1, t); // t = x1-z1
		MontgomeryMult(t, w, v); // v = (x2+z2)*(x1-z1)
		AddBigNbrModN(u, v, t); // t = 2*(x1*x2-z1*z2)
		MontgomeryMult(t, t, w); // w = 4*(x1*x2-z1*z2)^2
		SubtractBigNbrModN(u, v, t); // t = 2*(x2*z1-x1*z2)
		MontgomeryMult(t, t, v); // v = 4*(x2*z1-x1*z2)^2
		if (BigNbrAreEqual(x, x3)) {
			System.arraycopy(x, 0, u, 0, NumberLength);
			System.arraycopy(w, 0, t, 0, NumberLength);
			MontgomeryMult(z, t, w);
			MontgomeryMult(v, u, z3);
			System.arraycopy(w, 0, x3, 0, NumberLength);
		} else {
			MontgomeryMult(w, z, x3); // x3 = 4*z*(x1*x2-z1*z2)^2
			MontgomeryMult(x, v, z3); // z3 = 4*x*(x2*z1-x1*z2)^2
		}
	}

	private void AddBigNbr(long[] Nbr1, long[] Nbr2, long[] Sum) {
		int NumberLength = this.NumberLength;
		long Cy = 0;
		for (int i = 0; i < NumberLength; i++) {
			Cy = (Cy >> 31) + Nbr1[i] + Nbr2[i];
			Sum[i] = Cy & 0x7FFFFFFFl;
		}
	}

	private void AddBigNbr32(long[] Nbr1, long[] Nbr2, long[] Sum) {
		int NumberLength = this.NumberLength;
		long Cy = 0;
		for (int i = 0; i < NumberLength; i++) {
			Cy = (Cy >> 32) + Nbr1[i] + Nbr2[i];
			Sum[i] = Cy & 0xFFFFFFFFl;
		}
	}

	private void AddBigNbrModN(long[] Nbr1, long[] Nbr2, long[] Sum) {
		int NumberLength = this.NumberLength;
		long MaxUInt = 0x7FFFFFFFl;
		long Cy = 0;
		int i;

		for (i = 0; i < NumberLength; i++) {
			Cy = (Cy >> 31) + Nbr1[i] + Nbr2[i] - TestNbr[i];
			Sum[i] = Cy & MaxUInt;
		}
		if (Cy < 0) {
			Cy = 0;
			for (i = 0; i < NumberLength; i++) {
				Cy = (Cy >> 31) + Sum[i] + TestNbr[i];
				Sum[i] = Cy & MaxUInt;
			}
		}
	}

	private void AdjustModN(long[] Nbr) {
		int NumberLength = this.NumberLength;
		long MaxUInt = 0x7FFFFFFFl;
		long TrialQuotient;
		long Cy;
		int i;
		double dAux;

		dAux = Nbr[NumberLength] * dDosALa31 + Nbr[NumberLength - 1];
		if (NumberLength > 1) {
			dAux += Nbr[NumberLength - 2] / dDosALa31;
		}
		TrialQuotient = (long) Math.ceil(dAux / dN) + 2;
		if (TrialQuotient >= DosALa32) {
			Cy = 0;
			for (i = 0; i < NumberLength; i++) {
				Cy = Nbr[i + 1] - (TrialQuotient >>> 31) * TestNbr[i] - Cy;
				Nbr[i + 1] = Cy & MaxUInt;
				Cy = (MaxUInt - Cy) >>> 31;
			}
			TrialQuotient &= MaxUInt;
		}
		Cy = 0;
		for (i = 0; i < NumberLength; i++) {
			Cy = Nbr[i] - TrialQuotient * TestNbr[i] - Cy;
			Nbr[i] = Cy & MaxUInt;
			Cy = (MaxUInt - Cy) >>> 31;
		}
		Nbr[NumberLength] -= Cy;
		while ((Nbr[NumberLength] & MaxUInt) != 0) {
			Cy = 0;
			for (i = 0; i < NumberLength; i++) {
				Cy += Nbr[i] + TestNbr[i];
				Nbr[i] = Cy & MaxUInt;
				Cy >>= 31;
			}
			Nbr[NumberLength] += Cy;
		}
	}

	// Perform JS <- JS * JW
	private BigInteger BigIntToBigNbr(long[] GD) {
		byte[] Result;
		long[] Temp;
		int i, NL;
		long digit;

		Temp = new long[NumberLength];
		Convert31To32Bits(GD, Temp);
		NL = NumberLength * 4;
		Result = new byte[NL];
		for (i = 0; i < NumberLength; i++) {
			digit = Temp[i];
			Result[NL - 1 - 4 * i] = (byte) (digit & 0xFF);
			Result[NL - 2 - 4 * i] = (byte) (digit / 0x100 & 0xFF);
			Result[NL - 3 - 4 * i] = (byte) (digit / 0x10000 & 0xFF);
			Result[NL - 4 - 4 * i] = (byte) (digit / 0x1000000 & 0xFF);
		}
		return (new BigInteger(Result));
	}

	// Perform JS <- JS ^ 2
	private boolean BigNbrAreEqual(long[] Nbr1, long[] Nbr2) {
		for (int i = 0; i < NumberLength; i++) {
			if (Nbr1[i] != Nbr2[i]) {
				return false;
			}
		}
		return true;
	}

	private boolean BigNbrIsZero(long[] Nbr) {
		for (int i = 0; i < NumberLength; i++) {
			if (Nbr[i] != 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Converts BigInteger N into {TestNbr, NumberLength}.
	 * @param N
	 */
	private void BigNbrToBigInt(BigInteger N) {
		byte[] Result;
		long[] Temp;
		int i, j;
		long mask, p;

		Result = N.toByteArray();
		NumberLength = (Result.length * 8 + 30) / 31;
		Temp = new long[NumberLength + 1];
		j = 0;
		mask = 1;
		p = 0;
		for (i = Result.length - 1; i >= 0; i--) {
			p += mask * (Result[i] >= 0 ? Result[i] : Result[i] + 256);
			mask *= 0x100;
			if (mask == DosALa32) {
				Temp[j++] = p;
				mask = 1;
				p = 0;
			}
		}
		Temp[j] = p;
		Convert32To31Bits(Temp, TestNbr);
		if (TestNbr[NumberLength - 1] > Mi) {
			TestNbr[NumberLength] = 0;
			NumberLength++;
		}
		TestNbr[NumberLength] = 0;
	}

	private void ChSignBigNbr(long[] Nbr) {
		int NumberLength = this.NumberLength;
		long Cy = 0;
		for (int i = 0; i < NumberLength; i++) {
			Cy = (Cy >> 31) - Nbr[i];
			Nbr[i] = Cy & 0x7FFFFFFFl;
		}
	}

	private void Convert31To32Bits(long[] nbr31bits, long[] nbr32bits) {
		int i, j, k;
		i = 0;
		for (j = -1; j < NumberLength; j++) {
			k = i % 31;
			if (k == 0) {
				j++;
			}
			if (j == NumberLength) {
				break;
			}
			if (j == NumberLength - 1) {
				nbr32bits[i] = nbr31bits[j] >> k;
			} else {
				nbr32bits[i] = ((nbr31bits[j] >> k) | (nbr31bits[j + 1] << (31 - k))) & 0xFFFFFFFFl;
			}
			i++;
		}
		for (; i < NumberLength; i++) {
			nbr32bits[i] = 0;
		}
	}

	private void Convert32To31Bits(long[] nbr32bits, long[] nbr31bits) {
		int i, j, k;
		j = 0;
		nbr32bits[NumberLength] = 0;
		for (i = 0; i < NumberLength; i++) {
			k = i % 32;
			if (k == 0) {
				nbr31bits[i] = nbr32bits[j] & 0x7FFFFFFFl;
			} else {
				nbr31bits[i] = ((nbr32bits[j] >> (32 - k)) | (nbr32bits[j + 1] << k)) & 0x7FFFFFFFl;
				j++;
			}
		}
	}

	private void DivBigNbrByLong(long[] Dividend, long Divisor, long[] Quotient) {
		int i;
		boolean ChSignDivisor = false;
		long Divid, Rem = 0;

		if (Divisor < 0) { // If divisor is negative...
			ChSignDivisor = true; // Indicate to change sign at the end and
			Divisor = -Divisor; // convert divisor to positive.
		}
		if (Dividend[i = NumberLength - 1] >= 0x40000000l) { // If dividend is negative...
			Rem = Divisor - 1;
		}
		for (; i >= 0; i--) {
			Divid = Dividend[i] + (Rem << 31);
			Rem = Divid % Divisor;
			Quotient[i] = Divid / Divisor;
		}
		if (ChSignDivisor) { // Change sign if divisor is negative.
			ChSignBigNbr(Quotient); // Convert divisor to positive.
		}
	}

	/**
	 * computes 2P=(x2:z2) from P=(x1:z1), with 5 mul, 4 add/sub, 5 mod. Uses the following global variables: - n :
	 * number to factor - b : (a+2)/4 mod n - u, v, w : auxiliary variables Modifies: x2, z2, u, v, w
	 */
	private void duplicate(long[] x2, long[] z2, long[] x1, long[] z1) {
		long[] u = fieldUZ;
		long[] v = fieldTX;
		long[] w = fieldTZ;
		AddBigNbrModN(x1, z1, w); // w = x1+z1
		MontgomeryMult(w, w, u); // u = (x1+z1)^2
		SubtractBigNbrModN(x1, z1, w); // w = x1-z1
		MontgomeryMult(w, w, v); // v = (x1-z1)^2
		MontgomeryMult(u, v, x2); // x2 = u*v = (x1^2 - z1^2)^2
		SubtractBigNbrModN(u, v, w); // w = u-v = 4*x1*z1
		MontgomeryMult(fieldAA, w, u);
		AddBigNbrModN(u, v, u); // u = (v+b*w)
		MontgomeryMult(w, u, z2); // z2 = (w*u)
	}

	/**
	 * Gcd calculation:
	 * <ul>
	 * <li>Step 1: Set k<-0, and then repeatedly set k<-k+1, u<-u/2, v<-v/2 zero or more times until u and v are not
	 * both even.</li>
	 * <li>Step 2: If u is odd, set t<-(-v) and go to step 4. Otherwise set t<-u.</li>
	 * <li>Step 3: Set t<-t/2</li>
	 * <li>Step 4: If t is even, go back to step 3.</li>
	 * <li>Step 5: If t>0, set u<-t, otherwise set v<-(-t).</li>
	 * <li>Step 6: Set t<-u-v. If t!=0, go back to step 3.</li>
	 * <li>Step 7: The GCD is u*2^k.</li>
	 * </ul>
	 * 
	 * @param Nbr1
	 * @param Nbr2
	 * @param Gcd
	 */
	private void GcdBigNbr(long[] Nbr1, long[] Nbr2, long[] Gcd) {
		int i, k;
		int NumberLength = this.NumberLength;

		System.arraycopy(Nbr1, 0, CalcAuxGcdU, 0, NumberLength);
		System.arraycopy(Nbr2, 0, CalcAuxGcdV, 0, NumberLength);
		for (i = 0; i < NumberLength; i++) {
			if (CalcAuxGcdU[i] != 0) {
				break;
			}
		}
		if (i == NumberLength) {
			System.arraycopy(CalcAuxGcdV, 0, Gcd, 0, NumberLength);
			return;
		}
		for (i = 0; i < NumberLength; i++) {
			if (CalcAuxGcdV[i] != 0) {
				break;
			}
		}
		if (i == NumberLength) {
			System.arraycopy(CalcAuxGcdU, 0, Gcd, 0, NumberLength);
			return;
		}
		if (CalcAuxGcdU[NumberLength - 1] >= 0x40000000l) {
			ChSignBigNbr(CalcAuxGcdU);
		}
		if (CalcAuxGcdV[NumberLength - 1] >= 0x40000000l) {
			ChSignBigNbr(CalcAuxGcdV);
		}
		k = 0;
		while ((CalcAuxGcdU[0] & 1) == 0 && (CalcAuxGcdV[0] & 1) == 0) { // Step 1
			k++;
			DivBigNbrByLong(CalcAuxGcdU, 2, CalcAuxGcdU);
			DivBigNbrByLong(CalcAuxGcdV, 2, CalcAuxGcdV);
		}
		if ((CalcAuxGcdU[0] & 1) == 1) { // Step 2
			System.arraycopy(CalcAuxGcdV, 0, CalcAuxGcdT, 0, NumberLength);
			ChSignBigNbr(CalcAuxGcdT);
		} else {
			System.arraycopy(CalcAuxGcdU, 0, CalcAuxGcdT, 0, NumberLength);
		}
		do {
			while ((CalcAuxGcdT[0] & 1) == 0) { // Step 4
				DivBigNbrByLong(CalcAuxGcdT, 2, CalcAuxGcdT); // Step 3
			}
			if (CalcAuxGcdT[NumberLength - 1] < 0x40000000l) { // Step 5
				System.arraycopy(CalcAuxGcdT, 0, CalcAuxGcdU, 0, NumberLength);
			} else {
				System.arraycopy(CalcAuxGcdT, 0, CalcAuxGcdV, 0, NumberLength);
				ChSignBigNbr(CalcAuxGcdV);
			}
			SubtractBigNbr(CalcAuxGcdU, CalcAuxGcdV, CalcAuxGcdT); // Step 6
			for (i = 0; i < NumberLength; i++) {
				if (CalcAuxGcdT[i] != 0) {
					break;
				}
			}
		} while (i != NumberLength);
		System.arraycopy(CalcAuxGcdU, 0, Gcd, 0, NumberLength); // Step 7
		while (k > 0) {
			AddBigNbr(Gcd, Gcd, Gcd);
			k--;
		}
	}

	private void LongToBigNbr(long Nbr, long[] Out) {
		int i;

		Out[0] = Nbr & 0x7FFFFFFFl;
		Out[1] = (Nbr >> 31) & 0x7FFFFFFFl;
		for (i = 2; i < NumberLength; i++) {
			Out[i] = (Nbr < 0 ? 0x7FFFFFFFl : 0);
		}
	}

	/**
	 * <p>Find the multiplicative inverse (1/a) modulo b and return the result in inv.</p>
	 * 
	 * <p>This implementation returns dummy values for a-values that are not invertible modulo b.
	 * This is good for the performance of the ECM implementation because the latter  works with
	 * accumulated gcd's and then it doesn't matter if a few of the single gcd-values are wrong.</p>
	 */
	private void ModInvBigNbr(long[] a, long[] inv, long[] b) {
		int i;
		int NumberLength = this.NumberLength;
		int Dif, E;
		int st1, st2;
		long Yaa, Yab; // 2^E * A' = Yaa A + Yab B
		long Yba, Ybb; // 2^E * B' = Yba A + Ybb B
		long Ygb0; // 2^E * Mu' = Yaa Mu + Yab Gamma + Ymb0 B0
		long Ymb0; // 2^E * Gamma' = Yba Mu + Ybb Gamma + Ygb0 B0
		int Iaa, Iab, Iba, Ibb;
		long Tmp1, Tmp2, Tmp3, Tmp4, Tmp5;
		int B0l;
		int invB0l;
		int Al, Bl, T1, Gl, Ml;
		long Cy1, Cy2, Cy3, Cy4;
		int Yaah, Yabh, Ybah, Ybbh;
		int Ymb0h, Ygb0h;
		long Pr1, Pr2, Pr3, Pr4, Pr5, Pr6, Pr7;
		long[] B = this.biTmp;
		long[] CalcAuxModInvA = this.CalcAuxGcdU;
		long[] CalcAuxModInvB = this.CalcAuxGcdV;
		long[] CalcAuxModInvMu = this.CalcAuxGcdT; // Cannot be inv
		long[] CalcAuxModInvGamma = inv;

		Convert31To32Bits(a, CalcAuxModInvA);
		Convert31To32Bits(b, CalcAuxModInvB);
		System.arraycopy(CalcAuxModInvB, 0, B, 0, NumberLength);
		B0l = (int) B[0];
		invB0l = B0l; // 2 least significant bits of inverse correct.
		invB0l = invB0l * (2 - B0l * invB0l); // 4 LSB of inverse correct.
		invB0l = invB0l * (2 - B0l * invB0l); // 8 LSB of inverse correct.
		invB0l = invB0l * (2 - B0l * invB0l); // 16 LSB of inverse correct.
		invB0l = invB0l * (2 - B0l * invB0l); // 32 LSB of inverse correct.
		for (i = NumberLength - 1; i >= 0; i--) {
			CalcAuxModInvGamma[i] = 0;
			CalcAuxModInvMu[i] = 0;
		}
		CalcAuxModInvMu[0] = 1;
		Dif = 0;
		outer_loop: do {
			Iaa = Ibb = 1;
			Iab = Iba = 0;
			Al = (int) CalcAuxModInvA[0];
			Bl = (int) CalcAuxModInvB[0];
			E = 0;
			if (Bl == 0) {
				for (i = NumberLength - 1; i >= 0; i--) {
					if (CalcAuxModInvB[i] != 0)
						break;
				}
				if (i < 0)
					break; // Go out of loop if CalcAuxModInvB = 0
			}
			do {
				T1 = 0;
				while ((Bl & 1) == 0) {
					if (E == 31) {
						Yaa = Iaa;
						Yab = Iab;
						Yba = Iba;
						Ybb = Ibb;
						Gl = (int) CalcAuxModInvGamma[0];
						Ml = (int) CalcAuxModInvMu[0];
						Dif++;
						T1++;
						Yaa <<= T1;
						Yab <<= T1;
						Ymb0 = (-(int) Yaa * Ml - (int) Yab * Gl) * invB0l;
						Ygb0 = (-Iba * Ml - Ibb * Gl) * invB0l;
						Cy1 = Cy2 = Cy3 = Cy4 = 0;
						Yaah = (int) (Yaa >> 32);
						Yabh = (int) (Yab >> 32);
						Ybah = (int) (Yba >> 32);
						Ybbh = (int) (Ybb >> 32);
						Ymb0h = (int) (Ymb0 >> 32);
						Ygb0h = (int) (Ygb0 >> 32);
						Yaa &= 0xFFFFFFFFL;
						Yab &= 0xFFFFFFFFL;
						Yba &= 0xFFFFFFFFL;
						Ybb &= 0xFFFFFFFFL;
						Ymb0 &= 0xFFFFFFFFL;
						Ygb0 &= 0xFFFFFFFFL;

						st1 = Yaah * 6 + Yabh * 2 + Ymb0h;
						st2 = Ybah * 6 + Ybbh * 2 + Ygb0h;
						for (i = 0; i < NumberLength; i++) {
							Pr1 = Yaa * (Tmp1 = CalcAuxModInvMu[i]);
							Pr2 = Yab * (Tmp2 = CalcAuxModInvGamma[i]);
							Pr3 = Ymb0 * (Tmp3 = B[i]);
							Pr4 = (Pr1 & 0xFFFFFFFFL) + (Pr2 & 0xFFFFFFFFL) + (Pr3 & 0xFFFFFFFFL) + Cy3;
							Pr5 = Yaa * (Tmp4 = CalcAuxModInvA[i]);
							Pr6 = Yab * (Tmp5 = CalcAuxModInvB[i]);
							Pr7 = (Pr5 & 0xFFFFFFFFL) + (Pr6 & 0xFFFFFFFFL) + Cy1;
							switch (st1) {
							case -9:
								Cy3 = -Tmp1 - Tmp2 - Tmp3;
								Cy1 = -Tmp4 - Tmp5;
								break;
							case -8:
								Cy3 = -Tmp1 - Tmp2;
								Cy1 = -Tmp4 - Tmp5;
								break;
							case -7:
								Cy3 = -Tmp1 - Tmp3;
								Cy1 = -Tmp4;
								break;
							case -6:
								Cy3 = -Tmp1;
								Cy1 = -Tmp4;
								break;
							case -5:
								Cy3 = -Tmp1 + Tmp2 - Tmp3;
								Cy1 = -Tmp4 + Tmp5;
								break;
							case -4:
								Cy3 = -Tmp1 + Tmp2;
								Cy1 = -Tmp4 + Tmp5;
								break;
							case -3:
								Cy3 = -Tmp2 - Tmp3;
								Cy1 = -Tmp5;
								break;
							case -2:
								Cy3 = -Tmp2;
								Cy1 = -Tmp5;
								break;
							case -1:
								Cy3 = -Tmp3;
								Cy1 = 0;
								break;
							case 0:
								Cy3 = 0;
								Cy1 = 0;
								break;
							case 1:
								Cy3 = Tmp2 - Tmp3;
								Cy1 = Tmp5;
								break;
							case 2:
								Cy3 = Tmp2;
								Cy1 = Tmp5;
								break;
							case 3:
								Cy3 = Tmp1 - Tmp2 - Tmp3;
								Cy1 = Tmp4 - Tmp5;
								break;
							case 4:
								Cy3 = Tmp1 - Tmp2;
								Cy1 = Tmp4 - Tmp5;
								break;
							case 5:
								Cy3 = Tmp1 - Tmp3;
								Cy1 = Tmp4;
								break;
							case 6:
								Cy3 = Tmp1;
								Cy1 = Tmp4;
								break;
							case 7:
								Cy3 = Tmp1 + Tmp2 - Tmp3;
								Cy1 = Tmp4 + Tmp5;
								break;
							case 8:
								Cy3 = Tmp1 + Tmp2;
								Cy1 = Tmp4 + Tmp5;
								break;
							}
							Cy3 += (Pr1 >>> 32) + (Pr2 >>> 32) + (Pr3 >>> 32) + (Pr4 >> 32);
							Cy1 += (Pr5 >>> 32) + (Pr6 >>> 32) + (Pr7 >> 32);
							if (i > 0) {
								CalcAuxModInvMu[i - 1] = Pr4 & 0xFFFFFFFFL;
								CalcAuxModInvA[i - 1] = Pr7 & 0xFFFFFFFFL;
							}
							Pr1 = Yba * Tmp1;
							Pr2 = Ybb * Tmp2;
							Pr3 = Ygb0 * Tmp3;
							Pr4 = (Pr1 & 0xFFFFFFFFL) + (Pr2 & 0xFFFFFFFFL) + (Pr3 & 0xFFFFFFFFL) + Cy4;
							Pr5 = Yba * Tmp4;
							Pr6 = Ybb * Tmp5;
							Pr7 = (Pr5 & 0xFFFFFFFFL) + (Pr6 & 0xFFFFFFFFL) + Cy2;
							switch (st2) {
							case -9:
								Cy4 = -Tmp1 - Tmp2 - Tmp3;
								Cy2 = -Tmp4 - Tmp5;
								break;
							case -8:
								Cy4 = -Tmp1 - Tmp2;
								Cy2 = -Tmp4 - Tmp5;
								break;
							case -7:
								Cy4 = -Tmp1 - Tmp3;
								Cy2 = -Tmp4;
								break;
							case -6:
								Cy4 = -Tmp1;
								Cy2 = -Tmp4;
								break;
							case -5:
								Cy4 = -Tmp1 + Tmp2 - Tmp3;
								Cy2 = -Tmp4 + Tmp5;
								break;
							case -4:
								Cy4 = -Tmp1 + Tmp2;
								Cy2 = -Tmp4 + Tmp5;
								break;
							case -3:
								Cy4 = -Tmp2 - Tmp3;
								Cy2 = -Tmp5;
								break;
							case -2:
								Cy4 = -Tmp2;
								Cy2 = -Tmp5;
								break;
							case -1:
								Cy4 = -Tmp3;
								Cy2 = 0;
								break;
							case 0:
								Cy4 = 0;
								Cy2 = 0;
								break;
							case 1:
								Cy4 = Tmp2 - Tmp3;
								Cy2 = Tmp5;
								break;
							case 2:
								Cy4 = Tmp2;
								Cy2 = Tmp5;
								break;
							case 3:
								Cy4 = Tmp1 - Tmp2 - Tmp3;
								Cy2 = Tmp4 - Tmp5;
								break;
							case 4:
								Cy4 = Tmp1 - Tmp2;
								Cy2 = Tmp4 - Tmp5;
								break;
							case 5:
								Cy4 = Tmp1 - Tmp3;
								Cy2 = Tmp4;
								break;
							case 6:
								Cy4 = Tmp1;
								Cy2 = Tmp4;
								break;
							case 7:
								Cy4 = Tmp1 + Tmp2 - Tmp3;
								Cy2 = Tmp4 + Tmp5;
								break;
							case 8:
								Cy4 = Tmp1 + Tmp2;
								Cy2 = Tmp4 + Tmp5;
								break;
							}
							Cy4 += (Pr1 >>> 32) + (Pr2 >>> 32) + (Pr3 >>> 32) + (Pr4 >> 32);
							Cy2 += (Pr5 >>> 32) + (Pr6 >>> 32) + (Pr7 >> 32);
							if (i > 0) {
								CalcAuxModInvGamma[i - 1] = Pr4 & 0xFFFFFFFFL;
								CalcAuxModInvB[i - 1] = Pr7 & 0xFFFFFFFFL;
							}
						}

						if ((int) CalcAuxModInvA[i - 1] < 0) {
							Cy1 -= Yaa;
							Cy2 -= Yba;
						}
						if ((int) CalcAuxModInvB[i - 1] < 0) {
							Cy1 -= Yab;
							Cy2 -= Ybb;
						}
						if ((int) CalcAuxModInvMu[i - 1] < 0) {
							Cy3 -= Yaa;
							Cy4 -= Yba;
						}
						if ((int) CalcAuxModInvGamma[i - 1] < 0) {
							Cy3 -= Yab;
							Cy4 -= Ybb;
						}
						CalcAuxModInvA[i - 1] = Cy1 & 0xFFFFFFFFL;
						CalcAuxModInvB[i - 1] = Cy2 & 0xFFFFFFFFL;
						CalcAuxModInvMu[i - 1] = Cy3 & 0xFFFFFFFFL;
						CalcAuxModInvGamma[i - 1] = Cy4 & 0xFFFFFFFFL;
						continue outer_loop;
					}
					Bl >>= 1;
					Dif++;
					E++;
					T1++;
				}
				/* end while */
				Iaa <<= T1;
				Iab <<= T1;
				if (Dif >= 0) {
					Dif = -Dif;
					if (((Al + Bl) & 3) == 0) {
						T1 = Iba;
						Iba += Iaa;
						Iaa = T1;
						T1 = Ibb;
						Ibb += Iab;
						Iab = T1;
						T1 = Bl;
						Bl += Al;
						Al = T1;
					} else {
						T1 = Iba;
						Iba -= Iaa;
						Iaa = T1;
						T1 = Ibb;
						Ibb -= Iab;
						Iab = T1;
						T1 = Bl;
						Bl -= Al;
						Al = T1;
					}
				} else {
					if (((Al + Bl) & 3) == 0) {
						Iba += Iaa;
						Ibb += Iab;
						Bl += Al;
					} else {
						Iba -= Iaa;
						Ibb -= Iab;
						Bl -= Al;
					}
				}
				Dif--;
			} while (true);
		} while (true);
		if (CalcAuxModInvA[0] != 1) {
			SubtractBigNbr32(B, CalcAuxModInvMu, CalcAuxModInvMu);
		}
		if ((int) CalcAuxModInvMu[i = NumberLength - 1] < 0) {
			AddBigNbr32(B, CalcAuxModInvMu, CalcAuxModInvMu);
		}
		for (; i >= 0; i--) {
			if (B[i] != CalcAuxModInvMu[i])
				break;
		}
		if (i < 0 || B[i] < CalcAuxModInvMu[i]) { // If B < Mu
			SubtractBigNbr32(CalcAuxModInvMu, B, CalcAuxModInvMu); // Mu <- Mu - B
		}
		Convert32To31Bits(CalcAuxModInvMu, inv);
	}

	private void MultBigNbrByLongModN(long[] Nbr1, long Nbr2, long[] Prod) {
		int NumberLength = this.NumberLength;
		long MaxUInt = 0x7FFFFFFFl;
		long Pr;
		int j;

		Pr = 0;
		for (j = 0; j < NumberLength; j++) {
			Pr = (Pr >>> 31) + Nbr2 * Nbr1[j];
			Prod[j] = Pr & MaxUInt;
		}
		Prod[j] = (Pr >>> 31);
		AdjustModN(Prod);
	}

	private void MultBigNbrModN(long[] Nbr1, long[] Nbr2, long[] Prod) {
		int NumberLength = this.NumberLength;
		long MaxUInt = 0x7FFFFFFFl;
		int i, j;
		long Pr, Nbr;

		i = NumberLength;
		do {
			Prod[--i] = 0;
		} while (i > 0);
		i = NumberLength;
		do {
			Nbr = Nbr1[--i];
			j = NumberLength;
			do {
				Prod[j] = Prod[j - 1];
				j--;
			} while (j > 0);
			Prod[0] = 0;
			Pr = 0;
			for (j = 0; j < NumberLength; j++) {
				Pr = (Pr >>> 31) + Nbr * Nbr2[j] + Prod[j];
				Prod[j] = Pr & MaxUInt;
			}
			Prod[j] += (Pr >>> 31);
			AdjustModN(Prod);
		} while (i > 0);
	}

	/**
	 * Computes nP from P=(x:z) and puts the result in (x:z). Assumes n>2.
	 * 
	 * @param n
	 * @param x
	 * @param z
	 * @param xT
	 * @param zT
	 * @param xT2
	 * @param zT2
	 */
	private void prac(int n, long[] x, long[] z, long[] xT, long[] zT, long[] xT2, long[] zT2) {
		int d, e, r, i;
		long[] t;
		long[] xA = x, zA = z;
		long[] xB = fieldAux1, zB = fieldAux2;
		long[] xC = fieldAux3, zC = fieldAux4;

		/* chooses the best value of v */
		r = lucas_cost(n, v[0]);
		i = 0;
		for (d = 1; d < 10; d++) {
			e = lucas_cost(n, v[d]);
			if (e < r) {
				r = e;
				i = d;
			}
		}
		d = n;
		r = (int) (d / v[i] + 0.5);
		/* first iteration always begins by Condition 3, then a swap */
		d = n - r;
		e = 2 * r - n;
		System.arraycopy(xA, 0, xB, 0, NumberLength); // B = A
		System.arraycopy(zA, 0, zB, 0, NumberLength);
		System.arraycopy(xA, 0, xC, 0, NumberLength); // C = A
		System.arraycopy(zA, 0, zC, 0, NumberLength);
		duplicate(xA, zA, xA, zA); /* A=2*A */
		while (d != e) {
			if (d < e) {
				r = d;
				d = e;
				e = r;
				t = xA;
				xA = xB;
				xB = t;
				t = zA;
				zA = zB;
				zB = t;
			}
			/* do the first line of Table 4 whose condition qualifies */
			if (4 * d <= 5 * e && ((d + e) % 3) == 0) { /* condition 1 */
				r = (2 * d - e) / 3;
				e = (2 * e - d) / 3;
				d = r;
				add3(xT, zT, xA, zA, xB, zB, xC, zC); /* T = f(A,B,C) */
				add3(xT2, zT2, xT, zT, xA, zA, xB, zB); /* T2 = f(T,A,B) */
				add3(xB, zB, xB, zB, xT, zT, xA, zA); /* B = f(B,T,A) */
				t = xA;
				xA = xT2;
				xT2 = t;
				t = zA;
				zA = zT2;
				zT2 = t; /* swap A and T2 */
			} else if (4 * d <= 5 * e && (d - e) % 6 == 0) { /* condition 2 */
				d = (d - e) / 2;
				add3(xB, zB, xA, zA, xB, zB, xC, zC); /* B = f(A,B,C) */
				duplicate(xA, zA, xA, zA); /* A = 2*A */
			} else if (d <= (4 * e)) { /* condition 3 */
				d -= e;
				add3(xT, zT, xB, zB, xA, zA, xC, zC); /* T = f(B,A,C) */
				t = xB;
				xB = xT;
				xT = xC;
				xC = t;
				t = zB;
				zB = zT;
				zT = zC;
				zC = t; /* circular permutation (B,T,C) */
			} else if ((d + e) % 2 == 0) { /* condition 4 */
				d = (d - e) / 2;
				add3(xB, zB, xB, zB, xA, zA, xC, zC); /* B = f(B,A,C) */
				duplicate(xA, zA, xA, zA); /* A = 2*A */
			} else if (d % 2 == 0) { /* condition 5 */
				d /= 2;
				add3(xC, zC, xC, zC, xA, zA, xB, zB); /* C = f(C,A,B) */
				duplicate(xA, zA, xA, zA); /* A = 2*A */
			} else if (d % 3 == 0) { /* condition 6 */
				d = d / 3 - e;
				duplicate(xT, zT, xA, zA); /* T1 = 2*A */
				add3(xT2, zT2, xA, zA, xB, zB, xC, zC); /* T2 = f(A,B,C) */
				add3(xA, zA, xT, zT, xA, zA, xA, zA); /* A = f(T1,A,A) */
				add3(xT, zT, xT, zT, xT2, zT2, xC, zC); /* T1 = f(T1,T2,C) */
				t = xC;
				xC = xB;
				xB = xT;
				xT = t;
				t = zC;
				zC = zB;
				zB = zT;
				zT = t; /* circular permutation (C,B,T) */
			} else if ((d + e) % 3 == 0) { /* condition 7 */
				d = (d - 2 * e) / 3;
				add3(xT, zT, xA, zA, xB, zB, xC, zC); /* T1 = f(A,B,C) */
				add3(xB, zB, xT, zT, xA, zA, xB, zB); /* B = f(T1,A,B) */
				duplicate(xT, zT, xA, zA);
				add3(xA, zA, xA, zA, xT, zT, xA, zA); /* A = 3*A */
			} else if ((d - e) % 3 == 0) { /* condition 8 */
				d = (d - e) / 3;
				add3(xT, zT, xA, zA, xB, zB, xC, zC); /* T1 = f(A,B,C) */
				add3(xC, zC, xC, zC, xA, zA, xB, zB); /* C = f(A,C,B) */
				t = xB;
				xB = xT;
				xT = t;
				t = zB;
				zB = zT;
				zT = t; /* swap B and T */
				duplicate(xT, zT, xA, zA);
				add3(xA, zA, xA, zA, xT, zT, xA, zA); /* A = 3*A */
			} else if (e % 2 == 0) { /* condition 9 */
				e /= 2;
				add3(xC, zC, xC, zC, xB, zB, xA, zA); /* C = f(C,B,A) */
				duplicate(xB, zB, xB, zB); /* B = 2*B */
			}
		}
		add3(x, z, xA, zA, xB, zB, xC, zC);
	}

	private void SubtractBigNbr(long[] Nbr1, long[] Nbr2, long[] Diff) {
		int NumberLength = this.NumberLength;
		long Cy = 0;
		for (int i = 0; i < NumberLength; i++) {
			Cy = (Cy >> 31) + Nbr1[i] - Nbr2[i];
			Diff[i] = Cy & 0x7FFFFFFFl;
		}
	}

	private void SubtractBigNbr32(long[] Nbr1, long[] Nbr2, long[] Diff) {
		int NumberLength = this.NumberLength;
		long Cy = 0;
		for (int i = 0; i < NumberLength; i++) {
			Cy = (Cy >> 32) + Nbr1[i] - Nbr2[i];
			Diff[i] = Cy & 0xFFFFFFFFl;
		}
	}

	private void SubtractBigNbrModN(long[] Nbr1, long[] Nbr2, long[] Diff) {
		int NumberLength = this.NumberLength;
		long MaxUInt = 0x7FFFFFFFl; // Integer.MAX_VALUE
		long Cy = 0;
		int i;

		for (i = 0; i < NumberLength; i++) {
			Cy = (Cy >> 31) + Nbr1[i] - Nbr2[i];
			Diff[i] = Cy & MaxUInt;
		}
		if (Cy < 0) {
			Cy = 0;
			for (i = 0; i < NumberLength; i++) {
				Cy = (Cy >> 31) + Diff[i] + TestNbr[i];
				Diff[i] = Cy & MaxUInt;
			}
		}
	}

	public static void main(String[] args) {
		ConfigUtil.initProject();
		BigInteger[] testNums = new BigInteger[] {
				// easy for ECM
				new BigInteger("8225267468394993133669189614204532935183709603155231863020477010700542265332938919716662623"),
				
				// incomplete result, some unfactored rest is returned
				new BigInteger("101546450935661953908994991437690198927080333663460351836152986526126114727314353555755712261904130976988029406423152881932996637460315302992884162068350429"),
				
				// incomplete result, the factor map contains a composite
				new BigInteger("1593332576170570774181606244493046197050984933692181475920784855223341")
				// = 17 * 1210508704285703 * 2568160569265616473 * 30148619026320753545829271787156467
				// but ECM fails to factor 3108780723099354807613175415185519 = 1210508704285703 * 2568160569265616473
				// with the maximum number of curves
				
				// very hard for ECM, better suited for SIQS
				//new BigInteger("1794577685365897117833870712928656282041295031283603412289229185967719140138841093599"),
				// = 42181796536350966453737572957846241893933 * 42543889372264778301966140913837516662044603
		};
		
		EllipticCurveMethod ecm = new EllipticCurveMethod();
		
		long t0, t1;
		t0 = System.currentTimeMillis();
		for (BigInteger N : testNums) {
			SortedMap<BigInteger, Integer> primeFactors = new TreeMap<>();
			SortedMap<BigInteger, Integer> unfactoredComposites = ecm.factorize(N, primeFactors);
			if (!unfactoredComposites.isEmpty()) {
				LOG.debug("N = " + N + " = " + primeFactors + " * " + unfactoredComposites);
			} else {
				LOG.debug("N = " + N + " = " + primeFactors);
			}
		}
		t1 = System.currentTimeMillis();
		LOG.info("Test suite took " + (t1-t0) + "ms");
	}
}
