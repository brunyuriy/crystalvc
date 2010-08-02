package crystal.model;

import javax.swing.ImageIcon;

import crystal.Constants;

public class ConflictResult {

	public enum ResultStatus {
		SAME("/crystal/client/images/32X32/same.png"), AHEAD("/crystal/client/images/32X32/ahead.png"), BEHIND(
				"/crystal/client/images/32X32/behind.png"), MERGECLEAN("/crystal/client/images/32X32/merge.png"), MERGECONFLICT(
				"/crystal/client/images/32X32/mergeconflict.png"), COMPILECONLFICT("/crystal/client/images/32X32/compileconflict.png"), TESTCONFLICT(
				"/crystal/client/images/32X32/testconflict.png"), PENDING("/crystal/client/images/clock.png"), ERROR(
				"/crystal/client/images/32X32/error.png");

		private final ImageIcon _icon;

		ResultStatus(String iconAddress) {
			_icon = new ImageIcon(Constants.class.getResource(iconAddress));
		}

		public ImageIcon getIcon() {
			return _icon;
		}
	}

	private final DataSource _source;
	private final ResultStatus _status;
	private final ResultStatus _lastStatus;

	public ConflictResult(DataSource source, ResultStatus status, ResultStatus lastStatus) {
		_source = source;
		_status = status;
		_lastStatus = lastStatus;
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

	public ResultStatus getLastStatus() {
		return _lastStatus;
	}
}
