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
package de.tilman_neumann.jml.combinatorics;

import java.math.BigInteger;

import org.apache.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

import static de.tilman_neumann.jml.base.BigIntConstants.*;

/**
 * Implementations of the factorial function.
 * @author Tilman Neumann
 */
// TODO: Implement Luschny´s prime swing factorial
public class Factorial {

	private static final Logger LOG = Logger.getLogger(Factorial.class);

	private static BigInteger[] factorials = new BigInteger[]{ONE, ONE};
	
	/**
	 * Object used to synchronize access to the static factorials array.
	 * We need to call a constructor, with valueof() we would block one of the standard values!
	 */
	private static Boolean syncObject = new Boolean(true);

	/**
	 * Computes the factorial for non-negative integer arguments by the
	 * simple product rule.
	 * 
	 * @param n Argument
	 * @return n! if n is a non-negative integer
	 * @throws IllegalArgumentException if n is not a non-negative integer
	 */
	public static BigInteger simpleProduct(int n) throws IllegalArgumentException {
		if (n >= 0) {
			BigInteger ret = ONE;
			for (int i=2; i<=n; i++) {
				ret = ret.multiply(BigInteger.valueOf(i));
			}
			return ret;
		}
		throw new IllegalArgumentException("factorial currently supports only non-negative integers, but the argument is n=" + n);
	}

	/**
	 * Computes the factorial for non-negative integer arguments applying the
	 * simple product rule, but remembering previously computed values.
	 * Additional speed is provided by the the arraycopy-function.
	 * 
	 * @param n Argument
	 * @return n! if n is a non-negative integer
	 * @throws IllegalArgumentException if n is not a non-negative integer
	 */
	public static BigInteger withMemory(int n) {
	    if (n >= 0) {
	    	// pass by the synchronized block if the factorials array is big enough
	    	if (n >= factorials.length) {
	    		// we need to enlarge the factorials array, but we don't want
	    		// several threads to do this in parallel (and the latter undo
	    		// the effects of the first ones). Therefore we do this in a
	    		// synchronized block, and every thread checks again if the
	    		// array is still to small when it enters the block:
	    		synchronized (syncObject) {
	        		if (n >= factorials.length) {
	        			BigInteger[] newFactorials = new BigInteger[n+100];
			            System.arraycopy(factorials, 0, newFactorials, 0, factorials.length);
			        	factorials = newFactorials;
		        	}
	    		}
	        }
	        
	        // Check if value still needs to be computed:
	        if (factorials[n] == null) {
	        	// Recursion. Usually iterative updating should be faster, but the
	        	// recursion profits from multi-threaded environments: it stops
	        	// immediately when another thread has already computed a new value
	        	factorials[n] = BigInteger.valueOf(n).multiply(withMemory(n-1));
	            //LOG.debug(n + "! = " + _factorials[n]);
	        }
	        return factorials[n];
	    }
	    
	    throw new IllegalArgumentException("factorial currently supports only non-negative integers!");
	}
	
	/**
	 * Computes the factorial for non-negative integer arguments applying the
	 * simple product rule, but allowing for a previously computed start value.
	 * 
	 * Adapted from http://www.jonelo.de by Johann Nepomuk Loefflmann (jonelo@jonelo.de),
	 * published under GNU General Public License.
	 * 
	 * @param n Argument
	 * @param start Argument of the start result
	 * @param startResult Factorial for start
	 * @return n! if n is a non-negative integer
	 * @throws IllegalArgumentException if n is not a non-negative integer
	 */
	public static BigInteger withStartResult(int n, int start, BigInteger startResult) throws ArithmeticException {
        if (n<0) throw new ArithmeticException("The factorial function supports only non-negative arguments.");
        if (n==0) return BigInteger.ONE;
        if (n==start) return startResult;
        if (n<start) {
        	start=1; 
        	startResult = BigInteger.ONE; 
        }
        BigInteger x = startResult;
        for (int i=start+1; i <= n; i++) {
            x=x.multiply(BigInteger.valueOf(i));
        }
        return x;
	}

	/**
	 * Test.
	 * @param args Ignored.
	 */
	public static void main(String[] args) {
    	ConfigUtil.initProject();
    	int n=1000;
    	int numberOfTests = 1000;
    	long start = System.currentTimeMillis();
    	BigInteger result = null;
    	for (int i=0; i<numberOfTests; i++) {
    		result = simpleProduct(n);
    	}
    	long end = System.currentTimeMillis();
    	LOG.info("simpleProduct(" + n + ") took " + (end-start) + "ms");
    	start = System.currentTimeMillis();
    	BigInteger resultWithMemory = null;
    	for (int i=0; i<numberOfTests; i++) {
    		factorials = new BigInteger[]{ONE, ONE};
    		resultWithMemory = withMemory(n);
    	}
    	end = System.currentTimeMillis();
    	LOG.info("factorial(" + n + ") took " + (end-start) + "ms");
    	if (!resultWithMemory.equals(result)) {
    		LOG.error("factorial() computed wrong result!");
    	}
	}
}
