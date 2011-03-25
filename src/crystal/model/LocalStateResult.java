package crystal.model;

/**
 * Represents the result of a computation of a local state of a repository
 * LocalStateResult is immutable
 * 
 * @author brun
 */
public class LocalStateResult implements Result {

	/**
	 * Represents a local state of a repository
	 * LocalState is immutable
	 * 
	 * @author brun
	 */
	public static class LocalState {
		public static LocalState UNCHECKPOINTED = new LocalState("hg commit", "UNCHECKPOINTED");
		public static LocalState MUST_RESOLVE = new LocalState("hg fetch", "MUST RESOLVE");
		public static LocalState ALL_CLEAR = new LocalState("", "ALL CLEAR");
		public static LocalState PENDING = new LocalState("", "PENDING");
		public static LocalState ERROR = new LocalState("", "ERROR");
		public static LocalState BUILD = new LocalState("", "BUILD");
		public static LocalState TEST = new LocalState("", "TEST");
		
		// the String representation of the local state
		private final String _name;
		
		// the action to perform in this state
		private final String _action;

		/**
		 * Creates a new LocalState
		 * 
		 * @param action: the action to perform
		 * @param name: the String representation of the state
		 */
		private LocalState(String action, String name) {
			_action = action;
			_name = name;
		}

		/**
		 * @return the action to perform
		 */
		public String getAction() {
			return _action;
		}

		/**
		 * @return the String representation of this state (same as .toString())
		 */
		public String getName() {
			return _name;
		}

		@Override
		/**
		 * @return the String representation of this state
		 */
		public String toString() {
			return _name;
		}

	}
	
	// the repository for which this LocalStateResult holds
	private final DataSource _source;
	
	// the current state
	private final LocalState _state;
	
	// the previous state
	private final LocalState _lastState;

	/**
	 * Creates a new LocalStateResult
	 * 
	 * @param source: the repository for which this result will pertain
	 * @param state: the current state
	 * @param lastState: the previous state
	 */
	public LocalStateResult(DataSource source, LocalState state, LocalState lastState) {
		_source = source;
		_state = state;
		_lastState = lastState;
	}

	@Override
	/**
	 * A String representation of this result
	 */
	public String toString() {
		return "LocalStateResult - " + _source.getShortName() + " state: " + _state + " and last state: " + _lastState + ".";
	}

	/**
	 * @return the current state
	 */
	public LocalState getLocalState() {
		return _state;
	}
	
	/**
	 * @return the previous state
	 */
	public LocalState getLastLocalState() {
		return _lastState;
	}
}
