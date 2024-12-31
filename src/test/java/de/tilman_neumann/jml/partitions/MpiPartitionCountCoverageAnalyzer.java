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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Tests which natural numbers are represented by counts of partitions of multipartite numbers.
 * 
 * Complete results for partitions of n=1..16 and all their partitions of partitions:
 * 1 is the partitionCount of 1 multipartite numbers: [[1]]
 * 2 is the partitionCount of 2 multipartite numbers: [[2], [1, 1]]
 * 3 is the partitionCount of 1 multipartite numbers: [[3]]
 * 4 is the partitionCount of 1 multipartite numbers: [[2, 1]]
 * 5 is the partitionCount of 2 multipartite numbers: [[4], [1, 1, 1]]
 * 7 is the partitionCount of 2 multipartite numbers: [[5], [3, 1]]
 * 9 is the partitionCount of 1 multipartite numbers: [[2, 2]]
 * 11 is the partitionCount of 2 multipartite numbers: [[6], [2, 1, 1]]
 * 12 is the partitionCount of 1 multipartite numbers: [[4, 1]]
 * 15 is the partitionCount of 2 multipartite numbers: [[7], [1, 1, 1, 1]]
 * 16 is the partitionCount of 1 multipartite numbers: [[3, 2]]
 * 19 is the partitionCount of 1 multipartite numbers: [[5, 1]]
 * 21 is the partitionCount of 1 multipartite numbers: [[3, 1, 1]]
 * 22 is the partitionCount of 1 multipartite numbers: [[8]]
 * 26 is the partitionCount of 1 multipartite numbers: [[2, 2, 1]]
 * 29 is the partitionCount of 1 multipartite numbers: [[4, 2]]
 * 30 is the partitionCount of 2 multipartite numbers: [[9], [6, 1]]
 * 31 is the partitionCount of 1 multipartite numbers: [[3, 3]]
 * 36 is the partitionCount of 1 multipartite numbers: [[2, 1, 1, 1]]
 * 38 is the partitionCount of 1 multipartite numbers: [[4, 1, 1]]
 * 42 is the partitionCount of 1 multipartite numbers: [[10]]
 * 45 is the partitionCount of 1 multipartite numbers: [[7, 1]]
 * 47 is the partitionCount of 1 multipartite numbers: [[5, 2]]
 * 52 is the partitionCount of 2 multipartite numbers: [[3, 2, 1], [1, 1, 1, 1, 1]]
 * 56 is the partitionCount of 1 multipartite numbers: [[11]]
 * 57 is the partitionCount of 1 multipartite numbers: [[4, 3]]
 * 64 is the partitionCount of 1 multipartite numbers: [[5, 1, 1]]
 * 66 is the partitionCount of 1 multipartite numbers: [[2, 2, 2]]
 * 67 is the partitionCount of 1 multipartite numbers: [[8, 1]]
 * 74 is the partitionCount of 1 multipartite numbers: [[3, 1, 1, 1]]
 * 77 is the partitionCount of 2 multipartite numbers: [[12], [6, 2]]
 * 92 is the partitionCount of 1 multipartite numbers: [[2, 2, 1, 1]]
 * 97 is the partitionCount of 2 multipartite numbers: [[9, 1], [5, 3]]
 * 98 is the partitionCount of 1 multipartite numbers: [[4, 2, 1]]
 * 101 is the partitionCount of 1 multipartite numbers: [[13]]
 * 105 is the partitionCount of 1 multipartite numbers: [[6, 1, 1]]
 * 109 is the partitionCount of 2 multipartite numbers: [[4, 4], [3, 3, 1]]
 * 118 is the partitionCount of 1 multipartite numbers: [[7, 2]]
 * 135 is the partitionCount of 2 multipartite numbers: [[14], [2, 1, 1, 1, 1]]
 * 137 is the partitionCount of 1 multipartite numbers: [[3, 2, 2]]
 * 139 is the partitionCount of 1 multipartite numbers: [[10, 1]]
 * 141 is the partitionCount of 1 multipartite numbers: [[4, 1, 1, 1]]
 * 162 is the partitionCount of 1 multipartite numbers: [[6, 3]]
 * 165 is the partitionCount of 1 multipartite numbers: [[7, 1, 1]]
 * 171 is the partitionCount of 1 multipartite numbers: [[5, 2, 1]]
 * 176 is the partitionCount of 1 multipartite numbers: [[15]]
 * 181 is the partitionCount of 1 multipartite numbers: [[8, 2]]
 * 189 is the partitionCount of 1 multipartite numbers: [[5, 4]]
 * 195 is the partitionCount of 1 multipartite numbers: [[11, 1]]
 * 198 is the partitionCount of 1 multipartite numbers: [[3, 2, 1, 1]]
 * 203 is the partitionCount of 1 multipartite numbers: [[1, 1, 1, 1, 1, 1]]
 * 212 is the partitionCount of 1 multipartite numbers: [[4, 3, 1]]
 * 231 is the partitionCount of 1 multipartite numbers: [[16]]
 *
 * So we get three number sequences:
 * S0 = numbers not represented by any count of partitions of multipartite numbers
 *    = 6, 8, 10, 13, 14, 17, 18, 20, 23, 24, 25, 27, 28, 32, 33, 34, 35, 37, 39, 40, 41, 43, 44, 46, 48, 49, 50, 51, 53, 54, 55, 58, 59, 60, 61, 62, 63, 65, 68, 69, 70, 71, 72, 73, 75, 76, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 93, 94, 95, 96, 99, 100, 102, 103, 104, 106, 107, 108, ...
 * This seems to be A330976 = Numbers that are not the number of factorizations into factors > 1 of any positive integer.

 * S1 = numbers represented by exactly 1 count of partitions of multipartite numbers = 1, 3, 4, 9, 12, 16, 19, 21, 22, 26, 29, 31, 36, 38, 42, 45, 47, 56, 57, 64, 66, ...
 * (not in OEIS)
 * 
 * S2 = numbers represented by exactly 2 counts of partitions of multipartite numbers = 2, 5, 7, 11, 15, 30, 52, 77, 97, 109, 135, ...
 * (not in OEIS)
 * 
 * No natural number is represented by more than 2 counts of partitions of multipartite numbers !?
 */
public class MpiPartitionCountCoverageAnalyzer {
	
	private static final Logger LOG = LogManager.getLogger(MpiPartitionCountCoverageAnalyzer.class);
	
	private static final boolean DEBUG = false;
	
	private static void go() {
		Map<Long, TreeSet<Mpi>> partitionCountsToPartitions = new TreeMap<>();
		for (int n=1; ; n++) {
			long nPartitionCount = 0;
			// run over all additive partition of n:
			IntegerPartitionGenerator partgen = new IntegerPartitionGenerator(n);
			while (partgen.hasNext()) {
				nPartitionCount++;
				long start = System.currentTimeMillis();
				int[] flatPartition = partgen.next();
				// partition is in flat form, i.e. a list of all parts, like 5 = 3+1+1.
				// convert this into a multipartite number:
				Mpi mpiFromPartition = new Mpi_IntegerArrayImpl(flatPartition);
				//LOG.debug("mpiFromPartition=" + mpiFromPartition);
				// now count the partitions of the multipartite number
				MpiPartitionGenerator mpiPartGen = new MpiPartitionGenerator(mpiFromPartition);
				long partitionCount = 0;
				while (mpiPartGen.hasNext()) {
					Mpi[] multipartitePartition = mpiPartGen.next();
					if (DEBUG) LOG.debug("multipartite partition = " + Arrays.toString(multipartitePartition));
					partitionCount++;
				}
				if (DEBUG) LOG.debug(mpiFromPartition + " has " + partitionCount + " partitions! (computed in " + (System.currentTimeMillis()-start) + " ms)");
				TreeSet<Mpi> mpis = partitionCountsToPartitions.get(partitionCount);
				if (mpis == null) {
					mpis = new TreeSet<>(Collections.reverseOrder());
				}
				mpis.add(mpiFromPartition);
				partitionCountsToPartitions.put(partitionCount, mpis);
			}
			
			LOG.info("Full statistics after n=" + n + ":");
			for (Map.Entry<Long, TreeSet<Mpi>> entry : partitionCountsToPartitions.entrySet()) {
				long count = entry.getKey().longValue();
				if (count > nPartitionCount) {
					// From here on, the data is incomplete
					break;
				}
				LOG.info(entry.getKey() + " is the partitionCount of " + entry.getValue().size() + " multipartite numbers: " + entry.getValue());
			}
		}
	}

	/**
	 * Test
	 * @param args ignored
	 */
	public static void main(String[] args) {
    	ConfigUtil.initProject();
    	MpiPartitionCountCoverageAnalyzer.go();
    }
}
