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
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.apache.log4j.Level;
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
 * menu options and provides a lightweight home for bringing up the ConflictClient UI.
 * 
 * @author rtholmes
 */
public class ConflictSystemTray implements ComputationListener {

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

	/**
	 * Create the tray icon and get it installed in the tray.
	 */
	private void createAndShowGUI() {

		try {

			_prefs = ClientPreferences.loadPreferencesFromXML();

			if (_prefs != null) {
				_log.info("Preferences loaded successfully.");
			} else {
				String msg = "Error loading preferences.";

				System.err.println(msg);
				_log.error(msg);
			}

		} catch (Exception e) {
			String msg = "Error initializing ConflictClient. Please update your preference file ( " + ClientPreferences.CONFIG_PATH + " )";

			System.err.println(msg);
			_log.error(msg);

			System.err.println(e.getMessage());
			_log.error(e.getMessage());

			quit(-1);
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

		// Create a popup menu components
		MenuItem aboutItem = new MenuItem("About");
		MenuItem preferencesItem = new MenuItem("Preferences");
		CheckboxMenuItem enabledItem = new CheckboxMenuItem("Daemon Enabled");
		updateNowItem = new MenuItem("Update Now");
		final MenuItem showClientItem = new MenuItem("Show Client");
		MenuItem exitItem = new MenuItem("Exit");

		// Add components to popup menu
		trayMenu.add(aboutItem);
		trayMenu.addSeparator();
		trayMenu.add(preferencesItem);
		trayMenu.add(enabledItem);
		trayMenu.addSeparator();
		trayMenu.add(updateNowItem);
		trayMenu.addSeparator();
		trayMenu.add(showClientItem);
		trayMenu.addSeparator();
		trayMenu.add(exitItem);

		_trayIcon.setPopupMenu(trayMenu);

		// make sure the client is enabled by default
		enabledItem.setState(true);

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
				JOptionPane.showMessageDialog(null, "Built by Holmes, Brun, Ernst, and Notkin.");
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
				// NOTE: prefs UI broken by multiple project refactor (horrible
				// hack in the constructor here)
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
			}
		});

		showClientItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showClient();
			}
		});

		enabledItem.addItemListener(new ItemListener() {
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

				if (_client != null) {
					// get the client to nicely refresh its elements
					_client.calculateConflicts();
				} else {
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

	/**
	 * Perform the initial calculations
	 */
	private void performCalculations() {

		updateNowItem.setLabel("Updating...");
		updateNowItem.setEnabled(false);

		long start = System.currentTimeMillis();
		for (ProjectPreferences pp : _prefs.getProjectPreference()) {
			for (DataSource source : pp.getDataSources()) {
				ConflictDaemon.getInstance().calculateConflicts(source, pp);
			}
		}
		long end = System.currentTimeMillis();

		long delta = end - start;
		Constants.TIMER_CONSTANT = delta * Constants.TIMER_MULTIPLIER;

		_log.info("Computation took: " + TimeUtility.msToHumanReadable(delta));
		// _log.info("Adaptive timer interval now: " + TimeUtility.msToHumanReadable(Constants.TIMER_CONSTANT));

		updateNowItem.setEnabled(true);
		updateNowItem.setLabel("Update now");

		createTimer();
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
			// only update when the timer fires
			// initial update is fired manually
			// _client.calculateConflicts();
		}

		createTimer();

	}

	@Override
	public void update() {
		_log.trace("ConflictSystemTray::update()");

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

		LSMRLogger.startLog4J(Constants.QUIET_CONSOLE, true, Level.INFO, System.getProperty("user.home"), ".conflictClientLog");

		// UIManager.put("swing.boldMetal", Boolean.FALSE);

		ConflictSystemTray cst = new ConflictSystemTray();
		cst.createAndShowGUI();
	}

}