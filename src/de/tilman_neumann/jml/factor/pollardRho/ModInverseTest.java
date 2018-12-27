package de.tilman_neumann.jml.factor.pollardRho;

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
		
		for (int bits=10; bits<63; bits++) {
			LOG.debug("Test " + NCOUNT + " modular inverses of " + bits + " bit numbers...");
			for (int i=0; i<NCOUNT; i++) {
				BigInteger N = new BigInteger(bits, RNG);
				BigInteger mod = new BigInteger(bits, RNG);
				BigInteger inv = N.modInverse(mod);
			}
		}
	}
}
