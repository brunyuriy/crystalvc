package crystal.util;

import java.util.Collection;

public class ValidInputChecker {
	
	/**
	 * Check if given String is contained in given set
	 * @param s
	 * @param set
	 */
	public static void checkStringInSet(String s, Collection<String> set){
		if(s == null || !set.contains(s)){
			throw new IllegalArgumentException("Input: " + s + " is not contained in the set.");
		}
	}

	public static void checkNullInput(Object o){
		if(o == null){
			throw new IllegalArgumentException("Null input");
		}
	}
	
	public static void checkValidStringInput(String s){
		if(s == null || s.trim().equals("")){
			throw new IllegalArgumentException("Invalid string input");
		}
	}
	
	public static void checkNonNegativeNumberInput(int n){
		if(n < 0){
			throw new IllegalArgumentException("Negative integer input");
		}
	}
}
