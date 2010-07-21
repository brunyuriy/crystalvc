package crystal.client;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.apache.log4j.Logger;

import crystal.Constants;
import crystal.client.ConflictDaemon.ComputationListener;
import crystal.model.ConflictResult;
import crystal.model.DataSource;
import crystal.model.ConflictResult.ResultStatus;
import crystal.util.LSMRLogger;
import crystal.util.RunIt;
import crystal.util.TimeUtility;

/**
 * This is the UI that lives in the system tray (windows), title bar (OS X) or somewhere else (linux). It contains the
 * menu options and provides a lightweight home for bringing up the ConflictClient UI.
 * 
 * @author rtholmes
 */
public class ConflictSystemTray implements ComputationListener {
	
	public static String VERSION_ID = "0.1.20100720";

	/**
	 * Conflict client UI.
	 */
	private ConflictClient _client;

	private Logger _log = Logger.getLogger(this.getClass());

	/**
	 * Main preference reference.
	 */
	private ClientPreferences _prefs;

	/**
	 * Timer used for refreshing the results.
	 */
	private Timer _timer;

	final private TrayIcon _trayIcon = new TrayIcon(createImage("images/bulb.gif", "tray icon"));

	public ConflictSystemTray() {
		_log.info("ConflictSystemTray - started at: " + TimeUtility.getCurrentLSMRDateString());
	}

	private MenuItem updateNowItem;
	private CheckboxMenuItem daemonEnabledItem;

	/**
	 * Create the tray icon and get it installed in the tray.
	 */
	private void createAndShowGUI() {

		// Create a popup menu components (we need to reference these (just deamonEnabledItem) if something goes wrong reading the config file.)
		MenuItem aboutItem = new MenuItem("About");
		MenuItem preferencesItem = new MenuItem("Edit Configuration");
		daemonEnabledItem = new CheckboxMenuItem("Daemon Enabled");
		updateNowItem = new MenuItem("Update Now");
		final MenuItem showClientItem = new MenuItem("Show Client");
		MenuItem exitItem = new MenuItem("Exit");

		// make sure the client is enabled by default
		daemonEnabledItem.setState(true);

		try {

			_prefs = ClientPreferences.loadPreferencesFromXML();

			if (_prefs != null) {
				_log.info("Preferences loaded successfully.");
			} else {
				String msg = "Error loading preferences.";

				System.err.println(msg);
				_log.error(msg);
			}
			
			ClientPreferences.savePreferencesToDefaultXML(_prefs);

		} catch (Exception e) {
			//			e.printStackTrace();
			String msg = "Error initializing ConflictClient. Please update your preference file ( " + ClientPreferences.CONFIG_PATH + " )";

			System.err.println(msg);
			_log.error(msg);

			System.err.println(e.getMessage());
			_log.error(e.getMessage());

			String dialogMessage = "The preferences file ( " + ClientPreferences.CONFIG_PATH + " ) is invalid and could not be loaded:\n > > > " + e.getMessage() + "\n" +
			"Do you want to edit it using the GUI?  This may overwrite your previous configuration file.  Your alternative is to edit the .xml file directly.";
			int answer = JOptionPane.showConfirmDialog(null, dialogMessage, "Invalid configuration file", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

			if (answer == JOptionPane.YES_OPTION) {
				_prefs = ClientPreferences.DEFAULT_CLIENT_PREFERENCES;
				PreferencesGUIEditorFrame editorFrame = PreferencesGUIEditorFrame.getPreferencesGUIEditorFrame(_prefs);
				JOptionPane.showMessageDialog(editorFrame, "Please remember to restart the client after closing the configuraton editor.");
				//and disable client
				daemonEnabledItem.setState(false);
				if (_timer != null) {
					_timer.stop();
					_timer = null;
				}
				for (CalculateTask ct : tasks) {
					_log.info("disabling ct of state: " + ct.getState());
					ct.cancel(true);
				}
			} else { // answer == JOptionPane.NO_OPTION
				System.out.println("User decided to edit the configuration file by hand");
				_log.trace("User decided to edit the configuration file by hand");
				quit(0);
			}
		}
		// Check the SystemTray support
		if (!SystemTray.isSupported()) {
			String msg = "SystemTray is not supported on this system";

			System.err.println(msg);
			_log.error(msg);

			quit(-1);
		}

		final PopupMenu trayMenu = new PopupMenu();
		// _trayIcon = new TrayIcon(createImage("images/bulb.gif", "tray icon"));
		_trayIcon.setImage(createImage("images/16X16/greenp.png", ""));

		final SystemTray tray = SystemTray.getSystemTray();

		_trayIcon.setToolTip("ConflictClient");


		// Add components to popup menu
		trayMenu.add(aboutItem);
		trayMenu.addSeparator();
		trayMenu.add(preferencesItem);
		trayMenu.add(daemonEnabledItem);
		trayMenu.addSeparator();
		trayMenu.add(updateNowItem);
		trayMenu.addSeparator();
		trayMenu.add(showClientItem);
		trayMenu.addSeparator();
		trayMenu.add(exitItem);

		_trayIcon.setPopupMenu(trayMenu);

		try {
			tray.add(_trayIcon);
		} catch (AWTException e) {
			_log.error("TrayIcon could not be added.");
			return;
		}

		_trayIcon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				_log.trace("Tray icon ActionEvent: " + ae.getActionCommand());
				// doesn't work on OS X; it doesn't register double clicks on
				// the tray
				showClient();
			}

		});

		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Crystal version: " + VERSION_ID + "\nBuilt by Reid Holmes and Yuriy Brun.  Contact brun@cs.washington.edu.");
			}
		});

		updateNowItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_log.info("Update now manually selected.");
				performCalculations();
			}
		});

		preferencesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (_client != null) {
					_client.close();
					_client = null;
				}

				showClientItem.setEnabled(false);

				// either creates (if one did not exist) or displays an existing 
				// PreferencesGUIEditorFrame configuration editor.
				PreferencesGUIEditorFrame.getPreferencesGUIEditorFrame(_prefs);

				/*				Yuriy: Old Preferences UI code by Reid.  @deprecated.  

 				ClientPreferencesUI cp = new ClientPreferencesUI(new ClientPreferencesUI.IPreferencesListener() {

					@Override
					public void preferencesChanged(ProjectPreferences preferences) {
						// when the preferences are updated, show the
						// client
						// _prefs = preferences;
						// NOTE: prefs UI broken by multiple project
						// refactor
					}

					@Override
					public void preferencesDialogClosed() {
						showClientItem.setEnabled(true);
						// NOTE: prefs UI broken by multiple project
						// refactor
					}
				}); 
				cp.createAndShowGUI();
				 */
			}
		});

		showClientItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showClient();
			}
		});

		daemonEnabledItem.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				int cb1Id = e.getStateChange();
				if (cb1Id == ItemEvent.SELECTED) {
					// daemon enabled
					_log.info("ConflictDaemon enabled");
					if (_timer != null) {
						// do it
						_timer.start();
					} else {
						createTimer();
					}
				} else {
					// daemon disabled
					_log.info("ConflictDaemon disabled");
					if (_timer != null) {
						_timer.stop();
						_timer = null;
					}
					for (CalculateTask ct : tasks) {
						_log.info("disabling ct of state: " + ct.getState());
						ct.cancel(true);
					}
					update();
				}
			}
		});

		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tray.remove(_trayIcon);

				String msg = "ConflictClient exited successfully.";
				System.out.println(msg);
				_log.trace("Exit action selected");

				quit(0);
			}
		});

		ConflictDaemon.getInstance().addListener(this);

		performCalculations();
	}

	/**
	 * Create the image to use in the tray.
	 * 
	 * @param path
	 * @param description
	 * @return
	 */
	protected Image createImage(String path, String description) {
		URL imageURL = ConflictSystemTray.class.getResource(path);

		if (imageURL == null) {
			_log.error("Resource not found: " + path);
			return null;
		} else {
			return (new ImageIcon(imageURL, description)).getImage();
		}
	}

	private void createTimer() {

		if (_timer != null) {
			_timer.stop();
			_timer = null;
		}

		_timer = new Timer((int) Constants.TIMER_CONSTANT, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				_log.info("Timer fired at: " + TimeUtility.getCurrentLSMRDateString());
				//
				// if (_client != null) {
				// // get the client to nicely refresh its elements
				// // _client.calculateConflicts();
				// performCalculations();
				// } else {
				performCalculations();
				// }
			}
		});

		_timer.setInitialDelay((int) Constants.TIMER_CONSTANT);
		_timer.start();

		long nextFire = System.currentTimeMillis() + _timer.getDelay();

		_log.info("Timer created - will fire in: " + TimeUtility.msToHumanReadable(_timer.getInitialDelay()) + " (@ "
				+ new SimpleDateFormat("HH:mm:ss").format(new Date(nextFire)) + ")");
	}

	HashSet<CalculateTask> tasks = new HashSet<CalculateTask>();
	long startCalculations = 0L;

	/**
	 * Perform the conflict calculations
	 */
	private void performCalculations() {

		// if the daemon is disabled, don't perform calculations.
		if (!daemonEnabledItem.getState())
			return;

		if (tasks.size() > 0) {
			for (CalculateTask ct : tasks) {
				_log.trace("CT state: " + ct.getState());
			}
			throw new RuntimeException("PerformCalculations being called in error; tasks > 0");
		}

		updateNowItem.setLabel("Updating...");
		updateNowItem.setEnabled(false);

		startCalculations = System.currentTimeMillis();

		for (ProjectPreferences projPref : _prefs.getProjectPreference()) {
			for (final DataSource source : projPref.getDataSources()) {
				final CalculateTask ct = new CalculateTask(source, projPref, this, _client);
				tasks.add(ct);
				ct.execute();
				ct.addPropertyChangeListener(new PropertyChangeListener() {

					@Override
					public void propertyChange(PropertyChangeEvent evt) {
						// NOTE: this is a poor hack; update() should be called by the handlers automatically, but it
						// seems that when this call is made the CT's state isn't always DONE so it fails to clear the
						// tasks vector correctly.
						update();
					}
				});
			}
		}
	}

	private void quit(int status) {
		_log.info("ConflictSystemTray exited - code: " + status + " at: " + TimeUtility.getCurrentLSMRDateString());

		System.exit(status);
	}

	/**
	 * Show the client and set up the timer.
	 */
	private void showClient() {
		_log.info("Show client requested");
		if (_client != null) {
			_client.show();
		} else {
			_client = new ConflictClient();
			_client.createAndShowGUI(_prefs);
		}
	}

	@Override
	public void update() {
		_log.trace("ConflictSystemTray::update()");

		Iterator<CalculateTask> ctIterator = tasks.iterator();
		if (ctIterator.hasNext()) {
			CalculateTask ct = ctIterator.next();
			_log.trace("Current state: " + ct.getState());
			if (ct.isDone()) {
				ctIterator.remove();
			}
		}

		_log.trace("Task size in update: " + tasks.size());
		if (tasks.size() == 0) {

			if (daemonEnabledItem.getState()) {
				long end = System.currentTimeMillis();
				long delta = end - startCalculations;
				_log.info("Computation took: " + TimeUtility.msToHumanReadable(delta));
				Constants.TIMER_CONSTANT = delta * Constants.TIMER_MULTIPLIER;
				createTimer();
			}

			updateNowItem.setEnabled(true);
			updateNowItem.setLabel("Update now");
		}

		boolean anyGreen = false;
		boolean anyPull = false;
		boolean anyYellow = false;
		boolean anyRed = false;

		for (ConflictResult result : ConflictDaemon.getInstance().getResults()) {
			if (result.getStatus().equals(ResultStatus.SAME)) {
				anyGreen = true;
			}

			if (result.getStatus().equals(ResultStatus.MERGECLEAN)) {
				anyPull = true;
			}

			if (result.getStatus().equals(ResultStatus.MERGECONFLICT)) {
				anyRed = true;
			}

			if (result.getStatus().equals(ResultStatus.BEHIND)) {
				anyYellow = true;
			}
		}

		if (anyRed) {
			// TODO: should flush old images
			// _trayIcon.getImage().flush();
			_trayIcon.setImage(createImage("images/16X16/redstatus.png", ""));
		} else if (anyYellow) {
			_trayIcon.setImage(createImage("images/16X16/yellowstatus.png", ""));
		} else if (anyPull) {
			_trayIcon.setImage(createImage("images/16X16/greenp.png", ""));
		} else if (anyGreen) {
			_trayIcon.setImage(createImage("images/16X16/greenstatus.png", ""));
		}

		if (_client != null) {
			_client.update();
		}
	}

	/**
	 * Main execution point.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		if (args.length > 0) {
			if (args[0].equals("--version")) {
				System.out.println("Crystal version: " + VERSION_ID);
				System.exit(0);
			}
		}

		LSMRLogger.startLog4J(Constants.QUIET_CONSOLE, true, Constants.LOG_LEVEL, System.getProperty("user.home"), ".conflictClientLog");

		// UIManager.put("swing.boldMetal", Boolean.FALSE);

		ConflictSystemTray cst = new ConflictSystemTray();
		cst.createAndShowGUI();
	}

}