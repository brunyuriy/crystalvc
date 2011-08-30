package crystal.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileNotFoundException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import org.apache.log4j.Logger;

import crystal.Constants;
import crystal.model.DataSource;
import crystal.util.SpringLayoutUtility;
import crystal.util.ValidInputChecker;

/**
 * Class DataSourceGuiEditorFrame allows adding or editing new repository to a project
 * @author Haochen
 *
 */
public class DataSourceGuiEditorFrame extends JFrame {
	
	private static int SOURCE_COLUMNS = 2;

	private static final long serialVersionUID = 7756018720377051986L;

	private Logger _log = Logger.getLogger(this.getClass());

	
	/**
	 * Add a new dataSource to given projectPreference.
	 * @param prefs client preferences containing the project preferences.
	 * @param pref project preference containing the data source
	 */
	public DataSourceGuiEditorFrame(ClientPreferences prefs, ProjectPreferences pref){
		this(prefs, pref, null);
	}
	
	/**
	 * Edit data source.
	 * @param prefs client preferences containing the project preferences.
	 * @param pref project preference containing the data source
	 * @param sourceName data source name to edit: create new data source if it is null
	 */
	public DataSourceGuiEditorFrame(final ClientPreferences prefs, final ProjectPreferences pref, String sourceName){
		//ValidInputChecker.checkNullInput(prefs);
		/*if(prefs.getProjectPreference().contains(pref)){
			throw new IllegalArgumentException("Input project preference is not contained in the " +
					"client preferences.");
		}
		*/
		final boolean isAddRepo = sourceName == null;
		final DataSource _source;
		
		if(isAddRepo){
			_source = new DataSource("", "", null, false, "");
			setTitle("Crystal add new repository");
		} else if (pref.getDataSource(sourceName) != null){
			_source = pref.getDataSource(sourceName);
			setTitle("Crystal Reopsitory Editor");
		} else {
			throw new IllegalArgumentException("Illegal source name not contained in " +
					"projectPreference.");
		}


		setIconImage((new ImageIcon(Constants.class.getResource("/crystal/client/images/crystal-ball_blue_128.jpg"))).getImage());
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		
		
		JPanel sourcePanel = new JPanel(new SpringLayout());
		
		sourcePanel.add(new JLabel("Short name: "));
		final JTextField shortName = new JTextField(_source.getShortName());
		sourcePanel.add(shortName);
		shortName.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent arg0) {
				try {
					_source.setShortName(shortName.getText());
					//prefs.setChanged(true);
				} catch (Exception e){
					/*JOptionPane.showMessageDialog(null, "Invalid input for project name.", 
							"Warning", JOptionPane.ERROR_MESSAGE);
					shortName.setText(_source.getShortName());
					*/
				}
				
			}
			
		});
		
		sourcePanel.add(new JLabel("Clone address: "));
		final JPanel findAddressPanel = new JPanel(new SpringLayout());
		final JTextField cloneString = new JTextField(_source.getCloneString());
		findAddressPanel.add(cloneString);

		cloneString.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent arg0) {
				try {
					_source.setCloneString(cloneString.getText());
					//prefs.setChanged(true);
				} catch (Exception e){
					/*JOptionPane.showMessageDialog(null, "Invalid input for clone address.", 
							"Warning", JOptionPane.ERROR_MESSAGE);
					cloneString.setText(_source.getCloneString());
					*/
				}
			}
		});
		
		JButton findButton = new JButton("Find");
		findAddressPanel.add(findButton);
		findButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				new DirectoryChooser(cloneString, _source);
				//prefs.setChanged(true);
			}
			
		});
		findAddressPanel.add(findButton);
		SpringLayoutUtility.formGridInColumn(findAddressPanel, 1, 2);

		sourcePanel.add(findAddressPanel);
		
		sourcePanel.add(new JLabel("Hidden? "));
		JPanel boxBar = new JPanel(new BorderLayout());
		final JCheckBox hideBox = new JCheckBox();
		boxBar.add(hideBox, BorderLayout.WEST);
		hideBox.setSelected(_source.isHidden());
		
		sourcePanel.add(boxBar);
		
		hideBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_source.hide(hideBox.isSelected());
				//prefs.setChanged(true);
			}
		});
		
		
		sourcePanel.add(new JLabel("Parent: "));
		final JTextField parent = new JTextField(_source.getParent());
		sourcePanel.add(parent);
		parent.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				
			}

			public void focusLost(FocusEvent arg0) {
				try {
					_source.setParent(parent.getText());
					//prefs.setChanged(true);
				} catch (Exception e){
					/*JOptionPane.showMessageDialog(null, "Invalid input for parent.", 
							"Warning", JOptionPane.ERROR_MESSAGE);
					parent.setText(_source.getParent());
					*/
				}
			}
		});
		
		SpringLayoutUtility.formGridInColumn(sourcePanel, 
				sourcePanel.getComponents().length / SOURCE_COLUMNS, SOURCE_COLUMNS);
		
		add(sourcePanel);
		
		JPanel savePanel = new JPanel(new FlowLayout());
		
		
		if(isAddRepo){	// add new repository: use add button
			JButton addButton = new JButton("Add");
			savePanel.add(addButton);
			addButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent event) {
					try {
						ValidInputChecker.checkValidStringInput(_source.getShortName());
						pref.addDataSource(_source);
						//System.out.println(_source.getCloneString());
						//TODO save to xml file
						//prefs.setChanged(false);
						ClientPreferences.savePreferencesToDefaultXML(prefs);
						setVisible(false);
					} catch (Exception e){
						JOptionPane.showMessageDialog(null, 
								"Your short name for the new data source is invalid. \n" +
								"Please change to another name.", 
								"Warning", JOptionPane.ERROR_MESSAGE);
					}
				}
				
			});
		} else {	// edit repository: use save button
			JButton saveButton = new JButton("Save");
			savePanel.add(saveButton);
			saveButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					if(pref.getDataSources().contains(_source.getShortName())){	// new name is a duplicate
						JOptionPane.showMessageDialog(null, 
								"Short name already exists in the project repository.", 
								"Warning", JOptionPane.ERROR_MESSAGE);
					} else {
						try {
                            ClientPreferences.savePreferencesToDefaultXML(prefs);
                        } catch (FileNotFoundException fnfe) {
                            _log.error("Could not write to the configuration file. " + fnfe);
                        }
						setVisible(false);
					}
				}
				
			});
		}
		
		JButton cancelButton = new JButton("Cancel");
		savePanel.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//prefs.setChanged(false);
				setVisible(false);
			}
			
		});
		
		add(savePanel);
		
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		

		addWindowListener(new WindowListener() {
			public void windowClosing(WindowEvent arg0) {

				setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);	
				if (prefs.hasChanged()) {
					
					int n = JOptionPane.showConfirmDialog(null, "Do you want to save your data?", 
							"Saving data", JOptionPane.YES_NO_CANCEL_OPTION);
					
					if (n == JOptionPane.YES_OPTION) {

						if(pref.getDataSources().contains(_source)){	// new name is a duplicate
							JOptionPane.showMessageDialog(null, 
									"Short name already exists in the project repository.\n" +
									"Please change your short name for the repository.", 
									"Warning", JOptionPane.ERROR_MESSAGE);
							setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
						} else {
							//TODO save to xml file.
							//prefs.setChanged(false);
							try {
                                ClientPreferences.savePreferencesToDefaultXML(prefs);
	                        } catch (FileNotFoundException fnfe) {
	                            _log.error("Could not write to the configuration file. " + fnfe);
	                        }
						}
					} else if(n == JOptionPane.CANCEL_OPTION) {
						setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					} else {	// option is no
						//prefs.setChanged(false);
					}
				}
			}

			public void windowActivated(WindowEvent arg0) {
			}

			public void windowClosed(WindowEvent arg0) {
			}

			public void windowDeactivated(WindowEvent arg0) {
			}

			public void windowDeiconified(WindowEvent arg0) {
			}

			public void windowIconified(WindowEvent arg0) {
			}

			public void windowOpened(WindowEvent arg0) {
			}
		});
		

		
		setVisible(true);
		setSize(500, 180);
	}
	

	
	/**
	 * A directory chooser to select paths
	 * @author Haochen
	 *
	 */
	private class DirectoryChooser extends JFrame{
		/**
		 * 
		 */
		private static final long serialVersionUID = 8710734018181001535L;

		DirectoryChooser(final JTextField path, final DataSource source){
			super("Search path to clone repository");
			
			final JFrame chooserFrame = this;
			
			setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
			
			final JFileChooser chooser = new JFileChooser(path.getText());
			
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			getContentPane().add(chooser);
			chooser.setFileHidingEnabled(true);
			pack();
			setVisible(true);
			chooser.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String command = e.getActionCommand();
					if (command.equals(JFileChooser.APPROVE_SELECTION)) {
						path.setText(chooser.getSelectedFile().getAbsolutePath().replace('\\', '/'));			
						source.setCloneString(path.getText());
					}
					chooserFrame.setVisible(false);
				}
			});
			
		}
		
	}
}
