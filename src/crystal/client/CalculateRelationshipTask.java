package crystal.client;

import java.util.List;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import crystal.client.ConflictDaemon.ComputationListener;
import crystal.model.DataSource;
import crystal.model.RelationshipResult.Relationship;
import crystal.model.RelationshipResult;

/**
 * This class enables the calculations to happen on a background thread but _STILL_ update the UI. When we were doing
 * the analysis on a regular Thread the UI woudln't update until all of the tasks were done; the UI didn't block, but it
 * didn't update either. This fixes that problem.
 * 
 * @author brun & rtholmes
 */
class CalculateRelationshipTask extends SwingWorker<Void, RelationshipResult> {
	private Logger _log = Logger.getLogger(this.getClass());
	private ProjectPreferences _prefs;
	private DataSource _source;
	private ComputationListener _trayListener;
	private ComputationListener _clientListener;

	/**
	 * Constructor.
	 * 
	 * @param source
	 * @param prefs
	 * @param client
	 * @param conflictSystemTray
	 */
	CalculateRelationshipTask(DataSource source, ProjectPreferences prefs, ComputationListener trayListener, ComputationListener clientListener) {
		_source = source;
		_prefs = prefs;

		_trayListener = trayListener;
		_clientListener = clientListener;
	}

	@Override
	protected Void doInBackground() throws Exception {
		RelationshipResult calculatingPlaceholder = null;

		if (ConflictDaemon.getInstance().getRelationship(_source) != null) {
			calculatingPlaceholder = new RelationshipResult(_source, new Relationship(Relationship.PENDING), ConflictDaemon.getInstance().getRelationship(_source).getRelationship());
		} else {
			calculatingPlaceholder = new RelationshipResult(_source, new Relationship(Relationship.PENDING), null);
		}
		
		publish(calculatingPlaceholder);

		RelationshipResult result = ConflictDaemon.getInstance().calculateRelationships(_source, _prefs);

		_log.trace("Relationship computed: " + result);

		publish(result);
		return null;
	}

	@Override
	protected void process(List<RelationshipResult> chunks) {
		for (RelationshipResult cr : chunks) {
			_log.trace("Processing computed result: " + cr);

			if (_trayListener != null)
				_trayListener.update();

			if (_clientListener != null)
				_clientListener.update();
		}
	}
}