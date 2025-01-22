/*
 * java-math-library is a Java library focused on number theory, but not necessarily limited to it. It is based on the PSIQS 4.0 factoring project.
 * Copyright (C) 2018-2025 Tilman Neumann - tilman.neumann@web.de
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 */
package de.tilman_neumann.jml.factor;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import de.tilman_neumann.util.ConfigUtil;

import static org.junit.Assert.assertEquals;

public class CombinedFactorAlgorithm2ThreadsTest extends FactorTestBase {

	private boolean RUN_SLOW_TESTS_TOO = false;

	public CombinedFactorAlgorithm2ThreadsTest() {
		// gitgub CI results for the 236 bit test number:
		// 1 thread -> 22s, 2 threads -> 12s, 4 threads -> 14s.
		// Using 2 threads looks best so far.
		super(new CombinedFactorAlgorithm(2, null, true));
	}

	@BeforeClass
	public static void setup() {
		ConfigUtil.initProject();
	}
	
	@Test
	public void testSmallestComposites() {
		List<Integer> fails = testFullFactorizationOfComposites(100000);
		assertEquals("Failed to factor n = " + fails, 0, fails.size());
	}

	@Test
	public void testSmallCompositesWithSmallFactors() {
		assertFullFactorizationSuccess(949443, "3 * 11 * 28771"); // 20 bit
		assertFullFactorizationSuccess(996433, "31 * 32143"); // 20 bit
		assertFullFactorizationSuccess(1340465, "5 * 7 * 38299"); // 21 bit
		assertFullFactorizationSuccess(1979435, "5 * 395887"); // 21 bit
		assertFullFactorizationSuccess(2514615, "3 * 5 * 167641"); // 22 bit
		assertFullFactorizationSuccess(5226867, "3^2 * 580763"); // 23 bit
		assertFullFactorizationSuccess(10518047, "61 * 172427"); // 24 bit
		assertFullFactorizationSuccess(30783267, "3^3 * 1140121"); // 25 bit
		assertFullFactorizationSuccess(62230739, "67 * 928817"); // 26 bit
		assertFullFactorizationSuccess(84836647, "7 * 17 * 712913"); // 27 bit
		assertFullFactorizationSuccess(94602505, "5 * 18920501"); // 27 bit
		assertFullFactorizationSuccess(258555555, "3^2 * 5 * 5745679"); // 28 bit
		assertFullFactorizationSuccess(436396385, "5 * 87279277"); // 29 bit
		assertFullFactorizationSuccess(612066705, "3 * 5 * 40804447"); // 30 bit
		assertFullFactorizationSuccess(2017001503, "11 * 183363773"); // 31 bit
		assertFullFactorizationSuccess(3084734169L, "3^2 * 11 * 31158931"); // 32 bit
		assertFullFactorizationSuccess(6700794123L, "3 * 41 * 54478001"); // 33 bit
		assertFullFactorizationSuccess(16032993843L, "3 * 5344331281"); // 34 bit
		assertFullFactorizationSuccess(26036808587L, "83 * 313696489"); // 35 bit
		assertFullFactorizationSuccess(41703657595L, "5 * 8340731519"); // 36 bit
		assertFullFactorizationSuccess(68889614021L, "19 * 43 * 84320213"); // 37 bit
		assertFullFactorizationSuccess(197397887859L, "3^2 * 21933098651"); // 38 bit
		assertFullFactorizationSuccess(712869263, "89 * 8009767"); // 30 bit
		assertFullFactorizationSuccess(386575807, "73 * 5295559"); // 29 bit
		assertFullFactorizationSuccess(569172749, "83 * 6857503"); // 30 bit
		assertFullFactorizationSuccess(624800360363L, "233 * 2681546611"); // 40 bit
		assertFullFactorizationSuccess(883246601513L, "251 * 3518910763"); // 40 bit
	}

	@Test
	public void testSmallCompositesWithManyFactors() {
		assertFullFactorizationSuccess(35184372094495L, "5 * 13^2 * 17 * 19 * 29 * 47 * 271 * 349"); // 46 bit
		assertFullFactorizationSuccess(1096954293075013905L, "3 * 5 * 7^2 * 169681 * 8795650783"); // 60 bit
		assertFullFactorizationSuccess(9223372036854775807L, "7^2 * 73 * 127 * 337 * 92737 * 649657"); // Long.MAX_VALUE = 2^63-1 
		assertFullFactorizationSuccess(1100087778366101931L, "3 * 7 * 43 * 89 * 199 * 263 * 307 * 881 * 967"); // Fibonacci(88), 60 bit
	}
	
	@Test
	public void testSmallSquares() {
		assertFullFactorizationSuccess(100140049, "10007^2"); // 27 bit
		assertFullFactorizationSuccess(10000600009L, "100003^2"); // 34 bit
		assertFullFactorizationSuccess(1000006000009L, "1000003^2"); // 40 bit
		assertFullFactorizationSuccess(6250045000081L, "2500009^2"); // 43 bit
		assertFullFactorizationSuccess(10890006600001L, "3300001^2"); // 44 bit
		assertFullFactorizationSuccess(14062507500001L, "3750001^2"); // 44 bit
		assertFullFactorizationSuccess(25000110000121L, "5000011^2"); // (45 bit)
		assertFullFactorizationSuccess(100000380000361L, "10000019^2"); // 47 bit
		assertFullFactorizationSuccess(10000001400000049L, "100000007^2"); // 54 bit
		assertFullFactorizationSuccess(1000000014000000049L, "1000000007^2"); // 60 bit
	}

	@Test
	public void testSmallSemiprimesWithFactorsOfSimilarSize() {
		assertFullFactorizationSuccess(2157195374713L, "1037957 * 2078309"); // 41 bit
		assertFullFactorizationSuccess(8370014680591L, "2046637 * 4089643"); // 43 bit
		assertFullFactorizationSuccess(22568765132167L, "3360503 * 6715889"); // 45 bit
		assertFullFactorizationSuccess(63088136564083L, "3970567 * 15888949"); // 46 bit
		assertFullFactorizationSuccess(15241718503477L, "1234577 * 12345701"); // 44 bit
		assertFullFactorizationSuccess(135902052523483L, "8240363 * 16492241"); // 47 bit
		assertFullFactorizationSuccess(1253586675305333L, "6030247 * 207883139"); // 51 bit
		assertFullFactorizationSuccess(1139151196120601L, "232987 * 4889333723"); // 51 bit
		assertFullFactorizationSuccess(1553712951089947L, "29184373 * 53237839"); // 51 bit
		assertFullFactorizationSuccess(2235885271339597L, "38826707 * 57586271"); // 51 bit
		assertFullFactorizationSuccess(1586929215386303L, "4605361 * 344583023"); // 51 bit
		assertFullFactorizationSuccess(930705057210221L, "25325941 * 36749081"); // 50 bit
		assertFullFactorizationSuccess(1067332898136023L, "26165441 * 40791703"); // 50 bit
		assertFullFactorizationSuccess(8311092540494299L, "67613999 * 122919701"); // 53 bit
		assertFullFactorizationSuccess(23603982383629381L, "118493549 * 199200569"); // 55 bit
		assertFullFactorizationSuccess(58725827789610857L, "103129121 * 569439817"); // 56 bit
		assertFullFactorizationSuccess(369313815090910177L, "354877001 * 1040681177"); // 59 bit
		assertFullFactorizationSuccess(41382606407163353L, "69696377 * 593755489"); // 56 bit
		assertFullFactorizationSuccess(306358296309770459L, "209066843 * 1465360513"); // 59 bit
		assertFullFactorizationSuccess(474315852287951L, "15390191 * 30819361"); // 49 bit
		assertFullFactorizationSuccess(9400170223537253L, "47701151 * 197063803"); // 54 bit
		assertFullFactorizationSuccess(35239016917581299L, "82794071 * 425622469"); // 55 bit
		assertFullFactorizationSuccess(37915240075398767L, "16632857 * 2279538631"); // 56 bit
		assertFullFactorizationSuccess(459926431465210403L, "631315747 * 728520449"); // 59 bit
		assertFullFactorizationSuccess(752882545886305349L, "460178669 * 1636065721"); // 60 bit
		assertFullFactorizationSuccess(179503729521451L, "7420397 * 24190583"); // 48 bit
		assertFullFactorizationSuccess(1059150637518581L, "22339673 * 47411197"); // 50 bit
		assertFullFactorizationSuccess(3209190314462729L, "24571021 * 130608749"); // 52 bit
		assertFullFactorizationSuccess(17586811742837503L, "105611027 * 166524389"); // 54 bit
		assertFullFactorizationSuccess(13745855671622359L, "50960113 * 269737543"); // 54 bit
		assertFullFactorizationSuccess(15727038894518533L, "55460939 * 283569647"); // 54 bit
		assertFullFactorizationSuccess(66804960995707271L, "125296429 * 533175299"); // 56 bit
		assertFullFactorizationSuccess(38704493646912997L, "31501721 * 1228646957"); // 56 bit
		assertFullFactorizationSuccess(56025872236672099L, "53839061 * 1040617559"); // 56 bit
		assertFullFactorizationSuccess(57675022504187287L, "172307477 * 334721531"); // 56 bit
		assertFullFactorizationSuccess(69916262762899909L, "72133751 * 969258659"); // 56 bit
		assertFullFactorizationSuccess(51113648728234999L, "49275467 * 1037304197"); // 56 bit
		assertFullFactorizationSuccess(55878279398722441L, "131149259 * 426066299"); // 56 bit
		assertFullFactorizationSuccess(3225275494496681L, "56791489 * 56791529"); // 52 bit
		assertFullFactorizationSuccess(322527333642009919L, "567914891 * 567914909"); // 59 bit
	}

	@Test
	public void testSmallSemiprimesWithFactorsOfDifferentSize() {
		assertFullFactorizationSuccess(5640012124823L, "23117 * 243976819"); // 43 bit
		assertFullFactorizationSuccess(7336014366011L, "24781 * 296033831"); // 43 bit
		assertFullFactorizationSuccess(19699548984827L, "1464751 * 13449077"); // 45 bit
		assertFullFactorizationSuccess(52199161732031L, "2065879 * 25267289"); // 46 bit
		assertFullFactorizationSuccess(73891306919159L, "767827 * 96234317"); // 47 bit
		assertFullFactorizationSuccess(112454098638991L, "80309 * 1400267699"); // 47 bit
		
		assertFullFactorizationSuccess(32427229648727L, "1833401 * 17686927"); // 45 bit
		assertFullFactorizationSuccess(87008511088033L, "125299 * 694407067"); // 47 bit
		assertFullFactorizationSuccess(92295512906873L, "170123 * 542522251"); // 47 bit
		assertFullFactorizationSuccess(338719143795073L, "1516817 * 223309169"); // 49 bit
		assertFullFactorizationSuccess(346425669865991L, "70163 * 4937440957"); // 49 bit
		assertFullFactorizationSuccess(1058244082458461L, "65677 * 16112856593"); // 50 bit
		assertFullFactorizationSuccess(1773019201473077L, "6539893 * 271108289"); // 51 bit
		assertFullFactorizationSuccess(6150742154616377L, "181039 * 33974680343"); // 53 bit

		assertFullFactorizationSuccess(44843649362329L, "673879 * 66545551"); // 46 bit
		assertFullFactorizationSuccess(67954151927287L, "4919639 * 13812833"); // 46 bit
		assertFullFactorizationSuccess(134170056884573L, "446333 * 300605281"); // 47 bit
		assertFullFactorizationSuccess(198589283218993L, "1151581 * 172449253"); // 48 bit
		assertFullFactorizationSuccess(737091621253457L, "1772291 * 415897627"); // 50 bit
		assertFullFactorizationSuccess(1112268234497993L, "68053 * 16344146981"); // 50 bit
		assertFullFactorizationSuccess(2986396307326613L, "27054757 * 110383409"); // 52 bit
		
		assertFullFactorizationSuccess(26275638086419L, "92893 * 282859183"); // 45 bit
		assertFullFactorizationSuccess(62246008190941L, "2874499 * 21654559"); // 46 bit
		assertFullFactorizationSuccess(209195243701823L, "248231 * 842744233"); // 48 bit
		assertFullFactorizationSuccess(290236682491211L, "12819941 * 22639471"); // 49 bit
		assertFullFactorizationSuccess(485069046631849L, "287537 * 1686979577"); // 49 bit
		assertFullFactorizationSuccess(1239671094365611L, "5148659 * 240775529"); // 51 bit
		assertFullFactorizationSuccess(2815471543494793L, "3865469 * 728364797"); // 52 bit
		assertFullFactorizationSuccess(5682546780292609L, "52196827 * 108867667"); // 53 bit
	}

	@Test
	public void testHarderSmallSemiprimesWithFactorsOfSimilarSize() {
		assertFullFactorizationSuccess(1454149122259871L, "26970011 * 53917261"); // 51 bit
		assertFullFactorizationSuccess(5963992216323061L, "54599437 * 109231753"); // 53 bit
		assertFullFactorizationSuccess(26071073737844227L, "114161413 * 228370279"); // 55 bit
		assertFullFactorizationSuccess(8296707175249091L, "64415917 * 128799023"); // 53 bit
		assertFullFactorizationSuccess(35688516583284121L, "133595303 * 267139007"); // 55 bit
		assertFullFactorizationSuccess(35245060305489557L, "132737413 * 265524689"); // 55 bit
		assertFullFactorizationSuccess(107563481071570333L, "231892711 * 463850203"); // 57 bit
		assertFullFactorizationSuccess(107326406641253893L, "231668813 * 463275161"); // 57 bit
		assertFullFactorizationSuccess(120459770277978457L, "245433631 * 490803847"); // 57 bit
		assertFullFactorizationSuccess(3225273260887418687L, "567914891 * 5679148957"); // 62 bit
	}
	
	@Test
	public void testHarderSmallSemiprimesWithFactorsOfDifferentSize() {
		assertFullFactorizationSuccess(17977882519205951L, "6026521 * 2983127831"); // 54 bit
		assertFullFactorizationSuccess(57410188984551071L, "7419119 * 7738141009"); // 56 bit
		assertFullFactorizationSuccess(708198179721093877L, "31472003 * 22502481959"); // 60 bit
		assertFullFactorizationSuccess(4085731848127832849L, "825205747 * 4951167467"); // 62 bit
		
		assertFullFactorizationSuccess(873351084013120721L, "29133103 * 29977963007"); // 60 bit
		assertFullFactorizationSuccess(7355428158429213199L, "6226303 * 1181347608433"); // 63 bit
		assertFullFactorizationSuccess(7836704265571283783L, "130781947 * 59921911589"); // 63 bit
		assertFullFactorizationSuccess(8940500625246794041L, "240556271 * 37165942871"); // 63 bit
		assertFullFactorizationSuccess(3608228875180849937L, "49696057 * 72605938841"); // 62 bit
		assertFullFactorizationSuccess(9170754184293724117L, "290060959 * 31616644363"); // 63 bit
	}

	@Test
	public void testSomeBiggerNumbers() {
		assertFullFactorizationSuccess("1100087778366101931", "3 * 7 * 43 * 89 * 199 * 263 * 307 * 881 * 967"); // Fibonacci(88), 60 bit
		assertFullFactorizationSuccess("15841065490425479923", "2604221509 * 6082841047"); // 64 bit
		assertFullFactorizationSuccess("11111111111111111111111111", "11 * 53 * 79 * 859 * 265371653 * 1058313049"); // 84 bit
		assertFullFactorizationSuccess("5679148659138759837165981543", "3^3 * 466932157 * 450469808245315337"); // 93 bit
		assertFullFactorizationSuccess("874186654300294020965320730991996026550891341278308", "2^2 * 3 * 471997 * 654743 * 2855761 * 79833227 * 982552477 * 1052328969055591"); // 170 bit
		assertFullFactorizationSuccess("11111111111111111111111111155555555555111111111111111", "67 * 157 * 1056289676880987842105819104055096069503860738769"); // 173 bit
		assertFullFactorizationSuccess("1388091470446411094380555803943760956023126025054082930201628998364642", "2 * 3 * 1907 * 1948073 * 1239974331653 * 50222487570895420624194712095309533522213376829"); // 230 bit
		// 236 bit, takes ~ 8 seconds with 2 threads on Ryzen3900X
		assertFullFactorizationSuccess("99999999999999999999999999999999999999999999999999999999999999999999999", "3^2 * 241573142393627673576957439049 * 45994811347886846310221728895223034301839");
		if (RUN_SLOW_TESTS_TOO) {
			 // 263 bit bit, takes ~ 52 seconds with 2 threads on Ryzen3900X
			assertFullFactorizationSuccess("10000000000000000000000000000000000000000000000000000000000000000000000000005923", "1333322076518899001350381760807974795003 * 7500063320115780212377802894180923803641");
			 // 280 bit bit, takes ~ 158 seconds with 2 threads on Ryzen3900X
			assertFullFactorizationSuccess("1794577685365897117833870712928656282041295031283603412289229185967719140138841093599", "42181796536350966453737572957846241893933 * 42543889372264778301966140913837516662044603");
		}
		
		// this one only needs only trial division, very fast...
		assertFullFactorizationSuccess("2900608971182010301486951469292513060638582965350239259380273225053930627446289431038392125", "3^11 * 5^3 * 7^6 * 11^2 * 13^2 * 17^2 * 19 * 37 * 41 * 53 * 59 * 61 * 73 * 113 * 151 * 227^2 * 271 * 337 * 433 * 457 * 547 * 953 * 11113 * 11117 * 11119 * 33343 * 33347 * 33349 * 33353 * 33359"); // 301 bit
		// = 33333 * 33335 * 33337 * 33339 * 33341 * 33343 * 33345 * 33347 * 33349 * 33351 * 33353 * 33355 * 33357 * 33359 * 33361 * 33363 * 33367 * 33369 * 33369 * 33371
	}
}
