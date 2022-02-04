/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2022 Tilman Neumann (www.tilman-neumann.de)
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
package de.tilman_neumann.jml.squareSums;

import static de.tilman_neumann.jml.base.BigIntConstants.*;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.Random;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.base.GaussianInteger;
import de.tilman_neumann.jml.base.HurwitzQuaternion;
import de.tilman_neumann.jml.primes.exact.AutoExpandingPrimesArray;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.Timer;

/**
 * An implementation of the algorithm of Pollack and Treviño that finds some four squares representation of an odd number n
 * in O((log n)^2 * (log log n)^-1) given that ERH (the extended Riemann hypothesis) holds.
 * 
 * @author Tilman Neumann
 * 
 * @see Pollack, Treviño: "Finding the four squares in Lagrange’s theorem", Integers: 18A (2018)
 * @see http://campus.lakeforest.edu/trevino/finding4squares.pdf
 */
public class FourSquaresFinder {
	private static final Logger LOG = Logger.getLogger(FourSquaresFinder.class);

	private static final boolean DEBUG = false;
	
	private static final boolean ANALYZE = true;

	private static final AutoExpandingPrimesArray PRIMES = AutoExpandingPrimesArray.get().ensurePrimeCount(1000); // modest size initialization

	private static final Random RNG = new Random(43);
	
	/** The bases of the 4 squares representation that was found. */
	private BigInteger X=null, Y=null, Z=null, W=null;
	
	/** The number of random iterations that were needed. */
	private int numberOfIterations = 0;
	
	private Timer timer = new Timer();
	
	private long step1Duration, step2Duration, step3Duration;
	private long step2nPowDuration, step2kDuration, step2pDuration, step2uDuration, step2sDuration, step2sSquareDuration;
	
	/**
	 * Full constructor. One instance may be used for several four squares decompositions,
	 * the timings are accumulated.
	 */
	public FourSquaresFinder() {
		if (ANALYZE) {
			step1Duration = step2Duration = step3Duration = 0;
			step2nPowDuration = step2kDuration = step2pDuration = step2uDuration = step2sDuration = step2sSquareDuration = 0;
		}
	}
	
	/**
	 * Find a four square representation of odd n.
	 * @param n an odd integer > 20
	 */
	public void find(BigInteger n) {
		if (!n.testBit(0)) {
			throw new IllegalArgumentException("n = " + n + " is not odd");
		}
		if (n.compareTo(I_20) <= 0) {
			throw new IllegalArgumentException("4 squares finder is still missing implementation for n <= 20 but n is " + n);
		}
		if (ANALYZE) timer.capture();
		
		// (1) [Precomputation] Determine the primes not exceeding log n and compute their product M.
		int pmax = (int) Math.ceil(n.bitLength() * Math.log(2.0));
		BigInteger M = I_1;
		for (int i=0; ; i++) {
			int p = PRIMES.getPrime(i); // auto-expanding
			if (p > pmax) break;
			
			M = M.multiply(BigInteger.valueOf(p));
		}
		if (DEBUG) assertEquals(false, M.testBit(0)); // M is even
		BigInteger Mn = M.multiply(n);
		if (ANALYZE) step1Duration += timer.capture();
		
		// (2) [Random trials]
		BigInteger p, s; // output of step 2
		BigInteger nPow5 = n.pow(5);
		int nPow5Bits = nPow5.bitLength();
		if (ANALYZE) step2nPowDuration += timer.capture();
		
		for (numberOfIterations = 1; ; numberOfIterations++) { // iteration loop
			// Choose an odd number k < n^5 at random
			BigInteger k = new BigInteger(nPow5Bits, RNG);
			if (!k.testBit(0)) k = k.add(I_1); // make k odd
			if (k.compareTo(nPow5) >= 0) continue;
			if (ANALYZE) step2kDuration += timer.capture();

			// let p = Mnk - 1
			p = Mn.multiply(k).subtract(I_1);
			// (Notice that p == 1 (mod 4), since 2 || M and n, k are odd.)
			if (DEBUG) assertEquals(I_1, p.and(I_3)); // p == 1 (mod 4)
			if (ANALYZE) step2pDuration += timer.capture();

			// choose random u ∈ [1, p-1]
			BigInteger u;
			do {
				u = new BigInteger(p.bitLength(), RNG).add(I_1);
			} while (u.compareTo(p) >= 0);
			if (ANALYZE) step2uDuration += timer.capture();

			// compute s = u^((p-1)/4) mod p
			BigInteger pm1 = p.subtract(I_1);
			s = u.modPow(pm1.shiftRight(2), p); // this is the absolute performance bottleneck of the whole algorithm
			if (ANALYZE) step2sDuration += timer.capture();

			// Test if s^2 == -1 (mod p). If so, continue to the next step. Otherwise, restart this step.
			BigInteger sSquare = s.multiply(s);
			if (sSquare.mod(p).equals(pm1)) break;
			if (ANALYZE) step2sSquareDuration += timer.capture();
		}
		if (ANALYZE) {
			step2sSquareDuration += timer.capture();
			step2Duration += step2nPowDuration + step2kDuration + step2pDuration + step2uDuration + step2sDuration + step2sSquareDuration;
		}

		/* *****************************************************************************************************
		 * Notes on step 2:
		 * 
		 * s^2 = (u^((p-1)/4))^2 (mod p) = u^((p-1)/2) (mod p)
		 * So if p is prime, then by Euler's criterion we have s^2 = Legendre(u | p) (mod p).
		 * The algorithm finds most s^2 == -1 (mod p) for p that are prime.
		 * Since the modular power is by far the most expensive computation in this algorithm, one could think
		 * that "guarding" the modular power with a probable prime test could yield a performance improvement.
		 * But experiments showed that such an approach is slower;
		 * the solutions of s^2 == -1 (mod p) found by composite p seem to be important, too.
		 * 
		 * Consequently, the only way to improvement seems to speed up the modular power itself.
		 * *****************************************************************************************************/
		
		//(3) [Denouement] Compute A+Bi := gcd(s+i, p). Then compute gcrd(A+Bi+j, n), normalized to have integer components.
		//    Write this gcrd as X + Yi + Zj + Wk, and output the representation n = X^2 + Y^2 + Z^2 + W^2.
		GaussianInteger gcd = new GaussianInteger(s, I_1).gcd(new GaussianInteger(p, I_0));
		BigInteger A = gcd.realPart();
		BigInteger B = gcd.imaginaryPart();
		HurwitzQuaternion gcrd = new HurwitzQuaternion(A, B, I_1, I_0, true).rightGcd(new HurwitzQuaternion(n, true));
		if (ANALYZE) step3Duration += timer.capture();
		
		X = gcrd.getX().abs();
		Y = gcrd.getY().abs();
		Z = gcrd.getZ().abs();
		W = gcrd.getW().abs();
		if (DEBUG) assertEquals(n, X.multiply(X).add(Y.multiply(Y)).add(Z.multiply(Z)).add(W.multiply(W)));
		
		// done
	}
	
	public BigInteger[] getSquareBases() {
		return new BigInteger[] {X, Y, Z, W};
	}
	
	public int getNumberOfIterations() {
		return numberOfIterations;
	}
	
	public String getPhaseTimings() {
		return "step1=" + step1Duration + "ms, step2=" + step2Duration + "ms, step3=" + step3Duration + "ms";
	}
	
	public String getStep2Subtimings() {
		return "n^5=" + step2nPowDuration + "ms, k=" + step2kDuration + "ms, p=" + step2pDuration + "ms, u=" + step2uDuration + "ms, s=" + step2sDuration + "ms, s^2=" + step2sSquareDuration + "ms";
	}
	
	// Some test numbers:
	// RSA-100 = 1522605027922533360535618378132637429718068114961380688657908494580122963258952897654000350692006139
	// RSA-576 = 188198812920607963838697239461650439807163563379417382700763356422988859715234665485319060606504743045317388011303396716199692321205734031879550656996221305168759307650257059
	// RSA-768 = 1230186684530117755130494958384962720772853569595334792197322452151726400507263657518745202199786469389956474942774063845925192557326303453731548268507917026122142913461670429214311602221240479274737794080665351419597459856902143413
	// RSA-1024 = 135066410865995223349603216278805969938881475605667027524485143851526510604859533833940287150571909441798207282164471551373680419703964191743046496589274256239341020864383202110372958725762358509643110564073501508187510676594629205563685529475213500852879416377328533906109750544334999811150056977236890927563
	// RSA-1536 = 1847699703211741474306835620200164403018549338663410171471785774910651696711161249859337684305435744585616061544571794052229717732524660960646946071249623720442022269756756687378427562389508764678440933285157496578843415088475528298186726451339863364931908084671990431874381283363502795470282653297802934916155811881049844908319545009848393775227257052578591944993870073695755688436933812779613089230392569695253261620823676490316036551371447913932347169566988069
	// RSA-2048 = 25195908475657893494027183240048398571429282126204032027777137836043662020707595556264018525880784406918290641249515082189298559149176184502808489120072844992687392807287776735971418347270261896375014971824691165077613379859095700097330459748808428401797429100642458691817195118746121515172654632282216869987549182422433637259085141865462043576798423387184774447920739934236584823824281198163815010674810451660377306056201619676256133844143603833904414952634432190114657544454178424020924616515723350778707749817125772467962926386356373289912154831438167899885040445364023527381951378636564391212010397122822120720357 
	//
	// Sample test results (timings from single-threaded computation on a Ryzen 3900X):
	// RSA-100: 547 iterations, 3.2s
	// RSA-576: 1418 iterations, 34s
	// RSA-768: 201 iterations, 14s
	// RSA-1024: 410 iterations, 98s
	// RSA-1536: 1137 iterations, 522s
	// RSA-2048: > 90m
	public static void main(String[] args) {
    	ConfigUtil.initProject();
		while(true) {
			try {
				LOG.info("Please insert the number to decompose:");
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				String line = in.readLine();
				String input = line !=null ? line.trim() : "";
				//LOG.debug("input = >" + input + "<");
				BigInteger N = new BigInteger(input);
				LOG.info("Searching four squares of " + N + " (" + N.bitLength() + " bits)...");
				Timer timer = new Timer();
		    	FourSquaresFinder fsf = new FourSquaresFinder();
		    	fsf.find(N);
		    	long duration = timer.totalRuntime(); 
		    	LOG.info("4 squares representation " + N + " = " + fsf.X + "^2 + " + fsf.Y + "^2 + " + fsf.Z + "^2 + " + fsf.W + "^2 computed in " + fsf.getNumberOfIterations() + " iterations, " + duration + "ms");
		    	LOG.info("Phase timings: " + fsf.getPhaseTimings());
		    	LOG.info("Step 2 subtimings: " + fsf.getStep2Subtimings());
			} catch (Exception ex) {
				LOG.error("Error " + ex, ex);
			}
		}
	}
}
