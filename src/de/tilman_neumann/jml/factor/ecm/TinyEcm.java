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
package de.tilman_neumann.jml.factor.ecm;

import java.math.BigInteger;
import java.util.Random;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.base.Uint128;
import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.util.ConfigUtil;

public class TinyEcm extends FactorAlgorithm {

	private static final Logger LOG = Logger.getLogger(TinyEcm.class);

	private static final boolean DEBUG = false;
	
	private static final int D = 120;
	
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

	public String getName() {
		return "tinyEcm";
	}
	
	/**
	 * @param x
	 * @param y
	 * @param n
	 * @param nhat
	 * @return x*y mod n
	 */
	long mulredcx(long x, long y, long n, long nhat)
	{
//		if ((n & 0x8000000000000000L) != 0)
//		{
//			__asm__(
//				"mulx %2, %%r10, %%r11	\n\t"
//				"movq %%r10, %%rax		\n\t"
//				"xorq %%r8, %%r8 \n\t"
//				"xorq %%r12, %%r12 \n\t"
//				"mulq %3 \n\t"
//				"mulq %4 \n\t"
//				"addq %%r10, %%rax \n\t"
//				"adcq %%r11, %%rdx \n\t"
//				"cmovae %4, %%r12 \n\t"
//				"subq %4, %%rdx \n\t"
//				"cmovc %%r12, %%r8 \n\t"
//				"addq %%r8, %%rdx \n\t"
//				: "=&d"(x)
//				: "0"(x), "r"(y), "r"(nhat), "r"(n)
//				: "rax", "r8", "r10", "r11", "r12", "cc");
//		}
//		else
//		{
//			__asm__(
//				"mulx %2, %%r10, %%r11	\n\t"
//				"movq %3, %%rax		\n\t"
//				"xorq %%r8, %%r8 \n\t"
//				"mulq %%r10 \n\t"
//				"mulq %4 \n\t"
//				"addq %%r10, %%rax \n\t"
//				"adcq %%r11, %%rdx \n\t"
//				"subq %4, %%rdx \n\t"
//				"cmovc %4, %%r8 \n\t"
//				"addq %%r8, %%rdx \n\t"
//				: "=d"(x)
//				: "0"(x), "r"(y), "r"(nhat), "r"(n)
//				: "rax", "r8", "r10", "r11", "cc");
//
//		}
//		return x;
		return Uint128.montMul64(x, y, n, nhat);
	}

	/**
	 * x^2 mod n.
	 * @param x
	 * @param n
	 * @param nhat
	 * @return x^2 mod n
	 */
	long sqrredcx(long x, long n, long nhat) {
		return mulredcx(x, x, n, nhat);
	}

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
		long tt5;
		long s;
		long n;
		ecm_pt pt1 = new ecm_pt();
		ecm_pt pt2 = new ecm_pt();
		ecm_pt pt3 = new ecm_pt();
		ecm_pt pt4 = new ecm_pt();
		ecm_pt pt5 = new ecm_pt();
		int sigma;

		ecm_pt Pa = new ecm_pt();
		ecm_pt Pd = new ecm_pt();
		ecm_pt Pad = new ecm_pt();
		ecm_pt[] Pb = new ecm_pt[20];
		long Paprod;
		long[] Pbprod = new long[20];

		long stg2acc;
		long A;
		int last_pid;
		int stg1_max;
		int stg2_max;
		
		public ecm_work() {
			for (int i=0; i<20; i++) {
				Pb[i] = new ecm_pt();
			}
		}
	}

	private static final int[] map = {
		0, 1, 2, 0, 0, 0, 0, 3, 0, 0,
		0, 4, 0, 5, 0, 0, 0, 6, 0, 7,
		0, 0, 0, 8, 0, 0, 0, 0, 0, 9,
		0, 10, 0, 0, 0, 0, 0, 11, 0, 0,
		0, 12, 0, 13, 0, 0, 0, 14, 0, 15,
		0, 0, 0, 16, 0, 0, 0, 0, 0, 17,
		18 };

	private static final int NUMP = 801;
	
	private static final int[] primes = {
	2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31,
	37, 41, 43, 47, 53, 59, 61, 67, 71, 73,
	79, 83, 89, 97, 101, 103, 107, 109, 113, 127,
	131, 137, 139, 149, 151, 157, 163, 167, 173, 179,
	181, 191, 193, 197, 199, 211, 223, 227, 229, 233,
	239, 241, 251, 257, 263, 269, 271, 277, 281, 283,
	293, 307, 311, 313, 317, 331, 337, 347, 349, 353,
	359, 367, 373, 379, 383, 389, 397, 401, 409, 419,
	421, 431, 433, 439, 443, 449, 457, 461, 463, 467,
	479, 487, 491, 499, 503, 509, 521, 523, 541, 547,
	557, 563, 569, 571, 577, 587, 593, 599, 601, 607,
	613, 617, 619, 631, 641, 643, 647, 653, 659, 661,
	673, 677, 683, 691, 701, 709, 719, 727, 733, 739,
	743, 751, 757, 761, 769, 773, 787, 797, 809, 811,
	821, 823, 827, 829, 839, 853, 857, 859, 863, 877,
	881, 883, 887, 907, 911, 919, 929, 937, 941, 947,
	953, 967, 971, 977, 983, 991, 997, 1009, 1013, 1019,
	1021, 1031, 1033, 1039, 1049, 1051, 1061, 1063, 1069, 1087,
	1091, 1093, 1097, 1103, 1109, 1117, 1123, 1129, 1151, 1153,
	1163, 1171, 1181, 1187, 1193, 1201, 1213, 1217, 1223, 1229,
	1231, 1237, 1249, 1259, 1277, 1279, 1283, 1289, 1291, 1297,
	1301, 1303, 1307, 1319, 1321, 1327, 1361, 1367, 1373, 1381,
	1399, 1409, 1423, 1427, 1429, 1433, 1439, 1447, 1451, 1453,
	1459, 1471, 1481, 1483, 1487, 1489, 1493, 1499, 1511, 1523,
	1531, 1543, 1549, 1553, 1559, 1567, 1571, 1579, 1583, 1597,
	1601, 1607, 1609, 1613, 1619, 1621, 1627, 1637, 1657, 1663,
	1667, 1669, 1693, 1697, 1699, 1709, 1721, 1723, 1733, 1741,
	1747, 1753, 1759, 1777, 1783, 1787, 1789, 1801, 1811, 1823,
	1831, 1847, 1861, 1867, 1871, 1873, 1877, 1879, 1889, 1901,
	1907, 1913, 1931, 1933, 1949, 1951, 1973, 1979, 1987, 1993,
	1997, 1999, 2003, 2011, 2017, 2027, 2029, 2039, 2053, 2063,
	2069, 2081, 2083, 2087, 2089, 2099, 2111, 2113, 2129, 2131,
	2137, 2141, 2143, 2153, 2161, 2179, 2203, 2207, 2213, 2221,
	2237, 2239, 2243, 2251, 2267, 2269, 2273, 2281, 2287, 2293,
	2297, 2309, 2311, 2333, 2339, 2341, 2347, 2351, 2357, 2371,
	2377, 2381, 2383, 2389, 2393, 2399, 2411, 2417, 2423, 2437,
	2441, 2447, 2459, 2467, 2473, 2477, 2503, 2521, 2531, 2539,
	2543, 2549, 2551, 2557, 2579, 2591, 2593, 2609, 2617, 2621,
	2633, 2647, 2657, 2659, 2663, 2671, 2677, 2683, 2687, 2689,
	2693, 2699, 2707, 2711, 2713, 2719, 2729, 2731, 2741, 2749,
	2753, 2767, 2777, 2789, 2791, 2797, 2801, 2803, 2819, 2833,
	2837, 2843, 2851, 2857, 2861, 2879, 2887, 2897, 2903, 2909,
	2917, 2927, 2939, 2953, 2957, 2963, 2969, 2971, 2999, 3001,
	3011, 3019, 3023, 3037, 3041, 3049, 3061, 3067, 3079, 3083,
	3089, 3109, 3119, 3121, 3137, 3163, 3167, 3169, 3181, 3187,
	3191, 3203, 3209, 3217, 3221, 3229, 3251, 3253, 3257, 3259,
	3271, 3299, 3301, 3307, 3313, 3319, 3323, 3329, 3331, 3343,
	3347, 3359, 3361, 3371, 3373, 3389, 3391, 3407, 3413, 3433,
	3449, 3457, 3461, 3463, 3467, 3469, 3491, 3499, 3511, 3517,
	3527, 3529, 3533, 3539, 3541, 3547, 3557, 3559, 3571, 3581,
	3583, 3593, 3607, 3613, 3617, 3623, 3631, 3637, 3643, 3659,
	3671, 3673, 3677, 3691, 3697, 3701, 3709, 3719, 3727, 3733,
	3739, 3761, 3767, 3769, 3779, 3793, 3797, 3803, 3821, 3823,
	3833, 3847, 3851, 3853, 3863, 3877, 3881, 3889, 3907, 3911,
	3917, 3919, 3923, 3929, 3931, 3943, 3947, 3967, 3989, 4001,
	4003, 4007, 4013, 4019, 4021, 4027, 4049, 4051, 4057, 4073,
	4079, 4091, 4093, 4099, 4111, 4127, 4129, 4133, 4139, 4153,
	4157, 4159, 4177, 4201, 4211, 4217, 4219, 4229, 4231, 4241,
	4243, 4253, 4259, 4261, 4271, 4273, 4283, 4289, 4297, 4327,
	4337, 4339, 4349, 4357, 4363, 4373, 4391, 4397, 4409, 4421,
	4423, 4441, 4447, 4451, 4457, 4463, 4481, 4483, 4493, 4507,
	4513, 4517, 4519, 4523, 4547, 4549, 4561, 4567, 4583, 4591,
	4597, 4603, 4621, 4637, 4639, 4643, 4649, 4651, 4657, 4663,
	4673, 4679, 4691, 4703, 4721, 4723, 4729, 4733, 4751, 4759,
	4783, 4787, 4789, 4793, 4799, 4801, 4813, 4817, 4831, 4861,
	4871, 4877, 4889, 4903, 4909, 4919, 4931, 4933, 4937, 4943,
	4951, 4957, 4967, 4969, 4973, 4987, 4993, 4999, 5003, 5009,
	5011, 5021, 5023, 5039, 5051, 5059, 5077, 5081, 5087, 5099,
	5101, 5107, 5113, 5119, 5147, 5153, 5167, 5171, 5179, 5189,
	5197, 5209, 5227, 5231, 5233, 5237, 5261, 5273, 5279, 5281,
	5297, 5303, 5309, 5323, 5333, 5347, 5351, 5381, 5387, 5393,
	5399, 5407, 5413, 5417, 5419, 5431, 5437, 5441, 5443, 5449,
	5471, 5477, 5479, 5483, 5501, 5503, 5507, 5519, 5521, 5527,
	5531, 5557, 5563, 5569, 5573, 5581, 5591, 5623, 5639, 5641,
	5647, 5651, 5653, 5657, 5659, 5669, 5683, 5689, 5693, 5701,
	5711, 5717, 5737, 5741, 5743, 5749, 5779, 5783, 5791, 5801,
	5807, 5813, 5821, 5827, 5839, 5843, 5849, 5851, 5857, 5861,
	5867, 5869, 5879, 5881, 5897, 5903, 5923, 5927, 5939, 5953,
	5981, 5987, 6007, 6011, 6029, 6037, 6043, 6047, 6053, 6067,
	6073, 6079, 6089, 6091, 6101, 6113, 6121, 6131, 6133, 6143,
	};

	/**
	 * Compute (y-x) mod n.
	 * @param x
	 * @param y
	 * @param n
	 * @return (y-x) mod n
	 */
	long submod(long x, long y, long n) {
		long r0;
		if (Long.compareUnsigned(x, y) > 0) {
			r0 = (x-y)%n;
		} else {
			r0 = n + (x-y)%n;
		}
		//LOG.debug("submod: (" + x + " - " + y + ") mod " + n + " = " + r0);
		return r0;
	}

	/**
	 * Compute x+y mod n.
	 * @param x
	 * @param y
	 * @param n
	 * @return x+y mod n
	 */
	long addmod(long x, long y, long n)
	{
		return (x+y)%n;
	}

	/**
	 * Computes c*2^64 mod n.
	 * @param c
	 * @param n
	 * @return c*2^64 mod n
	 */
	long u64div(long c, long n)
	{
//		__asm__("divq %4"
//			: "=a"(c), "=d"(n) // outputs: c = quotient, n=remainder
//			: "1"(c), "0"(0), "r"(n)); // inputs: [0, c], n
//
//		return n;
		return spDivide(new long[] {0, c}, n);
	}

	/**
	 * Compute the remainder u mod v.
	 * @param u 128 bit unsigned integer
	 * @param v 64 bit unsigned integer
	 * @return u mod v
	 */
	long spDivide(long[/*2*/] u, long v)
	// _s_imple _p_recision divide (and remainder) -- all that remains here is the remainder
	{
//		*r = u[1];
//		*q = u[0];
//		__asm__("divq %4"
//			: "=a"(*q), "=d"(*r) // outputs: a = quotient, r=remainder
//			: "1"(*r), "0"(*q), "r"(v)); // inputs: [q, r], v

		long[] divRem = div(u[0], u[1], v);
		return divRem[1];
	}

	/**
	 * Unsigned 128 by 64 bit division and remainder, adapted from
	 * https://codereview.stackexchange.com/questions/67962/mostly-portable-128-by-64-bit-division.
	 * @param a_lo
	 * @param a_hi
	 * @param b
	 * @return [quotient, remainder]
	 */
	// XXX The quotient can not be correct if it has more than 64 bit
	long[] div(long a_lo, long a_hi, long b)
	{
	    long p_lo;
	    long p_hi;
	    long q = 0;
	    long r;
	    
	    long r_hi = a_hi;
	    long r_lo = a_lo;

	    int s = 0;
	    if(0 == (b >>> 63)){

	        // Normalize so quotient estimates are
	        // no more than 2 in error.

	        // Note: If any bits get shifted out of
	        // r_hi at this point, the result would
	        // overflow.

	    	long bsr = bsr(b);
	        if (DEBUG) LOG.debug("bsr=" + bsr + ", bitLength(b)=" + (64-Long.numberOfLeadingZeros(b)));
	        s = 63 - bsr(b);
	        int t = 64 - s;

	        b <<= s;
	        r_hi = (r_hi << s)|(r_lo >>> t);
	        r_lo <<= s;
	    }
	    if (DEBUG) LOG.debug("s=" + s + ", b=" + b + ", r_lo=" + r_lo + ", r_hi=" + r_hi);
	    
	    long b_hi = b >>> 32;

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
	    
	    long[] mulResult = mul(b, q_hat); // p_lo = mul(b, q_hat, p_hi);
	    p_lo = mulResult[0];
	    p_hi = mulResult[1];
	    if (DEBUG) LOG.debug("p_lo=" + Long.toUnsignedString(p_lo) + ", p_hi=" + Long.toUnsignedString(p_hi));
	    
	    long u_hi = r_hi >>> 32;
	    long u_lo = (r_hi << 32)|(r_lo >>> 32);

	    // r -= b*q_hat
	    //
	    // At most 2 iterations of this...
	    // In Java we must be careful in the comparisons of longs with the sign bit set!
	    //while( (p_hi > u_hi) || ((p_hi == u_hi) && (p_lo > u_lo)) )
	    while( (Long.compareUnsigned(p_hi, u_hi) > 0) || ((p_hi == u_hi) && (Long.compareUnsigned(p_lo, u_lo) > 0)) )
	    {
	        //if(p_lo < b){
	        if (Long.compareUnsigned(p_lo, b) < 0) {
	            --p_hi;
	        }
	        p_lo -= b;
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
	    if (DEBUG) LOG.debug("b=" + Long.toUnsignedString(b) + ", q_hat=" + Long.toUnsignedString(q_hat));
	    
	    mulResult = mul(b, q_hat); // p_lo = mul(b, q_hat, p_hi);
	    p_lo = mulResult[0];
	    p_hi = mulResult[1];
	    if (DEBUG) LOG.debug("2: p_lo=" + Long.toUnsignedString(p_lo) + ", p_hi=" + Long.toUnsignedString(p_hi));

	    // r -= b*q_hat
	    //
	    // ...and at most 2 iterations of this.
	    // In Java we must be careful in the comparisons of longs with the sign bit set!
	    //while( (p_hi > r_hi) || ((p_hi == r_hi) && (p_lo > r_lo)) )
		while( (Long.compareUnsigned(p_hi, r_hi) > 0) || ((p_hi == r_hi) && (Long.compareUnsigned(p_lo, r_lo) > 0)) )
	    {
	        //if(p_lo < b){
	        if(Long.compareUnsigned(p_lo, b) < 0){
	            --p_hi;
	        }
	        p_lo -= b;
	        --q_hat;
	    }

	    r_lo -= p_lo;

	    q |= q_hat;

	    r = r_lo >>> s;

	    return new long[] {q, r};
	}
	
	int bsr(long x)
	{
	    long y;
	    long r;

//	    r = (x > 0xFFFFFFFF) << 5; x >>= r;
//	    y = (x > 0xFFFF    ) << 4; x >>= y; r |= y;
//	    y = (x > 0xFF      ) << 3; x >>= y; r |= y;
//	    y = (x > 0xF       ) << 2; x >>= y; r |= y;
//	    y = (x > 0x3       ) << 1; x >>= y; r |= y;
	    
	    //r = (x > 0xFFFFFFFF) << 5; 
	    r = (x > 0xFFFFFFFFL) ? 32 : 0;
	    x >>>= r;
	    //y = (x > 0xFFFF    ) << 4; 
	    y = (x > 0xFFFFL) ? 16 : 0;
	    x >>>= y; r |= y;
	    //y = (x > 0xFF      ) << 3; 
	    y = (x > 0xFFL) ? 8 : 0;
	    x >>>= y; r |= y;
	    //y = (x > 0xF       ) << 2;
	    y = (x > 0xFL) ? 4 : 0;
	    x >>>= y; r |= y;
	    //y = (x > 0x3       ) << 1;
	    y = (x > 0x3L) ? 2 : 0;
	    x >>>= y; r |= y;

	    return (int) (r | (x >>> 1));
	}

	// TODO check old version again
	long[] mul(long a, long b)
	{
		long a_lo = a & 0x00000000FFFFFFFF;
		long a_hi = a >>> 32;

		long b_lo = b & 0x00000000FFFFFFFF;
		long b_hi = b >>> 32;

		long c0 = a_lo * b_lo;
		long c1 = a_hi * b_lo;
		long c2 = a_hi * b_hi;

		long u1 = c1 + (a_lo * b_hi);
	    if(u1 < c1){
	        c2 += 1L << 32;
	    }

	    long u0 = c0 + (u1 << 32);
	    if(u0 < c0){
	        ++c2;
	    }

	    long y = c2 + (u1 >>> 32);

	    // test
	    Uint128 prod = Uint128.mul64(a, b);
	    //LOG.debug("correct: " + a + "*" + b + " = " + prod.getLow() + ", " + prod.getHigh());
	    //LOG.debug("new    : " + a + "*" + b + " = " + u0 + ", " + y);
	    //return new long[] {u0, y};
	    return new long[] {prod.getLow(), prod.getHigh()};
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
		long w;

		Uint128 prod128 = Uint128.mul64(u, v);
		long[] p = new long[] {prod128.getLow(), prod128.getHigh()};
		w = spDivide(p, m); // w = p mod m

		return w;
	}

	long spGCD(long x, long y)
	{
		long a, b, c;
		a = x; b = y;
		while (b != 0)
		{
			c = a % b;
			a = b;
			b = c;
		}
		return a;
	}

	long LCGSTATE;
	int spRand(int lower, int upper)
	{
		// fix rng for negative upper values;
		long upperl = (long) upper;
		if (upperl<0) upperl += (1L<<32);
		if (DEBUG) LOG.debug("lower=" + lower + ", upper=" + upperl);
		
		// advance the state of the LCG and return the appropriate result
		LCGSTATE = 6364136223846793005L * LCGSTATE + 1442695040888963407L;
		if (DEBUG) LOG.debug("LCGSTATE = " + LCGSTATE);
		long diff = upperl - lower;
		int rand = (int)(diff * (double)(LCGSTATE >> 32) / 4294967296.0); // dividend is 2^32
		int result = lower + rand;
		if (DEBUG) LOG.debug("diff=" + diff + ", rand=" + rand + ", result=" + result);
		return result;
	}

	ecm_pt add(long rho, ecm_work work, ecm_pt P1, ecm_pt P2,
		ecm_pt Pin, ecm_pt lastPout)
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

		work.tt1 = mulredcx(work.diff1, work.sum2, work.n, rho);	//U
		work.tt2 = mulredcx(work.sum1, work.diff2, work.n, rho);	//V

		work.tt3 = addmod(work.tt1, work.tt2, work.n);
		work.tt4 = submod(work.tt1, work.tt2, work.n);
		work.tt1 = sqrredcx(work.tt3, work.n, rho);	//(U + V)^2
		work.tt2 = sqrredcx(work.tt4, work.n, rho);	//(U - V)^2

		ecm_pt Pout = new ecm_pt();
		if (Pin == lastPout)
		{
			long tmp;
			Pout.Z = mulredcx(work.tt1, Pin.Z, work.n, rho);		//Z * (U + V)^2
			Pout.X = mulredcx(work.tt2, Pin.X, work.n, rho);		//x * (U - V)^2
			tmp = Pout.Z;
			Pout.Z = Pout.X;
			Pout.X = tmp;
		}
		else
		{
			Pout.X = mulredcx(work.tt1, Pin.Z, work.n, rho);		//Z * (U + V)^2
			Pout.Z = mulredcx(work.tt2, Pin.X, work.n, rho);		//x * (U - V)^2
		}
		return Pout;
	}

	ecm_pt dup(long rho, ecm_work work,
		long insum, long indiff)
	{
		ecm_pt P = new ecm_pt();
		work.tt1 = sqrredcx(indiff, work.n, rho);			// U=(x1 - z1)^2
		work.tt2 = sqrredcx(insum, work.n, rho);			// V=(x1 + z1)^2
		P.X = mulredcx(work.tt1, work.tt2, work.n, rho);			// x=U*V

		work.tt3 = submod(work.tt2, work.tt1, work.n);			// w = V-U
		work.tt2 = mulredcx(work.tt3, work.s, work.n, rho);		// w = (A+2)/4 * w
		work.tt2 = addmod(work.tt2, work.tt1, work.n);			// w = w + U
		P.Z = mulredcx(work.tt2, work.tt3, work.n, rho);			// Z = w*(V-U)
		return P;
	}

	ecm_pt prac70(long rho, ecm_work work, ecm_pt P)
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
				work.pt1 = dup(rho, work, s1, d1);
			}
			else if (prac70Steps[i] == 3)
			{
				// integrate step 4 followed by swap(1,2)
				work.pt4 = add(rho, work, work.pt2, work.pt1, work.pt3, work.pt4);		// T = B + A (C)

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
				work.pt4 = add(rho, work, work.pt2, work.pt1, work.pt3, work.pt4);		// T = B + A (C)

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

				work.pt2 = add(rho, work, work.pt2, work.pt1, work.pt3, work.pt2);		// B = B + A (C)
				work.pt1 = dup(rho, work, s2, d2);		// A = 2A
			}
			else if (prac70Steps[i] == 6)
			{
				P = add(rho, work, work.pt1, work.pt2, work.pt3, P);		// A = A + B (C)
				// TODO assign changes to P.X, P.Z?
			}
		}

		return P;
	}

	// TODO return updated P ?
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
				work.pt1 = dup(rho, work, s1, d1);
			}
			else if (prac85Steps[i] == 3)
			{
				// integrate step 4 followed by swap(1,2)
				work.pt4 = add(rho, work, work.pt2, work.pt1, work.pt3, work.pt4);		// T = B + A (C)

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
				work.pt4 = add(rho, work, work.pt2, work.pt1, work.pt3, work.pt4);		// T = B + A (C)

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

				work.pt2 = add(rho, work, work.pt2, work.pt1, work.pt3, work.pt2);		// B = B + A (C)
				work.pt1 = dup(rho, work, s2, d2);		// A = 2A
			}
			else if (prac85Steps[i] == 6)
			{
				P = add(rho, work, work.pt1, work.pt2, work.pt3, P);		// A = A + B (C)
			}

		}

		return;

	}

	// TODO return updated P ?
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
		work.pt1 = dup(rho, work, s1, d1);

		while (d != e)
		{
			if (Long.compareUnsigned(d, e) < 0)
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

			if (Long.compareUnsigned((d + 3) / 4, e) <= 0)
			{
				d -= e;

				work.pt4 = add(rho, work, work.pt2, work.pt1, work.pt3, work.pt4);		// T = B + A (C)

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
				//d = (d - e) / 2;
				d = (d - e) >>> 1;

				d2 = submod(work.pt1.X, work.pt1.Z, work.n);
				s2 = addmod(work.pt1.X, work.pt1.Z, work.n);

				work.pt2 = add(rho, work, work.pt2, work.pt1, work.pt3, work.pt2);		// B = B + A (C)
				work.pt1 = dup(rho, work, s2, d2);		// A = 2A
			}
			else
			{
				// empirically, tiny B1 values only need the above prac cases.
				// just in case, fall back on this.
				LOG.error("unhandled case in prac");
				System.exit(1);
			}
		}

		P = add(rho, work, work.pt1, work.pt2, work.pt3, P);		// A = A + B (C)

		return;
	}

	// TODO Compare with my implementations
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

		if (sigma == 0)
		{
			work.sigma = spRand(7, (int)-1);
			//LOG.debug("random sigma=" + work.sigma);
		}
		else
		{
			work.sigma = sigma;
			//LOG.debug("use existing sigma=" + work.sigma);
		}
		sigma = work.sigma;

		u = sigma;
		u = u64div(u, n);
		//LOG.debug("u=" + u);
		
		t1 = 4;
		t1 = u64div(t1, n);
		//LOG.debug("t1=" + t1);

		v = mulredcx(u, t1, n, rho);		// v = 4*sigma
		//LOG.debug("v=" + v);

		u = mulredcx(u, u, n, rho);
		//LOG.debug("u=" + u);
		
		t1 = 5;
		t1 = u64div(t1, n);
		//LOG.debug("t1=" + t1);
		
		u = submod(u, t1, n);			// u = sigma^2 - 5
		//LOG.debug("u=" + u);

		t1 = mulredcx(u, u, n, rho);
		//LOG.debug("t1=" + t1);
		P.X = mulredcx(t1, u, n, rho);	// x = u^3
		if (DEBUG) LOG.debug("P.X=" + P.X);

		t1 = mulredcx(v, v, n, rho);
		P.Z = mulredcx(t1, v, n, rho);	// z = v^3
		if (DEBUG) LOG.debug("P.Z=" + P.Z);

		//compute parameter A
		t1 = submod(v, u, n);			// (v - u)
		t2 = mulredcx(t1, t1, n, rho);
		t4 = mulredcx(t2, t1, n, rho);	// (v - u)^3

		t1 = 3;
		t1 = u64div(t1, n);
		t2 = mulredcx(t1, u, n, rho);	// 3u
		t3 = addmod(t2, v, n);			// 3u + v

		t1 = mulredcx(t3, t4, n, rho);	// a = (v-u)^3 * (3u + v)

		t2 = 16;
		t2 = u64div(t2, n);
		t3 = mulredcx(P.X, t2, n, rho);	// 16*u^3
		t4 = mulredcx(t3, v, n, rho);	// 16*u^3*v

		// u holds the denom, t1 holds the numer
		// accomplish the division by multiplying by the modular inverse
		t2 = 1;
		t4 = mulredcx(t4, t2, n, rho);	// take t4 out of monty rep
		t1 = mulredcx(t1, t2, n, rho);	// take t1 out of monty rep

		t3 = modinv_64(t4, n);
		if (DEBUG) LOG.debug("t4=" + t4 + ", modinv t3 =" + t3);
		
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
		rho = (long)0 - x; // XXX some kind of complement of n mod 2^64 ?
		if (DEBUG) LOG.debug("rho = " + rho); // this is correct, the unsigned 64 bit value - 2^64
		
		work.n = n;

		work.stg1_max = B1;
		// pre-paired sequences have been prepared for this B2, so it is not an input
		work.stg2_max = 25 * B1;	

//		*f = 1;
		for (curve = 0; curve < curves; curve++)
		{
			if (DEBUG) LOG.debug("curve=" + curve);
			work.last_pid = 0;
			sigma = 0;
			if (DEBUG) LOG.debug("1: P.X=" + P.X + ", P.Z=" + P.Z);
			build(P, rho, work, sigma);
			if (DEBUG) LOG.debug("curve=" + curve + ": build finished");
			if (DEBUG) LOG.debug("2: P.X=" + P.X + ", P.Z=" + P.Z);
			
			P = ecm_stage1(rho, work, P);
			if (DEBUG) LOG.debug("curve=" + curve + ": stage1 finished");
			if (DEBUG) LOG.debug("3: P.X=" + P.X + ", P.Z=" + P.Z);
			result = check_factor(P.Z, n); // OK!

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

		LOG.warn("Failed to find a factor of N=" + n);
		return new EcmResult(1, curve);
	}

	ecm_pt ecm_stage1(long rho, ecm_work work, ecm_pt P)
	{
		int i=10; // guess for small n
		long q;
		long stg1 = (long)work.stg1_max;

		// handle the only even case 
		if (DEBUG) LOG.debug("P.X=" + P.X + ", P.Z=" + P.Z);
		q = 2;
		while (Long.compareUnsigned(q, stg1) < 0)
		{
			work.diff1 = submod(P.X, P.Z, work.n);
			work.sum1 = addmod(P.X, P.Z, work.n);
			P = dup(rho, work, work.sum1, work.diff1);
			if (DEBUG) LOG.debug("q=" + q + ": P.X=" + P.X + ", P.Z=" + P.Z);
			q *= 2;
		}
		if (DEBUG) LOG.debug("stg1=" + stg1 + ", q=" + q + ", diff1=" + work.diff1 + ", sum1= " + work.sum1);
		if (DEBUG) LOG.debug("P.X=" + P.X + ", P.Z=" + P.Z);
		
		if (stg1 == 70)
		{
			P = prac70(rho, work, P);
			i = 19;
		}
		else if (Long.compareUnsigned(stg1, 85) >= 0)
		{
			// call prac with best ratios found by a deep search.
			// some composites are cheaper than their 
			// constituent primes.
			prac85(rho, work, P);
			if (Long.compareUnsigned(stg1, 100) < 0)
			{
				// paired into a composite for larger bounds
				prac(rho, work, P, 61, 0.522786351415446049);
			}
			i = 23;

			if (Long.compareUnsigned(stg1, 125) >= 0)
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

				if (Long.compareUnsigned(stg1, 130) < 0)
				{
					prac(rho, work, P, 103, 0.632839806088706269);
					
				}
				
				i = 30;
			}
			
			if (Long.compareUnsigned(stg1, 165) >= 0)
			{
				prac(rho, work, P, 7747, 0.552188778811121); // 61 x 127
				prac(rho, work, P, 131, 0.618033988749894903);
				prac(rho, work, P, 14111, 0.632839806088706);	// 103 x 137
				prac(rho, work, P, 20989, 0.620181980807415);	// 139 x 151
				prac(rho, work, P, 157, 0.640157392785047019);
				prac(rho, work, P, 163, 0.551390822543526449);

				if (Long.compareUnsigned(stg1, 200) < 0)
				{
					prac(rho, work, P, 149, 0.580178728295464130);
				}
				i = 38;
			}
			
			if (Long.compareUnsigned(stg1, 205) >= 0)
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
				i = 46;
			}
		}

		work.last_pid = i;
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
		byte[] barray = null; // I needed some initialization
		int numb = 0; // I needed some initialization

		//stage 2 init
		//Q = P = result of stage 1
		//compute [d]Q for 0 < d <= D
		Pd = Pb[map[60]];

		// [1]Q
		Pb[1].Z = P.Z;
		Pb[1].X = P.X;
		work.Pbprod[1] = mulredcx(Pb[1].X, Pb[1].Z, work.n, rho);

		// [2]Q
		Pb[2].Z = P.Z;
		Pb[2].X = P.X;
		work.diff1 = submod(P.X, P.Z, work.n);
		work.sum1 = addmod(P.X, P.Z, work.n);
		Pb[2] = dup(rho, work, work.sum1, work.diff1);
		work.Pbprod[2] = mulredcx(Pb[2].X, Pb[2].Z, work.n, rho);

		// Calculate all Pb: the following is specialized for D=60
		// [2]Q + [1]Q([1]Q) = [3]Q
		Pb[3] = add(rho, work, Pb[1], Pb[2], Pb[1], Pb[3]);		// <-- temporary

		// 2*[3]Q = [6]Q
		work.diff1 = submod(Pb[3].X, Pb[3].Z, work.n);
		work.sum1 = addmod(Pb[3].X, Pb[3].Z, work.n);
		work.pt3 = dup(rho, work, work.sum1, work.diff1);	// pt3 = [6]Q

		// [3]Q + [2]Q([1]Q) = [5]Q
		work.pt1 = add(rho, work, Pb[3], Pb[2], Pb[1], work.pt1);	// <-- pt1 = [5]Q
		Pb[3].X = work.pt1.X;
		Pb[3].Z = work.pt1.Z;

		// [6]Q + [5]Q([1]Q) = [11]Q
		Pb[4] = add(rho, work, work.pt3, work.pt1, Pb[1], Pb[4]);	// <-- [11]Q

		i = 3;
		k = 4;
		j = 5;
		while ((j + 12) < (60))
		{
			// [j+6]Q + [6]Q([j]Q) = [j+12]Q
			Pb[map[j + 12]] = add(rho, work, work.pt3, Pb[k], Pb[i], Pb[map[j + 12]]);
			i = k;
			k = map[j + 12];
			j += 6;
		}

		// [6]Q + [1]Q([5]Q) = [7]Q
		Pb[3] = add(rho, work, work.pt3, Pb[1], work.pt1, Pb[3]);	// <-- [7]Q
		i = 1;
		k = 3;
		j = 1;
		while ((j + 12) < (60))
		{
			// [j+6]Q + [6]Q([j]Q) = [j+12]Q
			Pb[map[j + 12]] = add(rho, work, work.pt3, Pb[k], Pb[i], Pb[map[j + 12]]);
			i = k;
			k = map[j + 12];
			j += 6;
		}

		// Pd = [2w]Q
		// [31]Q + [29]Q([2]Q) = [60]Q
		Pd = add(rho, work, Pb[9], Pb[10], Pb[2], Pd);	// <-- [60]Q

		// make all of the Pbprod's
		for (i = 3; i < 19; i++)
		{
			work.Pbprod[i] = mulredcx(Pb[i].X, Pb[i].Z, work.n, rho);
		}

		//initialize info needed for giant step
		// temporary - make [4]Q
		work.diff1 = submod(Pb[2].X, Pb[2].Z, work.n);
		work.sum1 = addmod(Pb[2].X, Pb[2].Z, work.n);
		work.pt3 = dup(rho, work, work.sum1, work.diff1);	// pt3 = [4]Q

		// Pd = [w]Q
		// [17]Q + [13]Q([4]Q) = [30]Q
		work.Pad = add(rho, work, Pb[map[17]], Pb[map[13]], work.pt3, work.Pad);	// <-- [30]Q

		// [60]Q + [30]Q([30]Q) = [90]Q
		Pa = add(rho, work, Pd, work.Pad, work.Pad, Pa);
		work.pt1.X = Pa.X;
		work.pt1.Z = Pa.Z;
		
		// [90]Q + [30]Q([60]Q) = [120]Q
		Pa = add(rho, work, Pa, work.Pad, Pd, Pa);
		Pd.X = Pa.X;
		Pd.Z = Pa.Z;

		// [120]Q + [30]Q([90]Q) = [150]Q
		Pa = add(rho, work, Pa, work.Pad, work.pt1, Pa);

		// adjustment of Pa and Pad for larger B1.
		// Currently we have Pa=150, Pd=120, Pad=30
		if (work.stg1_max == 165)
		{
			// need Pa = 180, Pad = 60
			// [150]Q + [30]Q([120]Q) = [180]Q
			Pa = add(rho, work, Pa, work.Pad, Pd, Pa);

			work.diff1 = submod(work.Pad.X, work.Pad.Z, work.n);
			work.sum1 = addmod(work.Pad.X, work.Pad.Z, work.n);
			work.Pad = dup(rho, work, work.sum1, work.diff1);	// Pad = [60]Q
		}
		else if (work.stg1_max == 205)
		{
			// need Pa = 210, Pad = 90.
			// have pt1 = 90

			work.diff1 = submod(work.Pad.X, work.Pad.Z, work.n);
			work.sum1 = addmod(work.Pad.X, work.Pad.Z, work.n);
			work.Pad = dup(rho, work, work.sum1, work.diff1);	// Pad = [60]Q

			// [150]Q + [60]Q([90]Q) = [210]Q
			Pa = add(rho, work, Pa, work.Pad, work.pt1, Pa);
			work.Pad.X = work.pt1.X;
			work.Pad.Z = work.pt1.Z;
		}

		//initialize accumulator and Paprod
		acc = u64div(1, work.n);
		work.Paprod = mulredcx(Pa.X, Pa.Z, work.n, rho);

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

		for (i = 0; i < numb; i++)
		{
			if (barray[i] == 0)
			{
				//giant step - use the addition formula for ECM
				work.pt1.X = Pa.X;
				work.pt1.Z = Pa.Z;

				//Pa + Pd
				Pa = add(rho, work, Pa, Pd, work.Pad, Pa);

				//Pad holds the previous Pa
				work.Pad.X = work.pt1.X;
				work.Pad.Z = work.pt1.Z;

				//and Paprod
				work.Paprod = mulredcx(Pa.X, Pa.Z, work.n, rho);

				i++;
			}

			//we accumulate XrZd - XdZr = (Xr - Xd) * (Zr + Zd) + XdZd - XrZr
			//in CP notation, Pa -> (Xr,Zr), Pb -> (Xd,Zd)

			b = barray[i];
			// accumulate the cross product  (zimmerman syntax).
			// page 342 in C&P
			work.tt1 = submod(Pa.X, Pb[map[b]].X, work.n);
			work.tt2 = addmod(Pa.Z, Pb[map[b]].Z, work.n);
			work.tt3 = mulredcx(work.tt1, work.tt2, work.n, rho);
			work.tt1 = addmod(work.tt3, work.Pbprod[map[b]], work.n);
			work.tt2 = submod(work.tt1, work.Paprod, work.n);
			acc = mulredcx(acc, work.tt2, work.n, rho);

		}

		work.stg2acc = acc;

		return;
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
//		LCGSTATE = 65537 * rng.nextInt();
		LCGSTATE = 4295098403L;
		if (DEBUG) LOG.debug("LCGSTATE = " + LCGSTATE);
		
		int NBits = N.bitLength();
		if (NBits > 63) throw new IllegalArgumentException("N=" + N + " has " + NBits + " bit, but tinyEcm supports arguments up to 63 bit only.");
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
		} else if (NBits < 64) {
			B1 = 205;
			curves = 40;
		} else { // >= 64 bit can not happen here but we keep the data for the moment
			B1 = 1000;
			curves = 64;
		}
		if (DEBUG) LOG.debug("B1=" + B1 + ", curves=" + curves);
		
		LOG.debug("Try to factor N=" + N);
		EcmResult result = tinyecm(N.longValue(), B1, curves);
		return BigInteger.valueOf(result.f);
	}
	
	public static void main(String[] args) {
		ConfigUtil.initProject();
		
		TinyEcm factorizer = new TinyEcm();
		int numTestNumbers = 1;
		long[] testNumbers = new long[] { 1234577*12345701L };
		for (int i=0; i<numTestNumbers; i++) {
			long N = testNumbers[i];
			BigInteger factor = factorizer.findSingleFactor(BigInteger.valueOf(N));
			LOG.info("Found factor " + factor + " of N=" + N);
		}
	}
}
