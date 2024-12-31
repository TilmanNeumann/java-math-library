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

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * A generator for the additive partitions of multipartite numbers.
 * 
 * This work started from the observation that the number of additive partitions of a multipartite number
 * q = [q1, q2, ..., qk] equals the number of essentially distinct factorizations of
 * a number N with prime factorization N = p1^q1 * p2^q2 * ... * pk^qk.
 * (see e.g. [Hughes, Shallit: "On the number of multiplicative partitions", 1983])
 * 
 * I began with a very slow implementation counting distinct factorizations, and having this as a reference, little by little
 * developed a second implementation working only with manipulations of multipartite numbers and partitions of them.
 * 
 * The latter turned out to be much, much faster. Single-threaded and on a modest computer, it is capable to
 * count several million of partitions per second.
 * 
 * The crucial ingredients to obtain such a speed were the use of a stack, and the PowerMap.
 * Notably, the complements of parts are never computed twice for any (rest, part) pair.
 * 
 * @author Tilman Neumann
 */
public class MpiPartitionGenerator implements Generator<Mpi[]> {

	private static final long serialVersionUID = -1077231419311209122L;

	private static final Logger LOG = LogManager.getLogger(MpiPartitionGenerator.class);
	
	private static final boolean DEBUG = false;

	/** Internal class for stack elements. */
	private static class MpiPartitionStackElem {
		private Mpi rest;
		private Mpi[] partitionPrefix;
		
		private MpiPartitionStackElem(Mpi[] prefix, Mpi rest) {
			this.partitionPrefix = prefix;
			this.rest = rest;
		}
	}

	private MpiPowerMap subvalues;

	// stack behaviour is faster than queue behaviour,
	// and ArrayDeque is slightly faster than LinkedList than Stack (which extends Vector)
	private ArrayDeque<MpiPartitionStackElem> stack = new ArrayDeque<MpiPartitionStackElem>();
	private int maxStackSize = 0;
	
	/**
	 * Complete constructor for a generator of the partitions of the multivariate number q.
	 * @param q
	 */
	public MpiPartitionGenerator(Mpi q) {
		if (DEBUG) {
			int dim = q.getDim(); // dimension of the multipartite numbers
			LOG.debug("q=" + q + ", dim = " + dim);
		}
		subvalues = MpiPowerMap.create(q);
		if (DEBUG) LOG.debug("power map has " + subvalues.size() + " elements!");		
		MpiPartitionStackElem firstStackElem = new MpiPartitionStackElem(new Mpi[0], q);
		stack.push(firstStackElem);
	}

	/**
	 * @return true if there is another partition
	 */
	public boolean hasNext() {
		return (!stack.isEmpty());
	}
	
	/**
	 * Compute the next partition of the multipartite input.
	 * The result is a flat array of parts because this is much faster than creating multisets.
	 * 
	 * @return next partition
	 */
	public Mpi[] next() {
		if (stack.size()>maxStackSize) {
			maxStackSize = stack.size();
		}
		MpiPartitionStackElem stackElem = stack.pop();
		if (DEBUG) LOG.debug("POP prefix=" + Arrays.asList(stackElem.partitionPrefix) + ", rest=" + stackElem.rest);
		
		// rest will be the biggest of all parts when the recursion ends
		Mpi rest = stackElem.rest;
		
		// prefix is a list of the other parts sorted biggest first
		Mpi[] prefix = stackElem.partitionPrefix;
		int prefixSize = prefix.length;
		
		// determine max next part
		Mpi maxNextPart = (prefixSize>0) ? rest.maxNextPart(prefix[0], prefix[prefixSize-1]) : rest.div2()[0];
		
		// create next parts
		if (maxNextPart!=null && maxNextPart.getCardinality()>0 && rest.getCardinality()>1) {
			if (DEBUG) LOG.debug("create nextPartsAndComplements of " + rest);
			Map<Mpi, Mpi> nextPartsAndComplements = subvalues.getSubvaluesLessOrEqual(rest, maxNextPart);
			if (DEBUG) LOG.debug("nextPartsAndComplements= " + nextPartsAndComplements);
			for (Map.Entry<Mpi, Mpi> partAndComplement : nextPartsAndComplements.entrySet()) {
				Mpi[] newPrefix = new Mpi[prefixSize+1];
				System.arraycopy(prefix, 0, newPrefix, 0, prefixSize);
				newPrefix[prefixSize] = partAndComplement.getKey(); // next part
				// new rest is (rest-next part), the complement of next part
				if (DEBUG) LOG.debug("PUSH rest=" + rest + ", part=" + partAndComplement.getKey() + " -> newRest=" + partAndComplement.getValue() + ", newPrefix=" + Arrays.asList(newPrefix));
				stack.push(new MpiPartitionStackElem(newPrefix, partAndComplement.getValue()));
			}
		}
		
		// prepare result: array is much, much faster than Multiset !
		Mpi[] result = new Mpi[prefixSize+1];
		result[0] = rest; // biggest part
		System.arraycopy(prefix, 0, result, 1, prefixSize);
		if (DEBUG) LOG.debug("RETURN " + Arrays.toString(result));
		return result;
	}

	/**
	 * Computes the partitions of the given multipartite number.
	 * 
	 * @param q the multipartite number [q_1, q_2, ...]
	 * @return partitions = SortedSet<MpiPartition>,
	 * partition = MpiPartition,
	 * part = Mpi
	 */
	public static SortedSet<MpiPartition> partitionsOf(Mpi q) {
		SortedSet<MpiPartition> partitions = new TreeSet<MpiPartition>(Collections.reverseOrder());
		MpiPartitionGenerator partGen = new MpiPartitionGenerator(q);
		while (partGen.hasNext()) {
			Mpi[] flatPartition = partGen.next();
			MpiPartition expPartition = new MpiPartition(flatPartition);
			partitions.add(expPartition);
		}
		if (DEBUG) {
			LOG.debug(partGen.subvalues.accessStats());
	    	LOG.debug("maxStackSize = " + partGen.maxStackSize);
		}
		return partitions;
	}
	
	/**
	 * Counts the number of partitions of the given multipartite integer.
	 * @param q
	 * @return number of partitions of q
	 */
	public static long numberOfPartitionsOf(Mpi q) {
		MpiPartitionGenerator partGen = new MpiPartitionGenerator(q);
		long count = 0; // int range ~2.1*10^9 may be too small
		while (partGen.hasNext()) {
			partGen.next();
			count++;
		}
		if (DEBUG) {
			LOG.debug(partGen.subvalues.accessStats());
			LOG.debug("maxStackSize = " + partGen.maxStackSize);
		}
		return count;
	}
	
	/**
	 * Computes the number of essentially different prime factorizations of n.
	 * @param n
	 * @return number of essentially different prime factorizations of n
	 */
	public static long numberOfFactorizationsOf(BigInteger n) {
		PrimePowers mpiFromFactors = PrimePowers_DefaultImpl.valueOf(n);
		return numberOfPartitionsOf(mpiFromFactors);
	}
}
