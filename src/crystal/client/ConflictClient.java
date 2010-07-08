package crystal.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import crystal.model.ConflictResult;
import crystal.model.DataSource;
import crystal.model.ConflictResult.ResultStatus;

/**
 * Conflict Client UI; displays the view showing the state of the repositories contained in the preferences.
 * 
 * @author rtholmes
 * 
 */
public class ConflictClient implements IConflictClient {
	/**
	 * This class enables the calcualtions to happen on a background thread but _STILL_ update the UI. When we were
	 * doing the analysis on a regular Thread the UI woudln't update until all of the tasks were done; the UI didn't
	 * block, but it didn't update either. This fixes that problem.
	 * 
	 * @author rtholmes
	 */
	class CalculateTask extends SwingWorker<Void, ConflictResult> {
		ProjectPreferences _prefs;
		DataSource _source;

		/**
		 * Constructor.
		 * 
		 * @param source
		 * @param prefs
		 */
		CalculateTask(DataSource source, ProjectPreferences prefs) {
			_source = source;
			_prefs = prefs;
		}

		@Override
		protected Void doInBackground() throws Exception {

			ConflictResult calculatingPlaceholder = new ConflictResult(_source, ResultStatus.PENDING);
			publish(calculatingPlaceholder);

			ConflictResult result = ConflictDaemon.calculateConflict(_source, _prefs);

			publish(result);
			return null;
		}

		@Override
		protected void process(List<ConflictResult> chunks) {
			for (ConflictResult cr : chunks) {
				setStatus(cr);
			}
		}
	}

	/**
	 * UI frame.
	 */
	private JFrame _frame = null;

	/**
	 * Preference store used by the client.
	 */
	private ClientPreferences _preferences;

	/**
	 * Stores the results of the analysis. This provides a simple decoupling between the DataSource and the
	 * ConflictResult.
	 */
	Hashtable<DataSource, ConflictResult> resultMap = new Hashtable<DataSource, ConflictResult>();

	/**
	 * Runs the analysis on any any projects described by the preferences.
	 */
	public void calculateConflicts() {
		for (ProjectPreferences projPref : _preferences.getProjectPreference()) {
			for (final DataSource source : projPref.getDataSources()) {
				CalculateTask ct = new CalculateTask(source, projPref);
				ct.execute();
			}
		}
	}

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
				resultMap.put(source, new ConflictResult(source, ResultStatus.PENDING));
			}
		}

		refresh();
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

		Vector<DataSource> sources = new Vector<DataSource>();
		sources.addAll(projectPreferences.getDataSources());
		Collections.sort(sources, new Comparator<DataSource>() {

			@Override
			public int compare(DataSource o1, DataSource o2) {
				return o1.getShortName().compareTo(o2.getShortName());
			}

		});

		for (DataSource source : sources) {
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
		Collections.sort(sources, new Comparator<DataSource>() {

			@Override
			public int compare(DataSource o1, DataSource o2) {
				return o1.getShortName().compareTo(o2.getShortName());
			}

		});

		for (DataSource source : sources) {
			String rPre = "";

			String rBody = "";
			if (resultMap.containsKey(source)) {
				ResultStatus status = resultMap.get(source).getStatus();

				String bgColour = "";
				String icon = "";
				if (status.equals(ResultStatus.SAME)) {
					bgColour = "white";
					icon = "same.png";
				} else if (status.equals(ResultStatus.AHEAD)) {
					bgColour = "yellow";
					icon = "ahead.png";
				} else if (status.equals(ResultStatus.BEHIND)) {
					bgColour = "#FFA500";
					icon = "behind.png";
				} else if (status.equals(ResultStatus.CONFLICT)) {
					bgColour = "red";
					icon = "mergeconflict.png";
				} else if (status.equals(ResultStatus.PENDING)) {
					bgColour = "#CCCCFF";
					icon = "clock.png";
				}
				String iconPrefix = "http://www.cs.washington.edu/homes/rtholmes/tmp/speculationImages/";
				rBody = "<td align='center' bgcolor='" + bgColour + "'>" + "<img src='" + iconPrefix + icon + "' height='32px'/>" + "</td>";
			} else {
				rBody = "<td align='center'>" + "n/a" + "</td>";
			}

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
			System.out.println("ConflictClient::createText(..) - row text: " + rowText);
			body += rowText;
		}

		String retValue = pre + body + post;
		System.out.println("ConflictClient::createText(..): " + retValue);
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
		_frame.setVisible(true);
	}

	@Override
	public void setStatus(ConflictResult result) {
		System.out.println("ConflictClient::setStatus( " + result + ")");
		resultMap.put(result.getDataSource(), result);
		refresh();
	}

}