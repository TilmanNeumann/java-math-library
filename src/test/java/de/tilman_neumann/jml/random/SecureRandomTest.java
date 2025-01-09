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
package de.tilman_neumann.jml.random;

import java.security.SecureRandom;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;

import static org.junit.Assert.assertNotEquals;

public class SecureRandomTest {

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}
	
	@Test
	public void secureRandomSequenceIsNotReproducible() {
		SecureRandom secureRandom = new SecureRandom();
		long seed = 2487568097L;
		
		secureRandom.setSeed(seed);
		ArrayList<Integer> secureList1 = new ArrayList<>();
		for (int n=0; n<20; n++) {
			secureList1.add(secureRandom.nextInt());
		}

		secureRandom.setSeed(seed);
		ArrayList<Integer> secureList2 = new ArrayList<>();
		for (int n=0; n<20; n++) {
			secureList2.add(secureRandom.nextInt());
		}
		assertNotEquals(secureList1, secureList2);
	}
}
