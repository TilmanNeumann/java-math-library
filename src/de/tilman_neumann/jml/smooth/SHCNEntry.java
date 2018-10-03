package de.tilman_neumann.jml.smooth;

import static de.tilman_neumann.jml.base.BigIntConstants.*;

import java.math.BigInteger;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.primes.probable.BPSWTest;

/**
 * A superior highly composite number (SHCN), together with some information that was necessary to compute it.
 * @author Tilman Neumann
 * @since 2015-04-10
 */
public class SHCNEntry {
	private static final Logger LOG = Logger.getLogger(SHCNEntry.class);
	private static final boolean DEBUG = false;

	private double x;
	private BigInteger shcn;
	private ArrayList<BigInteger> primes;
	private ArrayList<Integer> exponents;
	/** the sum of prime factor exponents is the sequence element number of SHCNs */
	private int exponentSum;

	private static final BPSWTest bpsw = new BPSWTest();

	// private, use factory method computeSHCN(x)
	private SHCNEntry(double x) {
		this.x = x;
		this.shcn = ONE;
		this.primes = new ArrayList<BigInteger>();
		this.exponents = new ArrayList<Integer>();
		this.exponentSum = 0;
	}

	private void add(BigInteger prime, int exponent) {
		primes.add(prime);
		exponents.add(exponent);
		exponentSum += exponent;
		shcn = shcn.multiply(prime.pow(exponent));
	}

	/**
	 * Compute exponent of prime p in s(x) by e_p(x) = lower( 1 / (x.th root(p) - 1)).
	 * @param x
	 * @param p
	 * @return exponent
	 */
	private static int computeExponent(double x, BigInteger p) {
		// compute rootExpr = x.th root(p) = p^(1/x):
		// Unfortunately there is no BigInteger or BigDecimal pow function with floating point arguments in Java,
		// so we have to approximate it with doubles. But this is no problem because a slightly different x
		// will give the same SHCN(s) !!
		double rootExpr = Math.pow(p.doubleValue(), 1/x);
		return (int) Math.floor(1/(rootExpr-1));
	}

	/**
	 * Compute SHCN(x), where x is a positive real number.
	 * @param x
	 * @return SHCN(x)
	 */
	public static SHCNEntry computeSHCN(double x) {
		double twoPowx = Math.pow(2, x);
		SHCNEntry result = new SHCNEntry(x);
		BigInteger p = TWO;
		while (p.doubleValue() <= twoPowx) {
			int exponent = computeExponent(x, p);
			if (DEBUG) LOG.debug("    x=" + x + ", p=" + p + ", exponent=" + exponent);
			result.add(p, exponent);
			p = bpsw.nextProbablePrime(p);
		}
		return result;
	}
	
	public double getX() {
		return x;
	}
	
	public BigInteger getSHCN() {
		return shcn;
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
}
