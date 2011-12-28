package crystal.client;

import java.io.File;
import java.io.FileNotFoundException;
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
import crystal.util.RunIt;
import crystal.util.ValidInputChecker;
import crystal.util.XMLTools;

/**
 * ClientPreferences is the top level object that describes all the preferences and the projects and relevant repositories of a Crystal instance.
 * 
 * @author brun
 * @author rtholmes
 * 
 */
public class ClientPreferences implements Cloneable {

    static {
        ConflictSystemTray.startLogging();
    }

    // IPrefXML contains some constants for use when parsing and writing ClientPreferences to XML.
    private interface IPrefXML {

        /**
         * Each constant consists of an array of logically equivalent elements. For example, either the element "ccConfig" or "ccconfig", in an XML
         * file, will be considered the root element. When writing XML, the [0]th element in the array will be used.
         */

        // the name of the root element
        static final String[] ROOT = { "ccConfig", "ccconfig", "CcConfig", "CCConfig" };

        // the path to the temp directory
        static final String[] TMP_DIR = { "tempDirectory", "TempDirectory" };

        // @deprecated
        // the path to the hg executable
        // static final String[] HG_PATH = { "hgPath", "HgPath", "HGPath" };

        // the refresh rate in seconds
        static final String[] REFRESH = { "refresh", "Refresh", "REFRESH", "" };

        // a project
        static final String[] PROJECT = { "project", "Project", "PROJECT" };

        // a source (repository) within a project
        static final String[] SOURCE = { "source", "Source", "SOURCE", "mysource", "mySource", "mySOURCE" };

        // a source's kind
        static final String[] KIND = { "Kind", "kind", "KIND", "myKind", "mykind", "myKIND" };

        // a source's address
        static final String[] CLONE = { "Clone", "clone", "CLONE", "myClone", "myclone", "myCLONE" };

        // a source's name
        static final String[] LABEL = { "ShortName", "shortName", "SHORTNAME", "myShortName", "myshortName", "mySHORTNAME" };

        // whether or not a source is hidden
        static final String[] HIDE = { "Hidden", "hidden", "HIDDEN" };

        // a source's parent
        static final String[] PARENT = { "commonParent", "parent", "CommonParent", "COMMONPARENT", "myParent", "Parent" };

        // the path to the remote HG
        //static final String[] REMOTE_HG = { "RemoteHG", "remoteHG", "REMOTEHG", "Remotehg", "RemoteHg" };

        // the path to the remote command
        static final String[] REMOTE_CMD = { "RemoteCMD", "remoteCMD", "REMOTECMD", "Remotecmd", "RemoteCmd" };
        
        // the command to compile
        static final String[] COMPILE = { "compile", "Compile", "COMPILE" };

        // the command to test
        static final String[] TEST = { "test", "Test", "TEST" };
    }

    // The current refresh rate that's static because it needs to be visible to the GUI
    public static long REFRESH = Constants.DEFAULT_REFRESH;

    // The path to the configuration file.
    public static String CONFIG_PATH;

    public static Logger _log = Logger.getLogger(ClientPreferences.class);

    // The default to use if the config file cannot be read or parsed.
    public static ClientPreferences DEFAULT_CLIENT_PREFERENCES;
    static {
        String path = System.getProperty("user.home");
        if (!path.endsWith(File.separator))
            path += File.separator;

        CONFIG_PATH = path + ".conflictClient.xml";

        DEFAULT_CLIENT_PREFERENCES = new ClientPreferences("/tmp/conflictClient/", "/path/to/hg", "/path/to/git", Constants.DEFAULT_REFRESH);
        ProjectPreferences pp = new ProjectPreferences(new DataSource("myProject", "$HOME/dev/myProject/", DataSource.RepoKind.HG, false, "MASTER"),
                DEFAULT_CLIENT_PREFERENCES);
        pp.addDataSource(new DataSource("jim", "https://path/to/repo", DataSource.RepoKind.HG, false, "MASTER"));
        pp.addDataSource(new DataSource("MASTER", "http://path/to/repo", DataSource.RepoKind.HG, false, null));
        try {
            DEFAULT_CLIENT_PREFERENCES.addProjectPreferences(pp);
        } catch (DuplicateProjectNameException e) {
            // This will never happen because we know exactly what's in DEFAULT_CLIENT_PREFERENCES
            throw new RuntimeException(e);
        }
    }

    /**
     * A vector of projects.
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
     * Points to the user's git path
     */
    private String _gitPath;

    /**
     * The number of seconds between refreshes.
     */
    private long _refresh;

    /**
     * Indicates whether these preferences have changed since the last load from file.
     */
    private boolean _hasChanged;

    /**
     * Private constructor to restrict usage.
     */
    @SuppressWarnings("unused")
    private ClientPreferences() {
        // disabled
    }


    
    /**
     * Default constructor. Creates a new ClientPreferences with the tempDirectory and hgPath set and 0 projects.
     * 
     * @param tempDirectory
     * @param hgPath
     */
    public ClientPreferences(String tempDirectory, String hgPath, String gitPath, long refresh) {
    	ValidInputChecker.checkValidStringInput(tempDirectory);
    	ValidInputChecker.checkValidStringInput(hgPath);
    	if(refresh < 0){
    		throw new IllegalArgumentException("Negative number for time");
    	}
        _tempDirectory = tempDirectory;
        _hgPath = hgPath;
        _gitPath = gitPath;
        _refresh = refresh;
        REFRESH = refresh;
        _hasChanged = false;
    }
    

    /**
     * Compare this object with another object
     * @param o target object
     * @return true if they are same object; otherwise return false
     */
    public boolean equals(Object o){
    	if(o != null && getClass() == o.getClass()){
    		ClientPreferences other = (ClientPreferences) o;
    		return _tempDirectory.equals(other._tempDirectory) && _hgPath.equals(other._hgPath)
    				&& _refresh == other._refresh
    				&& _projectPreferences.equals(other._projectPreferences);
    	} else {
    		return false;
    	}
    }

    /**
     * Adds a project to this ClientPreferences.
     * 
     * @param pref
     *            : Preference to add;
     * @throws DuplicateProjectNameException
     *             if pref.getShortName() is not unique in the set of projects in this ClientPreferences.
     */
    public void addProjectPreferences(ProjectPreferences pref) throws DuplicateProjectNameException {
        // String shortName = pref.getEnvironment().getShortName();
        for (ProjectPreferences pp : _projectPreferences) {
            if (pp.getName().equals(pref.getName())) {
                throw new DuplicateProjectNameException("Duplicate project name: " + pp.getName());
            }
        }
        _projectPreferences.add(pref);
    }

    /**
     * Removes the project pref from this ClientPreferences.
     * 
     * @param pref
     *            : project to remove. If pref is not present, do nothing.
     */
    public void removeProjectPreferences(ProjectPreferences pref) {
        _projectPreferences.remove(pref);
    }

    /**
     * Removes the project at the index index from this ClientPreferences.
     * 
     * @param index
     *            : the index of the project to remove.
     */
    public void removeProjectPreferencesAtIndex(int index) {
        _projectPreferences.remove(index);
    }

    /**
     * Returns a Collection of projects. (Leaks internal representation.)
     * 
     * @return a Collection of projects.
     */
    public Collection<ProjectPreferences> getProjectPreference() {
        return _projectPreferences;
    }

    /**
     * Returns the project with the specified shortName.
     * 
     * @param shortName
     *            : the name of the project to return
     * @return the project with the specified shortName.
     * @throws NonexistentProjectException
     */
    public ProjectPreferences getProjectPreferences(String shortName) throws NonexistentProjectException {
        for (ProjectPreferences pp : _projectPreferences) {
            if (pp.getName().equals(shortName)) {
                return pp;
            }
        }
        // could not find the project with shortName
        throw new NonexistentProjectException("Project preferences: " + shortName + " does not exist.");
    }
    
    /**
     * Load preferences from the default config file location.
     * 
     * @return the ClientPreferences represented by the default config file. 
     *   If the config file does not exist, then it returns the default configuration 
     *   (written in defaultWindowsConfig.xml or defaultOtherConfig.xml, depending on the OS.  
     * @throws IOException 
     * @throws IOException from FileIO and 
     *         various Runtime exceptions from the XML reader and parser.
     */
    public static ClientPreferences loadPreferencesFromDefaultXML() throws IOException {
    	File configFile = new File(CONFIG_PATH);
    	if (!configFile.exists()) {
            configFile.createNewFile();
            
            String defaultXML = null;
            if (System.getProperty("os.name").toUpperCase().indexOf("WINDOWS") > -1)
                defaultXML = "defaultWindowsConfig.xml";
            else
                defaultXML = "defaultOtherConfig.xml";
            InputStream is = ClientPreferences.class.getResourceAsStream(defaultXML);
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
    	return loadPreferencesFromXML(configFile);
    }
    
    /**
     * Load the saved preferences from a config file.
     * 
     * @return the ClientPreferences represented by the configFile. 
     *   If the config file does not exist, then it returns null.  
     * @throws various Runtime exceptions from the XML reader and parser.
     */
    public static ClientPreferences loadPreferencesFromXML(File configFile) {
        ClientPreferences prefs = null;
        boolean prefsChanged = false;

        SAXBuilder builder = new SAXBuilder(false);
        Document doc = null;

        if (!configFile.exists())
        	return null;
        
        try {
        	// will throw a JDOMExeption if the XML file cannot be parsed
        	doc = builder.build(configFile.getAbsolutePath());
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

        	String temptempDirectory = tempDirectory.replace('\\', '/');
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
        					tempDirectory = JOptionPane.showInputDialog("The current temprorary path is invalid.\nPlease select another path.",
        							tempDirectory);
        					prefsChanged = true;
        				}
        		}
        	}

        	long refresh;
        	String refreshStr = getValue(rootElement, IPrefXML.REFRESH);
        	if (refreshStr == null)
        		refresh = Constants.DEFAULT_REFRESH;
        	else
        		refresh = Long.parseLong(refreshStr);
        	if (refresh < 0)
        		refresh = Constants.DEFAULT_REFRESH;

        	// @deprecated
        	// String hgPath = getValue(rootElement, IPrefXML.HG_PATH);
        	String hgPath = RunIt.getExecutable("hg");
        	String gitPath = RunIt.getExecutable("git");
        	
        	// check hg path
        	boolean happyHgPath = false;
        	while (!happyHgPath) {
        		try {
                    if (hgPath==null) 
                        throw new ConfigurationReadingException(ConfigurationReadingException.HG_PATH_INVALID);
        			verifyFile(hgPath);
        			happyHgPath = true;
        		} catch (ConfigurationReadingException e) {
        			// if the exception type is either ConfigurationReadingException.PATH_INVALID or
        			// ConfigurationReadingException.PATH_IS_DIRECTORY
        			// (only two possibilities))
        			
        			// check if projectPreferences contains hg repo
        			boolean need = false;
        			for (Element projectElement : getChildren(rootElement, IPrefXML.PROJECT)) {
        				String projectKind = getValue(projectElement, IPrefXML.KIND);
        				if (projectKind.equals("HG")) {
        					need = true;
        				}
        			}
        			
        			if (need) {
        				hgPath = JOptionPane.showInputDialog("The current path to hg is invalid.\nPlease select a proper path.", hgPath);
        				prefsChanged = true;
        			} else
                        happyHgPath = true;
        		}
        	}
        	
        	// check git path
        	boolean happyGitPath = false;
        	while (!happyGitPath) {
        		try {
        		    if (gitPath==null) 
        		        throw new ConfigurationReadingException(ConfigurationReadingException.GIT_PATH_INVALID);
        			verifyFile(gitPath);
        			happyGitPath = true;
        		} catch (ConfigurationReadingException e) {
        			// if the exception type is either ConfigurationReadingException.PATH_INVALID or
        			// ConfigurationReadingException.PATH_IS_DIRECTORY
        			// (only two possibilities))
        			
        			// check if projectPreferences contains git repo
        			boolean need = false;
        			for (Element projectElement : getChildren(rootElement, IPrefXML.PROJECT)) {
        				String projectKind = getValue(projectElement, IPrefXML.KIND);
        				if (projectKind.equals("GIT")) {
        					need = true;
        				}
        			}
        			if (need) {
        				gitPath = JOptionPane.showInputDialog("The current path to git is invalid.\nPlease select a proper path.", gitPath);
        				prefsChanged = true;
        			} else
                        happyGitPath = true;        			    
        		}
        	}

        	prefs = new ClientPreferences(tempDirectory, hgPath, gitPath, refresh);
        	prefs.setChanged(prefsChanged);

        	// read the attributes.
        	// make sure to check for old versions with the RETRO_PREFIX prefix.
        	List<Element> projectElements = getChildren(rootElement, IPrefXML.PROJECT);
        	for (Element projectElement : projectElements) {
        		String projectKind = getValue(projectElement, IPrefXML.KIND);
        		String projectLabel = getValue(projectElement, IPrefXML.LABEL);
        		String projectClone = getValue(projectElement, IPrefXML.CLONE);
        		String projectRemoteCmd = getValue(projectElement, IPrefXML.REMOTE_CMD);
        		String projectParent = getValue(projectElement, IPrefXML.PARENT);
        		String compileCommand = getValue(projectElement, IPrefXML.COMPILE);
        		String testCommand = getValue(projectElement, IPrefXML.TEST);


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
        		// verifyPath(projectClone);

        		
        		if (kind == null || (!kind.equals(RepoKind.HG) && !kind.equals(RepoKind.GIT))) {
        			throw new RuntimeException("ClientPreferences - Kind not valid. (currently only HG and GIT are supported).");
        		}

        		if (projectLabel == null || projectLabel.equals("")) {
        			throw new RuntimeException("ClientPreferences - project shortName must be specified.");
        		}

        		DataSource myEnvironment = new DataSource(projectLabel, projectClone, kind, false, projectParent);
        		myEnvironment.setRemoteCmd(projectRemoteCmd);
        		if ((compileCommand != null) && (!(compileCommand.trim().isEmpty()))) {
        			String compileCommandExecutable = RunIt.getExecutable(compileCommand);
        			if (compileCommandExecutable == null) {
        				_log.error("Error while looking for a way to execute the build command: " + compileCommand + "\nCrystal will ignore this command.");
        				// throw new Error("No executable found for " + compileCommand);
        			}
        			myEnvironment.setCompileCommand(compileCommandExecutable);
        		}
        		if ((testCommand != null) && (!(testCommand.trim().isEmpty()))) {
        			String testCommandExecutable = RunIt.getExecutable(testCommand);
        			if (testCommandExecutable == null) {
        				_log.error("Error while looking for a way to execute the test command: " + testCommand + "\nCrystal will ignore this command.");
        				// throw new Error("No executable found for " + testCommand);
        			}
        			myEnvironment.setTestCommand(testCommandExecutable);
        		}

        		_log.trace("Loaded project: " + myEnvironment);

        		ProjectPreferences projectPreferences = new ProjectPreferences(myEnvironment, prefs);
        		prefs.addProjectPreferences(projectPreferences);

        		if (getChild(projectElement, IPrefXML.SOURCE) != null) {
        			List<Element> sourceElements = getChildren(projectElement, IPrefXML.SOURCE);
        			for (Element sourceElement : sourceElements) {
        				String sourceLabel = getValue(sourceElement, IPrefXML.LABEL);
        				String sourceClone = getValue(sourceElement, IPrefXML.CLONE);
        				String sourceRemoteCmd = getValue(sourceElement, IPrefXML.REMOTE_CMD);
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
        				source.setRemoteCmd(sourceRemoteCmd);
        				_log.trace("Loaded data source: " + source);

        				projectPreferences.addDataSource(source);
        			}
        		}
        	}
        } catch (JDOMException jdome) {
        	_log.error("Error parsing configuration file: " + jdome.getMessage(), jdome);
        } catch (IOException ioe) {
            _log.error("IO Error parsing configuration file.", ioe);
        	throw new RuntimeException("Error reading configuration file: " + ioe.getMessage(), ioe);
        } catch (Exception e) {
        	_log.error("Unexpected error while parsing the configuration file: "+ e.getMessage(), e);
        	//e.printStackTrace();
        	throw new RuntimeException("Error parsing configuration file; " + e.getMessage(), e);
        }

        return prefs;
    }

    /**
     * @return the value of the first existing attribute in element.
     */
    private static String getValue(Element element, String[] attributes) {
        for (String attribute : attributes) {
            String answer = element.getAttributeValue(attribute);
            if (answer != null)
                return answer;
        }
        return null;
    }

    /**
     * @return the list of children of the first existing attribute in element.
     */
    private static List<Element> getChildren(Element element, String[] attributes) {
        for (String attribute : attributes) {
            @SuppressWarnings("unchecked")
            List<Element> answer = element.getChildren(attribute);
            if (answer != null)
                return answer;
        }
        return null;
    }

    /**
     * @return the child of the first existing attribute in element.
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
     * @throws FileNotFoundException 
     * 
     * @effect saves preferences to the default filename
     */
    public static void savePreferencesToDefaultXML(ClientPreferences prefs) throws FileNotFoundException {
        savePreferencesToXML(prefs, CONFIG_PATH);
    }

    /**
     * Save preferences to fName
     * 
     * @param fName
     *            : the name of the file
     * @throws FileNotFoundException 
     * @effect saves preferences to a file fName
     */
    public static void savePreferencesToXML(ClientPreferences prefs, String fName) throws FileNotFoundException {
        Document doc = XMLTools.newXMLDocument();

        Element rootElem = new Element(IPrefXML.ROOT[0]);

        Comment webref1 = new Comment(" Configuration file for Crystal conflict client. See documentation at ");
        Comment webref2 = new Comment(" http://www.cs.washington.edu/homes/brun/research/crystal/ . ");

        Comment sample = new Comment(" Example:\n" + "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<ccConfig tempDirectory=\"C:/temp/conflictClient/\" refresh=\"60\">\n"
                + "  <project Kind=\"HG\" ShortName=\"MyFirstProject\" Clone=\"C:/projects/myLocalFirstProjectRepo/\">\n"
                + "    <source ShortName=\"MASTER\" Clone=\"ssh://user@host/path/to/repo/\" />\n"
                + "    <source ShortName=\"Friend\" Clone=\"ssh://user@host/path/to/friend/repo/\" />\n" + "  </project>\n"
                + "  <project Kind=\"HG\" ShortName=\"MySecondProject\" Clone=\"C:/projects/myLocalSecondProjectRepo/\">\n"
                + "    <source ShortName=\"MASTER\" Clone=\"ssh://user@host/path/to/socond/project/repo/\" />\n"
                + "    <source ShortName=\"Friend\" Clone=\"https://user@host/path/to/friend/second/repo/\" />\n" + "  </project>\n"
                + "</ccConfig>\n");

        doc.addContent(webref1);
        doc.addContent(webref2);
        doc.addContent(sample);

        rootElem.setAttribute(IPrefXML.TMP_DIR[0], prefs.getTempDirectory());
        //		rootElem.setAttribute(IPrefXML.HG_PATH[0], prefs.getHgPath()); @deprecated
        rootElem.setAttribute(IPrefXML.REFRESH[0], Long.toString(prefs.getRefresh()));
        doc.setRootElement(rootElem);

        for (ProjectPreferences pp : prefs.getProjectPreference()) {
            Element projectElem = new Element(IPrefXML.PROJECT[0]);
            projectElem.setAttribute(IPrefXML.KIND[0], pp.getEnvironment().getKind().name());
            projectElem.setAttribute(IPrefXML.LABEL[0], pp.getName());
            projectElem.setAttribute(IPrefXML.CLONE[0], pp.getEnvironment().getCloneString());
            if (pp.getEnvironment().getParent() != null)
                projectElem.setAttribute(IPrefXML.PARENT[0], pp.getEnvironment().getParent());
            if (pp.getEnvironment().getCompileCommand() != null)
                projectElem.setAttribute(IPrefXML.COMPILE[0], pp.getEnvironment().getCompileCommand());
            if (pp.getEnvironment().getTestCommand() != null)
                projectElem.setAttribute(IPrefXML.TEST[0], pp.getEnvironment().getTestCommand());

            rootElem.addContent(projectElem);

            for (DataSource src : pp.getDataSources()) {
                Element sourceElem = new Element(IPrefXML.SOURCE[0]);
                // sourceElem.setAttribute(IPrefXML.KIND, src.getKind().name());
                sourceElem.setAttribute(IPrefXML.LABEL[0], src.getShortName());
                sourceElem.setAttribute(IPrefXML.CLONE[0], src.getCloneString());
                if (src.isHidden())
                    sourceElem.setAttribute(IPrefXML.HIDE[0], "true");
                else
                    sourceElem.setAttribute(IPrefXML.HIDE[0], "false");
                if (src.getParent() != null)
                	sourceElem.setAttribute(IPrefXML.PARENT[0], src.getParent());

                projectElem.addContent(sourceElem);
            }
        }

        XMLTools.writeXMLDocument(doc, fName);
    }

    /**
     * Check to ensure the provided file exists.
     * 
     * @effect Nothing! Just throws exceptions if something goes wrong.
     * @param fName
     *            : the filename to check
     * @throws ConfigurationReadingException
     *             if the file does not exist or is a directory
     * @throws NullPointerException
     *             if fname is null
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
     * @effect Nothing! Just throws exceptions if something goes wrong.
     * @param path
     *            : the path to check
     * @throws ConfigurationReadingException
     *             if the path does not exist or is not a directory
     * @throws NullPointerException
     *             if path is null
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
     * @return path to the user's hg binary
     */
    public String getHgPath() {
        return _hgPath;
    }

    /**
     * @effect set the path to the user's hg binary
     * @param hgPath
     *            : the path to hg
     */
    public void setHgPath(String hgPath) {
        _hgPath = hgPath;
    }
    
    /**
     * Get git path
     * @return path to the user's git binary
     */
    public String getGitPath() {
    	return _gitPath;
    }

    /**
     * @effect set the path to the user's git binary
     * @param gitPath the path to git
     */
    public void setGitPath(String gitPath) {
    	_gitPath = gitPath;
    }

    /**
     * @return path to the user's scratch space
     */
    public String getTempDirectory() {
        return _tempDirectory;
    }

    /**
     * @effect set the path to the user's scratch space
     * @param tempDirectory
     *            : the path to the scratch space
     */
    public void setTempDirectory(String tempDirectory) {
        _tempDirectory = tempDirectory;
    }

    /**
     * @return the refresh rate
     */
    public long getRefresh() {
        return _refresh;
    }

    /**
     * @effect set the refresh rate
     * @param refresh
     *            : the new refresh rate
     */
    public void setRefresh(long refresh) {
        _refresh = refresh;
    }

    /**
     * @return whether this has changed since loading or creating
     */
    public boolean hasChanged() {
        return _hasChanged;
    }

    /**
     * @effect set whether this has changed since loading or creating
     * @param status
     *            : whether this has changed since loading or creating
     */
    public void setChanged(boolean status) {
        _hasChanged = status;
    }
    
    /**
     * Return clone of this object
     * @return deep clone of this project
     */
    public ClientPreferences clone() {
    	try {
			ClientPreferences clone = (ClientPreferences) super.clone();
			
			Vector<ProjectPreferences> temp = new Vector<ProjectPreferences>();
			
			
			for (ProjectPreferences pref : this._projectPreferences) {
				temp.add(pref.clone());
			}
			
			clone._projectPreferences = temp;
			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
    }

    /**
     * Thrown when there is a a problem reading a configuration, such as a path or file locations are invalid.
     * 
     * @author brun
     */
    public static class ConfigurationReadingException extends Exception {
        private static final long serialVersionUID = 3577953111265604385L;

        // type differentiates possible reasons for the exception.
        public static final int HG_PATH_INVALID = 0;
        public static final int TEMP_PATH_INVALID = 1;
        public static final int PATH_INVALID = 2;
        public static final int PATH_NOT_DIRECTORY = 3;
        public static final int PATH_IS_DIRECTORY = 4;
        public static final int GIT_PATH_INVALID = 5;

        private int _type;

        // Create a new ConfigurationReadingException of type type.
        public ConfigurationReadingException(int type) {
            super();
            _type = type;
        }

        // Create a new ConfigurationReadingException of type type with a message.
        public ConfigurationReadingException(String message, int type) {
            super(message);
            _type = type;
        }

        // Get the type of this ConfigurationReadingException.
        public int getType() {
            return _type;
        }
    }

    /**
     * Thrown when two projects with the same name are added.
     * 
     * @author brun
     */
    public static class DuplicateProjectNameException extends Exception {
        private static final long serialVersionUID = 236669323196097853L;

        public DuplicateProjectNameException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when a requested project does not exist.
     * 
     * @author brun
     */
    public static class NonexistentProjectException extends Exception {
        private static final long serialVersionUID = 3426961654411908508L;

        public NonexistentProjectException(String message) {
            super(message);
        }
    }

}
