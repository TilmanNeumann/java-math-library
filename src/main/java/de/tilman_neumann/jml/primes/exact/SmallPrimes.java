package de.tilman_neumann.jml.primes.exact;

/**
 * Generation of primes for Lemire division.
 * 
 * @author Thilo Harich
 */
public class SmallPrimes {

    public static int[] generatePrimes(int limit) {
        boolean[] isComposite = getIsComposite(limit);

        int count = getCount(limit, isComposite);

        // Array mit Primzahlen füllen
        int[] primes = new int[count];
        int idx = 0;
        for (int i = 2; i <= limit; i++) {
            if (!isComposite[i]) primes[idx++] = i;
        }

        return primes;
    }

    private static int getCount(int limit, boolean[] isComposite) {
        int count = 0;
        for (int i = 2; i <= limit; i++) {
            if (!isComposite[i]) count++;
        }
        return count;
    }

    private static boolean[] getIsComposite(int limit) {
        boolean[] isComposite = new boolean[limit + 1];
        for (int i = 2; i * i <= limit; i++) {
            if (!isComposite[i]) {
                for (int j = i * i; j <= limit; j += i) {
                    isComposite[j] = true;
                }
            }
        }
        return isComposite;
    }
}
