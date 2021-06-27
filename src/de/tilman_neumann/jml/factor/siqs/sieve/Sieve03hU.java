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
package de.tilman_neumann.jml.factor.siqs.sieve;

import static de.tilman_neumann.jml.base.BigIntConstants.*;
import static de.tilman_neumann.jml.factor.base.GlobalFactoringOptions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.BinarySearch;
import de.tilman_neumann.jml.base.UnsignedBigInt;
import de.tilman_neumann.jml.factor.base.SortedIntegerArray;
import de.tilman_neumann.jml.factor.base.UnsafeUtil;
import de.tilman_neumann.jml.factor.siqs.data.SolutionArrays;
import de.tilman_neumann.util.Timer;
import sun.misc.Unsafe;

/**
 * A refinement of Sieve03gU selecting the sieve hits passed to trial division in a better way.
 * Needs to be used together with TDiv_QS_2Large_UBI2.
 * 
 * @author Tilman Neumann
 */
public class Sieve03hU implements Sieve {
	private static final Logger LOG = Logger.getLogger(Sieve03hU.class);
	private static final boolean DEBUG = false;
	private static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();

	private static final long LONG_MASK =   0x8080808080808080L;
	private static final long UPPER_MASK =  0x8080808000000000L;
	private static final long LOWER_MASK =          0x80808080L;

	private static final double LN2 = Math.log(2.0);
	
	private BigInteger daParam, bParam, cParam, kN;
	private int d;

	/** multiplier to convert dual logarithms (e.g. bit length) to the scaled log that yields a sieve hit if the sieve array entry x >= 128 */
	private double ld2logPMultiplier;
	
	private int tdivTestMinLogPSum;
	private int logQdivDaEstimate;
	
	// prime base
	private int primeBaseSize;
	/** we do not sieve with primes p_i, i<pMinIndex */
	private int pMinIndex;
	/** p_i with i>p1Index have at most 1 solution in the sieve array for each of x1, x2 */
	private int p1Index;
	private int p2Index;
	private int p3Index;
	private int[] minSolutionCounts_m3;
	private double[] smallPrimesLogPArray;
	
	private SolutionArrays solutionArrays;

	// sieve
	private int sieveArraySize;
	/** the value to initializate the sieve array with */
	private byte initializer;
	/** base address of the sieve array holding logP sums for all x */
	private long sieveArrayAddress;

	private List<SmoothCandidate> smoothCandidates = new ArrayList<>();

	/** buffers for trial division engine. */
	private UnsignedBigInt Q_rest_UBI = new UnsignedBigInt(new int[50]);
	private UnsignedBigInt quotient_UBI = new UnsignedBigInt(new int[50]);

	/** the indices of the primes found to divide Q in pass 1 */
	private int[] pass2PrimeIndices = new int[100];
	private int[] pass2Primes = new int[100];
	private int[] pass2Powers = new int[100];
	private int[] pass2Exponents = new int[100];

	private BinarySearch binarySearch = new BinarySearch();

	// timings
	private Timer timer = new Timer();
	private long initDuration, sieveDuration, collectDuration;
	
	@Override
	public String getName() {
		return "sieve03hU";
	}
	
	@Override
	public void initializeForN(SieveParams sieveParams, int[] primesArray, int mergedBaseSize) {
		this.kN = sieveParams.kN;
		this.pMinIndex = sieveParams.pMinIndex;
		this.ld2logPMultiplier = sieveParams.lnPMultiplier * LN2;
		this.tdivTestMinLogPSum = sieveParams.tdivTestMinLogPSum;
		this.logQdivDaEstimate = sieveParams.logQdivDaEstimate;
		this.initializer = sieveParams.initializer;
		
		this.smallPrimesLogPArray = new double[pMinIndex];
		for (int i=pMinIndex-1; i>=0; i--) {
			smallPrimesLogPArray[i] = Math.log(primesArray[i]) * sieveParams.lnPMultiplier;
		}
		
		// Allocate sieve array: Typically SIQS adjusts such that pMax/sieveArraySize = 2.5 to 5.0.
		// For large primes with 0 or 1 sieve locations we need to allocate pMax+1 entries;
		// For primes p[i], i<p1Index, we need p[i]+sieveArraySize = 2*sieveArraySize entries.
		this.sieveArraySize = sieveParams.sieveArraySize;
		int pMax = sieveParams.pMax;
		int sieveAllocationSize = Math.max(pMax+1, 2*sieveArraySize);
		sieveArrayAddress = UnsafeUtil.allocateMemory(sieveAllocationSize);
		if (DEBUG) LOG.debug("pMax = " + pMax + ", sieveArraySize = " + sieveArraySize + " --> sieveAllocationSize = " + sieveAllocationSize);

		if (ANALYZE) initDuration = sieveDuration = collectDuration = 0;
	}

	@Override
	public void initializeForAParameter(int d, BigInteger daParam, SolutionArrays solutionArrays, int filteredBaseSize) {
		this.d = d;
		this.daParam = daParam;
		this.solutionArrays = solutionArrays;
		int[] pArray = solutionArrays.pArray;
		this.primeBaseSize = filteredBaseSize;
		
		this.p1Index = binarySearch.getInsertPosition(pArray, primeBaseSize, sieveArraySize);
		this.p2Index = binarySearch.getInsertPosition(pArray, p1Index, (sieveArraySize+1)/2);
		this.p3Index = binarySearch.getInsertPosition(pArray, p2Index, (sieveArraySize+2)/3);
		if (DEBUG) LOG.debug("primeBaseSize=" + primeBaseSize + ", p1Index=" + p1Index + ", p2Index=" + p2Index + ", p3Index=" + p3Index);
		
		// The minimum number of x-solutions in the sieve array is floor(sieveArraySize/p).
		// E.g. for p=3, sieveArraySize=8 there are solutions (0, 3, 6), (1, 4, 7), (2, 5)  <-- 8 is not in sieve array anymore
		// -> minSolutionCount = 2
		this.minSolutionCounts_m3 = new int[p3Index];
		for (int i=p3Index-1; i>=pMinIndex; i--) {
			try { // entering a try-catch-block has no time cost
				minSolutionCounts_m3[i] = sieveArraySize/pArray[i] - 3;
			} catch (Exception e) {
				LOG.error("p3Index = " + p3Index + ", pMinIndex = " + pMinIndex + ", i = " + i + ", pArray[i] = " + pArray[i]);
				throw e;
			}
			//LOG.debug("p=" + primesArray[i] + ": minSolutionCount = " + minSolutionCounts_m3[i]);
		}
	}

	@Override
	public void setBParameter(BigInteger b) {
		this.bParam = b;
		if (DEBUG) assertTrue(b.multiply(b).subtract(kN).mod(daParam).equals(I_0));
		this.cParam = b.multiply(b).subtract(kN).divide(daParam);
	}

	@Override
	public List<SmoothCandidate> sieve() {
		if (ANALYZE) timer.capture();
		this.initializeSieveArray(sieveArraySize);
		if (ANALYZE) initDuration += timer.capture();
		
		// Sieve with positive x, large primes:
		final int[] pArray = solutionArrays.pArray;
		final int[] x1Array = solutionArrays.x1Array;
		final int[] x2Array = solutionArrays.x2Array;
		final byte[] logPArray = solutionArrays.logPArray;
		int i, j;
		long x1Addr, x2Addr;
		for (i=primeBaseSize-1; i>=p1Index; i--) {
			// x1 == x2 happens only if p divides k -> for large primes p > k there are always 2 distinct solutions.
			// x1, x2 may exceed sieveArraySize, but we allocated the arrays somewhat bigger to save the size checks.
			final byte logP = logPArray[i];
			x1Addr = sieveArrayAddress + x1Array[i];
			UNSAFE.putByte(x1Addr, (byte) (UNSAFE.getByte(x1Addr) + logP));
			x2Addr = sieveArrayAddress + x2Array[i];
			UNSAFE.putByte(x2Addr, (byte) (UNSAFE.getByte(x2Addr) + logP));
		}
		for ( ; i>=p2Index; i--) {
			final int p = pArray[i];
			final byte logP = logPArray[i];
			x1Addr = sieveArrayAddress + x1Array[i];
			UNSAFE.putByte(x1Addr, (byte) (UNSAFE.getByte(x1Addr) + logP));
			x2Addr = sieveArrayAddress + x2Array[i];
			UNSAFE.putByte(x2Addr, (byte) (UNSAFE.getByte(x2Addr) + logP));
			UNSAFE.putByte(x1Addr+p, (byte) (UNSAFE.getByte(x1Addr+p) + logP));
			UNSAFE.putByte(x2Addr+p, (byte) (UNSAFE.getByte(x2Addr+p) + logP));
		}
		for ( ; i>=p3Index; i--) {
			final int p = pArray[i];
			final byte logP = logPArray[i];
			x1Addr = sieveArrayAddress + x1Array[i];
			UNSAFE.putByte(x1Addr, (byte) (UNSAFE.getByte(x1Addr) + logP));
			x2Addr = sieveArrayAddress + x2Array[i];
			UNSAFE.putByte(x2Addr, (byte) (UNSAFE.getByte(x2Addr) + logP));
			x1Addr += p;
			UNSAFE.putByte(x1Addr, (byte) (UNSAFE.getByte(x1Addr) + logP));
			x2Addr += p;
			UNSAFE.putByte(x2Addr, (byte) (UNSAFE.getByte(x2Addr) + logP));
			UNSAFE.putByte(x1Addr+p, (byte) (UNSAFE.getByte(x1Addr+p) + logP));
			UNSAFE.putByte(x2Addr+p, (byte) (UNSAFE.getByte(x2Addr+p) + logP));
		}
		// Unrolling the loop with four large prime bounds looks beneficial for N>=340 bit
		
		// Positive x, small primes:
		for ( ; i>=pMinIndex; i--) {
			final int p = pArray[i];
			final byte logP = logPArray[i];
			x1Addr = sieveArrayAddress + x1Array[i];
			UNSAFE.putByte(x1Addr, (byte) (UNSAFE.getByte(x1Addr) + logP));
			x2Addr = sieveArrayAddress + x2Array[i];
			UNSAFE.putByte(x2Addr, (byte) (UNSAFE.getByte(x2Addr) + logP));
			x1Addr += p;
			UNSAFE.putByte(x1Addr, (byte) (UNSAFE.getByte(x1Addr) + logP));
			x2Addr += p;
			UNSAFE.putByte(x2Addr, (byte) (UNSAFE.getByte(x2Addr) + logP));
			x1Addr += p;
			UNSAFE.putByte(x1Addr, (byte) (UNSAFE.getByte(x1Addr) + logP));
			x2Addr += p;
			UNSAFE.putByte(x2Addr, (byte) (UNSAFE.getByte(x2Addr) + logP));
			for (j=minSolutionCounts_m3[i]; j>=0; j--) {
				x1Addr += p;
				UNSAFE.putByte(x1Addr, (byte) (UNSAFE.getByte(x1Addr) + logP));
				x2Addr += p;
				UNSAFE.putByte(x2Addr, (byte) (UNSAFE.getByte(x2Addr) + logP));
			}
		} // end for (p)
		if (ANALYZE) sieveDuration += timer.capture();

		// collect results: we check 8 sieve locations in one long
		smoothCandidates.clear();
		long x = sieveArrayAddress-8;
		while (x<sieveArrayAddress+sieveArraySize-8) {
			long t = UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8); 
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			if((t & LONG_MASK) == 0) continue;
			
			// back up to get the last 8 and look in more detail
			x -= 256;
			
			for(int l=0; l<32; l++) {				
				final long y = UNSAFE.getLong(x+=8);
				if((y & LONG_MASK) != 0) {
					testLongPositive(y, (int) (x-sieveArrayAddress));
				}
			}
		}
		if (ANALYZE) collectDuration += timer.capture();
		
		// re-initialize sieve array for negative x
		this.initializeSieveArray(sieveArraySize);
		if (ANALYZE) initDuration += timer.capture();

		// negative x, large primes:
		for (i=primeBaseSize-1; i>=p1Index; i--) {
			final int p = pArray[i];
			final byte logP = logPArray[i];
			x1Addr = sieveArrayAddress + p - x1Array[i];
			UNSAFE.putByte(x1Addr, (byte) (UNSAFE.getByte(x1Addr) + logP));
			x2Addr = sieveArrayAddress + p - x2Array[i];
			UNSAFE.putByte(x2Addr, (byte) (UNSAFE.getByte(x2Addr) + logP));
		}
		for (; i>=p2Index; i--) {
			final int p = pArray[i];
			final byte logP = logPArray[i];
			x1Addr = sieveArrayAddress + p - x1Array[i];
			UNSAFE.putByte(x1Addr, (byte) (UNSAFE.getByte(x1Addr) + logP));
			x2Addr = sieveArrayAddress + p - x2Array[i];
			UNSAFE.putByte(x2Addr, (byte) (UNSAFE.getByte(x2Addr) + logP));
			UNSAFE.putByte(x1Addr+p, (byte) (UNSAFE.getByte(x1Addr+p) + logP));
			UNSAFE.putByte(x2Addr+p, (byte) (UNSAFE.getByte(x2Addr+p) + logP));
		}
		for (; i>=p3Index; i--) {
			final int p = pArray[i];
			final byte logP = logPArray[i];
			x1Addr = sieveArrayAddress + p - x1Array[i];
			UNSAFE.putByte(x1Addr, (byte) (UNSAFE.getByte(x1Addr) + logP));
			x2Addr = sieveArrayAddress + p - x2Array[i];
			UNSAFE.putByte(x2Addr, (byte) (UNSAFE.getByte(x2Addr) + logP));
			x1Addr += p;
			UNSAFE.putByte(x1Addr, (byte) (UNSAFE.getByte(x1Addr) + logP));
			x2Addr += p;
			UNSAFE.putByte(x2Addr, (byte) (UNSAFE.getByte(x2Addr) + logP));
			UNSAFE.putByte(x1Addr+p, (byte) (UNSAFE.getByte(x1Addr+p) + logP));
			UNSAFE.putByte(x2Addr+p, (byte) (UNSAFE.getByte(x2Addr+p) + logP));
		}
		// negative x, small primes:
		for (; i>=pMinIndex; i--) {
			final int p = pArray[i];
			final byte logP = logPArray[i];
			x1Addr = sieveArrayAddress + p - x1Array[i];
			UNSAFE.putByte(x1Addr, (byte) (UNSAFE.getByte(x1Addr) + logP));
			x2Addr = sieveArrayAddress + p - x2Array[i];
			UNSAFE.putByte(x2Addr, (byte) (UNSAFE.getByte(x2Addr) + logP));
			x1Addr += p;
			UNSAFE.putByte(x1Addr, (byte) (UNSAFE.getByte(x1Addr) + logP));
			x2Addr += p;
			UNSAFE.putByte(x2Addr, (byte) (UNSAFE.getByte(x2Addr) + logP));
			x1Addr += p;
			UNSAFE.putByte(x1Addr, (byte) (UNSAFE.getByte(x1Addr) + logP));
			x2Addr += p;
			UNSAFE.putByte(x2Addr, (byte) (UNSAFE.getByte(x2Addr) + logP));
			for (j=minSolutionCounts_m3[i]; j>=0; j--) {
				x1Addr += p;
				UNSAFE.putByte(x1Addr, (byte) (UNSAFE.getByte(x1Addr) + logP));
				x2Addr += p;
				UNSAFE.putByte(x2Addr, (byte) (UNSAFE.getByte(x2Addr) + logP));
			}
		} // end for (p)
		if (ANALYZE) sieveDuration += timer.capture();

		// collect results
		x = sieveArrayAddress-8;
		while (x<sieveArrayAddress+sieveArraySize-8) {
			long t = UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8); 
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			t |= UNSAFE.getLong(x+=8);
			if((t & LONG_MASK) == 0) continue;
			
			// back up to get the last 8 and look in more detail
			x -= 256;
			
			for(int l=0; l<32; l++) {
				final long y = UNSAFE.getLong(x+=8);
				if((y & LONG_MASK) != 0) {
					testLongNegative(y, (int) (x-sieveArrayAddress));
				}
			}
		}
		if (ANALYZE) collectDuration += timer.capture();
		return smoothCandidates;
	}
	
	/**
	 * Initialize the sieve array(s) with the initializer value computed before.
	 * @param sieveArraySize
	 */
	private void initializeSieveArray(int sieveArraySize) {
		// Overwrite existing arrays with initializer. We know that sieve array size is a multiple of 256.
		UNSAFE.setMemory(sieveArrayAddress, sieveArraySize, initializer);		
	}

	private void testLongPositive(long y, int x) {
		if ((y & LOWER_MASK) != 0) {
			final int y0 = (int) y;
			if ((y0 &       0x80) != 0) addSmoothCandidate(x  ,  y0      & 0xFF);
			if ((y0 &     0x8000) != 0) addSmoothCandidate(x+1, (y0>> 8) & 0xFF);
			if ((y0 &   0x800000) != 0) addSmoothCandidate(x+2, (y0>>16) & 0xFF);
			if ((y0 & 0x80000000) != 0) addSmoothCandidate(x+3, (y0>>24) & 0xFF);
		}
		if((y & UPPER_MASK) != 0) {
			final int y1 = (int) (y >> 32);
			if ((y1 &       0x80) != 0) addSmoothCandidate(x+4,  y1      & 0xFF);
			if ((y1 &     0x8000) != 0) addSmoothCandidate(x+5, (y1>> 8) & 0xFF);
			if ((y1 &   0x800000) != 0) addSmoothCandidate(x+6, (y1>>16) & 0xFF);
			if ((y1 & 0x80000000) != 0) addSmoothCandidate(x+7, (y1>>24) & 0xFF);
		}
	}
	
	private void testLongNegative(long y, int x) {
		if ((y & LOWER_MASK) != 0) {
			final int y0 = (int) y;
			if ((y0 &       0x80) != 0) addSmoothCandidate(- x   ,  y0      & 0xFF);
			if ((y0 &     0x8000) != 0) addSmoothCandidate(-(x+1), (y0>> 8) & 0xFF);
			if ((y0 &   0x800000) != 0) addSmoothCandidate(-(x+2), (y0>>16) & 0xFF);
			if ((y0 & 0x80000000) != 0) addSmoothCandidate(-(x+3), (y0>>24) & 0xFF);
		}
		if((y & UPPER_MASK) != 0) {
			final int y1 = (int) (y >> 32);
			if ((y1 &       0x80) != 0) addSmoothCandidate(-(x+4),  y1      & 0xFF);
			if ((y1 &     0x8000) != 0) addSmoothCandidate(-(x+5), (y1>> 8) & 0xFF);
			if ((y1 &   0x800000) != 0) addSmoothCandidate(-(x+6), (y1>>16) & 0xFF);
			if ((y1 & 0x80000000) != 0) addSmoothCandidate(-(x+7), (y1>>24) & 0xFF);
		}
	}

	private void addSmoothCandidate(int x, int score) {
		// Compute Q(x)/(da): Note that if kN==1 (mod 8), then d=2 and Q(x) divides not just a but 2a
		BigInteger xBig = BigInteger.valueOf(x);
		BigInteger dax = daParam.multiply(xBig);
		BigInteger A = dax.add(bParam);
		BigInteger QDivDa = dax.multiply(xBig).add(bParam.multiply(BigInteger.valueOf(x<<1))).add(cParam);

		// Replace estimated small factor contribution by the true one:
		// The score has to rise if the true small factor contribution is greater than expected.
		// XXX Could we do Bernsteinisms here?
		SmallFactorsTDivResult smallFactorsTDivResult = tdivBySmallPrimes(A, QDivDa, x);
		int logSmallPSum = (int) smallFactorsTDivResult.logPSum;
		int adjustedScore = score - ((int)initializer) + logSmallPSum;
		if (DEBUG) LOG.debug("adjust initializer: original score = " + score + ", initializer = " + (int)initializer + ", logSmallPSum = " + logSmallPSum + " -> adjustedScore1 = " + adjustedScore);
		
		// XXX Also correct the contribution of the q-params whose product gives the a-parameter?

		// Replace estimated QDivDa size by the true one.
		// The score has to rise if the true QDivDa size is smaller than expected, because then we have less to factor.
		// We would always expect that trueLogQDivDaSize <= logQdivDaEstimate, because the latter is supposed to be an upper bound.
		// But actually we can get much bigger trueLogQDivDaSize values than expected, like 196 bit instead of logQdivDaEstimate=182 bit.
		// XXX This may be caused by imperfect aParam or bParam choices, but should get investigated.
		int trueLogQDivDaSize = (int) (QDivDa.bitLength() * ld2logPMultiplier);
		if (DEBUG) {
			if (trueLogQDivDaSize > logQdivDaEstimate + 2) { // +2 -> don't log too much :-/
				LOG.error("d=" + d + ": logQdivDaEstimate = " + logQdivDaEstimate + ", but trueLogQDivDaSize = " + trueLogQDivDaSize);
			}
			assertTrue(trueLogQDivDaSize <= logQdivDaEstimate + 2); // fails sometimes
		}
		
		int adjustedScore2 = (int) (adjustedScore + this.logQdivDaEstimate - trueLogQDivDaSize);
		if (DEBUG) LOG.debug("adjust Q/a size: adjustedScore1 = " + adjustedScore + ", logQdivDaEstimate = " + logQdivDaEstimate + ", truelogQDivDaSize = " + trueLogQDivDaSize + " -> adjustedScore2 = " + adjustedScore2);

		// If we always had trueLogQDivDaSize <= logQdivDaEstimate, then this check would be useless, because the adjusted score could only rise
		if (adjustedScore2 > tdivTestMinLogPSum) {
			if (DEBUG) LOG.debug("adjustedScore2 = " + adjustedScore2 + " is greater than tdivTestMinLogPSum = " + tdivTestMinLogPSum + " -> pass Q to tdiv");
			smoothCandidates.add(new SmoothCandidate(x, smallFactorsTDivResult.Q_rest, A, smallFactorsTDivResult.smallFactors));
		}
	}
	
	private SmallFactorsTDivResult tdivBySmallPrimes(BigInteger A, BigInteger QDivDa, int x) {
		SmallFactorsTDivResult smallFactorsTDivResult = new SmallFactorsTDivResult();
		SortedIntegerArray smallFactors = smallFactorsTDivResult.smallFactors;
		// For more precision, here we compute the logPSum in doubles instead of using solutionArrays.logPArray
		double logPSum = 0;
		
		// sign
		BigInteger Q_rest = QDivDa;
		if (QDivDa.signum() < 0) {
			smallFactors.add(-1);
			Q_rest = QDivDa.negate();
		}
		
		// Remove multiples of 2
		int lsb = Q_rest.getLowestSetBit();
		if (lsb > 0) {
			smallFactors.add(2, (short)lsb);
			logPSum += smallPrimesLogPArray[0] * lsb;
			Q_rest = Q_rest.shiftRight(lsb);
		}

		// Pass 1: Test solution arrays.
		// IMPORTANT: Java gives x % p = x for |x| < p, and we have many p bigger than any sieve array entry.
		// IMPORTANT: Not computing the modulus in these cases improves performance by almost factor 2!
		int pass2Count = 0;
		int[] pArray = solutionArrays.pArray;
		int[] primes = solutionArrays.primes;
		int[] exponents = solutionArrays.exponents;
		long[] pinvArrayL = solutionArrays.pinvArrayL;
		int[] x1Array = solutionArrays.x1Array, x2Array = solutionArrays.x2Array;
		
		final int xAbs = x<0 ? -x : x;
		for (int pIndex = pMinIndex-1; pIndex > 0; pIndex--) { // p[0]=2 was already tested
			int p = pArray[pIndex];
			int xModP;
			if (xAbs<p) {
				xModP = x<0 ? x+p : x;
			} else {
				// Compute x%p using long-valued Barrett reduction, see https://en.wikipedia.org/wiki/Barrett_reduction.
				// We can use the long-variant here because x*m will never overflow positive long values.
				final long m = pinvArrayL[pIndex];
				final long q = ((x*m)>>>32);
				xModP = (int) (x - q * p);
				if (xModP<0) xModP += p;
				else if (xModP>=p) xModP -= p;
				if (DEBUG) {
					assertTrue(0<=xModP && xModP<p);
					int xModP2 = x % p;
					if (xModP2<0) xModP2 += p;
					if (xModP != xModP2) LOG.debug("x=" + x + ", p=" + p + ": xModP=" + xModP + ", but xModP2=" + xModP2);
					assertEquals(xModP2, xModP);
				}
			}
			if (xModP==x1Array[pIndex] || xModP==x2Array[pIndex]) {
				pass2PrimeIndices[pass2Count] = pIndex;
				pass2Primes[pass2Count] = primes[pIndex];
				pass2Exponents[pass2Count] = exponents[pIndex];
				pass2Powers[pass2Count++] = p;
				// for some reasons I do not understand it is faster to divide Q by p in pass 2 only, not here
			}
		}

		// Pass 2: Reduce Q by the pass2Primes and collect small factors
		Q_rest_UBI.set(Q_rest);
		for (int pass2Index = 0; pass2Index < pass2Count; pass2Index++) {
			int p = pass2Powers[pass2Index];
			int rem;
			while ((rem = Q_rest_UBI.divideAndRemainder(p, quotient_UBI)) == 0) {
				// the division was exact. assign quotient to Q_rest and add p to factors
				UnsignedBigInt tmp = Q_rest_UBI;
				Q_rest_UBI = quotient_UBI;
				quotient_UBI = tmp;
				smallFactors.add(pass2Primes[pass2Index], (short)pass2Exponents[pass2Index]);
				logPSum += smallPrimesLogPArray[pass2PrimeIndices[pass2Index]] * pass2Exponents[pass2Index];
				if (DEBUG) {
					BigInteger pBig = BigInteger.valueOf(p);
					BigInteger[] div = Q_rest.divideAndRemainder(pBig);
					assertEquals(div[1].intValue(), rem);
					Q_rest = div[0];
				}
			}
		}

		smallFactorsTDivResult.Q_rest = Q_rest_UBI.toBigInteger();
		smallFactorsTDivResult.logPSum = logPSum;
		return smallFactorsTDivResult;
	}

	@Override
	public SieveReport getReport() {
		return new SieveReport(initDuration, sieveDuration, collectDuration);
	}
	
	@Override
	public void cleanUp() {
		solutionArrays = null;
		minSolutionCounts_m3 = null;
		UnsafeUtil.freeMemory(sieveArrayAddress);
	}
}