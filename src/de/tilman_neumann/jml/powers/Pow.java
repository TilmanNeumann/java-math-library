/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018 Tilman Neumann (www.tilman-neumann.de)
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
package de.tilman_neumann.jml.powers;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.base.BigDecimalConstants;
import de.tilman_neumann.jml.base.BigIntConstants;
import de.tilman_neumann.jml.precision.Magnitude;
import de.tilman_neumann.jml.precision.Precision;
import de.tilman_neumann.jml.precision.Scale;

public class Pow {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(Pow.class);

	/**
	 * Power function for large integer exponents (also negative)
	 * 
	 * @param x Basis
	 * @param n Exponent
	 * @return x^n
	 */
	public static BigDecimal pow(BigDecimal x, BigInteger n, Precision wantedDecPrec) {
		//LOG.debug(x + "^" + n + " [prec=" + wantedDecPrec + "]...");
		if (n.compareTo(BigIntConstants.ZERO) >= 0) {
			if (n.compareTo(BigIntConstants.MAX_EXPONENT) > 0) {
				// split exponent in permitted factors:
				BigInteger[] nDivMaxInt = n.divideAndRemainder(BigIntConstants.MAX_EXPONENT);
				BigDecimal xPowMaxInt = pow(x, BigIntConstants.MAX_EXPONENT.intValue(), wantedDecPrec); // x^MAX_EXPONENT
				// The following recursion allows arbitrary big exponents, because
				// (x^maxInt)^(floor(n/maxInt)) == x^((floor(n/maxInt)*maxInt)
				BigDecimal ret = pow(xPowMaxInt, nDivMaxInt[0], wantedDecPrec);
				return ret.multiply(pow(x, nDivMaxInt[1].intValue(), wantedDecPrec));
			}
			
			int nInt = n.intValue();
			//LOG.debug("nInt = " + nInt);
			return pow(x, nInt, wantedDecPrec);
		}
		
		return BigDecimalConstants.ONE.divide(pow(x, n.negate(), wantedDecPrec));
	}
	
	public static BigDecimal pow(BigDecimal x, int n, Scale resultScale) {
		// rough estimate of result size, may be improved a lot
		int resultMagnitude = n*Magnitude.of(x);
		//int resultMagnitude = n * (int) (Math.log10(Math.abs(x.doubleValue())) + 0.5);
		Precision resultPrecision = Precision.valueOf(Math.max(resultScale.digits() + resultMagnitude, 0));
		return pow(x, n, resultPrecision);
	}
	
	/**
	 * Built-in Java implementation of the pow function for integer exponents between 0...999999999.
	 * The result is computed exactly.
	 * @param x
	 * @param n
	 * @return x^n
	 */
	static BigDecimal nnPowJava(BigDecimal x, int n) {
		return x.pow(n);
	}
	
	static BigDecimal nnPowJavaTrunc(BigDecimal x, int n, Precision resultPrecision) {
		BigDecimal xInternal = x;
		Precision xPrecision = Precision.of(x);
		Precision cutPrecision = resultPrecision.add(4);
		if (xPrecision.compareTo(cutPrecision) >= 0) {
			// truncate precision of argument
			xInternal = cutPrecision.applyTo(x);
		}
		return resultPrecision.applyTo(xInternal.pow(n));
	}

	/**
	 * Built-in Java implementation of the pow function for integer exponents between -999999999...999999999.
	 * Only the <code>decPrec</code> leading digits of the output are correct!
	 * @param x
	 * @param n
	 * @return x^n
	 */
	static BigDecimal pow/*Java*/(BigDecimal x, int n, Precision decPrec) {
		// avoid rounding errors
		int innerDecPrec = decPrec.digits() + 4;
//		// avoid ArithmeticException (see comment in class PowTest):
//		if (innerDecPrec==1 && n>9) innerDecPrec = 2;
		// compute result at inner precision
		BigDecimal result = x.pow(n, new MathContext(innerDecPrec, RoundingMode.HALF_EVEN));
		// round to originally wanted output precision
		return decPrec.applyTo(result);
	}
	
	public static BigDecimal powJavaTrunc(BigDecimal x, int n, Precision resultPrecision) {
		BigDecimal xInternal = x;
		Precision xPrecision = Precision.of(x);
		Precision cutPrecision = resultPrecision.add(4);
		if (xPrecision.compareTo(cutPrecision) >= 0) {
			// truncate precision of argument
			xInternal = cutPrecision.applyTo(x);
		}
		return pow/*Java*/(xInternal, n, resultPrecision);
	}
}