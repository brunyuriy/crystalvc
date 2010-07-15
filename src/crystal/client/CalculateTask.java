package crystal.client;

import java.util.List;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import crystal.client.ConflictDaemon.ComputationListener;
import crystal.model.ConflictResult;
import crystal.model.DataSource;
import crystal.model.ConflictResult.ResultStatus;

/**
 * This class enables the calcualtions to happen on a background thread but _STILL_ update the UI. When we were doing
 * the analysis on a regular Thread the UI woudln't update until all of the tasks were done; the UI didn't block, but it
 * didn't update either. This fixes that problem.
 * 
 * @author rtholmes
 */
class CalculateTask extends SwingWorker<Void, ConflictResult> {
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
	CalculateTask(DataSource source, ProjectPreferences prefs, ComputationListener trayListener, ComputationListener clientListener) {
		_source = source;
		_prefs = prefs;

		_trayListener = trayListener;
		_clientListener = clientListener;
	}

	@Override
	protected Void doInBackground() throws Exception {

		ConflictResult calculatingPlaceholder = new ConflictResult(_source, ResultStatus.PENDING);
		publish(calculatingPlaceholder);

		ConflictResult result = ConflictDaemon.getInstance().calculateConflicts(_source, _prefs);

		_log.trace("ConflictClient::CalcualteTask::publish( " + result + " )");
		publish(result);
		return null;
	}

	@Override
	protected void process(List<ConflictResult> chunks) {
		for (ConflictResult cr : chunks) {
			_log.trace("ConflictClient::CalcualteTask::process( " + cr + " )");

			if (_trayListener != null)
				_trayListener.update();

			if (_clientListener != null)
				_clientListener.update();
		}
	}
}