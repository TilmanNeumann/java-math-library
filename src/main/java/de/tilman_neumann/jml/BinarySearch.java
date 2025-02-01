/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2025 Tilman Neumann - tilman.neumann@web.de
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

import java.util.Arrays;

import de.tilman_neumann.util.Ensure;

/**
 * Binary search in bottom-up sorted integer arrays.
 * @author Tilman Neumann
 */
public class BinarySearch {
	private static final boolean DEBUG = false;
	
	/**
	 * Find the insert position for x into array given that array is sorted bottom-up.
	 * 
	 * This version defines the insert position unambiguously, no matter if some entries in the array occur multiple times, as
	 * If array[maxIndex-1] >  x, return the index of the first entry of array[0].. array[maxIndex-1] greater than x.
	 * If array[maxIndex-1] <= x, return maxIndex.
	 * 
	 * @param array
	 * @param maxIndex the maximum index to consider, exclusive (may be smaller than the array size)
	 * @param x
	 * @return the insert position
	 */
	public int getPreciseInsertPosition(int[] array, int maxIndex, int x) {
		if (maxIndex<=0 || array[maxIndex-1] <= x) return maxIndex;
		int left = 0;
		int right = maxIndex-1;
		int median;
		do {
			median = (left+right)>>1; // floor
			if (array[median] <= x) {
				// the tested element was smaller or equal -> the right insert position must be higher than median
				left = median + 1;
			} else {
				// the tested element was bigger-> median could be the right insert position or too big
				right = median;
			}
		} while (left!=right);
		if (DEBUG) {
			if (left>0)	Ensure.ensureSmallerEquals(array[left-1], x);
			Ensure.ensureSmaller(x, array[left]);
		}
		return left;
	}

	/**
	 * Find the insert position for x into array given that array is sorted bottom-up.
	 * 
	 * This version defines the insert position unambiguously, no matter if some entries in the array occur multiple times, as
	 * If array[maxIndex-1] >  x, return the index of the first entry of array[0].. array[maxIndex-1] greater than x.
	 * If array[maxIndex-1] <= x, return maxIndex.
	 * 
	 * @param array
	 * @param maxIndex the maximum index to consider, exclusive (may be smaller than the array size)
	 * @param x
	 * @return the insert position
	 */
	public int getPreciseInsertPosition(byte[] array, int maxIndex, byte x) {
		if (maxIndex<=0 || array[maxIndex-1] <= x) return maxIndex;
		int left = 0;
		int right = maxIndex-1;
		int median;
		do {
			median = (left+right)>>1; // floor
			if (array[median] <= x) {
				// the tested element was smaller or equal -> the right insert position must be higher than median
				left = median + 1;
			} else {
				// the tested element was bigger-> median could be the right insert position or too big
				right = median;
			}
		} while (left!=right);
		if (DEBUG) {
			if (left>0)	Ensure.ensureSmallerEquals(array[left-1], x);
			Ensure.ensureSmaller(x, array[left]);
		}
		return left;
	}

	/**
	 * Find the insert position for x into array given that array is sorted bottom-up.
	 * 
	 * This version is faster than getPreciseInsertPosition(), but "If the range contains multiple elements with the specified value, there is no guarantee which one will be found."
	 * 
	 * @param array
	 * @param maxIndex the maximum index to consider, exclusive (may be smaller than the array size)
	 * @param x
	 * @return the insert position
	 */
	public int getInsertPosition(int[] array, int maxIndex, int x) {
		// see @returns in Arrays.binarySearch() javadoc
		int i = Arrays.binarySearch(array, 0, maxIndex, x);
		return i >= 0 ? i + 1 : -i - 1;
	}
	
	/**
	 * Find the insert position for x into array given that array is sorted bottom-up.
	 * 
	 * This version is faster than getPreciseInsertPosition(), but "If the range contains multiple elements with the specified value, there is no guarantee which one will be found."
	 * 
	 * @param array
	 * @param maxIndex the maximum index to consider, but "If the range contains multiple elements with the specified value, there is no guarantee which one will be found."
	 * @param x
	 * @return the insert position
	 */
	public int getInsertPosition(byte[] array, int maxIndex, byte x) {
		// see @returns in Arrays.binarySearch() javadoc
		int i = Arrays.binarySearch(array, 0, maxIndex, x);
		return i >= 0 ? i + 1 : -i - 1;
	}
}
