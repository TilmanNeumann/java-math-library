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
package de.tilman_neumann.jml.base;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.apache.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

import static org.junit.Assert.*;

/**
 * Rudimentary 128 bit unsigned int implementation.
 * 
 * @author Tilman Neumann
 */
public class Uint128 {
	private static final Logger LOG = Logger.getLogger(Uint128.class);
	
	private static final boolean DEBUG = false;
	
	private long high, low;
	
	public Uint128(long high, long low) {
		this.high = high;
		this.low = low;
	}
	
	public long getHigh() {
		return high;
	}
	
	public long getLow() {
		return low;
	}

	/**
	 * Add two unsigned 128 bit integers.
	 * @param other
	 * @return this + other
	 */
	public Uint128 add(Uint128 other) {
		// We know for sure that low overflows if both low and o_lo are 64 bit. If only one of the input 'low's
		// is 64 bit, then we can recognize an overflow if the result.lo is not 64 bit.
		final long o_lo = other.getLow();
		final long o_hi = other.getHigh();
		final long r_lo = low + o_lo;
		long r_hi = high + o_hi;
		if ((low<0 && o_lo<0) || ((low<0 || o_lo<0) && (r_lo >= 0))) r_hi++;
		// TODO Can we speed up this similarly like add_getHigh()
		return new Uint128(r_hi, r_lo);
	}

	/**
	 * Compute the sum of this and other, return the high part.
	 * @param other
	 * @return high part of this + other
	 */
	public long add_getHigh(Uint128 other) {
		// We know for sure that low overflows if both low and o_lo are 64 bit. If only one of the input 'low's
		// is 64 bit, then we can recognize an overflow if the result.lo is not 64 bit.
		// Thanks to Ben for a big performance boost (both for TinyEcm and PollardRho variants),
		// see https://www.mersenneforum.org/showpost.php?p=524300&postcount=173
		long a = low + other.getLow();
		long b = high + other.getHigh();
		return (Long.compareUnsigned(a, low) < 0) ? b + 1 : b;
	}

	/**
	 * Multiplication of unsigned 63 bit integers,
	 * following https://stackoverflow.com/questions/18859207/high-bits-of-long-multiplication-in-java.
	 * 
	 * This method ignores overflows of the "middle term".
	 * As such it won't work for 64 bit inputs but is otherwise faster than mul64().
	 * 
	 * @param a
	 * @param b
	 * @return a*b accurate for inputs <= 63 bit
	 */
	public static Uint128 mul63(long a, long b) {
		final long a_hi = a >>> 32;
		final long b_hi = b >>> 32;
		final long a_lo = a & 0xFFFFFFFFL;
		final long b_lo = b & 0xFFFFFFFFL;
		final long lo_prod = a_lo * b_lo;
		final long med_term = a_hi * b_lo + a_lo * b_hi; // possible overflow here
		final long hi_prod = a_hi * b_hi;
		final long r_hi = (((lo_prod >>> 32) + med_term) >>> 32) + hi_prod;
		final long r_lo = ((med_term & 0xFFFFFFFFL) << 32) + lo_prod;
		return new Uint128(r_hi, r_lo);
	}

	/**
	 * Multiplication of unsigned 64 bit integers,
	 * following https://stackoverflow.com/questions/18859207/high-bits-of-long-multiplication-in-java.
	 * 
	 * This method takes notice of overflows of the "middle term".
	 * As such it works for 64 bit inputs but is slightly slower than mul63().
	 * 
	 * @param a unsigned long
	 * @param b unsigned long
	 * @return a*b
	 */
	public static Uint128 mul64(long a, long b) {
		final long a_hi = a >>> 32;
		final long b_hi = b >>> 32;
		final long a_lo = a & 0xFFFFFFFFL;
		final long b_lo = b & 0xFFFFFFFFL;
		
		final long lo_prod = a_lo * b_lo;
		final long med_prod1 = a_hi * b_lo;
		final long med_prod2 = a_lo * b_hi;
		final long med_term = med_prod1 + med_prod2;
		final long hi_prod = a_hi * b_hi;
		
		// the medium term could overflow		
		long r_hi = (((lo_prod >>> 32) + med_term) >>> 32) + hi_prod;
		if ((med_prod1<0 && med_prod2<0) || ((med_prod1<0 || med_prod2<0) && med_term>=0)) r_hi += 1L<<32;
		final long r_lo = ((med_term & 0xFFFFFFFFL) << 32) + lo_prod;
		return new Uint128(r_hi, r_lo);
	}

	/**
	 * Multiplication of unsigned 64 bit integers,
	 * following https://codereview.stackexchange.com/questions/67962/mostly-portable-128-by-64-bit-division.
	 * 
	 * This method takes notice of overflows of the "middle term".
	 * As such it works for 64 bit inputs.
	 * 
	 * @param a unsigned long
	 * @param b unsigned long
	 * @return a*b
	 */
	public static Uint128 mul64_v2(long a, long b) {
		long a_lo = a & 0xFFFFFFFFL;
		long a_hi = a >>> 32;

		long b_lo = b & 0xFFFFFFFFL;
		long b_hi = b >>> 32;

		long c0 = a_lo * b_lo;
		long c1 = a_hi * b_lo;
		long c2 = a_hi * b_hi;

		long u1 = c1 + (a_lo * b_hi);
	    if(Long.compareUnsigned(u1, c1) < 0){
	        c2 += 1L << 32;
	    }

	    long u0 = c0 + (u1 << 32);
	    if(Long.compareUnsigned(u0, c0) < 0){
	        ++c2;
	    }

	    long y = c2 + (u1 >>> 32);
		return new Uint128(y, u0);
	}

	/**
	 * Computes the low part of the product of two unsigned 64 bit integers.
	 * 
	 * Overflows of the "middle term" are not interesting here because they'ld only
	 * affect the high part of the multiplication result.
	 * 
	 * @param a
	 * @param b
	 * @return (a*b) & 0xFFFFFFFFL
	 */
	public static long mul64_getLow(long a, long b) {
		final long a_hi = a >>> 32;
		final long b_hi = b >>> 32;
		final long a_lo = a & 0xFFFFFFFFL;
		final long b_lo = b & 0xFFFFFFFFL;
		final long lo_prod = a_lo * b_lo;
		final long med_term = a_hi * b_lo + a_lo * b_hi;
		final long r_lo = ((med_term & 0xFFFFFFFFL) << 32) + lo_prod;
		return r_lo;
	}

	/**
	 * Compute quotient and remainder of this / v.
	 * The quotient will be correct only if it is <= 64 bit.
	 * 
	 * @param v 64 bit unsigned integer
	 * @return [quotient, remainder] of this / v
	 */
	public long[] spDivide(long v)
	{
		long p_lo;
		long p_hi;
		long q = 0;
		long r;
		
		long r_hi = getHigh();
		long r_lo = getLow();
		if (DEBUG) LOG.debug("r_hi=" + Long.toUnsignedString(r_hi) + ", r_lo=" + Long.toUnsignedString(r_lo));
		
		int s = 0;
		if(0 == (v >>> 63)){
		    // Normalize so quotient estimates are no more than 2 in error.
		    // Note: If any bits get shifted out of r_hi at this point, the result would overflow.
		    s = Long.numberOfLeadingZeros(v);
		    int t = 64 - s;
		
		    v <<= s;
		    r_hi = (r_hi << s)|(r_lo >>> t);
		    r_lo <<= s;
		}
		if (DEBUG) LOG.debug("s=" + s + ", b=" + Long.toUnsignedString(v) + ", r_lo=" + r_lo + ", r_hi=" + r_hi);
		
		long b_hi = v >>> 32;
		
		/*
		The first full-by-half division places b
		across r_hi and r_lo, making the reduction
		step a little complicated.
		
		To make this easier, u_hi and u_lo will hold
		a shifted image of the remainder.
		
		[u_hi||    ][u_lo||    ]
		      [r_hi||    ][r_lo||    ]
		            [ b  ||    ]
		[p_hi||    ][p_lo||    ]
		              |
		              V
		            [q_hi||    ]
		*/
		
		long q_hat = Long.divideUnsigned(r_hi, b_hi);
		if (DEBUG) LOG.debug("q_hat=" + Long.toUnsignedString(q_hat));
		
		Uint128 mulResult = mul64(v, q_hat);
		p_lo = mulResult.getLow();
		p_hi = mulResult.getHigh();
		if (DEBUG) LOG.debug("p_lo=" + Long.toUnsignedString(p_lo) + ", p_hi=" + Long.toUnsignedString(p_hi));
		
		long u_hi = r_hi >>> 32;
		long u_lo = (r_hi << 32)|(r_lo >>> 32);
		
		// r -= b*q_hat
		//
		// At most 2 iterations of this...
		while( (Long.compareUnsigned(p_hi, u_hi) > 0) || ((p_hi == u_hi) && (Long.compareUnsigned(p_lo, u_lo) > 0)) )
		{
		    if (Long.compareUnsigned(p_lo, v) < 0) {
		        --p_hi;
		    }
		    p_lo -= v;
		    --q_hat;
		}
		
		long w_lo = (p_lo << 32);
		long w_hi = (p_hi << 32)|(p_lo >>> 32);
		if (DEBUG) LOG.debug("w_lo=" + Long.toUnsignedString(w_lo) + ", w_hi=" + Long.toUnsignedString(w_hi));
		
		if (Long.compareUnsigned(w_lo, r_lo) > 0) {
			if (DEBUG) LOG.debug("increment w_hi!");
		    ++w_hi;
		}
		
		r_lo -= w_lo;
		r_hi -= w_hi;
		if (DEBUG) LOG.debug("r_lo=" + Long.toUnsignedString(r_lo) + ", r_hi=" + Long.toUnsignedString(r_hi));
		
		q = q_hat << 32;
		
		/*
		The lower half of the quotient is easier,
		as b is now aligned with r_lo.
		
		      |r_hi][r_lo||    ]
		            [ b  ||    ]
		[p_hi||    ][p_lo||    ]
		                    |
		                    V
		            [q_hi||q_lo]
		*/
		
		q_hat = Long.divideUnsigned((r_hi << 32)|(r_lo >>> 32), b_hi);
		if (DEBUG) LOG.debug("b=" + Long.toUnsignedString(v) + ", q_hat=" + Long.toUnsignedString(q_hat));
		
		mulResult = mul64(v, q_hat);
		p_lo = mulResult.getLow();
		p_hi = mulResult.getHigh();
		if (DEBUG) LOG.debug("2: p_lo=" + Long.toUnsignedString(p_lo) + ", p_hi=" + Long.toUnsignedString(p_hi));
		
		// r -= b*q_hat
		//
		// ...and at most 2 iterations of this.
		while( (Long.compareUnsigned(p_hi, r_hi) > 0) || ((p_hi == r_hi) && (Long.compareUnsigned(p_lo, r_lo) > 0)) )
		{
		    if(Long.compareUnsigned(p_lo, v) < 0){
		        --p_hi;
		    }
		    p_lo -= v;
		    --q_hat;
		}
		
		r_lo -= p_lo;
		
		q |= q_hat;
		
		r = r_lo >>> s;
		
		return new long[] {q, r};
	}

	/**
	 * Shift this 'bits' bits to the left.
	 * @param bits
	 * @return this << bits
	 */
	public Uint128 shiftLeft(int bits) {
		if (bits<64) {
			long rh = (high<<bits) | (low>>>(64-bits));
			long rl = low<<bits;
			return new Uint128(rh, rl);
		}
		return new Uint128(low<<(bits-64), 0);
	}
	
	/**
	 * Shift this 'bits' bits to the right.
	 * @param bits
	 * @return this >>> bits
	 */
	public Uint128 shiftRight(int bits) {
		if (bits<64) {
			long rh = high>>>bits;
			long rl = (low>>>bits) | (high<<(64-bits));
			return new Uint128(rh, rl);
		}
		return new Uint128(0, high>>>(bits-64));
	}

	/**
	 * Bitwise "and" operation with a long.
	 * @param other
	 * @return this & other
	 */
	public long and(long other) {
		return low & other;
	}

	/**
	 * Convert this to BigInteger.
	 * @return this unsigned 128 bit integer converted to BigInteger
	 */
	public BigInteger toBigInteger() {
		return new BigInteger(Long.toBinaryString(high), 2).shiftLeft(64).add(new BigInteger(Long.toBinaryString(low), 2));
	}
	
	@Override
	public String toString() {
		return toBigInteger().toString();
	}
	
	private static void testCorrectness() {
		SecureRandom RNG = new SecureRandom();
		
		for (int i=0; i<100000; i++) {
			BigInteger a_hi_big = new BigInteger(63, RNG);
			BigInteger a_lo_big = new BigInteger(64, RNG);
			BigInteger b_hi_big = new BigInteger(63, RNG);
			BigInteger b_lo_big = new BigInteger(64, RNG);
			
			long a_hi = a_hi_big.longValue();
			long a_lo = a_lo_big.longValue();
			long b_hi = b_hi_big.longValue();
			long b_lo = b_lo_big.longValue();
			
			// test addition
			Uint128 a128 = new Uint128(a_hi, a_lo);
			Uint128 b128 = new Uint128(b_hi, b_lo);
			Uint128 sum128 = a128.add(b128);
			BigInteger sum128Big = sum128.toBigInteger();
			BigInteger sumBig = a128.toBigInteger().add(b128.toBigInteger());
			assertEquals(sumBig, sum128Big);

			// test multiplication with 63 bit numbers
			Uint128 prod128 = mul63(a_hi, b_hi);
			BigInteger prod128Big = prod128.toBigInteger();
			BigInteger prodBig = a_hi_big.multiply(b_hi_big);
			assertEquals(prodBig, prod128Big);

			// test multiplication with 64 bit numbers
			prod128 = mul64(a_lo, b_lo);
			prod128Big = prod128.toBigInteger();
			prodBig = a_lo_big.multiply(b_lo_big);
			//LOG.debug("Test " + a_lo_big + "*" + b_lo_big + ":");
			//LOG.debug("BigInt result = " + prodBig);
			//LOG.debug("int127 result = " + prod128Big);
			if (!prodBig.equals(prod128Big)) {
				LOG.debug("error! diff = " + prodBig.subtract(prod128Big));
			}
			assertEquals(prodBig, prod128Big);
		}
	}
	
	private static void testPerformance() {
		SecureRandom RNG = new SecureRandom();
		int NCOUNT = 40000000;
		
		// set up test numbers
		long[] a_arr = new long[NCOUNT];
		long[] b_arr = new long[NCOUNT];
		for (int i=0; i<NCOUNT; i++) {
			a_arr[i] = RNG.nextLong();
			b_arr[i] = RNG.nextLong();
		}
		
		// test performance of mul64 implementations
		long t0 = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			mul64(a_arr[i], b_arr[i]);
		}
		long t1 = System.currentTimeMillis();
		LOG.info("mul64 took " + (t1-t0) + "ms");
		
		t0 = System.currentTimeMillis();
		for (int i=0; i<NCOUNT; i++) {
			mul64_v2(a_arr[i], b_arr[i]);
		}
		t1 = System.currentTimeMillis();
		LOG.info("mul64_v2 took " + (t1-t0) + "ms");
		
		// Result: mul64 looks much faster than mul64_v2.
		// TODO But is that correct? Profiling TinyEcm with jvisualvm shows no difference...
	}

	/**
	 * Test.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		ConfigUtil.initProject();
		testCorrectness();
		testPerformance();
	}
}
