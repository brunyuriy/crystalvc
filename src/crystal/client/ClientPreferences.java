package crystal.client;

import java.io.IOException;
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

			doc = builder.build(ClientPreferences.class.getResourceAsStream("config.xml"));

			Element rootElement = doc.getRootElement();
			String tempDirectory = rootElement.getAttributeValue("tempDirectory");
			assert tempDirectory != null;

			List<Element> projectElements = rootElement.getChildren("project");
			for (Element projectElement : projectElements) {
				String myKind = projectElement.getAttributeValue("myKind");
				String myShortName = projectElement.getAttributeValue("myShortName");
				String myClone = projectElement.getAttributeValue("myClone");

				assert myKind != null;
				assert myShortName != null;
				assert myClone != null;

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
		}

		assert prefs != null;

		return prefs;
	}

}
