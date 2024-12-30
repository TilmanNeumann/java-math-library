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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tilman_neumann.jml.factor.base.matrixSolver.MatrixSolverBlockLanczos;
import de.tilman_neumann.jml.factor.siqs.powers.NoPowerFinder;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;
import de.tilman_neumann.util.TimeUtil;
import de.tilman_neumann.util.Timer;

public class PSIQS_U_Runner {

	private static final Logger LOG = LogManager.getLogger(PSIQS_U_Runner.class);

	/**
	 * Stand-alone test. 
	 * Should only be called with semiprime arguments.
	 * For general arguments use class CombinedFactorAlgorithm.
	 * 
	 * @param args ignored
	 */
	// Test numbers:
	// RSA-100 = 1522605027922533360535618378132637429718068114961380688657908494580122963258952897654000350692006139
	public static void main(String[] args) {
    	ConfigUtil.initProject();
		Timer timer = new Timer();
		int numThreads = 12; // insert your number of threads here
		PSIQS_U qs = new PSIQS_U(0.31F, 0.37F, null, numThreads, new NoPowerFinder(), new MatrixSolverBlockLanczos());

		while (true) {
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
					LOG.info("Factored N = " + factors.toString("*", "^") + " in " + TimeUtil.timeStr(duration) + ".");
				} else {
					LOG.info("No factor found...");
				}
			} catch (Exception ex) {
				LOG.error("Error " + ex, ex);
			}
		}
	}
}
