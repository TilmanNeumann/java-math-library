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
	
	enum FORMULA {
		DLP,
		INTERMEDIATE,
		TLP
	}
	
	private static final Logger LOG = Logger.getLogger(CycleFinder.class);
	private static final boolean DEBUG = false; // used for logs and asserts

	private static FORMULA formula = FORMULA.INTERMEDIATE;
	
	// cycle counting
	private int maxLargeFactors;
	private HashMap<Long, Long> edges; // contains edges: bigger to smaller prime; size is v = #vertices
	private HashSet<Long> roots; // roots of disjoint components
	private HashSet<Partial> relations; // all distinct relations
	private int edgeCount;
	// the number of smooths from partials found
	private int cycleCount;
	private int lastCorrectSmoothCount;
	
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
		lastCorrectSmoothCount = 0;
	}
	
	/**
	 * Counts the number of independent cycles in the partial relations following [LM94], [LLDMW02].
	 * Works for 2LP so far, but not for 3LP yet.
	 * 
	 * @param partial the newest partial relation to add
	 * @param correctSmoothCount the correct number of smooths from partials (only for debugging)
	 * @return the updated number of smooths from partials
	 */
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
		if (maxLargeFactors==2) {
			// standard formula: #cycles = #edges (one per relation) + #components - #vertices
			cycleCount = relations.size() + roots.size() - vertexCount;
		} else if (maxLargeFactors==3) {
			switch (formula) {
			case DLP: {
				// same formula as for 2LP, does not need edgeCount at all!
				cycleCount = relations.size() + roots.size() - vertexCount; 
				break;
			}
			case INTERMEDIATE: {
				if (largeFactorsCount==3) {
//					if (!isNewVertex[0] && !isNewVertex[1] && !isNewVertex[2] ) {
					if (vertexRoots[0]==vertexRoots[1] && vertexRoots[0]==vertexRoots[2] && vertexRoots[1]==vertexRoots[2]) {
						edgeCount += 2;
					} else if (vertexRoots[0]==vertexRoots[1] || vertexRoots[0]==vertexRoots[2] || vertexRoots[1]==vertexRoots[2]){
						edgeCount += 3;
					} else {
						edgeCount += 3;
					}

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
				cycleCount = edgeCount + roots.size() - vertexCount - relations.size();
				break;
			}
			case TLP: {
				edgeCount += 3;
				// The "thought to be" formula from [LLDMW02], p.7
				cycleCount = edgeCount + roots.size() - vertexCount - 2*relations.size();
				break;
			}
			}
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

			switch (formula) {
			case DLP: {
				// same formula as for 2LP, does not need edgeCount at all!
				String cycleCountFormula = "#edges + #roots - #vertices";
				LOG.debug("#edges=" + relations.size()  + ", #roots=" + roots.size()  + ", #vertices=" + edges.size() + " -> cycleCount = " + cycleCountFormula + " = " + cycleCount);
				// 150 bit: 273 instead of 274 cycles, 23 errors (12 down, 11 up)
				break;
			}
			case INTERMEDIATE: {
				String cycleCountFormula = "#edges + #roots - #vertices - #relations";
				LOG.debug("#edges=" + edgeCount  + ", #roots=" + roots.size()  + ", #vertices=" + edges.size() + ", #relations=" + relations.size() + " -> cycleCount = " + cycleCountFormula + " = " + cycleCount);
				// 150 bit: 287 instead of 274 cycles, 13 errors (all up)
				// all remaining errors come from adding 1LP- or 2LP-partials that are related to at least one 3LP-partial (and after removing singletons there is no partial left)
				break;
			}
			case TLP: {
				// original from [LLDMW02], p.7
				String cycleCountFormula = "#edges + #roots - #vertices - 2*#relations";
				LOG.debug("#edges=" + edgeCount  + ", #roots=" + roots.size()  + ", #vertices=" + edges.size() + ", #relations=" + relations.size() + " -> cycleCount = " + cycleCountFormula + " = " + cycleCount);
				// 150 bit: 273 instead of 274 cycles, 23 errors (12 down, 11 up)
				break;
			}
			}

			LOG.debug("#rootsFromVertices = " + getRootsFromVertices().size());

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
