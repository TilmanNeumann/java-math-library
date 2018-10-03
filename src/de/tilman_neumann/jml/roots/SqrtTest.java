package de.tilman_neumann.jml.roots;

import java.math.BigDecimal;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.precision.Scale;
import de.tilman_neumann.test.junit.ClassTest;

/**
 * @author Tilman Neumann
 * @since 2011-09-25
 */
public class SqrtTest extends ClassTest {
	private static final Logger LOG = Logger.getLogger(SqrtTest.class);
	
	public void testSpecialCases() {
		BigDecimal input = new BigDecimal("36.0000090000");
		BigDecimal sqrt = SqrtReal.sqrt(input, Scale.valueOf(5));
		LOG.debug("sqrt(" + input + ") = " + sqrt);
	}
	
	public void testBigArgs() {
		BigDecimal a = new BigDecimal("1312596401028278160");
		BigDecimal sqrt = SqrtReal.sqrt(a, Scale.valueOf(5));
		LOG.debug("sqrt(" + a + ") = " + sqrt);
	}
}
