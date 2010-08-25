package crystal.client;

import java.awt.ComponentOrientation;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

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
	 * A map that tells us which ImageIcon contained in a JLabel) corresponds to each DataSource on the GUI.
	 */
	private HashMap<DataSource, JLabel> _iconMap = null;

	/**
	 * Preference store used by the client.
	 */
	private ClientPreferences _preferences;

	public ConflictClient() {
		super();
	}

	// /**
	// * Runs the analysis on any any projects described by the preferences.
	// */
	// public void calculateConflicts() {
	// for (ProjectPreferences projPref : _preferences.getProjectPreference()) {
	// for (final DataSource source : projPref.getDataSources()) {
	// CalculateTask ct = new CalculateTask(source, projPref);
	// ct.execute();
	// }
	// }
	// }

	/**
	 * Close the ConflictClient UI.
	 */
	public void close() {
		if (ConflictSystemTray.TRAY_SUPPORTED)
			_frame.setVisible(false);
		else
			ConflictSystemTray.getInstance().exitAction();
	}

	private JMenuItem _update = null;
	private JMenuItem _disableDaemon;

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

		// Set up the menu:
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		// JMenu aboutMenu = new JMenu("About");
		menuBar.add(fileMenu);
		// menuBar.add(aboutMenu);

		_update = new JMenuItem("Update Now");
		JMenuItem editConfiguration = new JMenuItem("Edit Configuration");
		_disableDaemon = new JMenuItem("Disable Daemon");
		JMenuItem exit = new JMenuItem("Exit");
		JMenuItem about = new JMenuItem("About");

		fileMenu.add(_update);
		fileMenu.add(editConfiguration);
		fileMenu.add(_disableDaemon);
		fileMenu.add(exit);
		// aboutMenu.add(about);
		menuBar.add(about);

		about.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ConflictSystemTray.getInstance().aboutAction();
			}
		});

		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ConflictSystemTray.getInstance().exitAction();
			}
		});

		_update.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_log.info("Update now manually selected.");
				setCanUpdate(false);
				ConflictSystemTray.getInstance().performCalculations();
			}
		});

		editConfiguration.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ConflictSystemTray.getInstance().preferencesAction();
			}
		});

		_disableDaemon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ConflictSystemTray.getInstance().daemonAbleAction();
			}
		});

		_frame.setJMenuBar(menuBar);

		// Now set up the rest of the frame
		_frame.getContentPane().setLayout(new BoxLayout(_frame.getContentPane(), BoxLayout.Y_AXIS));

		// Create a notification that quitting saves.
		// _frame.getContentPane().add(new JLabel("Quitting Crystal saves your configuration.   ",
		// SwingConstants.CENTER));
		// or do it in the menu; looks nicer.
		menuBar.add(new JMenuItem("Quitting Crystal saves your configuration."));

		// Create a grid to hold the conflict results
		int maxSources = 0;
		for (ProjectPreferences projPref : prefs.getProjectPreference()) {
			if (projPref.getDataSources().size() > maxSources)
				maxSources = projPref.getDataSources().size();
		}
		// 1 extra in each dimension for heading labels
		JPanel grid = new JPanel(new GridLayout(prefs.getProjectPreference().size(), 0)); // no need to have maxSources
		// + 1;
		grid.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

		// Create the iconMap and populate it with icons.
		// Also create the layout of the GUI.
		_iconMap = new HashMap<DataSource, JLabel>();
		for (ProjectPreferences projPref : prefs.getProjectPreference()) {
			// name of project on the left
			grid.add(new JLabel(projPref.getEnvironment().getShortName()));

			for (DataSource source : projPref.getDataSources()) {
				ImageIcon image = new ImageIcon();
				JLabel imageLabel = new JLabel(source.getShortName(), image, SwingConstants.CENTER);
				_iconMap.put(source, imageLabel);
				ConflictDaemon.getInstance().getStatus(source);
				imageLabel.setVerticalTextPosition(JLabel.TOP);
				imageLabel.setHorizontalTextPosition(JLabel.CENTER);
				grid.add(imageLabel);
			}
			// Fill in the rest of the grid row with blanks
			for (int i = projPref.getDataSources().size(); i < maxSources; i++)
				grid.add(new JLabel());

			_frame.getContentPane().add(grid);
		}

		/*
		 * Reid's old code: // set all cells to pending on initial load // NOTE: caching might be a good idea here in
		 * the future. for (ProjectPreferences projPref : prefs.getProjectPreference()) { for (DataSource source :
		 * projPref.getDataSources()) {
		 * 
		 * // XXX: should set pending status for new requests // ConflictDaemon.getInstance().calculateConflicts(source,
		 * projPref); // resultMap.put(source, new ConflictResult(source, ResultStatus.PENDING)); } }
		 */

		refresh();

		_frame.setVisible(true);
		_frame.toFront();
	}

	public void setCanUpdate(boolean enable) {
		if (enable) {
			_update.setText("Update Now");
			_update.setEnabled(true);
		} else {
			_update.setText("Updating...");
			_update.setEnabled(false);
		}
	}

	public void setDaemonEnabled(boolean enable) {
		if (enable) {
			_disableDaemon.setText("Disable Daemon");
		} else {
			_disableDaemon.setText("Enable Daemon");
		}
	}

	/*
	 * /** Creates the HTML for header row for a project.
	 * 
	 * @param prefs ProjectPreferences to consider.
	 * 
	 * @param numColumns The maximum number of columns that should be displayed; enables HTML padding.
	 * 
	 * @return the HTML for the project rows. / private String createHeader(ProjectPreferences projectPreferences, int
	 * numColumns) { String pre = "<tr>";
	 * 
	 * String rows = "";
	 * 
	 * rows += "<td><b></b></td>";
	 * 
	 * // Vector<DataSource> sources = new Vector<DataSource>(); // sources.addAll(projectPreferences.getDataSources());
	 * // Collections.sort(sources, new Comparator<DataSource>() { // // @Override // public int compare(DataSource o1,
	 * DataSource o2) { // return o1.getShortName().compareTo(o2.getShortName()); // } // // });
	 * 
	 * for (DataSource source : projectPreferences.getDataSources()) { String rPre = ""; String rBody = "<td><b>" +
	 * source.getShortName() + "</b></td>"; String rPost = ""; rows += rPre + rBody + rPost; }
	 * 
	 * String post = ""; if (numColumns > projectPreferences.getDataSources().size()) { for (int i = 0; i < numColumns -
	 * projectPreferences.getDataSources().size(); i++) { post += "<td></td>"; } }
	 * 
	 * post += "</tr>";
	 * 
	 * return pre + rows + post; }
	 * 
	 * /** Creates the HTML for content row for a project.
	 * 
	 * @param prefs ProjectPreferences to consider.
	 * 
	 * @param numColumns The maximum number of columns that should be displayed; enables HTML padding.
	 * 
	 * @return the HTML for the project rows. / private String createProjectRow(ProjectPreferences prefs, int
	 * numColumns) { String pre = "<tr>";
	 * 
	 * String rows = "";
	 * 
	 * // my status rows += "<td>" + prefs.getEnvironment().getShortName() + "</td>";
	 * 
	 * // sort the columns so they're stable in subsequent runs of the client Vector<DataSource> sources = new
	 * Vector<DataSource>(); sources.addAll(prefs.getDataSources()); // Collections.sort(sources, new
	 * Comparator<DataSource>() { // // @Override // public int compare(DataSource o1, DataSource o2) { // return
	 * o1.getShortName().compareTo(o2.getShortName()); // } // // });
	 * 
	 * for (DataSource source : prefs.getDataSources()) { String rPre = "";
	 * 
	 * String rBody = ""; // if (resultMap.containsKey(source)) { // ResultStatus status =
	 * resultMap.get(source).getStatus(); ConflictResult conflictStatus =
	 * ConflictDaemon.getInstance().getStatus(source); ResultStatus status = conflictStatus.getStatus();
	 * 
	 * String bgColour = ""; String icon = ""; String DEFAULT_BG = "grey"; if (status.equals(ResultStatus.SAME)) {
	 * bgColour = DEFAULT_BG;// "white"; icon = "same.png"; } else if (status.equals(ResultStatus.AHEAD)) { bgColour =
	 * DEFAULT_BG;// "yellow"; icon = "ahead.png"; } else if (status.equals(ResultStatus.BEHIND)) { bgColour =
	 * DEFAULT_BG;// "#FFA500"; icon = "behind.png"; } else if (status.equals(ResultStatus.MERGECLEAN)) { bgColour =
	 * DEFAULT_BG;// i dunno; icon = "merge.png"; } else if (status.equals(ResultStatus.MERGECONFLICT)) { bgColour =
	 * DEFAULT_BG;// "red"; icon = "mergeconflict.png"; } else if (status.equals(ResultStatus.PENDING)) { bgColour =
	 * DEFAULT_BG;// "#CCCCFF"; icon = "clock.png"; } else if (status.equals(ResultStatus.ERROR)) { bgColour =
	 * DEFAULT_BG;// "#CCCCFF"; icon = "error.png"; } String iconPrefix =
	 * "http://www.cs.washington.edu/homes/rtholmes/tmp/speculationImages/"; rBody = "<td align='center' bgcolor='" +
	 * bgColour + "'>" + "<img src='" + iconPrefix + icon + "'/>" + "</td>"; // } else { // rBody =
	 * "<td align='center'>" + "n/a" + "</td>"; // }
	 * 
	 * String rPost = ""; rows += rPre + rBody + rPost; }
	 * 
	 * String post = ""; if (numColumns > sources.size()) { for (int i = 0; i < numColumns - sources.size(); i++) { post
	 * += "<td></td>"; } } post += "</tr>";
	 * 
	 * return pre + rows + post; }
	 * 
	 * /** Creates the body of the ConflictClient UI. Right now this simply makes a HTML table and fires it into the
	 * space since that is a lot easier than dealing with Swing UI elements.
	 * 
	 * @param prefs preferences used to create the body representaiton.
	 * 
	 * @return HTML corresponding to the UI body. / private String createText(ClientPreferences prefs) { String pre =
	 * "<html> <p>Quitting Crystal saves your configuration.</p>"; String post = "</html>";
	 * 
	 * String body = ""; int maxSources = 0; for (ProjectPreferences pPref : prefs.getProjectPreference()) { int
	 * numSources = pPref.getDataSources().size(); if (numSources > maxSources) maxSources = numSources; }
	 * 
	 * for (ProjectPreferences pPref : prefs.getProjectPreference()) { String rowText = createHeader(pPref, maxSources)
	 * + createProjectRow(pPref, maxSources); if (Constants.DEBUG_UI) {
	 * _log.trace("ConflictClient::createText(..) - row text: " + rowText); } body += rowText; }
	 * 
	 * String retValue = pre + body + post; if (Constants.DEBUG_UI) { _log.trace("ConflictClient::createText(..): " +
	 * retValue); } return retValue; }
	 */

	/**
	 * Refreshes the UI.
	 */
	private void refresh() {

		for (ProjectPreferences projPref : _preferences.getProjectPreference()) {
			for (DataSource source : projPref.getDataSources()) {
				JLabel current = _iconMap.get(source);
				current.removeAll();

				ConflictResult conflictStatus = ConflictDaemon.getInstance().getStatus(source);
				ResultStatus status = conflictStatus.getStatus();
				ResultStatus lastStatus = conflictStatus.getLastStatus();

				if (status.equals(ResultStatus.PENDING) && lastStatus != null) {
					// if it's pending, show whatever value it had last time
					current.setIcon(lastStatus.getIcon());
				} else {
					// usual case
					current.setIcon(status.getIcon());
				}
				current.repaint();
			}
		}

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