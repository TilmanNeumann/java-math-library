package de.tilman_neumann.jml.precision;

import java.math.BigDecimal;
import java.math.RoundingMode;

import de.tilman_neumann.jml.base.BigDecimalConstants;

/**
 * Relative precision for BigDecimal operations.
 * @author Tilman Neumann
 */
public class Precision implements Comparable<Precision> {
	// float has roughly 7 decimal digits precision (23 bit mantissa -> 2^23 ~ 8.3 * 10^6)
	static final int FLOAT_PRECISION = 7;
	// double has roughly 15 decimal digits precision (52 bit mantissa -> 2^52 ~ 4.5*10^15)
	static final int DOUBLE_PRECISION = 15;
	
	private static final int NUMBER_OF_STORED_PRECISIONS = 10000;
	private static Precision[] STORED_PRECISIONS;
	static {
		STORED_PRECISIONS = new Precision[NUMBER_OF_STORED_PRECISIONS];
		for (int i=0; i<NUMBER_OF_STORED_PRECISIONS; i++) {
			STORED_PRECISIONS[i] = new Precision(i);
		}
	}

	private int digits;
	
	/**
	 * Constructor is private to force access via the valueOf() methods
	 * which may permit performance improvements.
	 * @param digits
	 */
	private Precision(int digits) {
		this.digits = digits;
	}
	
	public static Precision valueOf(int digits) {
		if (digits < 0) throw new IllegalArgumentException("parameter digits is " + digits + " but must be non-negative.");
		return digits<NUMBER_OF_STORED_PRECISIONS ? STORED_PRECISIONS[digits] : new Precision(digits);
	}
	
	// floats and double usually use the full mantissa, but when the maximum exponent
	// is reached then the precision drops until 0 for zero values.
	public static final Precision of(float x) {
		return Precision.valueOf( Math.min(FLOAT_PRECISION, Magnitude.of(x) - Magnitude.ZERO_FLOAT_MAGNITUDE) );
	}
	
	public static final Precision of(double x) {
		return Precision.valueOf( Math.min(DOUBLE_PRECISION, Magnitude.of(x) - Magnitude.ZERO_DOUBLE_MAGNITUDE) );
	}

	/**
	 * The precision of a BigDecimal, with 0 for zero values.
	 */
	public static Precision of(BigDecimal x) {
		return Precision.valueOf(x.signum()!=0 ? x.precision() : 0);
	}
	
	public Precision add(int addend) {
		return valueOf(digits + addend);
	}
	
	public Precision multiply(int multiplier) {
		return valueOf(digits * multiplier);
	}

	/**
	 * Reduces the relative precision of x to this, or leaves it as it is
	 * if x already has a smaller precision.
	 * @param x
	 * @return x with maximum relative precision given by this.
	 */
	public BigDecimal applyTo(BigDecimal x) {
		if (x==null) return null;
		if (BigDecimalConstants.ZERO.compareTo(x)!=0) { // don´t use equals()
			int currentDigits = Precision.of(x).digits;
			int exceedingDigits = currentDigits - digits;
			if (exceedingDigits <= 0) return x;
			// we need to reduce the precision, i.e. the size of the unscaled value of x.
			// because of x = unscaledValue * 10^-scale = (unscaledValue/10) * 10^-(scale-1)
			// this implies that the scale is reduced accordingly.
			return x.setScale(x.scale()-exceedingDigits, RoundingMode.HALF_EVEN);
		}
		// zero
		int currentScale = x.scale();
		if (currentScale <= digits) return x;
		return x.setScale(digits);
	}

	public int digits() {
		return digits;
	}

	public String toString() {
		return "precision " + digits;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o==null || !(o instanceof Precision)) return false;
		return this.digits == ((Precision) o).digits;
	}

	@Override
	public int compareTo(Precision other) {
		return this.digits - other.digits;
	}
}
