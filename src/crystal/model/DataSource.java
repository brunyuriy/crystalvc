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

	// _shortName 
	private String _shortName;
	
	// the path to the remote repository
	private String _cloneString;
	
	// the path to the local clone of the remote repository
	private String _localString;
	
	// the kind of this repo
	private RepoKind _repoKind;

	public DataSource(String shortName, String cloneString, String localString, RepoKind repoKind) {
		assert shortName != null;
		assert cloneString != null;
		assert localString != null;
		assert repoKind != null;

		_shortName = shortName;
		_cloneString = cloneString;
		_localString = localString;
		_repoKind = repoKind;
	}

	public String getShortName() {
		return _shortName;
	}

	public String getCloneString() {
		return _cloneString;
	}
	
	public String getLocalString() {
		return _localString;
	}

	public RepoKind getKind() {
		return _repoKind;
	}

	public void setKind(RepoKind kind) {
		_repoKind = kind;
	}

	public void setShortName(String name) {
		_shortName = name;
	}

	public void setCloneString(String name) {
		_cloneString = name;
	}
	
	public void setLocalString(String name) {
		_localString = name;
	}	

	@Override
	public String toString() {
		return getShortName() + "- " + getKind() + ": " + getCloneString();
	}
}
