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
package de.tilman_neumann.jml.powers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.powers.PurePowerTest.Result;
import de.tilman_neumann.util.ConfigUtil;

/**
 * Test the pure powers by user inputs.
 * 
 * @author Tilman Neumann
 */
public class PurePowerTestRunner {
	private static final Logger LOG = LogManager.getLogger(PurePowerTestRunner.class);

	/**
	 * Test.
	 * @param args ignored
	 */
	public static void main(String[] args) {
	   	ConfigUtil.initProject();
	   	PurePowerTest powTest = new PurePowerTest();
	   	while(true) {
			try {
				LOG.info("Insert test argument N:");
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				String line = in.readLine();
				String input = line !=null ? line.trim() : "";
				//LOG.debug("input = >" + input + "<");
				BigInteger N = new BigInteger(input);
				Result purePower = powTest.test(N);
				if (purePower == null) {
					LOG.info("N = " + N + " is not a pure power.");
				} else {
					LOG.info("N = " + N + " = " + purePower.base + "^" + purePower.exponent + " is a pure power!");
				}
			} catch (Exception ex) {
				LOG.error("Error " + ex, ex);
			}
		}
	}
}
