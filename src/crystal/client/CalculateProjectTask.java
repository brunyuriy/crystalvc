package crystal.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;
import crystal.client.ConflictDaemon.ComputationListener;
import crystal.model.DataSource;
import crystal.model.LocalStateResult;
import crystal.model.RelationshipResult;
import crystal.model.Result;
import crystal.model.RevisionHistory;
import crystal.model.RelationshipResult.Relationship;

/**
 * This class enables the calculations to happen on a background thread but _STILL_ update the UI. When we were doing
 * the analysis on a regular Thread the UI woudln't update until all of the tasks were done; the UI didn't block, but it
 * didn't update either. This fixes that problem.
 * 
 * @author brun
 */
public class CalculateProjectTask extends SwingWorker<Void, Result> {
	
	private Logger _log = Logger.getLogger(this.getClass());
	private ProjectPreferences _prefs;
	private ComputationListener _trayListener;
	private ComputationListener _clientListener;

	/**
	 * Constructor.
	 * 
	 * @param prefs
	 * @param client
	 * @param conflictSystemTray
	 */
	CalculateProjectTask(ProjectPreferences prefs, ComputationListener trayListener, ComputationListener clientListener) {
		_prefs = prefs;

		_trayListener = trayListener;
		_clientListener = clientListener;
	}

	@Override
	protected Void doInBackground() throws Exception {
		
		// First, do the local state.
		// We do this by checking the current local state, updating the GUI (mostly to show the pending icons).
		// And then performing the calculation and updating the GUI again.  
		
		// So, first check the current state:
		// UPDATE: turns out we don't have to do this.
//		LocalStateResult localStatePlaceholder = null;
//		if (ConflictDaemon.getInstance().getLocalState(_prefs.getEnvironment()) != null) {
//			localStatePlaceholder = new LocalStateResult(_prefs.getEnvironment(), LocalState.PENDING, ConflictDaemon.getInstance().getLocalState(_prefs.getEnvironment()).getLocalState());
//		} else {
//			localStatePlaceholder = new LocalStateResult(_prefs.getEnvironment(), LocalState.PENDING, null);
//		}
//
//		// Now update the GUI with current state:
//		publish(localStatePlaceholder);
		
		// Now calculate the new state:
		LocalStateResult localStateResult = ConflictDaemon.getInstance().calculateLocalStates(_prefs);

		_log.trace("Local state computed: " + localStateResult);

		// UPDATE: turns out we don't have to do this.
		// And update the GUI
//		publish(localStateResult);
		

		// Second, do the relationships.
		// We do this by checking the current local state, updating the GUI (mostly to show the pending icons).
		// And then performing the calculation for relationships.
		// And then calculating the Guidance and updating the relationships.
		// And finally updating the GUI.  

		// We'll store the relationships here:
		Map<DataSource, RelationshipResult> relationships = new HashMap<DataSource, RelationshipResult>(); 
		
		// UPDATE: turns out we don't have to do this.
//		// So, first check the current state.  
//		for (DataSource source : _prefs.getDataSources()) {
//			RelationshipResult relationshipPlaceholder = null;
//
//			if (ConflictDaemon.getInstance().getRelationship(source) != null) {
//				relationshipPlaceholder = new RelationshipResult(source, Relationship.PENDING, ConflictDaemon.getInstance().getRelationship(source).getRelationship());
//			} else {
//				relationshipPlaceholder = new RelationshipResult(source, Relationship.PENDING, null);
//			}
//			// And update the GUI with current relationship:
//			publish(relationshipPlaceholder);
//		}
		
		// And then perform the calculations for all the relationships:
		for (DataSource source : _prefs.getDataSources()) {
			RelationshipResult relationshipResult = ConflictDaemon.getInstance().calculateRelationships(source, _prefs);
			relationships.put(source, relationshipResult);
		}
		
		// And then calculate the Guidance and update the relationships:
		RevisionHistory mine = _prefs.getEnvironment().getHistory();
		for (DataSource source : _prefs.getDataSources()) {
			RevisionHistory yours = source.getHistory();
			Relationship ourRelationship = relationships.get(source).getRelationship();
			// calculate the relevant Committers
			ourRelationship.setCommitters(mine.getCommitters(yours));
			
			DataSource parentSource = _prefs.getDataSource((_prefs.getEnvironment().getParent()));
			// If parent is not set, can't compute action
			if (parentSource != null) {
				Relationship parentRelationship = relationships.get(parentSource).getRelationship();
				ourRelationship.calculateAction(localStateResult.getLocalState(), parentRelationship);
			}
			
			DataSource commonParentSource = _prefs.getDataSource(source.getParent());
			// If commonParent is not set, we can't do guidance
			if (commonParentSource != null) {
				RevisionHistory parent = commonParentSource.getHistory();
				// calculate the When
				ourRelationship.setWhen(mine.getWhen(yours, parent, ourRelationship));
				// calculate the Consequences
				ourRelationship.setConsequences(mine.getConsequences(yours, parent, ourRelationship));
				// calculate the Capable
				ourRelationship.setCapable(mine.getCapable(yours, parent, ourRelationship));
				// calculate the Ease
				ourRelationship.setEase(mine.getEase());
			}
			_log.trace("Relationship computed: " + relationships.get(source));
			// And finally update the GUI:
			publish(relationships.get(source));
		}
		return null;
	}

	@Override
	// This never executes
	protected void process(List<Result> chunks) {
		for (Result result : chunks) {
			_log.trace("Processing computed result: " + result);

			if (_trayListener != null)
				_trayListener.update();

			if (_clientListener != null)
				_clientListener.update();
		}
	}
}
