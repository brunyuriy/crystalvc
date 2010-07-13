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

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import crystal.Constants;
import crystal.client.ConflictDaemon.ComputationListener;
import crystal.model.ConflictResult;
import crystal.model.ConflictResult.ResultStatus;

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

	/**
	 * Main preference reference.
	 */
	private ClientPreferences _prefs;

	/**
	 * Timer used for refreshing the results.
	 */
	private Timer _timer;

	final private TrayIcon _trayIcon = new TrayIcon(createImage("images/bulb.gif", "tray icon"));

	/**
	 * Create the tray icon and get it installed in the tray.
	 */
	private void createAndShowGUI() {

		try {

			_prefs = ClientPreferences.loadPreferencesFromXML();

			if (_prefs != null) {
				System.out.println("ConflictClient preferences loaded successfully.");
			} else {
				System.out.println("ConflictClient - Error loading preferences.");
			}

		} catch (Exception e) {
			System.err.println("Error initializing ConflictClient. Please update your preference file ( " + ClientPreferences.CONFIG_PATH + " )");
			System.exit(-1);
		}
		// Check the SystemTray support
		if (!SystemTray.isSupported()) {
			System.err.println("SystemTray is not supported");
			return;
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
		final MenuItem showClientItem = new MenuItem("Show Client");
		MenuItem exitItem = new MenuItem("Exit");

		// Add components to popup menu
		trayMenu.add(aboutItem);
		trayMenu.addSeparator();
		trayMenu.add(preferencesItem);
		trayMenu.add(enabledItem);
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
			System.out.println("TrayIcon could not be added.");
			return;
		}

		_trayIcon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				System.out.println("Tray icon action: " + ae);
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

		preferencesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (_client != null) {
					_client.close();
					_client = null;
				}

				showClientItem.setEnabled(false);
				// XXX: prefs UI broken by multiple project refactor (horrible
				// hack in the constructor here)
				ClientPreferencesUI cp = new ClientPreferencesUI(new ClientPreferencesUI.IPreferencesListener() {
					@Override
					public void preferencesChanged(ProjectPreferences preferences) {
						// when the preferences are updated, show the
						// client
						// _prefs = preferences;
						// XXX: prefs UI broken by multiple project
						// refactor
					}

					@Override
					public void preferencesDialogClosed() {
						// System.out.println("ConflictSystemTray::IPreferencesListener::preferencesDialogClosed()");
						showClientItem.setEnabled(true);
						// XXX: prefs UI broken by multiple project
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
					System.out.println("ConflictClient - ConflictDaemon enabled");
					if (_timer != null)
						_timer.start();
				} else {
					// daemon disabled
					System.out.println("ConflictClient - ConflictDaemon disabled");
					if (_timer != null)
						_timer.stop();
				}
			}
		});

		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("ConflictSystemTray::createAndShowGUI() - Client explicitly exited");
				tray.remove(_trayIcon);
				System.exit(0);
			}
		});

		ConflictDaemon.getInstance().addListener(this);
	}

	/**
	 * Create the image to use in the tray.
	 * 
	 * @param path
	 * @param description
	 * @return
	 */
	protected static Image createImage(String path, String description) {
		URL imageURL = ConflictSystemTray.class.getResource(path);

		if (imageURL == null) {
			System.err.println("Resource not found: " + path);
			return null;
		} else {
			return (new ImageIcon(imageURL, description)).getImage();
		}
	}

	/**
	 * Main execution point.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// UIManager.put("swing.boldMetal", Boolean.FALSE);

		// SwingUtilities.invokeLater(new Runnable() {
		//
		// public void run() {

		ConflictSystemTray cst = new ConflictSystemTray();
		cst.createAndShowGUI();

		// }
		// });
	}

	/**
	 * Show the client and set up the timer.
	 */
	private void showClient() {
		if (_client != null) {
			_client.close();
			_client = null;
		}
		_client = new ConflictClient();
		_client.createAndShowGUI(_prefs);
		_client.calculateConflicts();

		if (_timer != null) {
			_timer.stop();
			_timer = null;
		}

		_timer = new Timer(Constants.TIMER_CONSTANT, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("ConflictSystemTray::showClient - Timer fired: " + e.getSource());
				// get the client to nicely refresh its elements
				_client.calculateConflicts();
			}
		});
		_timer.setInitialDelay(Constants.TIMER_CONSTANT);
		_timer.start();
	}

	@Override
	public void update() {
		System.out.println("ConflictSystemTray::update()");
		
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
	}

}