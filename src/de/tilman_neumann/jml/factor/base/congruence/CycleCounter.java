package de.tilman_neumann.jml.factor.base.congruence;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

/**
 * Counts the number of independent cycles in the partial relations.
 * So far it seems to work for 1- and 2-partials.
 * 
 * @see [Lenstra, Manasse 1994: "Factoring With Two Large Primes", Mathematics of Computation, volume 63, number 208, page 789]
 * @see [Leyland, Lenstra, Dodson, Muffett, Wagstaff 2002: "MPQS with three large primes", Lecture Notes in Computer Science, 2369]
 * 
 * @author Tilman Neumann
 */
// TODO implement cycle-finding following [Lenstra, Manasse] and use that to verify that cycle-counting works for 2-partials
// TODO fix cycle-counting for 3-partials
public class CycleCounter {
	private static final Logger LOG = Logger.getLogger(CycleCounter.class);
	private static final boolean DEBUG = false; // used for logs and asserts

	// cycle counting
	private int maxLargeFactors;
	private HashMap<Long, Long> vertexMap; // contains edges: bigger to smaller prime; size is v = #vertices
	private HashSet<Long> roots; // roots of disjoint compounds
	private HashSet<Partial> relations; // all distinct relations
	private int edgeCount;

	/**
	 * Full constructor.
	 * @param maxLargeFactors the maximum number of large primes in partials:  1, 2 or 3
	 */
	public CycleCounter(int maxLargeFactors) {
		this.maxLargeFactors = maxLargeFactors;
		vertexMap = new HashMap<>(); // bigger to smaller prime
		roots = new HashSet<>();
		relations = new HashSet<>();
		edgeCount = 0;
	}
	
	/**
	 * Counts the number of independent cycles in the partial relations.
	 * @param partial the newest partial relation to add
	 */
	public void countIndependentCycles(Partial partial) {
		boolean added = relations.add(partial);
		if (!added) {
			// The partial is a duplicate of another relation we already have
			LOG.error("Found duplicate relation!" + partial);
			return;
		}
		
		// We compute the following two variable once again,
		// but that doesn't matter 'cause it's no production code
		Long[] oddExpBigFactors = partial.getLargeFactorsWithOddExponent();
		int oddExpBigFactorsCount = oddExpBigFactors.length;

		// pad array with 1's to the length maxLargeFactors
		Long[] largeFactors = new Long[maxLargeFactors];
		int oneCount = maxLargeFactors-oddExpBigFactorsCount;
		for (int i=0; i<oneCount; i++) {
			largeFactors[i] = 1L;
		}
		for (int i=0; i<oddExpBigFactorsCount; i++) {
			largeFactors[i+oneCount] = oddExpBigFactors[i];			
		}
		if (DEBUG) LOG.debug("Add largeFactors = " + Arrays.toString(oddExpBigFactors) + " = " + Arrays.toString(largeFactors));

		// add vertices
		for (int i=0; i<maxLargeFactors; i++) {
			long largeFactor = largeFactors[i];
			if (vertexMap.get(largeFactor) == null) {
				// new vertex creates new compound
				vertexMap.put(largeFactor, largeFactor); // v = v + 1
				roots.add(largeFactor); // c = c + 1
			}
		}
		if (DEBUG) LOG.debug("after adding vertices: vertexMap = " + vertexMap + ", roots = " + roots);
		
		// add edges
		for (int i=0; i<maxLargeFactors; i++) {
			long f1 = largeFactors[i];
			for (int j=i+1; j<maxLargeFactors; j++) {
				long f2 = largeFactors[j];
				if (f1 != f2) {
					// find roots
					long r1 = getRoot(f1);
					long r2 = getRoot(f2);
					if (DEBUG) LOG.debug("i="+i+", j=" + j + ": f1=" + f1 + ", f2=" + f2 + ", r1=" + r1 + ", r2=" + r2);
					// insert edge: the smaller root is made the parent of the larger root, and the larger root is no root anymore
					if (r1 < r2) {
						vertexMap.put(r2, r1);
						roots.remove(r2);
					} else if (r1 > r2) {
						vertexMap.put(r1, r2);
						roots.remove(r1);
					} // else: r1 and r2 are in the same compound -> a cycle has been found
					
					// To speed up the process we could also set the "parents" of all vertexMap nodes passed in root finding to the new root
				}
				edgeCount++;
			}
		}
		
		if (DEBUG) {
			if (maxLargeFactors==2) assertEquals(relations.size(), edgeCount);
			if (maxLargeFactors==3) assertEquals(3*relations.size(), edgeCount);
			// general case is maxLargeFactors*(maxLargeFactors-1)/2 ?
			
			LOG.debug(edgeCount + " edges");
			LOG.debug(roots.size() + " roots = " + roots);
			LOG.debug(vertexMap.size() + " vertices = " + vertexMap);
			LOG.debug(relations.size() + " relations");
		}
	}
	
	/**
	 * Find the root of a prime p in the edges graph, permitting 1 as a root.
	 * @param p
	 * @return root of p (may be p itself)
	 */
	// XXX allowing for 1 to be a root sounds wrong but yields just the number of smooths my algorithm derives from partials.
	@SuppressWarnings("unused")
	private Long getRoot_v1(long p) {
		long q;
		while ((q = vertexMap.get(p)) != p) {
			p = q;
		}
		return p;
	}

	/**
	 * Find the root of a prime p in the edges graph, not permitting 1 as a root.
	 * @param p
	 * @return root of p (may be p itself)
	 */
	// XXX Gives a somewhat bigger count than the number of smooths we actually derive from partials.
	// So if root counting with this algorithm is correct, my current root-finding is sub-optimal.
	private Long getRoot(long p) {
		long q;
		while ((q = vertexMap.get(p)) != p && q!=1) {
			p = q;
		}
		return p;
	}

	public String getCycleCountResult() {
		int cycleCount; {
			switch (maxLargeFactors) {
			case 1:
			case 2:
				cycleCount = edgeCount + roots.size() - vertexMap.size();
				return "maxLargeFactors=" + maxLargeFactors + ": #independent cycles = " + cycleCount + " (" + edgeCount + " edges + " + roots.size() + " compounds - " + vertexMap.size() + " vertices)";
			case 3:
				// The "thought to be" formula from [Leyland, Lenstra, Dodson, Muffett, Wagstaff: "MPQS with three large primes", Lecture Notes in Computer Science, 2369, p.7]
				cycleCount = edgeCount + roots.size() - vertexMap.size() - 2*relations.size();
				return "maxLargeFactors=" + maxLargeFactors + ": #independent cycles = " + cycleCount + " (" + edgeCount + " edges + " + roots.size() + " compounds - " + vertexMap.size() + " vertices - 2*" + relations.size() + " relations)";
			default:
				throw new IllegalStateException("cycle counting is not implemented yet for maxLargeFactors>3, but maxLargeFactors = " + maxLargeFactors);
			}
		}
	}
}
