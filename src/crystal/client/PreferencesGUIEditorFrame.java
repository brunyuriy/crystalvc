/**
 * 
 */
package crystal.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Vector;

import org.junit.Assert;

import crystal.model.DataSource;
import crystal.model.DataSource.RepoKind;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

/**
 * @author brun
 *
 */
public class PreferencesGUIEditorFrame extends JFrame {

	public PreferencesGUIEditorFrame(final List<ProjectPreferences> prefs) {
		super("Crystal Configuration Editor");
		Assert.assertNotNull(prefs);

		final JFrame frame = this;

		if (prefs.isEmpty()) {
			ClientPreferences client = new ClientPreferences("/usr/bin/hg", "/tmp/crystalClient");
			ProjectPreferences newGuy = new ProjectPreferences(new DataSource("", "", DataSource.RepoKind.HG), client); 
			prefs.add(newGuy);
		}

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE); 

		JPanel hgPanel = new JPanel();
		hgPanel.setLayout(new BoxLayout(hgPanel, BoxLayout.X_AXIS));
		hgPanel.add(new JLabel("Path to hg executable:"));
		final JTextField hgPath = new JTextField(prefs.get(0).getClientPreferences().getHgPath());
		hgPanel.add(hgPath);
		hgPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (ProjectPreferences pref : prefs)
					pref.getClientPreferences().setHgPath(hgPath.getText());
				frame.pack();
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
		final JTextField tempPath = new JTextField(prefs.get(0).getClientPreferences().getTempDirectory());
		tempPanel.add(tempPath);
		tempPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (ProjectPreferences pref : prefs)
					pref.getClientPreferences().setTempDirectory(tempPath.getText());
				frame.pack();
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


		final JTabbedPane projectsTabs = new JTabbedPane(JTabbedPane.TOP);
		for (ProjectPreferences pref : prefs) {
			ProjectPanel current = new ProjectPanel(pref);
			projectsTabs.addTab(current.getName(), current);
			//			getContentPane().add(current);
		}

		final JButton newProjectButton = new JButton("Add New Project");
		final JButton deleteProjectButton = new JButton("Delete This Project");

		newProjectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ClientPreferences client ;
				if (prefs.isEmpty()) {
					client = new ClientPreferences("/usr/bin/hg", "/tmp/crystalClient");
					deleteProjectButton.setEnabled(true);
				} else
					client = prefs.get(0).getClientPreferences();	
				ProjectPreferences newGuy = new ProjectPreferences(new DataSource("", "", DataSource.RepoKind.HG), client); 
				prefs.add(newGuy);
				ProjectPanel newGuyPanel = new ProjectPanel(newGuy);
				projectsTabs.addTab("New Project", newGuyPanel);
				frame.pack();
			}
		});

		deleteProjectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int current = projectsTabs.getSelectedIndex();
				prefs.remove(current);
				projectsTabs.remove(current);
				if (prefs.isEmpty())
					deleteProjectButton.setEnabled(false);
				frame.pack();
			}
		});

		getContentPane().add(newProjectButton);
		getContentPane().add(projectsTabs);
		getContentPane().add(deleteProjectButton);


		pack();
		setVisible(true);
		
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
		ClientPreferences client = new ClientPreferences("temp", "hgPath");
		ProjectPreferences one = new ProjectPreferences(new DataSource("first project", "~brun\\firstrepo", DataSource.RepoKind.HG), client); 
		DataSource oneOther = new DataSource("Mike's copy", "~mernst\\repo", DataSource.RepoKind.HG);
		DataSource twoOther = new DataSource("Reid's copy", "~rtholmes\\repo", DataSource.RepoKind.HG);
		DataSource threeOther = new DataSource("David's copy", "~notkin\\repo", DataSource.RepoKind.HG);
		one.addDataSource(oneOther);
		one.addDataSource(twoOther);

		ProjectPreferences two = new ProjectPreferences(new DataSource("second project", "~brun\\secondrepo", DataSource.RepoKind.HG), client); 
		two.addDataSource(threeOther);
		two.addDataSource(oneOther);

		Vector<ProjectPreferences> vec = new Vector<ProjectPreferences>();
		vec.add(one);
		vec.add(two);
		new PreferencesGUIEditorFrame(vec);

		//		int i = 0;
		//		while(true) {
		//			i = (i + 1) % 1000000;
		//			if (i == 0)
		//				System.out.println(mine.getEnvironment().getShortName());
		//		}
	}

}
