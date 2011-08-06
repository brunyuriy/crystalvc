package crystal.util;

import java.io.File;
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

	/**
	 * Check if input object is null
	 * @param o
	 */
	public static void checkNullInput(Object o){
		if(o == null){
			throw new IllegalArgumentException("Null input");
		}
	}
	
	/**
	 * Check if input string is a valid string.
	 * @param s
	 */
	public static void checkValidStringInput(String s){
		if(s == null || s.trim().equals("")){
			throw new IllegalArgumentException("Invalid string input");
		}
	}
	
	/**
	 * Check if input integer is negative.
	 * @param n
	 */
	public static void checkNonNegativeNumberInput(int n){
		if(n < 0){
			throw new IllegalArgumentException("Negative integer input");
		}
	}
	
	/**
	 * Check if input string path is a file path
	 * @param path
	 */
	public static void checkValidFilePath(String path){
		checkValidPath(path, true);
	}
	
	/**
	 * Check if input string path is a directory path.
	 * 
	 * @param path
	 */
	public static void checkValidDirectoryPath(String path){
		checkValidPath(path, false);
	}
	
	/**
	 * Check if input string path is valid path for the given type.
	 * @param path
	 * @param isFile
	 */
	private static void checkValidPath(String path, boolean isFile){
		File f = new File(path);
		System.out.println(f.exists());
		if(isFile && !f.isFile()){
			throw new IllegalArgumentException("Given path is not path to file.");
		} else if (!isFile && !f.isDirectory()){
			throw new IllegalArgumentException("Given path is not path to directory.");
		}
		
	}
}
