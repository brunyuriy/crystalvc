package crystal.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.xml.ws.Action;

import org.junit.Assert;

import crystal.Constants;
import crystal.client.ClientPreferences.DuplicateProjectNameException;
import crystal.model.DataSource;

/**
 *		The GUI that allows editing of the Crystal configuration.  
 *		Basically, PreferencesGUIEditorFrame lets you edit a ClientPreferences object by adding,
 *			removing, and augmenting projects, and changing other attributes.
 *
 * @author brun
 */
public class PreferencesGUIEditorFrame extends JFrame {

	private static final long serialVersionUID = 4574346360968958312L;

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

		final JFrame frame = this;
		frame.setIconImage((new ImageIcon(Constants.class.getResource("/crystal/client/images/crystal-ball_blue_128.jpg"))).getImage());

		if (prefs.getProjectPreference().isEmpty()) {
			// ClientPreferences client = new ClientPreferences("/usr/bin/hg/", "/tmp/crystalClient/");
			ProjectPreferences newGuy = new ProjectPreferences(new DataSource("", "", DataSource.RepoKind.HG, false, null), prefs);
			try {
				prefs.addProjectPreferences(newGuy);
				prefs.setChanged(true);
			} catch (DuplicateProjectNameException e) {
				// Just ignore the duplicate project name
			}
		}

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		getContentPane().add(new JLabel("Closing this window will save the configuraion settings."));

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

		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		tempPanel.add(new JLabel("Path to scratch space:"));
		final JTextField tempPath = new JTextField(prefs.getTempDirectory());
		tempPanel.add(tempPath);
		tempPath.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent arg0) {
				prefs.setTempDirectory(tempPath.getText());
				prefs.setChanged(true);
				frame.pack();
			}
		});
		JButton tempButton = new JButton("find");
		tempPanel.add(tempButton);
		tempButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new MyPathChooser("Path to scratch directory", tempPath, JFileChooser.DIRECTORIES_ONLY);
				prefs.setChanged(true);
			}
		});
		getContentPane().add(tempPanel);

		final JTabbedPane projectsTabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		/*
		 * projectsTabs.addChangeListener(new ChangeListener() {
		 * 
		 * @Override public void stateChanged(ChangeEvent e) { frame.pack(); System.out.println("Tabs changed"); } });
		 */

		for (ProjectPreferences pref : prefs.getProjectPreference()) {
			ProjectPanel current = new ProjectPanel(pref, prefs, frame, projectsTabs);
			projectsTabs.addTab(current.getName(), current);
			// projectsTabs.setTitleAt(projectsTabs.getTabCount() - 1, current.getName());
		}

		final JButton newProjectButton = new JButton("Add New Project");
		final JButton deleteProjectButton = new JButton("Delete This Project");

		newProjectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteProjectButton.setEnabled(true);
				HashSet<String> shortNameLookup = new HashSet<String>();
				for (ProjectPreferences current : prefs.getProjectPreference()) {
					shortNameLookup.add(current.getEnvironment().getShortName());
				}
				int count = 1;
				while (shortNameLookup.contains("New Project " + count++))
					;

				ProjectPreferences newGuy = new ProjectPreferences(new DataSource("New Project " + --count, "", DataSource.RepoKind.HG, false, null), prefs);
				try {
					prefs.addProjectPreferences(newGuy);
				} catch (DuplicateProjectNameException e1) {
					// This should never happen because we just found a clean project name to use.
					throw new RuntimeException("When I tried to create a new project, I found a nice, clean, unused name:\n" + 
							"New Project " + count + "\nbut then the preferences told me that name was in use.  \n" + 
							"This should never happen!");
				}
				
				ProjectPanel newGuyPanel = new ProjectPanel(newGuy, prefs, frame, projectsTabs);
				projectsTabs.addTab("New Project " + count, newGuyPanel);
				prefs.setChanged(true);
				frame.pack();
			}
		});

		deleteProjectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int current = projectsTabs.getSelectedIndex();
				prefs.removeProjectPreferencesAtIndex(current);
				projectsTabs.remove(current);
				if (prefs.getProjectPreference().isEmpty())
					deleteProjectButton.setEnabled(false);
				prefs.setChanged(true);
				frame.pack();
			}
		});

		getContentPane().add(newProjectButton);
		getContentPane().add(projectsTabs);
		getContentPane().add(deleteProjectButton);

		//TODO
		addWindowListener(new WindowListener() {
			public void windowClosing(WindowEvent arg0) {
				boolean canClose = true;
				setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);				
				if (prefs.hasChanged()) {
					for(ProjectPreferences pp : prefs.getProjectPreference()){
						for(DataSource ds : pp.getDataSources()){
							if(ds.getCloneString().trim().equals("")) {
								canClose = false;
							}
						}
					}

					if(canClose){
						ClientPreferences.savePreferencesToDefaultXML(prefs);
						prefs.setChanged(false);	

					} else {
						JOptionPane.showMessageDialog(null, "You have invalid input for clone address.", 
								"Warning", JOptionPane.ERROR_MESSAGE);
						setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					}
				}
			}

			public void windowActivated(WindowEvent arg0) {
			}

			public void windowClosed(WindowEvent arg0) {
			}

			public void windowDeactivated(WindowEvent arg0) {
			}

			public void windowDeiconified(WindowEvent arg0) {
			}

			public void windowIconified(WindowEvent arg0) {
			}

			public void windowOpened(WindowEvent arg0) {
			}
		});

		pack();
		setVisible(true);
	}


	/**
	 * A file chooser to select paths
	 * @author brun
	 */
	private static class MyPathChooser extends JFrame {
		private static final long serialVersionUID = 4078764196578702307L;

		MyPathChooser(String name, final JTextField path, int fileSelectionMode) {
			super(name);

			final JFrame chooserFrame = this;

			setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

			final JFileChooser chooser = new JFileChooser(path.getText());
			chooser.setFileSelectionMode(fileSelectionMode);
			getContentPane().add(chooser);

			pack();
			setVisible(true);

			chooser.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					path.setText(chooser.getSelectedFile().getAbsolutePath());
					// and pretend like you typed a key:
					path.getKeyListeners()[0].keyTyped(new KeyEvent(path, 0, 0, 0, 0, ' '));
					chooserFrame.setVisible(false);
				}
			});
		}
	}

	


	/**
	 * An execution point used only for testing.  
	 */
	//public static void main(String[] args) {
	//	ClientPreferences client = new ClientPreferences("temp", "hgPath", Constants.DEFAULT_REFRESH);
	//	ProjectPreferences one = new ProjectPreferences(new DataSource("first project", "~brun\\firstrepo", DataSource.RepoKind.HG, false, null), client);
	//	DataSource oneOther = new DataSource("Mike's copy", "~mernst\\repo", DataSource.RepoKind.HG, false, null);
	//	DataSource twoOther = new DataSource("Reid's copy", "~rtholmes\\repo", DataSource.RepoKind.HG, false, null);
	//	DataSource threeOther = new DataSource("David's copy", "~notkin\\repo", DataSource.RepoKind.HG, false, null);
	//	one.addDataSource(oneOther);
	//	one.addDataSource(twoOther);

	//	ProjectPreferences two = new ProjectPreferences(new DataSource("second project", "~brun\\secondrepo", DataSource.RepoKind.HG, false, null), client);
	//	two.addDataSource(threeOther);
	//	two.addDataSource(oneOther);

	//	try {
	//	client.addProjectPreferences(one);
	//	client.addProjectPreferences(two);
	//	} catch (DuplicateProjectNameException e) {
			// This should really never happen because we're dealing with an empty set of preferences.
	//		throw new RuntimeException("When I was creating some nice fresh preferences with two projects, one and two, I got this error:\n" + 
	//				e.getMessage());  
	//	}

	//	getPreferencesGUIEditorFrame(client);

		// int i = 0;
		// while(true) {
		// i = (i + 1) % 1000000;
		// if (i == 0)
		// System.out.println(mine.getEnvironment().getShortName());
		// }
	//}

}
