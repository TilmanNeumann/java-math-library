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
package de.tilman_neumann.jml.factor.base.matrixSolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.FactorException;
import de.tilman_neumann.jml.factor.base.congruence.AQPair;
import de.tilman_neumann.jml.factor.base.congruence.Smooth;

/**
 * A simple congruence equation system solver, doing Gaussian elimination.
 * Much faster than the first version due to improvements by Dave McGuigan.
 * 
 * @author Tilman Neumann, Dave McGuigan
 */
public class MatrixSolver_Gauss01b extends MatrixSolverBase02 {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(MatrixSolver_Gauss01b.class);
	
	@Override
	public String getName() {
		return "GaussSolver01b";
	}

	@Override
	public boolean sortIndices() {
		return true;
	}

	@Override
	protected void solve(List<Smooth> congruences, Map<Integer, Integer> factors_2_columnIndices) throws FactorException {
		// create matrix
		List<MatrixRow> rows = createMatrix(congruences, factors_2_columnIndices);
		// solve
		MatrixRow[] pivotRowsForColumns = new MatrixRow[factors_2_columnIndices.size()]; // storage for the pivot rows as they are found
		for(MatrixRow row : rows) {
			int columnIndex = row.getBiggestColumnIndex();
			while(columnIndex >= 0) {
				MatrixRow pivot = pivotRowsForColumns[columnIndex];
				if(pivot == null) {
					pivotRowsForColumns[columnIndex] = row;
					break;
				}
				
				// solution operations taken directly from MatrixSolver_Gauss01 ++
				row.addXor(pivot); // This operation should be fast!
				if (row.isNullVector()) {
					//LOG.debug("solve(): 5: Found null-vector: " + row);
					// Found null vector -> recover the set of AQ-pairs from its row index history
					HashSet<AQPair> totalAQPairs = new HashSet<AQPair>(); // Set required for the "xor"-operation below
					for (int rowIndex : row.getRowIndexHistoryAsList()) {
						Smooth congruence = congruences.get(rowIndex);
						// add the new AQ-pairs via "xor"
						congruence.addMyAQPairsViaXor(totalAQPairs);
					}
					// "return" the AQ-pairs of the null vector
					processNullVector(totalAQPairs);
					break;
				} else {
					// else: current row is not a null-vector, keep trying to reduce
					columnIndex = row.getBiggestColumnIndex();
				}
			}	
		}
	}

	/**
	 * Create the matrix.
	 * @param congruences
	 * @param factors_2_columnIndices
	 * @return
	 */
	private List<MatrixRow> createMatrix(List<Smooth> congruences, Map<Integer, Integer> factors_2_columnIndices) {
		ArrayList<MatrixRow> matrixRows = new ArrayList<MatrixRow>(congruences.size()); // ArrayList is faster than LinkedList, even with many remove() operations
		int rowIndex = 0;
		int numberOfRows = congruences.size();
		for (Smooth congruence : congruences) {
			// row entries = set of column indices where the congruence has a factor with odd exponent
			IndexSet columnIndicesFromOddExpFactors = createColumnIndexSetFromCongruence(congruence, factors_2_columnIndices);
			// initial row history = the current row index
			IndexSet rowIndexHistory = createRowIndexHistory(numberOfRows, rowIndex++);
			MatrixRow matrixRow = new MatrixRow(columnIndicesFromOddExpFactors, rowIndexHistory);
			matrixRows.add(matrixRow);
		}
		//LOG.debug("constructed matrix with " + matrixRows.size() + " rows and " + factors_2_columnIndices.size() + " columns");
		return matrixRows;
	}

	/**
	 * Create the set of matrix column indices from the factors that the congruence has with odd exponent.
	 * @param congruence
	 * @param factors_2_columnIndices
	 * @return set of column indices
	 */
	private IndexSet createColumnIndexSetFromCongruence(Smooth congruence, Map<Integer, Integer> factors_2_columnIndices) {
		Integer[] oddExpFactors = congruence.getMatrixElements();
		IndexSet columnIndexBitset = new IndexSet(factors_2_columnIndices.size());
		for (Integer oddExpFactor : oddExpFactors) {
			columnIndexBitset.add(factors_2_columnIndices.get(oddExpFactor));
		}
		return columnIndexBitset;
	}
	
	private IndexSet createRowIndexHistory(int numberOfRows, int rowIndex) {
		IndexSet rowIndexHistory = new IndexSet(numberOfRows);
		rowIndexHistory.add(rowIndex);
		//LOG.debug("numberOfRows=" + numberOfRows + ", rowIndex=" + rowIndex + " -> rowIndexHistory = " + rowIndexHistory);
		return rowIndexHistory;
	}
}
