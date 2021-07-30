package de.tilman_neumann.jml.factor.base.congruence;

import static org.junit.Assert.*;
import static de.tilman_neumann.jml.factor.base.GlobalFactoringOptions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;

import de.tilman_neumann.util.SortedMultiset;
import de.tilman_neumann.util.SortedMultiset_BottomUp;

/**
 * Algorithms to count and find independent cycles in partial relations containing partials with 2 or 3 large primes.
 * 
 * @see [LM94] Lenstra, Manasse 1994: "Factoring With Two Large Primes", Mathematics of Computation, volume 63, number 208, page 789.
 * @see [LLDMW02] Leyland, Lenstra, Dodson, Muffett, Wagstaff 2002: "MPQS with three large primes", Lecture Notes in Computer Science, 2369.
 * 
 * @author Tilman Neumann
 */
// TODO fix cycle counting for 3LP: There seems to be some degree of freedom where to add some things -> I want to see and count the edges !
// TODO implement cycle-finding following [LM94] for 2LP ?
public class CycleFinder {
	private static final Logger LOG = Logger.getLogger(CycleFinder.class);
	private static final boolean DEBUG = false; // used for logs and asserts

	// cycle counting
	private int maxLargeFactors;
	private HashMap<Long, Long> edges; // contains edges: bigger to smaller prime; size is v = #vertices
	private HashSet<Long> roots; // roots of disjoint components
	private HashSet<Partial> relations; // all distinct relations
	private int edgeCount;
	// the number of smooths from partials found
	private int cycleCount;
	
	/**
	 * Full constructor.
	 * @param maxLargeFactors the maximum number of large primes in partials:  2 or 3
	 */
	public CycleFinder(int maxLargeFactors) {
		this.maxLargeFactors = maxLargeFactors;
		edges = new HashMap<>(); // bigger to smaller prime
		roots = new HashSet<>();
		relations = new HashSet<>();
		edgeCount = 0;
		cycleCount = 0;
	}
	
	/**
	 * Counts the number of independent cycles in the partial relations following [LM94], [LLDMW02].
	 * Works for 2LP so far, but not for 3LP yet.
	 * 
	 * @param partial the newest partial relation to add
	 * @param correctSmoothCount the correct number of smooths from partials (only for debugging)
	 * @return the updated number of smooths from partials
	 */
	public int addPartial(Partial partial, int correctSmoothCount) {
		
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
		if (DEBUG_3LP_CYCLE_COUNTING) LOG.debug("Add " + largeFactorsCount + "-LP with large factors " + Arrays.toString(largeFactors));
		
		// add vertices and find their roots
		edges.put(1L, 1L); // v = 1
		roots.add(1L); // c = 1
		boolean[] isNewVertex = new boolean[largeFactorsCount];
		long[] vertexRoots = new long[largeFactorsCount];
		for (int i=0; i<largeFactorsCount; i++) {
			long largeFactor = largeFactors[i];
			if (DEBUG) assertTrue(largeFactor > 1);
			isNewVertex[i] = (edges.get(largeFactor) == null);
			if (DEBUG_3LP_CYCLE_COUNTING) LOG.debug(largeFactor + " is new vertex? " + isNewVertex[i]);
			if (isNewVertex[i]) {
				// new vertex creates new component
				edges.put(largeFactor, largeFactor); // v = v + 1
				roots.add(largeFactor); // c = c + 1
				vertexRoots[i] = largeFactor;
			} else {
				vertexRoots[i] = getRoot(largeFactor);
			}
		}
		//LOG.debug("after adding vertices: edges = " + edges + ", roots = " + roots);

		// add edges
		if (largeFactorsCount==1) {
			if (DEBUG) assertTrue(largeFactors[0] > 1);
			insertEdge(1, vertexRoots[0]);
		} else if (largeFactorsCount==2) {
			if (DEBUG) assertTrue(largeFactors[0] != largeFactors[1]);
			insertEdge(vertexRoots[0], vertexRoots[1]);
		} else if (largeFactorsCount==3) {
			if (DEBUG) {
				assertTrue(largeFactors[0] != largeFactors[1]);
				assertTrue(largeFactors[0] != largeFactors[2]);
				assertTrue(largeFactors[1] != largeFactors[2]);
			}
			insertEdges3(vertexRoots[0], vertexRoots[1], vertexRoots[2]);
		} else {
			LOG.warn("Holy shit, we found a " + largeFactorsCount + "-partial!");
		}
		
		// update edge count and cycle count
		int vertexCount = edges.size();
		if (maxLargeFactors==2) {
			edgeCount++;
			// standard formula: #cycles = #edges + #components - #vertices
			cycleCount = edgeCount + roots.size() - vertexCount;
		} else if (maxLargeFactors==3) {
			if (largeFactorsCount==3) {
				edgeCount += 3;
			} else if (largeFactorsCount==2) {
				if (!isNewVertex[0] && !isNewVertex[1] && (vertexRoots[0]==vertexRoots[1]) ) {
					// both vertices were already known and in the same component
					edgeCount += 2; // XXX do +1 only ?
				} else {
					edgeCount += 2;
				}
			} else {
				edgeCount += 2; // XXX Why do we count 2 edges for partials having only one big factor?
			}
			// The "thought to be" formula from [LLDMW02], p.7 // XXX modified
			cycleCount = edgeCount + roots.size() - vertexCount - /*2**/relations.size(); // XXX modified
		}

		if (DEBUG_3LP_CYCLE_COUNTING) {
			LOG.debug("edgeCount = " + edgeCount);
			int rootCount = roots.size();
			String rootsStr = rootCount<100 ? rootCount + " roots = " + roots : rootCount + " roots";
			LOG.debug(rootsStr);
			String vertexStr = vertexCount<100 ? vertexCount + " vertices = " + edges : vertexCount + " vertices";
			LOG.debug(vertexStr);
			LOG.debug(relations.size() + " relations");

			LOG.debug("correctSmoothCount = " + correctSmoothCount);
			String cycleCountFormula = "#edges + #roots - #vertices - #relations"; // XXX was "- 2*#relations"
			LOG.debug("#edges=" + edgeCount  + ", #roots=" + roots.size()  + ", #vertices=" + edges.size() + ", #relations=" + relations.size()   + " ->  cycleCount = " + cycleCountFormula + " = " + cycleCount);

			if (cycleCount != correctSmoothCount) {
				LOG.debug("ERROR: " + partial.getNumberOfLargeQFactors() + "-partial " + partial + " led to incorrect cycle count update!");
				System.exit(0);
			}
			
			LOG.debug("-------------------------------------------------------------");
		}
		
		return cycleCount;
	}
	
	private void insertEdge(long r1, long r2) {
		if (DEBUG_3LP_CYCLE_COUNTING) LOG.debug("r1=" + r1 + ", r2=" + r2);

		// insert edge: the smaller root is made the parent of the larger root, and the larger root is no root anymore
		if (r1 < r2) {
			edges.put(r2, r1);
			roots.remove(r2);
		} else if (r1 > r2) {
			edges.put(r1, r2);
			roots.remove(r1);
		} else {
			// else: r1 and r2 are in the same component -> a cycle has been found
			if (DEBUG_3LP_CYCLE_COUNTING) {
				LOG.debug("roots are equal! r1 = " + r1 + ", r2 = " + r2);
				LOG.debug("-> " + r1 + " is new root? " + !roots.contains(r1));
			}
		}
		
		// For a speedup, we could also set the "parents" of (all edges passed in root finding) to the new root
	}
	
	private void insertEdges3(long r1, long r2, long r3) {
		if (DEBUG_3LP_CYCLE_COUNTING) LOG.debug("r1=" + r1 + ", r2=" + r2 + ", r3=" + r3);
	
		// insert edge: the smallest root is made the parent of the other two, the larger root are dropped
		if (r1 < r2) {
			if (r1 < r3) { // r1 is the smallest root
				edges.put(r2, r1);
				edges.put(r3, r1);
				roots.remove(r2);
				roots.remove(r3);
			} else { // r3 is the smallest root
				edges.put(r1, r3);
				edges.put(r2, r3);
				roots.remove(r1);
				roots.remove(r2);
			}
		} else {
			if (r2 < r3) { // r2 is the smallest root
				edges.put(r1, r2);
				edges.put(r3, r2);
				roots.remove(r1);
				roots.remove(r3);
			} else { // r3 is the smallest root
				edges.put(r1, r3);
				edges.put(r2, r3);
				roots.remove(r1);
				roots.remove(r2);
			}
		}
		// XXX do we not have to consider cases where some or all roots are equal?
	}

	/**
	 * Find the root of a prime p in the edges graph.
	 * @param p
	 * @return root of p (may be 1 or p itself)
	 */
	private Long getRoot(long p) {
		long q;
		while ((q = edges.get(p)) != p) p = q;
		return p;
	}
	
	private void testRoots() {
		HashSet<Long> roots2 = new HashSet<>();
		for (Long vertex : edges.keySet()) {
			roots2.add(getRoot(vertex));
		}
		if (roots.equals(roots2)) {
			LOG.debug("All roots confirmed!");
		} else {
			LOG.debug("#roots computed originally = " + roots.size());
			LOG.debug("#roots verified = " + roots2.size());
		}
	}
	
	/**
	 * Finds independent cycles and uses them to combine partial to smooth relations, following [LLDMW02].
	 * Works for up to 3 large primes. (3LP tested with CFrac)
	 * 
	 * @return smooth relations found by combining partial relations
	 */
	public ArrayList<Smooth> findIndependentCycles() {
		// Create maps from large primes to partials, vice versa, and chains.
		// These are needed so we can remove elements without changing the relations itself.
		HashMap<Long, ArrayList<Partial>> rbp = new HashMap<>();
		HashMap<Partial, ArrayList<Long>> pbr = new HashMap<>();
		HashMap<Partial, ArrayList<Partial>> chains = new HashMap<>();
		for (Partial newPartial : relations) {
			Long[] oddExpBigFactors = newPartial.getLargeFactorsWithOddExponent();
			ArrayList<Long> factorsList = new ArrayList<>(); // copy needed
			for (Long oddExpBigFactor : oddExpBigFactors) {
				factorsList.add(oddExpBigFactor);				
				ArrayList<Partial> partialCongruenceList = rbp.get(oddExpBigFactor);
				// For large N, most large factors appear only once. Therefore we create an ArrayList with initialCapacity=1 to safe memory.
				// Even less memory would be needed if we had a HashMap<Long, Object>
				// and store AQPairs or AQPair[] in the Object part. But I do not want to break the generics...
				if (partialCongruenceList==null) partialCongruenceList = new ArrayList<Partial>(1);
				partialCongruenceList.add(newPartial);
				rbp.put(oddExpBigFactor, partialCongruenceList);
			}
			pbr.put(newPartial, factorsList);
			chains.put(newPartial, new ArrayList<>());
		}
		
		// result
		ArrayList<Smooth> smoothsFromPartials = new ArrayList<>();
		
		boolean tablesChanged;
		do {
			tablesChanged = false;
			Iterator<Partial> r0Iter = pbr.keySet().iterator();
			while (r0Iter.hasNext()) {
				Partial r0 = r0Iter.next();
				ArrayList<Long> r0Factors = pbr.get(r0);
				if (r0Factors.size() != 1) continue;
				
				Long p = r0Factors.get(0);
				ArrayList<Partial> riList = rbp.get(p);
				for (Partial ri : riList) {
					if (r0.equals(ri)) continue;
					
					ArrayList<Long> riFactors = pbr.get(ri);
					if (DEBUG) assertNotNull(riFactors);
					if (riFactors.size() == 1) {
						// found cycle -> create new Smooth consisting of r0, ri and their chains
						if (DEBUG) {
							SortedMultiset<Long> combinedLargeFactors = new SortedMultiset_BottomUp<Long>();
							combinedLargeFactors.addAll(r0.getLargeFactorsWithOddExponent());
							for (Partial partial : chains.get(r0)) combinedLargeFactors.addAll(partial.getLargeFactorsWithOddExponent());
							combinedLargeFactors.addAll(ri.getLargeFactorsWithOddExponent());
							for (Partial partial : chains.get(ri)) combinedLargeFactors.addAll(partial.getLargeFactorsWithOddExponent());
							// test combinedLargeFactors
							for (Long factor : combinedLargeFactors.keySet()) {
								assertTrue((combinedLargeFactors.get(factor) & 1) == 0);
							}
						}
						HashSet<Partial> allPartials = new HashSet<>();
						allPartials.add(r0);
						allPartials.addAll(chains.get(r0));
						allPartials.add(ri);
						allPartials.addAll(chains.get(ri));
						Smooth smooth = new Smooth_Composite(allPartials);
						smoothsFromPartials.add(smooth);
						continue;
					}
					
					// otherwise add r0 and its chain to the chain of ri
					ArrayList<Partial> riChain = chains.get(ri);
					riChain.add(r0);
					riChain.addAll(chains.get(r0));
					// delete p from the prime list of ri
					riFactors.remove(p);
				} // end for ri

				// "the entry keyed by r0 is deleted from pbr";
				// this requires Iterator.remove() so we can continue working with the outer collection
				r0Iter.remove(); // except avoiding ConcurrentModificationExceptions the same as pbr.remove(r0);
				
				// "the entry for r0 keyed by p is deleted from rbp";
				// This choice promised finding more smooths, but unfortunately it was wrong, delivered combinations with odd exponents
				// riList.remove(r0);
				// The following works
				ArrayList<Partial> partials = rbp.get(p);
				for (Partial partial : partials) {
					ArrayList<Long> pList = pbr.get(partial);
					if (pList != null) pList.remove(p);
				}
				
				tablesChanged = true;
			} // end while r0
		} while (tablesChanged);
		
		if (DEBUG) LOG.debug("Found " + smoothsFromPartials.size() + " smooths from partials");
		return smoothsFromPartials;
	}
	
	/**
	 * @return number of partial congruences found so far.
	 */
	public int getPartialCongruenceCount() {
		return relations.size();
	}
}
