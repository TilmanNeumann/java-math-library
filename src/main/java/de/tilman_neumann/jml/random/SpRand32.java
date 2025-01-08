/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2025 Tilman Neumann - tilman.neumann@web.de
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
package de.tilman_neumann.jml.random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * My Java port of the pseudo-random number generator from tinyEcm.c by Ben Buhrow.
 * 
 * @author Tilman Neumann
 */
public class SpRand32 {
	private static final Logger LOG = LogManager.getLogger(SpRand32.class);
	
	private static final boolean DEBUG = false;
	
	// seed
	//private long LCGSTATE = 65537 * new Random().nextInt();
	private long LCGSTATE = 4295098403L; // rng comparable with C version of TinyEcm

	public int nextInt(int lower, int upper) {
		if (DEBUG) LOG.debug("LCGSTATE=" + LCGSTATE);
		
		// fix rng for negative upper values
		long upperl = (long) upper;
		if (upperl<0) upperl += (1L<<32);
		long diff = upperl - lower;
		if (DEBUG) LOG.debug("lower=" + lower + ", upper=" + upperl + ", diff=" + diff);
		
		// advance the state of the LCG and return the appropriate result
		LCGSTATE = 6364136223846793005L * LCGSTATE + 1442695040888963407L;
		long LCGSTATE_shifted = LCGSTATE >>> 32;
		if (DEBUG) LOG.debug("LCGSTATE=" + LCGSTATE + ", LCGSTATE_shifted=" + LCGSTATE_shifted);
		
		double quot = (double)LCGSTATE_shifted / 4294967296.0; // dividend is 2^32
		double prod = diff * quot;
		int rand = (int)(0xFFFFFFFF & (long)prod); // (int)prod does not work for prod >= 2^31
		int result = lower + rand;
		if (DEBUG) LOG.debug("quot=" + quot + ", prod=" + prod + ", rand=" + rand + ", result=" + result);
		return result;
	}

	public int nextInt(int upper) {
		if (DEBUG) LOG.debug("LCGSTATE=" + LCGSTATE);
		
		// fix rng for negative upper values
		long upperl = (long) upper;
		if (upperl<0) upperl += (1L<<32);
		if (DEBUG) LOG.debug("upper=" + upperl);
		
		// advance the state of the LCG and return the appropriate result
		LCGSTATE = 6364136223846793005L * LCGSTATE + 1442695040888963407L;
		long LCGSTATE_shifted = LCGSTATE >>> 32;
		if (DEBUG) LOG.debug("LCGSTATE=" + LCGSTATE + ", LCGSTATE_shifted=" + LCGSTATE_shifted);
		
		double quot = (double)LCGSTATE_shifted / 4294967296.0; // dividend is 2^32
		double prod = upperl * quot;
		int rand = (int)(0xFFFFFFFF & (long)prod); // (int)prod does not work for prod >= 2^31
		int result = rand;
		if (DEBUG) LOG.debug("quot=" + quot + ", prod=" + prod + ", rand=" + rand + ", result=" + result);
		return result;
	}
}
