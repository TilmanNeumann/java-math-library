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
package de.tilman_neumann.jml.factor.cfrac;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tilman_neumann.jml.factor.base.matrixSolver.MatrixSolver_Gauss02;
import de.tilman_neumann.jml.factor.cfrac.tdiv.TDiv_CF02;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.TimeUtil;
import de.tilman_neumann.util.Timer;

public class CFracRunner {
	private static final Logger LOG = LogManager.getLogger(CFracRunner.class);

	private static void testInput() {
		CFrac cfrac = new CFrac(true, 5, 1.5F, 0.152F, 0.253F, new TDiv_CF02(), new MatrixSolver_Gauss02(), 5);
		Timer timer = new Timer();
		while(true) {
			try {
				LOG.info("Please insert the number to factor:");
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				String line = in.readLine();
				String input = line !=null ? line.trim() : "";
				//LOG.debug("input = >" + input + "<");
				BigInteger N = new BigInteger(input);
				LOG.info("Factoring " + N + " ...");
				timer.capture();
				BigInteger factor = cfrac.findSingleFactor(N);
				if (factor != null) {
					long duration = timer.capture();
					LOG.info("Found factor " + factor + " in " + TimeUtil.timeStr(duration) + ".");
				} else {
					LOG.info("No factor found.");
				}
			} catch (Exception ex) {
				LOG.error("Error " + ex, ex);
			}
		}
	}
	
	/**
	 * Runs CFrac with user inputs.<br>
	 * 
	 * Test numbers:<br>
	 * F7 = 340282366920938463463374607431768211457<br><br>
	 * 
	 * Some test numbers to debug cycle counting with 3LP:<br>
	 * 1131700560863845693969719287759517367069129639 (150 bit): found 304 smooth congruences (30 perfect, 47 from 1-partials, 215 involving 2-partials, 12 involving 3-partials) and 21132 partials (8253 1-partials, 12865 2-partials, 14 3-partials)<br>
	 * 1042841142257557545672851027890020895273750538581 (160 bit): found 388 smooth congruences (38 perfect, 45 from 1-partials, 259 involving 2-partials, 46 involving 3-partials) and 34699 partials (9696 1-partials, 24948 2-partials, 55 3-partials)<br>
	 * 1240365498452764190513871432931316765426281182537733 (170 bit): found 500 smooth congruences (90 perfect, 55 from 1-partials, 182 involving 2-partials, 173 involving 3-partials) and 51145 partials (10309 1-partials, 40527 2-partials, 309 3-partials)<br>
	 * 800428260973992320615961356229212951260121574827941327 (180 bit): found 630 smooth congruences (87 perfect, 38 from 1-partials, 186 involving 2-partials, 319 involving 3-partials) and 85259 partials (11123 1-partials, 73206 2-partials, 930 3-partials)<br>
	 * 
	 * @param args ignored
	 */
	public static void main(String[] args) {
    	ConfigUtil.initProject();
    	testInput();
	}
}
