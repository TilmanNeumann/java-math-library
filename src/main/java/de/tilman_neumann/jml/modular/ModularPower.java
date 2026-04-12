/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2026 Tilman Neumann - tilman.neumann@web.de
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

import static de.tilman_neumann.jml.base.BigIntConstants.*;

import java.math.BigInteger;

import de.tilman_neumann.jml.base.Int128;

/**
 * Modular power.
 * @author Tilman Neumann
 */
public class ModularPower {
	/**
	 * Computes a^b (mod c) for all-BigInteger arguments.</br></br>
	 * 
	 * <em>BigIntegers implementation is much faster!</em>
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
	 * Computes a^b (mod c) for <code>a</code> BigInteger, <code>b, c</code> long.
	 * This is about 4 times slower than BigInteger.modPow() with the same arguments.
	 * @param a
	 * @param b
	 * @param c
	 * @return a^b (mod c)
	 */
  	/* not public */ long modPow(BigInteger a, long b, long c) {
  		return modPowCore128(a.mod(BigInteger.valueOf(c)).longValue(), b, c);
  	}

	/**
	 * Computes a^b (mod c) for <code>a, b, c</code> long.
	 * This is about 4 times slower than BigInteger.modPow() with the same arguments.
	 * @param a
	 * @param b
	 * @param c
	 * @return a^b (mod c)
	 */
  	/* not public */ long modPow(long a, long b, long c) {
  		return modPowCore128(a % c, b, c);
  	}
  	
  	private long modPowCore128(long aModC, long b, long c) {
  		// if c is long, then the internal products need 128 bit
  		long modPow = 1;
  		while (b > 0) {
  			if ((b&1) == 1) {
  				Int128 prod = Int128.mul64Unsigned(modPow, aModC);
  				modPow = Int128.mod128by64Unsigned(prod.getHigh(), prod.getLow(), c);
  			}
  			Int128 aModCSquare = Int128.square64Unsigned(aModC);
  			aModC = Int128.mod128by64Unsigned(aModCSquare.getHigh(), aModCSquare.getLow(), c);
  			b >>= 1;
  		}
  		return modPow;
  	}

	/**
	 * Computes a^b (mod c) for <code>a</code> BigInteger, <code>b</code> long, <code>c</code> int. Very fast.
	 * @param a
	 * @param b
	 * @param c
	 * @return a^b (mod c)
	 */
  	public int modPow(BigInteger a, long b, int c) {
  		return modPowCore64(a.mod(BigInteger.valueOf(c)).longValue(), b, c);
  	}

	/**
	 * Computes a^b (mod c) for <code>a, b</code> long, <code>c</code> int. Very fast.
	 * @param a
	 * @param b
	 * @param c
	 * @return a^b (mod c)
	 */
  	public int modPow(long a, long b, int c) {
  		return modPowCore64(a % c, b, c);
  	}

	/**
	 * Computes a^b (mod c) for <code>a</code> int, <code>b</code> long, <code>c</code> int. Very fast.
	 * @param a
	 * @param b
	 * @param c
	 * @return a^b (mod c)
	 */
  	public int modPow(int a, long b, int c) {
  		return modPowCore64(a % c, b, c);
  	}
  	
  	private int modPowCore64(long aModC, long b, long c) {
  		long modPow = 1;
  		while (b > 0) {
  			if ((b&1) == 1) modPow = (modPow * aModC) % c;
  			aModC = (aModC * aModC) % c;
  			b >>= 1;
  		}
  		return (int) modPow;
  	}
}
