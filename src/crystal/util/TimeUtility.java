package crystal.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;

/**
 * Some handy time utilities.
 * 
 * @author rtholmes
 */
public class TimeUtility {

	/**
	 * A standard date string. ISO and RFC date strings do not work if you want to encode the date into a file name
	 * (e.g., having a colon in a windows file system generates native JNI exceptions).
	 * 
	 * This format works across platforms.
	 */
	// e.g., 2009-01-27T15-57-21.111-800
	private static String LSMR_DATE_FORMAT = "yyyy-MM-dd'T'HH-mm-ss.SSSZ";

	/**
	 * Parse a standard date format.
	 * 
	 * @param dateString
	 * @return
	 */
	public static Date parseLSMRDate(String dateString) {

		try {
			return new SimpleDateFormat(LSMR_DATE_FORMAT).parse(dateString);
		} catch (ParseException e) {
			// _log.error(e);
			return null;
		}
	}

	/**
	 * Format a date instance.
	 * 
	 * @param date
	 * @return
	 */
	public static String formatLSMRDate(Date date) {
		return new SimpleDateFormat(LSMR_DATE_FORMAT).format(date);
	}

	/**
	 * Format the current time.
	 * 
	 * @return
	 */
	public static String getCurrentLSMRDateString() {
		return formatLSMRDate(Calendar.getInstance().getTime());
	}

	/**
	 * @param ms
	 *            The number of milliseconds to parse nicely.
	 * @return A string containing an easily human-readable representation of some number of milliseconds. Best results
	 *         for a reasonable number of hours (aka < 24).
	 */
	public static String msToHumanReadable(long ms) {

		if (ms < 1000) {
			return ms + " ms";
		} else if (ms >= 1000 && ms < 60000) {
			return new Formatter().format("%.2f", ms / 1000f) + " sec";
		} else if (ms >= 60000 && ms < 60000 * 60) {
			long min = ms / 1000 / 60;
			long sec = (ms - (min * 60000)) / 1000;

			return min + " min " + sec + " sec";
		} else if (ms >= 60000 * 60) {
			long hours = ms / 1000 / 60 / 60;
			long min = (ms - hours * 60000 * 60) / 1000 / 60;

			if (hours > 1)
				return hours + " hours " + min + " min";
			else
				return hours + " hour " + min + " min";
		} else {
			// This should never happen.
			return "";
		}

	}

	/**
	 * Write the given timestamp in a 'nice' format (e.g.h 3h4m instead of 188m) 
	 * @param start
	 *            The start time of the period of interest.
	 * @return A human readable representation of the start time less the time at which the method was called.
	 */
	public static String msToHumanReadableDelta(long start) {
		long stop = System.currentTimeMillis();
		return msToHumanReadable(stop - start);
	}

}
