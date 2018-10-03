/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018 Tilman Neumann (www.tilman-neumann.de)
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
package de.tilman_neumann.math.base.bigint;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

import org.apache.log4j.Logger;

import de.tilman_neumann.math.factor.FactorAlgorithm;
import de.tilman_neumann.types.SortedMultiset;
import de.tilman_neumann.util.ConfigUtil;

/**
 * Implementations of the Moebius function.
 * 
 * @author Tilman Neumann
 */
public class Moebius {
	private static final Logger LOG = Logger.getLogger(Moebius.class);
	
	private Moebius() {
		// static class, not instantiable
	}
	
	/**
	 * Computes the value of the Moebius function at n.
	 * 
	 * Returns
	 * 1       if n=1,
	 * (-1)^k, if k is the number of distinct prime factors,
	 * 0,      if k has repeated prime factors.
	 * 
	 * @param n Argument
	 * @return the moebius function.
	 */
	public static int moebius(BigInteger n) {
		if (n.compareTo(BigInteger.ONE) <= 0) {
			return 1;
		}
		
		// factorize n:
		SortedMultiset<BigInteger> factors = FactorAlgorithm.DEFAULT.factor(n);
		//LOG.debug("factors of " + n + " = " + factors);
		
		if (factors==null || factors.keyCount()==0) {
			// prime
			return -1;
		}
		
		// accumulate number of different primes in k:
		int k = 1;
		for (Map.Entry<BigInteger, Integer> factorAndMultiplicity : factors.entrySet()) {
			final int e = factorAndMultiplicity.getValue().intValue();
			if ( e > 1 ) {
				return 0;
			}
			if ( e == 1) {
				k *= -1 ;
			}
		}
		// return (-1)^k:
		return k;
	}

	  //-------------------------------------------------------------------------
	  // This returns the value of the Moebius function, mu(x). See
	  // http://planetmath.org/encyclopedia/MoebiusFunction.html for a careful
	  // definition.
	  //
	  // The algorithm used is fairly efficient.
	  //
	  // Adapted from Natura math Functions.
	  //-------------------------------------------------------------------------

	  public static BigInteger moebius_natura(BigInteger x)
	  {
	    if(x.equals(new BigInteger("1")))
	    {
	      return new BigInteger("1");
	    }
	    else
	    {
	      if((isSquareFree(x)).equals(new BigInteger("0")))
	      {
	        return new BigInteger("0");
	      }
	      else
	      { 
	    	  SortedMultiset<BigInteger> factors = FactorAlgorithm.DEFAULT.factor(x);
	    	  return new BigInteger("-1").pow(factors.totalCount());
	      }
	    }
	  }

	  //-------------------------------------------------------------------------
	  // This functions returns 1 if no square divides x, 0 otherwise.
	  // I am not aware of any more efficient algorithm.
	  //
	  // Adapted from Natura math Functions.
	  //-------------------------------------------------------------------------

	  public static BigInteger isSquareFree(BigInteger x)
	  {
	    BigInteger current = new BigInteger("2");

	    for(; current.compareTo(SqrtInt.iSqrt(x)[0])<=0; current = current.add(new BigInteger("1"))) {
	      if(x.mod(current.multiply(current)).equals(new BigInteger("0")))
	      {
	        return new BigInteger("0");
	      }
	    }

	    return new BigInteger("1");
	  }

	/**
	 * Tests.
	 * 
	 * @param args Ignored
	 */
	public static void main(String[] args) {
    	ConfigUtil.initProject();
    	int nMax = 100;
		int[] myResults = new int[nMax];
		int[] naturaResults = new int[nMax];
		for (int n=0; n<nMax; n++) {
			BigInteger bigN = BigInteger.valueOf(n);
			myResults[n] = moebius(bigN);
			naturaResults[n] = moebius_natura(bigN).intValue();
		}
		LOG.info("my Moebius function = " + Arrays.toString(myResults));
		LOG.info("Natura Moebius fct. = " + Arrays.toString(naturaResults));
		// Result: The two implementations are equal except for n=0,
		// where my implementation gives 1 and natura -1.
		// Mupad does not allow an input of n==0...
	}
}
