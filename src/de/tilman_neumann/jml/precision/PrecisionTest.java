package de.tilman_neumann.jml.precision;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.base.BigDecimalConstants;
import de.tilman_neumann.test.junit.ClassTest;

public class PrecisionTest extends ClassTest {

	private static final Logger LOG = Logger.getLogger(PrecisionTest.class);
	
	public void testPrecision() {
		assertEquals(6, BigDecimal.valueOf(310.567).precision());
		assertEquals(3, BigDecimal.valueOf(310).precision());
		assertEquals(1, BigDecimal.valueOf(0.3).precision());
		assertEquals(2, BigDecimal.valueOf(0.0021).precision());

		assertEquals(6, BigDecimal.valueOf(310.5670).precision());
		assertEquals(7, new BigDecimal("310.5670").precision());
		assertEquals(13, new BigDecimal("310.5670").setScale(10).precision());
		assertEquals(5, new BigDecimal("310.5670").setScale(2, RoundingMode.HALF_EVEN).precision());

		assertEquals(1, BigDecimalConstants.ZERO.precision());
		assertEquals(1, BigDecimalConstants.ZERO.setScale(10).precision());
		assertEquals(1, BigDecimalConstants.ZERO.setScale(-10).precision());
		// zero has precision 1 no matter what it's scale is
	}

	public void testMathContext() {
		BigDecimal a = BigDecimal.valueOf(654321.123456789);
		BigDecimal b = BigDecimal.valueOf(22222.2222222);
		MathContext mc = new MathContext(5, RoundingMode.HALF_EVEN);
		BigDecimal result = a.add(b, mc);
		LOG.debug(a + " + " + b + " at 5 digits precision = " + result);
		assertEquals(5, result.precision());
		// setPrecision parameter of MathContext means relative precision!
	}

	public void testApplyTo() {
		Precision precision = Precision.valueOf(5);
		// test rounding
		assertEquals(new BigDecimal("54321"), precision.applyTo(new BigDecimal("54321.023")));
		assertEquals(new BigDecimal("0.023577"), precision.applyTo(new BigDecimal("0.0235768")));
		assertEquals(new BigDecimal("0.23577"), precision.applyTo(new BigDecimal("0.235768999")));
		// test scale reduction without rounding
		BigDecimal big = new BigDecimal("543000000000000");
		LOG.debug(big + " has unscaled value " + big.unscaledValue() + " and scale " + big.scale());
		BigDecimal adjustedBig = precision.applyTo(big);
		LOG.debug(adjustedBig + " has unscaled value " + adjustedBig.unscaledValue() + " and scale " + adjustedBig.scale());
		assertEquals(new BigDecimal(new BigInteger("54300"), -10), adjustedBig);
		// test zero ;)
		BigDecimal zero10 = BigDecimalConstants.ZERO.setScale(10);
		LOG.debug("zero with scale 10 = " + zero10);
		BigDecimal roundedZero10 = precision.applyTo(zero10);
		LOG.debug("rounded zero = " + roundedZero10);
		BigDecimal zero1 = BigDecimalConstants.ZERO.setScale(1);
		LOG.debug("zero with scale 1 = " + zero1);
		BigDecimal roundedZero1 = precision.applyTo(zero1);
		LOG.debug("rounded zero = " + roundedZero1);
		BigDecimal zero_7 = BigDecimalConstants.ZERO.setScale(-7);
		LOG.debug("zero with scale -7 = " + zero_7);
		BigDecimal roundedZero_7 = precision.applyTo(zero_7);
		LOG.debug("rounded zero = " + roundedZero_7);
	}
}
