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
	
	// whether or not this source should be hidden
	private boolean _hide;

	// _shortName
	private String _shortName;

	// the path to the remote repository
	private String _cloneString;
	
	// whether this repository is a Master
//	private boolean _master;
	
	// the shortName of the parent
	private String _parent;

	// the path to the local clone of the remote repository
	// private String _localString;

	// the kind of this repo
	private RepoKind _repoKind;
	
	// optional argument to pass to hg for --remotecmd in case the remote hg path is not just "hg"
	private String _remoteHg = null;

	// optional command for compiling a project
	private String _compileCommand = null;
	
	// optional command for compiling a project
	private String _testCommand = null;
	
	private RevisionHistory _history;
	private RevisionHistory _oldHistory;


	// Create a new, enabled source.  
	public DataSource(String shortName, String cloneString, RepoKind repoKind, boolean hide, String parent) {
		assert shortName != null;
		assert cloneString != null;
		// assert localString != null;
		assert repoKind != null;

		_enabled = true;
		_shortName = shortName;
		_cloneString = cloneString;
		// _localString = localString;
		_repoKind = repoKind;
//		_master = shortName.toLowerCase().trim().equals("master");
		_hide = hide;
		setParent(parent);
		_history = null;
		_oldHistory = null;
	}
	
	public void setHistory(RevisionHistory history) {
		_oldHistory = _history;
		_history = history;
//		System.out.println(_shortName + " " + _history.size());
	}
		
	public RevisionHistory getHistory() {
		return _history;
	}
	
	public boolean hasHistoryChanged() {
		if (_history == null)
			return true;
		return (!(_history.equals(_oldHistory)));
	}
	
	public void setRemoteHg(String remoteHg) {
		_remoteHg = remoteHg;
	}
	
	public String getRemoteHg() {
		return _remoteHg;
	}
	
	public void setCompileCommand(String compileCommand) {
		_compileCommand = compileCommand;
	}
	
	public String getCompileCommand() {
		return _compileCommand;
	}
	
	public void setTestCommand(String testCommand) {
		_testCommand = testCommand;
	}
	
	public String getTestCommand() {
		return _testCommand;
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
	
//	public boolean isMaster() {
//		return _master;
//	}
	
	public boolean isHidden() {
		return _hide;
	}
	
	public void hide(boolean hide) {
		_hide = hide;
	}
	
	public String getParent() {
		if (_parent == null)
			return "";
		else
			return _parent;
	}
	
	public void setParent(String parent) {
		if ((parent == null) || (parent.trim().equals("")))
			_parent = null;
		else
			_parent = parent;
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
//		_master = name.toLowerCase().trim().equals("master");
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
