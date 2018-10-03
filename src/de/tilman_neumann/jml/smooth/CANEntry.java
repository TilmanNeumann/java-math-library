package de.tilman_neumann.jml.smooth;

import static de.tilman_neumann.jml.base.BigIntConstants.*;

import java.math.BigInteger;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.primes.probable.BPSWTest;
import de.tilman_neumann.jml.precision.Magnitude;
import de.tilman_neumann.util.ConfigUtil;

/**
 * A colossally abundant number (CAN), together with some information that was necessary to compute it.
 * @author Tilman Neumann
 * @since 2015-04-11
 */
public class CANEntry {
	private static final Logger LOG = Logger.getLogger(CANEntry.class);
	private static final boolean DEBUG = false;

	private double epsilon;
	private BigInteger can;
	private ArrayList<BigInteger> primes;
	private ArrayList<Integer> exponents;
	/** the sum of prime factor exponents is the sequence element number of CANs */
	private int exponentSum;

	private static final BPSWTest bpsw = new BPSWTest();

	// private, use factory method computeCAN(epsilon)
	private CANEntry(double epsilon) {
		this.epsilon = epsilon;
		this.can = ONE;
		this.primes = new ArrayList<BigInteger>();
		this.exponents = new ArrayList<Integer>();
		this.exponentSum = 0;
	}

	private void add(BigInteger prime, int exponent) {
		primes.add(prime);
		exponents.add(exponent);
		exponentSum += exponent;
		can = can.multiply(prime.pow(exponent));
	}

	/**
	 * Compute exponent of prime p in CAN(epsilon).
	 * @param epsilon
	 * @param p
	 * @return exponent
	 */
	private static int computeExponent(double epsilon, BigInteger p) {
		double powTerm1 = Math.pow(p.doubleValue(), 1+epsilon);
		double logTerm1 = Math.log(powTerm1 - 1);
		double powTerm2 = Math.pow(p.doubleValue(), epsilon);
		double logTerm2 = Math.log(powTerm2 - 1);
		double logTerm3 = Math.log(p.doubleValue());
		double totalLogTerm = (logTerm1 - logTerm2) / logTerm3;
		int e = (int) Math.floor(totalLogTerm) - 1;
		return e;
	}

	/**
	 * Compute CAN(epsilon), where epsilon is a positive real number.
	 * @param epsilon
	 * @return CAN(epsilon)
	 */
	public static CANEntry computeCAN(double epsilon) {
		CANEntry result = new CANEntry(epsilon);
		BigInteger p = TWO;
		while (true) {
			int exponent = computeExponent(epsilon, p);
			if (exponent==0) break;
			if (DEBUG) LOG.debug("    epsilon=" + epsilon + ", p=" + p + ", exponent=" + exponent);
			result.add(p, exponent);
			p = bpsw.nextProbablePrime(p);
		}
		return result;
	}
	
	public double getEpsilon() {
		return epsilon;
	}
	
	public BigInteger getCAN() {
		return can;
	}
	
	public int getExponentSum() {
		return exponentSum;
	}
	
	public ArrayList<BigInteger> getPrimes() {
		return primes;
	}
	
	public ArrayList<Integer> getExponents() {
		return exponents;
	}

	/**
	 * Test.
	 * @param args ignored
	 */
	public static void main(String[] args) {
    	ConfigUtil.initProject();
		for (double epsilon=1.0; epsilon>0; epsilon-=0.05) {
			CANEntry entry = computeCAN(epsilon);
			BigInteger can = entry.getCAN();
			int digits = Magnitude.of(can);
			LOG.info("n=" + entry.getExponentSum() + ": epsilon=" + epsilon + ", " + digits + " digits CAN = " + entry.getCAN());
		}
		// Result: epsilon=0.5 gives first CAN = 2
	}
}
