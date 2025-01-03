/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2024 Tilman Neumann - tilman.neumann@web.de
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
package de.tilman_neumann.jml;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.logging.log4j.LogManager;

import de.tilman_neumann.jml.base.BigRational;
import de.tilman_neumann.util.ConfigUtil;

/**
 * Tests for harmonic and "hyper-harmonic" number computations.
 * 
 * @author Tilman Neumann
 */
public class HarmonicNumbersTest {
	private static final Logger LOG = LogManager.getLogger(HarmonicNumbersTest.class);

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}
	
	@Test
	public void testHarmonic() {
		String expected = "[1, 3/2, 11/6, 25/12, 137/60, 49/20, 363/140, 761/280, 7129/2520, 7381/2520, 83711/27720, 86021/27720]";
		List<BigRational> computedList = new ArrayList<>();
		for (int n=1; n<=20; n++) {
			BigRational h = HarmonicNumbers.harmonic(n);
			LOG.info("H(" + n + ") = " + h);
			if (n<=12) computedList.add(h);
		}
		assertEquals(expected, computedList.toString());
	}
	
	@Test
	public void testHyperHarmonic() {
    	int nMax = 10, rMax = 10;
    	for (int n=1; n<=nMax; n++) {
    		BigRational[] row = new BigRational[rMax];
        	for (int r=1; r<=rMax; r++) {
        		BigRational recurrent = HarmonicNumbers.hyperharmonic_recurrent(n,r);
        		BigRational closed = HarmonicNumbers.hyperharmonic_closedForm(n,r);
        		assertEquals(recurrent, closed);
        		row[r-1] = recurrent;
        	}
        	LOG.info("hyperharmonic(n=" + n + ", r=1.." + rMax + ") = " + Arrays.toString(row));
    	}
	}
	
	@Test
	public void testHarmonicPowers() {
    	int nMax = 10, rMax = 10;
    	for (int n=1; n<=nMax; n++) {
    		BigRational[] row = computeHarmonicPowerRow(n, rMax);
        	LOG.info("harmonicPower(n=" + n + ", r=1.." + rMax + ") = " + Arrays.toString(row));
    	}
    	// different, but interesting
    	
		BigRational[] row1 = computeHarmonicPowerRow(1, rMax);
    	assertEquals("[1, 1, 1, 1, 1, 1, 1, 1, 1, 1]", Arrays.toString(row1));
   	
		BigRational[] row2 = computeHarmonicPowerRow(2, rMax);
    	assertEquals("[3/2, 5/4, 9/8, 17/16, 33/32, 65/64, 129/128, 257/256, 513/512, 1025/1024]", Arrays.toString(row2));
    	
		BigRational[] row3 = computeHarmonicPowerRow(3, rMax);
    	assertEquals("[11/6, 49/36, 251/216, 1393/1296, 8051/7776, 47449/46656, 282251/279936, 1686433/1679616, 10097891/10077696, 60526249/60466176]", Arrays.toString(row3));
  	
		BigRational[] row4 = computeHarmonicPowerRow(4, rMax);
    	assertEquals("[25/12, 205/144, 2035/1728, 22369/20736, 257875/248832, 3037465/2985984, 36130315/35831808, 431733409/429981696, 5170139875/5159780352, 61978938025/61917364224]", Arrays.toString(row4));
	}
	
	private static final BigRational[] computeHarmonicPowerRow(int n, int rMax) {
		BigRational[] row = new BigRational[rMax];
    	for (int r=1; r<=rMax; r++) {
    		BigRational harmonicPower = HarmonicNumbers.harmonicPower(n,r);
    		row[r-1] = harmonicPower;
    	}
    	return row;
	}
}
