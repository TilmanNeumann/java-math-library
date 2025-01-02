/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2024 Tilman Neumann - tilman.neumann@web.de
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
package de.tilman_neumann.jml.squareSums;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Before;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Tests for the FourSquaresFinder.
 * @author Tilman Neumann
 */
public class FourSquaresFinderTest {

	@Before
	public void setup() {
		ConfigUtil.initProject();
	}

	@Test
	public void testRSA100() {
		BigInteger rsa100 = new BigInteger("1522605027922533360535618378132637429718068114961380688657908494580122963258952897654000350692006139"); // 330 bit
    	FourSquaresFinder fsf = new FourSquaresFinder();
    	fsf.find(rsa100);
    	BigInteger[] result = fsf.getSquareBases();
    	BigInteger x = result[0];
    	BigInteger y = result[1];
    	BigInteger z = result[2];
    	BigInteger w = result[3];
    	assertEquals(rsa100, x.multiply(x).add(y.multiply(y)).add(z.multiply(z)).add(w.multiply(w)));
	}
}
