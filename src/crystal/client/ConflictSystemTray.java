package crystal.client;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;

import crystal.Constants;
import crystal.client.ConflictDaemon.ComputationListener;
import crystal.model.LocalStateResult;
import crystal.model.Relationship;
import crystal.util.LSMRLogger;
import crystal.util.RunIt;
import crystal.util.TimeUtility;

/**
 * The system tray icon UI. (This lives in the title bar in OS X or somewhere else in Linux). This UI contains a few menu options and allows opening
 * up the larger window UI. If the system tray is not supported, the UI switches to a window-only view.
 * 
 * ConflictSystemTray is a singleton.
 * 
 * @author rtholmes
 * @author brun
 */
public class ConflictSystemTray implements ComputationListener {

    // The singleton instance.
    private static ConflictSystemTray _instance;

    // The boolean that tells us if the OS supports the system tray.
    public static boolean TRAY_SUPPORTED = SystemTray.isSupported();

    // The current Crystal version number.
    public static String VERSION_ID = "1.0.20111015";

    // A pointer to the Crystal window UI.
    private ConflictClient _client;

    // The logger.
    private Logger _log = Logger.getLogger(this.getClass());

    // The current configuration.
    private ClientPreferences _prefs;

    // A timer that we use to refresh the results.
    private Timer _timer;

    // A placekeeper to remember when we start each calculation.
    long startCalculations = 0L;

    // A handle on the actual system tray.
    final private SystemTray _tray;

    // The Crystal tray icon.
    final private TrayIcon _trayIcon;

    // A menu element that dictates whether Crystal ConflictDeamon is running (refreshing).
    private MenuItem _daemonEnabledItem;

    // A menu element that allows the user to start a new refresh right now.
    private MenuItem _refreshItem;

    // The other menu elements are not referenced from listeners, so they are declared only locally.

    /**
     * Constructs a brand new Crystal system tray icon, if the OS allows it. If the OS does not allow it, creates an empty tray icon object holding
     * some nulls.
     */
    private ConflictSystemTray() {
        _log.info("ConflictSystemTray - started at: " + TimeUtility.getCurrentLSMRDateString());
        if (TRAY_SUPPORTED) {
            _tray = SystemTray.getSystemTray();
            // _trayIcon = new TrayIcon((new ImageIcon(Constants.class.getResource("/crystal/client/images/bulb.gif"))).getImage());
            _trayIcon = new TrayIcon((new ImageIcon(Constants.class.getResource("/crystal/client/images/crystal-ball_blue_32.png"))).getImage());
        } else {
            _tray = null;
            _trayIcon = null;
        }
    }

    /**
     * A listener on the about menu item. When the user clicks on "about", a dialog pops up with some info on Crystal.
     */
    public void aboutAction() {
        JOptionPane.showMessageDialog(
                null,
                "Crystal version: "
                        + VERSION_ID
                        + "\nBuilt by Yuriy Brun, Reid Holmes, and Haochen Wei." 
                        + "\nContact crystalvc@googlegroups.com." 
                        + "\nhttp://crystalvc.googlecode.com",
                        "Crystal: Proactive Conflict Detector for Distributed Version Control", JOptionPane.PLAIN_MESSAGE, new ImageIcon(
                                Constants.class.getResource("/crystal/client/images/crystal-ball_blue_128.png")));
    }

    /**
     * Loads the Crystal preferences and creates the appropriate views
     */
    public void loadPreferences() {
        try {
            _prefs = ClientPreferences.loadPreferencesFromDefaultXML();

            if (_prefs != null) {
                _log.info("Preferences loaded successfully.");
            } else {
                String msg = "Error loading preferences.";

                System.err.println(msg);
                _log.error(msg);
            }
        } catch (IOException e) {
            String msg = "Error reading the configuration file ( " + ClientPreferences.CONFIG_PATH + " )";
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
                _daemonEnabledItem.setLabel("Start Crystal updates");
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

        if (_prefs.hasChanged()) {
            try {
                ClientPreferences.savePreferencesToDefaultXML(_prefs);
            } catch (FileNotFoundException fnfe) {
                if (fnfe.getMessage().indexOf("Access is denied") >= 0)
                    _log.info("XML file cannot be written to: " + fnfe);
                else
                    _log.error(fnfe);

            }
            _prefs.setChanged(false);
        }

        // clear and reload the main window
        showClient();
        _client.reloadWindowBody(_prefs);
        
        if (!ConflictDaemon.getInstance().isEnabled()) 
            _client.setDaemonEnabled(true);

        // refresh the window
        performCalculations();
    }

    /**
     * Creates the Crystal system tray icon and installs in the tray.
     */
    private void createAndShowGUI() {
        // Create components for a popup menu components to be used if System Tray is supported.
        MenuItem aboutItem = new MenuItem("About");
        MenuItem preferencesItem = new MenuItem("Edit Configuration");
        _daemonEnabledItem = new MenuItem("Stop Crystal updates");
        _refreshItem = new MenuItem("Refresh");
        final MenuItem showClientItem = new MenuItem("Show Client");
        MenuItem exitItem = new MenuItem("Exit");

        loadPreferences();

        // Check that we have a recent-enough version of hg
        try {
            if (!(RunIt.validHG(Constants.MIN_HG_VERSION, _prefs.getHgPath(), _prefs.getTempDirectory()))) {
                JOptionPane.showMessageDialog(null, 
                        "Your computer is running an outdated version of hg.\nThe must be running at least version " + Constants.MIN_HG_VERSION, 
                        "outdated hg", JOptionPane.ERROR_MESSAGE);
                quit(1);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Encountered an exception while checking hg version", 
                    "Error checking hg version", JOptionPane.ERROR_MESSAGE);
            quit(1);
        }
        
        //TODO check that we have a recent-enough version of git
        


        if (TRAY_SUPPORTED) {
            final PopupMenu trayMenu = new PopupMenu();
            _trayIcon.setImage((new ImageIcon(Constants.class.getResource("/crystal/client/images/16X16/must/clock.png"))).getImage());

            _trayIcon.setToolTip("Crystal");

            // Add components to the popup menu
            trayMenu.add(aboutItem);
            trayMenu.addSeparator();
            trayMenu.add(preferencesItem);
            trayMenu.add(_daemonEnabledItem);
            trayMenu.addSeparator();
            trayMenu.add(_refreshItem);
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

            _refreshItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    _log.info("Refresh manually selected.");
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

            _daemonEnabledItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    daemonAbleAction();
                }
            });

            exitItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    exitAction();
                }
            });

        }

        ConflictDaemon.getInstance().addListener(this);

        performCalculations();
    }

    /**
     * Creates and starts a new timer (throws away the old one). The timer fires an update every refresh seconds, unless there is a pending task.
     */
    private void createTimer() {

        // note that the timer works in milliseconds and the argument is in seconds

        boolean pTask = false;

        // check if anything is PENDING (first local states then relationships
        for (LocalStateResult localState : ConflictDaemon.getInstance().getLocalStates()) {
            if (localState.getLocalState().equals(LocalStateResult.PENDING)) {
                pTask = true;
            }
        }
        for (Relationship relationship : ConflictDaemon.getInstance().getRelationships()) {
            if (!(relationship.isReady())) {
                pTask = true;
            }
        }

        final boolean pendingTask = pTask;

        if (_timer != null) {
            _timer.stop();
            _timer = null;
        }

        _timer = new Timer((int) ClientPreferences.REFRESH * 1000, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                _log.info("Timer fired at: " + TimeUtility.getCurrentLSMRDateString());
                if (!pendingTask) {
                    // if tasks are pending don't start the calculations again
                    performCalculations();
                }
            }
        });

        _timer.setInitialDelay((int) ClientPreferences.REFRESH * 1000);
        _timer.start();

        long nextFire = System.currentTimeMillis() + _timer.getDelay();

        _log.info("Timer created - will fire in: " + TimeUtility.msToHumanReadable(_timer.getInitialDelay()) + " (@ "
                + new SimpleDateFormat("HH:mm:ss").format(new Date(nextFire)) + ")");
    }

    /**
     * A listener for clicking the menu to enable the deamon.
     */
    public void daemonAbleAction() {
        if (!ConflictDaemon.getInstance().isEnabled()) {
            // daemon currently disabled
            _log.info("Enabling ConflictDaemon");
            _daemonEnabledItem.setLabel("Stop Crystal updates");
            _refreshItem.setEnabled(true);
            _client.setDaemonEnabled(true);
            if (_timer != null) {
                // do it
                _timer.start();
            } else {
                createTimer();
            }
        } else {
            // daemon currently enabled
            _log.info("Disabling ConflictDaemon");
            _daemonEnabledItem.setLabel("Start Crystal updates");
            _refreshItem.setEnabled(false);
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

    /**
     * A listener for clicking the menu to exit.
     */
    public void exitAction() {
        if (TRAY_SUPPORTED)
            _tray.remove(_trayIcon);

        String msg = "ConflictClient exited successfully.";
        System.out.println(msg);
        _log.trace("Exit action selected");

        quit(0);
    }

    /**
     * If the deamon is not running, does nothing. If the deamon is running, creates a new executor and performs the calculations on all repos of all
     * projects of the current configuration.
     */
    public void performCalculations() {

        // if the daemon is disabled, don't perform calculations.
        if (!ConflictDaemon.getInstance().isEnabled()) {
            _log.error("Tried to perform calculations on a disabled daemon.");
            return;
        }

        // if the deamon is enabled.
        // # lines marked with //# are removed to simplify the execution process
        // # Executor ex = new SerialExecutor();

        _refreshItem.setLabel("Refreshing...");
        _log.trace("refresh text: " + _refreshItem.getLabel());
        _refreshItem.setEnabled(false);
        _client.setCanUpdate(false);

        startCalculations = System.currentTimeMillis();

        // for (ProjectPreferences projPref : _prefs.getProjectPreference()) {
        // final CalculateLocalStateTask clst = new CalculateLocalStateTask(projPref, this, _client);
        // ex.execute(clst);
        //
        // for (final DataSource source : projPref.getDataSources()) {
        // final CalculateRelationshipTask crt = new CalculateRelationshipTask(source, projPref, this, _client);
        // ex.execute(crt);
        // }
        // }

        for (ProjectPreferences projPref : _prefs.getProjectPreference()) {
            final CalculateProjectTask cpt = new CalculateProjectTask(projPref, this, _client);
            // # ex.execute(cpt);
            cpt.execute();
        }
    }

    /**
     * Either creates a new one (if one did not exist) or displays the existing GUI configuration editor.
     * @throws IOException 
     */
    public void preferencesAction(){
        // stop the daemon if it's running
        if (ConflictDaemon.getInstance().isEnabled())
            daemonAbleAction();
        PreferencesGUIEditorFrame.getPreferencesGUIEditorFrame(_prefs);
    }

    /**
     * Quit Crystal with a status.
     * 
     * @param status
     *            : the exit status (0 means normal).
     */
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

    /**
     * Updates the images and tool tips of all the projects and all the repositories within the current configuration.
     */
    @Override
    public void update() {
        _log.trace("ConflictSystemTray::update()");

        // _log.trace("Task size in update: " + tasks.size());
        
        if (!ConflictDaemon.getInstance().isEnabled())
            return;
            
        // check if anything is PENDING (first local states then relationships
        boolean pendingTask = false;
        for (LocalStateResult localState : ConflictDaemon.getInstance().getLocalStates()) {
            if (localState.getLocalState().equals(LocalStateResult.PENDING)) {
                pendingTask = true;
            }
        }
        for (Relationship relationship : ConflictDaemon.getInstance().getRelationships()) {
            if (relationship.getName().equals(Relationship.PENDING)) {
                pendingTask = true;
            }
        }

        if (pendingTask) {
            _log.trace("Update called with tasks still pending.");

            // keep the UI in updating mode
            _refreshItem.setLabel("Refreshing...");
            _refreshItem.setEnabled(false);
            _client.setCanUpdate(false);
        } else {
            _log.trace("Update called with no tasks pending.");

            createTimer();
            _refreshItem.setLabel("Refresh");
            _refreshItem.setEnabled(true);
            _client.setCanUpdate(true);
        }

        if (TRAY_SUPPORTED)
            updateTrayIcon();

        if (_client != null) {
            _client.update();
        }
    }

    /**
     * Updates the tray icon image to the harshest relationship in the current configuration.
     */
    private void updateTrayIcon() {

        if (!TRAY_SUPPORTED)
            return;

//        _trayIcon.getImage().flush();

        Image icon = Relationship.getDominant(ConflictDaemon.getInstance().getRelationships());

        _trayIcon.setImage(icon);
    }

    /**
     * @return the single instance of ConflictSystemTray
     */
    public static ConflictSystemTray getInstance() {
        if (_instance == null) {
            _instance = new ConflictSystemTray();
        }
        return _instance;
    }

    /**
     * Main execution point that starts Crystal.
     * 
     * @param args
     *            : --version : Prints the version number.
     */
    public static void main(String[] args) {
        setLookAndFeel();

        if (args.length > 0) {
            if (args[0].equals("--version")) {
                System.out.println("Crystal version: " + VERSION_ID);
                System.exit(0);
            }
        }

        ConflictSystemTray.startLogging();
        // UIManager.put("swing.boldMetal", Boolean.FALSE);

        ConflictSystemTray cst = ConflictSystemTray.getInstance();
        cst.createAndShowGUI();
    }

    /**
     * private: set the window to match the native look and feel of the
     * operating system
     */
    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
            // do nothing
        } catch (ClassNotFoundException e) {
            // do nothing
        } catch (InstantiationException e) {
            // do nothing
        } catch (IllegalAccessException e) {
            // do nothing
        }
    }


    public static void startLogging() {
        LSMRLogger.startLog4J(Constants.QUIET_CONSOLE, true, Constants.LOG_LEVEL, System.getProperty("user.home"), ".conflictClientLog");
    }
}
