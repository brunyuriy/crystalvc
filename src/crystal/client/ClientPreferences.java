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
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static ClientPreferences loadPreferencesFromXML() {

		ClientPreferences prefs = new ClientPreferences();

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
			assert tempDirectory != null;
			assert new File(tempDirectory).exists();
			assert new File(tempDirectory).isDirectory();

			List<Element> projectElements = rootElement.getChildren("project");
			for (Element projectElement : projectElements) {
				String myKind = projectElement.getAttributeValue("myKind");
				String myShortName = projectElement.getAttributeValue("myShortName");
				String myClone = projectElement.getAttributeValue("myClone");

				assert myKind != null;
				assert myShortName != null;
				assert myClone != null;
				assert new File(myClone).exists();
				assert new File(myClone).isDirectory();

				DataSource myEnvironment = new DataSource(myShortName, myClone, RepoKind.valueOf(myKind));

				ProjectPreferences projectPreferences = new ProjectPreferences(myEnvironment, tempDirectory);
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
		} catch (IOException ioe) {
			System.err.println(ioe);
		} catch (Exception e) {
			e.printStackTrace();
		}

		assert prefs != null;

		return prefs;
	}
}
