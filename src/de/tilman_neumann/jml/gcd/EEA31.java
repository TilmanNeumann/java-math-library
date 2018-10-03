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
package de.tilman_neumann.jml.gcd;

/**
 * Extended Euclidean algorithm, following [Crandall/Pomerance 2005: "Prime numbers", algorithm 2.1.4].
 * 
 * This int implementation is quite fast.
 * 
 * @author Tilman Neumann
 */
public class EEA31 {

	public static class Result {
		/** if g==1 and y>0 then a = (1/x) mod y */
		public int a;
		/** if g==1 and y>0 then a = (1/y) mod x */
		public int b;
		/** gcd */
		public int g;
		
		public Result(int g, int a, int b) {
			this.a = a;
			this.b = b;
			this.g = g;
		}
	}
	
	/**
	 * Computes gcd, a = (1/x) mod y and b = (1/y) mod x.
	 * @param x
	 * @param y
	 * @return
	 */
	public Result computeAll(int x, int y) {
		// initialize
		int a = 1;
		int b = 0;
		int g = x;
		int u = 0;
		int v = 1;
		int w = y;
		// loop
		int q, tmp;
		while(w>0) {
			q = g/w; // floor
			// update
			tmp = a - q*u; a=u; u=tmp;
			tmp = b - q*v; b=v; v=tmp;
			tmp = g - q*w; g=w; w=tmp;
		}
		if (a<0) a = a+y; // TODO: change b, too?
		return new Result(g, a, b);
	}
	
	/**
	 * Computes only gcd and a = (1/x) mod y.
	 * @param x
	 * @param y
	 * @return
	 */
	public Result computeHalf(int x, int y) {
		// initialize
		int a = 1;
		int g = x;
		int u = 0;
		int w = y;
		// loop
		int tmp, rem;
		while (w != 0) {
			rem = g % w;
			tmp = a - (g/w)*u; // floor(g/w) !
			a = u; 
			u = tmp;
			g = w; 
			w = rem;
		}
		if (a<0) a = a+y;
		return new Result(g, a, -1);
	}
	
	/**
	 * Computes only the modular inverse a = (1/x) mod y.
	 * @param x
	 * @param y
	 * @return (1/x) mod y
	 */
	public int modularInverse(int x, int y) {
		// initialize
		int a = 1;
		int g = x;
		int u = 0;
		int w = y;
		// loop
		int tmp, rem;
		while (w != 0) {
			rem = g % w;
			tmp = a - (g/w)*u; // floor(g/w) !
			a = u; 
			u = tmp;
			g = w; 
			w = rem;
		}
		return a<0 ? a+y : a;
	}
}
