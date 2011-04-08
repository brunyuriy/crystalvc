package crystal.client;

import java.awt.ComponentOrientation;
import java.awt.Font;
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
import javax.swing.JToolTip;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import crystal.Constants;
import crystal.model.DataSource;
import crystal.model.LocalStateResult;
import crystal.model.Relationship;
import crystal.util.JMultiLineToolTip;

/**
 * Conflict Client UI; displays the view showing the state of the repositories contained in the preferences.
 * 
 * @author rtholmes & brun
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

	private JMenuItem _refresh = null;
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
		_frame = new JFrame("Crystal");
		_frame.setIconImage((new ImageIcon(Constants.class.getResource("/crystal/client/images/crystal-ball_blue_128.jpg"))).getImage());

		// Set up the menu:
		JMenuBar menuBar = new JMenuBar();
		// could set the manuBar layout to make the last, invisible blank menu item take up all the space.
		// menuBar.setLayout(mgr)
		JMenu fileMenu = new JMenu("File");
		// JMenu aboutMenu = new JMenu("About");
		menuBar.add(fileMenu);
		// menuBar.add(aboutMenu);

		_refresh = new JMenuItem("Refresh");
		JMenuItem editConfiguration = new JMenuItem("Edit Configuration");
		_disableDaemon = new JMenuItem("Disable Daemon");
		JMenuItem exit = new JMenuItem("Exit");
		JMenuItem about = new JMenuItem("About");
		JMenuItem blank = new JMenuItem("");
		blank.setArmed(false);

		fileMenu.add(_refresh);
		fileMenu.add(editConfiguration);
		fileMenu.add(_disableDaemon);
		fileMenu.add(exit);
		// aboutMenu.add(about);
		menuBar.add(about);
		menuBar.add(blank);

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

		_refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_log.info("Refresh manually selected.");
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
//		menuBar.add(new JMenuItem("Quitting Crystal saves your configuration."));

		// Create a grid to hold the conflict results
		int maxSources = 0;
		for (ProjectPreferences projPref : prefs.getProjectPreference()) {
			if (projPref.getNumOfVisibleSources() > maxSources)
				maxSources = projPref.getNumOfVisibleSources();
		}

		JPanel grid = new JPanel(new GridLayout(prefs.getProjectPreference().size(), 0, 0, 0)); 
		grid.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

		// Create the iconMap and populate it with icons.
		// Also create the layout of the GUI.
		_iconMap = new HashMap<DataSource, JLabel>();
		for (ProjectPreferences projPref : prefs.getProjectPreference()) {
			// name of project on the left, with an empty JLabel for the Action
			JPanel name = new JPanel();
			name.setLayout(new BoxLayout(name, BoxLayout.Y_AXIS));
			name.add(new JLabel(projPref.getEnvironment().getShortName()));
//				name.add(new JLabel(" "));
			JLabel action = new JLabel("") {
                private static final long serialVersionUID = 1L;

                public JToolTip createToolTip() {
                    return new JMultiLineToolTip();
                }
            };
			action.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
			_iconMap.put(projPref.getEnvironment(), action);
			ConflictDaemon.getInstance().getLocalState(projPref.getEnvironment());
			name.add(action);
			grid.add(name);
			
			for (DataSource source : projPref.getDataSources()) {
				if (!(source.isHidden())) {
					ImageIcon image = new ImageIcon();
					JLabel imageLabel = new JLabel(source.getShortName(), image, SwingConstants.CENTER) {
						private static final long serialVersionUID = 1L;

						public JToolTip createToolTip() {
							return new JMultiLineToolTip();
						}
					};
					_iconMap.put(source, imageLabel);
					ConflictDaemon.getInstance().getRelationship(source);
					imageLabel.setVerticalTextPosition(JLabel.TOP);
					imageLabel.setHorizontalTextPosition(JLabel.CENTER);
					grid.add(imageLabel);
//					imageLabel.setToolTipText("Action: hg fetch\nConsequences: new relationship will be AHEAD \nCommiters: David and Yuriy");
				}
			}

			// Fill in the rest of the grid row with blanks
			for (int i = projPref.getNumOfVisibleSources(); i < maxSources; i++)
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
		_frame.pack();
	}

	public void setCanUpdate(boolean enable) {
		if (enable) {
			_refresh.setText("Refresh");
			_refresh.setEnabled(true);
		} else {
			_refresh.setText("Refreshing...");
			_refresh.setEnabled(false);
		}
	}

	public void setDaemonEnabled(boolean enable) {
		if (enable) {
			_disableDaemon.setText("Disable Daemon");
		} else {
			_disableDaemon.setText("Enable Daemon");
		}
	}

	/**
	 * Refreshes the UI.
	 */
	private void refresh() {

		for (ProjectPreferences projPref : _preferences.getProjectPreference()) {
			
			// first, set the Action
			JLabel action = _iconMap.get(projPref.getEnvironment());
			
			DataSource projectSource = projPref.getEnvironment();
			LocalStateResult actionResult = ConflictDaemon.getInstance().getLocalState(projectSource);
			
			// if it's pending, show whatever value it had last time
			if (actionResult.getLocalState().equals(LocalStateResult.PENDING) && actionResult.getLastLocalState() != null) {
				action.setText(actionResult.getLastAction());

				
				
			} else  { // otherwise, show fresh value
				action.setText(actionResult.getAction());
                String tip = actionResult.getErrorMessage();
                if ((tip == null) || (tip.trim().equals("")))
                    action.setToolTipText(null);
                else {
                    action.setToolTipText(tip);
                    if ((action.getText() == null) || (action.getText().isEmpty()))
                        action.setText("   ");
                }
			}
			// second, set the Relationships
			for (DataSource source : projPref.getDataSources()) {
				if (!(source.isHidden())) {
					JLabel current = _iconMap.get(source);
					current.removeAll();

					Relationship result = ConflictDaemon.getInstance().getRelationship(source);
					
					String tip = result.getToolTipText();
					current.setIcon(result.getIcon());
/*
					
					Relationship relationship = result.getRelationship();
					Relationship lastRelationship = result.getLastRelationship();


					// if it's pending, show whatever value it had last time
					String tip = null;
					if ((relationship.getName().equals(Relationship.PENDING) || (!(relationship.isReady()))) && lastRelationship != null) {
						current.setIcon(lastRelationship.getIcon());
						tip = lastRelationship.getToolTipText();
					} else {
						// otherwise, show fresh value
						current.setIcon(relationship.getIcon());
						tip = relationship.getToolTipText();
					}
*/
					if (tip.trim().equals(""))
						current.setToolTipText(null);
					else
						current.setToolTipText(tip);
					current.repaint();
				}
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