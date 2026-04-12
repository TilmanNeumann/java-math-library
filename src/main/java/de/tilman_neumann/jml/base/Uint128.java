/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2026 Tilman Neumann - tilman.neumann@web.de
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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.Ensure;

/**
 * An incomplete 128 bit integer implementation.
 * 
 * Implementation note:
 * r_lo+Long.MIN_VALUE < low+Long.MIN_VALUE is an inlined compareUnsigned(r_lo, low) < 0.
 * 
 * @author Tilman Neumann
 */
// TODO Now that there are signed methods, this class needs a refactoring
public class Uint128 {
	@SuppressWarnings("unused")
	private static final Logger LOG = LogManager.getLogger(Uint128.class);
	
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
	 * Add two 128 bit integers.
	 * @param b
	 * @return this + b
	 */
	public Uint128 add(Uint128 b) {
	    long r_lo = low + b.getLow();
	    long carry = Long.compareUnsigned(r_lo, low) < 0 ? 1 : 0;
	    long r_hi = high + b.getHigh() + carry;
		return new Uint128(r_hi, r_lo);
	}
	
	/**
	 * Add two 128 bit integers, return the high part.
	 * @param b
	 * @return high part of this + b
	 */
	public long add_getHigh(Uint128 b) {
		long r_lo = low + b.getLow();
	    long carry = Long.compareUnsigned(r_lo, low) < 0 ? 1 : 0;
	    return high + b.getHigh() + carry;
	}

	/**
	 * Subtract two 128 bit integers.
	 * 
	 * @param b
	 * @return this - b, may be negative
	 */
    public Uint128 subtract(Uint128 b) {
    	long b_lo = b.getLow();
        long r_lo = low - b_lo;
        long borrow = Long.compareUnsigned(low, b_lo) < 0 ? 1 : 0;
        long r_hi = high - b.getHigh() - borrow;
		return new Uint128(r_hi, r_lo);
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
	 * Multiplication of unsigned 64 bit integers with simplified carry recognition.
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
		final long carry = (med_term+Long.MIN_VALUE < med_prod1+Long.MIN_VALUE) ? 1L<<32 : 0;
		final long r_hi = (((lo_prod >>> 32) + med_term) >>> 32) + hi_prod + carry;
		final long r_lo = ((med_term & 0xFFFFFFFFL) << 32) + lo_prod;

		return new Uint128(r_hi, r_lo);
	}
	
	/**
	 * Multiplication of two unsigned 64-bit integers using Math.multiplyHigh().
	 * Pretty fast if supported by intrinsics, which needs newer hardware and Java 10+.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static Uint128 mul64_MH(long a, long b) {
		final long r_lo = a*b;
		long r_hi = Math.multiplyHigh(a, b);
		if (a<0) r_hi += b;
		if (b<0) r_hi += a;
		
		if (DEBUG) {
			// compare to pure Java implementation
			Uint128 testResult = mul64(a, b);
			Ensure.ensureEquals(testResult.high, r_hi);
			Ensure.ensureEquals(testResult.low, r_lo);
		}

		return new Uint128(r_hi, r_lo);
	}

	/**
	 * Multiplication of two signed 64 bit integers, adapted from Henry S. Warren, Hacker's Delight, Addison-Wesley, 2nd edition, chapter 8-2.
	 * This is more or less what Java9 does in Math.multiplyHigh(), but I think I made it a bit faster optimizing register usage.
	 * 
	 * @param a signed long
	 * @param b signed long
	 * @return a*b as a signed 127 bit number
	 */
	public static Uint128 mul64Signed(long a, long b) {
		final long a_hi = a >> 32;
		final long a_lo = a & 0xFFFFFFFFL;
		final long b_hi = b >> 32;
		final long b_lo = b & 0xFFFFFFFFL;
		
		// use b_lo twice as first argument hoping that this optimizes register usage
		final long w0 = b_lo * a_lo;
		final long t = b_lo * a_hi + (w0 >>> 32);
		// same with t
		final long w2 = t >> 32;
		final long w1 = (t & 0xFFFFFFFFL) + a_lo * b_hi;
	    
		final long r_hi = a_hi * b_hi + w2 + (w1 >> 32);
		final long r_lo = a * b;
		return new Uint128(r_hi, r_lo);
	}

	/**
	 * Multiplication of two signed 64-bit integers using Math.multiplyHigh().
	 * Pretty fast if supported by intrinsics, which needs newer hardware and Java 10+.<br><br>
	 * 
	 * @param a signed long
	 * @param b signed long
	 * @return a*b as a signed 127 bit number
	 */
	public static Uint128 mul64SignedMH(long a, long b) {
		final long r_lo = a*b;
		final long r_hi = Math.multiplyHigh(a, b);
		
		if (DEBUG) {
			// compare to pure Java implementation
			Uint128 testResult = mul64Signed(a, b);
			Ensure.ensureEquals(testResult.high, r_hi);
			Ensure.ensureEquals(testResult.low, r_lo);
		}

		return new Uint128(r_hi, r_lo);
	}

	/**
	 * The square of an unsigned 64 bit integer.
	 * 
	 * @param a unsigned long
	 * @return a^2
	 */
	// XXX speed up using intrinsics like in mul64_MH() ?
	public static Uint128 square64(long a) {
		final long a_hi = a >>> 32;
		final long a_lo = a & 0xFFFFFFFFL;
		
		final long lo_prod = a_lo * a_lo;
		final long med_prod = a_hi * a_lo;
		final long med_term = med_prod<<1;
		final long hi_prod = a_hi * a_hi;
		
		// the medium term could overflow
		final long carry = (med_term+Long.MIN_VALUE < med_prod+Long.MIN_VALUE) ? 1L<<32 : 0;
		final long r_hi = (((lo_prod >>> 32) + med_term) >>> 32) + hi_prod + carry;
		final long r_lo = ((med_term & 0xFFFFFFFFL) << 32) + lo_prod;
		return new Uint128(r_hi, r_lo);
	}

	/**
	 * Multiplication of two unsigned 128 bit integers.
	 * 
	 * @param a Uint128
	 * @param b Uint128
	 * @return a*b as an array of [low, high] Uint128 objects;
	 */
	public static Uint128[] mul128/*_v2*/(Uint128 a, Uint128 b) {
		final long a_hi = a.getHigh(); // a >>> 32;
		final long b_hi = b.getHigh(); // b >>> 32;
		final long a_lo = a.getLow(); // a & 0xFFFFFFFFL;
		final long b_lo = b.getLow(); // b & 0xFFFFFFFFL;
		
		final Uint128 lo_prod = mul64(a_lo, b_lo); // a_lo * b_lo;
		final Uint128 med_prod1 = mul64(a_hi, b_lo); // a_hi * b_lo;
		final Uint128 med_prod2 = mul64(a_lo, b_hi); // a_lo * b_hi;
		final Uint128 med_term = med_prod1.add(med_prod2); // med_prod1 + med_prod2;
		final Uint128 hi_prod = mul64(a_hi, b_hi); // a_hi * b_hi;
		
		// the medium term could overflow
		//final long carry = (med_term+Long.MIN_VALUE < med_prod1+Long.MIN_VALUE) ? 1L<<32 : 0;
		final Uint128 carry = (med_term.getHigh()+Long.MIN_VALUE < med_prod1.getHigh()+Long.MIN_VALUE) ? new Uint128(1, 0) : new Uint128(0, 0);
		
		//final long r_hi = (((lo_prod >>> 32) + med_term) >>> 32) + hi_prod + carry;
		final long lo_prod_hi = lo_prod.getHigh(); // (lo_prod >>> 32)
		final Uint128 intermediate = new Uint128(0, lo_prod_hi).add(med_term); // ((lo_prod >>> 32) + med_term)
		final long intermediate_hi = intermediate.getHigh(); // (((lo_prod >>> 32) + med_term) >>> 32)
		final Uint128 r_hi = new Uint128(0, intermediate_hi).add(hi_prod).add(carry);
		
		//final long r_lo = ((med_term & 0xFFFFFFFFL) << 32) + lo_prod;
		final long med_term_lo = med_term.getLow(); // (med_term & 0xFFFFFFFFL)
		final Uint128 r_lo = new Uint128(med_term_lo, 0).add(lo_prod);
		
		//return new Uint128(r_hi, r_lo);
		return new Uint128[] {r_lo, r_hi};
	}

	/**
	 * Get the lower 128 bit integer of a multiplication of two unsigned 128 bit integers.
	 * 
	 * @param a Uint128
	 * @param b Uint128
	 * @return the low Uint128 of a*b
	 */
	public static Uint128 mul128_getLow(Uint128 a, Uint128 b) { // derived from mul128_v2
		final long a_hi = a.getHigh(); // a >>> 32;
		final long b_hi = b.getHigh(); // b >>> 32;
		final long a_lo = a.getLow(); // a & 0xFFFFFFFFL;
		final long b_lo = b.getLow(); // b & 0xFFFFFFFFL;
		
		final Uint128 lo_prod = mul64(a_lo, b_lo); // a_lo * b_lo;
		final Uint128 med_prod1 = mul64(a_hi, b_lo); // a_hi * b_lo;
		final Uint128 med_prod2 = mul64(a_lo, b_hi); // a_lo * b_hi;
		final Uint128 med_term = med_prod1.add(med_prod2); // med_prod1 + med_prod2;
		
		//final long r_lo = ((med_term & 0xFFFFFFFFL) << 32) + lo_prod;
		final long med_term_lo = med_term.getLow(); // (med_term & 0xFFFFFFFFL)
		final Uint128 r_lo = new Uint128(med_term_lo, 0).add(lo_prod);
		
		//return new Uint128(r_hi, r_lo);
		return r_lo;
	}

	/**
	 * Get the lower 128 bit integer of a multiplication of two unsigned 128 bit integers, using Math.multiplyHigh().
	 * 
	 * @param a Uint128
	 * @param b Uint128
	 * @return the low Uint128 of a*b
	 */
	public static Uint128 mul128MH_getLow(Uint128 a, Uint128 b) { // derived from mul128_v2
		final long a_hi = a.getHigh(); // a >>> 32;
		final long b_hi = b.getHigh(); // b >>> 32;
		final long a_lo = a.getLow(); // a & 0xFFFFFFFFL;
		final long b_lo = b.getLow(); // b & 0xFFFFFFFFL;
		
		final Uint128 lo_prod = mul64_MH(a_lo, b_lo); // a_lo * b_lo;
		final Uint128 med_prod1 = mul64_MH(a_hi, b_lo); // a_hi * b_lo;
		final Uint128 med_prod2 = mul64_MH(a_lo, b_hi); // a_lo * b_hi;
		final Uint128 med_term = med_prod1.add(med_prod2); // med_prod1 + med_prod2;
		
		//final long r_lo = ((med_term & 0xFFFFFFFFL) << 32) + lo_prod;
		final long med_term_lo = med_term.getLow(); // (med_term & 0xFFFFFFFFL)
		final Uint128 r_lo = new Uint128(med_term_lo, 0).add(lo_prod);
		
		//return new Uint128(r_hi, r_lo);
		return r_lo;
	}

    /**
     * Complete unsigned 128 / 64 bit division.
     * This implementation has been worked out with support from Google Gemini.
     * 
     * @param u1 high 64 bits of the dividend
     * @param u0 low 64 bits of the dividend
     * @param v divisor
     * @return [quotientHigh, quotientLow, remainder]
     */
    public static long[] divide128by64Unsigned(long u1, long u0, long v) {
        if (v == 0) throw new ArithmeticException("Division by zero");

        // Step 1: check for quotients > 64 bit
        long qHigh = 0;
        if (Long.compareUnsigned(u1, v) >= 0) {
            qHigh = Long.divideUnsigned(u1, v);
            u1 = Long.remainderUnsigned(u1, v);
        }

        // Step 2: highly optimized 64/64 division for the remaining part
        // (the dividend now is u1:u0, which guarantees u1 < v)
        
        // 2.1. Normalization
        int s = Long.numberOfLeadingZeros(v);
        long v_norm = v << s;
        long v_hi = v_norm >>> 32;
        long v_lo = v_norm & 0xFFFFFFFFL;

        // shift u1:u0 (careful with s=0)
        long u_hi = (s == 0) ? u1 : (u1 << s) | (u0 >>> (64 - s));
        long u_lo = u0 << s;

        // 2.2. first 32-bit half of the quotient (q1)
        long q1 = Long.divideUnsigned(u_hi, v_hi);
        long rhat = Long.remainderUnsigned(u_hi, v_hi);

        while (Long.compareUnsigned(q1, 0x100000000L) >= 0 || Long.compareUnsigned(q1 * v_lo, (rhat << 32) | (u_lo >>> 32)) > 0) {
            q1--;
            rhat += v_hi;
            if (Long.compareUnsigned(rhat, 0x100000000L) >= 0) break;
        }

        // intermediate remainder
        long rem = ((u_hi << 32) | (u_lo >>> 32)) - (q1 * v_norm);

        // 2.3. Second 32-bit half of the quotient (q0)
        long q0 = Long.divideUnsigned(rem, v_hi);
        rhat = Long.remainderUnsigned(rem, v_hi);

        while (Long.compareUnsigned(q0, 0x100000000L) >= 0 || Long.compareUnsigned(q0 * v_lo, (rhat << 32) | (u_lo & 0xFFFFFFFFL)) > 0) {
            q0--;
            rhat += v_hi;
            if (Long.compareUnsigned(rhat, 0x100000000L) >= 0) break;
        }

        // 2.4. Assemble results
        long qLow = (q1 << 32) | q0;
        long finalRemainder = (((rem << 32) | (u_lo & 0xFFFFFFFFL)) - (q0 * v_norm)) >>> s;

        return new long[] { qHigh, qLow, finalRemainder };
    }
    
    /**
     * Computes the remainder of an unsigned 128 % 64 bit division.
     * This implementation has been worked out with support from Google Gemini.
     * 
     * @param u1 high 64 bits of the dividend
     * @param u0 low 64 bits of the dividend
     * @param v divisor
     * @return the (unsigned) 64-bit remainder
     */
    public static long mod128by64Unsigned(long u1, long u0, long v) {
        if (v == 0) throw new ArithmeticException("Division by zero");

        // 1. Reduce the high part (u1) directly to eliminate the case of quotients > 64 bit
        long rem = Long.remainderUnsigned(u1, v);
        
        // if rem is 0, a simple modulus on u0 is sufficient
        if (rem == 0) {
            return Long.remainderUnsigned(u0, v);
        }

        // 2. The remaining part now is (rem : u0) % v
        // Since rem < v, we can use a compact bit loop.
        // For modulus computations this is often faster than Knuths algorithm D style 32-bit decompositions,
        // because it needs less case distinctions and no multiplications.
        for (int i = 0; i < 64; i++) {
            long msb = (u0 >>> 63);
            rem = (rem << 1) | msb;
            u0 <<= 1;
            if (Long.compareUnsigned(rem, v) >= 0) {
                rem -= v;
            }
        }
        
        return rem;
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

	public double doubleValue() {
		return toBigInteger().doubleValue(); // TODO more efficient solution
	}

	public double doubleValueUnsigned() {
		return toBigIntegerUnsigned().doubleValue(); // TODO more efficient solution
	}
	
	/**
	 * Signed conversion to BigInteger.
	 * @return this as a signed 127 bit integer converted to BigInteger
	 */
	public BigInteger toBigInteger() {
		return BigInteger.valueOf(high).shiftLeft(64).or(toBigIntegerUnsigned(low));
	}

	/**
	 * Unsigned conversion to BigInteger.
	 * @return this as an unsigned 128 bit integer converted to BigInteger
	 */
	public BigInteger toBigIntegerUnsigned() {
		return toBigIntegerUnsigned(high).shiftLeft(64).or(toBigIntegerUnsigned(low));
	}

	// helper method
	private static BigInteger toBigIntegerUnsigned(long n) {
		BigInteger big = BigInteger.valueOf(n & Long.MAX_VALUE); // drop sign bit
	    if (n < 0) {
	    	big = big.setBit(63); // now big is unsigned 64 bit
	    }
	    return big;
	}
	
	/**
	 * @return a string representing this as a signed integer
	 */
	@Override
	public String toString() {
		return toBigInteger().toString(); // TODO more efficient solution
	}
	
	/**
	 * @return a string representing this as an unsigned integer
	 */
	public String toStringUnsigned() {
		return toBigIntegerUnsigned().toString(); // TODO more efficient solution
	}
}
