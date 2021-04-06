/*
 * PSIQS 4.0 is a Java library for integer factorization, including a parallel self-initializing quadratic sieve (SIQS).
 * Copyright (C) 2018  Tilman Neumann (www.tilman-neumann.de)
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
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import de.tilman_neumann.jml.factor.FactorException;
import de.tilman_neumann.jml.factor.base.congruence.AQPair;
import de.tilman_neumann.jml.factor.base.congruence.Smooth;

/**
 * A congruence equation system solver doing Gaussian elimination in parallel.
 * 
 * @author David McGuigan (adapted from Tilman Neumann Gauss)
 */
public class MatrixSolver_PGauss extends MatrixSolverBase02 {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(MatrixSolver_PGauss.class);

	int solveThreads = 8;
	int lockBlock = 100;  // number of pivot rows locked at a time

	public MatrixSolver_PGauss() {
		super();	
	}

	public MatrixSolver_PGauss(int threads, int lockBlock) {
		super();
		this.solveThreads = threads;
		this.lockBlock = lockBlock;
	}

	@Override
	public String getName() {
		return "PGaussSolver("+solveThreads+","+lockBlock+")";
	}

		
	// the threads need access to these
	List<Smooth> congruences;  // input list of congruences, needed when a null vector is found
	List<MatrixRow> rows;      // input list of rows, needed when a null vector is found
	private MatrixRow[] pivotRowsForColumns; // storage for the pivot rows as they are found
	private ReentrantLock[] locks;  // for guarding writing to pivotRowsForColumns

	private volatile FactorException factorFound;  // needed to pass the exception back from the threads
	
	// simple ticket system to iterate over the rows through the threads
	private int nextRow = 0;	
	
	private synchronized int getNextRow() {
		return nextRow++;
	}
	
	// Main processing thread.
	// Takes a row from the matrix and iteratively applies the pivot rows. 
	// if it's the first row to be resolved down to a column, it becomes
	// the pivot row for further rows having that column
	private class PivotThread extends Thread {

		public PivotThread() {
			super();
		}
		

		public void process(MatrixRow row) throws FactorException {
			int columnIndex = row.getBiggestColumnIndex();
			while(columnIndex >= 0) {
				MatrixRow pivot = pivotRowsForColumns[columnIndex];
				if(pivot == null) {
					int lock = columnIndex/lockBlock;
					locks[lock].lock();
					try {
						pivot = pivotRowsForColumns[columnIndex];
						if(pivot == null) {
							pivotRowsForColumns[columnIndex] = row;
							return;
						}
					} finally {
						locks[lock].unlock();
					}
				}
				
				// solution operations taken directly from original MatrixSolver_Gauss01 ++
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
					return;
				} else {
					// else: current row is not a null-vector, keep trying to reduce
					columnIndex = row.getBiggestColumnIndex();
				}
			}	
		}

		@Override
		public void run() {
			
			while(factorFound == null) {
				int rowNum = getNextRow();
				if(rowNum>=rows.size()) {
					return;
				}
				try {
					process(rows.get(rowNum));
				} catch (FactorException e) {
					// trap the exception so run() is compatible with override
					factorFound = e;
				}
			}
		}		
	}

	
	@Override
	public boolean sortIndices() {
		return true;
	}

	@Override
	protected void solve(List<Smooth> congruences, Map<Integer, Integer> factors_2_columnIndices) throws FactorException {
		
		//set the global structures
		this.congruences = congruences;
		this.rows = createMatrix(congruences, factors_2_columnIndices); // create the matrix
		
		int numColumn = factors_2_columnIndices.size();
		pivotRowsForColumns = new MatrixRow[numColumn];
		
		int numLocks = (numColumn+lockBlock-1)/lockBlock;
		locks = new ReentrantLock[numLocks];
		for(int i=0; i<numLocks; i++) {
			locks[i] = new ReentrantLock();
		}

		//set up for iteration
		nextRow = 0;
		factorFound = null;
		
		// release the hounds!
		PivotThread[] threads = new PivotThread[solveThreads];
		for(int i=0; i<solveThreads; i++) {
			threads[i] = new PivotThread();
			threads[i].setName("S-"+i);
			threads[i].start();
		}

		// take a nap while the threads work
		for(PivotThread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(factorFound != null) {
			throw factorFound;  // pass on the exception found by the threads
		}
	}

	// below were taken directly from MatrixSolver_Gauss01
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

