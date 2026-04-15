package de.tilman_neumann.jml.primes.exact;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

public class TDivPrimeTestPerformanceTest {
	private static final Logger LOG = LogManager.getLogger(TDivPrimeTestPerformanceTest.class);

	private static final int NMAX = 100000000;
	
	private static final BarrettPrimeTest BARRETT_TEST = BarrettPrimeTest.getInstance();
	private static final LemirePrimeTest LEMIRE_TEST = LemirePrimeTest.getInstance();
	private static final Random RNG = new Random();

	private static final int[] TEST_NUMBERS = setupTestNumbers();
	
	private static int[] setupTestNumbers() {
		int[] result = new int[NMAX];
		int i=0;
		while (i < NMAX) {
			int n = RNG.nextInt();
			if ((n&1) == 0) n++;
			if (n == Integer.MIN_VALUE) continue;
			n = Math.abs(n);
			if (n < 3) continue;
			result[i++] = n;
		};
		return result;
	}
	
	private static void test() {
		long t0, t1;
		
		t0 = System.nanoTime();
		for (int n : TEST_NUMBERS) {
			BARRETT_TEST.isPrime_v1(n);
		}
		t1 = System.nanoTime();
		LOG.info("BARRETT_TEST.isPrime_v1 took " + (t1-t0) + " ns");
		
		t0 = System.nanoTime();
		for (int n : TEST_NUMBERS) {
			BARRETT_TEST.isPrime_v2(n);
		}
		t1 = System.nanoTime();
		LOG.info("BARRETT_TEST.isPrime_v2 took " + (t1-t0) + " ns");
		
		t0 = System.nanoTime();
		for (int n : TEST_NUMBERS) {
			BARRETT_TEST.isPrimeUnrolled(n);
		}
		t1 = System.nanoTime();
		LOG.info("BARRETT_TEST.isPrimeUnrolled took " + (t1-t0) + " ns");

		t0 = System.nanoTime();
		for (int n : TEST_NUMBERS) {
			LEMIRE_TEST.isPrime_v1(n);
		}
		t1 = System.nanoTime();
		LOG.info("LEMIRE_TEST.isPrime_v1 took " + (t1-t0) + " ns");
		
		t0 = System.nanoTime();
		for (int n : TEST_NUMBERS) {
			LEMIRE_TEST.isPrime_v2(n);
		}
		t1 = System.nanoTime();
		LOG.info("LEMIRE_TEST.isPrime_v2 took " + (t1-t0) + " ns"); // best so far!
		
		t0 = System.nanoTime();
		for (int n : TEST_NUMBERS) {
			LEMIRE_TEST.isPrime/*Unrolled*/(n);
		}
		t1 = System.nanoTime();
		LOG.info("LEMIRE_TEST.isPrimeUnrolled took " + (t1-t0) + " ns");
	}
	
	public static void main(String[] args) {
		ConfigUtil.initProject();
		test();
	}
}
