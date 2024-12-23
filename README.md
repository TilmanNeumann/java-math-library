# java-math-library

This library is quite focused on number theory and particularly integer factorization, but not necessarily limited to it.

It provides some pretty fast implementations of various factoring algorithms, including the classes
* TDiv31Barrett: Trial division for numbers < 32 bit using long valued Barrett reduction
* Hart\_Fast2Mult: Highly optimized "Hart's one-line factorizer" for numbers <= 62 bit
* Lehman_Fast, Lehman_CustomKOrder: Fast Lehman implementations for numbers <= 62 bit
* SquFoF31Preload, SquFoF63: SquFoF implementations for numbers <= 52 rsp. 90 bit
* PollardRhoBrentMontgomery64_MHInlined: Highly optimized Pollard-Rho for numbers <= 62 bit.
* TinyEcm64_MHInlined: Highly optimized Java version of YaFu's tinyEcm.c for numbers <= 62 bit.
* CFrac63, CFrac: CFrac implementations working on longs rsp. BigIntegers internally.
* SIQS: Single-threaded self-initializing quadratic sieve (SIQS).
* PSIQS: Multi-threaded SIQS.
* PSIQS_U: Faster multi-threaded SIQS, using native memory access via sun.misc.Unsafe.

The factoring methods are used to implement a fast sumOfDivisors() function.

Another prominent subject in this library is prime generation and testing. For example, you can find
* a port of Kim Walisch's primesieve (basic for him, pretty fast for most others)
* SSOZJ, a fast twin prime sieve by Jabari Zakiya
* a BPSW probable prime test implementation, and
* state-of-the-art bound computations for the n.th prime and prime counting functions.

Other noteworthy parts of this library are
* sqrt(), nth_root(), ln() and exp() functions for BigDecimals.
* Gaussian integer and (Hurwitz) quaternion arithmetics including gcd's
* an implementation of [Pollack and Treviño's four squares finding algorithm](http://campus.lakeforest.edu/trevino/finding4squares.pdf)
* a fast generator for the partitions of multipartite numbers
* implementations of smooth number sequences like CANs (colossally abundant numbers) and SHCNs (superior highly composite numbers).


## Releases

* v1.3.1: Fixed two bugs that may lead to factoring failures when trying to factor arguments in the range 32..62 bit using class CombinedFactorAlgorithm.
  These bugs were introduced 2021-07-12 shortly after release 1.1, so they affect release 1.2 and 1.3.
* v1.3: Implemented Gaussian integer and quaternion arithmetics including gcd's, and a four-square finder using them.
* v1.2: Implemented SIQS with three large primes (but with the current parametrization, 3-partials are not found for N<=400 bit)
* v1.1: Faster sieve for large N, speedup close to factor 2 at 360 bit inputs. Improved Gaussian solvers (by Dave McGuigan), including a parallel Gaussian solver that outperforms Block-Lanczos until about 375 bit on a Ryzen 3900X with 20 threads.
From now on, <strong>Java 10</strong> is required!
* v1.0: Integrated and adjusted Dario Alpern's ECM in class CombinedFactorAlgorithm.
* v0.9.11: Added SSOZJ, a fast twin prime sieve; guard analysis code by final static booleans, so that the code is removed by the compiler when the boolean is set to false.
* v0.9.10: Added port of Ben Buhrow's tinyecm.c.
* v0.9.9.3: Added Hart's "one line factorizer"; simplified FactorAlgorithm type hierarchy.
* v0.9.9: Significantly faster trial division and Pollard-Rho.
* v0.9.8: Fixed bug in SquFoF for N not coprime with multipliers.
* v0.9.6: New Pollard-Rho-Brent implementation with Montgomery multiplication in longs;
  improved Lehman, trial division, EEA31, Gcd31.
* v0.9.5: Work on Lehman's algorithm, refactorings.
* v0.9.1: Implemented Peter Luschny's swinging prime factorial.
* v0.9: Thread-safe AutoExpandingPrimesArray, some refactorings.
* v0.8: The first revision containing all the stuff I wanted to add initially.


## Getting Started

Clone the repository and import it into your IDE. If you do not use the Maven build you might need to add the jars from the lib-folder to your classpath. 

You will need Java 8 for the project to compile, but <strong>it is strongly recommended to use Java 10 or above</strong> because that will enable faster intrinsics, e.g. for Math.multiplyHigh().

There is no documentation and no support, so you should be ready to start exploring the source code.


## Testing and comparing factoring algorithms

The main class for this purpose is class FactorizerTest.
Here you have many options:
* Choose the algorithms to run/compare by commenting in our out the appropriate lines in the constructor.
* Choose the number of test numbers, their bit range, step size etc. by setting the static variables `N_COUNT`, `START_BITS`, `INCR_BITS`, `MAX_BITS` and so on.
* Adjusting the static variables `TEST_NUMBER_NATURE` and `TEST_MODE` lets you choose the nature of test numbers (random, semi-prime, etc.) and if you want a complete factorization or only the first factor.

The amount of analysis and logging can be influenced by setting the static variables in the GlobalFactoringOptions interface. Typically one wants to have all those options set to false if `N_COUNT > 1`.

Note that for factoring very large numbers with multi-threaded algorithms like PSIQS, PSIQS_U, CombinedFactorAlgorithm or BatchFactorizer, the number of threads should not exceed the number of physical cores of your computer. The number size bound where this effect sets in seems to depend mostly on the L3 cache of your computer. The cause is explained well in [SMT disadvantages](https://en.wikipedia.org/wiki/Simultaneous_multithreading#Disadvantages).


## Factoring records

My current factoring record is the 400 bit (121 decimal digits) hard semiprime
<code>
1830579336380661228946399959861611337905178485105549386694491711628042180605636192081652243693741094118383699736168785617 
= 785506617513464525087105385677215644061830019071786760495463 * 2330444194315502255622868487811486748081284934379686652689159
</code><br />

Its factorization took less than 22 hours on a Ryzen 9 3900X with 12 sieve threads using jml 1.1. See [factoring report on mersenneforum.org](https://www.mersenneforum.org/showthread.php?p=583868#post583868).


## Authors

 **Tilman Neumann**


## License

This project is licensed under the GPL 3 License - see the [LICENSE](LICENSE) file for details


## Credits

Big thanks to
* Dario Alpern for the permission to use his Block-Lanczos solver under GPL 3
* Graeme Willoughby for his great comments on the BigInteger algorithms in the SqrtInt, SqrtExact, Root and PurePowerTest classes
* Thilo Harich for a great collaboration and his immense improvements on the Lehman factoring method
* Ben Buhrow for his free, open source [tinyecm.c](https://www.mersenneforum.org/showpost.php?p=521028&postcount=84) and his comments on mersenneforum.org that helped a lot to improve the performance of my Java port
* Dave McGuigan, who contributed a parallel Gaussian solver and even sped up my single-threaded Gaussian solver by a remarkable factor

Some (other) third-party software reused in this library:
* [Dario Alpern's ECM implementation](https://github.com/alpertron/calculators/blob/master/OldApplets/ecm.java),
refactored by Axel Kramer (and by myself)
* [Kim Walisch's primesieve](https://github.com/kimwalisch/primesieve) (ported by myself)
* [SSOZJ](https://gist.github.com/jzakiya/6c7e1868bd749a6b1add62e3e3b2341e), a fast twin prime sieve by Jabari Zakiya, [Java port](https://github.com/Pascal66/TwinsPrimesSieve) by Pascal Pechard



