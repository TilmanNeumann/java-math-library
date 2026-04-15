package de.tilman_neumann.jml.primes.exact;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;

public class TDivPrimeTestTest {
	private static final Logger LOG = LogManager.getLogger(TDivPrimeTestTest.class);

	private static final BarrettPrimeTest BARRETT_TEST = BarrettPrimeTest.getInstance();
	private static final LemirePrimeTest LEMIRE_TEST = LemirePrimeTest.getInstance();
	
	@Before
	public void setup() {
		ConfigUtil.initProject();
	}

	@Test()
	public void testTDivPrimeTest() {
		// test some even numbers
		test(2);
		test(256);
		test(2565572);
		
		// test many odd numbers
		for (int n=3; n < 10000000; n+=2) {
			test(n);
		}
		
		// test end of int range
		for (int i=100; i>=0; i--) {
			test(Integer.MAX_VALUE - i);
		}
	}
	
	private void test(int n) {
		boolean barrettResult = BARRETT_TEST.isPrime_v1(n);
		boolean barrettResult_v2 = BARRETT_TEST.isPrime_v2(n);
		boolean barrettResult_unrolled = BARRETT_TEST.isPrimeUnrolled(n);
		boolean lemireResult = LEMIRE_TEST.isPrime_v1(n);
		boolean lemireResult_v2 = LEMIRE_TEST.isPrime_v2(n);
		boolean lemireResult_unrolled = LEMIRE_TEST.isPrime/*Unrolled*/(n);
		if (barrettResult != barrettResult_v2) {
			LOG.error("n=" + n + ": barrettResult = " + barrettResult + ", barrettResult_v2 = " + barrettResult_v2);
		}
		if (barrettResult != barrettResult_unrolled) {
			LOG.error("n=" + n + ": barrettResult = " + barrettResult + ", barrettResult_unrolled = " + barrettResult_unrolled);
		}
		if (barrettResult != lemireResult) {
			LOG.error("n=" + n + ": barrettResult = " + barrettResult + ", lemireResult = " + lemireResult);
		}
		if (barrettResult != lemireResult_v2) {
			LOG.error("n=" + n + ": barrettResult = " + barrettResult + ", lemireResult_v2 = " + lemireResult_v2);
		}
		if (barrettResult != lemireResult_unrolled) {
			LOG.error("n=" + n + ": barrettResult = " + barrettResult + ", lemireResult_unrolled = " + lemireResult_unrolled);
		}
		assertEquals(barrettResult, barrettResult_v2);
		assertEquals(barrettResult, barrettResult_unrolled);
		assertEquals(barrettResult, lemireResult);
		assertEquals(barrettResult, lemireResult_v2);
		assertEquals(barrettResult, lemireResult_unrolled);
	}
}
