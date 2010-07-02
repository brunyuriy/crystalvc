package crystal.model;

public class ConflictResult {

	public enum ResultStatus {
		SAME, AHEAD, BEHIND, CONFLICT
	}

	private final DataSource _source;
	private final ResultStatus _status;

	public ConflictResult(DataSource source, ResultStatus status) {
		_source = source;
		_status = status;
	}

	@Override
	public String toString() {
		return "ConflictResult - " + _source.getShortName() + " status: " + _status;
	}

	public DataSource getDataSource() {
		return _source;
	}

	public ResultStatus getStatus() {
		return _status;
	}
}
