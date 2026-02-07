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
package de.tilman_neumann.jml.primes.exact;

/**
 * Generation of primes for Lemire division.
 * 
 * @author Thilo Harich
 */
public class SmallPrimes {

    public static int[] generatePrimes(int limit) {
        boolean[] isComposite = getIsComposite(limit);

        int count = getCount(limit, isComposite);

        // Array mit Primzahlen füllen
        int[] primes = new int[count];
        int idx = 0;
        for (int i = 2; i <= limit; i++) {
            if (!isComposite[i]) primes[idx++] = i;
        }

        return primes;
    }

    private static int getCount(int limit, boolean[] isComposite) {
        int count = 0;
        for (int i = 2; i <= limit; i++) {
            if (!isComposite[i]) count++;
        }
        return count;
    }

    private static boolean[] getIsComposite(int limit) {
        boolean[] isComposite = new boolean[limit + 1];
        for (int i = 2; i * i <= limit; i++) {
            if (!isComposite[i]) {
                for (int j = i * i; j <= limit; j += i) {
                    isComposite[j] = true;
                }
            }
        }
        return isComposite;
    }
}
