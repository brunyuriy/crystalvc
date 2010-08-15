package crystal.model;

/**
 * Describes a repository.
 * 
 * @author rtholmes & brun
 * 
 */
public class DataSource {

	public enum RepoKind {
		GIT, HG
	}
	
	// whether or not this source is enabled
	private boolean _enabled;

	// _shortName
	private String _shortName;

	// the path to the remote repository
	private String _cloneString;
	
	// whether this repository is a Master
	private boolean _master;

	// the path to the local clone of the remote repository
	// private String _localString;

	// the kind of this repo
	private RepoKind _repoKind;
	
	// optional argument to pass to hg for --remotecmd in case the remote hg path is not just "hg"
	private String _remoteHg = null;

	// Create a new, enabled source.  
	public DataSource(String shortName, String cloneString, RepoKind repoKind) {
		assert shortName != null;
		assert cloneString != null;
		// assert localString != null;
		assert repoKind != null;

		_enabled = true;
		_shortName = shortName;
		_cloneString = cloneString;
		// _localString = localString;
		_repoKind = repoKind;
		_master = shortName.toLowerCase().trim().equals("master");
	}
	
	public void setRemoteHg(String remoteHg) {
		_remoteHg = remoteHg;
	}
	
	public String getRemoteHg() {
		return _remoteHg;
	}

	public String getShortName() {
		return _shortName;
	}

	public String getCloneString() {
		return _cloneString;
	}
	
	public void setEnabled(boolean enabled) {
		_enabled = enabled;
	}

	public boolean isEnabled() {
		return _enabled;
	}
	
	// public String getLocalString() {
	// return _localString;
	// }

	public RepoKind getKind() {
		return _repoKind;
	}

	public void setKind(RepoKind kind) {
		_repoKind = kind;
	}

	public void setShortName(String name) {
		_shortName = name;
		_master = name.toLowerCase().trim().equals("master");
	}

	public void setCloneString(String name) {
		_cloneString = name;
	}

	// public void setLocalString(String name) {
	// _localString = name;
	// }

	@Override
	public String toString() {
		return getShortName() + "_" + getKind() + "_" + getCloneString();
	}
}
