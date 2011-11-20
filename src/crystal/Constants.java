package crystal;

/**
 * Constants contains several constants for Crystal to use.  
 * 
 * @author brun
 * @author rtholmes
 */

import org.apache.log4j.Level;

public class Constants {

	/**
	 * TIMER_CONSTANT is Crystal's refresh period, in milliseconds. Crystal will attempt to refresh the local state, actions, relationships, and guidance every TIMER_CONSTANT
	 * milliseconds.
	 */

	// the refresh time in seconds
	public static long DEFAULT_REFRESH = 10 * 60; // 10 mins default
	// public static long REFRESH = DEFAULT_REFRESH;
	// public static long TIMER_CONSTANT = 10 * 60 * 1000; // 10 mins default
	// for demos:
	// public static long TIMER_CONSTANT = 1000;
	// for debugging:
	// public static long TIMER_CONSTANT = 5 * 1000; // 5 seconds

	/**
	 * Minimum hg version may only contain 1 decimal point.
	 */
	public static final double MIN_HG_VERSION = 1.6;

	/**
	 * Debug constants.
	 */
	public static final boolean DEBUG_RUNIT = false;
	public static final boolean DEBUG_UI = false;

	/**
	 * Log constants.
	 */
	/**
	 * When false the Log4J console appender is quiet only reporting ERROR and above.
	 */
	public static final boolean QUIET_CONSOLE = true;
	/**
	 * The level of logging.
	 */
	public static final Level LOG_LEVEL = Level.DEBUG;

	/**
	 * HOME is the string that designates the home directory in the local file system.
	 */
	public static final String HOME = "$HOME";
}
