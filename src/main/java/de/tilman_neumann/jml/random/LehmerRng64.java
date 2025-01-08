package de.tilman_neumann.jml.random;

/**
 * stub
 * 
 * @see https://en.wikipedia.org/wiki/Lehmer_random_number_generator
 */
public class LehmerRng64 {
	/*
	uint64_t next(void)
	{
		uint64_t result = state >> 64;
		// GCC cannot write 128-bit literals, so we use an expression
		const unsigned __int128 mult =
			(unsigned __int128)0x12e15e35b500f16e << 64 |
			0x2e714eb2b37916a5;
		state *= mult;
		return result;
	}
	*/
}
