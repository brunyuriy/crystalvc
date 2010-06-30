package crystal.client;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JLabel;

import crystal.client.ConflictResult.ResultStatus;


public class ConflictClient implements IConflictClient {
	private ClientPreferences _preferences;
	JFrame _frame = null;

	public void createAndShowGUI(ClientPreferences prefs) {
		_preferences = prefs;

		// Create and set up the window.
		_frame = new JFrame("Conflict Client");

		refresh();

		ConflictDaemon.computeConflicts(_preferences, this);
	}

	private String createText(ClientPreferences prefs) {
		String pre = "<html>";
		String post = "</html>";

		return pre + createHeader(prefs) + createBody(prefs) + post;
	}

	private String createBody(ClientPreferences prefs) {
		String pre = "<tr>";

		String rows = "";

		// my status
		rows += "<td>status</td>";

		for (DataSource source : prefs.getDataSources()) {
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
				}
				String iconPrefix = "http://www.cs.washington.edu/homes/rtholmes/tmp/speculationImages/";
				rBody = "<td align='center' bgcolor='" + bgColour + "'>" + "<img src='" + iconPrefix + icon + "'/>" + "</td>";
			} else {
				rBody = "<td align='center'>" + "n/a" + "</td>";
			}
			String rPost = "";
			rows += rPre + rBody + rPost;
		}

		String post = "</tr>";
		return pre + rows + post;
	}

	private String createHeader(ClientPreferences prefs) {
		String pre = "<tr>";

		String rows = "";

		// my status
		rows += "<td><b>My Status</b></td>";

		for (DataSource source : prefs.getDataSources()) {
			String rPre = "";
			String rBody = "<td><b>" + source.getShortName() + "</b></td>";
			String rPost = "";
			rows += rPre + rBody + rPost;
		}

		String post = "</tr>";
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

}