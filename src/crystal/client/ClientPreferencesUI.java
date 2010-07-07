package crystal.client;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import crystal.model.DataSource;
import crystal.model.DataSource.RepoKind;

/**
 * UI for managing preferences.
 * 
 * NOTE: currently this class doesn't work for managing the preferences from multiple projects; use the configuration
 * file instead.
 * 
 * @author rtholmes
 * 
 */
public class ClientPreferencesUI {

	/**
	 * Listener enabling the PreferenceUI to let a client know when it is updated and closed.
	 * 
	 * @author rtholmes
	 * 
	 */
	public interface IPreferencesListener {

		/**
		 * Fired when the preferences are updated.
		 * 
		 * @param preferences
		 */
		void preferencesChanged(ProjectPreferences preferences);

		/**
		 * Fired when the preferences dialog closes.
		 */
		void preferencesDialogClosed();
	}

	private JFrame _frame = null;

	private IPreferencesListener _listener;

	private ProjectPreferences _preferences;

	private ClientPreferencesUI() {
		// disallow
	}

	/**
	 * Main constructor.
	 * 
	 * @param prefs
	 *            The preferences to populate the UI with
	 * @param listener
	 *            The listener that should be notified when the UI changes
	 */
	public ClientPreferencesUI(ProjectPreferences prefs, IPreferencesListener listener) {
		_preferences = prefs;
		_listener = listener;
	}

	/**
	 * Build the UI elements.
	 * 
	 * This is unfortunately hand-crafted Swing. Not very pretty.
	 */
	private void buildUI() {
		_frame.getContentPane().removeAll();

		Container contentPane = _frame.getContentPane();

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		contentPane.setLayout(layout);

		JLabel myRepoLabel = new JLabel("My Repository:");
		c.gridx = 0;
		c.gridy = 0;
		contentPane.add(myRepoLabel, c);

		final JTextField myRepoText = new JTextField(30);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		contentPane.add(myRepoText, c);

		JLabel myRepoKindLabel = new JLabel("My Repository Kind:");
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 1;
		contentPane.add(myRepoKindLabel, c);

		String[] repoKinds = new String[] { RepoKind.HG.toString(), RepoKind.GIT.toString() };
		final JComboBox myRepoKind = new JComboBox(repoKinds);
		c.gridx = 1;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		contentPane.add(myRepoKind, c);

		JLabel myTempLabel = new JLabel("Temporary Directory:");
		c.gridx = 0;
		c.gridy = 2;
		contentPane.add(myTempLabel, c);

		final JTextField myTempText = new JTextField(30);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 2;
		contentPane.add(myTempText, c);

		JLabel myRepositoriesLabel = new JLabel("Repositories:");
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 3;
		contentPane.add(myRepositoriesLabel, c);

		final DefaultListModel repoListModel = new DefaultListModel();

		Vector<String> data = new Vector<String>();
		for (DataSource source : _preferences.getDataSources()) {
			repoListModel.addElement(source);
		}

		final JList myRepositoryList = new JList(repoListModel);
		myRepositoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 3;
		contentPane.add(myRepositoryList, c);

		final JButton myRemoveRepoButton = new JButton("Remove Repository");
		c.gridx = 1;
		c.gridy = 4;
		contentPane.add(myRemoveRepoButton, c);
		myRemoveRepoButton.setEnabled(false);

		JPanel addPanelA = new JPanel();
		JLabel myAddRepoLabel = new JLabel("New Repository");
		final JComboBox myAddRepoKind = new JComboBox(repoKinds);
		JLabel myAddCloneShortNameLabel = new JLabel("Short Name");
		final JTextField myAddCloneShortNameText = new JTextField(8);

		addPanelA.add(myAddRepoLabel);
		addPanelA.add(myAddRepoKind);
		addPanelA.add(myAddCloneShortNameLabel);
		addPanelA.add(myAddCloneShortNameText);

		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 5;
		contentPane.add(addPanelA, c);

		JPanel addPanelB = new JPanel();
		JLabel myAddCloneLabel = new JLabel("Address");
		final JTextField myAddCloneText = new JTextField(15);
		final JButton myAddRepoButton = new JButton("Add");

		addPanelB.add(myAddCloneLabel);
		addPanelB.add(myAddCloneText);
		addPanelB.add(myAddRepoButton);
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 6;
		contentPane.add(addPanelB, c);

		JButton myOkButton = new JButton("Ok");
		c.gridx = 0;
		c.gridy = 7;
		contentPane.add(myOkButton, c);

		myRepoText.setText(_preferences.getEnvironment().getCloneString());
		myTempText.setText(_preferences.getTempDirectory());
		if (_preferences.getEnvironment().getKind().equals(RepoKind.HG.toString())) {
			myRepoKind.setSelectedIndex(0);
		}
		if (_preferences.getEnvironment().getKind().equals(RepoKind.GIT.toString())) {
			myRepoKind.setSelectedIndex(1);
		}

		/**
		 * Enables / disables the remove repo button based on whether a selection is made.
		 */
		myRepositoryList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (myRepositoryList.getSelectedIndex() >= 0) {
					myRemoveRepoButton.setEnabled(true);
				} else {
					myRemoveRepoButton.setEnabled(false);
				}
			}
		});

		myRemoveRepoButton.setActionCommand("click");

		/**
		 * Removes any selected repositories when the remove button is pressed.
		 */
		myRemoveRepoButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("click")) {

					int indices[] = myRepositoryList.getSelectedIndices();

					for (int index : indices) {

						DataSource sourceToRemove = (DataSource) repoListModel.getElementAt(index);
						_preferences.removeDataSource(sourceToRemove);

						repoListModel.remove(index);
					}
					myRepositoryList.validate();
					_frame.pack();
				}
			}
		});

		myAddRepoButton.setEnabled(false);

		/**
		 * Enables the 'add repository' button when the required fields have valid values.
		 */
		myAddCloneText.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
				if (myAddCloneShortNameText.getText().length() > 0 && myAddCloneText.getText().length() > 0) {
					myAddRepoButton.setEnabled(true);
				} else {
					myAddRepoButton.setEnabled(false);
				}

			}

		});

		myAddRepoButton.setActionCommand("click");
		/**
		 * Adds the repositories to the preferences when the 'add repository' button is clicked.
		 */
		myAddRepoButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("click")) {
					// add repo clicked
					RepoKind kind = RepoKind.valueOf(myAddRepoKind.getSelectedItem().toString());
					String shortName = myAddCloneShortNameText.getText();
					String cloneName = myAddCloneText.getText();
					String label = shortName + "- " + kind + ": " + cloneName;

					if (_preferences.getDataSource(shortName) == null) {
						// don't allow an element with a taken name to be added

						DataSource source = new DataSource(shortName, cloneName, kind);

						repoListModel.addElement(source);
						myAddCloneShortNameText.setText("");
						myAddCloneText.setText("");
						_preferences.addDataSource(source);

						_frame.pack();
					}
				}

			}
		});

		/**
		 * Enables the 'add repository' button when the required fields are populated.
		 */
		myAddCloneShortNameText.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyTyped(KeyEvent e) {
				if (myAddCloneShortNameText.getText().length() > 0 && myAddCloneText.getText().length() > 0) {
					myAddRepoButton.setEnabled(true);
				} else {
					myAddRepoButton.setEnabled(false);
				}

			}

		});

		/**
		 * Updates the preferences when the 'ok' button is clicked and notifies the listener.
		 */
		myOkButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				_preferences.getEnvironment().setCloneString(myRepoText.getText());
				_preferences.getEnvironment().setKind(RepoKind.valueOf(myRepoKind.getSelectedItem().toString()));
				_preferences.setTempDirectory(myTempText.getText());

				_frame.setVisible(false);
				_listener.preferencesChanged(_preferences);
				_listener.preferencesDialogClosed();
			}
		});

		/**
		 * Fires when the window is closed; notifies the listener.
		 */
		_frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.out.println("ClientPreferencesUI::WidnowListener - window closing: " + we);
				_listener.preferencesDialogClosed();
			}
		});

		// Display the window.
		_frame.pack();
		_frame.setVisible(true);

	}

	/**
	 * Creates the UI and brings it to the foreground.
	 */
	public void createAndShowGUI() {
		// Create and set up the window.
		_frame = new JFrame("Conflict Client Preferences");

		buildUI();
	}
}