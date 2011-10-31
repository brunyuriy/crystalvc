package crystal.client;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import crystal.model.DataSource;
import crystal.model.DataSource.RepoKind;
import crystal.client.PreferencesGUIEditorFrame.MyPathChooser;
import crystal.util.SpringLayoutUtility;
import crystal.util.ValidInputChecker;

/**
 * A panel used for the configuration editor (PreferencesGUIEditorFrame) to display a single project. 
 * 
 * @author brun
 */
public class ProjectPanel extends JPanel {

	private static final long serialVersionUID = 5244512987255240473L;
	private static final int SOURCES_COLUMNS = 6;
	private static final int ENVIRON_COLUMNS = 3;
	private static final int BAR_SIZE = 1;
	
	
	// The name of the project
	private String _name;

	/**
	 * Creates a new panel, with the pref Project configuration.
	 * @param: copyPref : the project configuration to display
	 * @param: copyPrefs: the overall configuration associated with this project
	 * @param mainFrame: the frame that will keep this panel
	 * @param tabbedPane: the pane on the mainFrame that will keep this panel
	 * @param prefs the original overall configuration
	 *  
	 */
	public ProjectPanel(final ProjectPreferences copyPref, final ClientPreferences copyPrefs, 
			final JFrame mainFrame, final JTabbedPane tabbedPane, 
			final Map<JComponent, Boolean> changedComponents, final ProjectPreferences pref,
			final Map<JTextField, Boolean> validText) {
		super();

		final JPanel panel = this;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		_name = copyPref.getEnvironment().getShortName();
		
		JPanel prefEnvironmentPanel = new JPanel(new SpringLayout());

		for (int i = 0 ; i < 2; i++) {
			prefEnvironmentPanel.add(new JLabel());
		}
		prefEnvironmentPanel.add(new JLabel("Valid?"));
		
		prefEnvironmentPanel.add(new JLabel("Project Name: "));
		final JTextField shortName = new JTextField(copyPref.getEnvironment().getShortName());
		final JLabel nameState = new JLabel("  valid");
		nameState.setForeground(Color.GREEN.darker());
		changedComponents.put(shortName, false);
		validText.put(shortName, true);
		
		prefEnvironmentPanel.add(shortName);
		prefEnvironmentPanel.add(nameState);
		
		shortName.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent arg0) {
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				copyPref.getEnvironment().setShortName(shortName.getText());
				_name = shortName.getText();
				((JLabel)((JPanel)tabbedPane.getTabComponentAt(tabbedPane.getSelectedIndex())).getComponent(0)).setText(_name);
				
				if (pref != null) {
					changedComponents.put(shortName, 
							!shortName.getText().equals(pref.getEnvironment().getShortName()));
				}
				boolean valid = ValidInputChecker.checkProjectPreferencesNameDuplicate(copyPrefs, copyPref);
				validText.put(shortName, valid);
				setState(nameState, valid);
				//prefs.setChanged(true);
			}

			@Override
			public void keyTyped(KeyEvent arg0) {	
			}
			
		});

		prefEnvironmentPanel.add(new JLabel("Parent Name (optional): "));
		final JTextField parentName = new JTextField(copyPref.getEnvironment().getParent());
		final JLabel parentState = new JLabel();
		prefEnvironmentPanel.add(parentName);
		prefEnvironmentPanel.add(parentState);
		
		changedComponents.put(parentName, false);
		validText.put(parentName, true);
		
		parentName.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent arg0) {
				copyPref.getEnvironment().setParent(parentName.getText());
				if (pref != null) {
					changedComponents.put(parentName, 
							!parentName.getText().equals(pref.getEnvironment().getParent()));
				}
				//prefs.setChanged(true);
				panel.validate();
				mainFrame.pack();
			}
		});


		prefEnvironmentPanel.add(new JLabel("Repo Type: "));
		final JComboBox type = new JComboBox();
		final JLabel typeState = new JLabel();
		
		//TODO change HG and GIT to hg and git.
		type.addItem(DataSource.RepoKind.HG);
		// Don't allow users to select GIT until it is ready
//		type.addItem(DataSource.RepoKind.GIT);
		type.setSelectedItem(copyPref.getEnvironment().getKind());
		prefEnvironmentPanel.add(type);
		prefEnvironmentPanel.add(typeState);
		
		changedComponents.put(type, false);
		
		type.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copyPref.getEnvironment().setKind((DataSource.RepoKind) type.getSelectedItem());
				if (pref != null) {
					changedComponents.put(type, 
							!((RepoKind)type.getSelectedItem()).equals(pref.getEnvironment().getKind()));
				}
			}
		});

		prefEnvironmentPanel.add(new JLabel("Clone Address: "));
		final JTextField address = new JTextField(copyPref.getEnvironment().getCloneString());
		final JPanel addressPanel = new JPanel();
		addressPanel.setLayout(new BoxLayout(addressPanel, BoxLayout.X_AXIS));
		JButton addressButton = new JButton("find");
		final JLabel addressState = new JLabel();
		
		addressPanel.add(address);
		addressPanel.add(addressButton);

		prefEnvironmentPanel.add(addressPanel);
		prefEnvironmentPanel.add(addressState);
		
		changedComponents.put(address, false);

		boolean addressValid = (address.getText().startsWith("http")) || (address.getText().startsWith("ssh")) ||		        
		        ValidInputChecker.checkDirectoryPath(address.getText());
		validText.put(address, addressValid);
		setState(addressState, addressValid);

		address.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				copyPref.getEnvironment().setCloneString(address.getText());
				if (pref != null) {
					changedComponents.put(address, 
							!address.getText().equals(pref.getEnvironment().getCloneString()));
				}
				boolean valid = (address.getText().startsWith("http")) || (address.getText().startsWith("ssh")) ||              
		                ValidInputChecker.checkDirectoryPath(address.getText()); 
				validText.put(address, valid);
				setState(addressState, valid);
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
			
		});
		
		addressButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new MyPathChooser("Path to clone address directory", address, JFileChooser.DIRECTORIES_ONLY);
				//new MyPathChooser("Path to scratch directory", address, JFileChooser.DIRECTORIES_ONLY);
				//prefs.setChanged(true);
			}
			
		});
		
		prefEnvironmentPanel.add(new JLabel("Compile Command: "));
		String compileCommand = copyPref.getEnvironment().getCompileCommand();
		if(compileCommand == null || compileCommand.trim().equals("")){
			compileCommand = "";
		}
		final JTextField compile = new JTextField(compileCommand);
		final JLabel compileState = new JLabel("  valid");
		compileState.setForeground(Color.GREEN.darker());
		
		prefEnvironmentPanel.add(compile);
		prefEnvironmentPanel.add(compileState);
		
		changedComponents.put(compile, false);
		
		boolean compileValid = ValidInputChecker.checkCommand(compile.getText());
		validText.put(compile, compileValid);
		
		setState(compileState, compileValid);

		compile.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent arg0) {
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
				copyPref.getEnvironment().setCompileCommand(compile.getText());
				if (pref != null) {
					changedComponents.put(compile, 
							!compile.getText().equals(pref.getEnvironment().getCompileCommand()));
					
				}
				boolean compileValid = ValidInputChecker.checkCommand(compile.getText());
				validText.put(compile, compileValid);
				
				setState(compileState, compileValid);
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
			}
		});
		
		prefEnvironmentPanel.add(new JLabel("Test Command: "));
		String testCommand = copyPref.getEnvironment().getTestCommand();
		if(testCommand == null || testCommand.trim().equals("")){
			testCommand = "";
		}
		final JTextField test = new JTextField(testCommand);
		final JLabel testState = new JLabel("  valid");
		testState.setForeground(Color.GREEN.darker());
		
		prefEnvironmentPanel.add(test);
		prefEnvironmentPanel.add(testState);
		
		changedComponents.put(test, false);
		
		boolean testValid = ValidInputChecker.checkCommand(test.getText());
		validText.put(compile, testValid);

		setState(testState, testValid);
		test.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				copyPref.getEnvironment().setTestCommand(test.getText());
				if (pref != null) {
					changedComponents.put(test, 
							!test.getText().equals(pref.getEnvironment().getTestCommand()));			
				}
				boolean testValid = ValidInputChecker.checkCommand(test.getText());
				validText.put(compile, testValid);
				
				setState(testState, testValid);
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
			
		});

		SpringLayoutUtility.formGridInColumn(prefEnvironmentPanel, 
				prefEnvironmentPanel.getComponents().length / ENVIRON_COLUMNS, ENVIRON_COLUMNS);
		
		panel.add(prefEnvironmentPanel);



		final JPanel sourcesPanel = new JPanel(new SpringLayout());
		final JButton newRepoButton = new JButton("Add New Repository");
		newRepoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				HashSet<String> shortNameLookup = new HashSet<String>();
				for (DataSource current : copyPref.getDataSources()) {
					shortNameLookup.add(current.getShortName());
				}
				int count = 1;
				while (shortNameLookup.contains("New Repo " + count++))
					;

				DataSource newGuy = new DataSource("New Repo " + --count, "", 
						DataSource.RepoKind.HG, false, null);
				copyPref.addDataSource(newGuy);		
				addRepoPanel(newGuy, copyPref, copyPrefs, panel, mainFrame, sourcesPanel, changedComponents, null, validText);
				SpringLayoutUtility.formGridInColumn(sourcesPanel, copyPref.getDataSources().size() + BAR_SIZE, SOURCES_COLUMNS);
				
				copyPrefs.setChanged(true);
				panel.validate();
				mainFrame.pack();
			}
		});
		add(newRepoButton);


		JLabel pShortName = new JLabel("Short Name", JLabel.CENTER);
		JLabel pHide = new JLabel("Hide?", JLabel.CENTER);
		JLabel pParent = new JLabel("Parent", JLabel.CENTER);
		JLabel pClone = new JLabel("Clone Address", JLabel.CENTER);
		JLabel pDelete = new JLabel();
		JLabel pState = new JLabel("Valid?", JLabel.CENTER);
		
		sourcesPanel.add(pShortName);
		sourcesPanel.add(pHide);
		sourcesPanel.add(pParent);
		sourcesPanel.add(pClone);
		sourcesPanel.add(pDelete);
		sourcesPanel.add(pState);
		
		for (DataSource copySource : copyPref.getDataSources()) {
			addRepoPanel(copySource, copyPref, copyPrefs, panel, mainFrame, 
					sourcesPanel, changedComponents, pref.getDataSource(copySource.getShortName()), validText);
		}

		SpringLayoutUtility.formGridInColumn(sourcesPanel, copyPref.getDataSources().size() + BAR_SIZE, SOURCES_COLUMNS);
		
		add(sourcesPanel);
	}
	
	/**
	 * @return this project's name
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Creates a panel used to display a single repository of a project.
	 * @param source: the configuration of the repo
	 * @param pref: the configuration of the project
	 * @param prefs: the overall configuration 
	 * @param panel: the panel in which this panel sits
	 * @param mainFrame: the frame in which this panel sits
	 * @return a panel used to display a single repository of a project.
	 */
	private void addRepoPanel(final DataSource copySource, final ProjectPreferences copyPref, 
			final ClientPreferences copyPrefs, final JPanel panel,
			final JFrame mainFrame, final JPanel sourcesPanel, 
			final Map<JComponent, Boolean> changedComponents, final DataSource source,
			final Map<JTextField, Boolean> validText) {
		
		//repoPanel.setLayout(new BoxLayout(repoPanel, BoxLayout.X_AXIS));

		/*
		 * repoPanel.add(new JLabel("Repo Type")); final JComboBox type = new JComboBox();
		 * type.addItem(DataSource.RepoKind.HG); // type.addItem(DataSource.RepoKind.GIT);
		 * type.setSelectedItem(source.getKind()); repoPanel.add(type); type.addActionListener(new ActionListener() {
		 * public void actionPerformed(ActionEvent e) { source.setKind((DataSource.RepoKind) type.getSelectedItem());
		 * prefs.setChanged(true); panel.validate(); mainFrame.pack(); } });
		 */

		final JLabel validState = new JLabel();
		final boolean[] states = new boolean[3];
		
		// repoPanel.add(new JLabel("Short Name"));
		final JTextField shortName = new JTextField(copySource.getShortName());
		
		changedComponents.put(shortName, false);
		validText.put(shortName, true);
		states[0] = true;
		
		shortName.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {	
			}

			@Override
			public void keyReleased(KeyEvent e) {
				copySource.setShortName(shortName.getText());
				if (source != null) {
					changedComponents.put(shortName, 
							!shortName.getText().equals(source.getShortName()));
				}
				boolean valid = ValidInputChecker.checkDataSourceNameDuplicate(copyPref, copySource);
				validText.put(shortName, valid);
				states[0] = valid;
				validState.setText(getState(states));
				if (getState(states).equals("valid"))
					validState.setForeground(Color.GREEN.darker());
				else
					validState.setForeground(Color.RED.darker());
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}
			
		});

		// repoPanel.add(new JLabel("Hide?"));
		final JCheckBox hideBox = new JCheckBox();
		if (copySource.isHidden())
			hideBox.setSelected(true);

		changedComponents.put(hideBox, false);
		hideBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// hideBox.setSelected(!(hideBox.isSelected()));
				copySource.hide(hideBox.isSelected());
				if (source != null)
					changedComponents.put(hideBox, hideBox.isSelected() != source.isHidden());
				//prefs.setChanged(true);
			}
		});

		// repoPanel.add(new JLabel("Parent"));
		final JTextField parent = new JTextField(copySource.getParent());

		changedComponents.put(parent, false);
		validText.put(parent, true);
		states[1] = true;
		
		parent.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent arg0) {
				copySource.setParent(parent.getText());
				if (source != null)
					changedComponents.put(parent, !parent.getText().equals(source.getParent()));
				//prefs.setChanged(true);
				panel.validate();
				mainFrame.pack();
			}
		});

		// repoPanel.add(new JLabel("Clone Address"));
		final JTextField cloneAddress = new JTextField(copySource.getCloneString());
		final JPanel cloneAddressPanel = new JPanel();
		cloneAddressPanel.setLayout(new BoxLayout(cloneAddressPanel, BoxLayout.X_AXIS));
		cloneAddressPanel.add(cloneAddress);
		JButton findButton = new JButton("find");
		cloneAddressPanel.add(findButton);
		
		changedComponents.put(cloneAddress, false);
		boolean validAddress = ValidInputChecker.checkDirectoryPath(cloneAddress.getText())
								|| ValidInputChecker.checkUrl(cloneAddress.getText());
		validText.put(cloneAddress, validAddress);
		states[2] = validAddress;
		
		cloneAddress.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent arg0) {
				copySource.setCloneString(cloneAddress.getText());	
				if (source != null) {
					changedComponents.put(cloneAddress, 
							!cloneAddress.getText().equals(source.getCloneString()));
				}
				
				boolean valid = ValidInputChecker.checkDirectoryPath(cloneAddress.getText())
									|| ValidInputChecker.checkUrl(cloneAddress.getText());
				validText.put(cloneAddress, valid);
				states[2] = valid;
				validState.setText(getState(states));
				if (getState(states).equals("valid"))
					validState.setForeground(Color.GREEN.darker());
				else
					validState.setForeground(Color.RED.darker());
				//prefs.setChanged(true);
				panel.validate();
				mainFrame.pack();
			}
		});
		
		findButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new MyPathChooser("Path to clone address directory", cloneAddress, JFileChooser.DIRECTORIES_ONLY);
				//prefs.setChanged(true);
			}
			
		});

		final JButton deleteRepoButton = new JButton("Delete");
		deleteRepoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int n = JOptionPane.showConfirmDialog(null, 
						"Delete " + copySource.getShortName() + "'s repository?");
				if(n == JOptionPane.YES_OPTION){
					copyPref.removeDataSource(copySource);
					//prefs.setChanged(true);
					sourcesPanel.remove(shortName);
					sourcesPanel.remove(hideBox);
					sourcesPanel.remove(parent);
					sourcesPanel.remove(cloneAddressPanel);
					sourcesPanel.remove(deleteRepoButton);
					
					validText.remove(shortName);
					validText.remove(parent);
					validText.remove(cloneAddress);
					
					copyPrefs.setChanged(true);
					SpringLayoutUtility.formGridInColumn(sourcesPanel, copyPref.getDataSources().size() + BAR_SIZE, 
							SOURCES_COLUMNS);
					mainFrame.pack();
				}
			}
		});

		
		
		sourcesPanel.add(shortName);
		sourcesPanel.add(hideBox);
		sourcesPanel.add(parent);
		sourcesPanel.add(cloneAddressPanel);
		sourcesPanel.add(deleteRepoButton);
		
		validState.setText(getState(states));
		if (getState(states).equals("valid"))
			validState.setForeground(Color.GREEN.darker());
		else
			validState.setForeground(Color.RED.darker());
		sourcesPanel.add(validState);
	}

	/**
	 * Set correct state representation for input label
	 * @param label
	 * @param valid
	 */
	private void setState(JLabel label, boolean valid) {
		if (valid) {
			label.setText("  valid");
			label.setForeground(Color.GREEN.darker());
		} else {
			label.setText("invalid");
			label.setForeground(Color.RED.darker());
		}
	}
	
	/**
	 * Make a state representation from given boolean array
	 * @param states
	 * @return
	 */
	private String getState(boolean[] states) {
		String s = "";
		if (!states[0])
			s = "short name ";
		
		if (!states[1])
			s += "parent ";
		
		if (!states[2])
			s += "address ";
		
		if (s.isEmpty())
			s = "valid";
		else 
			s += ": invalid";
		
		return s;
	}
}