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
package de.tilman_neumann.jml.transcendental;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.base.BigDecimalConstants;
import de.tilman_neumann.jml.base.BigDecimalMath;
import de.tilman_neumann.jml.base.BigIntConstants;
import de.tilman_neumann.jml.base.BigRational;
import de.tilman_neumann.jml.powers.Pow2;
import de.tilman_neumann.jml.precision.Magnitude;
import de.tilman_neumann.jml.precision.Scale;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.TimeUtil;

/**
 * Computations of Pi = 3.1415... to arbitrary precision.
 * @author Tilman Neumann
 */
// TODO sometimes precision is a digit or so too low
public class Pi {
	private static final Logger LOG = Logger.getLogger(Pi.class);

	/** PI computed to PI_DEC_PREC decimal digits precision. */
    private static BigDecimal PI;
    private static Scale PI_DEC_PREC = Scale.valueOf(0);
    
	/**
	 * Compute Pi using the approximation formula found by Plouffe and the
	 * Borwein brothers also used in mpfr.
	 * The result is accurate to at least decPrec decimal after-comma digits.
	 * 
	 * @param decPrec Wanted precision in after-comma decimal digits
	 * @return PI = 3.1415...
	 */
    public static BigDecimal pi(Scale decPrec) {
	    if (decPrec.compareTo(PI_DEC_PREC) > 0) {
	        // need to recompute Pi with higher precision...
            PI = BigDecimalConstants.ZERO;
	        BigDecimal maxErr = decPrec.getErrorBound();
	        int i=0;
	        
        	// Wanted series element precision is one digit more than output precision
	        Scale elemPrec = decPrec.add(1);
	        BigDecimal sElement;
	        
	        do {
	            // Compute i.th series element: ----------------
		        // Denominator is 16^i == 2^(4*i)
		        BigDecimal den = Pow2.pow2(4*i);
		        int denMagnitude = Magnitude.of(den);
		        // Numerator: Required precision is elemPrec + magnitude(den):
		        Scale numPrec = elemPrec.add(denMagnitude);
		        BigRational c1 = new BigRational(BigIntConstants.FOUR, BigInteger.valueOf(8*i+1));
		        BigRational c2 = new BigRational(BigIntConstants.ONE, BigInteger.valueOf(4*i+2));
		        BigRational c3 = new BigRational(BigIntConstants.ONE, BigInteger.valueOf(8*i+5));
		        BigRational c4 = new BigRational(BigIntConstants.ONE, BigInteger.valueOf(8*i+6));
	            sElement = c1.subtract(c2).subtract(c3).subtract(c4).toBigDecimal(numPrec);
		        // Divide by denominator:
	            // To achieve precision decPrec+1, the sElement-Argument needs precision elemPrec + magnitude(den)...
	            sElement = BigDecimalMath.divide(sElement, den, elemPrec);

	            // Add new series element to the total:
	            // We want both arguments to have precision decPrec+1...
	            PI = PI.add(sElement);
	            //LOG.debug("i=" + i + ", sElement=" + sElement + ", PI=" + PI);
	            i++;
	        // Stop if the last series element is smaller than the series computed
	        // so far multiplied with the desired relative error.
	        } while (sElement.compareTo(maxErr) > 0);
	        
	        // store PI with wished precision:
	        PI_DEC_PREC = decPrec;
	    }

	    // assign output with wished accuracy:
	    return decPrec.applyTo(PI);
	}

	private static void testPi(Scale maxDecPrec) {
        long t0 = System.currentTimeMillis();
        for (Scale decPrec=Scale.valueOf(2); decPrec.compareTo(maxDecPrec)<=0; decPrec = decPrec.add(1)) {
            LOG.debug("pi(" + decPrec + ")=" + Pi.pi(decPrec));
        }
        long t1 = System.currentTimeMillis();
        LOG.debug("Time of pi compuatation: " + TimeUtil.timeDiffStr(t0,t1));
	}

	/**
	 * Test.
	 * 
	 * @param argv command line arguments, ignored
	 */
	public static void main(String[] argv) {
    	ConfigUtil.initProject();
        testPi(Scale.valueOf(200));
	}
}
