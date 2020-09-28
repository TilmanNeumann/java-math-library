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
package de.tilman_neumann.jml.modular;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Stuff concerning sums of 4 squares representations of natural numbers.
 * 
 * @author Tilman Neumann
 */
public class SumOf4Squares {

	private static final Logger LOG = Logger.getLogger(SumOf4Squares.class);

	private static final boolean DEBUG = false;
	
	/**
	 * Compute all elements of A004215 below m, i.e. all k<m such that k can be expressed as a sum of 4 squares
	 * but not by a sum of less than four squares.
	 * As commented in http://oeis.org/A004215, these are numbers of the form 4^i(8j+7), i >= 0, j >= 0.
	 * 
	 * @param m
	 * @return A004215 entries < m
	 */
	public static TreeSet<Long> getA004215(long m) {
		TreeSet<Long> result = new TreeSet<Long>();
		int mBits = 64 - Long.numberOfLeadingZeros(m);
		int iMax = (mBits+1)>>1;
		for (int i=0; i<=iMax; i++) {
			long leftTerm = 1L << (i<<1); // 4^i == 2^(2*i)
			int j=0;
			while (true) {
				long entry = leftTerm * (8*j+7);
				if (entry > m) break;
				result.add(entry);
				j++;
			}
		}
		return result;
	}
	
	/**
	 * Compute all elements of A004215 below m = 2^n, i.e. all k<m such that k can be expressed as a sum of 4 squares
	 * but not by a sum of less than 4 squares.
	 * 
	 * This implementation seems to be faster than v1 but in the current form suffers from garbage collection.
	 * 
	 * @param n
	 * @return A004215 entries < 2^n
	 */
	public static List<Long> getA004215_v2(long n) {
		if (n < 3) return new ArrayList<>();
		
		List<Long> list = Arrays.asList(new Long[] {0L, 1L}); // mod=2
		for (int i=1; i<n; i++) {
			boolean iOdd = (i & 1) == 1;
			int mod = 1<<i;
			List<Long> nextList = new ArrayList<>(list); // copy
			for (long elem : list) {
				nextList.add(elem + mod);
			}
			// remove some stuff
//			if (DEBUG) LOG.debug("mod=" + mod + ", i odd = " + iOdd + ", list = " + list + ", nextList = " + nextList);
			if (iOdd) {
				nextList.remove(Long.valueOf(mod));
				nextList.remove(Long.valueOf(mod>>1));
			} else {
				nextList.remove(Long.valueOf((mod>>1) + (mod>>2)));
			}
//			if (DEBUG) LOG.debug("reduced nextList = " + nextList);
			list = nextList;
		}
		
		// remove more stuff
		list.remove(0L);
		boolean nOdd = (n & 1) == 1;
		if (nOdd) {
			list.remove(Long.valueOf(1 << (n-1)));
		} else {
			list.remove(Long.valueOf((1 << (n-1)) + (1 << (n-2))));
		}
		return list;
	}
	
	/**
	 * Another implementation using arrays, much faster than the previous ones.
	 * 
	 * @param n such that m=2^n
	 * @param array an array big enough to take roughly 2^n/6 values
	 * @return number of entries
	 */
	public static int getA004215_v3(long n, long[] array) {
		if (n < 3) return 0;
		
		// start with entries mod 2
		array[0] = 0;
		array[1] = 1;
		int count = 2;
		
		for (int i=1; i<n; i++) {
			boolean iOdd = (i & 1) == 1;
			int mod = 1<<i;
			
			// duplicate
			for (int j=0; j<count; j++) {
				array [j+count] = array[j] + mod;
			}
			count <<= 1;
			
			if (DEBUG) LOG.debug("mod=" + mod + ", i odd = " + iOdd + ", count = " + count + ", array = " + Arrays.toString(array));
			
			// remove some stuff
			int nextCount = 0;
			if (iOdd) {
				for (int j=0; j<count; j++) {
					long elem = array[j];
					if (elem != mod && elem != (mod>>1)) {
						// if (DEBUG) LOG.debug("mod=" + mod + ", i odd = " + iOdd + ": add elem = " + elem);
						array [nextCount++] = elem;
					}
				}
			} else {
				for (int j=0; j<count; j++) {
					long elem = array[j];
					if (elem != (mod>>1) + (mod>>2)) {
						// if (DEBUG) LOG.debug("mod=" + mod + ", i odd = " + iOdd + ": add elem = " + elem);
						array [nextCount++] = elem;
					}
				}
			}
			if (DEBUG) LOG.debug("reduced array = " + Arrays.toString(array));
			count = nextCount;
		}
		
		// remove last stuff
		int outCount = 0;
		boolean nOdd = (n & 1) == 1;
		if (nOdd) {
			for (int j=1; j<count; j++) { // remove 0
				long elem = array[j];
				if (elem != (1L << (n-1))) {
					array [outCount++] = elem;
				}
			}
		} else {
			for (int j=1; j<count; j++) { // remove 0
				long elem = array[j];
				if (elem != (1 << (n-1)) + (1 << (n-2))) {
					array [outCount++] = elem;
				}
			}
		}
		
		return outCount;
	}

	/**
	 * A test of the hypothesis that A023105(2^n) == 2 + the number of entries of A004215 that are less than 2^n, for n>0.
	 * Confirmed until n=29.
	 * 
	 * @param args ignored
	 */
	public static void main(String[] args) {
		ConfigUtil.initProject();
		
		ArrayList<Integer> quadraticResidueCounts = new ArrayList<Integer>();
		ArrayList<Integer> a004215EntryCounts = new ArrayList<Integer>();
		ArrayList<Integer> a004215EntryCounts_v2 = new ArrayList<Integer>();
		ArrayList<Integer> a004215EntryCounts_v3 = new ArrayList<Integer>();
		
		for (int n=0; n<=30; n++) {
			long m = 1L<<n;
			long t0 = System.currentTimeMillis();
			TreeSet<Long> quadraticResiduesMod2PowN = JacobiSymbol.getQuadraticResidues(m);
			long t1 = System.currentTimeMillis();
			LOG.info("n=" + n + ": There are " + quadraticResiduesMod2PowN.size() + " quadratic residues % " + m + (DEBUG ? ": " + quadraticResiduesMod2PowN : "") + " -- duration: " + (t1-t0) + "ms");
			quadraticResidueCounts.add(quadraticResiduesMod2PowN.size());
			
//			t0 = System.currentTimeMillis();
//			TreeSet<Long> a004215Entries = getA004215(m);
//			t1 = System.currentTimeMillis();
//			LOG.info("v1: There are " + a004215Entries.size() + " A004215 entries < " + m + (DEBUG ? ": " + a004215Entries : "") + " -- duration: " + (t1-t0) + "ms");
//			a004215EntryCounts.add(a004215Entries.size());
//			
//			t0 = System.currentTimeMillis();
//			List<Long> a004215Entries_v2 = getA004215_v2(n);
//			t1 = System.currentTimeMillis();
//			LOG.info("v2: There are " + a004215Entries_v2.size() + " A004215 entries < " + m + (DEBUG ? ": " + a004215Entries_v2 : "") + " -- duration: " + (t1-t0) + "ms");
//			a004215EntryCounts_v2.add(a004215Entries_v2.size());
			
			t0 = System.currentTimeMillis();
			long[] a004215Entries_v3 = new long[((1<<n) + 32) / 6]; // #{A004215(k) | k<m} is always near to m/6
			// 32 is fine up to 
			int count = getA004215_v3(n, a004215Entries_v3);
			t1 = System.currentTimeMillis();
			LOG.info("v3: There are " + count + " A004215 entries < " + m + (DEBUG ? ": " + Arrays.toString(a004215Entries_v3) : "") + " -- duration: " + (t1-t0) + "ms");
			a004215EntryCounts_v3.add(count);
			
			LOG.info("");
		}
		
		LOG.info("quadraticResidueCounts = " + quadraticResidueCounts);
		// A023105(n) = 1, 2, 2, 3, 4, 7, 12, 23, 44, 87, 172, 343, ...
		
		LOG.info("v1: a004215EntryCounts = " + a004215EntryCounts);
		LOG.info("v2: a004215EntryCounts = " + a004215EntryCounts_v2);
		LOG.info("v3: a004215EntryCounts = " + a004215EntryCounts_v3);
	}
}
