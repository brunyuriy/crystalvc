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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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
		
		setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		
		JPanel hgPanel = new JPanel();
		hgPanel.setLayout(new BoxLayout(hgPanel, BoxLayout.X_AXIS));
		hgPanel.add(new JLabel("Path to hg executable:"));
		final JTextField hgPath = new JTextField(prefs.getClientPreferences().getHgPath());
		hgPanel.add(hgPath);
		hgPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_prefs.getClientPreferences().setHgPath(hgPath.getText());
			}	
		});
		JButton hgButton = new JButton("find");
		hgPanel.add(hgButton);
		hgButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new MyPathChooser("Path to hg executable", hgPath, JFileChooser.FILES_ONLY);
			}
		});
		
		getContentPane().add(hgPanel);
		
		JPanel tempPanel = new JPanel();		
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		tempPanel.add(new JLabel("Path to scratchspace:"));
		final JTextField tempPath = new JTextField(prefs.getClientPreferences().getTempDirectory());
		tempPanel.add(tempPath);
		tempPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_prefs.getClientPreferences().setTempDirectory(tempPath.getText());
			}
		});
		JButton tempButton = new JButton("find");
		tempPanel.add(tempButton);
		tempButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new MyPathChooser("Path to scratch directory", tempPath, JFileChooser.DIRECTORIES_ONLY);
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
		repoPanel.setLayout(new BoxLayout(repoPanel, BoxLayout.X_AXIS));

		repoPanel.add(new JLabel("Repo Type"));
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
		
		repoPanel.add(new JLabel("Short Name"));
		final JTextField shortName = new JTextField(source.getShortName());
		repoPanel.add(shortName, BorderLayout.CENTER);
		shortName.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				source.setShortName(shortName.getText());
			}
		});
		
		repoPanel.add(new JLabel("Clone Address"));
		final JTextField cloneAddress = new JTextField(source.getCloneString());
		repoPanel.add(cloneAddress, BorderLayout.CENTER);
		cloneAddress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				source.setCloneString(cloneAddress.getText());
			}
		});

		return repoPanel;
	}
	
	private static class MyPathChooser extends JFrame {
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
					chooserFrame.setVisible(false);
				}	
			});
		}
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
