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
package de.tilman_neumann.jml.factor.siqs.powers;

/**
 * Auxiliary class that allows to get the powers sorted bottom-up by the power value.
 */
public class PowerEntry implements Comparable<PowerEntry> {
	public int p;
	public int exponent;
	public int power;
	public int t;
	public byte logPower;
	public double pinvD;
	public long pinvL;
	
	public PowerEntry(int p, int exponent, int power, int t, byte logPower) {
		this.p = p;
		this.exponent = exponent;
		this.power = power;
		this.t = t;
		this.logPower = logPower;
		this.pinvD = 1.0 / power;
		this.pinvL = (1L<<32) / power;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o==null || !(o instanceof PowerEntry)) return false;
		return power == ((PowerEntry)o).power;
	}
	
	@Override
	public int hashCode() {
		return power;
	}
	
	@Override
	public int compareTo(PowerEntry other) {
		return power - other.power;
	}
}