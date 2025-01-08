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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Java port of Marsaglia's xorshf generator.
 * @see https://stackoverflow.com/questions/1640258/need-a-fast-random-generator-for-c<br/><br/>
 * 
 * <strong>WARNING: This class is experimental!</strong> SpRand32 looks faster for numbers close to 31 bit.
 * Maybe I need to improve the conversion from C "unsigned long" to Java int (shifts, masks etc.).
 * 
 * @author Tilman Neumann
 */
public class Xorshf32 {
	private static final Logger LOG = LogManager.getLogger(Xorshf32.class);
	
	int x=123456789, y=362436069, z=521288629;
	
	public int nextInt(int max) {
		int t;
	    x ^= x << 16;
	    x ^= x >> 5;
	    x ^= x << 1;

	    t = x;
	    x = y;
	    y = z;
	    z = t ^ x ^ y;

	    return Math.abs(z%max);
	}
	
	public static void main(String[] args) {
		ConfigUtil.initProject();
		Xorshf32 rng = new Xorshf32();
		for (int i=0; i<1000; i++) {
			LOG.info("rand(" + i + ") = " + rng.nextInt(10));
		}
	}
}
