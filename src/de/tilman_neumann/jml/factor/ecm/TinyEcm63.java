/*
	Copyright (c) 2014, Ben Buhrow
	All rights reserved.

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions are met:

	1. Redistributions of source code must retain the above copyright notice, this
	   list of conditions and the following disclaimer.
	2. Redistributions in binary form must reproduce the above copyright notice,
	   this list of conditions and the following disclaimer in the documentation
	   and/or other materials provided with the distribution.

	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
	ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
	WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
	DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
	ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
	(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
	LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
	ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
	SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

	The views and conclusions contained in the software and documentation are those
	of the authors and should not be interpreted as representing official policies,
	either expressed or implied, of the FreeBSD Project.
*/
/*
 * From https://www.mersenneforum.org/showthread.php?t=22525&page=11:
 * "The code for the 64-bit version is posted back in post 84, if you are interested; it's open source."
 */
package de.tilman_neumann.jml.factor.ecm;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Random;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.base.Uint128;
import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.util.ConfigUtil;

/**
 * A port of Ben Buhrow's tinyecm.c (https://www.mersenneforum.org/showpost.php?p=521028&postcount=84)
 * an ECM implementation for unsigned 64 bit integers.
 * 
 * This variant ignores carry-bits in the multiplication of 64 bit integers.
 * Fails for more N than TinyEcm64 when N>54 bit.
 * 
 * @author Tilman Neumann
 */
public class TinyEcm63 extends FactorAlgorithm {
	
	private static class ecm_pt {
		long X;
		long Z;
	}

	private static class ecm_work {
		long sum1;
		long diff1;
		long sum2;
		long diff2;
		long tt1;
		long tt2;
		long tt3;
		long tt4;
		long s;
		long n;
		ecm_pt pt1 = new ecm_pt();
		ecm_pt pt2 = new ecm_pt();
		ecm_pt pt3 = new ecm_pt();
		ecm_pt pt4 = new ecm_pt();
		int sigma;

		ecm_pt Pa = new ecm_pt();
		ecm_pt Pad = new ecm_pt();
		ecm_pt[] Pb = new ecm_pt[20];
		long Paprod;
		long[] Pbprod = new long[20];

		long stg2acc;
		int stg1_max;
		
		public ecm_work() {
			for (int i=0; i<20; i++) {
				Pb[i] = new ecm_pt();
			}
		}
	}

	private static final Logger LOG = Logger.getLogger(TinyEcm63.class);

	private static final boolean DEBUG = false;

	private static final int MAX_BITS_SUPPORTED = 62;

	private static final byte[] prac70Steps = new byte[] { 
			0,6,0,6,0,6,0,4,6,0,4,6,0,4,4,6,
			0,4,4,6,0,5,4,6,0,3,3,4,6,0,3,5,
			4,6,0,3,4,3,4,6,0,5,5,4,6,0,5,3,
			3,4,6,0,3,3,4,3,4,6,0,5,3,3,3,3,
			3,3,3,3,4,3,3,4,6,0,5,4,3,3,4,6,
			0,3,4,3,5,4,6,0,5,3,3,3,4,6,0,5,
			4,3,5,4,6,0,5,5,3,3,4,6,0,4,3,3,
			3,5,4,6};

	private static final byte[] prac85Steps = new byte[] { 
			0,6,0,6,0,6,0,6,0,4,
			6,0,4,6,0,4,4,6,0,4,
			4,6,0,5,4,6,0,3,3,4,
			6,0,3,5,4,6,0,3,4,3,
			4,6,0,5,5,4,6,0,5,3,
			3,4,6,0,3,3,4,3,4,6,
			0,4,3,4,3,5,3,3,3,3,
			3,3,3,3,4,6,0,3,3,3,
			3,3,3,3,3,3,4,3,4,3,
			4,6,0,3,4,3,5,4,6,0,
			5,3,3,3,4,6,0,5,4,3,
			5,4,6,0,4,3,3,3,5,4,
			6,0,4,3,5,3,3,4,6,0,
			3,3,3,3,5,4,6,0,3,3,
			3,4,3,3,4,6 };

	// pre-paired sequences for various B1 and B2 = 25*B1
	private static final int numb1_70 = 186;
	private static final byte[] b1_70 = new byte[] { 53,49,47,43,41,37,23,19,13,11,1,7,17,29,31,0,59,47,43,41,37,31,29,19,13,7,1,11,23,0,59,53,43,41,37,31,23,17,11,7,1,19,29,49,0,53,49,47,43,31,23,19,11,7,1,13,37,59,0,59,53,43,37,31,29,23,17,13,11,1,47,0,59,49,41,31,23,17,11,7,1,19,37,47,0,59,49,47,43,41,31,17,13,11,7,37,0,53,49,43,37,23,19,13,7,1,29,31,41,59,0,59,49,47,41,23,19,17,13,7,1,43,53,0,59,49,43,37,29,17,13,7,1,19,47,53,0,59,53,49,47,43,31,29,23,11,17,0,47,43,41,37,31,23,19,17,11,1,13,29,53,0,59,47,41,37,31,23,19,11,7,17,29,0,53,47,43,41,17,13,11,1,23,31,37,49 };

	private static final int numb1_85 = 225;
	private static final byte[] b1_85 = new byte[] { 61,53,49,47,43,41,37,23,19,13,11,1,7,17,29,31,0,59,47,43,41,37,31,29,19,13,7,1,11,23,0,59,53,43,41,37,31,23,17,11,7,1,19,29,49,0,53,49,47,43,31,23,19,11,7,1,13,37,59,0,59,53,43,37,31,29,23,17,13,11,1,47,0,59,49,41,31,23,17,11,7,1,19,37,47,0,59,49,47,43,41,31,17,13,11,7,37,0,53,49,43,37,23,19,13,7,1,29,31,41,59,0,59,49,47,41,23,19,17,13,7,1,43,53,0,59,49,43,37,29,17,13,7,1,19,47,53,0,59,53,49,47,43,31,29,23,11,17,0,47,43,41,37,31,23,19,17,11,1,13,29,53,0,59,47,41,37,31,23,19,11,7,17,29,0,53,47,43,41,17,13,11,1,23,31,37,49,0,53,47,43,41,29,19,7,1,17,31,37,49,59,0,49,43,37,19,17,1,23,29,47,53,0,59,53,43,41,31,17,7,1,11,13,19,29 };

	private static final int numb1_125 = 319;
	private static final byte[] b1_125 = new byte[] { 23,19,13,11,1,7,17,29,31,0,59,47,43,41,37,31,29,19,13,7,1,11,23,0,59,53,43,41,37,31,23,17,11,7,1,19,29,49,0,53,49,47,43,31,23,19,11,7,1,13,37,59,0,59,53,43,37,31,29,23,17,13,11,1,47,0,59,49,41,31,23,17,11,7,1,19,37,47,0,59,49,47,43,41,31,17,13,11,7,37,0,53,49,43,37,23,19,13,7,1,29,31,41,59,0,59,49,47,41,23,19,17,13,7,1,43,53,0,59,49,43,37,29,17,13,7,1,19,47,53,0,59,53,49,47,43,31,29,23,11,17,0,47,43,41,37,31,23,19,17,11,1,13,29,53,0,59,47,41,37,31,23,19,11,7,17,29,0,53,47,43,41,17,13,11,1,23,31,37,49,0,53,47,43,41,29,19,7,1,17,31,37,49,59,0,49,43,37,19,17,1,23,29,47,53,0,59,53,43,41,31,17,7,1,11,13,19,29,0,59,53,49,47,37,29,11,13,17,23,31,0,59,43,41,37,29,23,17,13,1,31,47,0,59,53,49,47,41,37,31,19,13,7,11,17,29,43,0,47,29,19,11,7,1,41,43,59,0,53,49,37,23,13,11,7,1,17,19,29,41,43,59,0,59,49,41,37,23,13,1,7,11,29,43,47,53,0,59,53,49,31,23,13,7,1,17,29,43,47,0,59,31,29,19,11,7,37,49,53 };

	private static final int numb1_165 = 425;
	private static final byte[] b1_165 = new byte[] { 13,7,1,11,19,47,59,0,59,49,43,37,31,29,23,19,17,7,11,13,47,53,0,53,47,41,37,31,23,19,11,1,13,29,43,59,0,53,49,41,37,31,19,17,1,7,23,29,47,59,0,59,53,47,43,41,29,19,17,13,7,1,23,31,49,0,53,47,41,37,29,23,19,11,7,17,31,43,49,59,0,47,43,41,37,23,19,17,13,7,11,29,53,0,53,49,43,37,29,23,11,7,1,13,19,31,41,0,53,49,47,43,37,31,23,17,11,13,41,0,59,47,43,37,31,29,23,11,1,17,19,41,0,59,53,19,13,7,1,29,43,47,49,0,53,49,47,41,29,19,17,13,11,7,1,23,31,43,59,0,53,49,41,37,23,19,13,11,7,1,17,43,47,0,47,43,41,31,19,17,7,1,13,37,49,0,59,49,37,29,13,1,7,11,17,19,41,47,53,0,49,47,31,29,7,1,13,17,19,23,37,59,0,47,37,31,19,17,13,11,1,29,41,43,53,0,59,41,17,13,7,1,19,23,31,47,49,53,0,59,53,47,43,31,29,7,1,11,17,37,41,49,0,49,43,37,23,19,13,1,7,17,0,59,49,41,37,31,29,23,1,11,13,53,0,53,43,41,37,29,23,17,13,11,7,1,19,31,49,0,53,43,31,29,23,19,17,1,13,37,41,59,0,53,43,37,31,23,13,1,17,29,59,0,59,49,41,37,23,19,11,1,7,29,0,59,43,17,13,11,1,7,23,29,37,41,49,0,49,47,43,41,29,1,7,13,19,23,31,59,0,59,49,47,31,29,13,7,37,41,43,0,49,41,29,23,13,11,7,1,17,19,31,43,53,0,53,47,43,37,29,23,17,1,11,13,31,41,49,59,0,53,47,41,19,13,11,1,17,23,43,0,53,49,47,37,23,19,11,7,17,29,31,43,0,53,31,19,17,13,7,1,29,37,59 };

	private static final int numb1_205 = 511;
	private static final byte[] b1_205 = new byte[] { 1,23,41,0,59,53,49,47,37,23,19,17,13,1,7,29,43,0,53,49,41,31,29,19,17,11,7,1,13,37,59,0,49,47,29,23,13,7,1,17,31,37,43,0,59,49,47,43,37,31,29,17,13,7,1,11,19,53,0,59,53,49,41,37,23,13,1,11,17,19,29,43,47,0,53,49,47,43,23,19,11,1,7,17,37,41,0,59,53,41,37,31,29,19,17,11,1,13,43,47,0,53,47,41,19,17,7,1,11,23,31,43,59,0,59,53,41,31,13,11,7,1,17,29,37,0,49,43,37,29,11,1,13,17,19,23,41,0,59,49,47,43,41,37,31,19,7,1,13,23,29,53,0,53,49,43,41,37,31,29,23,13,7,17,19,47,59,0,49,47,37,29,23,17,11,7,13,19,31,41,53,0,59,43,29,23,19,17,13,11,1,41,0,59,37,31,23,17,13,11,7,1,19,29,43,53,0,49,47,43,41,31,19,17,1,7,11,13,23,0,47,43,37,29,13,11,7,1,17,19,23,31,59,0,59,37,31,29,23,19,13,1,7,11,41,47,53,0,53,49,43,31,23,17,13,41,59,0,59,53,31,19,17,1,7,11,23,37,47,49,0,59,53,47,43,41,37,31,23,19,17,11,1,0,59,53,49,47,31,17,13,7,1,11,29,37,0,53,43,31,17,13,7,1,29,41,49,0,53,49,41,29,23,11,7,1,19,31,47,0,47,43,41,29,23,19,7,1,11,49,0,59,31,29,23,17,11,7,1,13,41,43,0,59,43,37,17,1,7,11,13,19,41,49,0,59,53,43,41,37,31,29,23,13,11,1,47,0,59,53,47,31,19,17,13,1,7,11,29,37,43,49,0,49,43,41,31,17,13,7,11,23,37,53,0,53,49,41,23,19,13,11,7,1,17,37,59,0,49,47,43,37,31,29,23,1,7,41,0,59,43,41,37,31,17,13,11,7,47,49,0,59,49,47,37,31,29,19,17,7,1,0,53,47,37,19,13,1,11,31,41,0,49,47,37,23,17,13,11,7,19,31,53,0,59,53,47,29,13,11,7,1,23,41,0,49,47,41,37,19,11,13,17,23,29,31,43,0,59,29,19,13,1,41,43,47,53,0,59,53,43,41,37,23,17,11,7,1,13,29,49 };

	private static final int[] map = {
		0, 1, 2, 0, 0, 0, 0, 3, 0, 0,
		0, 4, 0, 5, 0, 0, 0, 6, 0, 7,
		0, 0, 0, 8, 0, 0, 0, 0, 0, 9,
		0, 10, 0, 0, 0, 0, 0, 11, 0, 0,
		0, 12, 0, 13, 0, 0, 0, 14, 0, 15,
		0, 0, 0, 16, 0, 0, 0, 0, 0, 17,
		18, 0 }; // last entry 0 or 1 makes no performance difference

	long LCGSTATE;

	public String getName() {
		return "TinyEcm63";
	}

	/**
	 * Compute (x-y) mod n, with 0 <= x, y < n.
	 * @param x
	 * @param y
	 * @param n
	 * @return (x-y) mod n
	 */
	long submod(long x, long y, long n) {
	    final long r0 = x-y;
		// This method's implementation history is quite an odyssey:
		// * My first version was quite bad
		// * Suggestion by Ben Buhrow, https://www.mersenneforum.org/showpost.php?p=524038&postcount=158: Much better
		//return (Long.compareUnsigned(r0, x) > 0) ? r0+n : r0;
		// * An idea of mine using inlined compareUnsigned(x, y) < 0 that made quite a difference:
		//return (x+Long.MIN_VALUE < y+Long.MIN_VALUE) ? r0+n : r0;
		// * A suggestion of xilman, https://www.mersenneforum.org/showpost.php?p=524662&postcount=205,
	    //   which seems to be a another bit faster, at least for n>=52 bit:
		return r0 + (n & (r0 >> 63));
	}

	/**
	 * Compute x+y mod n, with 0 <= x, y < n.
	 * @param x
	 * @param y
	 * @param n
	 * @return x+y mod n
	 */
	long addmod(long x, long y, long n)
	{
	    long r0 = x+y;
	    return (r0 >= n) ? r0-n : r0;
	    // From https://www.mersenneforum.org/showpost.php?p=524038&postcount=158:
	    // "With 64 bit operands you'd also have to check if (r0 < x), and trigger the subtract in that case as well.
	    // In fact, this might be one reason why it isn't working for you now with 64 bit inputs... 
	    // the addition will often overflow and the "%n" will incorrectly do nothing in that case."
	    // Unfortunately, this would slow down the overall performance by 5-10%...
	    //return (r0 >= n || Long.compareUnsigned(r0, x) < 0) ? r0-n : r0;
	}

	/**
	 * Computes c*2^64 mod n.
	 * @param c
	 * @param n
	 * @return c*2^64 mod n
	 */
	long u64div(long c, long n)
	{
		// optimizing on lo=0 does not yield any notable performance gain
		return new Uint128(c, 0L).spDivide(n)[1];
	}

	/**
	 * Compute u*v mod m.
	 * @param u
	 * @param v
	 * @param m
	 * @return u*v mod m
	 */
	long spMulMod(long u, long v, long m)
	{
		return Uint128.mul64(u, v).spDivide(m)[1];
	}

	long spGCD(long x, long y)
	{
		long a, b, c;
		a = x; b = y;
		while (b != 0)
		{
			c = Long.remainderUnsigned(a, b);
			a = b;
			b = c;
		}
		return a;
	}

	int spRand(int lower, int upper)
	{
		if (DEBUG) LOG.debug("LCGSTATE=" + LCGSTATE);
		
		// fix rng for negative upper values;
		long upperl = (long) upper;
		if (upperl<0) upperl += (1L<<32);
		long diff = upperl - lower;
		if (DEBUG) LOG.debug("lower=" + lower + ", upper=" + upperl + ", diff=" + diff);
		
		// advance the state of the LCG and return the appropriate result
		LCGSTATE = 6364136223846793005L * LCGSTATE + 1442695040888963407L;
		long LCGSTATE_shifted = LCGSTATE >>> 32;
		if (DEBUG) LOG.debug("LCGSTATE=" + LCGSTATE + ", LCGSTATE_shifted=" + LCGSTATE_shifted);
		
		double quot = (double)LCGSTATE_shifted / 4294967296.0; // dividend is 2^32
		double prod = diff * quot;
		int rand = (int)(0xFFFFFFFF & (long)prod); // (int)prod does not work for prod >= 2^31
		int result = lower + rand;
		if (DEBUG) LOG.debug("quot=" + quot + ", prod=" + prod + ", rand=" + rand + ", result=" + result);
		return result;
	}

	void add(long rho, ecm_work work, ecm_pt P1, ecm_pt P2, ecm_pt Pin, ecm_pt Pout)
	{
		// compute:
		//x+ = z- * [(x1-z1)(x2+z2) + (x1+z1)(x2-z2)]^2
		//z+ = x- * [(x1-z1)(x2+z2) - (x1+z1)(x2-z2)]^2
		// where:
		//x- = original x
		//z- = original z
		// given the sums and differences of the original points (stored in work structure).
		work.diff1 = submod(P1.X, P1.Z, work.n);
		work.sum1 = addmod(P1.X, P1.Z, work.n);
		work.diff2 = submod(P2.X, P2.Z, work.n);
		work.sum2 = addmod(P2.X, P2.Z, work.n);

		work.tt1 = montMul63(work.diff1, work.sum2, work.n, rho);	//U
		work.tt2 = montMul63(work.sum1, work.diff2, work.n, rho);	//V

		work.tt3 = addmod(work.tt1, work.tt2, work.n);
		work.tt4 = submod(work.tt1, work.tt2, work.n);
		long x = work.tt3;
		work.tt1 = montMul63(x, x, work.n, rho);
		long x1 = work.tt4;	//(U + V)^2
		work.tt2 = montMul63(x1, x1, work.n, rho);	//(U - V)^2

		if (Pin == Pout)
		{
			// Pin and Pout are the same object. Avoid changing X before it is used in the second operation...
			Pout.Z = montMul63(work.tt1, Pin.Z, work.n, rho);		//Z * (U + V)^2
			Pout.X = montMul63(work.tt2, Pin.X, work.n, rho);		//X * (U - V)^2
			final long tmp = Pout.Z;
			Pout.Z = Pout.X;
			Pout.X = tmp;
		}
		else
		{
			Pout.X = montMul63(work.tt1, Pin.Z, work.n, rho);		//Z * (U + V)^2
			Pout.Z = montMul63(work.tt2, Pin.X, work.n, rho);		//X * (U - V)^2
		}
		return;
	}

	/**
	 * 
	 * @param rho
	 * @param work
	 * @param insum
	 * @param indiff
	 * @param P input/output
	 */
	void dup(long rho, ecm_work work, long insum, long indiff, ecm_pt P)
	{
		work.tt1 = montMul63(indiff, indiff, work.n, rho);			// U=(x1 - z1)^2
		work.tt2 = montMul63(insum, insum, work.n, rho);			// V=(x1 + z1)^2
		P.X = montMul63(work.tt1, work.tt2, work.n, rho);			// x=U*V

		work.tt3 = submod(work.tt2, work.tt1, work.n);			// w = V-U
		work.tt2 = montMul63(work.tt3, work.s, work.n, rho);		// w = (A+2)/4 * w
		work.tt2 = addmod(work.tt2, work.tt1, work.n);			// w = w + U
		P.Z = montMul63(work.tt2, work.tt3, work.n, rho);			// Z = w*(V-U)
		return;
	}

	void prac70(long rho, ecm_work work, ecm_pt P)
	{
		long s1, s2, d1, d2;
		long swp;
		int i;

		for (i = 0; i < 116; i++)
		{
			if (prac70Steps[i] == 0)
			{
				work.pt1.X = work.pt2.X = work.pt3.X = P.X;
				work.pt1.Z = work.pt2.Z = work.pt3.Z = P.Z;

				d1 = submod(work.pt1.X, work.pt1.Z, work.n);
				s1 = addmod(work.pt1.X, work.pt1.Z, work.n);
				dup(rho, work, s1, d1, work.pt1);
			}
			else if (prac70Steps[i] == 3)
			{
				// integrate step 4 followed by swap(1,2)
				add(rho, work, work.pt2, work.pt1, work.pt3, work.pt4);		// T = B + A (C)

				swp = work.pt1.X;
				work.pt1.X = work.pt4.X;
				work.pt4.X = work.pt3.X;
				work.pt3.X = work.pt2.X;
				work.pt2.X = swp;
				swp = work.pt1.Z;
				work.pt1.Z = work.pt4.Z;
				work.pt4.Z = work.pt3.Z;
				work.pt3.Z = work.pt2.Z;
				work.pt2.Z = swp;
			}
			else if (prac70Steps[i] == 4)
			{
				add(rho, work, work.pt2, work.pt1, work.pt3, work.pt4);		// T = B + A (C)

				swp = work.pt2.X;
				work.pt2.X = work.pt4.X;
				work.pt4.X = work.pt3.X;
				work.pt3.X = swp;
				swp = work.pt2.Z;
				work.pt2.Z = work.pt4.Z;
				work.pt4.Z = work.pt3.Z;
				work.pt3.Z = swp;
			}
			else if (prac70Steps[i] == 5)
			{
				d2 = submod(work.pt1.X, work.pt1.Z, work.n);
				s2 = addmod(work.pt1.X, work.pt1.Z, work.n);

				add(rho, work, work.pt2, work.pt1, work.pt3, work.pt2);		// B = B + A (C)
				dup(rho, work, s2, d2, work.pt1);		// A = 2A
			}
			else if (prac70Steps[i] == 6)
			{
				add(rho, work, work.pt1, work.pt2, work.pt3, P);		// A = A + B (C)
			}
		}

		return;
	}

	void prac85(long rho, ecm_work work, ecm_pt P)
	{
		long s1, s2, d1, d2;
		long swp;
		int i;

		for (i = 0; i < 146; i++)
		{
			if (prac85Steps[i] == 0)
			{
				work.pt1.X = work.pt2.X = work.pt3.X = P.X;
				work.pt1.Z = work.pt2.Z = work.pt3.Z = P.Z;

				d1 = submod(work.pt1.X, work.pt1.Z, work.n);
				s1 = addmod(work.pt1.X, work.pt1.Z, work.n);
				dup(rho, work, s1, d1, work.pt1);
			}
			else if (prac85Steps[i] == 3)
			{
				// integrate step 4 followed by swap(1,2)
				add(rho, work, work.pt2, work.pt1, work.pt3, work.pt4);		// T = B + A (C)

				swp = work.pt1.X;
				work.pt1.X = work.pt4.X;
				work.pt4.X = work.pt3.X;
				work.pt3.X = work.pt2.X;
				work.pt2.X = swp;
				swp = work.pt1.Z;
				work.pt1.Z = work.pt4.Z;
				work.pt4.Z = work.pt3.Z;
				work.pt3.Z = work.pt2.Z;
				work.pt2.Z = swp;
			}
			else if (prac85Steps[i] == 4)
			{
				add(rho, work, work.pt2, work.pt1, work.pt3, work.pt4);		// T = B + A (C)

				swp = work.pt2.X;
				work.pt2.X = work.pt4.X;
				work.pt4.X = work.pt3.X;
				work.pt3.X = swp;
				swp = work.pt2.Z;
				work.pt2.Z = work.pt4.Z;
				work.pt4.Z = work.pt3.Z;
				work.pt3.Z = swp;
			}
			else if (prac85Steps[i] == 5)
			{
				d2 = submod(work.pt1.X, work.pt1.Z, work.n);
				s2 = addmod(work.pt1.X, work.pt1.Z, work.n);

				add(rho, work, work.pt2, work.pt1, work.pt3, work.pt2);		// B = B + A (C)
				dup(rho, work, s2, d2, work.pt1);		// A = 2A
			}
			else if (prac85Steps[i] == 6)
			{
				add(rho, work, work.pt1, work.pt2, work.pt3, P);		// A = A + B (C)
			}
		}

		return;
	}

	void prac(long rho, ecm_work work, ecm_pt P, long c, double v)
	{
		long d, e, r;
		long s1, s2, d1, d2;
		long swp;

		d = c;
		r = (long)((double)d * v + 0.5);

		s1 = work.sum1;
		s2 = work.sum2;
		d1 = work.diff1;
		d2 = work.diff2;

		d = c - r;
		e = 2 * r - c;

		// the first one is always a doubling
		// point1 is [1]P
		work.pt1.X = work.pt2.X = work.pt3.X = P.X;
		work.pt1.Z = work.pt2.Z = work.pt3.Z = P.Z;

		d1 = submod(work.pt1.X, work.pt1.Z, work.n);
		s1 = addmod(work.pt1.X, work.pt1.Z, work.n);

		// point2 is [2]P
		dup(rho, work, s1, d1, work.pt1);

		while (d != e)
		{
			if (d+Long.MIN_VALUE < e+Long.MIN_VALUE)
			{
				r = d;
				d = e;
				e = r;
				swp = work.pt1.X;
				work.pt1.X = work.pt2.X;
				work.pt2.X = swp;
				swp = work.pt1.Z;
				work.pt1.Z = work.pt2.Z;
				work.pt2.Z = swp;
			}

			if ((d + 3)/4 + Long.MIN_VALUE <= e+Long.MIN_VALUE)
			{
				d -= e;

				add(rho, work, work.pt2, work.pt1, work.pt3, work.pt4);		// T = B + A (C)

				swp = work.pt2.X;
				work.pt2.X = work.pt4.X;
				work.pt4.X = work.pt3.X;
				work.pt3.X = swp;
				swp = work.pt2.Z;
				work.pt2.Z = work.pt4.Z;
				work.pt4.Z = work.pt3.Z;
				work.pt3.Z = swp;
			}
			else if ((d + e) % 2 == 0)
			{
				d = (d - e) >>> 1;

				d2 = submod(work.pt1.X, work.pt1.Z, work.n);
				s2 = addmod(work.pt1.X, work.pt1.Z, work.n);

				add(rho, work, work.pt2, work.pt1, work.pt3, work.pt2);		// B = B + A (C)
				dup(rho, work, s2, d2, work.pt1);		// A = 2A
			}
			else
			{
				// empirically, tiny B1 values only need the above prac cases.
				// just in case, fall back on this.
				LOG.error("unhandled case in prac");
				System.exit(1);
			}
		}

		add(rho, work, work.pt1, work.pt2, work.pt3, P);		// A = A + B (C)

		return;
	}

	/**
	 * Compute the modular inverse x of a mod p, i.e. x = (1/a) mod p.
	 * @param a
	 * @param p modulus
	 * @return (1/a) mod p
	 */
	long modinv_64(long a, long p) {

		/* thanks to the folks at www.mersenneforum.org */

		long ps1, ps2, parity, dividend, divisor, rem, q, t;

		q = 1;
		rem = a;
		dividend = p;
		divisor = a;
		ps1 = 1;
		ps2 = 0;
		parity = 0;

		while (divisor > 1) {
			rem = dividend - divisor;
			t = rem - divisor;
			if (rem >= divisor) {
				q += ps1; rem = t; t -= divisor;
				if (rem >= divisor) {
					q += ps1; rem = t; t -= divisor;
					if (rem >= divisor) {
						q += ps1; rem = t; t -= divisor;
						if (rem >= divisor) {
							q += ps1; rem = t; t -= divisor;
							if (rem >= divisor) {
								q += ps1; rem = t; t -= divisor;
								if (rem >= divisor) {
									q += ps1; rem = t; t -= divisor;
									if (rem >= divisor) {
										q += ps1; rem = t; t -= divisor;
										if (rem >= divisor) {
											q += ps1; rem = t;
											if (rem >= divisor) {
												q = dividend / divisor;
												rem = dividend % divisor;
												q *= ps1;
											}
										}
									}
								}
							}
						}
					}
				}
			}

			q += ps2;
			parity = ~parity;
			dividend = divisor;
			divisor = rem;
			ps2 = ps1;
			ps1 = q;
		}

		if (parity == 0)
			return ps1;
		else
			return p - ps1;
	}

	void build(ecm_pt P, long rho, ecm_work work, int sigma)
	{
		long t1, t2, t3, t4;
		long u, v, n;
		n = work.n;

		if (DEBUG) LOG.debug("Pin=" + P.X + ", " + P.Z);
		
		if (sigma == 0)
		{
			work.sigma = spRand(7, (int)-1);
			if (DEBUG) LOG.debug("random sigma=" + work.sigma);
		}
		else
		{
			work.sigma = sigma;
			if (DEBUG) LOG.debug("use existing sigma=" + work.sigma);
		}
		sigma = work.sigma;

		if (DEBUG) LOG.debug("n=" + n);
		u = Integer.toUnsignedLong(sigma); // a simple cast would go wrong for sigma having negative signed int values
		u = u64div(u, n);
		if (DEBUG) LOG.debug("u=" + u);
		
		t1 = 4;
		t1 = u64div(t1, n);
		if (DEBUG) LOG.debug("t1=" + t1);

		v = montMul63(u, t1, n, rho);		// v = 4*sigma
		if (DEBUG) LOG.debug("v=" + v);

		u = montMul63(u, u, n, rho);
		if (DEBUG) LOG.debug("u=" + u);
		
		t1 = 5;
		t1 = u64div(t1, n);
		if (DEBUG) LOG.debug("t1=" + t1);
		
		u = submod(u, t1, n);			// u = sigma^2 - 5
		if (DEBUG) LOG.debug("u=" + u);

		t1 = montMul63(u, u, n, rho);
		if (DEBUG) LOG.debug("t1=" + t1);
		P.X = montMul63(t1, u, n, rho);	// x = u^3
		if (DEBUG) LOG.debug("P.X=" + P.X);

		t1 = montMul63(v, v, n, rho);
		P.Z = montMul63(t1, v, n, rho);	// z = v^3
		if (DEBUG) LOG.debug("P.Z=" + P.Z);

		//compute parameter A
		t1 = submod(v, u, n);			// (v - u)
		t2 = montMul63(t1, t1, n, rho);
		t4 = montMul63(t2, t1, n, rho);	// (v - u)^3

		t1 = 3;
		t1 = u64div(t1, n);
		t2 = montMul63(t1, u, n, rho);	// 3u
		t3 = addmod(t2, v, n);			// 3u + v

		t1 = montMul63(t3, t4, n, rho);	// a = (v-u)^3 * (3u + v)

		t2 = 16;
		t2 = u64div(t2, n);
		t3 = montMul63(P.X, t2, n, rho);	// 16*u^3
		t4 = montMul63(t3, v, n, rho);	// 16*u^3*v

		// u holds the denom, t1 holds the numer
		// accomplish the division by multiplying by the modular inverse
		t2 = 1;
		t4 = montMul63(t4, t2, n, rho);	// take t4 out of monty rep
		t1 = montMul63(t1, t2, n, rho);	// take t1 out of monty rep

		t3 = modinv_64(t4, n);
		if (DEBUG) {
			LOG.debug("t4=" + t4 + ", n=" + n + ", modinv t3 =" + t3);
			assertTrue(t4 < n);
			BigInteger t4Big = new BigInteger(Long.toUnsignedString(t4));
			BigInteger nBig = new BigInteger(Long.toUnsignedString(n));
			BigInteger t3Big = new BigInteger(Long.toUnsignedString(t3));
			BigInteger t3Correct = t4Big.modInverse(nBig);
			if (!t3Big.equals(t3Correct)) {
				LOG.debug("1/" + t4Big + " mod " + nBig + " gave " + t3Big + ", but correct is " + t3Correct + "!");
			}
		}
		
		work.s = spMulMod(t3, t1, n);
		if (DEBUG) LOG.debug("work.s=" + work.s);
		work.s = u64div(work.s, n);
		if (DEBUG) LOG.debug("work.s=" + work.s);
		return;
	}

	public static class EcmResult {
		public long f;
		public int curve;
		
		public EcmResult(long f, int curve) {
			this.f = f;
			this.curve = curve;
		}
	}
	
	EcmResult tinyecm(long n, int B1, int curves)
	{
		//attempt to factor n with the elliptic curve method
		//following brent and montgomery's papers, and CP's book
		int curve;
		long result;
		ecm_work work = new ecm_work();
		ecm_pt P = new ecm_pt();
		int sigma;
		long rho, x;

		x = (((n + 2) & 4) << 1) + n; // here x*a==1 mod 2**4
		x *= 2 - n * x;               // here x*a==1 mod 2**8
		x *= 2 - n * x;               // here x*a==1 mod 2**16
		x *= 2 - n * x;               // here x*a==1 mod 2**32         
		x *= 2 - n * x;               // here x*a==1 mod 2**64
		rho = (long)0 - x;
		if (DEBUG) LOG.debug("rho = " + Long.toUnsignedString(rho));
		
		work.n = n;

		work.stg1_max = B1;
		// pre-paired sequences have been prepared for this B2, so it is not an input

		for (curve = 0; curve < curves; curve++)
		{
			if (DEBUG) LOG.debug("curve=" + curve);
			sigma = 0;
			if (DEBUG) LOG.debug("1: P.X=" + P.X + ", P.Z=" + P.Z);
			build(P, rho, work, sigma);
			if (DEBUG) LOG.debug("curve=" + curve + ": build finished");
			if (DEBUG) LOG.debug("2: P.X=" + P.X + ", P.Z=" + P.Z);
			
			P = ecm_stage1(rho, work, P);
			if (DEBUG) LOG.debug("curve=" + curve + ": stage1 finished");
			if (DEBUG) LOG.debug("3: P.X=" + P.X + ", P.Z=" + P.Z);
			result = check_factor(P.Z, n);

			if (result > 1)
			{
				return new EcmResult(result, curve + 1);
			}

			ecm_stage2(P, rho, work);
			if (DEBUG) LOG.debug("curve=" + curve + ": stage2 finished");
			if (DEBUG) LOG.debug("4: P.X=" + P.X + ", P.Z=" + P.Z);
			result = check_factor(work.stg2acc, n);

			if (result > 1)
			{
				return new EcmResult(result, curve + 1);
			}

		}

		if (DEBUG) LOG.warn("Failed to find a factor of N=" + n);
		return new EcmResult(1, curve);
	}

	ecm_pt ecm_stage1(long rho, ecm_work work, ecm_pt P)
	{
		long q;
		long stg1 = (long)work.stg1_max;

		// handle the only even case 
		if (DEBUG) LOG.debug("P.X=" + P.X + ", P.Z=" + P.Z);
		q = 2;
		while (q+Long.MIN_VALUE < stg1+Long.MIN_VALUE)
		{
			work.diff1 = submod(P.X, P.Z, work.n);
			work.sum1 = addmod(P.X, P.Z, work.n);
			dup(rho, work, work.sum1, work.diff1, P);
			if (DEBUG) LOG.debug("q=" + q + ": P.X=" + P.X + ", P.Z=" + P.Z);
			q *= 2;
		}
		if (DEBUG) LOG.debug("stg1=" + stg1 + ", q=" + q + ", diff1=" + work.diff1 + ", sum1= " + work.sum1);
		if (DEBUG) LOG.debug("P.X=" + P.X + ", P.Z=" + P.Z);
		
		if (stg1 == 70)
		{
			prac70(rho, work, P);
		}
		else if (stg1+Long.MIN_VALUE >= 85+Long.MIN_VALUE)
		{
			// call prac with best ratios found by a deep search.
			// some composites are cheaper than their constituent primes.
			prac85(rho, work, P);
			if (stg1+Long.MIN_VALUE < 100+Long.MIN_VALUE)
			{
				// paired into a composite for larger bounds
				prac(rho, work, P, 61, 0.522786351415446049);
			}

			if (stg1+Long.MIN_VALUE >= 125+Long.MIN_VALUE)
			{
				prac(rho, work, P, 5, 0.618033988749894903);
				prac(rho, work, P, 11, 0.580178728295464130);
				prac(rho, work, P, 61, 0.522786351415446049);
				prac(rho, work, P, 89, 0.618033988749894903);
				prac(rho, work, P, 97, 0.723606797749978936);
				prac(rho, work, P, 101, 0.556250337855490828);
				prac(rho, work, P, 107, 0.580178728295464130);
				prac(rho, work, P, 109, 0.548409048446403258);
				prac(rho, work, P, 113, 0.618033988749894903);

				if (stg1+Long.MIN_VALUE < 130+Long.MIN_VALUE)
				{
					prac(rho, work, P, 103, 0.632839806088706269);
					
				}
			}
			
			if (stg1+Long.MIN_VALUE >= 165+Long.MIN_VALUE)
			{
				prac(rho, work, P, 7747, 0.552188778811121); // 61 x 127
				prac(rho, work, P, 131, 0.618033988749894903);
				prac(rho, work, P, 14111, 0.632839806088706);	// 103 x 137
				prac(rho, work, P, 20989, 0.620181980807415);	// 139 x 151
				prac(rho, work, P, 157, 0.640157392785047019);
				prac(rho, work, P, 163, 0.551390822543526449);

				if (stg1+Long.MIN_VALUE < 200+Long.MIN_VALUE)
				{
					prac(rho, work, P, 149, 0.580178728295464130);
				}
			}
			
			if (stg1+Long.MIN_VALUE >= 205+Long.MIN_VALUE)
			{
				prac(rho, work, P, 13, 0.618033988749894903);
				prac(rho, work, P, 167, 0.580178728295464130);
				prac(rho, work, P, 173, 0.612429949509495031);
				prac(rho, work, P, 179, 0.618033988749894903);
				prac(rho, work, P, 181, 0.551390822543526449);
				prac(rho, work, P, 191, 0.618033988749894903);
				prac(rho, work, P, 193, 0.618033988749894903);
				prac(rho, work, P, 29353, 0.580178728295464);	// 149 x 197
				prac(rho, work, P, 199, 0.551390822543526449);
			}
		}

		return P;
	}

	void ecm_stage2(ecm_pt P, long rho, ecm_work work)
	{
		int b;
		int i, j, k;
		ecm_pt Pa = work.Pa;
		ecm_pt[] Pb = work.Pb;
		ecm_pt Pd;
		long acc = work.stg2acc;
		byte[] barray = null;
		int numb = 0;

		//stage 2 init
		//Q = P = result of stage 1
		//compute [d]Q for 0 < d <= D
		Pd = Pb[map[60]];

		// [1]Q
		Pb[1].Z = P.Z;
		Pb[1].X = P.X;
		work.Pbprod[1] = montMul63(Pb[1].X, Pb[1].Z, work.n, rho);

		// [2]Q
		Pb[2].Z = P.Z;
		Pb[2].X = P.X;
		work.diff1 = submod(P.X, P.Z, work.n);
		work.sum1 = addmod(P.X, P.Z, work.n);
		dup(rho, work, work.sum1, work.diff1, Pb[2]);
		work.Pbprod[2] = montMul63(Pb[2].X, Pb[2].Z, work.n, rho);

		// Calculate all Pb: the following is specialized for D=60
		// [2]Q + [1]Q([1]Q) = [3]Q
		add(rho, work, Pb[1], Pb[2], Pb[1], Pb[3]);		// <-- temporary

		// 2*[3]Q = [6]Q
		work.diff1 = submod(Pb[3].X, Pb[3].Z, work.n);
		work.sum1 = addmod(Pb[3].X, Pb[3].Z, work.n);
		dup(rho, work, work.sum1, work.diff1, work.pt3);	// pt3 = [6]Q

		// [3]Q + [2]Q([1]Q) = [5]Q
		add(rho, work, Pb[3], Pb[2], Pb[1], work.pt1);	// <-- pt1 = [5]Q
		Pb[3].X = work.pt1.X;
		Pb[3].Z = work.pt1.Z;

		// [6]Q + [5]Q([1]Q) = [11]Q
		add(rho, work, work.pt3, work.pt1, Pb[1], Pb[4]);	// <-- [11]Q

		i = 3;
		k = 4;
		j = 5;
		while ((j + 12) < (60))
		{
			// [j+6]Q + [6]Q([j]Q) = [j+12]Q
			add(rho, work, work.pt3, Pb[k], Pb[i], Pb[map[j + 12]]);
			i = k;
			k = map[j + 12];
			j += 6;
		}

		// [6]Q + [1]Q([5]Q) = [7]Q
		add(rho, work, work.pt3, Pb[1], work.pt1, Pb[3]);	// <-- [7]Q
		i = 1;
		k = 3;
		j = 1;
		while ((j + 12) < (60))
		{
			// [j+6]Q + [6]Q([j]Q) = [j+12]Q
			add(rho, work, work.pt3, Pb[k], Pb[i], Pb[map[j + 12]]);
			i = k;
			k = map[j + 12];
			j += 6;
		}

		// Pd = [2w]Q
		// [31]Q + [29]Q([2]Q) = [60]Q
		add(rho, work, Pb[9], Pb[10], Pb[2], Pd);	// <-- [60]Q

		// make all of the Pbprod's
		for (i = 3; i < 19; i++)
		{
			work.Pbprod[i] = montMul63(Pb[i].X, Pb[i].Z, work.n, rho);
		}

		//initialize info needed for giant step
		// temporary - make [4]Q
		work.diff1 = submod(Pb[2].X, Pb[2].Z, work.n);
		work.sum1 = addmod(Pb[2].X, Pb[2].Z, work.n);
		dup(rho, work, work.sum1, work.diff1, work.pt3);	// pt3 = [4]Q

		// Pd = [w]Q
		// [17]Q + [13]Q([4]Q) = [30]Q
		add(rho, work, Pb[map[17]], Pb[map[13]], work.pt3, work.Pad);	// <-- [30]Q

		// [60]Q + [30]Q([30]Q) = [90]Q
		add(rho, work, Pd, work.Pad, work.Pad, Pa);
		work.pt1.X = Pa.X;
		work.pt1.Z = Pa.Z;
		
		// [90]Q + [30]Q([60]Q) = [120]Q
		add(rho, work, Pa, work.Pad, Pd, Pa);
		Pd.X = Pa.X;
		Pd.Z = Pa.Z;

		// [120]Q + [30]Q([90]Q) = [150]Q
		add(rho, work, Pa, work.Pad, work.pt1, Pa);

		// adjustment of Pa and Pad for larger B1.
		// Currently we have Pa=150, Pd=120, Pad=30
		if (work.stg1_max == 165)
		{
			// need Pa = 180, Pad = 60
			// [150]Q + [30]Q([120]Q) = [180]Q
			add(rho, work, Pa, work.Pad, Pd, Pa);

			work.diff1 = submod(work.Pad.X, work.Pad.Z, work.n);
			work.sum1 = addmod(work.Pad.X, work.Pad.Z, work.n);
			dup(rho, work, work.sum1, work.diff1, work.Pad);	// Pad = [60]Q
		}
		else if (work.stg1_max == 205)
		{
			// need Pa = 210, Pad = 90.
			// have pt1 = 90

			work.diff1 = submod(work.Pad.X, work.Pad.Z, work.n);
			work.sum1 = addmod(work.Pad.X, work.Pad.Z, work.n);
			dup(rho, work, work.sum1, work.diff1, work.Pad);	// Pad = [60]Q

			// [150]Q + [60]Q([90]Q) = [210]Q
			add(rho, work, Pa, work.Pad, work.pt1, Pa);
			work.Pad.X = work.pt1.X;
			work.Pad.Z = work.pt1.Z;
		}

		//initialize accumulator and Paprod
		acc = u64div(1, work.n);
		work.Paprod = montMul63(Pa.X, Pa.Z, work.n, rho);

		if (DEBUG) LOG.debug("stg1_max = " + work.stg1_max);
		if (work.stg1_max == 70)
		{
			barray = b1_70;
			numb = numb1_70;
		}
		else if (work.stg1_max == 85)
		{
			barray = b1_85;
			numb = numb1_85;
		}
		else if (work.stg1_max == 125)
		{
			barray = b1_125;
			numb = numb1_125;
		}
		else if (work.stg1_max == 165)
		{
			barray = b1_165;
			numb = numb1_165;
		}
		else if (work.stg1_max == 205)
		{
			barray = b1_205;
			numb = numb1_205;
		}

		if (DEBUG) LOG.debug("numb = " + numb);
		for (i = 0; i < numb; i++)
		{
			//LOG.debug("i=" + i + ", barray[i]=" + barray[i]);
			if (barray[i] == 0)
			{
				//giant step - use the addition formula for ECM
				work.pt1.X = Pa.X;
				work.pt1.Z = Pa.Z;

				//Pa + Pd
				add(rho, work, Pa, Pd, work.Pad, Pa);

				//Pad holds the previous Pa
				work.Pad.X = work.pt1.X;
				work.Pad.Z = work.pt1.Z;
				if (DEBUG) LOG.debug("Pad.X=" + work.Pad.X + ", Pad.Z=" + work.Pad.Z);
				
				//and Paprod
				work.Paprod = montMul63(Pa.X, Pa.Z, work.n, rho);

				i++;
			}

			//we accumulate XrZd - XdZr = (Xr - Xd) * (Zr + Zd) + XdZd - XrZr
			//in CP notation, Pa -> (Xr,Zr), Pb -> (Xd,Zd)

			b = barray[i];
			if (DEBUG) if (b==61) LOG.debug("b=" + b + ", map[b]=" + map[b]);
			
			// accumulate the cross product  (zimmerman syntax).
			// page 342 in C&P
			work.tt1 = submod(Pa.X, Pb[map[b]].X, work.n);
			work.tt2 = addmod(Pa.Z, Pb[map[b]].Z, work.n);
			work.tt3 = montMul63(work.tt1, work.tt2, work.n, rho);
			work.tt1 = addmod(work.tt3, work.Pbprod[map[b]], work.n);
			work.tt2 = submod(work.tt1, work.Paprod, work.n);
			acc = montMul63(acc, work.tt2, work.n, rho);
		}

		work.stg2acc = acc;

		return;
	}
	
	/**
	 * Montgomery multiplication of a*b mod n with regard to R=2^63. ("mulredc63x" in Yafu)
	 * @param a
	 * @param b
	 * @param N
	 * @param Nhat complement of N mod 2^63
	 * @return Montgomery multiplication of a*b mod n
	 */
	public static long montMul63(long a, long b, long N, long Nhat) {
		// Step 1: Compute a*b
		Uint128 ab = Uint128.mul63(a, b);
		// Step 2: Compute t = ab * (-1/N) mod R
		// Since R=2^64, "x mod R" just means to get the low part of x.
		// That would give t = Uint128.mul64(ab.getLow(), minusNInvModR).getLow();
		// but even better, the long product just gives the low part -> we can get rid of one expensive mul64().
		long t = ab.getLow() * Nhat;
		// Step 3: Compute r = (a*b + t*N) / R
		// Since R=2^64, "x / R" just means to get the high part of x.
		long r = ab.add_getHigh(Uint128.mul63(t, N));
		// If the correct result is c, then now r==c or r==c+N.
		r = r<N ? r : r-N; // required at ecm

		if (DEBUG) {
			//LOG.debug(a + " * " + b + " = " + r);
			assertTrue(a >= 0 && a<N);
			assertTrue(b >= 0 && b<N);
			assertTrue(r >= 0 && r < N);
		}
		
		return r;
	}

	/**
	 * Test if Z is a factor of N.
	 * @param Z
	 * @param n
     * @return factor or 0 if no factor
	 */
	long check_factor(long Z, long n)
	{
		long f = spGCD(Z, n);
		if (DEBUG) LOG.debug("check_factor: gcd(" + Z + ", " + n + ") = " + f);
        return (f>1 && f<n) ? f : 0;
	}

	public BigInteger findSingleFactor(BigInteger N) {
		Random rng = new Random();
		rng.setSeed(42);
//		LCGSTATE = 65537 * rng.nextInt(); // original rng is not comparable with C version
		LCGSTATE = 4295098403L; // rng comparable with C version
		if (DEBUG) LOG.debug("LCGSTATE = " + LCGSTATE);
		
		int NBits = N.bitLength();
		if (NBits > MAX_BITS_SUPPORTED) throw new IllegalArgumentException("N=" + N + " has " + NBits + " bit, but tinyEcm only supports arguments <= " + MAX_BITS_SUPPORTED + " bit.");
		// TODO Try to make it work for 63, 64 bit numbers
		if (DEBUG) LOG.debug("N=" + N + " has " + NBits + " bits");
		
		// parameters for N <= 50 bit
		int curves;
		int B1;
		if (NBits <= 50) {
			curves = 24;
			B1 = 70;
		} else if (NBits <= 52) {
			B1 = 85;
			curves = 24;
		} else if (NBits <= 56) {
			B1 = 125;
			curves = 24;
		} else if (NBits <= 60) {
			B1 = 165;
			curves = 32;
		} else { // here the original tinyecm.c bound was < 64 bit
			B1 = 205;
			curves = 40;
		} // else for NBits >= 64 bit, tinyecm.c had the parameters  B1 = 1000; curves = 64;

		if (DEBUG) LOG.debug("B1=" + B1 + ", curves=" + curves);
		
		if (DEBUG) LOG.debug("Try to factor N=" + N);
		EcmResult result = tinyecm(N.longValue(), B1, curves);
		return BigInteger.valueOf(result.f);
	}
	
	public static void main(String[] args) {
		ConfigUtil.initProject();
		
		TinyEcm63 factorizer = new TinyEcm63();
		long[] testNumbers = new long[] { 
				1234577*12345701L,
				// Failures before map[] fix
				1253586675305333L,
				1139151196120601L,
				1553712951089947L,
				2235885271339597L,
				1586929215386303L,
				// Failures before spRand() fix
				930705057210221L,
				1067332898136023L,
				8311092540494299L,
				23603982383629381L,
				58725827789610857L,
				369313815090910177L,
				// Failures before int to long cast fix
				41382606407163353L,
				306358296309770459L,
				// Failures because #curves is too small
				474315852287951L,
				9400170223537253L,
				35239016917581299L,
				37915240075398767L,
				459926431465210403L,
				752882545886305349L,
				179503729521451L,
				1059150637518581L,
				3209190314462729L,
				17586811742837503L,
				13745855671622359L,
				15727038894518533L,
				66804960995707271L,
				38704493646912997L,
				56025872236672099L,
				57675022504187287L,
				69916262762899909L,
				51113648728234999L,
				55878279398722441L,
		};
		
		for (int i=0; i<testNumbers.length; i++) {
			long N = testNumbers[i];
			BigInteger factor = factorizer.findSingleFactor(BigInteger.valueOf(N));
			LOG.info("Found factor " + factor + " of N=" + N);
		}
	}
}
