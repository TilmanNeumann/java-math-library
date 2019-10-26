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
package de.tilman_neumann.jml.modular;

import java.math.BigInteger;

import static de.tilman_neumann.jml.base.BigIntConstants.I_0;
import static de.tilman_neumann.jml.base.BigIntConstants.I_1;

/**
 * Modular power.
 * @author Tilman Neumann
 */
public class ModularPower {

	/**
	 * Montgomery computation of x1 ^ x2 mod m for all-BigInteger arguments.</br></br>
	 *
	 * <em>MontgomeryReduction implementation faster than library-based</em>.
	 *
	 * @param x1
	 * @param x2
	 * @param m
	 * @return x1^x2 (mod m)
	 */
	BigInteger modPow2(BigInteger x1, BigInteger x2, BigInteger m) {
		MontgomeryReduction.Montgomery mont = new MontgomeryReduction.Montgomery(m);
		BigInteger prod = mont.reduce(mont.rrm);
		BigInteger base = mont.reduce(x1.multiply(mont.rrm));
		BigInteger exp = x2;
		while (exp.bitLength()>0) {
			if (exp.testBit(0)) prod=mont.reduce(prod.multiply(base));
			exp = exp.shiftRight(1);
			base = mont.reduce(base.multiply(base));
		}
		return mont.reduce(prod);
	}
	/**
	 * Computes a^b (mod c) for all-BigInteger arguments.</br></br>
	 * 
	 * <em>BigIntegers implementation is much faster!</em>.
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @return a^b (mod c)
	 */
  	/* not public */ BigInteger modPow(BigInteger a, BigInteger b, BigInteger c) {
  		BigInteger modPow = I_1;
  		while (b.compareTo(I_0) > 0) {
  			if ((b.intValue() & 1) == 1) { // oddness test needs only the lowest bit
  				modPow = modPow.multiply(a).mod(c);
  			}
  			a = a.multiply(a).mod(c);
  			b = b.shiftRight(1);
  		}
  		return modPow;
  	}
  	
	/**
	 * Computes a^b (mod c) for <code>a</code> BigInteger, <code>b, c</code> int. Very fast.
	 * @param a
	 * @param b
	 * @param c
	 * @return a^b (mod c)
	 */
  	public int modPow(BigInteger a, int b, int c) {
  		// products need long precision
  		long modPow = 1;
  		long aModC = a.mod(BigInteger.valueOf(c)).longValue();
  		while (b > 0) {
  			if ((b&1) == 1) modPow = (modPow * aModC) % c;
  			aModC = (aModC * aModC) % c;
  			b >>= 1;
  		}
  		return (int) modPow;
  	}
  	
	/**
	 * Computes a^b (mod c) for all-int arguments. Very fast.
	 * @param a
	 * @param b
	 * @param c
	 * @return a^b (mod c)
	 */
  	public int modPow(int a, int b, int c) {
  		// products need long precision
  		long modPow = 1;
  		long aModC = a % c;
  		while (b > 0) {
  			if ((b&1) == 1) modPow = (modPow * aModC) % c;
  			aModC = (aModC * aModC) % c;
  			b >>= 1;
  		}
  		return (int) modPow;
  	}
}
