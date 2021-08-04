package de.tilman_neumann.jml.factor.base.congruence;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.log4j.Logger;

/**
 * Cycle counting algorithm implementation for two large primes, following [LM94].
 * The algorithm is exact for partials with 2 large primes.
 * 
 * @see [LM94] Lenstra, Manasse 1994: "Factoring With Two Large Primes", Mathematics of Computation, volume 63, number 208, page 789.
 * 
 * @author Tilman Neumann
 */
public class CycleCounter2LP implements CycleCounter {
	
	private static final Logger LOG = Logger.getLogger(CycleCounter2LP.class);
	private static final boolean DEBUG = false; // used for logs and asserts
	
	// cycle counting
	private HashSet<Partial> relations; // all distinct relations
	private HashMap<Long, Long> edges; // contains edges: bigger to smaller prime; size is v = #vertices
	private HashSet<Long> roots; // roots of disjoint components (only used for debugging)
	private int componentCount; // number of disjoint components
	// the number of smooths from partials found
	private int cycleCount;
	
	/**
	 * Full constructor.
	 */
	public CycleCounter2LP() {
		relations = new HashSet<>();
		edges = new HashMap<>(); // bigger to smaller prime
		if (DEBUG) roots = new HashSet<>();
		componentCount = 0;
		cycleCount = 0;
	}
	
	@Override
	public int addPartial(Partial partial, int correctSmoothCount, HashSet<Partial> relatedPartials) {
		
		boolean added = relations.add(partial);
		if (!added) {
			// The partial is a duplicate of another relation we already have
			LOG.error("Found duplicate relation: " + partial);
			return cycleCount;
		}
		
		// We compute the following two variable once again,
		// but that doesn't matter 'cause it's no production code
		Long[] largeFactors = partial.getLargeFactorsWithOddExponent();
		int largeFactorsCount = largeFactors.length;
		String partialStr = largeFactorsCount + "LP-partial " + Arrays.toString(largeFactors);
		if (DEBUG) LOG.debug("Add " + partialStr);
		
		// add edges
		if (largeFactorsCount==1) {
			if (DEBUG) assertTrue(largeFactors[0] > 1);
			insertEdge(1, largeFactors[0]);
		} else if (largeFactorsCount==2) {
			if (DEBUG) assertTrue(largeFactors[0] != largeFactors[1]);
			insertEdge(largeFactors[0], largeFactors[1]);
		} else {
			LOG.warn("Holy shit, we found a " + largeFactorsCount + "-partial!");
		}
		
		// update cycle count by standard formula: #cycles = #edges (one per relation) + #components - #vertices
		int vertexCount = edges.size();
		cycleCount = relations.size() + componentCount - vertexCount;

		if (DEBUG) {
			LOG.debug("correctSmoothCount = " + correctSmoothCount);
			String cycleCountFormula = "#relations + #roots - #vertices";
			LOG.debug("#relations=" + relations.size() + ", #roots=" + componentCount + ", #vertices=" + edges.size() + " -> cycleCount = " + cycleCountFormula + " = " + cycleCount);

			assertEquals(roots.size(), componentCount);
			//assertEquals(getRootsFromVertices().size(), componentCount); // expensive test
			LOG.debug("-------------------------------------------------------------");
		}
		
		return cycleCount;
	}

	/** p1 = smaller p, p2 = larger p */
	private void insertEdge(long p1, long p2) {
		Long r1 = getRoot(p1);
		Long r2 = getRoot(p2);

		if (r1!=null && r2!=null) {
			// both vertices already exist.
			// if the roots are different, then we have distinct components which we can join now
			if (r1<r2) {
				if (DEBUG) LOG.debug("2LP: 2 old vertices from distinct components");
				edges.put(r2, r1);
				if (DEBUG) roots.remove(r2);
				componentCount--;
			} else if (r2<r1) {
				if (DEBUG) LOG.debug("2LP: 2 old vertices from distinct components");
				edges.put(r1, r2);
				if (DEBUG) roots.remove(r1);
				componentCount--;
			} else {
				// if the roots are equal than both primes are already part of the same component so nothing more happens
				if (DEBUG) LOG.debug("2LP: 2 old vertices from the same components");
			}
		} else if (r1 != null) {
			// p1 already exists, p2 is new -> we just add p2 to the component of p1
			if (DEBUG) LOG.debug("2LP: 1 old vertex, 1 new vertex");
			edges.put(p2, r1);
		} else if (r2 != null) {
			// p2 already exists, p1 is new -> we just add p1 to the component of p2
			if (DEBUG) LOG.debug("2LP: 1 old vertex, 1 new vertex");
			edges.put(p1, r2);
		} else {
			// both primes are new and form their own new disconnected component
			// we know p1 < p2
			if (DEBUG) LOG.debug("2LP: 2 new vertices");
			edges.put(p1, p1);
			edges.put(p2, p1);
			if (DEBUG) roots.add(p1);
			componentCount++;
		}
	}

	/**
	 * Find the root of a prime p in the edges graph.
	 * @param p
	 * @return root of p (may be 1 or p itself)
	 */
	private Long getRoot(Long p) {
		Long q = edges.get(p);
		while (q != p) { // includes null test
			p = q;
			q = edges.get(p);
		}
		return p;
	}
	
	@SuppressWarnings("unused")
	private HashSet<Long> getRootsFromVertices() {
		HashSet<Long> roots = new HashSet<>();
		for (long vertex : edges.keySet()) {
			long r = getRoot(vertex);
			roots.add(r);
		}
		return roots;
	}

	@Override
	public HashSet<Partial> getPartialRelations() {
		return relations;
	}
	
	@Override
	public int getPartialRelationsCount() {
		return relations.size();
	}
	
	@Override
	public int getCycleCount() {
		return cycleCount;
	}
}
