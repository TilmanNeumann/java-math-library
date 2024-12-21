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

import org.apache.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Tests which natural numbers are represented by counts of partitions of multipartite numbers. (experimental)
 */
public class MpiPartitionCountCoverageChecker {
	
	private static final Logger LOG = Logger.getLogger(MpiPartitionCountCoverageChecker.class);
	
	private static final boolean DEBUG = false;
	
	private static void go() {
		Map<Integer, TreeSet<Mpi>> partitionCountsToPartitions = new TreeMap<>();
		for (int n=1; ; n++) {
			int nPartitionCount = 0;
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
				int partitionCount = 0;
				while (mpiPartGen.hasNext()) {
					Mpi[] multipartitePartition = mpiPartGen.next();
					if (DEBUG) LOG.debug("multipartite partition = " + Arrays.toString(multipartitePartition));
					partitionCount++;
				}
				if (DEBUG) LOG.info(mpiFromPartition + " has " + partitionCount + " partitions! (computed in " + (System.currentTimeMillis()-start) + " ms)");
				TreeSet<Mpi> mpis = partitionCountsToPartitions.get(partitionCount);
				if (mpis == null) {
					mpis = new TreeSet<>(Collections.reverseOrder());
				}
				mpis.add(mpiFromPartition);
				partitionCountsToPartitions.put(partitionCount, mpis);
			}
			
			LOG.debug("Full statistics after n=" + n + ":");
			for (Map.Entry<Integer, TreeSet<Mpi>> entry : partitionCountsToPartitions.entrySet()) {
				int count = entry.getKey().intValue();
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
    	MpiPartitionCountCoverageChecker.go();
    }
}
