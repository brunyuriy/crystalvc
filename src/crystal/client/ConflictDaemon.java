package crystal.client;

import java.io.IOException;

import crystal.model.ConflictResult;
import crystal.model.DataSource;
import crystal.model.ConflictResult.ResultStatus;
import crystal.model.DataSource.RepoKind;
import crystal.server.HgStateChecker;

/**
 * Daemon that decouples the UI from the analysis. This class can be extended to perform the analysis on an external
 * machine, enable caching, or serve tea without having to update the UI.
 * 
 * @author rtholmes
 */
public class ConflictDaemon {

	/**
	 * Perform the analysis.
	 * 
	 * @param source
	 *            Data source to consider.
	 * @param prefs
	 *            Preferences to abide by.
	 * @return the conflict status of the given data source to the developer's environment.
	 */
	public static ConflictResult calculateConflict(DataSource source, ProjectPreferences prefs) {
		ResultStatus status = null;
		try {
			if (source.getKind().equals(RepoKind.HG)) {

				status = HgStateChecker.getState(prefs, source);

			} else if (source.getKind().equals(RepoKind.GIT)) {
				// Git isn't implemented yet
				System.err.println("ConflictDaemon::caluclateConflict(..) - Cannot handle RepoKind: " + source.getKind());

			} else {
				System.err.println("ConflictDaemon::caluclateConflict(..) - Cannot handle RepoKind: " + source.getKind());
			}

			ConflictResult result = new ConflictResult(source, status);
			return result;
		} catch (IOException ioe) {
			System.err.println("ConflictDaemon::calculateConflict(..) - error: " + ioe.getMessage());
			ioe.printStackTrace();
		}
		return null;
	}
}
