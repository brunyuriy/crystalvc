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

		for (DataSource source : _preferences.getDataSources()) {
			resultMap.put(source, new ConflictResult(source, ResultStatus.PENDING));
		}

		refresh();

		for (final DataSource source : _preferences.getDataSources()) {
			//
			// final ConflictClient client = this;
			// Runnable runnable = new Runnable() {
			//
			// @Override
			// public void run() {
			//
			// ConflictDaemon.calculateConflict(source, _preferences, client);
			//
			// }
			// };
			//
			// SwingUtilities.invokeLater(runnable);
			// }

			CalculateTask ct = new CalculateTask(source, _preferences);
			ct.execute();
		}
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

	class CalculateTask extends SwingWorker<Void, ConflictResult> {
		DataSource _source;
		ClientPreferences _prefs;

		CalculateTask(DataSource source, ClientPreferences prefs) {
			_source = source;
			_prefs = prefs;
		}

		@Override
		protected Void doInBackground() throws Exception {
			// TODO Auto-generated method stub
			ConflictResult result = ConflictDaemon.calculateConflict(_source, _prefs);

			publish(result);
			return null;
		}

		@Override
		protected void process(List<ConflictResult> chunks) {
			// TODO Auto-generated method stub
			// super.process(chunks);
			for (ConflictResult cr : chunks)
				setStatus(cr);

		}
		//		
		// @Override
		// protected void process(List<FlipPair> pairs) {
		// FlipPair pair = pairs.get(pairs.size() - 1);
		// headsText.setText(String.format("%d", pair.heads));
		// totalText.setText(String.format("%d", pair.total));
		// devText.setText(String.format("%.10g",
		// ((double) pair.heads)/((double) pair.total) - 0.5));
		// }

	}

}