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
package de.tilman_neumann.jml;

import java.util.ArrayList;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.modular.JacobiSymbol;
import de.tilman_neumann.util.ConfigUtil;

/**
 * Stuff concerning sums of 4 squares representations of natural numbers.
 * 
 * @author Tilman Neumann
 */
public class SumOf4Squares {

	private static final Logger LOG = Logger.getLogger(SumOf4Squares.class);

	/**
	 * Compute all elements of A004215 below m, i.e. all n<m such that n can be expressed as a sum of 4 squares
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
	 * A test of the hypothesis that A023105(2^n) == 2 + the number of entries of A004215 that are less than 2^n.
	 * 
	 * @param args ignored
	 */
	public static void main(String[] args) {
		ConfigUtil.initProject();
		
		ArrayList<Integer> quadraticResidueCounts = new ArrayList<Integer>();
		ArrayList<Integer> a004215EntryCounts = new ArrayList<Integer>();
		
		for (int n=0; n<20; n++) {
			int m = 1<<n;
			TreeSet<Long> quadraticResiduesMod2PowN = JacobiSymbol.getQuadraticResidues(m);
			LOG.info("There are " + quadraticResiduesMod2PowN.size() + " quadratic residues % " + m + ": " + quadraticResiduesMod2PowN);
			quadraticResidueCounts.add(quadraticResiduesMod2PowN.size());
			
			TreeSet<Long> a004215Entries = getA004215(m);
			LOG.info("There are " + a004215Entries.size() + " A004215 entries < " + m + ": " + a004215Entries);
			a004215EntryCounts.add(a004215Entries.size());
			LOG.info("");
		}
		
		LOG.info("quadraticResidueCounts = " + quadraticResidueCounts);
		// A023105(n) = 1, 2, 2, 3, 4, 7, 12, 23, 44, 87, 172, 343, ...
		
		LOG.info("a004215EntryCounts = " + a004215EntryCounts);
	}
}
