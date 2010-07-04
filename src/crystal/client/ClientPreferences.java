package crystal.client;

import java.util.Collection;
import java.util.Hashtable;

/**
 * Enable us to hold multiple sets of preferences, rather than just one
 * 
 * @author rtholmes
 * 
 */
public class ClientPreferences {

	Hashtable<String, ProjectPreferences> _projectPreferences = new Hashtable<String, ProjectPreferences>();

	public Collection<ProjectPreferences> getProjectPreference() {
		return _projectPreferences.values();
	}

	public void addProjectPreferences(ProjectPreferences pref) {
		String key = pref.getEnvironment().getShortName();

		assert !_projectPreferences.containsKey(key);

		_projectPreferences.put(key, pref);
	}

	public ProjectPreferences getProjectPreferences(String prefKey) {
		assert _projectPreferences.containsKey(prefKey);

		return _projectPreferences.get(prefKey);
	}

}
