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
package de.tilman_neumann.math.base.bigint;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static de.tilman_neumann.math.base.bigint.BigIntConstants.*;

/**
 * GCD implementations for BigIntegers.
 * 
 * By far the fastest implementation is BigInteger's gcd(), which uses a hybrid gcd with a very fast binary gcd implementation.
 * 
 * @author Tilman Neumann
 */
public class Gcd {

	/**
	 * Greatest common divisor of the given two arguments.
	 * Euclid's algorithm implementation with division.
	 * @param m
	 * @param n
	 * @return
	 */
	public BigInteger gcd_euclid_withDivision(BigInteger m, BigInteger n) {
		m = m.abs();
		n = n.abs();
		int mCmp1 = m.compareTo(ONE);
		int nCmp1 = n.compareTo(ONE);
		if (mCmp1>0 && nCmp1>0) {
			// initialize
			BigInteger g; // greater argument
			BigInteger s; // smaller argument
			if (m.compareTo(n) >= 0) {
				g = m;
				s = n;
			} else {
				g = n;
				s = m;
			}
			// iterate
			while (s.compareTo(ZERO)>0) {
				BigInteger[] dv = g.divideAndRemainder(s);
				g = s;
				s = dv[1]; // remainder
			}
			return g;
		}
		if (mCmp1<0) return n;
		if (nCmp1<0) return m;
		// else one argument is 1
		return ONE;
	}
	
	// very slow for arguments that are not very small
	public BigInteger gcd_euclid_withoutDivision(BigInteger m, BigInteger n) {
		m = m.abs();
		n = n.abs();
		int mCmp1 = m.compareTo(ONE);
		int nCmp1 = n.compareTo(ONE);
		if (mCmp1>0 && nCmp1>0) {
			int cmp = m.compareTo(n);
			while (cmp != 0) {
				//LOG.debug("m = " + m + ", n = " + n + ", cmp = " + cmp);
				if (cmp > 0) {
					m = m.subtract(n);
				} else {
					n = n.subtract(m);
				}
				cmp = m.compareTo(n);
			}
			return m;
		}
		if (mCmp1<0) return n;
		if (nCmp1<0) return m;
		// else one argument is 1
		return ONE;
	}
	
	/**
	 * Binary gcd implementation.
	 * @param m
	 * @param n
	 * @return gcd(m, n)
	 */
	public BigInteger gcd/*_binary*/(BigInteger m, BigInteger n) {
		m = m.abs();
		n = n.abs();
		int mCmp1 = m.compareTo(ONE);
		int nCmp1 = n.compareTo(ONE);
		if (mCmp1>0 && nCmp1>0) {
			int m_lsb = m.getLowestSetBit();
			int n_lsb = n.getLowestSetBit();
			int shifts = Math.min(m_lsb, n_lsb);
			m = m.shiftRight(m_lsb);
			n = n.shiftRight(n_lsb);
			// now m and n are odd
			//LOG.debug("m=" + m + ", n=" + n + ", g=" + g);
			while (m.signum() > 0) {
		    	BigInteger t = m.subtract(n).shiftRight(1);
		        if (t.signum()<0) {
		        	t = t.negate();
		        	n = t.shiftRight(t.getLowestSetBit());
		        } else {
		        	m = t.shiftRight(t.getLowestSetBit());
		        }
				//LOG.debug("m=" + m + ", n=" + n);
			}
			BigInteger gcd = n.shiftLeft(shifts);
			//LOG.debug("gcd=" + gcd);
			return gcd;
		}
		if (mCmp1<0) return n;
		if (nCmp1<0) return m;
		// else one argument is 1
		return ONE;
	}

	/**
	 * GCD of all arguments.
	 * @param arguments
	 * @return
	 */
	public static BigInteger gcd(Collection<BigInteger> arguments) {
		if (arguments==null || arguments.size()==0) { 
			return null;
		}

		Iterator<BigInteger> itr = arguments.iterator();
		BigInteger ret = itr.next();
		while(itr.hasNext()) {
			ret = ret.gcd(itr.next()); // fastest gcd
		}
		return ret;
	}

	/**
	 * Least common multiple of two arguments.
	 * @param a
	 * @param b
	 * @return LCM
	 */
	public static BigInteger lcm(BigInteger a, BigInteger b) {
		if (a.equals(ZERO) || b.equals(ZERO)) return ZERO;
		return a.multiply(b).divide(a.gcd(b));
	}
	
	/**
	 * Least common multiple of n arguments.
	 * @param arguments list of arguments
	 * @return LCM
	 */
	public static BigInteger lcm(List<BigInteger> arguments) {
		if (arguments==null || arguments.size()==0) { 
			return null;
		}

		Iterator<BigInteger> itr = arguments.iterator();
		BigInteger ret = itr.next();
		while(itr.hasNext()) {
			ret = lcm(ret, itr.next());
		}
		return ret;
	}
}
