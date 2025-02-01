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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Tests for binary search in bottom-up sorted integer arrays.
 * @author Tilman Neumann
 */
public class BinarySearchTest {
	private static final Logger LOG = LogManager.getLogger(BinarySearchTest.class);

	private static final BinarySearch bs = new BinarySearch();
	
	@Before
	public void setup() {
		ConfigUtil.initProject();
	}

	@Test
	public void testGetPreciseInsertPosition() {
		int[] array = new int[] {1, 2, 3, 7, 10, 10, 11};
		assertCorrectPreciseInsertPosition(array, 4, 3, 3);
		assertCorrectPreciseInsertPosition(array, 4, 20, 4); // insert index can not exceed maxIndex
		assertCorrectPreciseInsertPosition(array, 7, 10, 6);
		assertCorrectPreciseInsertPosition(array, 7, 11, 7);
		assertCorrectPreciseInsertPosition(array, 7, 20, 7);
	}
	
	@Test
	public void testGetPreciseInsertPositionWithMultipleEntries() {
		// each entry occurs 10 times
		int[] array = new int[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7};
		assertCorrectPreciseInsertPosition(array, 70, 5, 50);
		assertCorrectPreciseInsertPosition(array, 59, 4, 40);
		assertCorrectPreciseInsertPosition(array, 55, 3, 30);
		assertCorrectPreciseInsertPosition(array, 42, 2, 20);
		assertCorrectPreciseInsertPosition(array, 15, 1, 10);
	}

	@Test
	public void testGetInsertPosition() {
		int[] array = new int[] {1, 2, 3, 7, 10, 10, 11};
		assertCorrectInsertPosition(array, 4, 3, 3);
		assertCorrectInsertPosition(array, 4, 20, 4); // insert index can not exceed maxIndex
		assertCorrectInsertPosition(array, 7, 10, 6);
		assertCorrectInsertPosition(array, 7, 11, 7);
		assertCorrectInsertPosition(array, 7, 20, 7);
	}

	@Test
	public void testGetAmbigousInsertPositionWithMultipleEntries() {
		// each entry occurs 10 times
		int[] array = new int[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7};
		assertWrongInsertPosition(array, 70, 5, 50);
		assertWrongInsertPosition(array, 59, 4, 40);
		assertWrongInsertPosition(array, 55, 3, 30);
		assertWrongInsertPosition(array, 42, 2, 20);
		assertWrongInsertPosition(array, 15, 1, 10);
	}

	private static void assertCorrectPreciseInsertPosition(int[] array, int maxIndex, int x, int expectedInsertIndex) {
		int index = bs.getPreciseInsertPosition(array, maxIndex, x);
		LOG.info("precise insert index of '" + x + "' in " + Arrays.toString(array) + " (restricted to maxIndex=" + maxIndex + ") = " + index);
		assertEquals(expectedInsertIndex, index);
	}

	private static void assertCorrectInsertPosition(int[] array, int maxIndex, int x, int expectedInsertIndex) {
		int index = bs.getInsertPosition(array, maxIndex, x);
		LOG.info("insert index of '" + x + "' in " + Arrays.toString(array) + " (restricted to maxIndex=" + maxIndex + ") = " + index);
		assertEquals(expectedInsertIndex, index);
	}

	private static void assertWrongInsertPosition(int[] array, int maxIndex, int x, int expectedInsertIndex) {
		int index = bs.getInsertPosition(array, maxIndex, x);
		LOG.info("insert index of '" + x + "' in " + Arrays.toString(array) + " (restricted to maxIndex=" + maxIndex + ") = " + index);
		assertNotEquals(expectedInsertIndex, index);
	}
}
