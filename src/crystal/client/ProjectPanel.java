package crystal.client;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;

import crystal.model.DataSource;

/**
 * A panel used for the configuration editor (PreferencesGUIEditorFrame) to display a single project. 
 * 
 * @author brun
 */
public class ProjectPanel extends JPanel {

	private static final long serialVersionUID = 5244512987255240473L;
	private static final int SOURCES_COLUMNS = 5;
	private static final int ENVIRON_COLUMNS = 2;
	private static final int BAR_SIZE = 1;
	
	
	// The name of the project
	private String _name;

	/**
	 * Creates a new panel, with the pref Project configuration.
	 * @param: pref : the project configuration to display
	 * @param: prefs: the overall configuration associated with this project
	 * @param mainFrame: the frame that will keep this panel
	 * @param tabbedPane: the pane on the mainFrame that will keep this panel
	 */
	public ProjectPanel(final ProjectPreferences pref, final ClientPreferences prefs, 
			final JFrame mainFrame, final JTabbedPane tabbedPane) {
		super();

		final JPanel panel = this;

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		_name = pref.getEnvironment().getShortName();
		
		JPanel prefEnvironmentPanel = new JPanel(new SpringLayout());

		prefEnvironmentPanel.add(new JLabel("Project Name: "));
		final JTextField shortName = new JTextField(pref.getEnvironment().getShortName());
		prefEnvironmentPanel.add(shortName);
		shortName.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				try {
					pref.getEnvironment().setShortName(shortName.getText());
					prefs.setChanged(true);
					_name = shortName.getText();
					tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), _name);
					panel.validate();
					mainFrame.pack();
				} catch (Exception e){
					JOptionPane.showMessageDialog(null, "Invalid input for project name.", 
							"Warning", JOptionPane.ERROR_MESSAGE);
					shortName.setText(pref.getEnvironment().getShortName());
				}
				
			}
		});

		prefEnvironmentPanel.add(new JLabel("Parent Name (optional): "));
		final JTextField parentName = new JTextField(pref.getEnvironment().getParent());
		if (parentName.getText().equals(""))
			parentName.setText(" ");
		prefEnvironmentPanel.add(parentName);
		parentName.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent arg0) {
				pref.getEnvironment().setParent(parentName.getText());
				prefs.setChanged(true);
				if (parentName.getText().equals(""))
					parentName.setText(" ");
				panel.validate();
				mainFrame.pack();
			}
		});


		prefEnvironmentPanel.add(new JLabel("Repo Type: "));
		final JComboBox type = new JComboBox();
		type.addItem(DataSource.RepoKind.HG);
		// type.addItem(DataSource.RepoKind.GIT);
		type.setSelectedItem(pref.getEnvironment().getKind());
		prefEnvironmentPanel.add(type);
		type.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pref.getEnvironment().setKind((DataSource.RepoKind) type.getSelectedItem());
				prefs.setChanged(true);
			}
		});

		prefEnvironmentPanel.add(new JLabel("Clone Address: "));
		final JTextField address = new JTextField(pref.getEnvironment().getCloneString());
		prefEnvironmentPanel.add(address);
		address.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				try {
					pref.getEnvironment().setCloneString(address.getText());
					prefs.setChanged(true);
					panel.validate();
					mainFrame.pack();
				} catch (Exception e){
					JOptionPane.showMessageDialog(null, "Invalid input for clone address.", 
							"Warning", JOptionPane.ERROR_MESSAGE);
					address.setText(pref.getEnvironment().getCloneString());
				}
				
			}
		});
		
		prefEnvironmentPanel.add(new JLabel("Compile Command: "));
		String compileCommand = pref.getEnvironment().getCompileCommand();
		if(compileCommand == null || compileCommand.trim().equals("")){
			compileCommand = "Please write compile command here.";
		}
		final JTextField compile = new JTextField(compileCommand);
		prefEnvironmentPanel.add(compile);
		compile.addFocusListener(new FocusListener() {
			
			@Override
			public void focusGained(FocusEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				try {
					pref.getEnvironment().setCompileCommand(compile.getText());
					prefs.setChanged(true);
					panel.validate();
					mainFrame.pack();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Invalid input for compile command.", 
							"Warning", JOptionPane.ERROR_MESSAGE);
					if (pref.getEnvironment().getCompileCommand() != null 
							&& !pref.getEnvironment().getCompileCommand().trim().equals(""))
						compile.setText(pref.getEnvironment().getCompileCommand());
					else
						compile.setText("Please write compile command here.");
				}
				
			}
		});
		
		prefEnvironmentPanel.add(new JLabel("Test Command: "));
		String testCommand = pref.getEnvironment().getTestCommand();
		if(testCommand == null || testCommand.trim().equals("")){
			testCommand = "Please write test command here.";
		}
		final JTextField test = new JTextField(testCommand);
		prefEnvironmentPanel.add(test);
		test.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				try {
					pref.getEnvironment().setTestCommand(test.getText());
					prefs.setChanged(true);
					panel.validate();
					mainFrame.pack();
				} catch (Exception e){
					JOptionPane.showMessageDialog(null, "Invalid input for test command.", 
							"Warning", JOptionPane.ERROR_MESSAGE);
					if (pref.getEnvironment().getTestCommand() != null 
							&& !pref.getEnvironment().getTestCommand().trim().equals(""))
						test.setText(pref.getEnvironment().getTestCommand());
					else
						test.setText("lease write test command here.");
				}
				
			}
		});

		formGrid(prefEnvironmentPanel, 
				prefEnvironmentPanel.getComponents().length / ENVIRON_COLUMNS, ENVIRON_COLUMNS);
		
		panel.add(prefEnvironmentPanel);

		final JButton newRepoButton = new JButton("Add New Repository");

		final JPanel sourcesPanel = new JPanel(new SpringLayout());
		
		newRepoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				HashSet<String> shortNameLookup = new HashSet<String>();
				for (DataSource current : pref.getDataSources()) {
					shortNameLookup.add(current.getShortName());
				}
				int count = 1;
				while (shortNameLookup.contains("New Repo " + count++))
					;

				DataSource newGuy = new DataSource("New Repo " + --count, "", 
						DataSource.RepoKind.HG, false, null);
				pref.addDataSource(newGuy);
				addRepoPanel(newGuy, pref, prefs, panel, mainFrame, sourcesPanel);
				formGrid(sourcesPanel, pref.getDataSources().size() + BAR_SIZE, SOURCES_COLUMNS);
				prefs.setChanged(true);
				panel.validate();
				mainFrame.pack();
			}
		});
		add(newRepoButton);


		JLabel pShortName = new JLabel("Short Name", JLabel.CENTER);
		JLabel pHide = new JLabel("Hide?", JLabel.CENTER);
		JLabel pParent = new JLabel("Parent", JLabel.CENTER);
		JLabel pClone = new JLabel("Clone Address", JLabel.CENTER);
		JLabel pDelete = new JLabel("", JLabel.CENTER);
		
		sourcesPanel.add(pShortName);
		sourcesPanel.add(pHide);
		sourcesPanel.add(pParent);
		sourcesPanel.add(pClone);
		sourcesPanel.add(pDelete);
		
		for (DataSource source : pref.getDataSources()) {
			addRepoPanel(source, pref, prefs, panel, mainFrame, sourcesPanel);
		}

		formGrid(sourcesPanel, pref.getDataSources().size() + BAR_SIZE, SOURCES_COLUMNS);
		
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
	private void addRepoPanel(final DataSource source, final ProjectPreferences pref, 
			final ClientPreferences prefs, final JPanel panel,
			final JFrame mainFrame, final JPanel sourcesPanel) {
		
		//repoPanel.setLayout(new BoxLayout(repoPanel, BoxLayout.X_AXIS));

		/*
		 * repoPanel.add(new JLabel("Repo Type")); final JComboBox type = new JComboBox();
		 * type.addItem(DataSource.RepoKind.HG); // type.addItem(DataSource.RepoKind.GIT);
		 * type.setSelectedItem(source.getKind()); repoPanel.add(type); type.addActionListener(new ActionListener() {
		 * public void actionPerformed(ActionEvent e) { source.setKind((DataSource.RepoKind) type.getSelectedItem());
		 * prefs.setChanged(true); panel.validate(); mainFrame.pack(); } });
		 */

		// repoPanel.add(new JLabel("Short Name"));
		final JTextField shortName = new JTextField(source.getShortName());
		
		shortName.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void focusLost(FocusEvent arg) {
				try {
					source.setShortName(shortName.getText());
					prefs.setChanged(true);
					panel.validate();
					mainFrame.pack();
				} catch (Exception e){
					JOptionPane.showMessageDialog(null, "Invalid input for short name.", 
							"Warning", JOptionPane.ERROR_MESSAGE);
					shortName.setText(source.getShortName());
				}
				
			}
		});

		// repoPanel.add(new JLabel("Hide?"));
		final JCheckBox hideBox = new JCheckBox();
		if (source.isHidden())
			hideBox.setSelected(true);

		hideBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// hideBox.setSelected(!(hideBox.isSelected()));
				source.hide(hideBox.isSelected());
				prefs.setChanged(true);
			}
		});

		// repoPanel.add(new JLabel("Parent"));
		final JTextField parent = new JTextField(source.getParent());
		if (parent.getText().equals(""))
			parent.setText(" ");

		parent.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent arg0) {
				source.setParent(parent.getText());
				prefs.setChanged(true);
				if (parent.getText().equals(""))
					parent.setText(" ");
				panel.validate();
				mainFrame.pack();
			}
		});

		// repoPanel.add(new JLabel("Clone Address"));
		final JTextField cloneAddress = new JTextField(source.getCloneString());

		cloneAddress.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				try{
					source.setCloneString(cloneAddress.getText());	
					prefs.setChanged(true);
					panel.validate();
					mainFrame.pack();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Invalid path for clone address.", 
							"Warning", JOptionPane.ERROR_MESSAGE);
					cloneAddress.setText(source.getCloneString());
				}
				
			}
		});

		final JButton deleteRepoButton = new JButton("Delete");
		deleteRepoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int n = JOptionPane.showConfirmDialog(null, 
						"Delete " + source.getShortName() + "'s repository?");
				if(n == JOptionPane.YES_OPTION){
					pref.removeDataSource(source);
					prefs.setChanged(true);
					sourcesPanel.remove(shortName);
					sourcesPanel.remove(hideBox);
					sourcesPanel.remove(parent);
					sourcesPanel.remove(cloneAddress);
					sourcesPanel.remove(deleteRepoButton);
					formGrid(sourcesPanel, pref.getDataSources().size() + BAR_SIZE, 
							SOURCES_COLUMNS);
					mainFrame.pack();
				}
			}
		});

		sourcesPanel.add(shortName);
		sourcesPanel.add(hideBox);
		sourcesPanel.add(parent);
		sourcesPanel.add(cloneAddress);
		
		sourcesPanel.add(deleteRepoButton);

	}
	
	  /* Used by makeCompactGrid. */
	private SpringLayout.Constraints getConstraintsForCell(int row, int col, 
			int totalCols, JPanel panel) {
		SpringLayout layout = (SpringLayout) panel.getLayout();
		Component c = panel.getComponent(row * totalCols + col);
		return layout.getConstraints(c);
	}
	
	/**
	 * Form panel of sources to grid.
	 * 
	 * @param sourcesPanel panel to reform
	 * @param rows number of rows in the panel
	 * @param cols number of columns in the panel
	 */
	private void formGrid(JPanel panel, int rows, int cols) {
		SpringLayout layout = (SpringLayout) panel.getLayout();

		// Align all cells in each column and make them the same width.
		Spring x = Spring.constant(3);
		for (int col = 0; col < cols; col++) {
			Spring width = Spring.constant(0);
			for (int row = 0; row < rows; row++) {
				width = Spring.max(width, getConstraintsForCell(row, col, cols, panel).getWidth());
			}
			for (int row = 0; row < rows; row++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(row, col, cols, panel);
				constraints.setX(x);
				constraints.setWidth(width);
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(3)));
		}

		// Align all cells in each row and make them the same height.
		Spring y = Spring.constant(3);
		for (int row = 0; row < rows; row++) {
			Spring height = Spring.constant(0);
			for (int col = 0; col < cols; col++) {
				height = Spring.max(height, getConstraintsForCell(row, col, cols, panel).getHeight());
			}
			for (int col = 0; col < cols; col++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(row, col, cols, panel);
				constraints.setY(y);
				constraints.setHeight(height);
			}
			y = Spring.sum(y, Spring.sum(height, Spring.constant(3)));
		}

		// Set panel's size
		SpringLayout.Constraints pCons = layout.getConstraints(panel);
		pCons.setConstraint(SpringLayout.SOUTH, y);
		pCons.setConstraint(SpringLayout.EAST, x);
	}
}