package de.tilman_neumann;

import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.util.Random;

public class Main  {

    private static final Logger LOG = Logger.getLogger(Main.class);

    private static final Random random = new Random();

    public static void main(String[] args) {
        new Main().init();
    }

    private void init() {
        int length = 90;
        BigInteger a = BigInteger.probablePrime(length + 1, random);
        BigInteger b = BigInteger.probablePrime(length - 1, random);

        BigInteger input = a.multiply(b);
        LOG.info(a + ", " + b);
//        LOG.INFO(input, input.toString().length());
//        new QuadraticThieve(input).start();
    }
}
