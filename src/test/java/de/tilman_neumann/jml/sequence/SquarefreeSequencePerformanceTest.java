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
package de.tilman_neumann.jml.sequence;

import static de.tilman_neumann.jml.base.BigIntConstants.*;

import java.math.BigInteger;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Performance comparison of generators for squarefree numbers 1,2,3,5,6,7,10,11,13,...
 * @author Tilman Neumann
 */
public class SquarefreeSequencePerformanceTest {
	private static final Logger LOG = LogManager.getLogger(SquarefreeSequencePerformanceTest.class);
	private static final boolean DEBUG = false;
	private static final int NCOUNT = 1000000;
	
	private static void testBigIntegerImplementation() {
	   	SquarefreeSequence seqGen = new SquarefreeSequence(I_1);
		seqGen.reset();
		for (int i=1; i<=NCOUNT; i++) {
			BigInteger squarefree = seqGen.next();
			if (DEBUG) LOG.info("squarefree(" + i + ") = " + squarefree);
		}
	}

	private static void testLongImplementation() {
	   	SquarefreeSequence63 seqGen = new SquarefreeSequence63(1);
		seqGen.reset();
		for (int i=1; i<=NCOUNT; i++) {
			long squarefree = seqGen.next();
			if (DEBUG) LOG.info("squarefree(" + i + ") = " + squarefree);
		}
	}
	
	public static void main(String[] args) {
	   	ConfigUtil.initProject();
	   	
	   	long start = System.currentTimeMillis();
	   	testBigIntegerImplementation();
	   	LOG.info("BigInteger computation took " + (System.currentTimeMillis()-start) + " ms");
		
	   	start = System.currentTimeMillis();
	   	testLongImplementation();
		LOG.info("long computation took " + (System.currentTimeMillis()-start) + " ms");
	}
}
