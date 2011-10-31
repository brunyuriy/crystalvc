package crystal.client;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import crystal.model.LocalStateResult;
import crystal.model.DataSource;
import crystal.model.Relationship;
import crystal.model.DataSource.RepoKind;
import crystal.server.GitStateChecker;
import crystal.server.HgStateChecker;
import crystal.util.TimeUtility;

/**
 * ConflictDaemon decouples the UI from the analysis.  
 *  
 * ConflictDaemon is a singleton.
 * 
 * @author rtholmes
 * @author brun
 */
public class ConflictDaemon {
    
	private Logger _log = Logger.getLogger(this.getClass());
	
    private boolean _enabled;

	/**
	 *  A set of listeners for this ConflictDaemon.    
	 */
	Vector<ComputationListener> _listeners = new Vector<ComputationListener>();

	/**
	 * An Interface for a computational listener.  
	 * Whenever the ConflictDaemon finishes a task, it calls .update on the listeners
	 * to update their status.   
	 */
	public interface ComputationListener {
		public void update();
	}

	// the singleton value
	private static ConflictDaemon _instance = null;

	/**
	 * _relationshipMap and _localStateMap store the results of the analysis. 
	 */
	private Hashtable<DataSource, Relationship> _relationshipMap = new Hashtable<DataSource, Relationship>();
	private Hashtable<DataSource, LocalStateResult> _localStateMap = new Hashtable<DataSource, LocalStateResult>();

	// disable constructor	
	private ConflictDaemon() {
	    _enabled = true;
	}
	
	/**
	 * Enables or disables the daemon
	 * @param enable
	 */
	public void enable(boolean enable) {
	    _enabled = enable;
	}
	
	/**
	 * @return true iff the daemon is enabled
	 */
	public boolean isEnabled() {
	    return _enabled;
	}

	/**
	 * Adds a listener
	 * @param listener: the new listener to add
	 */
	public void addListener(ComputationListener listener) {
		if (!_listeners.contains(listener)) {
			_listeners.add(listener);
		}
	}
	
	/**
	 * Removes all listeners
	 */
	public void removeAllListeners() {
	    _listeners.removeAllElements();
	}

	/**
	 * Computes the relationship between my repo and one given other repo.
	 * 
	 * @param source: data source (repo) to consider.
	 * @param prefs: the configuration to use.
	 * @return the relationship between the given data source and the developer's environment.
	 */
	public Relationship calculateRelationship(DataSource source, ProjectPreferences prefs) {
	    if(!_enabled)
	        return null;
	    
		String relationship = null;
		long start = System.currentTimeMillis();

		try {
			if (source.getKind().equals(RepoKind.HG)) {

				_log.trace("ConflictDaemon::calculateRelationship( " + source + ", ... )");

				String oldRelationship = _relationshipMap.get(source).getName();
				relationship = HgStateChecker.getRelationship(prefs, source, oldRelationship);
				if (relationship == null)
					relationship = Relationship.ERROR;

				_log.info("Relationship calculated::" + source + "::" + relationship);

			} else if (source.getKind().equals(RepoKind.GIT)) {
				
				_log.trace("ConflictDaemon::calculateRelationship( " + source + ", ... )");

				String oldRelationship = _relationshipMap.get(source).getName();
				relationship = GitStateChecker.getRelationship(prefs, source, oldRelationship);
				if (relationship == null) 
					relationship = Relationship.ERROR;

				_log.info("Relationship calculated::" + source + "::" + relationship);
				

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
//		}  catch (IOException ioe) {
//			_log.error(ioe);
		} catch (RuntimeException re) {
			_log.error("Runtime Exception caught while getting state for: " + source + "\n" + re.getMessage());
			re.printStackTrace();
		} catch (Exception e) {
			_log.error(e);
		} 
		return null;
	}

	/**
	 * Computes the local state of my repo.
	 * 
	 * @param prefs: the configuration to use.
	 * @return the local state of the developer's environment.
	 */
	public LocalStateResult calculateLocalState(ProjectPreferences prefs) {
	    
	    if (!_enabled)
	        return null;
	    
		String localState = null;
		long start = System.currentTimeMillis();

		DataSource source = prefs.getEnvironment();

		try {
			if (source.getKind().equals(RepoKind.HG)){

				_log.trace("ConflictDaemon::calculateLocalState( " + source + ", ...)");

				localState = HgStateChecker.getLocalState(prefs);
				if (localState == null)
					localState = LocalStateResult.ERROR;

				_log.info("Local State calculated::" + source + "::" + localState);

			} else if (source.getKind().equals(RepoKind.GIT)) {
				
				_log.trace("ConflictDaemon::calculateLocalState( " + source + ", ...)");

				localState = GitStateChecker.getLocalState(prefs);
				if (localState == null)
					localState = LocalStateResult.ERROR;

				_log.info("Local State calculated::" + source + "::" + localState);

			} else {
				_log.error("ConflictDaemon::calculateLocalState(..)- Cannot handle RepoKind: " + source.getKind());
			}
			_log.info("Computed local state for: " + source + " in: " + TimeUtility.msToHumanReadableDelta(start));

			LocalStateResult result = getLocalState(source);
			if (result != null) {
				result = new LocalStateResult(prefs.getEnvironment(), localState, result.getLastLocalState(), result.getLastAction(), result.getLastErrorMessage());
			} else {
				result = new LocalStateResult(prefs.getEnvironment(), localState, null, null, null);
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
	 * Looks up the last calculated relationship between the environment and the given source.
	 * 
	 * @param source
	 * @return the current known relationship between the environment and the given source
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
	
	/**
	 * Looks up the last calculated local state of the given source.
	 * 
	 * @param source
	 * @return the current known local state of the given source
	 */
	public LocalStateResult getLocalState(DataSource source) {
		LocalStateResult localState = _localStateMap.get(source);
		
		if (localState == null) {
			// we don't have a local state, pretend it is pending.
			localState = new LocalStateResult(source, LocalStateResult.PENDING, null, null, null);
			_localStateMap.put(source, localState);
		}
		
		return localState;
	}

	/**
	 * @return the singleton instance of ConflictDaemon  
	 */
	public static ConflictDaemon getInstance() {
		if (_instance == null) {
			_instance = new ConflictDaemon();
		}
		return _instance;
	}

	/**
	 * @return a collection of all the relationships over all the repos,
	 * as compared to the environment.
	 */
	public Collection<Relationship> getRelationships() {
		return _relationshipMap.values();
	}
	
	/**
	 * @return a collection of all the local states over all the projects.
	 */	
	public Collection<LocalStateResult> getLocalStates() {
		return _localStateMap.values();
	}

	/**
	 * Performs the local state computations for each project 
	 * and the relationships between the environment and each repository in each project. 	
	 * 
	 * @param prefs: the configuration over which to do the computations
	 */
	public void prePerformCalculations(ClientPreferences prefs) {
	    
	    if(!_enabled)
	        return;

		// for each project
		for (ProjectPreferences pp : prefs.getProjectPreference()) {
			
			// first look up the local state
			DataSource ps = pp.getEnvironment();
			_localStateMap.put(ps, new LocalStateResult(ps, LocalStateResult.PENDING, _localStateMap.get(ps).getLocalState(), 
			        _localStateMap.get(ps).getAction(), _localStateMap.get(ps).getErrorMessage()));
			
			// and then the relationships
			for (DataSource ds : pp.getDataSources()) {
				_relationshipMap.put(ds, new Relationship(Relationship.PENDING, _relationshipMap.get(ds).getIcon(), _relationshipMap.get(ds).getImage()));
			}
		}
	}
	
}
