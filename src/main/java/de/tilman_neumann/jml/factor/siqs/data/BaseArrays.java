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
package de.tilman_neumann.jml.factor.siqs.data;

/**
 * Passive data structure bundling primes/powers, modular sqrts and logP-values.
 * 
 * Having a structure with several arrays of the same size is faster than having an array of a structure,
 * because the former permits to exploit AVX/SSE mechanisms in Java 8.
 * 
 * @author Tilman Neumann
 */
public class BaseArrays {
	/** The prime base */
	public int[] primes;
	/** exponents of primes */
	public int[] exponents;
	/** powers, e.g. powers[i] = primes[i]^exponents[i] */
	public int[] pArray;
	/** the modular sqrt's t with t^2==kN (mod p) for primes p, or t^2==kN (mod power) for powers */
	public int[] tArray;
	/** log-values of the primes or powers */
	public byte[] logPArray;
	/** 1/p for all primes/powers */
	public double[] pinvArrayD;
	/** 2^32 / p for all primes/powers */
	public long[] pinvArrayL;
	
	/**
	 * Constructor allocating all arrays.
	 * @param solutionsCount
	 */
	public BaseArrays(int solutionsCount) {
		primes = new int[solutionsCount];
		exponents = new int[solutionsCount];
		pArray = new int[solutionsCount];
		tArray = new int[solutionsCount];
		logPArray = new byte[solutionsCount];
		pinvArrayD = new double[solutionsCount];
		pinvArrayL = new long[solutionsCount];
	}
	
	/**
	 * Constructor setting all arrays.
	 * @param primes
	 * @param exponents
	 * @param powers
	 * @param tArray
	 * @param logPArray
	 * @param pinvArrayD array of 1/p values
	 * @param pinvArrayL array of 2^32/p values
	 */
	public BaseArrays(int[] primes, int[] exponents, int[] powers, int[] tArray, byte[] logPArray, double[] pinvArrayD, long[] pinvArrayL) {
		this.primes = primes;
		this.exponents = exponents;
		this.pArray = powers;
		this.tArray = tArray;
		this.logPArray = logPArray;
		this.pinvArrayD = pinvArrayD;
		this.pinvArrayL = pinvArrayL;
	}
}
