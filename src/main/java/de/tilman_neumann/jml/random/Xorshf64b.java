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
 * Yet another <strong>experimental</strong> 64 bit random number generator computing longs from two consecutive 32 bit random numbers.
 * 
 * @author Tilman Neumann
 */
public class Xorshf64b {
	
	private Xorshf32 intGenerator = new Xorshf32();
	
	public long nextLong() {
		final long i1 = intGenerator.nextInt();
		final long i2 = intGenerator.nextInt();
	    return i1^2 + i2;
	}
	
	// TODO nextLong(upper)
	
	// TODO nextLong(lower, upper)
	
}
