package de.tilman_neumann.jml.factor.base.congruence;

import java.util.HashSet;

/**
 * Interface for cycle counting algorithms.
 * @author Till
 *
 * @see [LLDMW02] Leyland, Lenstra, Dodson, Muffett, Wagstaff 2002: "MPQS with three large primes", Lecture Notes in Computer Science, 2369.
 * @see [LM94] Lenstra, Manasse 1994: "Factoring With Two Large Primes", Mathematics of Computation, volume 63, number 208, page 789.
 */
public interface CycleCounter {

	/** Initialize this cycle counter for a new factor argument. */
	void initializeForN();
	
	/**
	 * Counts the number of independent cycles in the partial relations.
	 * 
	 * @param partial the newest partial relation to add
	 * @param correctSmoothCount the correct number of smooths from partials (only for debugging)
	 * @return the updated number of smooths from partials
	 */
	int addPartial(Partial partial, int correctSmoothCount);

	/**
	 * @return the partial relations found so far
	 */
	HashSet<Partial> getPartialRelations();

	/**
	 * @return number of partial relations found so far.
	 */
	int getPartialRelationsCount();
	
	/**
	 * @return the number of cycles counted by this algorithm
	 */
	int getCycleCount();
}
