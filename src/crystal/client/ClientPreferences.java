package crystal.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import crystal.model.DataSource;
import crystal.model.DataSource.RepoKind;

/**
 * Maintains multiple sets of preferences, rather than just one.
 * 
 * @author rtholmes
 * 
 */
public class ClientPreferences {
	public static String CONFIG_PATH;

	static {
		String path = System.getProperty("user.home");
		if (!path.endsWith(File.separator))
			path += File.separator;

		CONFIG_PATH = path + ".conflictClient.xml";

	}
	/**
	 * Maps a short name (usually project id) to a preference.
	 */
	Hashtable<String, ProjectPreferences> _projectPreferences = new Hashtable<String, ProjectPreferences>();

	/**
	 * Points to the user's scratch space. Directory must exist.
	 */
	private String _tempDirectory;

	/**
	 * Poits to the user's hg path.
	 */
	private String _hgPath;

	/**
	 * Private constructor to restrict usage.
	 */
	@SuppressWarnings("unused")
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
	}

	/**
	 * Adds the preference to the project.
	 * 
	 * @param pref
	 *            Preference to add; the pref short name must be unique or an assertion will fail.
	 */
	public void addProjectPreferences(ProjectPreferences pref) {
		String shortName = pref.getEnvironment().getShortName();

		assert !_projectPreferences.containsKey(shortName);

		_projectPreferences.put(shortName, pref);
	}

	/**
	 * Returns the preferences.
	 * 
	 * @return
	 */
	public Collection<ProjectPreferences> getProjectPreference() {
		return _projectPreferences.values();
	}

	/**
	 * Get the preference for a given key.
	 * 
	 * @param shortName
	 * @return
	 */
	public ProjectPreferences getProjectPreferences(String shortName) {
		assert _projectPreferences.containsKey(shortName);

		return _projectPreferences.get(shortName);
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

				System.out.println("ClientPreferences::loadPreferencesFromXML(..) - Created new configuration file: " + configFile.getAbsolutePath());

			} else {

				System.out.println("ClientPreferences::loadPreferencesFromXML(..) - Using existing config file: " + configFile.getAbsolutePath());

			}

			doc = builder.build(CONFIG_PATH);

			Element rootElement = doc.getRootElement();
			String tempDirectory = rootElement.getAttributeValue("tempDirectory");
			verifyPath(tempDirectory);

			String hgPath = rootElement.getAttributeValue("hgPath");
			verifyFile(hgPath);

			prefs = new ClientPreferences(tempDirectory, hgPath);

			List<Element> projectElements = rootElement.getChildren("project");
			for (Element projectElement : projectElements) {
				String myKind = projectElement.getAttributeValue("myKind");
				String myShortName = projectElement.getAttributeValue("myShortName");
				String myClone = projectElement.getAttributeValue("myClone");

				verifyPath(myClone);

				assert myKind != null;
				assert myShortName != null;

				DataSource myEnvironment = new DataSource(myShortName, myClone, RepoKind.valueOf(myKind));

				ProjectPreferences projectPreferences = new ProjectPreferences(myEnvironment, prefs);
				prefs.addProjectPreferences(projectPreferences);

				if (projectElement.getChild("sources") != null) {
					List<Element> sourceElements = projectElement.getChild("sources").getChildren("source");
					for (Element sourceElement : sourceElements) {
						String kind = sourceElement.getAttributeValue("kind");
						String shortName = sourceElement.getAttributeValue("shortName");
						String clone = sourceElement.getAttributeValue("clone");

						assert kind != null;
						assert shortName != null;
						assert clone != null;

						DataSource source = new DataSource(shortName, clone, RepoKind.valueOf(kind));
						projectPreferences.addDataSource(source);
					}
				}
			}
		} catch (JDOMException jdome) {
			System.err.println(jdome);
			throw new RuntimeException("Error parsing configuration file.", jdome);
		} catch (IOException ioe) {
			System.err.println(ioe);
			throw new RuntimeException("Error reading configuration file.", ioe);
		} catch (Exception e) {
			System.err.println(e);
			throw new RuntimeException("Error parsing configuration file.", e);
		}

		assert prefs != null;

		return prefs;
	}

	/**
	 * Check to ensure the provided file exists.
	 * 
	 * @param fName
	 */
	private static void verifyFile(String fName) {

		assert fName != null;
		assert new File(fName).exists();
		assert !new File(fName).isDirectory();

		if (fName == null || !new File(fName).exists() || !new File(fName).isDirectory()) {
			throw new RuntimeException("ConflictClient::verifyFile( " + fName + " ) - File does not exist.");
		}
	}

	/**
	 * Check to ensure the provided path is a valid directory.
	 * 
	 * @param path
	 */
	private static void verifyPath(String path) {

		assert path != null;
		assert new File(path).exists();
		assert new File(path).isDirectory();

		if (path == null || !new File(path).exists() || !new File(path).isDirectory()) {
			throw new RuntimeException("ConflictClient::verifyPath( " + path + " ) - Path does not exist.");
		}
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
	 * @return path to the user's scratch space
	 */
	public String getTempDirectory() {
		return _tempDirectory;
	}
}
