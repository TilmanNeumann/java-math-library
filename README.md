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

The factoring methods are used as well to implement a fast sumOfDivisors() function.
Other noteworthy parts of this library are sqrt(), nth_root(), ln() and exp() functions for BigDecimals.

More special contents are a fast generator for the partitions of multipartite numbers and 
implementations of smooth number sequences like CANs (colossally abundant numbers) and SHCNs (superior highly composite numbers).


## Releases

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

You will need Java 8 or higher for the project to compile.

There is no documentation and no support, so you should be ready to start exploring the source code.


## Remarks

The quadratic sieve is still lacking the integration of ECM when it would be useful.
So it will be quite efficient only for inputs having few small factors (in particular semiprimes),
but not when the number of small prime factors is large.


## Authors

 **Tilman Neumann**


## License

This project is licensed under the GPL 3 License - see the [LICENSE](LICENSE) file for details


## Credits

Big thanks to
* Dario Alpern for the permission to use his Block-Lanczos solver under GPL 3
* Graeme Willoughby for his great comments on the BigInteger algorithms in the SqrtInt, SqrtExact, Root and PurePowerTest classes
* Thilo Harich for a great collaboration and his immense improvements on the Lehman factoring method
* Ben Buhrow for his free, open source tinyecm.c and his comments on mersenneforum.org that helped a lot to improve the performance of my Java port

