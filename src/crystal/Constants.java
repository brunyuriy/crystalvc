package crystal;

import org.apache.log4j.Level;

public class Constants {

//	public static long TIMER_CONSTANT = 10 * 60 * 1000; // 10 mins default
	public static long TIMER_CONSTANT = 1000; // 10 mins default


	// for debugging
	// public static long TIMER_CONSTANT = 5 * 1000; // 15 seconds

	public static final boolean DEBUG_RUNIT = false;
	public static final boolean DEBUG_UI = false;

	/**
	 * When false the Log4J console appender is quiet only reporting ERROR and above.
	 */
	public static final boolean QUIET_CONSOLE = true;

	public static final Level LOG_LEVEL = Level.INFO;

	public static final String HOME = "$HOME";
}
