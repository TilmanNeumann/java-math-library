/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2024 Tilman Neumann - tilman.neumann@web.de
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
package de.tilman_neumann.jml.primes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.Divisors;
import de.tilman_neumann.jml.base.BigDecimalMath;
import de.tilman_neumann.jml.precision.Magnitude;
import de.tilman_neumann.jml.precision.Scale;
import de.tilman_neumann.jml.smooth.CANEntry;
import de.tilman_neumann.jml.smooth.CANIterator;
import de.tilman_neumann.jml.transcendental.EulerConstant;
import de.tilman_neumann.jml.transcendental.Exp;
import de.tilman_neumann.jml.transcendental.Ln;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;
import de.tilman_neumann.util.SortedMultiset_BottomUp;
import de.tilman_neumann.util.Timer;

import static de.tilman_neumann.jml.base.BigDecimalConstants.F_0;

/**
 * Tests Robin's Riemann hypothesis tests on colossally abundant numbers (CANs).
 * 
 * From page 4 of http://www.math.lsa.umich.edu/~lagarias/doc/elementaryrh.pdf:
 * "Robin showed that, if the Riemann hypothesis is false, then there will necessarily exist a counterexample
 * to the inequality (1.2) that is a colossally abundant number; the same property can be established for inequality (1.1)"
 * 
 * Inequality (1.1) (Lagarias): sigma(n) <= Hn + exp(Hn)*ln(Hn)
 * 
 * Inequality (1.2) (Robin):    sigma(n) <= exp(gamma) n ln(ln(n)) for each n >=5041
 */
public class RiemannRobinHypothesisAnalyzer {
	private static final Logger LOG = LogManager.getLogger(RiemannRobinHypothesisAnalyzer.class);

	private static final boolean DEBUG = false;

	private static final Timer timer = new Timer();
	
	/**
	 * Test inequality (1.2) with CANs.
	 * 
	 * >24k CANs checked. Typical timings:
	 * Tested CAN(24003) with 118278 digits... t0=2297, t1=16, t2=0, t3=0, t4=16, t5=281
	 * Tested CAN(24004) with 118284 digits... t0=0,    t1=26, t2=0, t3=0, t4=0, t5=297
	 * Tested CAN(24005) with 118289 digits... t0=1735, t1=15, t2=0, t3=0, t4=16, t5=281
	 * Tested CAN(24006) with 118295 digits... t0=0,    t1=18, t2=0, t3=0, t4=0, t5=313
	 * Tested CAN(24007) with 118300 digits... t0=1187, t1=31, t2=0, t3=0, t4=0, t5=282
	 * Tested CAN(24008) with 118306 digits... t0=0,    t1=18, t2=0, t3=0, t4=0, t5=297
	 * Tested CAN(24009) with 118311 digits... t0=1703, t1=32, t2=0, t3=0, t4=0, t5=296
	 * Tested CAN(24010) with 118314 digits... t0=0,    t1=30, t2=0, t3=0, t4=0, t5=297
	 * Tested CAN(24011) with 118319 digits... t0=609,  t1=16, t2=0, t3=0, t4=15, t5=282
	 * Tested CAN(24012) with 118325 digits... t0=0,    t1=24, t2=0, t3=0, t4=0, t5=297
	 */
	private static void runRobinsRHTest() {
		long t0, t1, t2, t3, t4;
		
		Scale scale = Scale.valueOf(30); // precision in after-floating point decimal digits
		BigDecimal expGamma = Exp.exp(EulerConstant.gamma(scale), scale);
		
		CANIterator canIter = new CANIterator();
		for (int m=1; ; m++) {
			timer.capture();
			CANEntry canEntry = canIter.next();
			BigInteger n = canEntry.getCAN();
			t0 = timer.capture();
			
			BigDecimal n_flt = new BigDecimal(n, 0);
			BigDecimal lnln_n = Ln.ln(Ln.ln(n_flt, scale), scale);
			t1 = timer.capture();
			
			BigDecimal robin = expGamma.multiply(n_flt).multiply(lnln_n);
			t2 = timer.capture();
			
			// Fast sumOfDivisors computation with known prime factorization
			SortedMultiset<BigInteger> factors = toSortedMultiset(canEntry.getPrimes(), canEntry.getExponents());
			t3 = timer.capture();
			if (DEBUG) LOG.debug("factors(n)=" + factors);
			BigInteger sigma = Divisors.sumOfDivisors(factors);
			BigDecimal diff = BigDecimalMath.subtract(robin, sigma);
			t4 = timer.capture();
			if (DEBUG) LOG.debug("sigma(n)=" + sigma + ", robin=" + robin + ", diff=" + diff);
			if (diff.compareTo(F_0) < 0) {
				LOG.info("Found RH counterexample candidate!");
				LOG.info("    m=" + m + ": n has " + Magnitude.of(n) + " digits");
				LOG.info("    n=" + n);
			} else {
				LOG.info("Tested CAN(" + m + ") with " + Magnitude.of(n) + " digits... t0=" + t0 + ", t1=" + t1 + ", t2=" + t2 + ", t3=" + t3 + ", t4=" + t4);
			}
		}
	}

	private static SortedMultiset<BigInteger> toSortedMultiset(ArrayList<BigInteger> primes, ArrayList<Integer> exponents) {
		SortedMultiset<BigInteger> factors = new SortedMultiset_BottomUp<>();
		for (int i=0; i<primes.size(); i++) {
			factors.add(primes.get(i), exponents.get(i));
		}
		return factors;
	}
	
	public static void main(String[] argv) {
    	ConfigUtil.initProject();
    	runRobinsRHTest();
	}
}
