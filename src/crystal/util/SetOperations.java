package crystal.util;

import java.util.Set;
import java.util.HashSet;

public class SetOperations {
	
	public static Set<String> intersection(Set<String> a, Set<String> b) {
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
	
	public static Set<String> union(Set<String> a, Set<String> b) {
		Set<String> answer = new HashSet<String>();
		answer.addAll(a);
		answer.addAll(b);
		return answer;
	}
	
	public static Set<String> aminusb(Set<String> a, Set<String> b) {
		Set<String> answer = new HashSet<String>();
		answer.addAll(a);
		answer.removeAll(b);
		return answer;
	}
	
	public static Set<String> xor(Set<String> a, Set<String> b) {
		Set<String> answer = new HashSet<String>();
		answer.addAll(aminusb(a,b));
		answer.addAll(aminusb(b,a));
		return answer;
	}

}
