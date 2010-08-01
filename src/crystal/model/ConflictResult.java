package crystal.model;

import javax.swing.ImageIcon;

public class ConflictResult {

	public enum ResultStatus {
		SAME("C:\\temp\\conflictClient\\same.png"),
		AHEAD("C:\\temp\\conflictClient\\ahead.png"), 
		BEHIND("C:\\temp\\conflictClient\\behind.png"), 
		MERGECLEAN("C:\\temp\\conflictClient\\merge.png"), 
		MERGECONFLICT("C:\\temp\\conflictClient\\mergeconflict.png"), 
		COMPILECONLFICT("C:\\temp\\conflictClient\\compileconflict.png"),
		TESTCONFLICT("C:\\temp\\conflictClient\\testconflict.png"),
		PENDING("C:\\temp\\conflictClient\\clock.png"), 
		ERROR("C:\\temp\\conflictClient\\error.png");
		
		private final ImageIcon _icon;
	    ResultStatus(String iconAddress) {
	    	_icon = new ImageIcon(iconAddress);
	    }
		public ImageIcon getIcon() {
			return _icon; 
		}
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
