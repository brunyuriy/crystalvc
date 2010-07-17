/**
 * 
 */
package crystal.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import crystal.model.DataSource;

/**
 * @author brun
 *
 */
public class ProjectPanel extends JPanel {
	
	private String _name;

	/**
	 * 
	 */
	public ProjectPanel(final ProjectPreferences pref, final JFrame mainFrame) {
		super();
		
		final JPanel panel = this;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));		
		namePanel.add(new JLabel("Project Name: "));
		final JTextField shortName = new JTextField(pref.getEnvironment().getShortName());
		namePanel.add(shortName);
		shortName.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {				
			}

			public void keyReleased(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
				pref.getEnvironment().setShortName(shortName.getText());
				_name = shortName.getText();
				panel.validate();
				mainFrame.pack();
			}
		});
		add(namePanel);
		_name = shortName.getText();

		JPanel typePanel = new JPanel();
		typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.X_AXIS));		
		typePanel.add(new JLabel("Repo Type: "));
		final JComboBox type = new JComboBox();
		type.addItem(DataSource.RepoKind.HG);
		type.addItem(DataSource.RepoKind.GIT);
		type.setSelectedItem(pref.getEnvironment().getKind());
		typePanel.add(type);
		type.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pref.getEnvironment().setKind((DataSource.RepoKind) type.getSelectedItem());
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

			public void keyReleased(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
				pref.getEnvironment().setCloneString(address.getText());
				panel.validate();
				mainFrame.pack();
			}
		});
		add(addressPanel);
		
		final JButton newRepoButton = new JButton("Add New Repository");
		
		newRepoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DataSource newGuy = new DataSource("", "", DataSource.RepoKind.HG);
				pref.addDataSource(newGuy);
				add(repoPanel(newGuy, panel, mainFrame));
				panel.validate();
				mainFrame.pack();
			}
		});
		add(newRepoButton);

		for (DataSource source : pref.getDataSources()) {
			add(repoPanel(source, panel, mainFrame));			
		}
	}

	public String getName() {
		return _name;
	}

	private JPanel repoPanel(final DataSource source, final JPanel panel, final JFrame mainFrame) {
		JPanel repoPanel = new JPanel();		
		repoPanel.setLayout(new BoxLayout(repoPanel, BoxLayout.X_AXIS));

		repoPanel.add(new JLabel("Repo Type"));
		final JComboBox type = new JComboBox();
		type.addItem(DataSource.RepoKind.HG);
		type.addItem(DataSource.RepoKind.GIT);
		type.setSelectedItem(source.getKind());
		repoPanel.add(type);
		type.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				source.setKind((DataSource.RepoKind) type.getSelectedItem());
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

			public void keyReleased(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
				source.setShortName(shortName.getText());
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

			public void keyReleased(KeyEvent arg0) {
			}

			public void keyTyped(KeyEvent arg0) {
				source.setCloneString(cloneAddress.getText());
				panel.validate();
				mainFrame.pack();
			}
		});

		return repoPanel;
	}
}
