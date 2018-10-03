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
package de.tilman_neumann.math.factor;

import java.math.BigInteger;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.TimeUtil;
import de.tilman_neumann.util.Timer;

public class TestsetGeneratorTest {
	private static final Logger LOG = Logger.getLogger(TestsetGeneratorTest.class);

	public static void main(String[] args) {
		ConfigUtil.initProject();
		Timer timer = new Timer();
		int nCount = 100;
		for (int bits = 20; ; bits+=10) {
			long start = timer.capture();
			ArrayList<BigInteger> t1 = TestsetGenerator.generate(bits, nCount);
			long end = timer.capture();
			LOG.info("bits=" + bits + ": t1 took " + TimeUtil.timeDiffStr(start, end));
		}
	}
}
