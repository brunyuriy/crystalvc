package crystal.model;

import java.awt.Image;
import java.util.Collection;

import javax.swing.ImageIcon;

import crystal.Constants;

public class ConflictResult {
	
	public static class ResultStatus implements Comparable<ResultStatus>{
		public static ResultStatus SAME = new ResultStatus("/crystal/client/images/32X32/same.png");
		public static ResultStatus AHEAD = new ResultStatus("/crystal/client/images/32X32/ahead.png");
		public static ResultStatus BEHIND = new ResultStatus("/crystal/client/images/32X32/behind.png");
		public static ResultStatus MERGECLEAN = new ResultStatus("/crystal/client/images/32X32/merge.png");
		public static ResultStatus MERGECONFLICT = new ResultStatus("/crystal/client/images/32X32/mergeconflict.png"); 
		public static ResultStatus COMPILECONLFICT = new ResultStatus("/crystal/client/images/32X32/compileconflict.png"); 
		public static ResultStatus TESTCONFLICT = new ResultStatus("/crystal/client/images/32X32/testconflict.png");
		public static ResultStatus PENDING = new ResultStatus("/crystal/client/images/32X32/clock.png"); 
		public static ResultStatus ERROR = new ResultStatus("/crystal/client/images/32X32/error.png");

		private final ImageIcon _icon;
		private final Image _image;

		private ResultStatus(String iconAddress) {
			_icon = new ImageIcon(Constants.class.getResource(iconAddress));
			_image = (new ImageIcon(Constants.class.getResource(iconAddress.replaceAll("32", "16")))).getImage();
		}

		public ImageIcon getIcon() {
			return _icon;
		}
		
		public Image getImage() {
			return _image;
		}
		
		@Override
		public int compareTo(ResultStatus other) {
			if (other == null) return 1;
			return (this.getIntRepresentation() - other.getIntRepresentation());
		}
		
		private int getIntRepresentation() {
			if (this == ERROR) return 1;
			if (this == PENDING) return 2;
			if (this == SAME) return 3;
			if (this == BEHIND) return 4;
			if (this == AHEAD) return 5;
			if (this == MERGECLEAN) return 6;
			if (this == TESTCONFLICT) return 7;
			if (this == COMPILECONLFICT) return 8;
			if (this == MERGECONFLICT) return 9;
			else
				return 0;
		}
		
		public static ResultStatus getDominant(Collection<ConflictResult> conflictResults) {
			ResultStatus dominant = null;
			for (ConflictResult currentConflictResult : conflictResults) {
				ResultStatus currentStatus = null;
				if ((currentConflictResult.getStatus() == PENDING) && (currentConflictResult.getLastStatus() != null)) { 
					// if it's pending, use whatever value it had last time
					currentStatus = currentConflictResult.getLastStatus();
				} else {
					currentStatus = currentConflictResult.getStatus();
				}
				if (currentStatus.compareTo(dominant) > 0) {
					dominant = currentStatus;
				}
			}
			return dominant;
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
