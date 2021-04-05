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
 * Improved Gaussian solver, faster than the first version for any factor argument sizes.
 * 
 * The improvements for small N stem from the explicit control of the biggest column indices of all rows, permitting to find the pivot row without iterating over all rows.
 * For large N, the selection of pivot rows is the more important point, leading to sparser matrices during the solution procedure.
 * 
 * @author Tilman Neumann
 */
public class MatrixSolver_Gauss02 extends MatrixSolver {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(MatrixSolver_Gauss02.class);
	
	@Override
	public String getName() {
		return "GaussSolver02";
	}
	
	@Override
	protected void solve(List<Smooth> congruences, Map<Integer, Integer> factors_2_columnIndices) throws FactorException {
		
		// create matrix
		List<MatrixRow> rows = createMatrix(congruences, factors_2_columnIndices);
		
		// create sorted map from biggest columns to row indices (using an array having all keys seems to be faster for small N than TreeMap)
		int columnCount = factors_2_columnIndices.size();
		@SuppressWarnings("unchecked")
		ArrayList<Integer>[] biggestColumnIndex2RowIndices = new ArrayList[columnCount];
		for (int i=columnCount-1; i>=0; i--) {
			biggestColumnIndex2RowIndices[i] = new ArrayList<Integer>();
		}
		for (int rowIndex=0; rowIndex<rows.size(); rowIndex++) {
			MatrixRow row = rows.get(rowIndex);
			int biggestColumnIndex = row.getBiggestColumnIndex();
			List<Integer> rowIndices = biggestColumnIndex2RowIndices[biggestColumnIndex];
			rowIndices.add(rowIndex);
		}
		
		// solve
		for (int pivotColumnIndex=columnCount-1; pivotColumnIndex>=0; pivotColumnIndex--) {
			// Find pivot column index and row:
			List<Integer> rowIndices = biggestColumnIndex2RowIndices[pivotColumnIndex];
			if (rowIndices.size() > 0) {
				int pivotRowIndex = selectPivotRowIndex(rowIndices, rows);
				rowIndices.remove(Integer.valueOf(pivotRowIndex));
				MatrixRow pivotRow = rows.get(pivotRowIndex);
	
				// Do one Gaussian elimination step
				for (int rowIndex : rowIndices) {
					MatrixRow row = rows.get(rowIndex);
					// Add the pivot row to the current row in Z_2 ("xor"):
					// We can modify the current row object because its old state is not required anymore,
					// and because working on it does not affect the original congruences.
					row.addXor(pivotRow); // This operation should be fast!
					
					// update biggest column index map
					int updatedBiggestColumnIndex = row.getBiggestColumnIndex();
					if (updatedBiggestColumnIndex >= 0) {
						List<Integer> updatedRowIndices = biggestColumnIndex2RowIndices[updatedBiggestColumnIndex];
						updatedRowIndices.add(rowIndex);
					}
					
					if (row.isNullVector()) {
						// Found null vector -> recover the set of AQ-pairs from its row index history
						HashSet<AQPair> totalAQPairs = new HashSet<AQPair>(); // Set required for the "xor"-operation below
						for (int rowIndex2 : row.getRowIndexHistoryAsList()) {
							Smooth congruence = congruences.get(rowIndex2);
							// add the new AQ-pairs via "xor"
							congruence.addMyAQPairsViaXor(totalAQPairs);
						}
						// "return" the AQ-pairs of the null vector
						processNullVector(totalAQPairs);
					}
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
	
	/**
	 * Return the index of the row to become the next pivot row.
	 * @param choosableRowIndices
	 * @param rows
	 * @return index of the chosen row
	 */
	private int selectPivotRowIndex(List<Integer> choosableRowIndices, List<MatrixRow> rows) {
		int minWeight = Integer.MAX_VALUE;
		int minRowIndex = -1;
		for (int rowIndex : choosableRowIndices) {
			MatrixRow row = rows.get(rowIndex);
			int weight = row.getColumnCount();
			if (weight < minWeight) {
				minWeight = weight;
				minRowIndex = rowIndex;
			}
		}
		return minRowIndex;
	}
}
