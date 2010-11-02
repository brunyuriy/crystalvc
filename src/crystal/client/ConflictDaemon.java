package crystal.client;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import crystal.model.LocalStateResult;
import crystal.model.DataSource;
import crystal.model.LocalStateResult.LocalState;
import crystal.model.Relationship;
import crystal.model.DataSource.RepoKind;
import crystal.server.HgStateChecker;
import crystal.util.TimeUtility;

/**
 * Daemon that decouples the UI from the analysis. This class can be extended to perform the analysis on an external
 * machine, enable caching, or serve tea without having to update the UI.
 * 
 * ConflictDaemon is a singleton.
 * 
 * @author rtholmes & brun
 */
public class ConflictDaemon {

	private Logger _log = Logger.getLogger(this.getClass());

	Vector<ComputationListener> _listeners = new Vector<ComputationListener>();

	public interface ComputationListener {
		public void update();
	}

	private static ConflictDaemon _instance = null;

	/**
	 * Stores the results of the analysis. This provides a simple decoupling between the DataSource and the
	 * ConflictResult.
	 */
	private Hashtable<DataSource, Relationship> _relationshipMap = new Hashtable<DataSource, Relationship>();
	private Hashtable<DataSource, LocalStateResult> _localStateMap = new Hashtable<DataSource, LocalStateResult>();

	private ConflictDaemon() {
	}

	public void addListener(ComputationListener listener) {
		if (!_listeners.contains(listener)) {
			_listeners.add(listener);
		}
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
	public Relationship calculateRelationship(DataSource source, ProjectPreferences prefs) {
		String relationship = null;
		long start = System.currentTimeMillis();

		try {
			if (source.getKind().equals(RepoKind.HG)) {

				_log.trace("ConflictDaemon::calculateRelationship( " + source + ", ... )");

				relationship = HgStateChecker.getRelationship(prefs, source);
				if (relationship == null)
					relationship = Relationship.ERROR;

				_log.info("Relationship calculated::" + source + "::" + relationship);

			} else if (source.getKind().equals(RepoKind.GIT)) {
				// Git isn't implemented yet
				_log.error("ConflictDaemon::caluclateRelationship(..) - Cannot handle RepoKind: " + source.getKind());

			} else {
				_log.error("ConflictDaemon::caluclateRelationship(..) - Cannot handle RepoKind: " + source.getKind());
			}
			_log.info("Computed relationship for: " + source + " in: " + TimeUtility.msToHumanReadableDelta(start));

			Relationship result = getRelationship(source);
			if (result != null) {
				result = new Relationship(relationship, result.getIcon(), result.getImage());
			} else {
				result = new Relationship(relationship, null, null);
			}
			_relationshipMap.put(source, result);
			/* don't see how this is possible, so removing:
			if (result == null)
				result = new Relationship(source, new Relationship(Relationship.ERROR), null);
			*/
			for (ComputationListener cl : _listeners) {
				cl.update();
			}
			return result;
		}  catch (IOException ioe) {
			_log.error(ioe);
		} catch (RuntimeException re) {
			_log.error("Runtime Exception caught while getting state for: " + source + "\n" + re.getMessage());
			re.printStackTrace();
		} catch (Exception e) {
			_log.error(e);
		} 
		return null;
	}

	public LocalStateResult calculateLocalState(ProjectPreferences prefs) {
		LocalState localState = null;
		long start = System.currentTimeMillis();

		DataSource source = prefs.getEnvironment();

		try {
			if (source.getKind().equals(RepoKind.HG)){

				_log.trace("ConflictDaemon::calculateLocalState( " + source + ", ...)");

				localState = HgStateChecker.getLocalState(prefs);
				if (localState == null)
					localState = LocalState.ERROR;

				_log.info("Local State calculated::" + source + "::" + localState);

			} else if (source.getKind().equals(RepoKind.GIT)) {
				// Git isn't implemented yet
				_log.error("ConflictDaemon::calculateLocalState(..)- Cannot handle RepoKind: " + source.getKind());
			} else {
				_log.error("ConflictDaemon::calculateLocalState(..)- Cannot handle RepoKind: " + source.getKind());
			}
			_log.info("Computed local state for: " + source + " in: " + TimeUtility.msToHumanReadableDelta(start));

			LocalStateResult result = getLocalState(source);
			if (result != null) {
				result = new LocalStateResult(prefs.getEnvironment(), localState, result.getLastLocalState());
			} else {
				result = new LocalStateResult(prefs.getEnvironment(), localState, null);
			}
			_localStateMap.put(prefs.getEnvironment(), result);
			
			for (ComputationListener cl : _listeners) {
				cl.update();
			}

			return result;
		}  catch (IOException ioe) {
			_log.error(ioe);
		} catch (RuntimeException re) {
			_log.error("Runtime Exception caught while getting state for: " + source + "\n" + re.getMessage());
			re.printStackTrace();
		} catch (Exception e) {
			_log.error(e);
		} 
		return null;
	}
	
	

	/**
	 * 
	 * @param source
	 * @return
	 */
	public Relationship getRelationship(DataSource source) {
		Relationship relationship = _relationshipMap.get(source);

		if (relationship == null) {
			// if we don't have a relationship, pretend it is pending.
			relationship = new Relationship(Relationship.PENDING, null, null);
			_relationshipMap.put(source, relationship);
		}

		return relationship;
	}
	
	public LocalStateResult getLocalState(DataSource source) {
		LocalStateResult localState = _localStateMap.get(source);
		
		if (localState == null) {
			// we don't have a local state, pretend it is pending.
			localState = new LocalStateResult(source, LocalState.PENDING, null);
			_localStateMap.put(source, localState);
		}
		
		return localState;
	}

	public static ConflictDaemon getInstance() {
		if (_instance == null) {
			_instance = new ConflictDaemon();
		}
		return _instance;
	}

	public Collection<Relationship> getRelationships() {
		return _relationshipMap.values();
	}
	
	public Collection<LocalStateResult> getLocalStates() {
		return _localStateMap.values();
	}

	public void prePerformCalculations(ClientPreferences prefs) {

		// for each project
		for (ProjectPreferences pp : prefs.getProjectPreference()) {
			
			// first look up the local state
			DataSource ps = pp.getEnvironment();
			_localStateMap.put(ps, new LocalStateResult(ps, LocalState.PENDING, _localStateMap.get(ps).getLocalState()));
			
			// and then the relationships
			for (DataSource ds : pp.getDataSources()) {
				_relationshipMap.put(ds, new Relationship(Relationship.PENDING, _relationshipMap.get(ds).getIcon(), _relationshipMap.get(ds).getImage()));
			}
		}
	}
	
}
