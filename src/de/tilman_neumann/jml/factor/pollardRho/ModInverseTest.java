package de.tilman_neumann.jml.factor.pollardRho;

import static de.tilman_neumann.jml.base.BigIntConstants.*;
import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.apache.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Test correctness of Java's modInverse() implementation.
 * 
 * @author Tilman Neumann
 */
public class ModInverseTest {
	private static final Logger LOG = Logger.getLogger(ModInverseTest.class);
	private static final SecureRandom RNG = new SecureRandom();
	private static final int NCOUNT = 1000;
	
	public static void main(String[] args) {
		ConfigUtil.initProject();
		
		// test absolute random numbers that are invertible
		for (int bits=10; bits<100; bits++) {
			LOG.debug("Test " + NCOUNT + " modular inverses of " + bits + " bit numbers...");
			for (int i=0; i<NCOUNT; i++) {
				// test numbers:
				// mod may be any integer >= 2
				BigInteger mod;
				do {
					mod = new BigInteger(bits, RNG);
				} while (mod.compareTo(I_1)<=0);
				// we need N >= 2, N odd and gcd(N,mod)==1
				BigInteger N, gcd;
				boolean invertible;
				do {
					N = new BigInteger(bits, RNG);
					gcd = N.gcd(mod);
					invertible = gcd.compareTo(I_1)==0;
				} while (N.compareTo(I_1)<=0 || N.and(I_1).equals(I_0) || !invertible);
				LOG.debug("N=" + N + ", mod=" + mod + ", gcd=" + gcd);
				
				// compute and check inverse
				BigInteger inv = N.modInverse(mod);
				assertEquals(I_1, N.multiply(inv).mod(mod));
			}
		}
	}
}
