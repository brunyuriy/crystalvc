package crystal;

import java.io.File;
import java.util.Vector;

public class Constants {

	public static String PROJECT_PATH = "";
	static {

		/**
		 * This should contain the path to your workspace project location.
		 */
		Vector<String> possiblePaths = new Vector<String>();
		possiblePaths.add("/Users/rtholmes/Documents/workspaces/workspace_longitudinal/conflictClient/"); // rtholmes -
																											// laptop
		possiblePaths.add("/homes/gws/brun/tempCrystal/sourceRepo/");

		for (String possiblePath : possiblePaths) {
			if (new File(possiblePath).exists()) {
				PROJECT_PATH = possiblePath;
				break;
			}
		}
		if (!PROJECT_PATH.endsWith(File.separator)) {
			PROJECT_PATH += File.separator;
		}

		System.out.println("Constants - Working path: " + PROJECT_PATH);
	}

}
