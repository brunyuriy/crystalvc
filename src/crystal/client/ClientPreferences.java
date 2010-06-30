package crystal.client;

import java.util.Vector;

public class ClientPreferences {

	Vector<DataSource> _dataSources;
	DataSource _myEnvironment = null;
	String _tempDirectory = null;

	ClientPreferences(String fName) {
		loadPreferences(fName);
	}

	private void loadPreferences(String fName) {
		// TODO: load from a properties or XML file

		_myEnvironment = new DataSource("brun", "http://github.com/brun/voldemort.git", DataSource.RepoKind.GIT);

		_dataSources = new Vector<DataSource>();
		_dataSources.add(new DataSource("master", "http://github.com/voldemort/voldemort.git", DataSource.RepoKind.GIT));
		_dataSources.add(new DataSource("kirktrue", "http://github.com/kirktrue/voldemort.git", DataSource.RepoKind.GIT));
		_dataSources.add(new DataSource("afeinberg", "http://github.com/afeinberg/voldemort.git", DataSource.RepoKind.GIT));
		_dataSources.add(new DataSource("rsumbaly", "http://github.com/rsumbaly/voldemort.git", DataSource.RepoKind.GIT));

		_tempDirectory = "/tmp/";
	}

	Vector<DataSource> getDataSources() {
		return _dataSources;
	}

	DataSource getEnvironment() {
		return _myEnvironment;
	}

	String getTempDirectory() {
		return _tempDirectory;
	}

	void setTempDirectory(String name) {
		_tempDirectory = name;
	}
}
