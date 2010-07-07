package crystal.model;

public class DataSource {

	public enum RepoKind {
		GIT, HG
	}

	private String _shortName;
	private String _cloneString;
	private RepoKind _repoKind;

	public DataSource(String shortName, String cloneString, RepoKind repoKind) {
		assert shortName != null;
		assert cloneString != null;
		assert repoKind != null;

		_shortName = shortName;
		_cloneString = cloneString;
		_repoKind = repoKind;
	}

	public String getShortName() {
		return _shortName;
	}

	public String getCloneString() {
		return _cloneString;
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

	@Override
	public String toString() {
		return getShortName() + "- " + getKind() + ": " + getCloneString();
	}
}
