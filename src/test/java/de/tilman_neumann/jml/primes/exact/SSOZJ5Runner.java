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
package de.tilman_neumann.jml.primes.exact;

import java.math.BigInteger;
import java.util.Scanner;

public class SSOZJ5Runner {

	/**
	 * Run SSOZJ5.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		Scanner userInput = new Scanner(System.in).useDelimiter("[,\\s+]");
		System.out.println("Please enter a space-separated range of integers: ");
		BigInteger stop = userInput.nextBigDecimal().toBigIntegerExact();
		BigInteger start = userInput.nextBigDecimal().toBigIntegerExact();
		userInput.close();

		if (stop.compareTo(start) < 0) {
			BigInteger tmp = start;
			start = stop;
			stop = tmp;
		}
		
		SSOZJ5.twinprimes_ssoz(start, stop);
	}
}
