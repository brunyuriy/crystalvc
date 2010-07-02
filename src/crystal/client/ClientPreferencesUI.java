package crystal.client;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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

public class ClientPreferencesUI {
	private ClientPreferences _preferences;
	JFrame _frame = null;

	public void createAndShowGUI(ClientPreferences prefs) {
		_preferences = prefs;

		// Create and set up the window.
		_frame = new JFrame("Conflict Client Preferences");

		buildUI(prefs);

	}

	private void buildUI(final ClientPreferences prefs) {
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
		c.gridx = 1;
		c.gridy = 2;
		contentPane.add(myTempText, c);

		JLabel myRepositoriesLabel = new JLabel("Repositories:");
		c.gridx = 0;
		c.gridy = 3;
		contentPane.add(myRepositoriesLabel, c);

		final DefaultListModel repoListModel = new DefaultListModel();

		Vector<String> data = new Vector<String>();
		for (DataSource source : prefs.getDataSources()) {
			// data.add();
			String label = source.getShortName() + "- " + source.getKind() + ": " + source.getCloneString();
			repoListModel.addElement(label);
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

		myRepoText.setText(prefs.getEnvironment().getCloneString());
		myTempText.setText(prefs.getTempDirectory());
		if (prefs.getEnvironment().getKind().equals(RepoKind.HG.toString())) {
			myRepoKind.setSelectedIndex(0);
		}
		if (prefs.getEnvironment().getKind().equals(RepoKind.GIT.toString())) {
			myRepoKind.setSelectedIndex(1);
		}

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

		myRemoveRepoButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("click")) {

					int indices[] = myRepositoryList.getSelectedIndices();

					for (int index : indices) {

						// remove from model
						// XXX: this is now broken
						// DataSource removed = prefs.getDataSources().remove(index);
						// System.out.println("removed index: " + index + " ds: " + removed + " model: " +
						// repoListModel.get(index));

						// remove from list
						repoListModel.remove(index);
					}
					myRepositoryList.validate();
					_frame.pack();
				}
			}
		});

		myAddRepoButton.setEnabled(false);

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
		myAddRepoButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("click")) {
					// add repo clicked
					RepoKind kind = RepoKind.valueOf(myAddRepoKind.getSelectedItem().toString());
					String shortName = myAddCloneShortNameText.getText();
					String cloneName = myAddCloneText.getText();
					String label = shortName + "- " + kind + ": " + cloneName;
					repoListModel.addElement(label);
					myAddCloneShortNameText.setText("");
					myAddCloneText.setText("");

					DataSource source = new DataSource(shortName, cloneName, kind);
					prefs.getDataSources().add(source);
					_frame.pack();
				}

			}
		});

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

		myOkButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				prefs.getEnvironment().setCloneString(myRepoText.getText());
				prefs.getEnvironment().setKind(RepoKind.valueOf(myRepoKind.getSelectedItem().toString()));
				prefs.setTempDirectory(myTempText.getText());

				_frame.setVisible(false);
			}
		});
		// Display the window.
		_frame.pack();
		_frame.setVisible(true);

	}
}