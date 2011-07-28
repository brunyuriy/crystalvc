package crystal.util;

import java.util.HashSet;
import java.util.Set;

public class ValidInputChecker {
	
	public static Set<String> actionNames = new HashSet<String>();
	static {
		actionNames.add("UNCHECKPOINTED");
		actionNames.add("MUST RESOLVE");
		actionNames.add("ALL CLEAR");
		actionNames.add("PENDING");
		actionNames.add("ERROR");
		actionNames.add("BUILD");
		actionNames.add("TEST");
		
	}
	
	public static void checkActionNames(String s){
		if(s == null || !actionNames.contains(s.toUpperCase())){
			throw new IllegalArgumentException("Illegal action names.");
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
