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
package de.tilman_neumann.jml.factor.psiqs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.factor.base.congruence.CongruenceCollector;
import de.tilman_neumann.jml.factor.base.congruence.CongruenceCollector03;
import de.tilman_neumann.jml.factor.base.matrixSolver.MatrixSolver;
import de.tilman_neumann.jml.factor.base.matrixSolver.MatrixSolverBlockLanczos;
import de.tilman_neumann.jml.factor.siqs.data.BaseArrays;
import de.tilman_neumann.jml.factor.siqs.poly.AParamGenerator;
import de.tilman_neumann.jml.factor.siqs.poly.AParamGenerator02;
import de.tilman_neumann.jml.factor.siqs.powers.NoPowerFinder;
import de.tilman_neumann.jml.factor.siqs.powers.PowerFinder;
import de.tilman_neumann.jml.factor.siqs.sieve.SieveParams;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;
import de.tilman_neumann.util.TimeUtil;
import de.tilman_neumann.util.Timer;

/**
 * Multi-threaded SIQS using the fastest sieve depending on sun.misc.Unsafe, and all sub-algorithms working with 3-partials.
 * Note that with the current parametrization, 3-partials are not found for N<=400 bit.
 * 
 * @author Tilman Neumann
 */
public class PSIQS_U_3LP extends PSIQSBase {

	private static final Logger LOG = LogManager.getLogger(PSIQS_U_3LP.class);

	/**
	 * Standard constructor.
	 * @param Cmult multiplier for prime base size
	 * @param Mmult multiplier for sieve array size
	 * @param wantedQCount hypercube dimension (null for automatic selection)
	 * @param numberOfThreads
	 * @param powerFinder algorithm to add powers to the primes used for sieving
	 * @param matrixSolver solver for smooth congruences matrix
	 */
	public PSIQS_U_3LP(float Cmult, float Mmult, Integer wantedQCount, int numberOfThreads, PowerFinder powerFinder, MatrixSolver matrixSolver) {
		super(Cmult, Mmult, numberOfThreads, null, powerFinder, matrixSolver, new AParamGenerator02(wantedQCount), new CongruenceCollector03(10));
	}

	@Override
	public String getName() {
		return "PSIQS_U_3LP(Cmult=" + Cmult + ", Mmult=" + Mmult + ", qCount=" + apg.getQCount() + ", " + powerFinder.getName() + ", " + matrixSolver.getName() + ", " + numberOfThreads + " threads)";
	}

	@Override
	protected PSIQSThreadBase createThread(
			int k, BigInteger N, BigInteger kN, int d, SieveParams sieveParams, BaseArrays baseArrays,
			AParamGenerator apg, CongruenceCollector cc, int threadIndex) {
		
		return new PSIQSThread_U_3LP(k, N, kN, d, sieveParams, baseArrays, apg, cc, threadIndex);
	}

	// Standalone test --------------------------------------------------------------------------------------------------

	/**
	 * Stand-alone test. 
	 * Should only be called with semiprime arguments.
	 * For general arguments use class CombinedFactorAlgorithm.
	 * 
	 * @param args ignored
	 */
	// Good 330 bit number to test 3LP: 1193021186851987582089887341794273573523742049278854202794336422907159752083864394187553156410578431
	public static void main(String[] args) {
    	ConfigUtil.initProject();
		Timer timer = new Timer();
		int numThreads = 20; // insert your number of threads here
		PSIQS_U_3LP qs = new PSIQS_U_3LP(0.31F, 0.37F, null, numThreads, new NoPowerFinder(), new MatrixSolverBlockLanczos());

		while(true) {
			try {
				LOG.info("Please insert the number to factor:");
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				String line = in.readLine();
				String input = line !=null ? line.trim() : "";
				//LOG.debug("input = >" + input + "<");
				BigInteger N = new BigInteger(input);
				LOG.info("Factoring " + N + " (" + N.bitLength() + " bits)...");
				timer.capture();
				SortedMultiset<BigInteger> factors = qs.factor(N);
				if (factors != null) {
					long duration = timer.capture();
					LOG.info("Factored N = " + factors + " in " + TimeUtil.timeStr(duration) + ".");
			} else {
					LOG.info("No factor found...");
				}
			} catch (Exception ex) {
				LOG.error("Error " + ex, ex);
			}
		}
	}
}
