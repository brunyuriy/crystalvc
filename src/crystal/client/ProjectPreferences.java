package crystal.client;

import java.util.Collection;
import java.util.Hashtable;

import crystal.model.DataSource;

public class ProjectPreferences {

	Hashtable<String, DataSource> _dataSources;
	DataSource _myEnvironment = null;
	String _tempDirectory = null;

	public ProjectPreferences(DataSource myEnvironment, String tempDirectory) {
		_myEnvironment = myEnvironment;
		_tempDirectory = tempDirectory;
		_dataSources = new Hashtable<String, DataSource>();
	}

	public void addDataSource(DataSource source) {
		assert !_dataSources.containsKey(source.getShortName());

		_dataSources.put(source.getShortName(), source);
	}

	public Collection<DataSource> getDataSources() {
		return _dataSources.values();
	}

	public DataSource getEnvironment() {
		return _myEnvironment;
	}

	public String getTempDirectory() {
		return _tempDirectory;
	}

	void setTempDirectory(String name) {
		_tempDirectory = name;
	}

	public DataSource getDataSource(String key) {
		return _dataSources.get(key);
	}

	public void removeDataSource(DataSource sourceToRemove) {
		_dataSources.remove(sourceToRemove.getShortName());

	}
}
