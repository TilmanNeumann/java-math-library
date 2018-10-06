# java-math-library

This library is focused on number theory, but not necessarily limited to it.
Its intention is to collect basic math routines that may be of value to others.
It is based on PSIQS 4.0 and as such provides some pretty good methods for integer factorization. 
The factoring methods are used as well to implement a fast sumOfDivisors() function.
Other noteworthy things added on top of PSIQS 4.0 are sqrt(), nth_root(), ln() and exp() functions for BigDecimals,
a fast generator for the partitions of multipartite numbers, and - quite special -
implementations of smooth number sequences like CANs (colossally abundant numbers) and SHCNs (superior highly composite numbers).


## Releases

### v0.8 The first revision containing all the stuff I wanted to add initially.


## Getting Started

Clone the repository, create a plain Java project importing it, make sure that 'src' is the source folder of your project, and add the log4j- and junit-jars from the lib-folder to your classpath. 

There is no documentation and no support, so you should be ready to start exploring the source code.


## Authors

* **Tilman Neumann** - *Initial work*


## License

This project is licensed under the GPL 3 License - see the [License.txt](License.txt) file for details


## Credits

Big thanks to
* Dario Alpern for the permission to use his Block-Lanczos solver under GPL 3, and
* Graeme Willoughby for his great comments on the BigInteger algorithms in the SqrtInt, SqrtExact, Root and PurePowerTest classes.

More code used or adapted from other software:
* The method Factorial.withStartResult(...) was adapted from Johann Nepomuk Loefflmann's BigAl program, see http://www.jonelo.de

