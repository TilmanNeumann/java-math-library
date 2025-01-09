package de.tilman_neumann.jml.random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Tests of Lehmer's random number generator for 64 bit numbers.
 * 
 * Currently only prints some generated numbers.
 * 
 * @author Tilman Neumann
 */
public class LehmerRng64Test {
	private static final Logger LOG = LogManager.getLogger(LehmerRng64Test.class);
	
	public static void main(String[] args) {
		ConfigUtil.initProject();
		
		LehmerRng64 rng = new LehmerRng64();
		for (int i=0;i<100; i++) {
			LOG.debug("" + rng.nextLong());
		}
	}
}
