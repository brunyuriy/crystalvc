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

public class ConflictClient implements IConflictClient {
	private ClientPreferences _preferences;
	JFrame _frame = null;

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

	public void calculateConflicts() {
		for (ProjectPreferences projPref : _preferences.getProjectPreference()) {
			for (final DataSource source : projPref.getDataSources()) {
				CalculateTask ct = new CalculateTask(source, projPref);
				ct.execute();
			}
		}
	}

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

		// NOTE: off by one on maxSources?

		for (ProjectPreferences pPref : prefs.getProjectPreference()) {
			String rowText = createHeader(pPref, maxSources) + createBody(pPref, maxSources);
			System.out.println("ConflictClient::createText(..) - row text: " + rowText);
			body += rowText;
		}
		// return pre + createHeader(prefs) + createBody(prefs) + post;
		String retValue = pre + body + post;
		System.out.println("ConflictClient::createText(..): " + retValue);
		return retValue;
	}

	private String createBody(ProjectPreferences prefs, int numColumns) {
		String pre = "<tr>";

		String rows = "";

		// my status
		rows += "<td>" + prefs.getEnvironment().getShortName() + "</td>";

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
					icon = "look.png";
				} else if (status.equals(ResultStatus.AHEAD)) {
					bgColour = "yellow";
					icon = "split.png";
				} else if (status.equals(ResultStatus.BEHIND)) {
					bgColour = "#FFA500";
					icon = "yield.png";
				} else if (status.equals(ResultStatus.CONFLICT)) {
					bgColour = "red";
					icon = "stop.png";
				} else if (status.equals(ResultStatus.PENDING)) {
					bgColour = "#CCCCFF";
					icon = "clock.png";
				}
				String iconPrefix = "http://www.cs.washington.edu/homes/rtholmes/tmp/speculationImages/";
				rBody = "<td align='center' bgcolor='" + bgColour + "'>" + "<img src='" + iconPrefix + icon + "'/>" + "</td>";
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

	private String createHeader(ProjectPreferences projectPreferences, int numColumns) {
		String pre = "<tr>";

		String rows = "";

		// my status
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

	Hashtable<DataSource, ConflictResult> resultMap = new Hashtable<DataSource, ConflictResult>();

	@Override
	public void setStatus(ConflictResult result) {
		System.out.println("SetStatus: " + result);
		resultMap.put(result.getDataSource(), result);
		refresh();
	}

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

	class CalculateTask extends SwingWorker<Void, ConflictResult> {
		DataSource _source;
		ProjectPreferences _prefs;

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
			for (ConflictResult cr : chunks)
				setStatus(cr);
		}
	}

	public void close() {
		_frame.setVisible(false);
	}

}