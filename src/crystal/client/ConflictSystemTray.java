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
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import crystal.model.DataSource;
import crystal.model.DataSource.RepoKind;

public class ConflictSystemTray {
	private static ConflictClient _client;
	private static ProjectPreferences _prefs;
	private static Timer _timer;

	public static void main(String[] args) {

		// UIManager.put("swing.boldMetal", Boolean.FALSE);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				_prefs = loadPreferences();

				createAndShowGUI();
			}

			@SuppressWarnings("unchecked")
			private ProjectPreferences loadPreferences() {
				// TestHgStateChecker thgsc = new TestHgStateChecker();
				// ClientPreferences prefs = thgsc.getPreferences();
				ProjectPreferences prefs = null;

				SAXBuilder builder = new SAXBuilder(false);
				Document doc = null;

				try {

					doc = builder.build(this.getClass().getResourceAsStream("config.xml"));

					Element rootElement = doc.getRootElement();
					String tempDirectory = rootElement.getAttributeValue("tempDirectory");
					assert tempDirectory != null;

					List<Element> projectElements = rootElement.getChildren("project");
					for (Element projectElement : projectElements) {
						String myKind = projectElement.getAttributeValue("myKind");
						String myShortName = projectElement.getAttributeValue("myShortName");
						String myClone = projectElement.getAttributeValue("myClone");

						assert myKind != null;
						assert myShortName != null;
						assert myClone != null;

						DataSource myEnvironment = new DataSource(myShortName, myClone, RepoKind.valueOf(myKind));

						prefs = new ProjectPreferences(myEnvironment, tempDirectory);

						if (projectElement.getChild("sources") != null) {
							List<Element> sourceElements = projectElement.getChild("sources").getChildren("source");
							for (Element sourceElement : sourceElements) {
								String kind = sourceElement.getAttributeValue("kind");
								String shortName = sourceElement.getAttributeValue("shortName");
								String clone = sourceElement.getAttributeValue("clone");

								assert kind != null;
								assert shortName != null;
								assert clone != null;

								DataSource source = new DataSource(shortName, clone, RepoKind.valueOf(kind));
								prefs.addDataSource(source);
							}
						}
					}
				} catch (JDOMException jdome) {
					System.err.println(jdome);
				} catch (IOException ioe) {
					System.err.println(ioe);
				}

				assert prefs != null;

				return prefs;
			}
		});
	}

	private static void createAndShowGUI() {
		// Check the SystemTray support
		if (!SystemTray.isSupported()) {
			System.err.println("SystemTray is not supported");
			return;
		}

		final PopupMenu trayMenu = new PopupMenu();
		final TrayIcon trayIcon = new TrayIcon(createImage("images/bulb.gif", "tray icon"));
		final SystemTray tray = SystemTray.getSystemTray();

		trayIcon.setToolTip("ConflictClient");

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

		trayIcon.setPopupMenu(trayMenu);

		// make sure the client is enabled by default
		enabledItem.setState(true);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
			return;
		}

		trayIcon.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				System.out.println("Tray icon action: " + ae);
				// doesn't work on OS X; it doesn't register double clicks on the tray
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
				ClientPreferencesUI cp = new ClientPreferencesUI(_prefs, new ClientPreferencesUI.IPreferencesListener() {
					@Override
					public void preferencesChanged(ProjectPreferences preferences) {
						// when the preferences are updated, show the client
						_prefs = preferences;
					}

					@Override
					public void preferencesDialogClosed() {
						System.out.println("ConflictSystemTray::IPreferencesListener::preferencesDialogClosed()");
						showClientItem.setEnabled(true);
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
				} else {
					// daemon disabled
					System.out.println("ConflictClient - ConflictDaemon disabled");
				}
			}
		});

		exitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("ConflictClient - Client explicitly exited");
				tray.remove(trayIcon);
				System.exit(0);
			}
		});

	}

	private static void showClient() {
		if (_client != null) {
			_client.close();
			_client = null;
		}
		_client = new ConflictClient();
		_client.createAndShowGUI(_prefs);
		_client.calculateConflicts();

		// Set up timer to drive refreshes
		int TIMER_CONSTANT = 10000;
		if (_timer != null) {
			_timer.stop();
			_timer = null;
		}

		_timer = new Timer(TIMER_CONSTANT, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("ConflictSystemTray::showClient - Timer fired: " + e.getSource());
				// get the client to nicely refresh its elements
				_client.calculateConflicts();
				// if (e.getSource() instanceof Timer) {
				// ((Timer) e.getSource()).stop();
				// }
			}
		});
		_timer.setInitialDelay(TIMER_CONSTANT);
		_timer.start();

	}

	// Obtain the image URL
	protected static Image createImage(String path, String description) {
		URL imageURL = ConflictSystemTray.class.getResource(path);

		if (imageURL == null) {
			System.err.println("Resource not found: " + path);
			return null;
		} else {
			return (new ImageIcon(imageURL, description)).getImage();
		}
	}
}