package de.tilman_neumann.jml.factor.base.congruence;

import java.util.HashSet;

/**
 * Interface for cycle counter algorithms.
 * @author Till
 *
 * @see [LLDMW02] Leyland, Lenstra, Dodson, Muffett, Wagstaff 2002: "MPQS with three large primes", Lecture Notes in Computer Science, 2369.
 * @see [LM94] Lenstra, Manasse 1994: "Factoring With Two Large Primes", Mathematics of Computation, volume 63, number 208, page 789.
 */
public interface CycleCounter {

	/**
	 * Counts the number of independent cycles in the partial relations following [LLDMW02], [LM94].
	 * Works for 2LP so far, but not for 3LP yet.
	 * 
	 * @param partial the newest partial relation to add
	 * @param correctSmoothCount the correct number of smooths from partials (only for debugging)
	 * @param relatedPartials (only for debugging)
	 * @return the updated number of smooths from partials
	 */
	int addPartial(Partial partial, int correctSmoothCount, HashSet<Partial> relatedPartials);

	/**
	 * @return the partial relations found so far
	 */
	HashSet<Partial> getPartialRelations();

	/**
	 * @return number of partial relations found so far.
	 */
	int getPartialRelationsCount();
}
