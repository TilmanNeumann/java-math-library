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
public class CycleCounter3LP2 implements CycleCounter {
	
	private static final Logger LOG = Logger.getLogger(CycleCounter3LP2.class);
	private static final boolean DEBUG = false; // used for logs and asserts
	
	// cycle counting
	private HashMap<Long, Long> edges; // contains edges: bigger to smaller prime; size is v = #vertices
	private HashSet<Long> roots; // roots of disjoint components
	private HashSet<Partial> relations; // all distinct relations
	// the number of smooths from partials found
	private int cycleCount;
	private int lastCorrectSmoothCount;
	private int additionalEdgeCount;
	
	/**
	 * Full constructor.
	 * @param maxLargeFactors the maximum number of large primes in partials:  2 or 3
	 */
	public CycleCounter3LP2() {
		edges = new HashMap<>(); // bigger to smaller prime
		roots = new HashSet<>();
		relations = new HashSet<>();
		cycleCount = 0;
		lastCorrectSmoothCount = 0;
		additionalEdgeCount = 0;
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
		
		// add edges
		if (largeFactorsCount==1) {
			if (DEBUG) assertTrue(largeFactors[0] > 1);
//			insertEdge1(largeFactors[0]); // XXX
			insertEdge2(1, largeFactors[0]);
		} else if (largeFactorsCount==2) {
			if (DEBUG) assertTrue(largeFactors[0] != largeFactors[1]);
			insertEdge2(largeFactors[0], largeFactors[1]);
		} else if (largeFactorsCount==3) {
			if (DEBUG) {
				assertTrue(largeFactors[0] != largeFactors[1]);
				assertTrue(largeFactors[0] != largeFactors[2]);
				assertTrue(largeFactors[1] != largeFactors[2]);
			}
//			insertEdge2(largeFactors[0], largeFactors[1]);
//			insertEdge2(largeFactors[0], largeFactors[2]);
//			insertEdge2(largeFactors[1], largeFactors[2]);
			insertEdge3(largeFactors[0], largeFactors[1], largeFactors[2]); // XXX !!!
		} else {
			LOG.warn("Holy shit, we found a " + largeFactorsCount + "-partial!");
		}
		
		// update edge count and cycle count
		int vertexCount = edges.size();
		int edgeCount = relations.size() + additionalEdgeCount; // XXX additionalEdgeCount is new
		cycleCount = edgeCount + roots.size() - vertexCount; 

		if (DEBUG_3LP_CYCLE_COUNTING) {
			int rootCount = roots.size();
			String rootsStr = rootCount<100 ? rootCount + " roots = " + roots : rootCount + " roots";
			LOG.debug(rootsStr);
			String vertexStr = vertexCount<100 ? vertexCount + " vertices = " + edges : vertexCount + " vertices";
			LOG.debug(vertexStr);
			LOG.debug(relations.size() + " relations");
			LOG.debug("additionalEdgeCount = " + additionalEdgeCount);

			LOG.debug("correctSmoothCount = " + correctSmoothCount);

			// same formula as for 2LP, does not need edgeCount at all!
			String cycleCountFormula = "#edges + #components - #vertices";
			LOG.debug("#edges=" + edgeCount + ", #components=" + roots.size() + ", #vertices=" + edges.size() + " -> cycleCount = " + cycleCountFormula + " = " + cycleCount);
			
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
	
	private void insertEdge1(long p1) {
		Long r1 = getRoot(p1);
		
		if (r1!=null) {
			// the vertex already exists -> do nothing
			LOG.debug("1LP: 1 old vertex");
		} else {
			// the prime is new and forms its own new disconnected component
			LOG.debug("1LP: 1 new vertex");
			edges.put(p1, p1);
			roots.add(p1);
		}
	}

	/** p1 = smaller p, p2 = larger p */
	private void insertEdge2(long p1, long p2) {
		Long r1 = getRoot(p1);
		Long r2 = getRoot(p2);

		if (r1!=null && r2!=null) {
			// both vertices already exist.
			// if the roots are different, then we have distinct components which we can join now
			if (r1<r2) {
				LOG.debug("2LP: 2 old vertices from distinct components");
				edges.put(r2, r1);
				roots.remove(r2);
			} else if (r2<r1) {
				LOG.debug("2LP: 2 old vertices from distinct components");
				edges.put(r1, r2);
				roots.remove(r1);
			} else {
				// if the roots are equal than both primes are already part of the same component so nothing more happens
				LOG.debug("2LP: 2 old vertices from the same components");
			}
		} else if (r1 != null) {
			// p1 already exists, p2 is new -> we just add p2 to the component of p1
			LOG.debug("2LP: 1 old vertex, 1 new vertex");
			edges.put(p2, r1);
		} else if (r2 != null) {
			// p2 already exists, p1 is new -> we just add p1 to the component of p2
			LOG.debug("2LP: 1 old vertex, 1 new vertex");
			edges.put(p1, r2);
		} else {
			// both primes are new and form their own new disconnected component
			// we know p1 < p2
			LOG.debug("2LP: 2 new vertices");
			edges.put(p1, p1);
			edges.put(p2, p1);
			roots.add(p1);
		}
	}

	/** p1 <= p2 <= p3 */
	private void insertEdge3(long p1, long p2, long p3) {
		Long r1 = getRoot(p1);
		Long r2 = getRoot(p2);
		Long r3 = getRoot(p3);
		
		if (r1!=null && r2!=null && r3!=null) {
			// all three vertices already exist. in this case we only need the roots, so we can sort them without keeping the referecne to the primes
			long tmp;
			if (r2<r1) { tmp=r1; r1=r2; r2=tmp; }
			if (r3<r2) { tmp=r2; r2=r3; r3=tmp; }
			if (r2<r1) { tmp=r1; r1=r2; r2=tmp; }
			// now r1 <= r2 <= r3
			
			// if they lie in different components we can connect them
			if (r1<r2) {
				if (r2<r3) {
					// r1 < r2 < r3, three different components
					LOG.debug("3LP: 3 old vertices from 3 distinct components");
					edges.put(r2, r1);
					edges.put(r3, r1);
					roots.remove(r2);
					roots.remove(r3);
					additionalEdgeCount++;
				} else {
					// r1 < r2==r3, two different components
					LOG.debug("3LP: 3 old vertices from 2 distinct components");
					edges.put(r2, r1);
					roots.remove(r2);
					additionalEdgeCount++;
				}
			} else {
				// r1==r2
				if (r2<r3) {
					// r1==r2 < r3, two different components
					LOG.debug("3LP: 3 old vertices from 2 distinct components");
					edges.put(r3, r1);
					roots.remove(r3);
					additionalEdgeCount++;
				} else {
					// r1==r2==r3, all vertices lie in the same component
					LOG.debug("3LP: 3 old vertices all from the same components");
				}
			}
		} else if (r1!=null && r2!=null) {
			// p1 and p2 already existed, p3 is new.
			// if the two existing roots are different, we can connect their components and add p3 to it.
			// thereby, the number of components reduces by 1.
			if (r1<r2) {
				LOG.debug("3LP: 2 old vertices from distinct components, one new vertex");
				edges.put(r2, r1);
				roots.remove(r2);
				edges.put(p3, r1);
				additionalEdgeCount++;
			} else if (r2<r1) {
				LOG.debug("3LP: 2 old vertices from distinct components, one new vertex");
				edges.put(r1, r2);
				roots.remove(r1);
				edges.put(p3, r2);
				additionalEdgeCount++;
			} else {
				// the two existing primes belong to the same component, thus we only add the new prime to it
				LOG.debug("3LP: 2 old vertices from the same components, one new vertex");
				edges.put(p3, r1);
			}
		} else if (r1!=null && r3!=null) {
			// p1 and p3 already existed, p2 is new.
			if (r1<r3) {
				LOG.debug("3LP: 2 old vertices from distinct components, one new vertex");
				edges.put(r3, r1);
				roots.remove(r3);
				edges.put(p2, r1);
				additionalEdgeCount++;
			} else if (r3<r1) {
				LOG.debug("3LP: 2 old vertices from distinct components, one new vertex");
				edges.put(r1, r3);
				roots.remove(r1);
				edges.put(p2, r3);
				additionalEdgeCount++;
			} else {
				// the two existing primes belong to the same component, thus we only add the new prime to it
				LOG.debug("3LP: 2 old vertices from the same components, one new vertex");
				edges.put(p2, r1);
			}
		} else if (r2!=null && r3!=null) {
			// p2 and p3 already existed, p1 is new.
			if (r2<r3) {
				LOG.debug("3LP: 2 old vertices from distinct components, one new vertex");
				edges.put(r3, r2);
				roots.remove(r3);
				edges.put(p1, r2);
				additionalEdgeCount++;
			} else if (r3<r2) {
				LOG.debug("3LP: 2 old vertices from distinct components, one new vertex");
				edges.put(r2, r3);
				roots.remove(r2);
				edges.put(p1, r3);
				additionalEdgeCount++;
			} else {
				// the two existing primes belong to the same component, thus we only add the new prime to it
				LOG.debug("3LP: 2 old vertices from the same components, one new vertex");
				edges.put(p1, r2);
			}
		} else if (r1!=null) {
			// p1 already existed, p2 and p3 are new.
			// We add both new primes to the existing component. The number of components remains unchanged.
			LOG.debug("3LP: 1 old vertex, two new vertices");
			edges.put(p2, r1);
			edges.put(p3, r1);
			additionalEdgeCount++;
		} else if (r2!=null) {
			// p2 already existed, p1 and p3 are new.
			LOG.debug("3LP: 1 old vertex, two new vertices");
			edges.put(p1, r2);
			edges.put(p3, r2);
			additionalEdgeCount++;
		} else if (r3!=null) {
			// p3 already existed, p1 and p2 are new.
			LOG.debug("3LP: 1 old vertex, two new vertices");
			edges.put(p1, r3);
			edges.put(p2, r3);
			additionalEdgeCount++;
		} else {
			// all three vertices are new -> we create a new component with the smallest prime as root
			// fortunately we already know that p1 <= p2 <= p3
			LOG.debug("3LP: three new vertices");
			edges.put(p1, p1);
			edges.put(p2, p1);
			edges.put(p3, p2);
			roots.add(p1);
			additionalEdgeCount++;
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
