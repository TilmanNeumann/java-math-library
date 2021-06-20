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
package de.tilman_neumann.jml.factor.siqs.sieve;

import java.math.BigInteger;

/**
 * A sieve hit that is a candidate to yield a smooth relation.
 * @author Tilman Neumann
 */
public class SmoothCandidate {
	/** The sieve location */
	public int x;
	/** Q(x)/a, where a is the a-parameter of the polynomial */
	public BigInteger QdivA;
	/** A(x) = d*a*x + b */
	public BigInteger A;
	
	public SmoothCandidate(int x) {
		this.x = x;
		this.QdivA = null;
		this.A = null;
	}

	public SmoothCandidate(int x, BigInteger QdivA, BigInteger A) {
		this.x = x;
		this.QdivA = QdivA;
		this.A = A;
	}
}
