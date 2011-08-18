package crystal.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Iterator;

import crystal.client.ClientPreferences;
import crystal.client.ProjectPreferences;
import crystal.model.DataSource;


public class ValidInputChecker {
	
	/**
	 * Check if input url path is valid.
	 * @param path
	 * @return
	 */
	public static boolean checkUrl(String path) {
		try {
		    URL url = new URL(path);
		    URLConnection conn = url.openConnection();
		    conn.connect();
		} catch (MalformedURLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	
	/**
	 * Check if there are duplicate data source in the same projectPreferences
	 * @param pref
	 * @param source
	 * @return
	 */
	public static boolean checkDataSourceNameDuplicate(ProjectPreferences pref, DataSource source) {
		Iterator<DataSource> i = pref.getDataSources().iterator();
		int count = 0;
		while(i.hasNext()) {
			if (i.next().equals(source))
				count++;
		}
		return count < 2;
	}
	/**
	 * Check if input command is valid command
	 * @param command
	 * @return
	 */
	public static boolean checkCommand(String command) {
		return command == null || command.trim().isEmpty() || RunIt.getExecutable(command) != null;
	}
	
	/**
	 * Check if there are duplicate projectPreferences name in the same clientPreferences
	 * @param prefs
	 * @param pref
	 * @return
	 */
	public static boolean checkProjectPreferencesNameDuplicate(ClientPreferences prefs, ProjectPreferences pref) {
		Iterator<ProjectPreferences> i = prefs.getProjectPreference().iterator();
		int count = 0;
		while (i.hasNext()) {
			if (i.next().equals(pref)) {
				count++;
			}
		}
		return count < 2;
	}
	
	
	/**
	 * Check if input path is directory path
	 * @param path
	 * @return
	 */
	public static boolean checkDirectoryPath(String path) {
		return (new File(path).exists()) && (new File(path).isDirectory());
	}
	
	
	/**
	 * Check if input string is correct representation for long value
	 * @param s
	 * @return
	 */
	public static boolean checkStringToLong(String s) {
		try {
			Long.valueOf(s);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
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
