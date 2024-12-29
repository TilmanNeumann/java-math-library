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
package de.tilman_neumann.jml.factor.pollardRho;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;

public class PollardRhoBrentRunner {
	private static final Logger LOG = LogManager.getLogger(PollardRhoBrentRunner.class);

	/**
	 * Test.<br>
	 * Note that timings may vary a lot from run to run.<br>
	 * Some test numbers:<br/>
	 * 
	 * 5679148659138759837165981543 = 450469808245315337 * 466932157 * 3^3, takes ~ 77-250 ms<br/>
	 * 
	 * 54924524576914518357355679148659138759837165981543 = 1557629117554716582307318666440656471 * 35261619058033, takes ~ 0.8-12 seconds<br/>
	 * 
	 * F6 = 18446744073709551617 = 274177 * 67280421310721, takes ~ 2-166 ms<br/>
	 * 
	 * F7 = 2^128 + 1 = 340282366920938463463374607431768211457 = 5704689200685129054721 * 59649589127497217;
	 * takes ~ 87-414 seconds for PollardRhoBrent, easy for CFrac or ECM<br/>
	 * 
	 * F8 = 115792089237316195423570985008687907853269984665640564039457584007913129639937 = 1238926361552897 * 93461639715357977769163558199606896584051237541638188580280321,
	 * takes ~ 15-141 seconds<br/>
	 * 
	 * 8225267468394993133669189614204532935183709603155231863020477010700542265332938919716662623
	 * = 1234567891 * 1234567907 * 1234567913 * 1234567927 * 1234567949 * 1234567967 * 1234567981 * 1234568021 * 1234568029 * 1234568047,
	 * takes ~ 123-300 ms<br/>
	 * 
	 * 101546450935661953908994991437690198927080333663460351836152986526126114727314353555755712261904130976988029406423152881932996637460315302992884162068350429 = 
	 * 123456789012419 * 123456789012421 * 123456789012437 * 123456789012439 * 123456789012463 * 123456789012521 *
	 * 123456789012523 * 123456789012533 * 123456789012577 * 123456789012629 * 123456789012637,
	 * takes ~ 72-147 seconds<br/>
	 * 
	 * @param args ignored
	 */
	public static void main(String[] args) {
    	ConfigUtil.initProject();
    	
		while(true) {
			String input;
			try {
				LOG.info("Please insert the integer to factor:");
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				String line = in.readLine();
				input = line.trim();
				LOG.debug("Factoring " + input + "...");
			} catch (IOException ioe) {
				LOG.error("IO-error occurring on input: " + ioe.getMessage());
				continue;
			}
			
			long start = System.currentTimeMillis();
			BigInteger n = new BigInteger(input);
			SortedMultiset<BigInteger> result = new PollardRhoBrent().factor(n);
			LOG.info("Factored " + n + " = " + result.toString() + " in " + (System.currentTimeMillis()-start) + " ms");

		} // next input...
	}
}
