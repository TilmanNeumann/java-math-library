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
package de.tilman_neumann.jml.sequence;

import java.math.BigInteger;

/**
 * Interface for integer sequence generators.
 * @author Tilman Neumann
 * 
 * @param T sequence element type
 */
public interface IntegerSequence<T> {
	/** The name of this sequence generator */
	public String getName();
	
	/**
	 * Reset sequence so that it starts again with its first element.
	 * @param N the number to factor
	 */
	public void reset(BigInteger N);
	
	/** Return the next integer */
	public T next();
}
