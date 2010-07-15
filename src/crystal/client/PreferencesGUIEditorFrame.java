/**
 * 
 */
package crystal.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.junit.Assert;

import crystal.model.DataSource;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author brun
 *
 */
public class PreferencesGUIEditorFrame extends JFrame {

	private static ProjectPreferences _prefs;
	
	public PreferencesGUIEditorFrame(ProjectPreferences prefs) {
		super("Crystal Configuration Editor");
		Assert.assertNotNull(prefs);
		_prefs = prefs;
		
		setLayout(new FlowLayout());
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		
		JPanel hgPanel = new JPanel();		
		hgPanel.setLayout(new FlowLayout());
		hgPanel.add(new JLabel("Path to hg executable:"), BorderLayout.CENTER);
		final JTextField hgPath = new JTextField(prefs.getClientPreferences().getHgPath());
		hgPanel.add(hgPath, BorderLayout.CENTER);
		hgPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_prefs.getClientPreferences().setHgPath(hgPath.getText());
			}	
		});
		getContentPane().add(hgPanel);
		
		JPanel tempPanel = new JPanel();		
		tempPanel.setLayout(new FlowLayout());
		tempPanel.add(new JLabel("Path to scratchspace:"), BorderLayout.CENTER);
		final JTextField tempPath = new JTextField(prefs.getClientPreferences().getTempDirectory());
		tempPanel.add(tempPath, BorderLayout.CENTER);
		tempPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_prefs.getClientPreferences().setTempDirectory(tempPath.getText());
			}	
		});
		getContentPane().add(tempPanel);
		
		getContentPane().add(repoPanel(_prefs.getEnvironment()));
		
		for (DataSource source : _prefs.getDataSources()) {
			getContentPane().add(repoPanel(source));			
		}
		
		
		pack();
		setVisible(true);
	}
	
	private JPanel repoPanel(final DataSource source) {
		JPanel repoPanel = new JPanel();		
		repoPanel.setLayout(new FlowLayout());

		repoPanel.add(new JLabel("Repo Type (only HG for now)"), BorderLayout.CENTER);
		final JComboBox type = new JComboBox();
		type.addItem(DataSource.RepoKind.HG);
		type.addItem(DataSource.RepoKind.GIT);
		type.setSelectedItem(source.getKind());
		repoPanel.add(type, BorderLayout.CENTER);
		type.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				source.setKind((DataSource.RepoKind) type.getSelectedItem());
			}
		});
		
		repoPanel.add(new JLabel("Short Name"), BorderLayout.CENTER);
		final JTextField shortName = new JTextField(source.getShortName());
		repoPanel.add(shortName, BorderLayout.CENTER);
		shortName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				source.setShortName(shortName.getText());
			}
		});
		
		repoPanel.add(new JLabel("Clone Address"), BorderLayout.CENTER);
		final JTextField cloneAddress = new JTextField(source.getCloneString());
		repoPanel.add(cloneAddress, BorderLayout.CENTER);
		cloneAddress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				source.setCloneString(cloneAddress.getText());
			}
		});

		return repoPanel;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ProjectPreferences mine = new ProjectPreferences(new DataSource("hello", "goodbye", DataSource.RepoKind.HG), new ClientPreferences("temp", "hgPath"));
		new PreferencesGUIEditorFrame(mine);

//		int i = 0;
//		while(true) {
//			i = (i + 1) % 1000000;
//			if (i == 0)
//				System.out.println(mine.getEnvironment().getShortName());
//		}
	}

}
