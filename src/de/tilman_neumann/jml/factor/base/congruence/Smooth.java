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
package de.tilman_neumann.jml.factor.base.congruence;

import java.util.Set;

/**
 * A smooth congruence.
 * The matrix elements of a smooth congruence are the small factors appearing with odd exponent.
 *
 * @author Tilman Neumann
 */
public interface Smooth {

	/**
	 * Test if the Q of this smooth congruence is an exact square.
	 * Since smooth congruences can not have non-square large Q-factors, only the small Q-factors need to be checked.
	 * 
	 * @return true if Q is square
	 */
	boolean isExactSquare();

	/**
	 * @return set matrix elements. For a smooth relation these are the small factors appearing with odd exponent.
	 */
	Integer[] getMatrixElements();

	/**
	 * @return the set of AQPairs this smooth relation consists of
	 */
	Set<AQPair> getAQPairs();
	
	/**
	 * Add <code>this</code>'s AQPairs to the target set via xor.
	 * This operation permits to get around without creating new array objects
	 * for all those congruence sub-classes that represent just a single AQPair.
	 *
	 * @param targetSet
	 */
	void addMyAQPairsViaXor(Set<AQPair> targetSet);
}
