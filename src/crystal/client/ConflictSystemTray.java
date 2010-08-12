package crystal.client;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import crystal.util.TimeUtility;

/**
 * This is the UI that lives in the system tray (windows), title bar (OS X) or somewhere else (linux). It contains the
 * menu options and provides a lightweight home for bringing up the ConflictClient UI.  If the system tray is not supported, the UI 
 * switches to a window-only view.  
 * 
 * ConflictSystemTray is a singleton.
 * 
 * @author rtholmes & brun
 */
public class ConflictSystemTray implements ComputationListener {

	private static ConflictSystemTray _instance;

	public static boolean TRAY_SUPPORTED = SystemTray.isSupported();
	// public static boolean TRAY_SUPPORTED = false;

	public static String VERSION_ID = "0.1.20100811";

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

	final private SystemTray _tray;

	final private TrayIcon _trayIcon;

	private MenuItem daemonEnabledItem;

	long startCalculations = 0L;

	// It would be nice to get rid of this, but it is nice to be able to cancel tasks once they are in flight
	// HashSet<CalculateTask> tasks = new HashSet<CalculateTask>();

	private MenuItem updateNowItem;

	private ConflictSystemTray() {
		_log.info("ConflictSystemTray - started at: " + TimeUtility.getCurrentLSMRDateString());
		if (TRAY_SUPPORTED) {
			_tray = SystemTray.getSystemTray();
//			_trayIcon = new TrayIcon((new ImageIcon(Constants.class.getResource("/crystal/client/images/bulb.gif"))).getImage());
			_trayIcon = new TrayIcon((new ImageIcon(Constants.class.getResource("/crystal/client/images/crystal-ball_blue_32.png"))).getImage());
		} else {
			_tray = null;
			_trayIcon = null;
		}
	}

	public void aboutAction() {
		JOptionPane.showMessageDialog(
						null,
						"Crystal version: " + VERSION_ID + 
								"\nBuilt by Reid Holmes and Yuriy Brun.  Contact brun@cs.washington.edu.\nhttp://www.cs.washington.edu/homes/brun/research/crystal",
								"Crystal: Proactive Conflict Detector for Distributed Version Control", 
								JOptionPane.PLAIN_MESSAGE,
								new ImageIcon(Constants.class.getResource("/crystal/client/images/crystal-ball_blue_128.png")));
	}

	/**
	 * Create the tray icon and get it installed in the tray.
	 */
	private void createAndShowGUI() {

		// Create components for a popup menu components to be used if System Tray is supported.
		MenuItem aboutItem = new MenuItem("About");
		MenuItem preferencesItem = new MenuItem("Edit Configuration");
		daemonEnabledItem = new MenuItem("Disable Daemon");
		updateNowItem = new MenuItem("Update Now");
		final MenuItem showClientItem = new MenuItem("Show Client");
		MenuItem exitItem = new MenuItem("Exit");

		try {
			_prefs = ClientPreferences.loadPreferencesFromXML();

			if (_prefs != null) {
				_log.info("Preferences loaded successfully.");
			} else {
				String msg = "Error loading preferences.";

				System.err.println(msg);
				_log.error(msg);
			}

			if (_prefs.hasChanged()) {
				ClientPreferences.savePreferencesToDefaultXML(_prefs);
				_prefs.setChanged(false);
			}
		} catch (Exception e) {
			// e.printStackTrace();
			String msg = "Error initializing ConflictClient. Please update your preference file ( " + ClientPreferences.CONFIG_PATH + " )";
			System.err.println(msg);
			_log.error(msg);

			System.err.println(e.getMessage());
			_log.error(e.getMessage());

			String dialogMessage = "The preferences file ( "
					+ ClientPreferences.CONFIG_PATH
					+ " ) is invalid and could not be loaded:\n > > > "
					+ e.getMessage()
					+ "\n"
					+ "Do you want to edit it using the GUI?  This may overwrite your previous configuration file.  Your alternative is to edit the .xml file directly.";
			int answer = JOptionPane.showConfirmDialog(null, dialogMessage, "Invalid configuration file", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE);

			if (answer == JOptionPane.YES_OPTION) {
				_prefs = ClientPreferences.DEFAULT_CLIENT_PREFERENCES;
				PreferencesGUIEditorFrame editorFrame = PreferencesGUIEditorFrame.getPreferencesGUIEditorFrame(_prefs);
				JOptionPane.showMessageDialog(editorFrame, "Please remember to restart the client after closing the configuraton editor.");
				// and disable client
				daemonEnabledItem.setLabel("Enable Daemon");
				if (_timer != null) {
					_timer.stop();
					_timer = null;
				}

				// for (CalculateTask ct : tasks) {
				// _log.info("disabling ct of state: " + ct.getState());
				// ct.cancel(true);
				// }

			} else { // answer == JOptionPane.NO_OPTION
				System.out.println("User decided to edit the configuration file by hand");
				_log.trace("User decided to edit the configuration file by hand");
				quit(0);
			}
		}

		/*
		 * Old code for quiting if there is no System Tray support. // Check the SystemTray support if
		 * (!SystemTray.isSupported()) {dsfdsfdsfds //for testing change above line to the following one: // if (true) {
		 * String msg = "SystemTray is not supported on this system";
		 * 
		 * System.err.println(msg); _log.error(msg);
		 * 
		 * JOptionPane.showMessageDialog(null,
		 * "Your operating system does not support a system tray, which is currently required for Crystal."); quit(0,
		 * _log); }
		 */

		// Start out with the client showing.
		showClient();

		if (TRAY_SUPPORTED) {
			final PopupMenu trayMenu = new PopupMenu();
			_trayIcon.setImage((new ImageIcon(Constants.class.getResource("/crystal/client/images/16X16/clock.png"))).getImage());

			_trayIcon.setToolTip("Crystal");

			// Add components to the popup menu
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
				_tray.add(_trayIcon);
			} catch (AWTException e) {
				_log.error("TrayIcon could not be added.");
				return;
			}

			_trayIcon.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ae) {
					_log.trace("Tray icon ActionEvent: " + ae.getActionCommand());
					// doesn't work on OS X; it doesn't register double clicks on the tray
					showClient();
				}
			});

			aboutItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					aboutAction();
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
					preferencesAction();
				}
			});

			showClientItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					showClient();
				}
			});

			daemonEnabledItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					daemonAbleAction();
				}
			});

			exitItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					exitAction();
				}
			});

			ConflictDaemon.getInstance().addListener(this);
		}

		performCalculations();
	}

	/**
	 * Create the image to use in the tray.
	 * 
	 * @param path
	 * @param description
	 * @return
	protected Image createImage(String path, String description) {
		URL imageURL = ConflictSystemTray.class.getResource(path);

		if (imageURL == null) {
			_log.error("Resource not found: " + path);
			return null;
		} else {
			return (new ImageIcon(imageURL, description)).getImage();
		}
	}
	 */


	private void createTimer() {

		boolean pTask = false;

		for (ConflictResult result : ConflictDaemon.getInstance().getResults()) {
			if (result.getStatus().equals(ResultStatus.PENDING)) {
				pTask = true;
			}
		}

		final boolean pendingTask = pTask;

		if (_timer != null) {
			_timer.stop();
			_timer = null;
		}

		_timer = new Timer((int) Constants.TIMER_CONSTANT, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				_log.info("Timer fired at: " + TimeUtility.getCurrentLSMRDateString());
				if (!pendingTask) {
					// if tasks are pending don't start the calculations again
					performCalculations();
				}
			}
		});

		_timer.setInitialDelay((int) Constants.TIMER_CONSTANT);
		_timer.start();

		long nextFire = System.currentTimeMillis() + _timer.getDelay();

		_log.info("Timer created - will fire in: " + TimeUtility.msToHumanReadable(_timer.getInitialDelay()) + " (@ "
				+ new SimpleDateFormat("HH:mm:ss").format(new Date(nextFire)) + ")");
	}

	public void daemonAbleAction() {
		if (daemonEnabledItem.getLabel().equals("Enable Daemon")) {
			// daemon enabled
			_log.info("ConflictDaemon enabled");
			daemonEnabledItem.setLabel("Disable Daemon");
			_client.setDaemonEnabled(true);
			if (_timer != null) {
				// do it
				_timer.start();
			} else {
				createTimer();
			}
		} else {
			// daemon disabled
			_log.info("ConflictDaemon disabled");
			daemonEnabledItem.setLabel("Enable Daemon");
			_client.setDaemonEnabled(false);
			if (_timer != null) {
				_timer.stop();
				_timer = null;
			}

			// for (CalculateTask ct : tasks) {
			// _log.info("disabling ct of state: " + ct.getState());
			// ct.cancel(true);
			// }

			update();
		}
	}

	public void exitAction() {
		if (TRAY_SUPPORTED)
			_tray.remove(_trayIcon);

		String msg = "ConflictClient exited successfully.";
		System.out.println(msg);
		_log.trace("Exit action selected");

		quit(0);
	}

	/**
	 * Perform the conflict calculations
	 */
	public void performCalculations() {

		// if the daemon is disabled, don't perform calculations.
		if (daemonEnabledItem.getLabel().equals("Enable Daemon")) {
			return;
		}

		// if (!pendingTask) {
		// get all of the tasks in pending mode
		ConflictDaemon.getInstance().prePerformCalculations(_prefs);

		updateNowItem.setLabel("Updating...");
		_log.trace("update now text: " + updateNowItem.getLabel());
		updateNowItem.setEnabled(false);
		_client.setCanUpdate(false);

		startCalculations = System.currentTimeMillis();

		for (ProjectPreferences projPref : _prefs.getProjectPreference()) {
			for (final DataSource source : projPref.getDataSources()) {
				final CalculateTask ct = new CalculateTask(source, projPref, this, _client);
				ct.execute();
			}
		}
		// } else {
		// _log.info("Tasks still pending; new run not initiated");
		// }
	}

	public void preferencesAction() {
		// either creates (if one did not exist) or displays an existing
		// PreferencesGUIEditorFrame configuration editor.
		PreferencesGUIEditorFrame.getPreferencesGUIEditorFrame(_prefs);
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

		// _log.trace("Task size in update: " + tasks.size());

		boolean pendingTask = false;

		for (ConflictResult result : ConflictDaemon.getInstance().getResults()) {
			if (result.getStatus().equals(ResultStatus.PENDING)) {
				pendingTask = true;
			}
		}

		if (pendingTask) {
			_log.trace("Update called with tasks still pending.");

			// keep the UI in updating mode
			updateNowItem.setLabel("Updating...");
			updateNowItem.setEnabled(false);
			_client.setCanUpdate(false);
		} else {
			_log.trace("Update called with no tasks pending.");

			createTimer();
			updateNowItem.setLabel("Update Now");
			updateNowItem.setEnabled(true);
			_client.setCanUpdate(true);
		}

		if (TRAY_SUPPORTED)
			updateTrayIcon();

		if (_client != null) {
			_client.update();
		}
	}

	private void updateTrayIcon() {
		
		if (!TRAY_SUPPORTED)
			return;

		_trayIcon.getImage().flush();
		
		Image icon = (ConflictResult.ResultStatus.getDominant(ConflictDaemon.getInstance().getResults())).getImage();

		_trayIcon.setImage(icon);
		
		/*
		boolean anyGreen = false;
		boolean anyPull = false;
		boolean anyYellow = false;
		boolean anyRed = false;
		boolean anyError = false;

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

			if (result.getStatus().equals(ResultStatus.ERROR)) {
				anyError = true;
			}

			// if they're pending consider the last status
			if (result.getStatus().equals(ResultStatus.PENDING) && result.getLastStatus() != null) {
				if (result.getLastStatus().equals(ResultStatus.SAME)) {
					anyGreen = true;
				}

				if (result.getLastStatus().equals(ResultStatus.MERGECLEAN)) {
					anyPull = true;
				}

				if (result.getLastStatus().equals(ResultStatus.MERGECONFLICT)) {
					anyRed = true;
				}

				if (result.getLastStatus().equals(ResultStatus.BEHIND)) {
					anyYellow = true;
				}

				if (result.getLastStatus().equals(ResultStatus.ERROR)) {
					anyError = true;
				}
			}
		}

		if (anyError) {
			_trayIcon.setImage(createImage("images/16X16/error.png", ""));
		} else if (anyRed) {
			_trayIcon.setImage(createImage("images/16X16/redstatus.png", ""));
		} else if (anyYellow) {
			_trayIcon.setImage(createImage("images/16X16/yellowstatus.png", ""));
		} else if (anyPull) {
			_trayIcon.setImage(createImage("images/16X16/greenp.png", ""));
		} else if (anyGreen) {
			_trayIcon.setImage(createImage("images/16X16/greenstatus.png", ""));
		} else {
			_trayIcon.setImage(createImage("images/16X16/greenstatus.png", ""));
		}
		 */
	}

	public static ConflictSystemTray getInstance() {
		if (_instance == null) {
			_instance = new ConflictSystemTray();
		}
		return _instance;
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

		ConflictSystemTray cst = ConflictSystemTray.getInstance();
		cst.createAndShowGUI();
	}

}
