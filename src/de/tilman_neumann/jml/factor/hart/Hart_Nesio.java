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
package de.tilman_neumann.jml.factor.hart;

import java.math.BigInteger;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.jml.gcd.Gcd63;
import de.tilman_neumann.util.ConfigUtil;

/**
 * Hart's one line factor algorithm with multipliers as proposed by Nesiolovskiy and Nesiolovskiy, see.
 * 
 * @see <a href="https://www.mersenneforum.org/showthread.php?t=24247">https://www.mersenneforum.org/showthread.php?t=24247</a>
 * @see <a href="https://arxiv.org/ftp/arxiv/papers/1903/1903.12449.pdf">https://arxiv.org/ftp/arxiv/papers/1903/1903.12449.pdf</a>
 * 
 * @author Tilman Neumann
 */
public class Hart_Nesio extends FactorAlgorithm {
	private static final Logger LOG = Logger.getLogger(Hart_Nesio.class);

	/** This is a constant that is below 1 for rounding up double values to long. */
	private static final double ROUND_UP_DOUBLE = 0.9999999665;

	private long m;
	private double[] sqrtkm;

	private final Gcd63 gcdEngine = new Gcd63();

	public Hart_Nesio(long m) {
		this.m = m;
		
		// Precompute sqrt(k*m) for all possible k
		final int kMax = 1<<21;
		sqrtkm = new double[kMax + 1];
		for (int k = 1; k <= kMax; k++) {
			sqrtkm[k] = Math.sqrt(k*m);
		}
	}
	
	@Override
	public String getName() {
		return "Hart_Nesio(" + m + ")";
	}

	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		return BigInteger.valueOf(findSingleFactor(N.longValue()));
	}

	public long findSingleFactor(long N) {
		long mN = m*N;
		double sqrtN = Math.sqrt(N);
		try {
			for (int k = 1; ; k++) {
				final long a = (long) (sqrtN * sqrtkm[k] + ROUND_UP_DOUBLE);
				final long test = a*a - k * mN;
				final long b = (long) Math.sqrt(test);
				if (b*b == test) {
					long gcd = gcdEngine.gcd(a+b, N);
					if (gcd>1 && gcd<N) return gcd;
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			LOG.error(getName() + ": Failed to factor N=" + N);
			return 1;
		}
	}
	
	/**
	 * Test.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		ConfigUtil.initProject();

		// These test number were too hard for previous versions:
		long[] testNumbers = new long[] {
				5640012124823L,
				7336014366011L,
				19699548984827L,
				52199161732031L,
				73891306919159L,
				112454098638991L,
				
				32427229648727L,
				87008511088033L,
				92295512906873L,
				338719143795073L,
				346425669865991L,
				1058244082458461L,
				1773019201473077L,
				6150742154616377L,

				44843649362329L,
				67954151927287L,
				134170056884573L,
				198589283218993L,
				737091621253457L,
				1112268234497993L,
				2986396307326613L,
				
				26275638086419L,
				62246008190941L,
				209195243701823L,
				290236682491211L,
				485069046631849L,
				1239671094365611L,
				2815471543494793L,
				5682546780292609L,
			};
		
		Hart_Nesio holf = new Hart_Nesio(1);
		for (long N : testNumbers) {
			long factor = holf.findSingleFactor(N);
			LOG.info("N=" + N + " has factor " + factor);
		}
	}
}
