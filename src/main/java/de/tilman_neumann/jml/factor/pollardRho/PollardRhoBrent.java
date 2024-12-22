/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018 Tilman Neumann - tilman.neumann@web.de
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
package de.tilman_neumann.jml.factor.pollardRho;

import static de.tilman_neumann.jml.base.BigIntConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.SecureRandom;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.FactorAlgorithm;
import de.tilman_neumann.util.ConfigUtil;
import de.tilman_neumann.util.SortedMultiset;

/**
 * Brents's improvement of Pollard's Rho algorithm, following [Richard P. Brent: An improved Monte Carlo Factorization Algorithm, 1980].
 * 
 * @author Tilman Neumann
 */
public class PollardRhoBrent extends FactorAlgorithm {
	private static final Logger LOG = Logger.getLogger(PollardRhoBrent.class);
	private static final SecureRandom RNG = new SecureRandom();

	private BigInteger N;

	@Override
	public String getName() {
		return "PollardRhoBrent";
	}
	
	@Override
	public BigInteger findSingleFactor(BigInteger N) {
		this.N = N;
		int Nbits = N.bitLength();
        BigInteger G, x, ys;
        do {
	        // start with random x0, c from [0, N-1]
        	BigInteger c = new BigInteger(Nbits, RNG);
            if (c.compareTo(N)>=0) c = c.subtract(N);
            BigInteger x0 = new BigInteger(Nbits, RNG);
            if (x0.compareTo(N)>=0) x0 = x0.subtract(N);
            BigInteger y = x0;

            // Brent: "The probability of the algorithm failing because q_i=0 increases, so it is best not to choose m too large"
        	final int m = 100;
        	int r = 1;
        	BigInteger q = I_1;
        	do {
	    	    x = y;
	    	    for (int i=1; i<=r; i++) {
	    	        y = addModN(y.multiply(y).mod(N), c);
	    	    }
	    	    int k = 0;
	    	    do {
	    	        ys = y;
	    	        final int iMax = Math.min(m, r-k);
	    	        for (int i=1; i<=iMax; i++) {
	    	            y = addModN(y.multiply(y).mod(N), c);
	    	            final BigInteger diff = x.compareTo(y) < 0 ? y.subtract(x) : x.subtract(y);
	    	            q = diff.multiply(q).mod(N);
	    	        }
	    	        G = q.gcd(N);
	    	        // if q==0 then G==N -> the loop will be left and restarted with new x0, c
	    	        k += m;
		    	    //LOG.info("r = " + r + ", k = " + k);
	    	    } while (k<r && G.equals(I_1));
	    	    r <<= 1;
	    	    //LOG.info("r = " + r + ", G = " + G);
	    	} while (G.equals(I_1));
	    	if (G.equals(N)) {
	    	    do {
	    	        ys = addModN(ys.multiply(ys).mod(N), c);
    	            final BigInteger diff = x.compareTo(ys) < 0 ? ys.subtract(x) : x.subtract(ys);
    	            G = diff.gcd(N);
	    	    } while (G.equals(I_1));
	    	    //LOG.info("G = " + G);
	    	}
        } while (G.equals(N));
		//LOG.debug("Found factor of " + N + " = " + factor);
        return G;
	}

	/**
	 * Addition modulo N, with <code>a, b < N</code>.
	 * @param a
	 * @param b
	 * @return (a+b) mod N
	 */
	private BigInteger addModN(BigInteger a, BigInteger b) {
		BigInteger sum = a.add(b);
		return sum.compareTo(N)<0 ? sum : sum.subtract(N);
	}
	
	/**
	 * Test.
	 * Some test numbers:<br/>
	 * 
	 * 5679148659138759837165981543 = 450469808245315337 * 466932157 * 3^3, takes ~250 ms<br/>
	 * 
	 * 54924524576914518357355679148659138759837165981543 = 1557629117554716582307318666440656471 * 35261619058033, takes ~ 4s<br/>
	 * 
	 * F6 = 18446744073709551617 = 274177 * 67280421310721 takes ~166ms<br/>
	 * 
	 * F7 = 2^128 + 1 = 340282366920938463463374607431768211457 = 5704689200685129054721 * 59649589127497217;
	 * Hard for Pollard-Rho-Brent (~ 173-414s) , easy for CFrac or ECM<br/>
	 * 
	 * F8 = 115792089237316195423570985008687907853269984665640564039457584007913129639937 = 1238926361552897 * 93461639715357977769163558199606896584051237541638188580280321, takes ~141s<br/>
	 * 
	 * 8225267468394993133669189614204532935183709603155231863020477010700542265332938919716662623
	 * = 1234567891 * 1234567907 * 1234567913 * 1234567927 * 1234567949 * 1234567967 * 1234567981 * 1234568021 * 1234568029 * 1234568047,
	 * takes about 300 ms<br/>
	 * 
	 * 101546450935661953908994991437690198927080333663460351836152986526126114727314353555755712261904130976988029406423152881932996637460315302992884162068350429 = 
	 * 123456789012419 * 123456789012421 * 123456789012437 * 123456789012439 * 123456789012463 * 123456789012521 *
	 * 123456789012523 * 123456789012533 * 123456789012577 * 123456789012629 * 123456789012637,
	 * takes about 147s<br/>
	 * 
	 * @param args ignored
	 */
	public static void main(String[] args) {
    	ConfigUtil.initProject();
    	
		while(true) {
			String input;
			try {
				LOG.info("Please insert the integer to factor:");
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				String line = in.readLine();
				input = line.trim();
				LOG.debug("Factoring " + input + "...");
			} catch (IOException ioe) {
				LOG.error("IO-error occurring on input: " + ioe.getMessage());
				continue;
			}
			
			long start = System.currentTimeMillis();
			BigInteger n = new BigInteger(input);
			SortedMultiset<BigInteger> result = new PollardRhoBrent().factor(n);
			LOG.info("Factored " + n + " = " + result.toString() + " in " + (System.currentTimeMillis()-start) + " ms");

		} // next input...
	}
}
