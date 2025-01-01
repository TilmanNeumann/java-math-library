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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.SortedSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * MPI partition generator runner.
 * @author Tilman Neumann
 */
public class MpiPartitionGeneratorRunner {

	private static final Logger LOG = LogManager.getLogger(MpiPartitionGeneratorRunner.class);
	
	private static final boolean DEBUG = false;

	/**
	 * Test
	 * @param args ignored
	 */
	public static void main(String[] args) {
    	ConfigUtil.initProject();
    	
		while(true) {
			String input;
			try {
				LOG.info("\nPlease insert comma-separated parts of multipartite number:");
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				String line = in.readLine();
				input = line.trim();
				LOG.debug("multipartite number input = [" + input + "]");
			} catch (IOException ioe) {
				LOG.error("io-error occurring on input: " + ioe.getMessage());
				continue;
			}
			try {
				Mpi q = new Mpi_IntegerArrayImpl(input);
				long start = System.currentTimeMillis();
				long count = MpiPartitionGenerator.numberOfPartitionsOf(q);
				LOG.info(q + " has " + count + " partitions (computed in " + (System.currentTimeMillis()-start) + "ms)");
		    	if (DEBUG) {
		    		SortedSet<MpiPartition> partitions = MpiPartitionGenerator.partitionsOf(q);
		    		LOG.debug(q + " has " + partitions.size() + " partitions: " + partitions);
		    	}
			} catch (NumberFormatException nfe) {
				LOG.error("input " + input + " is not a multipartite integer");
			}
		} // next input...
    }
}