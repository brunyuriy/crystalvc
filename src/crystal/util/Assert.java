package crystal.util;

public class Assert {

	public static void assertTrue(boolean condition) {
		if (condition != true)
			throw new RuntimeException("Assertion Failed");
	}

	public static void assertTrue(boolean condition, String msg) {
		if (condition != true)
			throw new RuntimeException("Assertion Failed: " + msg);
	}
}
