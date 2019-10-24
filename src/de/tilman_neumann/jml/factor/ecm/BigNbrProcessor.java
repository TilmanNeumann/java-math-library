// Elliptic Curve Method (ECM) Prime Factorization
//
// Written by Dario Alejandro Alpern (Buenos Aires - Argentina)
// Last updated August 22nd, 2010. See http://www.alpertron.com.ar/ECM.HTM
//
// Based in Yuji Kida's implementation for UBASIC interpreter
//
// No part of this code can be used for commercial purposes without
// the written consent from the author. Otherwise it can be used freely
// except that you have to write somewhere in the code this header.
//
// Refactoring 2015 by Tilman Neumann (Germany)
// --------------------------------------------------------------------------
package de.tilman_neumann.jml.factor.ecm;

import java.io.Serializable;
import java.math.BigInteger;

public class BigNbrProcessor implements Serializable {
	private static final long serialVersionUID = -5633331032408512026L;

	public static final long DosALa32 = (long) 1 << 32;
	public static final long DosALa31 = (long) 1 << 31;
	public static final double dDosALa31 = (double) DosALa31;
	public static final double dDosALa62 = dDosALa31 * dDosALa31;
	
	public static final long Mi = 1000000000;

	public boolean BigNbrIsZero(int Nbr[], int NumberLength)
	{
	    for (int i = 0; i < NumberLength; i++)
	    {
	      if (Nbr[i] != 0)
	      {
	        return false;
	      }
	    }
	    return true;
	}

	public boolean BigNbrAreEqual(int Nbr1[], int Nbr2[], int NumberLength)
	{
	    for (int i = 0; i < NumberLength; i++)
	    {
	      if (Nbr1[i] != Nbr2[i])
	      {
	        return false;
	      }
	    }
	    return true;
	}

	public void ChSignBigNbr(int Nbr[], int NumberLength)
	{
	    int carry = 0;
	    for (int i = 0; i < NumberLength; i++)
	    {
	      carry = (carry >> 31) - Nbr[i];
	      Nbr[i] = carry & 0x7FFFFFFF;
	    }
	}

	public void AddBigNbr(int Nbr1[], int Nbr2[], int Sum[], int NumberLength)
	{
	    long carry = 0;
	    for (int i = 0; i < NumberLength; i++)
	    {
	      carry = (carry >> 31) + (long)Nbr1[i] + (long)Nbr2[i];
	      Sum[i] = (int)(carry & 0x7FFFFFFFL);
	    }
	}

	public void SubtractBigNbr(int Nbr1[], int Nbr2[], int Diff[], int NumberLength)
	{
	    long carry = 0;
	    for (int i = 0; i < NumberLength; i++)
	    {
	      carry = (carry >> 31) + (long)Nbr1[i] - (long)Nbr2[i];
	      Diff[i] = (int)(carry & 0x7FFFFFFFL);
	    }
	}

	public void AddBigNbr32(long Nbr1[], long Nbr2[], long Sum[], int NumberLength)
	{
	    long carry = 0;
	    for (int i = 0; i < NumberLength; i++)
	    {
	      carry = (carry >> 32) + Nbr1[i] + Nbr2[i];
	      Sum[i] = carry & 0xFFFFFFFFL;
	    }
	}

	public void SubtractBigNbr32(long Nbr1[], long Nbr2[], long Diff[], int NumberLength)
	{
	    long carry = 0;
	    for (int i = 0; i < NumberLength; i++)
	    {
	      carry = (carry >> 32) + Nbr1[i] - Nbr2[i];
	      Diff[i] = carry & 0xFFFFFFFFL;
	    }
	}

	/**
	 * Convert the long Nbr to BigNbr Out.
	 * @param Nbr
	 * @param Out
	 * @param NumberLength
	 */
	public void LongToBigNbr(long Nbr, int Out[], int NumberLength)
	{
	    int i;
	
	    Out[0] = (int)(Nbr & 0x7FFFFFFF);
	    Out[1] = (int)((Nbr >> 31) & 0x7FFFFFFF);
	    for (i = 2; i < NumberLength; i++)
	    {
	      Out[i] = (Nbr < 0 ? 0x7FFFFFFF : 0);
	    }
	}

	public void MultBigNbr(int Nbr1[], int Nbr2[], int Prod[], int NumberLength)
	{
	    long MaxUInt = 0x7FFFFFFFL;
	    long carry, Pr;
	    int i, j;
	    carry = Pr = 0;
	    for (i = 0; i < NumberLength; i++)
	    {
	      Pr = carry & MaxUInt;
	      carry >>>= 31;
	      for (j = 0; j <= i; j++)
	      {
	        Pr += (long)Nbr1[j] * (long)Nbr2[i - j];
	        carry += (Pr >>> 31);
	        Pr &= MaxUInt;
	      }
	      Prod[i] = (int)Pr;
	    }
	}

	public void MultBigNbrByLong(int Nbr1[], long Nbr2, int Prod[], int NumberLength)
	{
	    long MaxUInt = 0x7FFFFFFFL;
	    long Pr;
	    int i;
	    Pr = 0;
	    for (i = 0; i < NumberLength; i++)
	    {
	      Pr = (Pr >> 31) + Nbr2 * (long)Nbr1[i];
	      Prod[i] = (int)(Pr & MaxUInt);
	    }
	}

	public long BigNbrModLong(int Nbr1[], long Nbr2, int NumberLength)
	{
	    int i;
	    long Rem = 0;
	
	    for (i = NumberLength - 1; i >= 0; i--)
	    {
	      Rem = ((Rem << 31) + (long)Nbr1[i]) % Nbr2;
	    }
	    return Rem;
	}

	public void AddBigNbrModN(int Nbr1[], int Nbr2[], int Sum[], int TestNbr[], int NumberLength)
	{
	    long MaxUInt = 0x7FFFFFFFL;
	    long carry = 0;
	    int i;
	
	    for (i = 0; i < NumberLength; i++)
	    {
	      carry = (carry >> 31) + (long)Nbr1[i] + (long)Nbr2[i] - (long)TestNbr[i];
	      Sum[i] = (int)(carry & MaxUInt);
	    }
	    if (carry < 0)
	    {
	      carry = 0;
	      for (i = 0; i < NumberLength; i++)
	      {
	        carry = (carry >> 31) + (long)Sum[i] + (long)TestNbr[i];
	        Sum[i] = (int)(carry & MaxUInt);
	      }
	    }
	}

	public void SubtractBigNbrModN(int Nbr1[], int Nbr2[], int Diff[],
	                                 int TestNbr[], int NumberLength)
	{
	    long MaxUInt = 0x7FFFFFFFL;
	    long carry = 0;
	    int i;
	
	    for (i = 0; i < NumberLength; i++)
	    {
	      carry = (carry >> 31) + (long)Nbr1[i] - (long)Nbr2[i];
	      Diff[i] = (int)(carry & MaxUInt);
	    }
	    if (carry < 0)
	    {
	      carry = 0;
	      for (i = 0; i < NumberLength; i++)
	      {
	        carry = (carry >> 31) + (long)Diff[i] + (long)TestNbr[i];
	        Diff[i] = (int)(carry & MaxUInt);
	      }
	    }
	}

	/**
	 * Converts a BigNbr in 31-bit encoding into the same number in 32-bit encoding.
	 * 
	 * NumberLength must be less or equal the size of the 32-bit number array.
	 * 
	 * @param nbr31bits
	 * @param nbr32bits
	 * @param NumberLength
	 */
	public void Convert31To32Bits(int[] nbr31bits, long[] nbr32bits, int NumberLength)
	{
	    int i, j, k;
	    i = 0;
	    for (j = -1; j < NumberLength; j++)
	    {
	      k = i%31;
	      if (k == 0)
	      {
	        j++;
	      }
	      if (j == NumberLength)
	      {
	        break;
	      }
	      if (j == NumberLength-1)
	      {
	        nbr32bits[i] = nbr31bits[j] >> k;
	      }
	      else
	      {
	        nbr32bits[i] = ((nbr31bits[j] >> k) |
	                        (nbr31bits[j+1] << (31-k))) & 0xFFFFFFFFL;
	      }
	      i++;
	    }
	    for (; i<NumberLength; i++)
	    {
	      nbr32bits[i] = 0;
	    }
	}

	/**
	 * Converts a BigNbr in 32-bit encoding into the same number in 31-bit encoding.
	 * 
	 * NumberLength must be less the size of the 32-bit number array.
	 * 
	 * @param nbr32bits
	 * @param nbr31bits
	 * @param NumberLength
	 */
	public void Convert32To31Bits(long [] nbr32bits, int [] nbr31bits, int NumberLength)
	{
	    int i, j, k;
	    j = 0;
	    nbr32bits[NumberLength] = 0;
	    for (i = 0; i < NumberLength; i++)
	    {
	      k = i & 0x0000001F;
	      if (k == 0)
	      {
	        nbr31bits[i] = (int)(nbr32bits[j] & 0x7FFFFFFF);
	      }
	      else
	      {
	        nbr31bits[i] = (int)(((nbr32bits[j] >> (32-k)) |
	                              (nbr32bits[j+1] << k)) & 0x7FFFFFFF);
	        j++;
	      }
	    }
	}

	public void AdjustModN(int Nbr[], int TestNbr[], int NumberLength)
	{
	    long MaxUInt = 0x7FFFFFFFL;
	    long TrialQuotient;
	    long carry;
	    int i;
	    double dAux, dN;
	
	    dN = (double) TestNbr[NumberLength - 1];
	    if (NumberLength > 1)
	    {
	      dN += (double) TestNbr[NumberLength - 2] / dDosALa31;
	    }
	    if (NumberLength > 2)
	    {
	      dN += (double) TestNbr[NumberLength - 3] / dDosALa62;
	    }
	    dAux =
	      (double) Nbr[NumberLength] * dDosALa31 + (double) Nbr[NumberLength - 1];
	    if (NumberLength > 1)
	    {
	      dAux += (double) Nbr[NumberLength - 2] / dDosALa31;
	    }
	    TrialQuotient = (long) (dAux / dN) + 3;
	    if (TrialQuotient >= DosALa32)
	    {
	      carry = 0;
	      for (i = 0; i < NumberLength; i++)
	      {
	        carry = Nbr[i + 1] - (TrialQuotient >>> 31) * TestNbr[i] - carry;
	        Nbr[i + 1] = (int)(carry & MaxUInt);
	        carry = (MaxUInt - carry) >>> 31;
	      }
	      TrialQuotient &= MaxUInt;
	    }
	    carry = 0;
	    for (i = 0; i < NumberLength; i++)
	    {
	      carry = Nbr[i] - TrialQuotient * TestNbr[i] - carry;
	      Nbr[i] = (int)(carry & MaxUInt);
	      carry = (MaxUInt - carry) >>> 31;
	    }
	    Nbr[NumberLength] -= (int)carry;
	    while ((Nbr[NumberLength] & MaxUInt) != 0)
	    {
	      carry = 0;
	      for (i = 0; i < NumberLength; i++)
	      {
	        carry += (long)Nbr[i] + (long)TestNbr[i];
	        Nbr[i] = (int)(carry & MaxUInt);
	        carry >>= 31;
	      }
	      Nbr[NumberLength] += (int)carry;
	    }
	}

	public void DivBigNbrByLong(int Dividend[], long Divisor, int Quotient[], int NumberLength)
	{
	    int i;
	    boolean ChSignDivisor = false;
	    long Divid, Rem = 0;
	
	    if (Divisor < 0)
	    {                            // If divisor is negative...
	      ChSignDivisor = true;      // Indicate to change sign at the end and
	      Divisor = -Divisor;        // convert divisor to positive.
	    }
	    if (Dividend[i = NumberLength - 1] >= 0x40000000)
	    {                            // If dividend is negative...
	      Rem = Divisor - 1;
	    }
	    for ( ; i >= 0; i--)
	    {
	      Divid = Dividend[i] + (Rem << 31);
	      Rem = Divid - (Quotient[i] = (int)(Divid / Divisor))*Divisor;
	    }
	    if (ChSignDivisor)
	    {                            // Change sign if divisor is negative.
	                                 // Convert divisor to positive.
	      ChSignBigNbr(Quotient, NumberLength);
	    }
	}

	public long RemDivBigNbrByLong(int Dividend[], long Divisor, int NumberLength)
	{
	    int i;
	    long Rem = 0;
	    long Mod2_31;
	    int divis = (int)(Divisor < 0?-Divisor:Divisor);
	    if (Divisor < 0)
	    {                            // If divisor is negative...
	      Divisor = -Divisor;        // Convert divisor to positive.
	    }
	    Mod2_31 = ((-2147483648)-divis)%divis;  // 2^31 mod divis.
	    if (Dividend[i = NumberLength - 1] >= 0x40000000)
	    {                            // If dividend is negative...
	      Rem = Divisor - 1;
	    }
	    for ( ; i >= 0; i--)
	    {
	      Rem = Rem * Mod2_31 + Dividend[i];
	      do
	      {
	        Rem = (Rem >> 31)*Mod2_31+(Rem & 0x7FFFFFFF);
	      } while (Rem > 0x1FFFFFFFFL);
	    }
	    return Rem % divis;
	}

	public void MultBigNbrByLongModN(int Nbr1[], long Nbr2, int Prod[], int TestNbr[], int NumberLength)
	{
	    long MaxUInt = 0x7FFFFFFFL;
	    long Pr;
	    int j;
	
	    if (NumberLength>=2 && TestNbr[NumberLength-1]==0 && TestNbr[NumberLength-2]<0x40000000)
	    {
	      NumberLength--;
	    }
	    Pr = 0;
	    for (j = 0; j < NumberLength; j++)
	    {
	      Pr = (Pr >>> 31) + Nbr2 * Nbr1[j];
	      Prod[j] = (int)(Pr & MaxUInt);
	    }
	    Prod[j] = (int)(Pr >>> 31);
	    AdjustModN(Prod, TestNbr, NumberLength);
	}

	/**
	 * Multiply Nbr1 and Nbr2 and store the result in Prod. (?)
	 * @param Nbr1
	 * @param Nbr2
	 * @param Prod
	 * @param TestNbr
	 * @param NumberLength
	 */
	public void MultBigNbrModN(int Nbr1[], int Nbr2[], int Prod[], int TestNbr[], int NumberLength)
	{
	    long MaxUInt = 0x7FFFFFFFL;
	    int i, j;
	    long Pr, Nbr;
	
	    if (NumberLength >= 2 && TestNbr[NumberLength-1]==0 && TestNbr[NumberLength-2]<0x40000000)
	    {
	      NumberLength--;
	    }
	    i = NumberLength;
	    do
	    {
	      Prod[--i] = 0;
	    }
	    while (i > 0);
	    i = NumberLength;
	    do
	    {
	      Nbr = Nbr1[--i];
	      j = NumberLength;
	      do
	      {
	        Prod[j] = Prod[j - 1];
	        j--;
	      }
	      while (j > 0);
	      Prod[0] = 0;
	      Pr = 0;
	      for (j = 0; j < NumberLength; j++)
	      {
	        Pr = (Pr >>> 31) + Nbr * Nbr2[j] + Prod[j];
	        Prod[j] = (int)(Pr & MaxUInt);
	      }
	      Prod[j] += (Pr >>> 31);
	      AdjustModN(Prod, TestNbr, NumberLength);
	    }
	    while (i > 0);
	}

	public void BigNbrModN(int Nbr[], int Length, int Mod[], int[] TestNbr, int NumberLength)
	{
	    int i, j;
	    for (i = 0; i < NumberLength; i++)
	    {
	      Mod[i] = Nbr[i + Length - NumberLength];
	    }
	    Mod[i] = 0;
	    AdjustModN(Mod, TestNbr, NumberLength);
	    for (i = Length - NumberLength - 1; i >= 0; i--)
	    {
	      for (j = NumberLength; j > 0; j--)
	      {
	        Mod[j] = Mod[j - 1];
	      }
	      Mod[0] = Nbr[i];
	      AdjustModN(Mod, TestNbr, NumberLength);
	    }
	}

	/**
	 * Converts the int[] GD into a BigInteger.
	 * @param GD
	 * @param NumberLength
	 * @return GD as BigInteger
	 */
	public BigInteger BigIntToBigNbr(int[] GD, int NumberLength)
	{
	    byte[] Result;
	    long[] Temp;
	    int i, NL;
	    long digit;
	
	    Temp = new long[NumberLength];
	    Convert31To32Bits(GD, Temp, NumberLength);
	    NL = NumberLength * 4;
	    Result = new byte[NL];
	    for (i = 0; i < NumberLength; i++)
	    {
	      digit = Temp[i];
	      Result[NL - 1 - 4 * i] = (byte) (digit & 0xFF);
	      Result[NL - 2 - 4 * i] = (byte) (digit / 0x100 & 0xFF);
	      Result[NL - 3 - 4 * i] = (byte) (digit / 0x10000 & 0xFF);
	      Result[NL - 4 - 4 * i] = (byte) (digit / 0x1000000 & 0xFF);
	    }
	    return (new BigInteger(Result));
	}

	/**
	 * Converts the BigInteger N into the int[] TestNbr.
	 * @param Noriginal value
	 * @param TestNbr int[] representing the principal return value.
	 * @return NumberLength
	 */
	public int BigNbrToBigInt(BigInteger N, int TestNbr[])
	{
	    byte[] Result;
	    long[] Temp;
	    int i, j, mask;
	    long p;
	    int NumberLength;
	
	    Result = N.toByteArray();
	    NumberLength = (Result.length * 8 + 30)/31;
	    Temp = new long[NumberLength+1];
	    j = 0;
	    mask = 1;
	    p = 0;
	    for (i = Result.length - 1; i >= 0; i--)
	    {
	      p += mask * (long) (Result[i] >= 0 ? Result[i] : Result[i] + 256);
	      mask <<= 8;
	      if (mask == 0)
	      {                        // Overflow
	        Temp[j++] = p;
	        mask = 1;
	        p = 0;
	      }
	    }
	    Temp[j] = p;
	    Convert32To31Bits(Temp, TestNbr, NumberLength);
	    if (TestNbr[NumberLength - 1] > Mi)
	    {
	      TestNbr[NumberLength] = 0;
	      NumberLength++;
	    }
	    TestNbr[NumberLength] = 0;
	    return NumberLength;
	}
	
	/**
	 * Converts a BigNbr in 31-bit representation into a String.
	 * @param Nbr
	 * @param NumberLength
	 * @return
	 */
	// XXX 01: workaround for buggy ToString
	public String BigNbrToString(int Nbr[], int NumberLength) {
		BigInteger bigInt = this.BigIntToBigNbr(Nbr, NumberLength);
		return bigInt.toString();
	}
	  
	/**
	 * Converts a BigNbr in 32-bit representation into a String.
	 * @param Nbr
	 * @param NumberLength
	 * @return
	 */
	// XXX 01: workaround for buggy ToString
	public String BigNbrToString(long Nbr[], int NumberLength) {
		int nbr31Size = (NumberLength*32+30)/31;
		int[] nbr31 = new int[nbr31Size];
		this.Convert32To31Bits(Nbr, nbr31, NumberLength);
		BigInteger bigInt = this.BigIntToBigNbr(nbr31, nbr31Size);
		return bigInt.toString();
	}
}
