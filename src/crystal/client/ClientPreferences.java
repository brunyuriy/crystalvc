package crystal.client;

import java.util.Collection;
import java.util.Hashtable;

import crystal.model.DataSource;

public class ClientPreferences {

	Hashtable<String, DataSource> _dataSources;
	DataSource _myEnvironment = null;
	String _tempDirectory = null;

	public ClientPreferences(DataSource myEnvironment, String tempDirectory) {
		_myEnvironment = myEnvironment;
		_tempDirectory = tempDirectory;
		_dataSources = new Hashtable<String, DataSource>();
	}

	// ClientPreferences(String fName) {
	// loadPreferences(fName);
	// }
	//
	// private void loadPreferences(String fName) {
	// // TODO: load from a properties or XML file
	//
	// _myEnvironment = new DataSource("brun", "http://github.com/brun/voldemort.git", DataSource.RepoKind.GIT);
	// _tempDirectory = "/tmp/";
	//		
	//		
	// _dataSources = new Vector<DataSource>();
	// _dataSources.add(new DataSource("master", "http://github.com/voldemort/voldemort.git", DataSource.RepoKind.GIT));
	// _dataSources.add(new DataSource("kirktrue", "http://github.com/kirktrue/voldemort.git",
	// DataSource.RepoKind.GIT));
	// _dataSources.add(new DataSource("afeinberg", "http://github.com/afeinberg/voldemort.git",
	// DataSource.RepoKind.GIT));
	// _dataSources.add(new DataSource("rsumbaly", "http://github.com/rsumbaly/voldemort.git",
	// DataSource.RepoKind.GIT));
	//
	//		
	// }

	public void addDataSource(DataSource source) {
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
}
