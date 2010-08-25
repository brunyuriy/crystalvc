package crystal.client;

import java.util.List;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import crystal.client.ConflictDaemon.ComputationListener;
import crystal.model.LocalStateResult;
import crystal.model.LocalStateResult.LocalState;

/**
 * This class enables the calculations to happen on a background thread but _STILL_ update the UI. When we were doing
 * the analysis on a regular Thread the UI woudln't update until all of the tasks were done; the UI didn't block, but it
 * didn't update either. This fixes that problem.
 * 
 * @author brun
 */
class CalculateLocalStateTask extends SwingWorker<Void, LocalStateResult> {
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
	CalculateLocalStateTask(ProjectPreferences prefs, ComputationListener trayListener, ComputationListener clientListener) {
		_prefs = prefs;

		_trayListener = trayListener;
		_clientListener = clientListener;
	}

	@Override
	protected Void doInBackground() throws Exception {
		LocalStateResult calculatingPlaceholder = null;

		if (ConflictDaemon.getInstance().getLocalState(_prefs.getEnvironment()) != null) {
			calculatingPlaceholder = new LocalStateResult(_prefs.getEnvironment(), LocalState.PENDING, ConflictDaemon.getInstance().getLocalState(_prefs.getEnvironment()).getLocalState());
		} else {
			calculatingPlaceholder = new LocalStateResult(_prefs.getEnvironment(), LocalState.PENDING, null);
		}
		
		publish(calculatingPlaceholder);

		LocalStateResult result = ConflictDaemon.getInstance().calculateLocalStates(_prefs);

		_log.trace("Local state computed: " + result);

		publish(result);
		return null;
	}

	@Override
	protected void process(List<LocalStateResult> chunks) {
		for (LocalStateResult cr : chunks) {
			_log.trace("Processing computed result: " + cr);

			if (_trayListener != null)
				_trayListener.update();

			if (_clientListener != null)
				_clientListener.update();
		}
	}
}