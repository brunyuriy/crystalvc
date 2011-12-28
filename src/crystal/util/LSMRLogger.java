/**
 * Created on Jan 30, 2009
 * @author rtholmes
 */
package crystal.util;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.xml.XMLLayout;

/**
 * A wrapper for a logger.
 * @author rtholmes
 */
public class LSMRLogger {

	public static String LOG_PATTERN_VERBOSE = "%5p %d (%F:%L) - %m%n";

	/**
	 * This logger pattern is much faster as it does not have to compute class/line numbers on the fly.
	 */
	// public static String LOG_PATTERN_FAST = "%5p %d - %m%n";
	public static String LOG_PATTERN_FAST = "%5p - %m%n";

	/**
	 * Starts the standard logging. By default the verbose pattern is used.
	 */
	public static void startLog4J() {

		startLog4J(true);

	}

	/**
	 * The default Logger level will be INFO using this method. If you want a different level use one of the methods
	 * that takes a level parameter.
	 * 
	 * @param verbose
	 *            true for the verbose pattern; false for the fast pattern.
	 */
	public static void startLog4J(boolean verbose) {
		// We don't want any duplicate appenders laying around
		Logger.getRootLogger().removeAllAppenders();

		BasicConfigurator.configure();

		// This is bad form but BasicConfigurator only adds one appender so it works out just fine
		ConsoleAppender ca = (ConsoleAppender) Logger.getRootLogger().getAllAppenders().nextElement();

		if (ca != null)
			if (verbose)
				ca.setLayout(new PatternLayout(LOG_PATTERN_VERBOSE));
			else
				ca.setLayout(new PatternLayout(LOG_PATTERN_FAST));

		Logger.getRootLogger().setLevel(Level.INFO);
	}

	/**
	 * @param verbose
	 *            Verbose pattern or fast pattern?
	 * @param level
	 *            The Level that should be logged (e.g., Level.INFO, Level.DEBUG)
	 */
	public static void startLog4J(boolean verbose, Level level) {

		startLog4J(verbose);
		Logger.getRootLogger().setLevel(level);
	}

	/**
	 * 
	 * @param verbose
	 * @param level
	 * @param logDirectory
	 *            the directory where log files should be written; set to null if you don't want log files.
	 * @param logFNamePrefix
	 *            the filename prefix of the log files; if the log directory is set to null this won't be used.
	 */
	public static void startLog4J(boolean shipping, boolean verbose, Level level, String logDirectory, String logFNamePrefix) {
		// We don't want any duplicate appenders laying around
		Logger.getRootLogger().removeAllAppenders();

		BasicConfigurator.configure();

		// This is bad form but BasicConfigurator only adds one appender so it works out just fine
		ConsoleAppender ca = (ConsoleAppender) Logger.getRootLogger().getAllAppenders().nextElement();
		if (shipping) {
			ca.setThreshold(Level.ERROR);
		}

		if (logDirectory != null && !logDirectory.endsWith(File.separator))
			logDirectory = logDirectory + File.separator;

		if (logDirectory != null) {
			File logDir = new File(logDirectory);
			if (!logDir.exists()) {
				logDir.mkdirs();
				Logger.getLogger(LSMRLogger.class).trace("Log directory: " + logDir.getAbsolutePath());
			}
		}

		if (logFNamePrefix == null)
			logFNamePrefix = "lsmrLog";

		RollingFileAppender rfa = null;
		Layout fileLayout = null;
		if (verbose) {
			// fileLayout = new PatternLayout(LOG_PATTERN_VERBOSE);
			fileLayout = new XMLLayout();
		} else {
			// fileLayout = new PatternLayout(LOG_PATTERN_FAST);
			fileLayout = new XMLLayout();
		}

		try {
			if (ca != null)
				if (verbose) {
					ca.setLayout(new PatternLayout(LOG_PATTERN_VERBOSE));

					if (logDirectory != null)
						rfa = new RollingFileAppender(fileLayout, logDirectory + logFNamePrefix + ".xml");

				} else {
					ca.setLayout(new PatternLayout(LOG_PATTERN_FAST));

					if (logDirectory != null)
						rfa = new RollingFileAppender(fileLayout, logDirectory + logFNamePrefix + ".xml");
				}

			if (logDirectory != null) {
				rfa.setMaxBackupIndex(10);
				rfa.setMaxFileSize("10MB");
				Logger.getRootLogger().addAppender(rfa);

				XMLLayout xmlLayout = new XMLLayout();
				xmlLayout.setLocationInfo(true);

				rfa = new RollingFileAppender(xmlLayout, logDirectory + logFNamePrefix + ".xml");
				rfa.setMaxBackupIndex(10);
				rfa.setMaxFileSize("10MB");
				Logger.getRootLogger().addAppender(rfa);

				PatternLayout layout = new PatternLayout(LOG_PATTERN_VERBOSE);
				rfa = new RollingFileAppender(layout, logDirectory + logFNamePrefix + ".log");
				rfa.setMaxBackupIndex(10);
				rfa.setMaxFileSize("10MB");
				Logger.getRootLogger().addAppender(rfa);

			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		Logger.getRootLogger().setLevel(level);
	}

}
