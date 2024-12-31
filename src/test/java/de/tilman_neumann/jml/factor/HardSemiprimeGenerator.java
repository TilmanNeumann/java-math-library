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
package de.tilman_neumann.jml.factor;

import java.math.BigInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

public class HardSemiprimeGenerator {
	private static final Logger LOG = LogManager.getLogger(HardSemiprimeGenerator.class);

	/**
	 * A simple main function to generate hard semi-primes.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		ConfigUtil.initProject();

		for (int bits = 330; bits<=450; bits+=10) {
			BigInteger num = TestsetGenerator.generate(1, bits, TestNumberNature.QUITE_HARD_SEMIPRIMES)[0];
			LOG.info("// " + bits + " bits:");
			LOG.info(num);
		}
	}
}
