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
package de.tilman_neumann.jml.roots;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.util.ConfigUtil;

import static de.tilman_neumann.jml.base.BigIntConstants.*;

/**
 * Generator for good parameterizations of the exactSqrt() algorithm implemented in class SqrtExact.
 * 
 * @author Tilman Neumann
 */
/*
 * Implementation notes:
 * 1. We could speed up the algorithm if we store the quadratic residues for all m; however this leads to memory problems
 *    at fastMod values ~ 14 or 15 because it requires memory of the order of fastMod^2.
 *    Therefore we compute the square rests just when they are needed; the generation is fast enough.
 * 2. Principally we would need to generate only the mod-lists for each "fastMod"; the required square rests could be computed
 *    in the initializer of class SqrtExactTest. However, in Java the code with inlined generated square rest constants is notably faster.
 * 3. To stretch Java's limit of 64k code for methods/constructors/initializers a bit, the square rests for each mod
 *    are wrapped into a proper method.
*/
public class SquareRestAnalyzer {
	private static final Logger LOG = LogManager.getLogger(SquareRestAnalyzer.class);
	private static final Random RNG = new Random();
	private static final BigInteger ZERO = BigInteger.ZERO;
	private static final BigInteger ONE = BigInteger.ONE;
	
	/** number of test numbers */
	private static final int NCOUNT = 20000000;

	/**
	 * size of test numbers.
	 * 60 bits should be sufficient to have typically no square at all in the test set for all feasible NCOUNT values;
	 * then (almost always) only false positives are counted; and within the resolution 1/NCOUNT we get an accurate
	 * estimate of the percentage of possible squares that could still be excluded by a better mod-list.
	 */
	private static final int NBITS = 63;
	
	public static void main(String[] args) {
		ConfigUtil.initProject(); // set up logger
		
		// generate disclaimer
		LOG.info("// Code generated with class SquareRestAnalyzer");
		LOG.info("// @author Tilman Neumann");
		LOG.info(""); // spacer
		
		// generate test set
		LOG.info("// generate test set with " + NCOUNT + " numbers of " + NBITS + " bits size...");
		ArrayList<BigInteger> testSet = new ArrayList<BigInteger>();
		for (int i=0; i<NCOUNT; i++) {
			testSet.add(new BigInteger(NBITS, RNG));
		}
		LOG.info("// test set complete.");
		LOG.info(""); // spacer

		HashSet<Integer> m_generated = new HashSet<Integer>(); // moduli for which square rest sets have already been generated
		for (int fastModExp = 1; ; fastModExp++) {
			// start with fast square bit pattern test
			int fastMod = 1 << fastModExp;
			LOG.info("// fastModExp = " + fastModExp + " -> max mod = 2^" + fastModExp + " = " + fastMod + ":");
			ArrayList<BigInteger> bestPossibleSquares = filterPossibleSquares_withSquareBitPatternTest(testSet);
			float bestRatio = ((float) bestPossibleSquares.size()) / NCOUNT;
			LOG.info("// square bit pattern test -> ~ " + (bestRatio*100) + "% possible squares");
			
			// now add one mod after another
			ArrayList<Integer> bestMods = new ArrayList<Integer>();
			bestMods.add(fastMod); // though fastMod is not used anymore, we still add it here to keep indices compatible with older versions
			HashSet<Integer> bestSquareRests = null;
			for (int modCount=2; ; modCount++) {
				int bestNewM = -1;
				ArrayList<Integer> lastBestMods = bestMods; // we'll make copies of that below
				ArrayList<BigInteger> lastBestPossibleSquares = bestPossibleSquares;
				for (int m=2; m<fastMod; m++) {
					if (lastBestMods.contains(m)) continue;
					HashSet<Integer> squareRests = generateSquareRests(m); // hashing speeds up filterPossibleSquares()
					ArrayList<BigInteger> possibleSquares = filterPossibleSquares(m, squareRests, lastBestPossibleSquares);
					float ratio = ((float) possibleSquares.size()) / NCOUNT;
					if (ratio<bestRatio) {
						bestRatio = ratio;
						bestMods = new ArrayList<Integer>(lastBestMods); // copy
						bestMods.add(m);
						bestNewM = m;
						bestPossibleSquares = possibleSquares;
						bestSquareRests = squareRests;
					}
				}

				if (bestMods.size()<modCount) {
					String bestModsStr = bestMods.toString();
					bestModsStr = bestModsStr.substring(1, bestModsStr.length()-1); // remove parentheses
					LOG.info("static { mods["+fastModExp+"] = new int[] {" + bestModsStr + "}; }");
					break; // no more improvement
				}

				TreeSet<Integer> sortedSquareRests = new TreeSet<Integer>(bestSquareRests);
				String sortedSquareRestsStr = sortedSquareRests.toString();
				sortedSquareRestsStr = sortedSquareRestsStr.substring(1, sortedSquareRestsStr.length()-1); // remove parentheses
				if (!m_generated.contains(bestNewM)) { // reduce produced data
					LOG.info("static HashSet<Integer> squareRestsMod"+bestNewM+"() { return toHashSet(new int[] {" + sortedSquareRestsStr + "}); }");
					LOG.info("static { squareRests["+bestNewM+"] = squareRestsMod"+bestNewM+"(); }");
					m_generated.add(bestNewM);
				}
				LOG.info("// add mod "+ bestNewM + " (" + sortedSquareRests.size() + " square rests) -> ~ " + (bestRatio*100) + "% possible squares");
			}
			LOG.info(""); // spacer
		}
	}
	
	/**
	 * Generate all square rests % m.
	 * All square rests % m can be obtained from n^2 % m, n=0..floor(m/2).
	 * 
	 * @param m
	 * @return
	 */
	private static HashSet<Integer> generateSquareRests(int m) {
		BigInteger m_big = BigInteger.valueOf(m);
		BigInteger nMax = m_big.shiftRight(1);
		HashSet<Integer> rests = new HashSet<Integer>();
		for (BigInteger n=ZERO; n.compareTo(nMax)<=0; n=n.add(ONE)) {
			BigInteger nSquare = n.multiply(n); // square by design
			int rest = nSquare.mod(m_big).intValue();
			rests.add(rest);
		}
		return rests;
	}
	
	/**
	 * Filter all input numbers with the bit patterns allowed for squares.
	 * 
	 * @param potentialSquares
	 * @return subset of potentialSquares having the binary representation n = <something>001<2m 0's>
	 */
	private static ArrayList<BigInteger> filterPossibleSquares_withSquareBitPatternTest(ArrayList<BigInteger> potentialSquares) {
		ArrayList<BigInteger> possibleSquares = new ArrayList<BigInteger>();
		for (BigInteger n : potentialSquares) {
			// check contained power of 2
			int lsb = n.getLowestSetBit();
			if ((lsb & 1) == 1) continue; // n has an odd power of 2 -> no square
			
			// Thanks to Graeme Willoughby:
			// Write some number as (2^m)k ,where k is the odd number left after factoring out all the multiples of 2.
			// The square of this number is (2 ^(2m))k^2. Now we know that the square of an odd number is of the form 8N+1.
			// So the general form of any square number is (2^(2m))(8N+1) , with m,N=0,1,2...
			// -> If n is square, then it must have the binary representation n = <something>001<2m 0's>
			// -> If n is square, then it must satisfy (n/2^lsb) % 8 == 1.
			// This is a very fast test, and 86% of input values are determined to be non-square by this method,
			// which is everything you can get from examining bit patterns.
			if (n.shiftRight(lsb).and(I_7).intValue() != 1) continue;
			
			// n could be square
			possibleSquares.add(n);
		}
		return possibleSquares;
	}

	/**
	 * Filter all potential squares with the new modulus m.
	 * Only numbers with a rest % m that is a square rest are added to the result set.
	 * 
	 * @param m
	 * @param squareRests rests of squares % m
	 * @param potentialSquares
	 * @return possibleSquares remaining after checking tests % m
	 */
	private static ArrayList<BigInteger> filterPossibleSquares(int m, HashSet<Integer> squareRests, ArrayList<BigInteger> potentialSquares) {
		BigInteger bigM = BigInteger.valueOf(m);
		ArrayList<BigInteger> possibleSquares = new ArrayList<BigInteger>();
		for (BigInteger potentialSquare : potentialSquares) {
			// check if squareRests[m] contains rest = potentialSquare % m 
			int rest = potentialSquare.mod(bigM).intValue();
			if (squareRests.contains(rest)) {
				// rest is square rest -> potentialSquare could be square
				possibleSquares.add(potentialSquare);
			}
		}
		return possibleSquares;
	}
}
