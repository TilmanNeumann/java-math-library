package de.tilman_neumann.jml.factor.base;

import java.math.BigInteger;

public class FactorArguments {
	/** The number to factor */
	public BigInteger N;
	
	/** The number of bits of N */
	public int NBits;

	/** The exponent of N */
	public int exp;
	
	/**
	 * Check for small factors before running the real algorithm? This makes sense in the general setup, but not if it is known that N is semiprime.
	 * Certain algorithms may ignore this flag.
	 */
	public boolean searchSmallFactors;

	/** the smallest factor that could occur, e.g. because smaller factors have been excluded by trial division */
	public long smallestPossibleFactor;
	
	/**
	 * Full constructor.
	 * @param N the number to factor
	 * @param exp the exponent of N
	 * @param searchSmallFactors explicitly look for small factors before applying the real algorithm;
	 * @param smallestPossibleFactor the smallest factor that could occur
	 * this parameter may be ignored by algorithms where it does not make sense
	 */
	public FactorArguments(BigInteger N, int exp, boolean searchSmallFactors, long smallestPossibleFactor) {
		this.N = N;
		this.NBits = N.bitLength();
		this.exp = exp;
		this.searchSmallFactors = searchSmallFactors;
		this.smallestPossibleFactor = smallestPossibleFactor;
	}
}
