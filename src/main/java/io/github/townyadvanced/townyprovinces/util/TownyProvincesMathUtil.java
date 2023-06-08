package io.github.townyadvanced.townyprovinces.util;

public class TownyProvincesMathUtil {


	/**
	 * Example:
	 * - lowest = 2, highest = 5
	 * - range = 4
	 * - random part = 0,1,2 or 3
	 * - result = 2,3,4 or 5
	 * 
	 * @param lowest the lowest possible result
	 * @param highest the highest possible result
	 * @return the result
	 */
	public static int generateRandomInteger(int lowest, int highest) {
		int range = (highest - lowest) + 1;
		int randomPart = (int)(Math.random() * range);
		int result = lowest + randomPart;
		return result;
	}
}
