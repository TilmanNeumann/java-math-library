package de.tilman_neumann.jml.factor.base.congruence;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import de.tilman_neumann.jml.factor.FactorException;
import de.tilman_neumann.jml.factor.base.matrixSolver.FactorTest;
import de.tilman_neumann.jml.factor.base.matrixSolver.MatrixSolver;

public interface CongruenceCollector {

	/**
	 * Initialize congruence collector for a new N.
	 * @param N
	 * @param primeBaseSize
	 * @param matrixSolver
	 * @param factorTest
	 */
	void initialize(BigInteger N, int primeBaseSize, MatrixSolver matrixSolver, FactorTest factorTest);

	/**
	 * Initialize congruence collector for a new N.
	 * @param N
	 * @param factorTest
	 */
	void initialize(BigInteger N, FactorTest factorTest);

	/**
	 * Collect AQ pairs and run the matrix solver if appropriate.
	 * In a multi-threaded factoring algorithm, this method needs to be run in a block synchronized on this.
	 * This also speeds up single-threaded solvers like Block-Lanczos, because on modern CPUs single threads run at a higher clock rate.
	 * @param aqPairs
	 * @output this.factor
	 */
	void collectAndProcessAQPairs(List<AQPair> aqPairs);

	/**
	 * Add a new elementary partial or smooth congruence.
	 * @param aqPair
	 * @return true if a smooth congruence was added
	 * @throws FactorException
	 */
	boolean add(AQPair aqPair) throws FactorException;

	/**
	 * @return number of smooth congruences required before the matrix solver is called
	 */
	int getRequiredSmoothCongruenceCount();

	/**
	 * @return number of smooth congruences found so far.
	 */
	int getSmoothCongruenceCount();

	/**
	 * @return smooth congruences found so far.
	 */
	ArrayList<Smooth> getSmoothCongruences();

	/**
	 * @return number of partial congruences found so far.
	 */
	int getPartialCongruenceCount();

	/**
	 * @return the factor that was found or null
	 */
	BigInteger getFactor();
	
	CongruenceCollectorReport getReport();

	long getCollectDuration();

	long getSolverDuration();

	/**
	 * @return the number of solvers runs required to find a factor (should be 1)
	 */
	int getSolverRunCount();

	/**
	 * @return the number of null vector tests required to find a factor
	 */
	int getTestedNullVectorCount();

	/**
	 * Release memory after a factorization.
	 */
	void cleanUp();

}