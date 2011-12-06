package crystal.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;

import org.apache.log4j.Logger;

import crystal.Constants;
import crystal.model.DataSource;
import crystal.model.LocalStateResult;
import crystal.model.Relationship;
import crystal.util.JMultiLineToolTip;
import crystal.util.RunIt;
import crystal.util.SpringLayoutUtility;

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
	public void createAndShowGUI(final ClientPreferences prefs) {
		_preferences = prefs;

		// Create and set up the window.
		_frame = new JFrame("Crystal");
		_frame.setIconImage((new ImageIcon(Constants.class.getResource("/crystal/client/images/crystal-ball_blue_128.png"))).getImage());

		// Set up the menu:
		JMenuBar menuBar = new JMenuBar();
		// could set the manuBar layout to make the last, invisible blank menu item take up all the space.
		// menuBar.setLayout(mgr)
		JMenu fileMenu = new JMenu("File");
		// JMenu aboutMenu = new JMenu("About");
		menuBar.add(fileMenu);
		// menuBar.add(aboutMenu);

		
		// making tool tips remain visible
		int dismissDelay = ToolTipManager.sharedInstance().getDismissDelay();
		
		dismissDelay = Integer.MAX_VALUE;
		ToolTipManager.sharedInstance().setDismissDelay(dismissDelay);
		
		_refresh = new JMenuItem("Refresh");
		JMenuItem editConfiguration = new JMenuItem("Edit configuration");
		_disableDaemon = new JMenuItem("Stop Crystal updates");
		JMenuItem clearCache = new JMenuItem("Clear cache");
		JMenuItem exit = new JMenuItem("Exit");
		JMenuItem about = new JMenuItem("About");
		JMenuItem reload = new JMenuItem("Reload configuration");
		JMenuItem blank = new JMenuItem("");
                
		blank.setArmed(false);

		fileMenu.add(_refresh);
		fileMenu.add(editConfiguration);
		fileMenu.add(reload);
		fileMenu.add(_disableDaemon);
		fileMenu.add(clearCache);
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
		
		reload.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                _log.info("Reloading configuration requested by user.");
                setDaemonEnabled(true);
                ConflictSystemTray.getInstance().loadPreferences();
            }
        });

		_refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				_log.info("Refresh manually selected.");
				ConflictSystemTray.getInstance().performCalculations();
			}
		});
		
		clearCache.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int option = JOptionPane.showConfirmDialog(null, "Do you want to empty cache?", 
						"Empty cache", JOptionPane.YES_NO_OPTION);
				// TODO wait for the current refresh to finish or kill it
				if(option == JOptionPane.YES_OPTION) {
				    boolean hadToDisable =_disableDaemon.getText().equals("Stop Crystal updates");
				     if (hadToDisable)
				         ConflictSystemTray.getInstance().daemonAbleAction();
				     // TODO wait for disabling to finish
				     
					RunIt.deleteDirectory(new File(_preferences.getTempDirectory()));
					_log.info("User selected Clear Cache from the menu. All cache has been emptied at " + _preferences.getTempDirectory());
					String newDirectoy = _preferences.getTempDirectory();
					if ((new File(newDirectoy)).mkdir())
						_log.info("An empty cache directory has been created at " + newDirectoy);
					else
						_log.error("Failed to clear an empty cache directory at " + newDirectoy);
					if (hadToDisable) 
	                    ConflictSystemTray.getInstance().daemonAbleAction();
				}
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
	    reloadWindowBody(_preferences);


		refresh();

		_frame.setVisible(true);
		_frame.toFront();
		_frame.pack();
	}
	
    public void reloadWindowBody(final ClientPreferences prefs) {
        _preferences = prefs;
        _frame.getContentPane().removeAll();
        _frame.getContentPane().setLayout(new BoxLayout(_frame.getContentPane(), BoxLayout.Y_AXIS));

        // Create a grid to hold the conflict results
        int maxSources = 0;
        for (ProjectPreferences projPref : prefs.getProjectPreference()) {
            if (projPref.getNumOfVisibleSources() > maxSources)
                maxSources = projPref.getNumOfVisibleSources();
        }

        final JPanel grid = new JPanel(new SpringLayout()); 
        
        // Create the iconMap and populate it with icons.
        // Also create the layout of the GUI.
        _iconMap = new HashMap<DataSource, JLabel>();
        for (final ProjectPreferences projPref : prefs.getProjectPreference()) {
            JPanel projectPanel = new JPanel(new SpringLayout());
            
            // name of project on the left, with an empty JLabel for the Action
            JPanel name = new JPanel();
            name.setLayout(new BoxLayout(name, BoxLayout.Y_AXIS));
                        
            JLabel projectName = new JLabel(projPref.getEnvironment().getShortName() + " ");
            final JPopupMenu projectMenu = new JPopupMenu("Project menu");

            projectMenu.add(getClearCacheItem(projPref));
            projectMenu.add(getAddRepoItem(projPref, prefs));


            projectName.addMouseListener(new MouseAdapter() {

                public void mousePressed(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        projectMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }

                public void mouseReleased(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        projectMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }

                public void mouseClicked(MouseEvent e) {
                }
            });

            name.add(projectName);

            JLabel action = new JLabel();
            action.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
            _iconMap.put(projPref.getEnvironment(), action);
            action.setText(new String(new char["git commit".length()]).replace('\0', ' '));
            
            ConflictDaemon.getInstance().getLocalState(projPref.getEnvironment());
            name.add(action);
            projectPanel.add(name);


            JPanel iconPanel = new JPanel(new BorderLayout());
            final JPanel iconGrid = new JPanel(new GridLayout(1, 0, 3, 3));
            iconPanel.add(iconGrid, BorderLayout.NORTH);

            for (final DataSource source : projPref.getDataSources()) {
                if (!(source.isHidden())) {

                    ImageIcon image = new ImageIcon();
                    final JLabel imageLabel = new JLabel(source.getShortName(), image, SwingConstants.CENTER) {
                        private static final long serialVersionUID = 1L;

                        public JToolTip createToolTip() {
                            return new JMultiLineToolTip();
                        }
                    };


                    final JPopupMenu repoMenu = new JPopupMenu("Repository");
                    JMenuItem deleteRepo = new JMenuItem("Delete this repository");




                    deleteRepo.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            int option = JOptionPane.showConfirmDialog(null, "Do you want to delete the " + source.getShortName() + "'s repository?", 
                                    "Delete repository?", JOptionPane.YES_NO_OPTION);

                            if(option == JOptionPane.YES_OPTION) {
                                projPref.getDataSources().remove(source);
                                _iconMap.remove(source);
                                iconGrid.remove(imageLabel);
                                try {
                                    ClientPreferences.savePreferencesToDefaultXML(prefs);
                                } catch (FileNotFoundException fnfe) {
                                    _log.error("Could not write to the configuration file. " + fnfe);
                                }
                            }


                        }

                    });

                    JMenuItem editRepo = new JMenuItem("Edit Repository");

                    editRepo.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                            new DataSourceGuiEditorFrame(prefs, projPref, source.getShortName());
                        }

                    });

                    repoMenu.add(getAddRepoItem(projPref, prefs));
                    repoMenu.add(getClearCacheItem(projPref));
                    repoMenu.add(editRepo);
                    repoMenu.add(deleteRepo);

                    imageLabel.addMouseListener(new MouseAdapter() {

                        public void mousePressed(MouseEvent e) {
                            if (e.isPopupTrigger()) {
                                repoMenu.show(e.getComponent(), e.getX(), e.getY());
                            }
                        }

                        public void mouseReleased(MouseEvent e) {
                            if (e.isPopupTrigger()) {
                                repoMenu.show(e.getComponent(), e.getX(), e.getY());
                            }
                        }

                        public void mouseClicked(MouseEvent e) {
                        }
                    });

                    _iconMap.put(source, imageLabel);
                    ConflictDaemon.getInstance().getRelationship(source);
                    imageLabel.setVerticalTextPosition(JLabel.TOP);
                    imageLabel.setHorizontalTextPosition(JLabel.CENTER);
                    iconGrid.add(imageLabel);
                }
            }




            // Fill in the rest of the grid row with blanks
            for (int i = projPref.getNumOfVisibleSources(); i < maxSources; i++)
                iconGrid.add(new JLabel());


            projectPanel.add(iconPanel);
            SpringLayoutUtility.formGridInColumn(projectPanel, 1, 2);

            projectPanel.setBorder(BorderFactory.createLineBorder(Color.black));

            grid.add(projectPanel);
        }

        SpringLayoutUtility.formGridInColumn(grid, prefs.getProjectPreference().size(), 1);

        _frame.getContentPane().add(grid);
    }

	
	/**
	 * Get JMenuItem to add repository for given project
	 * @param projPref
	 * @param prefs
	 * @return
	 */
	private JMenuItem getAddRepoItem(final ProjectPreferences projPref, final ClientPreferences prefs){
		JMenuItem addRepo = new JMenuItem("Add new repository");
		addRepo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new DataSourceGuiEditorFrame(prefs, projPref);
			}
			
		});
		return addRepo;
	}
	
	/**
	 * Get JMenuItem to clear cache for the given project
	 * @param projPref
	 * @return
	 */
	private JMenuItem getClearCacheItem(final ProjectPreferences projPref){
		JMenuItem clearProjectCacheMenu = new JMenuItem("Clear " + projPref.getEnvironment().getShortName() + " project cache");
		clearProjectCacheMenu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent action) {
				int option = JOptionPane.showConfirmDialog(null, "Do you want to empty the " + projPref.getEnvironment().getShortName() + " cache?", 
						"Warning", JOptionPane.YES_NO_OPTION);
				// TODO wait for the current refresh to finish or kill it
				if(option == JOptionPane.YES_OPTION) {
				    boolean hadToDisable =_disableDaemon.getText().equals("Stop Crystal updates");
				    if (hadToDisable)
				        ConflictSystemTray.getInstance().daemonAbleAction();

					Set<String> target = new TreeSet<String>();
					
					target.add(_preferences.getTempDirectory() + projPref.getEnvironment().getShortName() + "_" + projPref.getEnvironment().getShortName());
					for(DataSource ds : projPref.getDataSources()){
						
						target.add(_preferences.getTempDirectory() + projPref.getEnvironment().getShortName() + "_" + ds.getShortName());
      					}
					
					for(String path : target){
						RunIt.deleteDirectory(new File(path));
						_log.info("Deleting " + path);
					}

					_log.info("Cleared the " + projPref.getEnvironment().getShortName() + " project's cache.");
					if (hadToDisable)
					    ConflictSystemTray.getInstance().daemonAbleAction();
				}
			}
		});
		
		return clearProjectCacheMenu;
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
			_disableDaemon.setText("Stop Crystal updates");
			_refresh.setEnabled(true);
		} else {
			_disableDaemon.setText("Start Crystal updates");
			_refresh.setEnabled(false);
		}
		ConflictDaemon.getInstance().enable(enable);
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
				if ((actionResult == null) || (actionResult.getAction() == null))
				    action.setText(new String(new char["git commit".length()]).replace('\0', ' '));
				else { 
				    if (actionResult.getAction().length() < "git commit".length()) {
				        action.setText(actionResult.getAction() 
				        		+ new String(new char["git commit".length() - actionResult.getAction().length()]).replace('\0', ' '));
				    } else
				        action.setText(actionResult.getAction());
				}
				String tip = actionResult.getErrorMessage();
                if ((tip == null) || (tip.trim().equals("")))
                    action.setToolTipText(null);
                else {
                    action.setToolTipText(tip);
                    if ((action.getText() == null) || (action.getText().isEmpty()))
                    	action.setText(new String(new char["git commit".length()]).replace('\0', ' '));
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