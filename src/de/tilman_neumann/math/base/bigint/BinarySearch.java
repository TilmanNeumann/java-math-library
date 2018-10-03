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

import static org.junit.Assert.*;

/**
 * Binary search.
 * @author Tilman Neumann
 */
public class BinarySearch {
	private static final boolean DEBUG = false;
	
	/**
	 * Find the index of the first array entry that is greater than elementToSearch.
	 * 
	 * @param elementArray the array to search in
	 * @param elementCount the valid number of array entries (may be smaller than the array size)
	 * @param elementToSearch
	 * @return the index of the first array entry greater than elementToSearch. or -1 if there is no such element
	 */
	public int getFirstGreaterEntryIndex(int[] elementArray, int elementCount, int elementToSearch) {
		if (elementCount<=0 || elementArray[elementCount-1] <= elementToSearch) return -1; // there is no entry greater than elementToSearch
		int left = 0;
		int right = elementCount-1;
		int median;
		do {
			median = (left+right)>>1; // floor
			if (elementArray[median] <= elementToSearch) {
				// the tested element was too small, the result must have a higher index
				left = median + 1;
			} else {
				// the tested element was not too small, it could be right or too big
				right = median;
			}
		} while (left!=right);
		if (DEBUG) {
			if (left>0)	assertTrue(elementArray[left-1] <= elementToSearch);
			assertTrue(elementToSearch < elementArray[left]);
		}
		return left;
	}
}
