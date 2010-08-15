package crystal.model;

import java.awt.Image;
import java.util.Collection;

import javax.swing.ImageIcon;

import crystal.Constants;

/**
 * Represents a conflict detection question.
 * 
 * @author brun
 * 
 */

public class ConflictResult {
	
	/**
	 * Represents a conflict detection answer.
	 * 
	 * @author brun
	 * 
	 * ResltStatus is interned
	 */
	
	public static class ResultStatus implements Comparable<ResultStatus>{
		public static ResultStatus SAME = new ResultStatus("/crystal/client/images/32X32/same.png", "In Sync");
		public static ResultStatus AHEAD = new ResultStatus("/crystal/client/images/32X32/ahead.png", "Publish");
		public static ResultStatus BEHIND = new ResultStatus("/crystal/client/images/32X32/behind.png", "Sync");
		public static ResultStatus MERGECLEAN = new ResultStatus("/crystal/client/images/32X32/merge.png", "Sync");
		public static ResultStatus MERGECONFLICT = new ResultStatus("/crystal/client/images/32X32/mergeconflict.png", "Sync + Resolve"); 
		public static ResultStatus COMPILECONFLICT = new ResultStatus("/crystal/client/images/32X32/compileconflict.png", "Sync + Resolve"); 
		public static ResultStatus TESTCONFLICT = new ResultStatus("/crystal/client/images/32X32/testconflict.png", "Sync + Resolve");
		public static ResultStatus PENDING = new ResultStatus("/crystal/client/images/32X32/clock.png", ""); 
		public static ResultStatus ERROR = new ResultStatus("/crystal/client/images/32X32/error.png", "");
		public static ResultStatus TWOHEADED = new ResultStatus("/crystal/client/images/32X32/twohead.png", "Resolve");


		private final ImageIcon _icon;
		private final Image _image;
		private final String _action;

		private ResultStatus(String iconAddress, String action) {
			_icon = new ImageIcon(Constants.class.getResource(iconAddress));
			_image = (new ImageIcon(Constants.class.getResource(iconAddress.replaceAll("32", "16")))).getImage();
			_action = action;
		}
				
		public String getAction() {
			return _action;
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
			if (this == COMPILECONFLICT) return 8;
			if (this == MERGECONFLICT) return 9;
			if (this == TWOHEADED) return 10;
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
