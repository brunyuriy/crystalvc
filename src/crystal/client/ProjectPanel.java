/**
 * 
 */
package crystal.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
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
	public ProjectPanel(final ProjectPreferences prefs) {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));		
		namePanel.add(new JLabel("Project Name: "));
		final JTextField shortName = new JTextField(prefs.getEnvironment().getShortName());
		namePanel.add(shortName);
		shortName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				prefs.getEnvironment().setShortName(shortName.getText());
				_name = shortName.getText();
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
		type.setSelectedItem(prefs.getEnvironment().getKind());
		typePanel.add(type);
		type.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				prefs.getEnvironment().setKind((DataSource.RepoKind) type.getSelectedItem());
			}
		});
		add(typePanel);

		JPanel addressPanel = new JPanel();
		addressPanel.setLayout(new BoxLayout(addressPanel, BoxLayout.X_AXIS));		
		addressPanel.add(new JLabel("Clone Address: "));
		final JTextField address = new JTextField(prefs.getEnvironment().getCloneString());
		addressPanel.add(address);
		address.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				prefs.getEnvironment().setCloneString(address.getText());
			}
		});
		add(addressPanel);

		for (DataSource source : prefs.getDataSources()) {
			add(repoPanel(source));			
		}
	}

	public String getName() {
		return _name;
	}

	private JPanel repoPanel(final DataSource source) {
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
			}
		});

		repoPanel.add(new JLabel("Short Name"));
		final JTextField shortName = new JTextField(source.getShortName());
		repoPanel.add(shortName);
		shortName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				source.setShortName(shortName.getText());
			}
		});

		repoPanel.add(new JLabel("Clone Address"));
		final JTextField cloneAddress = new JTextField(source.getCloneString());
		repoPanel.add(cloneAddress);
		cloneAddress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				source.setCloneString(cloneAddress.getText());
			}
		});

		return repoPanel;
	}
}
