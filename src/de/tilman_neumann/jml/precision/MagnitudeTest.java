package de.tilman_neumann.jml.precision;

import java.math.BigDecimal;
import java.math.BigInteger;

import de.tilman_neumann.jml.base.BigIntConstants;
import de.tilman_neumann.jml.base.BigDecimalConstants;
import de.tilman_neumann.test.junit.ClassTest;

public class MagnitudeTest extends ClassTest {
	
	public void testBigIntegerBitOperations() {
		assertEquals(0, BigIntConstants.ZERO.bitLength());
		assertEquals(1, BigIntConstants.ONE.bitLength());
		assertEquals(2, BigIntConstants.TWO.bitLength());
		assertEquals(2, BigIntConstants.THREE.bitLength());
		assertEquals(3, BigIntConstants.FOUR.bitLength());
		
		assertEquals(0, BigIntConstants.ZERO.bitCount());
		assertEquals(1, BigIntConstants.ONE.bitCount());
		assertEquals(1, BigIntConstants.TWO.bitCount());
		assertEquals(2, BigIntConstants.THREE.bitCount());
		assertEquals(1, BigIntConstants.FOUR.bitCount());
		// bitCount counts the number of set bits
		
		assertEquals(-1, BigIntConstants.ZERO.getLowestSetBit()); // !
		assertEquals(0, BigIntConstants.ONE.getLowestSetBit());
		assertEquals(1, BigIntConstants.TWO.getLowestSetBit());
		assertEquals(0, BigIntConstants.THREE.getLowestSetBit());
		assertEquals(2, BigIntConstants.FOUR.getLowestSetBit());
		// getLowestSetBit() gives 0 for odd numbers, -1 for zero
	}
	
	public void testDigits() {
		assertEquals(0, Magnitude.of(BigIntConstants.ZERO));
		assertEquals(0, Magnitude.of(BigIntConstants.ZERO.negate()));
		assertEquals(1, Magnitude.of(BigIntConstants.ONE));
		assertEquals(1, Magnitude.of(BigIntConstants.ONE.negate()));
		assertEquals(1, Magnitude.of(BigIntConstants.NINE));
		assertEquals(1, Magnitude.of(BigIntConstants.NINE.negate()));
		assertEquals(2, Magnitude.of(BigIntConstants.TEN));
		assertEquals(2, Magnitude.of(BigIntConstants.TEN.negate()));
		assertEquals(3, Magnitude.of(BigInteger.valueOf(999)));
		assertEquals(3, Magnitude.of(BigInteger.valueOf(-999)));
		assertEquals(4, Magnitude.of(BigIntConstants.THOUSAND));
		assertEquals(4, Magnitude.of(BigIntConstants.THOUSAND.negate()));
	}
	
	public void testBits() {
		assertEquals(0, Magnitude.bitsOf(BigIntConstants.ZERO));
		assertEquals(0, Magnitude.bitsOf(BigIntConstants.ZERO.negate()));
		assertEquals(1, Magnitude.bitsOf(BigIntConstants.ONE));
		assertEquals(1, Magnitude.bitsOf(BigIntConstants.ONE.negate()));
		assertEquals(2, Magnitude.bitsOf(BigIntConstants.TWO));
		assertEquals(2, Magnitude.bitsOf(BigIntConstants.TWO.negate()));
		assertEquals(2, Magnitude.bitsOf(BigIntConstants.THREE));
		assertEquals(2, Magnitude.bitsOf(BigIntConstants.THREE.negate()));
		assertEquals(3, Magnitude.bitsOf(BigIntConstants.FOUR));
		assertEquals(3, Magnitude.bitsOf(BigIntConstants.FOUR.negate()));
	}
	
	public void testZero() {
		assertEquals(BigIntConstants.ZERO, BigDecimalConstants.ZERO.unscaledValue());
		assertEquals(0, BigDecimalConstants.ZERO.scale());
		assertEquals(10, BigDecimalConstants.ZERO.setScale(10).scale());
		assertEquals(-10, BigDecimalConstants.ZERO.setScale(-10).scale());
		// it is possible to set the scale of 0 to arbitrary values
	}

	public void testMagnitude() {
		assertEquals(8, Magnitude.of(BigDecimal.valueOf(23450000.000)));
		assertEquals(4, Magnitude.of(BigDecimal.valueOf(2345.456)));
		assertEquals(1, Magnitude.of(BigDecimal.valueOf(7.0456)));
		assertEquals(-1, Magnitude.of(BigDecimal.valueOf(0.0456)));
		assertEquals(0, Magnitude.of(BigDecimalConstants.ZERO));
		assertEquals(-10, Magnitude.of(BigDecimalConstants.ZERO.setScale(10)));
		assertEquals(14, Magnitude.of(new BigDecimal("12345678901234.001")));
		assertEquals(4, Magnitude.of(new BigDecimal("2323.001")));
		assertEquals(1, Magnitude.of(new BigDecimal("1.000000001")));
		assertEquals(1, Magnitude.of(BigDecimalConstants.ONE));
		assertEquals(-2, Magnitude.of(new BigDecimal("0.001")));
		assertEquals(-2, Magnitude.of(new BigDecimal("-0.001")));
		assertEquals(3, Magnitude.of(new BigDecimal("-232.001")));
	}
}
