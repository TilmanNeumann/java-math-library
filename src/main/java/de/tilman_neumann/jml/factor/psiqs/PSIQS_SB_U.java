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

import java.math.BigInteger;

import de.tilman_neumann.jml.factor.base.congruence.CongruenceCollector;
import de.tilman_neumann.jml.factor.base.congruence.CongruenceCollector01;
import de.tilman_neumann.jml.factor.base.matrixSolver.MatrixSolver;
import de.tilman_neumann.jml.factor.siqs.data.BaseArrays;
import de.tilman_neumann.jml.factor.siqs.poly.AParamGenerator;
import de.tilman_neumann.jml.factor.siqs.poly.AParamGenerator02;
import de.tilman_neumann.jml.factor.siqs.powers.PowerFinder;
import de.tilman_neumann.jml.factor.siqs.sieve.SieveParams;

/**
 * Multi-threaded SIQS using a single-block sieve depending on sun.misc.Unsafe.
 * 
 * @author Tilman Neumann
 */
public class PSIQS_SB_U extends PSIQSBase {

	/**
	 * Standard constructor.
	 * @param Cmult multiplier for prime base size
	 * @param Mmult multiplier for sieve array size
	 * @param wantedQCount hypercube dimension (null for automatic selection)
	 * @param numberOfThreads
	 * @param powerFinder algorithm to add powers to the primes used for sieving
	 * @param matrixSolver solver for smooth congruences matrix
	 */
	public PSIQS_SB_U(float Cmult, float Mmult, Integer wantedQCount, int numberOfThreads, PowerFinder powerFinder, MatrixSolver matrixSolver) {
		super(Cmult, Mmult, numberOfThreads, null, powerFinder, matrixSolver, new AParamGenerator02(wantedQCount), new CongruenceCollector01(10));
	}

	@Override
	public String getName() {
		return "PSIQS_SB_U(Cmult=" + Cmult + ", Mmult=" + Mmult + ", qCount=" + apg.getQCount() + ", " + powerFinder.getName() + ", " + matrixSolver.getName() + ", " + numberOfThreads + " threads)";
	}

	@Override
	protected PSIQSThreadBase createThread(
			int k, BigInteger N, BigInteger kN, int d, SieveParams sieveParams, BaseArrays baseArrays,
			AParamGenerator apg, CongruenceCollector cc, int threadIndex) {
		
		return new PSIQSThread_SB_U(k, N, kN, d, sieveParams, baseArrays, apg, cc, threadIndex);
	}
}
