package crystal.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import crystal.Constants;
import crystal.model.DataSource;
import crystal.model.DataSource.RepoKind;
import crystal.util.XMLTools;

/**
 * Maintains multiple sets of preferences, rather than just one.
 * 
 * @author rtholmes & brun
 * 
 */
public class ClientPreferences {

	private interface IPrefXML {

		static final String[] ROOT = { "ccConfig", "ccconfig", "CcConfig", "CCConfig" };

		static final String[] TMP_DIR = { "tempDirectory", "TempDirectory" };
		static final String[] HG_PATH = { "hgPath", "HgPath", "HGPath" };

		static final String[] PROJECT = { "project", "Project", "PROJECT" };

		static final String[] SOURCE = { "source", "Source", "SOURCE", "mysource", "mySource", "mySOURCE" };

		static final String[] KIND = { "Kind", "kind", "KIND", "myKind", "mykind", "myKIND" };
		static final String[] CLONE = { "Clone", "clone", "CLONE", "myClone", "myclone", "myCLONE" };
		static final String[] LABEL = { "ShortName", "shortName", "SHORTNAME", "myShortName", "myshortName", "mySHORTNAME" };
		static final String[] HIDE = { "Hidden", "hidden", "HIDDEN" };
		static final String[] PARENT = { "commonParent", "parent", "CommonParent", "COMMONPARENT", "myParent", "Parent" };

		static final String[] REMOTE_HG = { "RemoteHG", "remoteHG", "REMOTEHG", "Remotehg", "RemoteHg" };	
	}

	/**
	 * Path to the configuration (user.home)
	 */
	public static String CONFIG_PATH;

	public static Logger _log = Logger.getLogger(ClientPreferences.class);

	public static ClientPreferences DEFAULT_CLIENT_PREFERENCES;

	static {
		String path = System.getProperty("user.home");
		if (!path.endsWith(File.separator))
			path += File.separator;

		CONFIG_PATH = path + ".conflictClient.xml";

		DEFAULT_CLIENT_PREFERENCES = new ClientPreferences("/tmp/conflictClient/", "/path/to/hg");
		ProjectPreferences pp = new ProjectPreferences(new DataSource("myProject", "$HOME/dev/myProject/", DataSource.RepoKind.HG, false, "MASTER"), DEFAULT_CLIENT_PREFERENCES);
		pp.addDataSource(new DataSource("jim", "https://path/to/repo", DataSource.RepoKind.HG, false, "MASTER"));
		pp.addDataSource(new DataSource("MASTER", "http://path/to/repo", DataSource.RepoKind.HG, false, null));
		DEFAULT_CLIENT_PREFERENCES.addProjectPreferences(pp);
	}
	/**
	 * Maps a short name (usually project id) to a preference.
	 */
	// Hashtable<String, ProjectPreferences> _projectPreferences = new Hashtable<String, ProjectPreferences>();
	Vector<ProjectPreferences> _projectPreferences = new Vector<ProjectPreferences>();

	/**
	 * Points to the user's scratch space. Directory must exist.
	 */
	private String _tempDirectory;

	/**
	 * Points to the user's hg path.
	 */
	private String _hgPath;

	/**
	 * Indicates whether these preferences have changed since the last load.
	 */
	private boolean _hasChanged;
	
	/**
	 * Private constructor to restrict usage.
	 */
	private ClientPreferences() {
		// disabled
	}

	/**
	 * Default constructor.
	 * 
	 * @param tempDirectory
	 * @param hgPath
	 */
	public ClientPreferences(String tempDirectory, String hgPath) {
		_tempDirectory = tempDirectory;
		_hgPath = hgPath;
		_hasChanged = false;
	}

	/**
	 * Adds the preference to the project.
	 * 
	 * @param pref
	 *            Preference to add; the pref short name must be unique or an assertion will fail.
	 */
	public void addProjectPreferences(ProjectPreferences pref) {
		//String shortName = pref.getEnvironment().getShortName();

		for (ProjectPreferences pp : _projectPreferences) {
			if (pp.getEnvironment().getShortName().equals(pref.getEnvironment().getShortName())) {
				throw new RuntimeException("Duplicate project name: " + pp.getEnvironment().getShortName());
			}
		}

		_projectPreferences.add(pref);
	}

	/**
	 * Removes the preference from the project.
	 * 
	 * @param pref: preference to remove; if pref is not present, do nothing.  
	 */
	public void removeProjectPreferences(ProjectPreferences pref) {

		_projectPreferences.remove(pref);
	}

	/**
	 * Removes the preference from the project.
	 * 
	 * @param index: index of the preference to remove.  
	 */
	public void removeProjectPreferencesAtIndex(int index) {

		_projectPreferences.remove(index);
	}


	/**
	 * Returns the preferences.
	 * 
	 * @return
	 */
	public Collection<ProjectPreferences> getProjectPreference() {
		return _projectPreferences;
	}

	/**
	 * Get the preference for a given key.
	 * 
	 * @param shortName
	 * @return
	 */
	public ProjectPreferences getProjectPreferences(String shortName) {
		// assert _projectPreferences.containsKey(shortName);
		for (ProjectPreferences pp : _projectPreferences) {
			if (pp.getEnvironment().getShortName().equals(shortName)) {
				return pp;
			}
		}
		throw new RuntimeException("Project preferences: " + shortName + " does not exist.");
	}

	/**
	 * Load the saved preferences from config.xml.
	 * 
	 * TODO: Sensibly display when a preference is invalid.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static ClientPreferences loadPreferencesFromXML() {
		ClientPreferences prefs = null;
		boolean prefsChanged = false;

		SAXBuilder builder = new SAXBuilder(false);
		Document doc = null;

		try {

			File configFile = new File(CONFIG_PATH);
			if (!configFile.exists()) {
				configFile.createNewFile();

				InputStream is = ClientPreferences.class.getResourceAsStream("defaultConfig.xml");
				assert is != null;

				OutputStream os = new FileOutputStream(configFile);
				assert os != null;

				byte[] buffer = new byte[1024];
				int len;

				while ((len = is.read(buffer)) >= 0)
					os.write(buffer, 0, len);

				is.close();
				os.close();

				_log.info("Created new configuration file: " + configFile.getAbsolutePath());

			} else {

				_log.info("Using existing config file: " + configFile.getAbsolutePath());

			}

			// will throw a JDOMExeption if the XML file cannot be parsed
			doc = builder.build(CONFIG_PATH);

			Element rootElement = doc.getRootElement();
			String tempDirectory = getValue(rootElement, IPrefXML.TMP_DIR);
			if (tempDirectory.startsWith(Constants.HOME)) {
				String firstPart = System.getProperty("user.home");
				String lastPart = tempDirectory.substring(Constants.HOME.length());
				tempDirectory = firstPart + lastPart;

				_log.trace("$HOME in temporary path: " + (firstPart + lastPart));
			}

			if ((!(tempDirectory.endsWith(File.separator))) && (!(tempDirectory.endsWith("/")))) {
				tempDirectory += File.separator;
				prefsChanged = true;
			}

			String temptempDirectory = tempDirectory.replace('\\','/');
			if (!temptempDirectory.equals(tempDirectory)) {
				prefsChanged = true;
				tempDirectory = temptempDirectory;
			}

			boolean happyTempPath = false;
			while (!happyTempPath) {
				try {
					verifyPath(tempDirectory);
					happyTempPath = true;
				} catch (ConfigurationReadingException e) {
					if (e.getType() == ConfigurationReadingException.PATH_INVALID)
						if ((new File(tempDirectory)).mkdirs())
							happyTempPath = true;
						else {
							tempDirectory = JOptionPane.showInputDialog("The current temprorary path is invalid.\nPlease select another path.", tempDirectory);
							prefsChanged = true;
						}
				}
			}

			String hgPath = getValue(rootElement, IPrefXML.HG_PATH);
			boolean happyHgPath = false;
			while (!happyHgPath) {
				try {
					verifyFile(hgPath);
					happyHgPath = true;
				} catch (ConfigurationReadingException e) {
					// if the exception type is either ConfigurationReadingException.PATH_INVALID or ConfigurationReadingException.PATH_IS_DIRECTORY 
					// (only two possibilities))
					hgPath = JOptionPane.showInputDialog("The current path to hg is invalid.\nPlease select a proper path.", hgPath);
					prefsChanged = true;
				}
			}

			prefs = new ClientPreferences(tempDirectory, hgPath);
			prefs.setChanged(prefsChanged);

			// read the attributes.
			// make sure to check for old versions with the RETRO_PREFIX prefix.  
			List<Element> projectElements = getChildren(rootElement, IPrefXML.PROJECT);
			for (Element projectElement : projectElements) {
				String projectKind = getValue(projectElement, IPrefXML.KIND);
				String projectLabel = getValue(projectElement, IPrefXML.LABEL);
				String projectClone = getValue(projectElement, IPrefXML.CLONE);
				String projectRemoteHg = getValue(projectElement, IPrefXML.REMOTE_HG);
				String projectParent = getValue(projectElement, IPrefXML.PARENT);

				if (projectKind == null) {
					throw new RuntimeException("Kind attribute must be set for project element.");
				}
				if (projectLabel == null) {
					throw new RuntimeException("ShortName attribute must be set for project element.");
				}
				if (projectClone == null) {
					throw new RuntimeException("Clone attribute must be set for project element.");
				}

				if (projectClone.startsWith(Constants.HOME)) {
					String firstPart = System.getProperty("user.home");
					String lastPart = projectClone.substring(Constants.HOME.length());
					projectClone = firstPart + lastPart;

					_log.trace("$HOME in project path: " + (firstPart + lastPart));
				}

				RepoKind kind = RepoKind.valueOf(projectKind);

				// The project need not be a local path!  
				//				verifyPath(projectClone);

				if (kind == null || !kind.equals(RepoKind.HG)) {
					throw new RuntimeException("ClientPreferences - Kind not valid. (currently only HG is supported).");
				}

				if (projectLabel == null || projectLabel.equals("")) {
					throw new RuntimeException("ClientPreferences - project shortName must be specified.");
				}

				DataSource myEnvironment = new DataSource(projectLabel, projectClone, kind, false, projectParent);
				myEnvironment.setRemoteHg(projectRemoteHg);

				_log.trace("Loaded project: " + myEnvironment);

				ProjectPreferences projectPreferences = new ProjectPreferences(myEnvironment, prefs);
				prefs.addProjectPreferences(projectPreferences);

				if (getChild(projectElement, IPrefXML.SOURCE) != null) {
					List<Element> sourceElements = getChildren(projectElement, IPrefXML.SOURCE);
					for (Element sourceElement : sourceElements) {
						String sourceLabel = getValue(sourceElement, IPrefXML.LABEL);
						String sourceClone = getValue(sourceElement, IPrefXML.CLONE);
						String sourceRemoteHg = getValue(sourceElement, IPrefXML.REMOTE_HG);
						String sourceHidden = getValue(sourceElement, IPrefXML.HIDE);
						boolean sourceHide = ((sourceHidden != null) && (sourceHidden.toLowerCase().trim().equals("true"))) ? true : false;		
						String sourceParent = getValue(sourceElement, IPrefXML.PARENT);

						if (sourceLabel == null || sourceLabel.equals("")) {
							throw new RuntimeException("Label attribute must be set for source element.");
						}

						if (sourceClone == null || sourceClone.equals("")) {
							throw new RuntimeException("Clone attribute must be set for source element.");
						}

						if (sourceClone.startsWith(Constants.HOME)) {
							String firstPart = System.getProperty("user.home");
							String lastPart = sourceClone.substring(Constants.HOME.length());
							sourceClone = firstPart + lastPart;

							_log.trace("$HOME in project path: " + (firstPart + lastPart));
						}

						DataSource source = new DataSource(sourceLabel, sourceClone, kind, sourceHide, sourceParent);
						source.setRemoteHg(sourceRemoteHg);
						_log.trace("Loaded data source: " + source);

						projectPreferences.addDataSource(source);
					}
				}
			}
		} catch (JDOMException jdome) {
			_log.error("Error parsing configuration file.", jdome);
		} catch (IOException ioe) {
			throw new RuntimeException("Error reading configuration file; " + ioe.getMessage(), ioe);
		} catch (Exception e) {
//			e.printStackTrace();
			throw new RuntimeException("Error parsing configuration file; " + e.getMessage(), e);
		}

		return prefs;
	}
	
	/*
	 * Returns the value of the first existing attribute in element 
	 */
	private static String getValue(Element element, String[] attributes) {
		for (String attribute : attributes) {
			String answer = element.getAttributeValue(attribute);
			if (answer != null)
				return answer;
		}
		return null;
	}
	
	/*
	 * Returns the list of children of the first existing attribute in element 
	 */
	private static List<Element> getChildren(Element element, String[] attributes) {
		for (String attribute : attributes) {
			List<Element> answer = element.getChildren(attribute);
			if (answer != null)
				return answer;
		}
		return null;
	}
	
	/*
	 * Returns the child of the first existing attribute in element 
	 */
	private static Element getChild(Element element, String[] attributes) {
		for (String attribute : attributes) {
			Element answer = element.getChild(attribute);
			if (answer != null)
				return answer;
		}
		return null;
	}


	/**
	 * Save preferences to the default filename
	 * 
	 * @effect saves preferences to the default filename
	 */
	public static void savePreferencesToDefaultXML(ClientPreferences prefs) {
		savePreferencesToXML(prefs, CONFIG_PATH);
	}

	/**
	 * Save preferences to fName
	 * 
	 * @effect saves preferences to a file fName
	 */
	public static void savePreferencesToXML(ClientPreferences prefs, String fName) {

		Document doc = XMLTools.newXMLDocument();

		Element rootElem = new Element(IPrefXML.ROOT[0]);
		
		Comment webref1 = new Comment(" Configuration file for Crystal conflict client. See documentation at ");
		Comment webref2 = new Comment(" http://www.cs.washington.edu/homes/brun/research/crystal/ . ");

		Comment sample = new Comment(
				" Example:\n" + 
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<ccConfig tempDirectory=\"C:/temp/conflictClient/\" hgPath=\"C:/Program Files/TortoiseHg/hg.exe\">\n" +
				"  <project Kind=\"HG\" ShortName=\"MyFirstProject\" Clone=\"C:/projects/myLocalFirstProjectRepo/\">\n" +
				"    <source ShortName=\"MASTER\" Clone=\"ssh://user@host/path/to/repo/\" />\n" +
				"    <source ShortName=\"Friend\" Clone=\"ssh://user@host/path/to/friend/repo/\" />\n" +
				"  </project>\n" +
				"  <project Kind=\"HG\" ShortName=\"MySecondProject\" Clone=\"C:/projects/myLocalSecondProjectRepo/\">\n" + 
				"    <source ShortName=\"MASTER\" Clone=\"ssh://user@host/path/to/socond/project/repo/\" />\n" +
				"    <source ShortName=\"Friend\" Clone=\"https://user@host/path/to/friend/second/repo/\" />\n" +
				"  </project>\n" +
				"</ccConfig>\n");

		doc.addContent(webref1);
		doc.addContent(webref2);
		doc.addContent(sample);
		
		rootElem.setAttribute(IPrefXML.TMP_DIR[0], prefs.getTempDirectory());
		rootElem.setAttribute(IPrefXML.HG_PATH[0], prefs.getHgPath());
		doc.setRootElement(rootElem);

		for (ProjectPreferences pp : prefs.getProjectPreference()) {
			Element projectElem = new Element(IPrefXML.PROJECT[0]);
			projectElem.setAttribute(IPrefXML.KIND[0], pp.getEnvironment().getKind().name());
			projectElem.setAttribute(IPrefXML.LABEL[0], pp.getEnvironment().getShortName());
			projectElem.setAttribute(IPrefXML.CLONE[0], pp.getEnvironment().getCloneString());
			projectElem.setAttribute(IPrefXML.PARENT[0], pp.getEnvironment().getParent());
						
			rootElem.addContent(projectElem);

			for (DataSource src : pp.getDataSources()) {
				Element sourceElem = new Element(IPrefXML.SOURCE[0]);
//				sourceElem.setAttribute(IPrefXML.KIND, src.getKind().name());
				sourceElem.setAttribute(IPrefXML.LABEL[0], src.getShortName());
				sourceElem.setAttribute(IPrefXML.CLONE[0], src.getCloneString());
				if (src.isHidden())
					sourceElem.setAttribute(IPrefXML.HIDE[0], "true");
				else
					sourceElem.setAttribute(IPrefXML.HIDE[0], "false");
				sourceElem.setAttribute(IPrefXML.PARENT[0], src.getParent());

				projectElem.addContent(sourceElem);
			}
		}

		XMLTools.writeXMLDocument(doc, fName);
	}

	/**
	 * Check to ensure the provided file exists.
	 * 
	 * @param fName 
	 * @throws ConfigurationReadingException 
	 */
	private static void verifyFile(String fName) throws ConfigurationReadingException {

		// assert fName != null;
		// assert new File(fName).exists();
		// assert new File(fName).isFile();

		if (fName == null)
			throw new NullPointerException("Null path checked");
		if (!new File(fName).exists())
			throw new ConfigurationReadingException("File does not exist: " + fName, ConfigurationReadingException.PATH_INVALID);
		if (!new File(fName).isFile()) 
			throw new ConfigurationReadingException("File is a directory: " + fName, ConfigurationReadingException.PATH_IS_DIRECTORY);
	}

	/**
	 * Check to ensure the provided path is a valid directory.
	 * 
	 * @param path
	 * @throws ConfigurationReadingException 
	 */
	private static void verifyPath(String path) throws ConfigurationReadingException {

		// assert path != null;
		// assert new File(path).exists();
		// assert new File(path).isDirectory();

		if (path == null) 
			throw new NullPointerException("Null path checked");
		if (!new File(path).exists())
			throw new ConfigurationReadingException("Path does not exist: " + path, ConfigurationReadingException.PATH_INVALID);
		if (!new File(path).isDirectory()) 
			throw new ConfigurationReadingException("Path is not a directory: " + path, ConfigurationReadingException.PATH_NOT_DIRECTORY);		
	}

	/**
	 * 
	 * @return path to the user's hg binary
	 */
	public String getHgPath() {
		return _hgPath;
	}

	/**
	 * 
	 * @effect set the path to the user's hg binary
	 */
	public void setHgPath(String hgPath) {
		_hgPath = hgPath;
	}

	/**
	 * @return path to the user's scratch space
	 */
	public String getTempDirectory() {
		return _tempDirectory;
	}

	/**
	 * @effect set the path to the user's scratch space
	 */
	public void setTempDirectory(String tempDirectory) {
		_tempDirectory = tempDirectory;
	}

	/**
	 * @return whether this has changed since loading or creating
	 */
	public boolean hasChanged() {
		return _hasChanged;
	}

	/**
	 * 
	 * @effect set whether this has changed since loading or creating
	 */
	public void setChanged(boolean status) {
		_hasChanged = status;
	}


	public static class ConfigurationReadingException extends Exception {
		private static final long serialVersionUID = 3577953111265604385L;

		public static final int HG_PATH_INVALID = 0;
		public static final int TEMP_PATH_INVALID = 1;
		public static final int PATH_INVALID = 2;
		public static final int PATH_NOT_DIRECTORY = 3;
		public static final int PATH_IS_DIRECTORY = 4;

		private int _type;

		public ConfigurationReadingException(int type) {
			super();
			_type = type;
		}

		public ConfigurationReadingException(String message, int type) {
			super(message);
			_type = type;
		}

		public int getType() {
			return _type;
		}
	}
}
