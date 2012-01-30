package crystal.client;

import java.io.File;
import java.util.Collection;
import java.util.Vector;

import crystal.model.DataSource;

/**
 * Stores the preferences for a specific project.
 * 
 * @author rtholmes
 * @author brun
 * 
 */
public class ProjectPreferences implements Cloneable {

	// A vector of configurations of repositories.
	private Vector<DataSource> _dataSources;

	// The configuration of the project's main repository.
	private DataSource _myEnvironment = null;

	// The full configuration.
	private ClientPreferences _clientPreferences = null;

	/**
	 * Creates a new ProjectPreferences with myEnvironment as the main repository and clientPrefs as the overall configuration.
	 * 
	 * @param myEnvironment: the configuration of the project's main repository.
	 * @param clientPrefs: the overall configuration
	 */
	public ProjectPreferences(DataSource myEnvironment, ClientPreferences clientPrefs) {
		_myEnvironment = myEnvironment;
		_clientPreferences = clientPrefs;
		_dataSources = new Vector<DataSource>();
	}
	
	public ProjectPreferences clone() {
		try {
			ProjectPreferences clone = (ProjectPreferences) super.clone();
			
			Vector<DataSource> temp = new Vector<DataSource>();
			
			for (DataSource ds : this._dataSources) {
				temp.add(ds.clone());
			}
			clone._dataSources = temp;
			
			clone._myEnvironment = _myEnvironment.clone();
			clone._clientPreferences = _clientPreferences;
			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

    /**
     * Compare this object with another object
     * @param o target object
     * @return true if they are same object; otherwise return false
     */
	public boolean equals(Object o){
		if(o != null && getClass() == o.getClass()){
			ProjectPreferences other = (ProjectPreferences) o;
			return _myEnvironment.equals(other._myEnvironment);
		} else {
			return false;
		}
	}
	
	/**
	 * Adds a new data source.
	 * 
	 * @param source: the source to add; the short name must be unique.
	 */
	public void addDataSource(DataSource source) {
		// assert !_dataSources.containsKey(source.getShortName());
		for (DataSource ds : _dataSources) {
			if (ds.getShortName().equals(source.getShortName())) {
				throw new RuntimeException("Error adding new source; short name: " + source.getShortName() + " already exists for project: "
						+ _myEnvironment.getShortName());
			}
		}
		_dataSources.add(source);
	}

	/**
	 * @return a collection of all the repositories associated with this project.
	 * The collection does not include  main repository.
	 */
	public Collection<DataSource> getDataSources() {
		return _dataSources;
	}
	
	/**
	 * @return the number of nonhidden data sources
	 */
	public int getNumOfVisibleSources() {
		int answer = 0;
		for (DataSource ds : _dataSources) {
			if (!ds.isHidden())
				answer++;
		}
		return answer;
	}

	/**
	 * @return the data source of the main repository.
	 * 
	 */
	public DataSource getEnvironment() {
		return _myEnvironment;
	}
	
	/**
	 * @return the name of the project
	 */
	public String getName() {
	    return _myEnvironment.getShortName();
	}
	
	/**
	 * @return true iff no other project in this project's client preferences has the name name.  
	 * @effect sets the name of this project to be name unless another project in this project's 
	 *          client preferences already has that name.  
	 */
	public boolean setName(String name) {
	    for (ProjectPreferences pp : _clientPreferences.getProjectPreference()) {
	        if ((pp.getName().equals(name)) && (pp != this))
	            return false;
	    }
	    _myEnvironment.setShortName(name);
	    return true;
	}

	/**
	 * @return a specific repository configuration
	 * @param shortName: the name of the desired repository configuration
	 */
	public DataSource getDataSource(String shortName) {
		for (DataSource ds : _dataSources) {
			if (ds.getShortName().toLowerCase().trim().equals(shortName.toLowerCase().trim())) {
				return ds;
			}
		}
		return null;
	}

	/**
	 * Removes a repository configuration.
	 * @param sourceToRemove: the repository configuration to remove
	 */
	public void removeDataSource(DataSource sourceToRemove) {
		_dataSources.remove(sourceToRemove);
	}

	/**
	 * @return the full configuration.
	 */
	public ClientPreferences getClientPreferences() {
		return _clientPreferences;
	}

	/**
	 * @return the location on disk of the specific repository in this project
	 * @param source: the repository for which to find the location 
	 */
	public String getProjectCheckoutLocation(DataSource source) {
		String basePath = getClientPreferences().getTempDirectory();
		if (!basePath.endsWith(File.separator))
			basePath += File.separator;

		return basePath + getName() + "_" + source.getShortName();
	}
}
