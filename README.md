# java-math-library

This library is quite focused on number theory, but not necessarily limited to it.
It is based on PSIQS 4.0 and as such provides some pretty good methods for integer factorization.
If you are interested in factoring then have a look at the following classes:
* TDiv31Inverse
* TDiv63Inverse
* Hart\_Fast2Mult
* Lehman_CustomKOrder
* SquFoF31Preload
* SquFoF63
* PollardRhoBrentMontgomeryR64Mul63
* PollardRhoBrentMontgomery64
* PollardRhoBrent
* TinyEcm64
* CFrac63
* CFrac
* SIQS (single-threaded)
* PSIQS (multi-threaded)
* PSIQS_U (multi-threaded, using sun.misc.Unsafe)

The factoring methods are used to implement a fast sumOfDivisors() function.

Another prominent subject in this library is prime generation and testing. For example, you can find
* a port of Kim Walisch's primesieve (basic for him, pretty fast for most others)
* SSOZJ, a fast twin prime sieve by Jabari Zakiya
* a BPSW probable prime test implementation, and
* state-of-the-art bound computations for the n.th prime and prime counting functions.

Other noteworthy parts of this library are sqrt(), nth_root(), ln() and exp() functions for BigDecimals.

More special contents are a fast generator for the partitions of multipartite numbers and 
implementations of smooth number sequences like CANs (colossally abundant numbers) and SHCNs (superior highly composite numbers).


## Releases

* v1.0: Integrated and adjusted Dario Alpern's ECM in class CombinedFactorAlgorithm. From now on, <strong>Java 9</strong> is required!
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

Clone the repository, create a plain Java project importing it, make sure that 'src' is the source folder of your project, and add the jars from the lib-folder to your classpath. 

You will need <strong>Java 9</strong> or higher for the project to compile. (Java 9 is required to support Math.fma())

There is no documentation and no support, so you should be ready to start exploring the source code.


## Testing and comparing factoring algorithms

The main class for this purpose is class FactorizerTest.
Here you have many options:
* Choose the algorithms to run/compare by commenting in our out the appropriate lines in the constructor.
* Choose the number of test numbers, their bit range, step size etc. by setting the static variables `N_COUNT`, `START_BITS`, `INCR_BITS`, `MAX_BITS` and so on.
* Adjusting the static variables `TEST_NUMBER_NATURE` and `TEST_MODE` lets you choose the nature of test numbers (random, semi-prime, etc.) and if you want a complete factorization or only the first factor.

The amount of analysis and logging can be influenced by setting the static variables in the GlobalFactoringOptions interface. Typically one wants to have all those options set to false if `N_COUNT > 1`.


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



