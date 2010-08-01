package crystal.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import crystal.model.DataSource;

/**
 * @author brun
 *
 */
public class ProjectPanel extends JPanel {
	
	private static final long serialVersionUID = 5244512987255240473L;
	private String _name;

	/**
	 * 
	 */
	public ProjectPanel(final ProjectPreferences pref, final ClientPreferences prefs, final JFrame mainFrame, final JTabbedPane tabbedPane) {
		super();
		
		final JPanel panel = this;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		_name = pref.getEnvironment().getShortName();

		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
		namePanel.add(new JLabel("Project Name: "));
		final JTextField shortName = new JTextField(pref.getEnvironment().getShortName());
		namePanel.add(shortName);
		shortName.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {				
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent arg0) {
				pref.getEnvironment().setShortName(shortName.getText());
				prefs.setChanged(true);
				_name = shortName.getText();
				tabbedPane.setTitleAt(tabbedPane.getSelectedIndex(), _name);
				panel.validate();
				mainFrame.pack();
			}
		});
		add(namePanel);
		
		JPanel typePanel = new JPanel();
		typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.X_AXIS));		
		typePanel.add(new JLabel("Repo Type: "));
		final JComboBox type = new JComboBox();
		type.addItem(DataSource.RepoKind.HG);
//		type.addItem(DataSource.RepoKind.GIT);
		type.setSelectedItem(pref.getEnvironment().getKind());
		typePanel.add(type);
		type.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pref.getEnvironment().setKind((DataSource.RepoKind) type.getSelectedItem());
				prefs.setChanged(true);
			}
		});
		add(typePanel);

		JPanel addressPanel = new JPanel();
		addressPanel.setLayout(new BoxLayout(addressPanel, BoxLayout.X_AXIS));		
		addressPanel.add(new JLabel("Clone Address: "));
		final JTextField address = new JTextField(pref.getEnvironment().getCloneString());
		addressPanel.add(address);
		address.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {				
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent arg0) {
				pref.getEnvironment().setCloneString(address.getText());
				prefs.setChanged(true);
				panel.validate();
				mainFrame.pack();
			}
		});
		add(addressPanel);
		
		final JButton newRepoButton = new JButton("Add New Repository");
		
		newRepoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				HashSet<String> shortNameLookup = new HashSet<String>();
				for (DataSource current : pref.getDataSources()) {
					shortNameLookup.add(current.getShortName());
				}
				int count = 1;
				while (shortNameLookup.contains("New Repo " + count++));
				
				DataSource newGuy = new DataSource("New Repo " + --count, "", DataSource.RepoKind.HG);
				pref.addDataSource(newGuy);
				add(repoPanel(newGuy, pref, prefs, panel, mainFrame));
				prefs.setChanged(true);
				panel.validate();
				mainFrame.pack();				
			}
		});
		add(newRepoButton);

		for (DataSource source : pref.getDataSources()) {
			add(repoPanel(source, pref, prefs, panel, mainFrame));			
		}
	}

	public String getName() {
		return _name;
	}

	private JPanel repoPanel(final DataSource source, final ProjectPreferences pref, final ClientPreferences prefs, final JPanel panel, final JFrame mainFrame) {
		final JPanel repoPanel = new JPanel();		
		repoPanel.setLayout(new BoxLayout(repoPanel, BoxLayout.X_AXIS));

		repoPanel.add(new JLabel("Repo Type"));
		final JComboBox type = new JComboBox();
		type.addItem(DataSource.RepoKind.HG);
//		type.addItem(DataSource.RepoKind.GIT);
		type.setSelectedItem(source.getKind());
		repoPanel.add(type);
		type.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				source.setKind((DataSource.RepoKind) type.getSelectedItem());
				prefs.setChanged(true);
				panel.validate();
				mainFrame.pack();
			}
		});

		repoPanel.add(new JLabel("Short Name"));
		final JTextField shortName = new JTextField(source.getShortName());
		repoPanel.add(shortName);
		shortName.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {				
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent arg0) {
				source.setShortName(shortName.getText());
				prefs.setChanged(true);
				panel.validate();
				mainFrame.pack();
			}
		});

		repoPanel.add(new JLabel("Clone Address"));
		final JTextField cloneAddress = new JTextField(source.getCloneString());
		repoPanel.add(cloneAddress);
		cloneAddress.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {				
			}

			public void keyTyped(KeyEvent arg0) {
			}

			public void keyReleased(KeyEvent arg0) {
				source.setCloneString(cloneAddress.getText());
				prefs.setChanged(true);
				panel.validate();
				mainFrame.pack();
			}
		});
		
		final JButton deleteRepoButton = new JButton("Delete");
		deleteRepoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pref.removeDataSource(source);
				prefs.setChanged(true);
				panel.remove(repoPanel);
				mainFrame.pack();
			}
		});
		repoPanel.add(deleteRepoButton);

		return repoPanel;
	}
}
