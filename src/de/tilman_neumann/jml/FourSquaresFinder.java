package de.tilman_neumann.jml;

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
 * An implementation of the algorithm of Pollack and Trevino that finds the four squares representation of an odd number n
 * in O((log n)^2 (log log n)) given that ERH (the extended Riemann hypothesis) holds.
 * 
 * @author Till
 * 
 * @see Pollack, Trevinho: "Finding the four squares in Lagrange’s theorem", Integers: 18A (2018)
 * @see http://campus.lakeforest.edu/trevino/finding4squares.pdf
 */
public class FourSquaresFinder {
	private static final Logger LOG = Logger.getLogger(FourSquaresFinder.class);

	private static final boolean DEBUG = true;
	
	private static final AutoExpandingPrimesArray PRIMES = AutoExpandingPrimesArray.get().ensurePrimeCount(1000); // modest initialization

	private static final Random RNG = new Random(43);
	
	/** The input number */
	private BigInteger n;
	
	/** The bases of the 4 squares representation that was found. */
	private BigInteger X=null, Y=null, Z=null, W=null;
	
	/** The number of random iterations that were needed. */
	private int numberOfIterations = 0;
	
	/**
	 * Construct a finder to find a four square representation of odd n.
	 * 
	 * @param n an odd integer > 20
	 */
	public FourSquaresFinder(BigInteger n) {
		if (!n.testBit(0)) {
			throw new IllegalArgumentException("n = " + n + " is not odd");
		}
		if (n.compareTo(I_20) <= 0) {
			throw new IllegalArgumentException("4 squares finder is still missing implementation for n <= 20 but n is " + n);
		}
		this.n = n;
	}
	
	public void find() {
		// (1) [Precomputation] Determine the primes not exceeding log n and compute their product M.
		int pmax = (int) Math.ceil(n.bitLength() * Math.log(2.0));
		BigInteger M = I_1;
		for (int i=0; ; i++) {
			int p = PRIMES.getPrime(i); // auto-expanding
			if (p > pmax) break;
			
			M = M.multiply(BigInteger.valueOf(p));
		}
		assertEquals(false, M.testBit(0)); // M is even
		BigInteger Mn = M.multiply(n);
		
		// (2) [Random trials]
		BigInteger p, s; // output of step 2
		BigInteger nPow5 = n.pow(5);
		int nPow5Bits = nPow5.bitLength();
		BigInteger k;
		for(numberOfIterations = 1; ; numberOfIterations++) { // iteration loop
			// Choose an odd number k < n^5 at random
			k = new BigInteger(nPow5Bits, RNG);
			if (!k.testBit(0)) k = k.add(I_1); // make k odd
			if (k.compareTo(nPow5) >= 0) continue; // XXX maybe it'ld be faster to ignore that some numbers are bigger than wanted 
			
			// let p = Mnk - 1
			p = Mn.multiply(k).subtract(I_1);
			// (Notice that p == 1 (mod 4), since 2 || M and n, k are odd.)
			if (DEBUG) assertEquals(I_1, p.and(I_3)); // p == 1 (mod 4)
			
			// choose random u ∈ [1, p-1]
			BigInteger u;
			do {
				u = new BigInteger(p.bitLength(), RNG).add(I_1);
			} while (u.compareTo(p) >= 0);
			
			// compute s = u^((p-1)/4) mod p
			BigInteger pm1 = p.subtract(I_1);
			s = u.modPow(pm1.shiftRight(2), p);
			
			// Test if s^2 == -1 (mod p). If so, continue to the next step. Otherwise, restart this step.
			BigInteger sSquare = s.multiply(s);
			if (sSquare.mod(p).equals(pm1)) break;
		}
		
		//(3) [Denouement] Compute A+Bi := gcd(s+i, p). Then compute gcrd(A+Bi+j, n), normalized to have integer components.
		//    Write this gcrd as X + Yi + Zj + Wk, and output the representation n = X^2 + Y^2 + Z^2 + W^2.
		GaussianInteger gcd = new GaussianInteger(s, I_1).gcd(new GaussianInteger(p, I_0));
		BigInteger A = gcd.realPart();
		BigInteger B = gcd.imaginaryPart();
		HurwitzQuaternion gcrd = new HurwitzQuaternion(A, B, I_1, I_0, true).rightGcd(new HurwitzQuaternion(n, true));
		X = gcrd.getX();
		Y = gcrd.getY();
		Z = gcrd.getZ();
		W = gcrd.getW();
		if (DEBUG) assertEquals(n, X.multiply(X).add(Y.multiply(Y)).add(Z.multiply(Z)).add(W.multiply(W)));
		
		// done
	}
	
	public BigInteger[] getSquareBases() {
		return new BigInteger[] {X, Y, Z, W};
	}
	
	public int getNumberOfIterations() {
		return numberOfIterations;
	}
	
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
		    	FourSquaresFinder fsf = new FourSquaresFinder(N);
		    	fsf.find();
		    	long duration = timer.totalRuntime(); 
		    	LOG.info("Found 4 squares representation " + N + " = " + fsf.X.abs() + "^2 + " + fsf.Y.abs() + "^2 + " + fsf.Z.abs() + "^2 + " + fsf.W.abs() + "^2 in " + fsf.getNumberOfIterations() + " iterations / " + duration + "ms");
			} catch (Exception ex) {
				LOG.error("Error " + ex, ex);
			}
		}
	}
}
