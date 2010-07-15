package crystal.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.log4j.Logger;

import crystal.Constants;
import crystal.model.ConflictResult;
import crystal.model.DataSource;
import crystal.model.ConflictResult.ResultStatus;

/**
 * Conflict Client UI; displays the view showing the state of the repositories contained in the preferences.
 * 
 * @author rtholmes
 * 
 */
public class ConflictClient implements ConflictDaemon.ComputationListener {
	private Logger _log = Logger.getLogger(this.getClass());

	

	/**
	 * UI frame.
	 */
	private JFrame _frame = null;

	/**
	 * Preference store used by the client.
	 */
	private ClientPreferences _preferences;

//	/**
//	 * Runs the analysis on any any projects described by the preferences.
//	 */
//	public void calculateConflicts() {
//		for (ProjectPreferences projPref : _preferences.getProjectPreference()) {
//			for (final DataSource source : projPref.getDataSources()) {
//				CalculateTask ct = new CalculateTask(source, projPref);
//				ct.execute();
//			}
//		}
//	}

	/**
	 * Close the ConflictClient UI.
	 */
	public void close() {
		_frame.setVisible(false);
	}

	/**
	 * Creates the UI and brings it to the foreground.
	 * 
	 * @param prefs
	 *            Preferences used to populate the UI with.
	 */
	public void createAndShowGUI(ClientPreferences prefs) {
		_preferences = prefs;

		// Create and set up the window.
		_frame = new JFrame("Conflict Client");

		// set all cells to pending on initial load
		// NOTE: caching might be a good idea here in the future.
		for (ProjectPreferences projPref : prefs.getProjectPreference()) {
			for (DataSource source : projPref.getDataSources()) {

				// XXX: should set pending status for new requests
				// ConflictDaemon.getInstance().calculateConflicts(source, projPref);
				ConflictDaemon.getInstance().getStatus(source);
				// resultMap.put(source, new ConflictResult(source, ResultStatus.PENDING));
			}
		}

		refresh();

		_frame.setVisible(true);
		_frame.toFront();
	}

	/**
	 * Creates the HTML for header row for a project.
	 * 
	 * @param prefs
	 *            ProjectPreferences to consider.
	 * @param numColumns
	 *            The maximum number of columns that should be displayed; enables HTML padding.
	 * @return the HTML for the project rows.
	 */
	private String createHeader(ProjectPreferences projectPreferences, int numColumns) {
		String pre = "<tr>";

		String rows = "";

		rows += "<td><b></b></td>";

		// Vector<DataSource> sources = new Vector<DataSource>();
		// sources.addAll(projectPreferences.getDataSources());
		// Collections.sort(sources, new Comparator<DataSource>() {
		//
		// @Override
		// public int compare(DataSource o1, DataSource o2) {
		// return o1.getShortName().compareTo(o2.getShortName());
		// }
		//
		// });

		for (DataSource source : projectPreferences.getDataSources()) {
			String rPre = "";
			String rBody = "<td><b>" + source.getShortName() + "</b></td>";
			String rPost = "";
			rows += rPre + rBody + rPost;
		}

		String post = "";
		if (numColumns > projectPreferences.getDataSources().size()) {
			for (int i = 0; i < numColumns - projectPreferences.getDataSources().size(); i++) {
				post += "<td></td>";
			}
		}

		post += "</tr>";

		return pre + rows + post;
	}

	/**
	 * Creates the HTML for content row for a project.
	 * 
	 * @param prefs
	 *            ProjectPreferences to consider.
	 * @param numColumns
	 *            The maximum number of columns that should be displayed; enables HTML padding.
	 * @return the HTML for the project rows.
	 */
	private String createProjectRow(ProjectPreferences prefs, int numColumns) {
		String pre = "<tr>";

		String rows = "";

		// my status
		rows += "<td>" + prefs.getEnvironment().getShortName() + "</td>";

		// sort the columns so they're stable in subsequent runs of the client
		Vector<DataSource> sources = new Vector<DataSource>();
		sources.addAll(prefs.getDataSources());
		// Collections.sort(sources, new Comparator<DataSource>() {
		//
		// @Override
		// public int compare(DataSource o1, DataSource o2) {
		// return o1.getShortName().compareTo(o2.getShortName());
		// }
		//
		// });

		for (DataSource source : prefs.getDataSources()) {
			String rPre = "";

			String rBody = "";
			// if (resultMap.containsKey(source)) {
			// ResultStatus status = resultMap.get(source).getStatus();
			ConflictResult conflictStatus = ConflictDaemon.getInstance().getStatus(source);
			ResultStatus status = conflictStatus.getStatus();

			String bgColour = "";
			String icon = "";
			String DEFAULT_BG = "grey";
			if (status.equals(ResultStatus.SAME)) {
				bgColour = DEFAULT_BG;// "white";
				icon = "same.png";
			} else if (status.equals(ResultStatus.AHEAD)) {
				bgColour = DEFAULT_BG;// "yellow";
				icon = "ahead.png";
			} else if (status.equals(ResultStatus.BEHIND)) {
				bgColour = DEFAULT_BG;// "#FFA500";
				icon = "behind.png";
			} else if (status.equals(ResultStatus.MERGECLEAN)) {
				bgColour = DEFAULT_BG;// i dunno;
				icon = "merge.png";
			} else if (status.equals(ResultStatus.MERGECONFLICT)) {
				bgColour = DEFAULT_BG;// "red";
				icon = "mergeconflict.png";
			} else if (status.equals(ResultStatus.PENDING)) {
				bgColour = DEFAULT_BG;// "#CCCCFF";
				icon = "clock.png";
			} else if (status.equals(ResultStatus.ERROR)) {
				bgColour = DEFAULT_BG;// "#CCCCFF";
				icon = "error.png";
			}
			String iconPrefix = "http://www.cs.washington.edu/homes/rtholmes/tmp/speculationImages/";
			rBody = "<td align='center' bgcolor='" + bgColour + "'>" + "<img src='" + iconPrefix + icon + "'/>" + "</td>";
			// } else {
			// rBody = "<td align='center'>" + "n/a" + "</td>";
			// }

			String rPost = "";
			rows += rPre + rBody + rPost;
		}

		String post = "";
		if (numColumns > sources.size()) {
			for (int i = 0; i < numColumns - sources.size(); i++) {
				post += "<td></td>";
			}
		}
		post += "</tr>";

		return pre + rows + post;
	}

	/**
	 * Creates the body of the ConflictClient UI. Right now this simply makes a HTML table and fires it into the space
	 * since that is a lot easier than dealing with Swing UI elements.
	 * 
	 * @param prefs
	 *            preferences used to create the body representaiton.
	 * @return HTML corresponding to the UI body.
	 */
	private String createText(ClientPreferences prefs) {
		String pre = "<html>";
		String post = "</html>";

		String body = "";
		int maxSources = 0;
		for (ProjectPreferences pPref : prefs.getProjectPreference()) {
			int numSources = pPref.getDataSources().size();
			if (numSources > maxSources)
				maxSources = numSources;
		}

		for (ProjectPreferences pPref : prefs.getProjectPreference()) {
			String rowText = createHeader(pPref, maxSources) + createProjectRow(pPref, maxSources);
			if (Constants.DEBUG_UI) {
				_log.trace("ConflictClient::createText(..) - row text: " + rowText);
			}
			body += rowText;
		}

		String retValue = pre + body + post;
		if (Constants.DEBUG_UI) {
			_log.trace("ConflictClient::createText(..): " + retValue);
		}
		return retValue;
	}

	/**
	 * Refreshes the UI.
	 */
	private void refresh() {

		_frame.getContentPane().removeAll();

		Container contentPane = _frame.getContentPane();
		JLabel content = new JLabel();

		String labelText = createText(_preferences);
		content.setText(labelText);

		contentPane.add(content, BorderLayout.CENTER);

		// Display the window.
		_frame.pack();
		// _frame.setVisible(true);
	}

	@Override
	public void update() {
		_log.trace("ConflictClient::update()");
		refresh();
	}

	public void show() {
		_frame.setVisible(true);
		_frame.toFront();
	}

}