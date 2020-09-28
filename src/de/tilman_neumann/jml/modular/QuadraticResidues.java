package de.tilman_neumann.jml.modular;

import java.util.ArrayList;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import de.tilman_neumann.util.ConfigUtil;

/**
 * Methods to generate quadratic residues or test for quadratic residuosity.
 * 
 * @author Tilman Neumann
 */
public class QuadraticResidues {
	
	private static final Logger LOG = Logger.getLogger(QuadraticResidues.class);

    /**
     * Return all quadratic residues modulo m, where m may be even, too.
     * Computed by brute force.
     * 
     * @param m
     * @return set of quadratic residues modulo m, sorted bottom up.
     */
    public static TreeSet<Long> getQuadraticResidues(long m) {
    	TreeSet<Long> quadraticResidues = new TreeSet<Long>();
    	for (long k=0; k<=m/2; k++) {
    		quadraticResidues.add(k*k % m);
    	}
    	return quadraticResidues;
    }
    
    /**
     * Get the quadratic residues of even "k" modulo m.
     * @param m
     * @return square residues generated from even k^2 modulo m
     */
    public static TreeSet<Long> getEvenQuadraticResidues(long m) {
    	TreeSet<Long> quadraticResidues = new TreeSet<Long>();
    	for (long k=0; k<=m/2; k+=2) {
    		quadraticResidues.add(k*k % m);
    	}
    	return quadraticResidues;
    }

	/**
	 * Test.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		ConfigUtil.initProject();
		TreeSet<Long> quadraticResiduesMod100 = getQuadraticResidues(100);
		LOG.info("m = 100 has " + quadraticResiduesMod100.size() + " quadratic residues: " + quadraticResiduesMod100);
		
		ArrayList<Integer> counts = new ArrayList<Integer>();
		ArrayList<Integer> evenCounts = new ArrayList<Integer>();
		
		for (int n=0; n<20; n++) {
			int m = 1<<n;
			
			TreeSet<Long> quadraticResiduesMod2PowN = getQuadraticResidues(m);
			LOG.info("m = " + m + " has " + quadraticResiduesMod2PowN.size() + " quadratic residues: " + quadraticResiduesMod2PowN);
			counts.add(quadraticResiduesMod2PowN.size());

			TreeSet<Long> evenQuadraticResiduesMod2PowN = getEvenQuadraticResidues(m);
			LOG.info("m = " + m + " has " + evenQuadraticResiduesMod2PowN.size() + " 'even' quadratic residues: " + evenQuadraticResiduesMod2PowN);
			evenCounts.add(evenQuadraticResiduesMod2PowN.size());
		}
		
		LOG.info("counts = " + counts);
		// A023105(n) = 1, 2, 2, 3, 4, 7, 12, 23, 44, 87, 172, 343, ...
		LOG.info("evenCounts = " + evenCounts);
		// a(n) = {1, 1} + A023105(n-2) = 1, 1, 1, 2, 2, 3, 4, 7, 12, 23, 44, 87, 172, 343, 684, 1367, 2732, 5463, 10924, 21847
	}

}
