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
package de.tilman_neumann.jml.primes.exact;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;

import static de.tilman_neumann.jml.base.BigIntConstants.I_0;

public class SSOZJ5Runner {

	/**
	 * Run SSOZJ5.
	 * @param args ignored
	 */
	public static void main(String[] args) {
	   	while (true) {
			try {
				System.out.println("\nPlease enter the range in integer(s) [<start>] <stop>:");
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				String line = in.readLine();
				String[] splitted = line.split("\\s+");
				int argc = splitted!=null ? splitted.length : 0;
				BigInteger start, stop;
				if (argc == 0 || argc > 2) {
					System.err.println("Illegal input.");
					continue;
				}
				if (argc == 1) {
					start = I_0;
					stop = new BigInteger(splitted[0]);
				} else {
					start = new BigInteger(splitted[0]);
					stop = new BigInteger(splitted[1]);
				}

				SSOZJ5.twinprimes_ssoz(start, stop);
		        
			} catch (Exception e) {
				System.err.println("Error " + e);
			}
		}
	}
}
