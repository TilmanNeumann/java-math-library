package de.tilman_neumann.jml.factor.base.congruence;

import static org.junit.Assert.*;
import static de.tilman_neumann.jml.factor.base.GlobalFactoringOptions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Cycle counting algorithm implementation following [LLDMW02], as far as possible.
 * The algorithm is exact for partials with 2 large primes.
 * With three large primes, unlike in [LLDMW02] we can only obtain an upper bound estimate of the cycle count.
 * So if the counting algorithm predicts a possible new smooth we need to run some PartialSolver to verify it.
 * 
 * @see [LLDMW02] Leyland, Lenstra, Dodson, Muffett, Wagstaff 2002: "MPQS with three large primes", Lecture Notes in Computer Science, 2369.
 * @see [LM94] Lenstra, Manasse 1994: "Factoring With Two Large Primes", Mathematics of Computation, volume 63, number 208, page 789.
 * 
 * @author Tilman Neumann
 */
public class CycleCounter3LP3 implements CycleCounter {

	private static final Logger LOG = Logger.getLogger(CycleCounter3LP3.class);
	private static final boolean DEBUG = false; // used for logs and asserts
	
	// cycle counting
	private HashMap<Long, Long> edges; // contains edges: bigger to smaller prime; size is v = #vertices
	private HashSet<Long> roots; // roots of disjoint components
	private HashSet<Partial> relations; // all distinct relations
	private int edgeCount;
	// the number of smooths from partials found
	private int cycleCount;
	private int lastCorrectSmoothCount;
	private int extraRelations;
	
	/**
	 * Full constructor.
	 * @param maxLargeFactors the maximum number of large primes in partials:  2 or 3
	 */
	public CycleCounter3LP3() {
		edges = new HashMap<>(); // bigger to smaller prime
		roots = new HashSet<>();
		relations = new HashSet<>();
		edgeCount = 0;
		cycleCount = 0;
		lastCorrectSmoothCount = 0;
		extraRelations  = 0;
	}
	
	@Override
	public int addPartial(Partial partial, int correctSmoothCount, HashSet<Partial> relatedPartials) {
		int correctSmoothCountIncr = correctSmoothCount - lastCorrectSmoothCount;
		lastCorrectSmoothCount = correctSmoothCount;
		int lastCycleCount = cycleCount;
		
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
		if (DEBUG_3LP_CYCLE_COUNTING) LOG.debug("Add " + partialStr);
		
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

		LOG.debug("vertexRoots = " + Arrays.toString(vertexRoots));
		
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
		if (largeFactorsCount==3) {
			if (vertexRoots[0]==vertexRoots[1] && vertexRoots[0]==vertexRoots[2] && vertexRoots[1]==vertexRoots[2]) {
				edgeCount += 2;
			} else if (vertexRoots[0]==vertexRoots[1] || vertexRoots[0]==vertexRoots[2] || vertexRoots[1]==vertexRoots[2]){
				edgeCount += 2;
			} else {
				edgeCount += 2;
			}
		} else if (largeFactorsCount==2) {
			if (!isNewVertex[0] && !isNewVertex[1] && (vertexRoots[0]==vertexRoots[1]) ) {
				// both vertices were already known and in the same component
				edgeCount += 1;
			} else {
				edgeCount += 1;
			}
		} else {
			edgeCount += 1;
		}
		cycleCount = edgeCount + roots.size() - vertexCount;

		if (DEBUG_3LP_CYCLE_COUNTING) {
			LOG.debug("#edges = " + edgeCount);
			LOG.debug("#roots = " + roots.size());
			LOG.debug("#rootsFromVertices = " + getRootsFromVertices().size());
			LOG.debug("#vertices = " + vertexCount);
			LOG.debug("#relations = "+ relations.size());
			LOG.debug("");
			LOG.debug("#edges - #relations = " + (edgeCount-relations.size()));
			LOG.debug("");
			LOG.debug("correctSmoothCount = " + correctSmoothCount);

			String cycleCountFormula = "#edges + #roots - #vertices"; // TODO
			LOG.debug("#cycles = " + cycleCountFormula + " = " + cycleCount);
			// 150 bit: 287 instead of 274 cycles, 13 errors (all up)
			// all remaining errors come from adding 1LP- or 2LP-partials that are related to at least one 3LP-partial (and after removing singletons there is no partial left)

			int cycleCount4 = cycleCount - (edgeCount - relations.size())/2;
			LOG.debug("cycleCount4 = " + cycleCount4);
			
			// best choice so far >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
			int cycleCount6 = cycleCount + relations.size() - edgeCount;
			LOG.debug("cycleCount6 = " + cycleCount6);
			// <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

			int cycleCount7 = relations.size() + extraRelations + roots.size() - vertexCount;
			if (cycleCount7 < 0) {
				extraRelations -= cycleCount7;
				cycleCount7 = relations.size() + extraRelations + roots.size() - vertexCount;
			}
			LOG.debug("cycleCount7 = " + cycleCount7);

			int cycleCountIncr = cycleCount - lastCycleCount;
			if (correctSmoothCountIncr != cycleCountIncr) {
				LOG.debug("ERROR: " + partialStr + " led to incorrect cycle count update!");
				// log all related partials
				LOG.debug(relatedPartials.size() + " related partials");
//				for (Partial par : relatedPartials) {
//					LOG.debug("    related partial has large factors " + Arrays.toString(par.getLargeFactorsWithOddExponent()));
//				}
				
				// log related partials after removing singletons
				@SuppressWarnings({ "unchecked", "rawtypes" })
				ArrayList<Partial> congruencesCopy = new ArrayList(relatedPartials.size());
				Map<Long, ArrayList<Partial>> largeFactors_2_partials = new HashMap<>();
				for (Partial congruence : relatedPartials) {
					congruencesCopy.add(congruence);
					addToColumn2RowMap(congruence, largeFactors_2_partials);
				}
				removeSingletons(congruencesCopy, largeFactors_2_partials);
				LOG.debug(congruencesCopy.size() + " related partials after removing singletons");
				for (Partial par : congruencesCopy) {
					LOG.debug("    related partial has large factors " + Arrays.toString(par.getLargeFactorsWithOddExponent()));
				}
//				System.exit(0);
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
			LOG.debug("roots contains r2=" + r2 + ": " + roots.contains(r2));
			roots.remove(r2);
		} else if (r1 > r2) {
			LOG.debug("roots contains r1=" + r1 + ": " + roots.contains(r1));
			edges.put(r1, r2);
			roots.remove(r1);
		} else {
			// else: r1 and r2 are in the same component -> a cycle has been found
			if (DEBUG_3LP_CYCLE_COUNTING) {
				LOG.debug("roots are equal! r1 = " + r1 + ", r2 = " + r2);
				LOG.debug("-> " + r1 + " is new root? " + !roots.contains(r1));
//				if (r1!=1) roots.remove(r1); // XXX
			}
		}
		
		// For a speedup, we could also set the "parents" of (all edges passed in root finding) to the new root
	}
	
	private void insertEdges3(long r1, long r2, long r3) {
		if (DEBUG_3LP_CYCLE_COUNTING) LOG.debug("r1=" + r1 + ", r2=" + r2 + ", r3=" + r3);
	
		// insert edge: the smallest root is made the parent of the other two, the larger roots are dropped
		if (r1 < r2) {
			if (r1 < r3) { // r1 is the smallest root
				edges.put(r2, r1);
				edges.put(r3, r1);
				roots.remove(r2);
				roots.remove(r3); // XXX
			} else { // r3 <= r1 is the smallest root
				edges.put(r1, r3);
				edges.put(r2, r3);
				if (r3 != r1) roots.remove(r1);
				roots.remove(r2);
			}
		} else { // r2 <= r1
			if (r2 < r3) { // r2 <= r1 is the smallest root
				edges.put(r1, r2);
				edges.put(r3, r2);
				if (r2 != r1) roots.remove(r1);
				roots.remove(r3);
			} else { // r3 <= r1, r2 is the smallest root
				edges.put(r1, r3);
				edges.put(r2, r3);
				if (r3 != r1) roots.remove(r1);
				if (r3 != r2) roots.remove(r2);
			}
		}
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
	
	private HashSet<Long> getRootsFromVertices() {
		HashSet<Long> roots = new HashSet<>();
		for (long vertex : edges.keySet()) {
			long r = getRoot(vertex);
			roots.add(r);
		}
		return roots;
	}

	/**
	 * Remove singletons from <code>congruences</code>.
	 * This can reduce the size of the equation system; actually it never diminishes the difference (#eqs - #vars).
	 * It is very fast, too - like 60ms for a matrix for which solution via Gauss elimination takes 1 minute.
	 * 
	 * @param congruences 
	 * @param largeFactors_2_partials 
	 */
	private void removeSingletons(List<Partial> congruences, Map<Long, ArrayList<Partial>> largeFactors_2_partials) {
		// Parse all congruences as long as we find a singleton in a complete pass
		boolean foundSingleton;
		do {
			foundSingleton = false;
			Iterator<? extends Partial> congruenceIter = congruences.iterator();
			while (congruenceIter.hasNext()) {
				Partial congruence = congruenceIter.next();
				for (Long oddExpFactor : congruence.getLargeFactorsWithOddExponent()) {
					if (largeFactors_2_partials.get(oddExpFactor).size()==1) {
						// found singleton -> remove from list
						if (DEBUG) LOG.debug("Found singleton -> remove " + congruence);
						congruenceIter.remove();
						// remove from oddExpFactors_2_congruences so we can detect further singletons
						removeFromColumn2RowMap(congruence, largeFactors_2_partials);
						foundSingleton = true;
						break;
					}
				}
			} // one pass finished
		} while (foundSingleton && congruences.size()>0);
		// now all singletons have been removed from congruences.
		if (DEBUG) LOG.debug("#congruences after removing singletons: " + congruences.size());
	}
	
	private void addToColumn2RowMap(Partial congruence, Map<Long, ArrayList<Partial>> largeFactors_2_partials) {
		for (Long factor : congruence.getLargeFactorsWithOddExponent()) {
			ArrayList<Partial> congruenceList = largeFactors_2_partials.get(factor);
			if (congruenceList == null) {
				congruenceList = new ArrayList<Partial>();
				largeFactors_2_partials.put(factor, congruenceList);
			}
			congruenceList.add(congruence);
		}
	}
	
	private void removeFromColumn2RowMap(Partial congruence, Map<Long, ArrayList<Partial>> largeFactors_2_partials) {
		for (Long factor : congruence.getLargeFactorsWithOddExponent()) {
			ArrayList<Partial> congruenceList = largeFactors_2_partials.get(factor);
			congruenceList.remove(congruence);
			if (congruenceList.size()==0) {
				// there are no more congruences with the current factor
				largeFactors_2_partials.remove(factor);
			}
		}
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
