package de.tilman_neumann.jml.factor.base;

import java.math.BigInteger;

import de.tilman_neumann.util.SortedMultiset;

public class FactorResult {

	/** factors that are at least probable prime */
	public SortedMultiset<BigInteger> primeFactors;
	
	/** factors whose primeness has not been checked yet */
	public SortedMultiset<BigInteger> untestedFactors;
	
	/** factors that are certainly composite */
	public SortedMultiset<BigInteger> compositeFactors;
	
	/** the smallest factor that could occur in the unfactored rest, e.g. because smaller factors have been excluded by trial division */
	public long smallestPossibleFactor;
	
	/**
	 * Full constructor.
	 * @param primeFactors prime factors found
	 * @param untestedFactors factors found but not investigated for primeness
	 * @param compositeFactors factors found that are certainly composite
	 * @param smallestPossibleFactor the smallest factor that could occur in untestedFactors or compositeFactors
	 */
	public FactorResult(SortedMultiset<BigInteger> primeFactors, SortedMultiset<BigInteger> untestedFactors, SortedMultiset<BigInteger> compositeFactors, long smallestPossibleFactor) {
		this.primeFactors = primeFactors;
		this.untestedFactors = untestedFactors;
		this.compositeFactors = compositeFactors;
		this.smallestPossibleFactor = smallestPossibleFactor;
	}
	
	@Override
	public String toString() {
		return "primeFactors = " + primeFactors + ", untestedFactors = " + untestedFactors + ", compositeFactors = " + compositeFactors + ", smallestPossibleFactor = " + smallestPossibleFactor;
	}
}
