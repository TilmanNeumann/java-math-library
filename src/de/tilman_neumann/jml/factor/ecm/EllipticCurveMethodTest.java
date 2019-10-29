package de.tilman_neumann.jml.factor.ecm;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.apache.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

public class EllipticCurveMethodTest {
	private static final Logger LOG = Logger.getLogger(EllipticCurveMethodTest.class);
	
	private static final SecureRandom RNG = new SecureRandom();

	private static final int N_COUNT = 10000;
	
	public static void main(String[] args) {
		ConfigUtil.initProject();
		
		EllipticCurveMethod ecm = new EllipticCurveMethod();
		
		int[] a31 = new int[EllipticCurveMethod.NLen];
		int[] b31 = new int[EllipticCurveMethod.NLen];
		int[] c31 = new int[EllipticCurveMethod.NLen];
		
		for (int bits = 10; bits<1000; bits += 1) {
			ecm.NumberLength = EllipticCurveMethod.computeNumberLength(bits);
			
			LOG.debug("Create " + N_COUNT + " N with " + bits + " bit...");
			BigInteger[] NArray = new BigInteger[N_COUNT];
			for (int i=0; i<N_COUNT; i++) {
				NArray[i] = new BigInteger(bits, RNG)/*.negate()*/; // XXX test negative args fail
			}
			
			// in-out-conversion
			LOG.debug("Test in-out-conversion of N with " + bits + " bit...");
			for (int i=0; i<N_COUNT; i++) {
				BigInteger Nin = NArray[i];
				try {
					ecm.BigNbrToBigInt(Nin, a31);
					BigInteger Nout = ecm.BigIntToBigNbr(a31);
					if (!Nin.equals(Nout)) {
						LOG.error("In-out-conversion failure: Nin=" + Nin + ", Nout = " + Nout + ", NumberLength = " + ecm.NumberLength);
					}
				} catch (Exception e) {
					LOG.error("Conversion of N=" + Nin + " caused " + e, e);
				}
			}
			
			// toString
			LOG.debug("Test toString conversion of N with " + bits + " bit...");
			for (int i=0; i<N_COUNT; i++) {
				BigInteger N = NArray[i];
				String NStr = N.toString();
				ecm.BigNbrToBigInt(N, a31);
				String big31Str = ecm.BigNbrToString(a31);
				if (!NStr.equals(big31Str)) {
					LOG.error("toString-conversion failure: correct=" + NStr + ", big31Str = " + big31Str);
				}
			}
			
			if (bits < 64) {
				// long conversion
				LOG.debug("Test long conversion of N with " + bits + " bit...");
				for (int i=0; i<N_COUNT; i++) {
					BigInteger N = NArray[i];
					ecm.BigNbrToBigInt(N, a31);
					String a31Str = ecm.BigNbrToString(a31);
					ecm.LongToBigNbr(N.longValue(), b31);
					String b31Str = ecm.BigNbrToString(b31);
					ecm.SubtractBigNbr(a31, b31, c31);
					String diffStr = ecm.BigNbrToString(c31);
					if (!a31Str.equals(b31Str)) {
						LOG.error("long conversion failure: a31Str=" + a31Str + ", b31Str = " + b31Str + ", diff = " + diffStr + ", NumberLength = " + ecm.NumberLength);
					}
				}
			}
			
			// add31
			LOG.debug("Test add31 of N with " + bits + " bit...");
			int imax = (int) Math.sqrt(N_COUNT);
			int jmin = N_COUNT - (int) Math.sqrt(N_COUNT);
			for (int i=0; i<imax; i++) {
				BigInteger a = NArray[i];
				ecm.BigNbrToBigInt(a, a31);
				for (int j=jmin; j<N_COUNT; j++) {
					BigInteger b = NArray[j];
					ecm.BigNbrToBigInt(b, b31);
					BigInteger correctSum = a.add(b);
					ecm.AddBigNbr(a31, b31, c31);
					BigInteger testSum = ecm.BigIntToBigNbr(c31);
					if (!correctSum.equals(testSum)) {
						LOG.error("add31 failure: a=" + a + " + b=" + b + ": correct = " + correctSum + ", add31 = " + testSum);
					}
				}
			}
		} // end_for bits
	}
}
