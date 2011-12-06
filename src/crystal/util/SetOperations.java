package crystal.util;

import java.util.Set;
import java.util.HashSet;

/**
 * A set of standard set operations
 * @author brun
 */
public class SetOperations {

	/**
	 * @param a: set a
	 * @param b: set b
	 * @return the intersection of a and b.
	 */
	public static Set<String> intersection(Set<String> a, Set<String> b) {
		ValidInputChecker.checkNullInput(a);
		ValidInputChecker.checkNullInput(b);
		Set<String> all = new HashSet<String>();
		all.addAll(a);
		all.addAll(b);

		Set<String> answer = new HashSet<String>();

		for (String current : all) {
			if (a.contains(current) && b.contains(current))
				answer.add(current);
		}
		return answer;
	}

	/**
	 * @param a: set a
	 * @param b: set b
	 * @return the union of a and b
	 */
	public static Set<String> union(Set<String> a, Set<String> b) {
		ValidInputChecker.checkNullInput(a);
		ValidInputChecker.checkNullInput(b);
		Set<String> answer = new HashSet<String>();
		answer.addAll(a);
		answer.addAll(b);
		return answer;
	}

	/**
	 * @param a: set a
	 * @param b: set b
	 * @return a / b
	 */
	public static Set<String> setDifference(Set<String> a, Set<String> b) {
		Set<String> answer = new HashSet<String>();
		answer.addAll(a);
		answer.removeAll(b);
		return answer;
	}

	/**
	 * @param a: set a
	 * @param b: set b
	 * @return a xor b
	 */
	public static Set<String> xor(Set<String> a, Set<String> b) {
		Set<String> answer = new HashSet<String>();
		answer.addAll(setDifference(a,b));
		answer.addAll(setDifference(b,a));
		return answer;
	}

	/**
	 * @param a: set a
	 * @param b: set b
	 * @return true iff a is a subset of b
	 */
	public static boolean isSubset(Set<String> a, Set<String> b) {
		return (setDifference(a, b).isEmpty());
	}
	
	/**
	 * @param a: set a
	 * @param b: set b
	 * @return true iff a is a superset of b
	 */
	public static boolean isSuperset(Set<String> a, Set<String> b) {
		return isSubset(b, a);
	}

}
