package crystal.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.SpringLayout;

import org.apache.log4j.Logger;
import org.junit.Assert;

import crystal.Constants;
import crystal.client.ClientPreferences.DuplicateProjectNameException;
import crystal.client.ClientPreferences.NonexistentProjectException;
import crystal.model.DataSource;
import crystal.util.JMultiLineToolTip;
import crystal.util.SpringLayoutUtility;
import crystal.util.ValidInputChecker;

/**
 *		The GUI that allows editing of the Crystal configuration.  
 *		Basically, PreferencesGUIEditorFrame lets you edit a ClientPreferences object by adding,
 *			removing, and augmenting projects, and changing other attributes.
 *
 * @author brun
 */
public class PreferencesGUIEditorFrame extends JFrame {
	
	private static final long serialVersionUID = 4574346360968958312L;
	
	private Logger _log = Logger.getLogger(this.getClass());

	// The singleton instance of this frame.
	private static PreferencesGUIEditorFrame editorFrame;

	/**
	 * @return the singleton instance of this frame.  If a frame already exists, just returns that one. 
	 * 		otherwise, it creates a new one with the specified prefs configuration.
	 * @param prefs: the configuration to use if no frame exists yet.
	 */
	public static PreferencesGUIEditorFrame getPreferencesGUIEditorFrame(ClientPreferences prefs) {
		if (editorFrame == null)
			editorFrame = new PreferencesGUIEditorFrame(prefs);
		editorFrame.setVisible(true);
		return editorFrame;
	}

	/**
	 * @return the singleton instance of this frame.  
	 */
	public static PreferencesGUIEditorFrame getPreferencesGUIEditorFrame() {
		if (editorFrame != null)
			editorFrame.setVisible(true);
		return editorFrame;
	}

	/**
	 * A private constructor to create a new editor frame based on the specified prefs configuration.
	 * @param prefs: the configuration to use.
	 */
	private PreferencesGUIEditorFrame(final ClientPreferences prefs) {
		super("Crystal Configuration Editor");
		Assert.assertNotNull(prefs);

		final ClientPreferences copyPrefs = prefs.clone();
		final Map<JComponent, Boolean> changedComponents = new HashMap<JComponent, Boolean>();
		final Map<JTextField, Boolean> validText = new HashMap<JTextField, Boolean>();
		final JFrame frame = this;
		frame.setIconImage((new ImageIcon(Constants.class.getResource("/crystal/client/images/crystal-ball_blue_128.jpg"))).getImage());

		if (copyPrefs.getProjectPreference().isEmpty()) {
			// ClientPreferences client = new ClientPreferences("/usr/bin/hg/", "/tmp/crystalClient/");
			ProjectPreferences newGuy = new ProjectPreferences(new DataSource("", "", DataSource.RepoKind.HG, false, null), copyPrefs);
			try {
				copyPrefs.addProjectPreferences(newGuy);
				copyPrefs.setChanged(true);
			} catch (DuplicateProjectNameException e) {
				// Just ignore the duplicate project name
			}
		}

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

//		getContentPane().add(new JLabel("Closing this window will save the configuraion settings."));

//		JPanel hgPanel = new JPanel();
//		hgPanel.setLayout(new BoxLayout(hgPanel, BoxLayout.X_AXIS));
//		hgPanel.add(new JLabel("Path to hg executable:"));
//		final JTextField hgPath = new JTextField(prefs.getHgPath());
		// hgPath.setSize(hgPath.getWidth(), 16);
//		hgPanel.add(hgPath);
//		hgPath.addKeyListener(new KeyListener() {
//			public void keyPressed(KeyEvent arg0) {
//			}
//
//			public void keyTyped(KeyEvent arg0) {
//			}
//
//			public void keyReleased(KeyEvent arg0) {
//				prefs.setHgPath(hgPath.getText());
//				prefs.setChanged(true);
//				frame.pack();
//			}
//		});
//
//		JButton hgButton = new JButton("find");
//		hgPanel.add(hgButton);
//		hgButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				new MyPathChooser("Path to hg executable", hgPath, JFileChooser.FILES_ONLY);
//				prefs.setChanged(true);
//			}
//		});
//		getContentPane().add(hgPanel);
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new SpringLayout());
		
		for (int i = 0; i < 2; i++) {
			topPanel.add(new JLabel());
		}
		
		topPanel.add(new JLabel("Valid?"));
		
		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		topPanel.add(new JLabel("Path to scratch space: "));
		
		final JTextField tempPath = new JTextField(copyPrefs.getTempDirectory());
		final JLabel tempPathState = new JLabel();
		tempPanel.add(tempPath);

		changedComponents.put(tempPath, false);
		boolean pathValid = ValidInputChecker.checkDirectoryPath(tempPath.getText())
								|| ValidInputChecker.checkUrl(tempPath.getText());
		if (pathValid) {
			tempPathState.setText("  valid");
			tempPathState.setForeground(Color.GREEN.darker());
		} else {
			tempPathState.setText("invalid");
			tempPathState.setForeground(Color.RED.darker());
		}
		validText.put(tempPath, pathValid);
		
		tempPath.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent arg0) {
				//TODO check if text is valid text
				copyPrefs.setTempDirectory(tempPath.getText());
				changedComponents.put(tempPath, !prefs.getTempDirectory().equals(tempPath.getText()));
				
				boolean pathValid = ValidInputChecker.checkDirectoryPath(tempPath.getText())
										|| ValidInputChecker.checkUrl(tempPath.getText());
				validText.put(tempPath, pathValid);
				if (pathValid) {
					tempPathState.setText("  valid");
					tempPathState.setForeground(Color.GREEN.darker());
				} else {
					tempPathState.setText("invalid");
					tempPathState.setForeground(Color.RED.darker());
				}
				//copyPrefs.setChanged(true);
				frame.pack();
			}
		});
		
		tempPath.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent arg0) {
				tempPath.selectAll();
			}

			@Override
			public void focusLost(FocusEvent arg0) {

			}
		});
		JButton tempButton = new JButton("find");
		tempPanel.add(tempButton);
		tempButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new MyPathChooser("Path to scratch directory", tempPath, JFileChooser.DIRECTORIES_ONLY);
				//copyPrefs.setChanged(true);
			}
		});
		
		topPanel.add(tempPanel);
		topPanel.add(tempPathState);
		
		
		topPanel.add(new JLabel("Refresh rate: "));
		
		final JTextField refreshRate = new JTextField(String.valueOf(copyPrefs.getRefresh()));
		final JLabel rateState = new JLabel("  valid");
		rateState.setForeground(Color.GREEN.darker());
		topPanel.add(refreshRate);
		topPanel.add(rateState);
		changedComponents.put(refreshRate, false);
		validText.put(refreshRate, true);
		
		refreshRate.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent arg0) {
				
				changedComponents.put(refreshRate, !String.valueOf(prefs.getRefresh()).equals(refreshRate.getText()));
				try {
					copyPrefs.setRefresh(Long.valueOf(refreshRate.getText()));
				} catch (Exception e) {
					
				}
				boolean valid = ValidInputChecker.checkStringToLong(refreshRate.getText());
				validText.put(refreshRate, valid);
				
				if (valid) {
					rateState.setText("  valid");
					rateState.setForeground(Color.GREEN.darker());
				} else {
					rateState.setText("invalid");
					rateState.setForeground(Color.RED.darker());
				}
				//copyPrefs.setChanged(true);	
			}
		});
		
		SpringLayoutUtility.formGridInColumn(topPanel, 3, 3);
		getContentPane().add(topPanel);
		
		final JTabbedPane projectsTabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		/*
		 * projectsTabs.addChangeListener(new ChangeListener() {
		 * 
		 * @Override public void stateChanged(ChangeEvent e) { frame.pack(); System.out.println("Tabs changed"); } });
		 */

		for (final ProjectPreferences copyPref : copyPrefs.getProjectPreference()) {
			ProjectPanel current;
			try {
				current = new ProjectPanel(copyPref, copyPrefs, frame, projectsTabs, 
						changedComponents, prefs.getProjectPreferences(copyPref.getEnvironment().getShortName()), validText);
				projectsTabs.addTab(current.getName(), current);
				JPanel pnl = new JPanel();
				JLabel tabName = new JLabel(current.getName());
				pnl.setOpaque(false);
				pnl.add(tabName);
				pnl.add(new DeleteProjectButton(copyPrefs, projectsTabs, frame, current, copyPref));
				projectsTabs.setTabComponentAt(projectsTabs.getTabCount() - 1, pnl);
				// projectsTabs.setTitleAt(projectsTabs.getTabCount() - 1, current.getName());
			} catch (NonexistentProjectException e1) {
				// never happens
			}

		}

		final JButton newProjectButton = new JButton("Add New Project");
		//final JButton deleteProjectButton = new JButton("Delete This Project");

		newProjectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//deleteProjectButton.setEnabled(true);
				HashSet<String> shortNameLookup = new HashSet<String>();
				for (ProjectPreferences current : copyPrefs.getProjectPreference()) {
					shortNameLookup.add(current.getEnvironment().getShortName());
				}
				int count = 1;
				while (shortNameLookup.contains("New Project " + count++))
					;

				final ProjectPreferences newGuy = new ProjectPreferences(new DataSource("New Project " + --count, "", DataSource.RepoKind.HG, false, null), copyPrefs);
				try {
					copyPrefs.addProjectPreferences(newGuy);
				} catch (DuplicateProjectNameException e1) {
					// This should never happen because we just found a clean project name to use.
					throw new RuntimeException("When I tried to create a new project, I found a nice, clean, unused name:\n" + 
							"New Project " + count + "\nbut then the preferences told me that name was in use.  \n" + 
							"This should never happen!");
				}
				
				final ProjectPanel newGuyPanel = new ProjectPanel(newGuy, copyPrefs, frame, projectsTabs, changedComponents, null, validText);
				projectsTabs.addTab("New Project " + count, newGuyPanel);
				JPanel pnl = new JPanel();
				JLabel tabName = new JLabel(newGuy.getEnvironment().getShortName());

				pnl.setOpaque(false);
				pnl.add(tabName);
				pnl.add(new DeleteProjectButton(copyPrefs, projectsTabs, frame, newGuyPanel, newGuy));
				projectsTabs.setTabComponentAt(projectsTabs.getTabCount() - 1, pnl);
				projectsTabs.setSelectedIndex(projectsTabs.getTabCount() - 1);
				copyPrefs.setChanged(true);
				frame.pack();
			}
		});

		/*
		deleteProjectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int current = projectsTabs.getSelectedIndex();
				int option = JOptionPane.showConfirmDialog(null, "Do you want to delete project \"" + projectsTabs.getTitleAt(current) + "\"?", 
						"Empty cache", JOptionPane.YES_NO_OPTION);
				// TODO wait for the current refresh to finish or kill it
				if(option == JOptionPane.YES_OPTION) {
					prefs.removeProjectPreferencesAtIndex(current);
					projectsTabs.remove(current);
					if (prefs.getProjectPreference().isEmpty())
						deleteProjectButton.setEnabled(false);
					prefs.setChanged(true);
					frame.pack();
				}
			}
		});
		*/

		getContentPane().add(newProjectButton);
		getContentPane().add(projectsTabs);
		//getContentPane().add(deleteProjectButton);

		JPanel savePanel = new JPanel();
		savePanel.setLayout(new FlowLayout());
		final JButton saveButton = new JButton("Save");
		final JButton cancelButton = new JButton("Cancel");
		savePanel.add(saveButton);
		savePanel.add(cancelButton);
		getContentPane().add(savePanel);
		
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {			
				if (copyPrefs.hasChanged() || changedComponents.values().contains(true)) {

					if(!validText.values().contains(false)){
						try {
                            ClientPreferences.savePreferencesToDefaultXML(copyPrefs);
						} catch (FileNotFoundException fnfe) {
                            _log.error("Could not write to the configuration file. " + fnfe);
                        }
						//TODO
						copyPrefs.setChanged(false);	
						frame.setVisible(false);
					} else {
						JOptionPane.showMessageDialog(null, "You have invalid input.", 
								"Warning", JOptionPane.ERROR_MESSAGE);
					}
				} else {
					
					frame.setVisible(false);
				} 
			}
			
		});
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editorFrame = new PreferencesGUIEditorFrame(prefs);
				copyPrefs.setChanged(false);
				frame.setVisible(false);
			}
			
		});
		
		addWindowListener(new WindowListener() {
			public void windowClosing(WindowEvent arg0) {
				setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				if (copyPrefs.hasChanged() || changedComponents.values().contains(true)) {
					int n = JOptionPane.showConfirmDialog(null, "Do you want to save your data?", 
							"Saving data", JOptionPane.YES_NO_CANCEL_OPTION);
					if (n == JOptionPane.YES_OPTION) {
						/*for (JTextField field : validText.keySet()) {
							if(validText.get(field) == false) {
								System.out.println(field.getText() + ": bad");
							} else {
								System.out.println(field.getText() + ": good");
							}
						}*/
						
						if(!validText.values().contains(false)){
							try {
                                ClientPreferences.savePreferencesToDefaultXML(copyPrefs);
		                    } catch (FileNotFoundException fnfe) {
	                            _log.error("Could not write to the configuration file. " + fnfe);
	                        }
							//TODO
							copyPrefs.setChanged(false);	
						} else {
							JOptionPane.showMessageDialog(null, "You have invalid input.", 
									"Warning", JOptionPane.ERROR_MESSAGE);
							setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
						}
					} else if(n == JOptionPane.CANCEL_OPTION) {
						setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					} else {	// option is no
						editorFrame = new PreferencesGUIEditorFrame(prefs);
						copyPrefs.setChanged(false);
					}
				}
			}

			public void windowActivated(WindowEvent arg0) {
			}

			public void windowClosed(WindowEvent arg0) {
			}

			public void windowDeactivated(WindowEvent arg0) {
                //Reload the preferences
			    ConflictSystemTray.getInstance().loadPreferences();
			}

			public void windowDeiconified(WindowEvent arg0) {
			}

			public void windowIconified(WindowEvent arg0) {
			}

			public void windowOpened(WindowEvent arg0) {
			}
		});

		pack();
		//setVisible(true);
	}


	private class DeleteProjectButton extends JButton {
		private static final long serialVersionUID = 1L;

		public DeleteProjectButton(final ClientPreferences prefs, 
				final JTabbedPane projectsTabs, final JFrame frame, final ProjectPanel newGuyPanel,
				final ProjectPreferences newGuy) {
			super("X");
			setToolTipText("delete this project ");
			addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					String targetProject = "";
					for(int i = 0; i < projectsTabs.getTabCount(); i++) {
						JPanel tabPanel = (JPanel) projectsTabs.getTabComponentAt(i);
						JLabel nameLabel = (JLabel) tabPanel.getComponent(0);
						String tabName = nameLabel.getText();
						if (tabName.equals(newGuy.getEnvironment().getShortName())) {
							targetProject = tabName;
						}
					}	
					int option = JOptionPane.showConfirmDialog(null, "Do you want to delete project \"" + targetProject + "\"?", 
							"Delete Project", JOptionPane.YES_NO_OPTION);

					if(option == JOptionPane.YES_OPTION) {
						prefs.removeProjectPreferences(newGuy);
						projectsTabs.remove(newGuyPanel);
						prefs.setChanged(true);
						frame.pack();
					}
				}
				
			});
			addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent arg0) {
				}

				@Override
				public void mouseEntered(MouseEvent arg0) {
					setBorderPainted(true);
				}

				@Override
				public void mouseExited(MouseEvent arg0) {
					setBorderPainted(false);
				}

				@Override
				public void mousePressed(MouseEvent arg0) {
				}

				@Override
				public void mouseReleased(MouseEvent arg0) {
				}
				
			});
			setPreferredSize(new Dimension(15, 15));
			setContentAreaFilled(false);
			setBorder(BorderFactory.createEtchedBorder());
			setBorderPainted(false);
		}
		
        public JToolTip createToolTip() {
            return new JMultiLineToolTip();
        }
	}

	/**
	 * A file chooser to select paths
	 * @author brun
	 */
	protected static class MyPathChooser extends JFrame {
		private static final long serialVersionUID = 4078764196578702307L;

		MyPathChooser(String name, final JTextField path, int fileSelectionMode) {
			super(name);

			final JFrame chooserFrame = this;

			setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

			final JFileChooser chooser = new JFileChooser(path.getText());
			
			chooser.setFileSelectionMode(fileSelectionMode);
			getContentPane().add(chooser);
			chooser.setFileHidingEnabled(true);
			pack();
			setVisible(true);

			chooser.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String command = e.getActionCommand();
					if (command.equals(JFileChooser.APPROVE_SELECTION)) {
						path.setText(chooser.getSelectedFile().getAbsolutePath().replace('\\', '/'));
						// and pretend like you typed a key:
						path.getKeyListeners()[0].keyReleased(new KeyEvent(path, 0, 0, 0, 0, ' '));
					}
					chooserFrame.setVisible(false);
				}
			});
		}
	}
}
