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
package de.tilman_neumann.jml.factor.base.congruence;

import static de.tilman_neumann.jml.factor.base.GlobalFactoringOptions.*;

import java.util.Map;
import java.util.TreeMap;

import de.tilman_neumann.util.Multiset;
import de.tilman_neumann.util.SortedMultiset_BottomUp;

public class CongruenceCollectorReport {
	private int partialCount;
	private int smoothCount;
	private int[] smoothFromPartialCounts;
	private int[] partialCounts;
	private int perfectSmoothCount;
	private Multiset<Integer> qRestSizes;
	private Multiset<Integer> bigFactorSizes;
	private Multiset<Integer> bigFactorSizes4Smooth;
	private int partialWithPositiveQCount;
	private int smoothWithPositiveQCount;
	private Multiset<Integer>[] qRestSizes4SmoothFromLPCount;
	private Multiset<Integer>[] bigFactorSizes4SmoothFromLPCount;
	
	public CongruenceCollectorReport(int partialCount, int smoothCount, int[] smoothFromPartialCounts, int[] partialCounts, int perfectSmoothCount,
			                         Multiset<Integer> qRestSizes, Multiset<Integer> bigFactorSizes,
			                         int partialWithPositiveQCount, int smoothWithPositiveQCount,
			                         Multiset<Integer>[] qRestSizes4SmoothFromLPCount, Multiset<Integer>[] bigFactorSizes4SmoothFromLPCount) {
		
		this.partialCount = partialCount;
		this.smoothCount = smoothCount;
		this.smoothFromPartialCounts = smoothFromPartialCounts;
		this.partialCounts = partialCounts;
		this.perfectSmoothCount = perfectSmoothCount;
		this.qRestSizes = qRestSizes;
		this.bigFactorSizes = bigFactorSizes;
		this.partialWithPositiveQCount = partialWithPositiveQCount;
		this.smoothWithPositiveQCount = smoothWithPositiveQCount;
		this.qRestSizes4SmoothFromLPCount = qRestSizes4SmoothFromLPCount;
		this.bigFactorSizes4SmoothFromLPCount = bigFactorSizes4SmoothFromLPCount;
		this.bigFactorSizes4Smooth = aggregateCounts(bigFactorSizes4SmoothFromLPCount);
	}
	
	private Multiset<Integer> aggregateCounts(Multiset<Integer>[] multisetArray) {
		Multiset<Integer> aggregated = new SortedMultiset_BottomUp<Integer>();
		if (multisetArray != null) {
			for (int i=0; i<multisetArray.length; i++) {
				Multiset<Integer> multiset = multisetArray[i];
				if (multiset != null) {
					aggregated.addAll(multiset);
				}
			}
		}
		return aggregated;
	}
	
	public String getOperationDetails() {
		if (ANALYZE) {
			String smoothFromPartialsStr = smoothFromPartialCounts[0] + " from 1-partials";
			if (smoothFromPartialCounts[1]>0) smoothFromPartialsStr += ", " + smoothFromPartialCounts[1] + " involving 2-partials";
			if (smoothFromPartialCounts[2]>0) smoothFromPartialsStr += ", " + smoothFromPartialCounts[2] + " involving 3-partials";
			String partialsStr = partialCounts[0] + " 1-partials";
			if (partialCounts[1]>0) partialsStr += ", " + partialCounts[1] + " 2-partials";
			if (partialCounts[2]>0) partialsStr += ", " + partialCounts[2] + " 3-partials";
			return "found " + smoothCount + " smooth congruences (" + perfectSmoothCount + " perfect, " 
				   + smoothFromPartialsStr + ") and " + partialCount + " partials (" + partialsStr + ")";
		}
		// simple report
		return "found " + smoothCount + " smooth congruences and " + partialCount + " partials";
	}

	/**
	 * @param lpCount number of large primes in the partial that lead to a smooth congruence
	 * @return a string pointing out the required QRest bit sizes to find certain percentiles of all smooth congruences.
	 */
	public String getSmoothQRestPercentiles(int lpCount) {
		return "Required QRest sizes for smooth percentiles from " + lpCount + " large primes = " + computePercentiles(qRestSizes4SmoothFromLPCount[lpCount]);
	}

	/**
	 * @param lpCount number of large primes in the partial that lead to a smooth congruence
	 * @return a string pointing out the required big factor bit sizes to find certain percentiles of all smooth congruences.
	 */
	public String getSmoothBigFactorPercentiles(int lpCount) {
		return "Required big factor sizes for smooth percentiles from " + lpCount + " large primes = " + computePercentiles(bigFactorSizes4SmoothFromLPCount[lpCount]);
	}
	
	public String getPartialQRestPercentiles() {
		return "QRest sizes of collected partial percentiles: " + computePercentiles(qRestSizes);
	}

	public String getPartialBigFactorPercentiles() {
		return "Big factor sizes of collected partial percentiles: " + computePercentiles(bigFactorSizes);
	}

	private static TreeMap<Integer, Integer> computePercentiles(Multiset<Integer> bitsizeCounts) {
		int[] percentiles = new int[] {80, 90, 95, 98, 99};
		int totalCount = bitsizeCounts.totalCount();
		TreeMap<Integer, Integer> resultMap = new TreeMap<>();
		for (int i=0; i<percentiles.length; i++) {
			int requiredCount = (int) Math.ceil((totalCount * percentiles[i]) / 100.0);
			int count = 0;
			// factor sizes are sorted bottom-up
			for (int factorSize : bitsizeCounts.keySet()) {
				int sizeCount = bitsizeCounts.get(factorSize);
				count += sizeCount;
				if (count > requiredCount) {
					resultMap.put(percentiles[i], factorSize);
					break;
				}
			}
		}
		return resultMap;
	}
	
	/**
	 * @return a string pointing out how many factors>31bit contributed to collected partial and smooth relations.
	 */
	public String getNonIntFactorPercentages() {
		int totalPartialBigFactorCount = 0;
		int nonIntPartialBigFactorCount = 0;
		for (Map.Entry<Integer, Integer> entry : bigFactorSizes.entrySet()) {
			int size = entry.getKey();
			int count = entry.getValue();
			totalPartialBigFactorCount += count;
			if (size > 31) {
				nonIntPartialBigFactorCount += count;
			}
		}
		float partialPercentage = totalPartialBigFactorCount==0 ? 0 : (nonIntPartialBigFactorCount*100.0F) / totalPartialBigFactorCount;
		
		int totalSmoothBigFactorCount = 0;
		int nonIntSmoothBigFactorCount = 0;
		for (Map.Entry<Integer, Integer> entry : bigFactorSizes4Smooth.entrySet()) {
			int size = entry.getKey();
			int count = entry.getValue();
			totalSmoothBigFactorCount += count;
			if (size > 31) {
				nonIntSmoothBigFactorCount += count;
			}
		}
		float smoothPercentage = totalSmoothBigFactorCount==0 ? 0 : (nonIntSmoothBigFactorCount*100.0F) / totalSmoothBigFactorCount;
		return String.format("%.2f", smoothPercentage) + "% of smooths' big factors and " + String.format("%.2f", partialPercentage) + "% of partials' big factors are > 31 bit";
	}
	
	public String getPartialQSignCounts() {
		float partialWithPositiveQPercentage = partialWithPositiveQCount*100.0F / partialCount;
		return partialWithPositiveQCount + " partials (" + String.format("%.2f", partialWithPositiveQPercentage) + "%) had positive Q, " + (partialCount-partialWithPositiveQCount) + " partials (" + String.format("%.2f", 100-partialWithPositiveQPercentage) + "%) had negative Q";
	}
	
	public String getSmoothQSignCounts() {
		float smoothWithPositiveQPercentage = smoothWithPositiveQCount*100.0F / smoothCount;
		return smoothWithPositiveQCount + " smooths (" + String.format("%.2f", smoothWithPositiveQPercentage) + "%) had positive Q, " + (smoothCount-smoothWithPositiveQCount) + " smooths (" + String.format("%.2f", 100-smoothWithPositiveQPercentage) + "%) had negative Q";
	}
}
