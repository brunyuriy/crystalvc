package crystal.model;

import javax.swing.ImageIcon;

public class ConflictResult {

	public enum ResultStatus {
		SAME("same.png"),
		AHEAD("ahead.png"), 
		BEHIND("behind.png"), 
		MERGECLEAN("merge.png"), 
		MERGECONFLICT("mergeconflict.png"), 
		COMPILECONLFICT("compileconflict.png"),
		TESTCONFLICT("testconflict.png"),
		PENDING("clock.png"), 
		ERROR("error.png");
		
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
