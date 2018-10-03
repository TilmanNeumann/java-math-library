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
package de.tilman_neumann.math.base.bigint.primes.exact;

import java.math.BigInteger;
import java.util.HashSet;

import org.apache.log4j.Logger;

import de.tilman_neumann.math.base.bigint.IntArray;

import static org.junit.Assert.*;

/**
 * Singleton implementation of a set containing a first few small primes.
 * @author Tilman Neumann
 */
public class SmallPrimesSet extends HashSet<Integer> {

	private static final long serialVersionUID = -2700398407077930815L;
	private static final Logger LOG = Logger.getLogger(SmallPrimesSet.class);
	private static final boolean DEBUG = false;
		
	private int limit;

	private static final SieveFacade THE_SIEVE = SieveFacade.get();

	private static final SmallPrimesSet THE_SET = new SmallPrimesSet();
	
	/**
	 * Not public, use get() method to access the singleton.
	 */
	private SmallPrimesSet() {
		this.add(2);
		limit = 2;
	}

	/**
	 * @return the small primes set containing all primes <= bound
	 */
	public static SmallPrimesSet get() {
		return THE_SET;
	}
	
	/**
	 * Ensures that the set contains the first 'count' primes
	 * @param count
	 * @return SmallPrimesSet
	 */
	public SmallPrimesSet ensurePrimeCount(int count) {
		int initialSize = this.size();
		if (initialSize < count) {
			IntArray primes = THE_SIEVE.ensurePrimeCount(count).getPrimes();
			int[] array = primes.array;
			if (DEBUG) LOG.debug("set.initialSize = " + initialSize  + ", array.size = " + primes.count);
			for (int i = initialSize; i<count; i++) {
				this.add(array[i]);
			}
			int finalSize = this.size();
			limit = array[finalSize-1];
			if (DEBUG) LOG.debug("Enhanced small primes set has " + finalSize + " elements.");
		}
		return this;
	}

	/**
	 * Ensures that the set contains all primes <= desiredLimit.
	 * @param desiredLimit
	 * @return SmallPrimesSet containing all primes <= desiredLimit
	 */
	public SmallPrimesSet ensureLimit(int desiredLimit) {
		if (limit < desiredLimit) {
			int initialSize = this.size();
			IntArray primes = THE_SIEVE.ensureLimit(desiredLimit).getPrimes();
			int[] array = primes.array;
			if (DEBUG) LOG.debug("set.initialSize = " + initialSize  + ", array.size = " + primes.count);
			
			for (int i = initialSize; i < primes.count; i++) {// XXX Should we really add all primes the array contains? That might become expensive...
				this.add(array[i]);
			}
			if (DEBUG) assertEquals(primes.count, this.size());
			limit = Math.max(array[primes.count-1], desiredLimit);
			if (DEBUG) LOG.debug("Small primes set has " + primes.count + " elements.");
		}
		return this;
	}

	// for debugging only
	//	@Override
	//	public boolean add(Integer p) {
	//		LOG.info("add p = " + p);
	//		return super.add(p);
	//	}
	
	/**
	 * @return the biggest integer tested for being prime covered by this set.
	 */
	public int getLimit() {
		return limit;
	}
	
	/**
	 * @param arg
	 * @return true if the small primes set contains an integer with the value of 'arg'
	 */
	public boolean contains(BigInteger arg) {
		if (arg.bitLength() >= 32) return false;
		int arg_int = arg.intValue();
		return arg_int<=limit && super.contains(Integer.valueOf(arg_int));
	}

	/**
	 * @param arg
	 * @return true if the small primes set contains an integer with the value of 'arg'
	 */
	public boolean contains(long arg) {
		return arg<=limit && super.contains(Integer.valueOf((int)arg));
	}
	
	/**
	 * @param arg int
	 * @return true if the small primes set contains 'arg'
	 */
	public boolean contains(int arg) {
		return arg<=limit && super.contains(Integer.valueOf(arg));
	}
}
