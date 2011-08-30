package crystal.model;

import java.util.HashMap;
import java.util.Map;


/**
 * Represents the result of a computation of a local state of a repository
 * LocalStateResult is immutable
 * 
 * @author brun
 */
public class LocalStateResult implements Result {

    public static String HG_UNCHECKPOINTED = "HG_UNCHECKPOINTED";
    public static String HG_MUST_RESOLVE = "HG_MUST_RESOLVE";
    public static String GIT_UNCHECKPOINTED = "GIT_UNCHECKPOINTED";
    public static String GIT_MUST_RESOLVE = "GIT_MUST_RESOLVE";
    
    public static String ALL_CLEAR = "ALL CLEAR";
    public static String PENDING = "PENDING";
    public static String ERROR = "ERROR";
    public static String BUILD = "BUILD";
    public static String TEST = "TEST";
    
    private static Map<String, String> actions = new HashMap<String, String>();
    static {
        actions.put(HG_UNCHECKPOINTED, "hg commit");
        actions.put(HG_MUST_RESOLVE, "hg merge");
        actions.put(GIT_UNCHECKPOINTED, "git commit");
        actions.put(GIT_MUST_RESOLVE, "git merge");
        actions.put(ALL_CLEAR, "");
        actions.put(PENDING, "");
        actions.put(ERROR, "");
        actions.put(BUILD, "");
        actions.put(TEST, "");
    }		

    // the local state name
    private String _name;
    
    // the action that can be taken for this local state
    private String _action;
    
    // the error message associated with this ERROR relationship
    private String _errorMessage;
    
    // the previous state
    private String _lastState;
    
    // the previous state's action
    private String _lastAction;

    // the last error message 
    private String _lastErrorMessage;


    /**
     * Creates a new LocalStateResult
     * 
     * @param source: the repository for which this result will pertain
     * @param name: the String representation of the state
     * @param lastState: the previous state
     * @throws IllegalArgumentException if input String is invalid or name is not
     * contained in the set of actions.
     */
    public LocalStateResult(DataSource source, String name, String lastState, String lastAction, String lastErrorMessage) {
        //ValidInputChecker.checkNullInput(source);
        //ValidInputChecker.checkValidStringInput(name);
        //ValidInputChecker.checkValidStringInput(lastState);
        //ValidInputChecker.checkValidStringInput(lastAction);
        //ValidInputChecker.checkValidStringInput(lastErrorMessage);
    	//ValidInputChecker.checkStringInSet(name, actions.keySet());
        
    	if (name.startsWith(ERROR)) {
            _errorMessage = name.substring(ERROR.length());
            _name = ERROR;
        } else {
            assert(actions.keySet().contains(name));
            _name = name.toUpperCase();
            _action = actions.get(_name);
        }
        _source = source;
        _lastState = lastState;
        _lastAction = lastAction;
        _lastErrorMessage = lastErrorMessage;
    }

    
	/**
	 * Compare this object with another object
	 * @param o other object to be compared with this object
	 * @return true if they are same; otherwise return false
	 */
    public boolean equals(Object o){
		if (o != null && getClass() == o.getClass()){
			LocalStateResult other = (LocalStateResult) o;
			return this._source.equals(other._source) && 
			(this._name == null && other._name == null || this._name.equals(other._name));

		} else {
			return false;
		}
    }

    /**
     * @return the action to perform
     */
    public String getAction() {
        return _action;
    }
    
    /**
     * @return the last state's action
     */
    public String getLastAction() {
        return _lastAction;
    }

    /**
     * @return the String representation of this state (same as .toString())
     */
    public String getName() {
        return _name;
    }

    // the repository for which this LocalStateResult holds
	private final DataSource _source;
	
	/**
	 * @return the tool tip, which is the error message, if one exists
	 */
	public String getErrorMessage() {
	    return _errorMessage;
	}
	
	/**
     * @return the last state's tool tip, which is the error message, if one exists
     */
    public String getLastErrorMessage() {
        return _lastErrorMessage;
    }

	@Override
	/**
	 * A String representation of this result
	 */
	public String toString() {
		return "LocalStateResult - " + _source.getShortName() + " state: " + _name + " and last state: " + _lastState + ".";
	}

	/**
	 * @return the current state
	 */
	public String getLocalState() {
		return _name;
	}
	
	/**
	 * @return the previous state
	 */
	public String getLastLocalState() {
		return _lastState;
	}
}
