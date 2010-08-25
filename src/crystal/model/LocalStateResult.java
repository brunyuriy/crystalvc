package crystal.model;


public class LocalStateResult {

	/**
	 * Represents a local state
	 */

	public static class LocalState {
		public static LocalState UNCHECKPOINTED = new LocalState("hg commit", "UNCHECKPOINTED");
		public static LocalState MUST_RESOLVE = new LocalState("hg fetch", "MUST RESOLVE");
		public static LocalState ALL_CLEAR = new LocalState("all clear", "ALL CLEAR");
		public static LocalState PENDING = new LocalState("", "PENDING");
		public static LocalState ERROR = new LocalState("", "ERROR");

		private final String _name;
		private final String _action;

		private LocalState(String action, String name) {
			_action = action;
			_name = name;
		}

		public String getAction() {
			return _action;
		}

		public String getName() {
			return _name;
		}

		@Override
		public String toString() {
			return _name;
		}

	}
	
	private final DataSource _source;
	private final LocalState _state;
	private final LocalState _lastState;

	public LocalStateResult(DataSource source, LocalState state, LocalState lastState) {
		_source = source;
		_state = state;
		_lastState = lastState;
	}

	@Override
	public String toString() {
		return "LocalStateResult - " + _source.getShortName() + " state: " + _state + " and last state: " + _lastState + ".";
	}

	public LocalState getLocalState() {
		return _state;
	}
	
	public LocalState getLastLocalState() {
		return _lastState;
	}
}
