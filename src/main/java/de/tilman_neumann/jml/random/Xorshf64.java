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

/**
 * <strong>Experimental</strong> 64 bit random number generator computing longs from three consecutive 32 bit random numbers.
 * 
 * @author Tilman Neumann
 */
public class Xorshf64 {
	
	private Xorshf32b intGenerator = new Xorshf32b();
	
	private long i1, i2;
	
	public Xorshf64() {
		i1 = intGenerator.nextInt();
		i2 = intGenerator.nextInt();
	}
	
	public long nextLong() {
		final long i3 = intGenerator.nextInt();
	    long result = ((i1*i2)<<1) + i3; // make upper bound 63 bit
	    i1 = i2;
	    i2 = i3;
	    return result;
	}
}
