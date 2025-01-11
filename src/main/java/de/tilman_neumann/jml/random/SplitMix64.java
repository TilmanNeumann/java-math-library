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
 * 64 bit random number generator adapted from https://rosettacode.org/wiki/Pseudo-random_numbers/Splitmix64.
 * 
 * Very fast.
 * 
 * @author Tilman Neumann
 */
final class SplitMix64 {
	
	private static final long constant1 = 0x9e3779b97f4a7c15L;
	private static final long constant2 = 0xbf58476d1ce4e5b9L;
	private static final long constant3 = 0x94d049bb133111ebL;
    
	private long state;

	public SplitMix64() {
		state = 90832436910930047L; // just some semiprime, arbitrary choice
	}
	
	public SplitMix64(long aSeed) {
		state = aSeed;
	}
	
	public void seed(long aNumber) {
		state = aNumber;
	}
    
	public long nextLong() {
	    long z = (state += constant1);
	    z = (z ^ (z>>>30)) * constant2;
	    z = (z ^ (z>>>27)) * constant3;
	    return z ^ (z>>>31);
	}
}
