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

public class BigIntConstants {
	
	/** -1 */
	public static final BigInteger MINUS_ONE = BigInteger.ONE.negate();
	/**
	 * 0.
	 * UNDOCUMENTED_FEATURE: Some BigInteger methods return this object, so that we can use "==" for equality tests.
	 * TODO: divideAndRemainder() and which other methods?
	 */
	public static final BigInteger ZERO = BigInteger.ZERO;
	/**
	 * 1.
	 * TODO: Which methods return this constant?
	 */
	public static final BigInteger ONE = BigInteger.ONE;
	
	/** 2 */				public static final BigInteger TWO = BigInteger.valueOf(2);
	/** 3 */				public static final BigInteger THREE = BigInteger.valueOf(3);
	/** 4 */				public static final BigInteger FOUR = BigInteger.valueOf(4);
	/** 5 */				public static final BigInteger FIVE = BigInteger.valueOf(5);
	/** 6 */				public static final BigInteger SIX = BigInteger.valueOf(6);
	/** 7 */				public static final BigInteger SEVEN = BigInteger.valueOf(7);
	/** 8 */				public static final BigInteger EIGHT = BigInteger.valueOf(8);
	/** 9 */				public static final BigInteger NINE = BigInteger.valueOf(9);
	
	/**
	 * 10.
	 * TODO: Which methods return this constant?
	 */
	public static final BigInteger TEN = BigInteger.TEN;
	
	/** 11 */				public static final BigInteger ELEVEN = BigInteger.valueOf(11);
	/** 12 */				public static final BigInteger TWELVE = BigInteger.valueOf(12);
	/** 13 */				public static final BigInteger THIRTEEN = BigInteger.valueOf(13);
	/** 14 */				public static final BigInteger FOURTEEN = BigInteger.valueOf(14);
	/** 15 */				public static final BigInteger FIFTEEN = BigInteger.valueOf(15);
	/** 16 */				public static final BigInteger SIXTEEN = BigInteger.valueOf(16);
	/** 17 */				public static final BigInteger SEVENTEEN = BigInteger.valueOf(17);
	/** 18 */				public static final BigInteger EIGHTEEN = BigInteger.valueOf(18);
	/** 19 */				public static final BigInteger NINETEEN = BigInteger.valueOf(19);
	/** 20 */				public static final BigInteger TWENTY = BigInteger.valueOf(20);
	/** 21 */				public static final BigInteger TWENTYONE = BigInteger.valueOf(21);
	/** 22 */				public static final BigInteger TWENTYTWO = BigInteger.valueOf(22);
	/** 23 */				public static final BigInteger TWENTYTHREE = BigInteger.valueOf(23);
	/** 24 */				public static final BigInteger TWENTYFOUR = BigInteger.valueOf(24);
	/** 25 */				public static final BigInteger TWENTYFIVE = BigInteger.valueOf(25);
	/** 29 */				public static final BigInteger TWENTYNINE = BigInteger.valueOf(29);
	/** 30 */				public static final BigInteger THIRTY = BigInteger.valueOf(30);
	/** 32 */				public static final BigInteger THIRTYTWO = BigInteger.valueOf(32);
	/** 40 */				public static final BigInteger FOURTY = BigInteger.valueOf(40);
	/** 48 */				public static final BigInteger FOURTYEIGHT = BigInteger.valueOf(48);
	/** 50 */				public static final BigInteger FIFTY = BigInteger.valueOf(50);
	/** 60 */				public static final BigInteger SIXTY = BigInteger.valueOf(60);
	/** 64 */				public static final BigInteger SIXTYFOUR = BigInteger.valueOf(64);
	/** 100 */				public static final BigInteger HUNDRED = BigInteger.valueOf(100);
	/** 1000 */				public static final BigInteger THOUSAND = BigInteger.valueOf(1000);
	/** 10000 */			public static final BigInteger TEN_THOUSAND = BigInteger.valueOf(10000);
	/** 100000 */			public static final BigInteger HUNDRED_THOUSAND = BigInteger.valueOf(100000);
	/** a million */		public static final BigInteger MILLION = BigInteger.valueOf(1000000);
	/** ten million */		public static final BigInteger TEN_MILLION = BigInteger.valueOf(10000000);
	/** 100 million */		public static final BigInteger HUNDRED_MILLION = BigInteger.valueOf(100000000);
	/** max pow exponent */	public static final BigInteger MAX_EXPONENT = BigInteger.valueOf(999999999);
	/** 10^9 */				public static final BigInteger TENPOW9 = BigInteger.valueOf(1000000000);
	/** max integer */		public static final BigInteger MAX_INT = BigInteger.valueOf(Integer.MAX_VALUE);
	/** 10^10 */			public static final BigInteger TENPOW10 = BigInteger.valueOf(10000000000L);
	/** 10^11 */			public static final BigInteger TENPOW11 = BigInteger.valueOf(100000000000L);
	/** 10^12 */			public static final BigInteger TENPOW12 = BigInteger.valueOf(1000000000000L);
	/** 10^13 */			public static final BigInteger TENPOW13 = BigInteger.valueOf(10000000000000L);
	/** 10^14 */			public static final BigInteger TENPOW14 = BigInteger.valueOf(100000000000000L);
	/** 10^15 */			public static final BigInteger TENPOW15 = BigInteger.valueOf(1000000000000000L);
	/** 10^16 */			public static final BigInteger TENPOW16 = BigInteger.valueOf(10000000000000000L);
	/** 10^17 */			public static final BigInteger TENPOW17 = BigInteger.valueOf(100000000000000000L);
	/** 10^18 */			public static final BigInteger TENPOW18 = BigInteger.valueOf(1000000000000000000L);
	/** 10^19 */			public static final BigInteger TENPOW19 = TENPOW10.multiply(TENPOW9);
	/** 10^20 */			public static final BigInteger TENPOW20 = TENPOW10.multiply(TENPOW10);
}
