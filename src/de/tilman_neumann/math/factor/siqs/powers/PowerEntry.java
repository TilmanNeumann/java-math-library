/*
 * PSIQS 4.0 is a Java library for integer factorization, including a parallel self-initializing quadratic sieve (SIQS).
 * Copyright (C) 2018  Tilman Neumann (www.tilman-neumann.de)
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
package de.tilman_neumann.math.factor.siqs.powers;

/**
 * Auxiliary class that allows to get the powers sorted bottom-up by the power value.
 */
public class PowerEntry implements Comparable<PowerEntry> {
	public int p;
	public int exponent;
	public int power;
	public int t;
	public byte logPower;
	
	public PowerEntry(int p, int exponent, int power, int t, byte logPower) {
		this.p = p;
		this.exponent = exponent;
		this.power = power;
		this.t = t;
		this.logPower = logPower;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o==null || !(o instanceof PowerEntry)) return false;
		return power == ((PowerEntry)o).power;
	}
	
	@Override
	public int compareTo(PowerEntry other) {
		return power - other.power;
	}
}