package de.tilman_neumann.jml.factor;

import java.math.BigInteger;
import java.util.Map;

import de.tilman_neumann.util.SortedMultiset;

public class FactorUtil {

	/**
	 * Returns a product-like representation of the given factorization,
	 * with distinct keys separated by "*" and the multiplicity indicated by "^".
	 * 
	 * @param factorization a prime factorization
	 * @return string representation
	 */
	public static String getPrettyFactorString(SortedMultiset<BigInteger> factorization) {
		if (factorization.size()>0) {
			// Implementation note: Is faster with String than with StringBuffer!
			String factorStr = "";
			for (Map.Entry<BigInteger, Integer> entry : factorization.entrySet()) {
				factorStr += entry.getKey();
				Integer multiplicity = entry.getValue();
				if (multiplicity.intValue() > 1) {
					factorStr += "^" + multiplicity;
				}
				factorStr += " * ";
			}
			// remove the last ", "
			return factorStr.substring(0, factorStr.length()-3);
		}
		
		// no elements
		return "1";
	}

}
