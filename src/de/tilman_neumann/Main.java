package de.tilman_neumann;

import org.apache.log4j.Logger;

import java.math.BigInteger;
import java.util.Random;

import de.tilman_neumann.jml.factor.CombinedFactorAlgorithm;

public class Main {

    private static final Logger LOG = Logger.getLogger(Main.class);

//    private static final Random random = new Random();

    public static void main(String[] args) {
        new Main().init();
    }

    private void init() {
//        int length = 90;
//        BigInteger a = BigInteger.probablePrime(length + 1, random);
//        BigInteger b = BigInteger.probablePrime(length - 1, random);

//        BigInteger N = a.multiply(b);
//        LOG.info(a + ", " + b);
//        LOG.INFO(N, input.toString().length());

        CombinedFactorAlgorithm.main(new String[] {"-t", "6", "966983290915691193309978723256242679920691599725908954700676674631843021151"});

    }
}
//2019-10-28 09:41:52,644 INFO  PSIQSBase(298) [main]: PSIQS_U(Cmult=0.32, Mmult=0.37, qCount=10, maxQRestExponent=0,179, noPowers, solver02_BL, 6 threads):
//2019-10-28 09:41:52,671 INFO  PSIQSBase(299) [main]: Found factor 2166660942804222727904664493239497749 (121 bits) of N=966983290915691193309978723256242679920691599725908954700676674631843021151 in 55s, 399ms
//2019-10-28 09:41:52,716 INFO  PSIQSBase(320) [main]:     Approximate phase timings: powerTest=5ms, initN=73ms, createThreads=193ms, initPoly=5642ms, sieve=34795ms, tdiv=6442ms, cc=1203ms, solver=7194ms
//2019-10-28 09:41:52,718 INFO  PSIQSBase(321) [main]:     -> initPoly sub-timings: a-param=10ms, first b-param=2ms, filter prime base=62ms, first x-arrays=2057ms, next b-params=32ms, next x-arrays=3477ms
//2019-10-28 09:41:52,719 INFO  PSIQSBase(322) [main]:     -> sieve sub-timings: init=789ms, sieve=32176ms, collect=1829ms
//2019-10-28 09:41:52,719 INFO  PSIQSBase(323) [main]:     -> tdiv sub-timings: AQ=147ms, pass1=4601ms, pass2=530ms, primeTest=1051ms, factor=110ms

//2019-10-28 12:23:25,826 INFO  PSIQSBase(297) [main]: PSIQS_U(Cmult=0.32, Mmult=0.37, qCount=10, maxQRestExponent=0,179, noPowers, solver02_BL, 6 threads):
//2019-10-28 12:23:25,834 INFO  PSIQSBase(298) [main]: Found factor 446301159453293757389122758418041256099 (129 bits) of N=966983290915691193309978723256242679920691599725908954700676674631843021151 in 1m, 4s, 433ms
//2019-10-28 12:23:25,844 INFO  PSIQSBase(319) [main]:     Approximate phase timings: powerTest=7ms, initN=115ms, createThreads=280ms, initPoly=6487ms, sieve=40780ms, tdiv=7345ms, cc=1308ms, solver=8329ms
//2019-10-28 12:23:25,844 INFO  PSIQSBase(320) [main]:     -> initPoly sub-timings: a-param=12ms, first b-param=4ms, filter prime base=72ms, first x-arrays=2395ms, next b-params=39ms, next x-arrays=3964ms
//2019-10-28 12:23:25,845 INFO  PSIQSBase(321) [main]:     -> sieve sub-timings: init=932ms, sieve=37766ms, collect=2081ms
//2019-10-28 12:23:25,846 INFO  PSIQSBase(322) [main]:     -> tdiv sub-timings: AQ=173ms, pass1=5136ms, pass2=655ms, primeTest=1243ms, factor=137ms
//966983290915691193309978723256242679920691599725908954700676674631843021151 (250 bits) = 2166660942804222727904664493239497749 * 446301159453293757389122758418041256099 (factored in 1m, 4s, 559ms)

//2019-10-28 12:51:53,944 INFO  PSIQSBase(297) [main]: PSIQS_U(Cmult=0.32, Mmult=0.37, qCount=10, maxQRestExponent=0,179, noPowers, solver02_BL, 6 threads):
//2019-10-28 12:51:53,951 INFO  PSIQSBase(298) [main]: Found factor 2166660942804222727904664493239497749 (121 bits) of N=966983290915691193309978723256242679920691599725908954700676674631843021151 in 58s, 145ms
//2019-10-28 12:51:53,960 INFO  PSIQSBase(319) [main]:     Approximate phase timings: powerTest=6ms, initN=79ms, createThreads=181ms, initPoly=5786ms, sieve=36004ms, tdiv=6747ms, cc=1214ms, solver=8452ms
//2019-10-28 12:51:53,961 INFO  PSIQSBase(320) [main]:     -> initPoly sub-timings: a-param=10ms, first b-param=7ms, filter prime base=55ms, first x-arrays=2144ms, next b-params=50ms, next x-arrays=3519ms
//2019-10-28 12:51:53,961 INFO  PSIQSBase(321) [main]:     -> sieve sub-timings: init=851ms, sieve=33376ms, collect=1776ms
//2019-10-28 12:51:53,962 INFO  PSIQSBase(322) [main]:     -> tdiv sub-timings: AQ=149ms, pass1=4903ms, pass2=547ms, primeTest=1038ms, factor=108ms
//966983290915691193309978723256242679920691599725908954700676674631843021151 (250 bits) = 2166660942804222727904664493239497749 * 446301159453293757389122758418041256099 (factored in 58s, 234ms)