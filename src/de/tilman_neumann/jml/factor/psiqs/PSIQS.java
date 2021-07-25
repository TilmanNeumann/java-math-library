/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018 Tilman Neumann (www.tilman-neumann.de)
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

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.base.congruence.CongruenceCollector;
import de.tilman_neumann.jml.factor.base.congruence.CongruenceCollector01;
import de.tilman_neumann.jml.factor.base.matrixSolver.MatrixSolver;
import de.tilman_neumann.jml.factor.base.matrixSolver.MatrixSolver_BlockLanczos;
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
 * Multi-threaded SIQS using the fastest sieve not depending on sun.misc.Unsafe.
 * 
 * @author Tilman Neumann
 */
public class PSIQS extends PSIQSBase {

	private static final Logger LOG = Logger.getLogger(PSIQS.class);

	/**
	 * Standard constructor.
	 * @param Cmult multiplier for prime base size
	 * @param Mmult multiplier for sieve array size
	 * @param wantedQCount hypercube dimension (null for automatic selection)
	 * @param numberOfThreads
	 * @param powerFinder algorithm to add powers to the primes used for sieving
	 * @param matrixSolver solver for smooth congruences matrix
	 */
	public PSIQS(float Cmult, float Mmult, Integer wantedQCount, int numberOfThreads, PowerFinder powerFinder, MatrixSolver matrixSolver) {
		super(Cmult, Mmult, numberOfThreads, null, powerFinder, matrixSolver, new AParamGenerator02(wantedQCount), new CongruenceCollector01(10));
	}

	@Override
	public String getName() {
		return "PSIQS(Cmult=" + Cmult + ", Mmult=" + Mmult + ", qCount=" + apg.getQCount() + ", " + powerFinder.getName() + ", " + matrixSolver.getName() + ", " + numberOfThreads + " threads)";
	}

	@Override
	protected PSIQSThreadBase createThread(
			int k, BigInteger N, BigInteger kN, int d, SieveParams sieveParams, BaseArrays baseArrays,
			AParamGenerator apg, CongruenceCollector cc, int threadIndex) {
		
		return new PSIQSThread(k, N, kN, d, sieveParams, baseArrays, apg, cc, threadIndex);
	}

	// Standalone test --------------------------------------------------------------------------------------------------

	/**
	 * Stand-alone test. 
	 * Should only be called with semiprime arguments.
	 * For general arguments use class CombinedFactorAlgorithm.
	 * 
	 * @param args ignored
	 */
	public static void main(String[] args) {
    	ConfigUtil.initProject();
		Timer timer = new Timer();
		// conservative choice of number of threads; put there what you have
		PSIQS qs = new PSIQS(0.31F, 0.37F, null, 4, new NoPowerFinder(), new MatrixSolver_BlockLanczos());

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
