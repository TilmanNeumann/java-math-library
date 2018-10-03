package de.tilman_neumann.jml;

import static de.tilman_neumann.jml.base.BigIntConstants.*;

import java.math.BigInteger;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.base.BigIntTriangle;
import de.tilman_neumann.jml.base.BigRational;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.TimeUtil;

/**
 * Computes the number of terms/steps the Greedy algorithm requires
 * to find a sum of simple quotients for any k/n; 0<k<=n.
 * @author Tilman Neumann
 */
// TODO: Compute number of terms
public class EgyptianFractionsTriangle extends BigIntTriangle {
	private static final Logger LOG = Logger.getLogger(EgyptianFractionsTriangle.class);

	public EgyptianFractionsTriangle(int n) {
		super(n, ZERO);
		for (int m=1; m<=n; m++) {
			for (int k=1; k<=m; k++) {
				this.set(m, k, BigInteger.valueOf(greedyCount(m,k)));
			}
		}
	}
	
	private int greedyCount(int n, int k) {
		//LOG.debug("compute greedyCount("+n+","+k+")");
		int count = 0;
		BigRational rest = new BigRational(BigInteger.valueOf(k), BigInteger.valueOf(n));
		int rez = 1;
		while (!rest.equals(ZERO)) {
			BigRational test = new BigRational(ONE, BigInteger.valueOf(rez));
			//LOG.debug("test=" + test + ", rest = " + rest);
			if (test.compareTo(rest) <= 0) {
				rest = rest.subtract(test);
				//LOG.debug("new rest = " + rest);
				count++;
			}
			rez++;
		}
		return count;
	}
	
	public static void main(String[] args) {
    	ConfigUtil.initProject();
    	
    	int n = 30; // 28 is easy (8s), 29 is harder (2:26), 30 again easy (2:25), 31 is very heavy
    	
    	long start = System.currentTimeMillis();
		EgyptianFractionsTriangle t = new EgyptianFractionsTriangle(n);
    	long end = System.currentTimeMillis();
		LOG.info("greedy count triangle " + n + " computed in " + TimeUtil.timeDiffStr(start, end) + ":\n" + t.toString());
	}
}
