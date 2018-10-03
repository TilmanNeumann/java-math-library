package de.tilman_neumann.jml.smooth;

import static de.tilman_neumann.jml.base.BigIntConstants.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import de.tilman_neumann.jml.Divisors;
import de.tilman_neumann.jml.base.BigDecimalMath;
import de.tilman_neumann.jml.base.BigRational;
import de.tilman_neumann.jml.precision.Scale;
import de.tilman_neumann.jml.roots.SqrtInt;
import de.tilman_neumann.jml.roots.SqrtReal;

/**
 * Collection of four smoothness measures:
 * composedness = d(n)/n
 * abundance = sigma(n)/n,
 * quadratic composedness = #(divisors(n) <= sqrt(n)) / sqrt(n),
 * quadratic abundance = sum(divisors(n) <= sqrt(n)) / sqrt(n)
 * 
 * @author Tilman Neumann
 * @since 2015-04-12
 */
public class SmoothnessMeasures {
	private static final Scale SCALE_5 = Scale.valueOf(5);

	// static pool to store smoothness measures
	// Warning: not thread-safe!
	private static final HashMap<BigInteger, SmoothnessMeasures> n_2_smoothnessMeasures = new HashMap<BigInteger, SmoothnessMeasures>();

	// basics
	private BigInteger[] isqrt;
	private BigDecimal sqrt;
	private SortedSet<BigInteger> smallDivisors;
	private TreeSet<BigInteger> allDivisors;
	private BigInteger smallDivisorsSum = ZERO;
	private BigInteger allDivisorsSum = ZERO; // sigma(n)
	private int smallDivisorsCount;
	private int allDivisorsCount; // d(n)
	
	// the measures
	private BigDecimal composedness;
	private BigDecimal abundance;
	private BigDecimal quadraticComposedness;
	private BigDecimal quadraticAbundance;

	/**
	 * private constructor, use factory method getSmoothnessMeasures(n).
	 */
	private SmoothnessMeasures(BigInteger n) {
		// basics
		smallDivisors = Divisors.getSmallDivisors(n);
		allDivisors = new TreeSet<BigInteger>(smallDivisors); // copy
		for (BigInteger smallDivisor : smallDivisors) {
			BigInteger complementaryDivisor = n.divide(smallDivisor); // exact
			allDivisors.add(complementaryDivisor); // since allDivisors is a set, sqrt(n) can not be added twice
			smallDivisorsSum = smallDivisorsSum.add(smallDivisor);
			allDivisorsSum = allDivisorsSum.add(smallDivisor);
			allDivisorsSum = allDivisorsSum.add(complementaryDivisor);
		}
		smallDivisorsCount = smallDivisors.size();
		allDivisorsCount = allDivisors.size();
		isqrt = SqrtInt.iSqrt(n);
		if (isqrt[0].equals(isqrt[1])) {
			// n is square -> remove sqrt once from allDivisorsSum
			allDivisorsSum = allDivisorsSum.subtract(isqrt[0]);
		}
		sqrt = SqrtReal.sqrt(new BigDecimal(n), SCALE_5);
		
		// the measures
		composedness = new BigRational(BigInteger.valueOf(allDivisorsCount), n).toBigDecimal(SCALE_5);
		abundance = new BigRational(allDivisorsSum, n).toBigDecimal(SCALE_5);
		quadraticComposedness = BigDecimalMath.divide(new BigDecimal(smallDivisorsCount), sqrt, SCALE_5);
		quadraticAbundance = BigDecimalMath.divide(new BigDecimal(smallDivisorsSum), sqrt, SCALE_5);
	}
	
	/**
	 * @return abundance, the most important smoothness measure (?).
	 */
	public BigDecimal getAbundance() {
		return abundance;
	}
	
	public String toString() {
		return "composed = " + composedness + ", abund = " + abundance + ", q.composed = " + quadraticComposedness + ", q.abund = " + quadraticAbundance;
	}
	
	/**
	 * @param s
	 * @return smoothness scores of s
	 */
	public static SmoothnessMeasures getSmoothnessMeasures(BigInteger s) {
		SmoothnessMeasures smoothnessMeasures = n_2_smoothnessMeasures.get(s);
		if (smoothnessMeasures==null) {
			smoothnessMeasures = new SmoothnessMeasures(s);
			n_2_smoothnessMeasures.put(s, smoothnessMeasures);
		}
		return smoothnessMeasures;
	}
}
