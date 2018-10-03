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
package de.tilman_neumann.jml.factor.squfof;

import static de.tilman_neumann.jml.base.BigIntConstants.*;

import java.math.BigInteger;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.FactorAlgorithmBase;
import de.tilman_neumann.jml.sequence.IntegerSequence;
import de.tilman_neumann.jml.sequence.SquarefreeSequence;

/**
 * Shanks' SQUFOF algorithm, 63-bit version.<br/>
 * Implemented according to <link>http://en.wikipedia.org/wiki/Shanks'_square_forms_factorization</link>.
 * 
 * Final choice with self-initialization of parameters.
 * Stopping criterion: after a maximum number of iterations.
 * 
 * @author Tilman Neumann
 */
public class SquFoF63 extends FactorAlgorithmBase {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(SquFoF63.class);

	// input
	private BigInteger N, kN;
	private long floor_sqrt_kN;
	
	// maximum number of iterations
	private int maxI;

	@Override
	public String getName() {
		return "SquFoF63";
	}
	
	public long findSingleFactor(long N) {
		return findSingleFactor(BigInteger.valueOf(N)).longValue();
	}

	/**
	 * Find a factor of the given composite N.</br></br>
	 * <em>Warning:</em> This method will not return when called with a prime argument.
	 * @param N composite integer
	 * @return factor of N
	 */
	public BigInteger findSingleFactor(BigInteger N) {
		this.N = N;
		// best parameters found by experiments:
		// multiplier = 1680 always, and stopMult approximated piecewise:
		float stopMult;
		int bits = N.bitLength();
		if      (bits < 34) stopMult = 1.92F + (33-bits)*0.06F;
		else if (bits < 50) stopMult = 1.1F  + (50-bits)*0.035F;
		else if (bits < 70) stopMult = 1.1F  + (bits-50)*0.0075F;
		else /* bits>=70 */ stopMult = 1.25F + (bits-70)*0.062F;
		// max iterations: there is no need to account for k, because expansions of smooth kN are typically not longer than those for N.
		this.maxI = (int) (stopMult * Math.pow(N.doubleValue(), 0.2)); // stopMult * (5.th root of N)
		// sequence: for each new N start again with the first k
		IntegerSequence<BigInteger> kSequence = new SquarefreeSequence(1680);
		kSequence.reset(N);
		//int iterations=0;
		while (true) {
			// get a new k, return immediately if kN is square
			this.kN = kSequence.next().multiply(N);
			// sqrt(double) is much faster than BigInteger.sqrt() but the class cast may be wrong for some N >= 54 bit
			floor_sqrt_kN = (long) Math.sqrt(kN.doubleValue());
			BigInteger sqrt_kN_big = BigInteger.valueOf(floor_sqrt_kN);
			long diff = kN.subtract(sqrt_kN_big.multiply(sqrt_kN_big)).longValue();
			if (diff==0) return N.gcd(sqrt_kN_big);
			while (diff<0) {
				// floor_sqrt_kN was too big, diff too small -> compute correction.
				// a loop seems to pay out, thus the error term may often be bigger than 1.
				floor_sqrt_kN--;
				diff += (floor_sqrt_kN<<1) + 1;
			}
			// search square Q_i
			BigInteger factor = test(diff);
			if (factor!=null) {
				//if (iterations>1000) LOG.debug(getName() + ": Found factor after " + iterations + " k-values");
				return factor;
			}
			//iterations++;
		}
	}

	// inlining this method makes hardly a difference (100ms of 27s for a 81 but number)
	protected BigInteger test(long Q_ip1) {
		// initialization for first iteration step
		int i = 0;
		long P_im1 = 1;
		long P_i = floor_sqrt_kN;
		long Q_i = 1;
		
		// first iteration step
		while (true) {
			// [McMath 2004] points out that we have to look for a square Q_i at some even i.
			// Here I test Q_i+1, so I have to look for square Q_i+1 at odd i!
			if ((i&1)==1) {
				long Q_ip1_sqrt = (long) Math.sqrt(Q_ip1);
				if (Q_ip1_sqrt*Q_ip1_sqrt==Q_ip1) {
					// Q_i+1 is square -> do reverse iteration
					BigInteger factor = reverseIteration(P_i, Q_ip1_sqrt);
					if (factor!=null) return factor; // if factor is null we try another k
				}
			}
		
			// exit ?
			if (++i==maxI) return null;
			// keep values from last round
			P_im1 = P_i;
			long Q_im1 = Q_i;
			Q_i = Q_ip1;
			// compute next values
			long b_i = (floor_sqrt_kN + P_im1)/Q_i; // floor(rational result)
			P_i = b_i*Q_i - P_im1;
			Q_ip1 = Q_im1 + b_i*(P_im1-P_i);
		}
	}
	
	private BigInteger reverseIteration(long found_P, long found_Q_sqrt) {
		// initialization for second iteration step
		int i = 0;
		long b_i = (floor_sqrt_kN-found_P)/found_Q_sqrt; // floor(rational result)
		long P_i = b_i*found_Q_sqrt + found_P;
		long Q_i = found_Q_sqrt;
		// computing Q_ip1 needs BigInteger precision at every detail!
		BigInteger P_i_big = BigInteger.valueOf(P_i);
		long Q_ip1 = kN.subtract(P_i_big.multiply(P_i_big)).divide(BigInteger.valueOf(found_Q_sqrt)).longValue();
		
		// second iteration step
		long P_im1, Q_im1;
		do {
			// exit ?
			if (++i==maxI) return null;
			// keep values from last round
			P_im1 = P_i;
			Q_im1 = Q_i;
			Q_i = Q_ip1;
			// compute next values
			b_i = (floor_sqrt_kN+P_im1)/Q_i; // floor(rational result)
			P_i = b_i*Q_i - P_im1;
			Q_ip1 = Q_im1 + b_i*(P_im1-P_i);
		} while (P_i != P_im1);
		
		// result
		BigInteger gcd = N.gcd(BigInteger.valueOf(P_i));
		return (gcd.compareTo(ONE)>0 && gcd.compareTo(N)<0) ? gcd : null;
	}
}
