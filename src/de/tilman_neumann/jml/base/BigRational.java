package de.tilman_neumann.jml.base;

import java.lang.Number;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.precision.Precision;
import de.tilman_neumann.jml.precision.Scale;
import de.tilman_neumann.jml.base.BigDecimalMath;

/**
 * Big rational numbers with exact arithmetics.
 * @author Tilman Neumann
 */
public class BigRational extends Number implements Comparable<BigRational> {
	
	private static final long serialVersionUID = -578518708160143029L;
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(BigRational.class);
	
	// Constants -----------------------------------------------------------------------------
	
	public static final BigRational ZERO = new BigRational(BigInteger.ZERO);
	public static final BigRational ONE_HALF = new BigRational(BigInteger.ONE, BigIntConstants.TWO);
	public static final BigRational ONE = new BigRational(BigInteger.ONE);
	
	// Fields -----------------------------------------------------------------------------
	
	/** Numerator. */
	private BigInteger num = null;
	/** Denominator. */
	private BigInteger den = null;

	// Constructors -----------------------------------------------------------------------------
	
	/**
	 * Constructor for an integer.
	 * @param n Number in decimal representation.
	 */
	public BigRational(BigInteger n) {
		this(n, BigIntConstants.ONE);
	}
	
	/**
	 * Constructor for a rational number.
	 * @param num Numerator
	 * @param den Denominator
	 */
	public BigRational(BigInteger num, BigInteger den) {
		this.num = num;
		this.den = den;
	}
	
	// Basic Arithmetic -----------------------------------------------------------------------------
	
	/**
	 * Computes the sum of this and the argument.
	 * @param b Argument.
	 * @return sum
	 */
	public BigRational add(BigRational b) {
		BigInteger n = this.num.multiply(b.den).add(b.num.multiply(this.den));
		BigInteger d = this.den.multiply(b.den);
		return new BigRational(n, d);
	}
	
	/**
	 * Computes the subtraction of this and the argument.
	 * @param b Argument.
	 * @return subtraction
	 */
	public BigRational subtract(BigRational b) {
		BigInteger n = this.num.multiply(b.den).subtract(b.num.multiply(this.den));
		BigInteger d = this.den.multiply(b.den);
		return new BigRational(n, d);
	}
	
	/**
	 * Computes the product of this and the argument.
	 * @param b Argument.
	 * @return product
	 */
	public BigRational multiply(BigInteger b) {
		BigInteger n = this.num.multiply(b);
		return new BigRational(n, this.den);
	}
	
	/**
	 * Computes the product of this and the argument.
	 * @param b Argument.
	 * @return product
	 */
	public BigRational multiply(BigRational b) {
		BigInteger n = this.num.multiply(b.num);
		BigInteger d = this.den.multiply(b.den);
		return new BigRational(n, d);
	}
	
	/**
	 * Computes the fraction of this and the argument.
	 * @param b Argument.
	 * @return fraction
	 */
	public BigRational divide(BigInteger b) {
		BigInteger d = this.den.multiply(b);
		return new BigRational(this.num, d);
	}
	
	/**
	 * Computes the fraction of this and the argument.
	 * @param b Argument.
	 * @return fraction
	 */
	public BigRational divide(BigRational b) {
		BigInteger n = this.num.multiply(b.den);
		BigInteger d = this.den.multiply(b.num);
		return new BigRational(n, d);
	}

	/**
	 * @return absolut value
	 */
	public BigRational abs() {
		return new BigRational(this.num.abs(), this.den.abs());
	}
	
	/**
	 * @return copy of this with opposite sign
	 */
	public BigRational negate() {
		return new BigRational(this.num.negate(), this.den); // It is not required to copy the denominator because BigIntegers are inmutable
	}
	
	/**
	 * @return 1/this
	 */
	public BigRational reciprocal() {
		return new BigRational(this.den, this.num);
	}
	
	/**
	 * @return this with gcd of numerator and denominator reduced to 1.
	 */
	public BigRational normalize() {
		if (den.signum()<0) {
			// make denominator positive always
			num = num.negate();
			den = den.negate();
		}
		BigInteger gcd = this.num.gcd(this.den);
		if (gcd!=null && gcd.compareTo(BigInteger.ONE)>0) {
			return new BigRational(this.num.divide(gcd), this.den.divide(gcd));
		}
		return this;
	}
	
	public BigRational expandTo(BigInteger newDenominator) {
		BigInteger multiplier = newDenominator.divide(this.den);
		return new BigRational(this.num.multiply(multiplier), this.den.multiply(multiplier));
	}

	// Informations about this number ---------------------------------------------------

	/**
	 * @return The numerator of this number.
	 */
	public BigInteger getNumerator() {
		return this.num;
	}
	
	/**
	 * @return The denominator of this number.
	 */
	public BigInteger getDenominator() {
		return this.den;
	}
	
	/**
	 * @return <0/0/>0 if this is negative/zero/positive.
	 */
	public int signum() {
		return this.num.signum()*this.den.signum();
	}

	/**
	 * @return the nearest smaller-or-equal integer value.
	 */
	public BigInteger floorInt() {
		// when the division is exact then we return the quotient.
		// when the division is not exact we drop the rest -> we also return the quotient!
		return num.divideAndRemainder(den)[0];
	}

	/**
	 * @return the nearest bigger-or-equal integer value.
	 */
	public BigInteger ceilInt() {
		BigInteger[] div = num.divideAndRemainder(den);
		if (div[1].equals(BigInteger.ZERO)) {
			// no rest, the division was exact and the quotient is the ceil() value.
			return div[0];
		}
		// division was not exact, return quotient+1
		return div[0].add(BigInteger.ONE);
	}
	
	// Comparison --------------------------------------------------------------------
	
	@Override
	public int compareTo(BigRational other) {
		// simple and correct :)
		BigRational diff = this.subtract(other);
		return diff.signum();
	}
	
	public int compareTo(BigInteger other) {
		return this.compareTo(new BigRational(other));
	}
	
	/**
	 * equals() must be consistent with compareTo()...
	 */
	public boolean equals(Object o) {
		if (o==null) return false;
		if (o instanceof BigRational) {
			BigRational other = (BigRational) o;
			return (this.num.multiply(other.den).equals(other.num.multiply(this.den)));
		}
		if (o instanceof BigInteger) {
			BigInteger other = (BigInteger) o;
			return (this.num.equals(other.multiply(this.den)));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((den == null) ? 0 : den.hashCode());
		result = prime * result + ((num == null) ? 0 : num.hashCode());
		return result;
	}
	
	// Conversion --------------------------------------------------------------------

	/**
	 * @return this as a byte (rounding may be necessary)
	 */
	@Override
	public byte byteValue() {
		return this.toBigDecimal(Scale.valueOf(0)).byteValue();
	}

	/**
	 * @return this as an int (rounding may be necessary)
	 */
	@Override
	public int intValue() {
		return this.toBigDecimal(Scale.valueOf(0)).intValue();
	}

	/**
	 * @return this as a long integer (rounding may be necessary)
	 */
	@Override
	public long longValue() {
		return this.toBigDecimal(Scale.valueOf(0)).longValue();
	}
	
	/**
	 * @return this as a float (rounding may be necessary)
	 */
	@Override
	public float floatValue() {
		return this.toBigDecimal(Precision.valueOf(7)).floatValue();
	}
	
	/**
	 * @return this as a double (rounding may be necessary)
	 */
	@Override
	public double doubleValue() {
		return this.toBigDecimal(Precision.valueOf(15)).doubleValue();
	}
	
	public BigDecimal toBigDecimal(Precision decPrec) {
		return BigDecimalMath.divide(new BigDecimal(this.num), this.den, decPrec);
	}
	
	/**
	 * Converts this to a BigDecimal with decPrec digits precision. 
	 * Because we can access numerator and denominator directly, implementation in this class
	 * is more efficient than as BigFloat.valueOf(BigRational).
	 * 
	 * @param decPrec Precision in decimal digits.
	 * @return this as a big float with the wanted precision.
	 */
	public BigDecimal toBigDecimal(Scale decPrec) {
		return BigDecimalMath.divide(new BigDecimal(this.num), this.den, decPrec);
	}
	
	/**
	 * @return this as a (fractional) string
	 */
	@Override
	public String toString() {
		if (this.den.equals(BigIntConstants.ONE)) {
			return this.num.toString();
		}
		return this.num + "/" + this.den;
	}
	
	/**
	 * Converts this into a string with the given decimal digits precision.
	 * @param decPrec output precision in decimal digits.
	 * @return this as a string with the wanted precision.
	 */
	public String toString(Scale decPrec) {
		return this.toBigDecimal(decPrec).toString();
	}
}
