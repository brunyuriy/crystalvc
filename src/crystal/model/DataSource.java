package crystal.model;

import crystal.util.ValidInputChecker;

/**
 * Describes a repository.
 * 
 * @author rtholmes
 * @author brun
 * 
 */
public class DataSource implements Cloneable{

    public enum RepoKind {
        GIT, HG;
    }

    // whether or not this source is enabled
    private boolean _enabled;

    // whether or not this source should be hidden
    private boolean _hide;

    // _shortName
    private String _shortName;

    // the path to the remote repository
    private String _cloneString;

    // the shortName of the parent
    private String _parent;

    // the kind of this repo
    private RepoKind _repoKind;

    // optional argument to pass to hg for --remotecmd in case the remote hg path is not just "hg"
    private String _remoteCmd = null;

    // optional command for compiling a project
    private String _compileCommand = null;

    // optional command for compiling a project
    private String _testCommand = null;

    private RevisionHistory _history;
    private RevisionHistory _oldHistory;


    /**
     * Creates a new DataSource
     * @param shortName: the name of this repository
     * @param cloneStrong: the path to the remote repository
     * @param repoKind: the kind of this repository
     * @param hide: false if this repository should not be shown in Crystal's view
     * @param parent: the name of the parent repository
     */
    public DataSource(String shortName, String cloneString, RepoKind repoKind, boolean hide, String parent) {
        //ValidInputChecker.checkValidStringInput(shortName);
        //ValidInputChecker.checkValidStringInput(cloneString);
        //ValidInputChecker.checkNullInput(repoKind);

        assert shortName != null;
        assert cloneString != null;
        // assert localString != null;
        assert repoKind != null;

        _enabled = true;
        _shortName = shortName.replace(' ', '_').replace('\\', '_').replace('/', '_').replace(':', '_').replace(';', '_');
        _cloneString = cloneString;
        _repoKind = repoKind;
        _hide = hide;
        setParent(parent);
        _history = null;
        _oldHistory = null;
    }

    /**
     * Return clone of this object.
     */
    public DataSource clone() {
        try {  
            DataSource clone = (DataSource) super.clone();

            if (_history != null)
                clone._history = _history.clone();
            if (_oldHistory != null)
                clone._oldHistory = _oldHistory.clone();

            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Compare this object with another object
     * @param o other object to be compared with this object
     * @return true if they have same short name; otherwise return false
     */
    public boolean equals(Object o){
        if (o != null && getClass() == o.getClass()){
            DataSource other = (DataSource) o;
            return this._shortName == null && other._shortName == null 
                    || this._shortName.equals(other._shortName);
        } else {
            return false;
        }
    }

    /**
     * Sets the history of this repository and moves the old history to _oldHistory
     * @param history: the history 
     */
    public void setHistory(RevisionHistory history) {
        //ValidInputChecker.checkNullInput(history);
        _oldHistory = _history;
        _history = history;
    }

    /**
     * @return this repository's history
     */
    public RevisionHistory getHistory() {
        return _history;
    }

    /**
     * @return true iff the history has changed recently (if _history and _oldHistory are logically different)
     */
    public boolean hasHistoryChanged() {
        if (_history == null)
            return true;
        return (!(_history.equals(_oldHistory)));
    }

    /**
     * Sets the remoteHg command for this repository
     * @param remoteHg : the remoteHg command
     */
    public void setRemoteCmd(String remoteCmd) {
        _remoteCmd = remoteCmd;
    }

    /**
     * @return the remoteHg command
     */
    public String getRemoteCmd() {
        return _remoteCmd;
    }

    /**
     * Sets the command to compile the code in this repository
     * @param compileCommand: the compile command
     */
    public void setCompileCommand(String compileCommand) {
        _compileCommand = compileCommand;
    }

    /**
     * @return the compile command for this repository
     */
    public String getCompileCommand() {
        return _compileCommand;
    }

    /**
     * Sets the command to run the tests in this repository
     * @param testCommand: the test command
     */
    public void setTestCommand(String testCommand) {
        _testCommand = testCommand;
    }

    /**
     * @return the test command for this repository
     */
    public String getTestCommand() {
        return _testCommand;
    }

    /**
     * @return the name of this repository
     */
    public String getShortName() {
        return _shortName;
    }

    /**
     * Sets this repository's name
     * @param name: this repository's name
     */
    public void setShortName(String name) {
        ValidInputChecker.checkValidStringInput(name);
        _shortName = name.replace(' ', '_').replace('\\', '_').replace('/', '_').replace(':', '_').replace(';', '_');
    }


    /**
     * @return the path to the remote repository
     */
    public String getCloneString() {
        return _cloneString;
    }

    /**
     * Set whether this repository is enabled (or ignored by Crystal)
     * @param enabled: true iff the repository is becoming enabled (regardless of what it was before)
     */
    public void setEnabled(boolean enabled) {
        _enabled = enabled;
    }

    /**
     * @return true iff this repository is enabled
     */
    public boolean isEnabled() {
        return _enabled;
    }

    /**
     * @return true iff this repository is hidden
     */
    public boolean isHidden() {
        return _hide;
    }

    /**
     * Set whether this repository is hidden (= not shown by Crystal)
     * @param hide: true iff the repository is becoming hidden (regardless of what it was before)
     */
    public void hide(boolean hide) {
        _hide = hide;
    }

    /**
     * @return this repository's parent's name
     */
    public String getParent() {
        if (_parent == null)
            return "";
        else
            return _parent;
    }

    /**
     * Sets the parent name of this repository
     * @param parent: the shortName of the parent
     */
    public void setParent(String parent) {
        if ((parent == null) || (parent.trim().equals("")))
            _parent = null;
        else
            _parent = parent;
    }

    /**
     * @return the kind of this repository
     */
    public RepoKind getKind() {
        return _repoKind;
    }

    /**
     * Sets the kind of this repository
     * @param kind: the kind of this repository
     */
    public void setKind(RepoKind kind) {
        //ValidInputChecker.checkNullInput(kind);
        _repoKind = kind;
    }

    /**
     * Sets this repository's remote path
     * @param name: this repository's remote path
     */
    public void setCloneString(String name) {
        ValidInputChecker.checkValidStringInput(name);
        _cloneString = name;
    }

    /**
     * Converts this repository to a String representation
     */
    @Override
    public String toString() {
        return getShortName() + "_" + getKind() + "_" + getCloneString();
    }
}
