package crystal.client;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import crystal.model.ConflictResult;
import crystal.model.DataSource;
import crystal.model.ConflictResult.ResultStatus;
import crystal.model.DataSource.RepoKind;
import crystal.server.HgStateChecker;
import crystal.util.TimeUtility;

/**
 * Daemon that decouples the UI from the analysis. This class can be extended to perform the analysis on an external
 * machine, enable caching, or serve tea without having to update the UI.
 * 
 * @author rtholmes
 */
public class ConflictDaemon {

	Vector<ComputationListener> _listeners = new Vector<ComputationListener>();

	public interface ComputationListener {
		public void update();
	}

	private static ConflictDaemon _instance = null;

	/**
	 * Stores the results of the analysis. This provides a simple decoupling between the DataSource and the
	 * ConflictResult.
	 */
	private Hashtable<DataSource, ConflictResult> _resultMap = new Hashtable<DataSource, ConflictResult>();

	private ConflictDaemon() {
	}

	public void addListener(ComputationListener listener) {
		if (!_listeners.contains(listener))
			_listeners.add(listener);
	}

	/**
	 * Perform the analysis.
	 * 
	 * @param source
	 *            Data source to consider.
	 * @param prefs
	 *            Preferences to abide by.
	 * @return the conflict status of the given data source to the developer's environment.
	 */
	private ConflictResult calculateConflict(DataSource source, ProjectPreferences prefs) {
		ResultStatus status = null;
		long start = System.currentTimeMillis();

		try {
			if (source.getKind().equals(RepoKind.HG)) {

				System.out.println("ConflictDaemon::calculateConflict( " + source + ", ... )");

				status = HgStateChecker.getState(prefs, source);

				// System.out.println("ConflictDaemon::calculateConflict( " + source + ", ... ) - caluculated: " +
				// status);

			} else if (source.getKind().equals(RepoKind.GIT)) {
				// Git isn't implemented yet
				System.err.println("ConflictDaemon::caluclateConflict(..) - Cannot handle RepoKind: " + source.getKind());

			} else {
				System.err.println("ConflictDaemon::caluclateConflict(..) - Cannot handle RepoKind: " + source.getKind());
			}
			System.out.println("ConflictDaemon::calculateConflict(..) - computed conflicts in: " + TimeUtility.msToHumanReadableDelta(start));
			ConflictResult result = new ConflictResult(source, status);
			return result;
		} catch (IOException ioe) {
			System.err.println("ConflictDaemon::calculateConflict(..) - error: " + ioe.getMessage());
			ioe.printStackTrace();
		} catch (Exception e) {
			System.err.println("ConflictDaemon::calculateConflict(..) - caught exception: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	public ConflictResult calculateConflicts(DataSource source, ProjectPreferences prefs) {
		ConflictResult result = calculateConflict(source, prefs);
		_resultMap.put(source, result);

		for (ComputationListener cl : _listeners) {
			cl.update();
		}

		return result;
	}

	/**
	 * 
	 * @param source
	 * @param prefs
	 * @return
	 */
	public ConflictResult getStatus(DataSource source) {
		ConflictResult result = _resultMap.get(source);

		if (result == null) {
			// if we don't have a result, pretend it is pending.
			_resultMap.put(source, new ConflictResult(source, ResultStatus.PENDING));
			// TODO: actually start the pending operation testing
		}

		return _resultMap.get(source);
	}

	public static ConflictDaemon getInstance() {
		if (_instance == null) {
			_instance = new ConflictDaemon();
		}
		return _instance;
	}

	public Collection<ConflictResult> getResults() {
		return _resultMap.values();
	}
}
