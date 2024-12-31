/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2024 Tilman Neumann - tilman.neumann@web.de
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
package de.tilman_neumann.jml.partitions;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.combinatorics.Factorial;
import de.tilman_neumann.util.ConfigUtil;

public class MpiPartitionGeneratorTest {
	
	private static final Logger LOG = LogManager.getLogger(MpiPartitionGeneratorTest.class);
	
	private static final boolean DEBUG = false;
	
	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}

	/**
	 * Test the number of essentially different factorizations of n= 1, 2, 3, ...
	 * A001055 (n=1...) = 1, 1, 1, 2, 1, 2, 1, 3, 2, 2, 1, 4, 1, 2, 2, 5, 1, 4, 1, 4, 2, 2, 1, 7, 2, 2, 3, 4, 1, ...
	 */
	@Test
	public void testNumberOfFactorizations() {
		int maxListSize = 30;
		ArrayList<Long> list = new ArrayList<>();
    	for (int n=1; n<=10000; n++) {
    		long numberOfFactorizations = MpiPartitionGenerator.numberOfFactorizationsOf(BigInteger.valueOf(n));
    		if (DEBUG) LOG.debug(n + " can be factored in " + numberOfFactorizations + " different ways");
    		if (list.size() < maxListSize) list.add(numberOfFactorizations);
    	}
    	if (DEBUG) LOG.debug("First 30 sequence elements = " + list);
    	assertEquals("[1, 1, 1, 2, 1, 2, 1, 3, 2, 2, 1, 4, 1, 2, 2, 5, 1, 4, 1, 4, 2, 2, 1, 7, 2, 2, 3, 4, 1, 5]", list.toString());
	}
	
	/**
	 * Test record numbers of essentially different factorizations of n = 1, 2, 3, ...
	 * A033833 = 1, 4, 8, 12, 16, 24, 36, 48, 72, 96, 120, 144, 192, 216, 240, 288, 360, 432, 480, 576, 720, 960, 1080, 1152, 1440, 2160, 2880, 4320, 5040, 5760, 7200, 8640, 10080, 11520, ...
	 */
	@Test
	public void testNumberOfFactorizationsRecords() {
		int maxListSize = 30;
		ArrayList<Integer> list = new ArrayList<>();
 		long record = 0;
    	for (int n=1; n<=10000; n++) {
    		long numberOfFactorizations = MpiPartitionGenerator.numberOfFactorizationsOf(BigInteger.valueOf(n));
    		if (numberOfFactorizations > record) { // same value does not count as new record
    			LOG.info(n + " can be factored in " + numberOfFactorizations + " different ways");
    			record = numberOfFactorizations;
        		if (list.size() < maxListSize) list.add(n);
    		}
    	}
    	if (DEBUG) LOG.debug("First 30 sequence elements = " + list);
    	assertEquals("[1, 4, 8, 12, 16, 24, 36, 48, 72, 96, 120, 144, 192, 216, 240, 288, 360, 432, 480, 576, 720, 960, 1080, 1152, 1440, 2160, 2880, 4320, 5040, 5760]", list.toString());
	}
	
	/**
	 * Test record numbers of essentially different factorizations of n per bit.
	 * This sequence S(n) = 1, 12, 16, 24, 36, 48, 72, 96, 120, 144, 240, 288, 360, 432, 480, 576, 720, 960, 1080, 1152, ... is not in OEIS.
	 */
	@Test
	public void testNumberOfFactorizationsRecordsPerBit() {
		int maxListSize = 30;
		ArrayList<Integer> list = new ArrayList<>();
		double recordRatio = 0;
    	for (int n=1; n<=10000; n++) {
    		long numberOfFactorizations = MpiPartitionGenerator.numberOfFactorizationsOf(BigInteger.valueOf(n));
    		double bits = n==1 ? 1.0 : Math.log(n)/Math.log(2.0); // ld(n)
    		double ratio = numberOfFactorizations/bits;
    		if (ratio > recordRatio) { // same value does not count as new record
    			LOG.info(n + " (" + bits + " bit) can be factored in " + numberOfFactorizations + " different ways -> ratio = " + ratio);
    			recordRatio = ratio;
        		if (list.size() < maxListSize) list.add(n);
    		}
    	}
    	if (DEBUG) LOG.debug("First 30 sequence elements = " + list);
    	assertEquals("[1, 12, 16, 24, 36, 48, 72, 96, 120, 144, 240, 288, 360, 432, 480, 576, 720, 960, 1080, 1152, 1440, 2160, 2880, 4320, 5040, 5760, 7200, 8640]", list.toString());
	}

	/**
	 * Test the number of essentially different factorizations of n!.
	 * This is A076716(n=2, 3, ...) = 1, 2, 7, 21, 98, 392, 2116, 11830, 70520, 425240, ...
	 */
	@Test
	public void testNumberOfFactorialFactorizations() {
		ArrayList<Long> list = new ArrayList<>();
    	for (int n=2; n<=11; n++) {
			long start = System.currentTimeMillis();
    		BigInteger factorial = Factorial.factorial(n);
    		long numberOfFactorizations = MpiPartitionGenerator.numberOfFactorizationsOf(factorial);
    		LOG.info(n + "! = " + factorial + " can be factored in " + numberOfFactorizations + " different ways (computed in " + (System.currentTimeMillis()-start) + " ms)");
    		list.add(numberOfFactorizations);
    	}
    	if (DEBUG) LOG.debug("list = " + list);
    	assertEquals("[1, 2, 7, 21, 98, 392, 2116, 11830, 70520, 425240]", list.toString());
	}
	
	/**
	 * Test the number of partitions of partitions.
	 * A001970 = 1, 1, 3, 6, 14, 27, 58, 111, 223, 424, 817, 1527, 2870, 5279, 9710, 17622, 31877, 57100, 101887, 180406, 318106, 557453, 972796, 1688797, 2920123, ...
	 */
	@Test
	public void testPartitionsOfPartitions() {
		ArrayList<Long> list = new ArrayList<>();
		for (int n=0; n<=20; n++) {
			long start = System.currentTimeMillis();
			long totalNumberOfPartitions = MpiPartitionGenerator.numberOfPartitionsOfPartitions(n);
			LOG.info(n + " has " + totalNumberOfPartitions + " partitions of partitions (computed in " + (System.currentTimeMillis()-start) + " ms)");
    		list.add(totalNumberOfPartitions);
		}
    	if (DEBUG) LOG.debug("list = " + list);
    	assertEquals("[1, 1, 3, 6, 14, 27, 58, 111, 223, 424, 817, 1527, 2870, 5279, 9710, 17622, 31877, 57100, 101887, 180406, 318106]", list.toString());
	}
	
	/**
	 * Test the number of partitions of strong multisets.
	 * This is A035310 = "Ways of partitioning an n-multiset with multiplicities some partition of n."
     * = 1, 4, 12, 47, 170, 750, 3255, 16010, 81199, 448156, 2579626, 15913058, 102488024, 698976419, 4976098729, 37195337408, 289517846210, 2352125666883, 19841666995265, 173888579505200, 1577888354510786, 14820132616197925, 143746389756336173, 1438846957477988926, ...
	 */
	@Test
	public void testMultisetPartitions() {
		ArrayList<Long> list = new ArrayList<>();
		for (int n=1; n<=10; n++) {
			long start = System.currentTimeMillis();
			long totalNumberOfPartitions = MpiPartitionGenerator.numberOfMultisetPartitions(n);
			LOG.info(n + " has " + totalNumberOfPartitions + " multiset partitions (computed in " + (System.currentTimeMillis()-start) + " ms)");
    		list.add(totalNumberOfPartitions);
		}
    	if (DEBUG) LOG.debug("list = " + list);
    	assertEquals("[1, 4, 12, 47, 170, 750, 3255, 16010, 81199, 448156]", list.toString());
	}
}
