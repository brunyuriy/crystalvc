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

public class StateAndRelationship {
	
	/**
	 * Represents a local state
	 */
	
	public static class LocalState {
		public static LocalState UNCHECKPOINTED = new LocalState("hg commit", "UNCHECKPOINTED");
		public static LocalState MUST_RESOLVE = new LocalState("hg fetch", "MUST RESOLVE");
		public static LocalState ALL_CLEAR = new LocalState("", "ALL CLEAR");
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
	
	/**
	 * Represents a conflict detection answer.
	 * 
	 * @author brun
	 * 
	 * ResultStatus is interned
	 */
	
	public static class Relationship implements Comparable<Relationship>{
		public static Relationship SAME = new Relationship("/crystal/client/images/32X32/same.png", "SAME");
		public static Relationship AHEAD = new Relationship("/crystal/client/images/32X32/ahead.png", "AHEAD");
		public static Relationship BEHIND = new Relationship("/crystal/client/images/32X32/behind.png", "BEHIND");
		public static Relationship MERGECLEAN = new Relationship("/crystal/client/images/32X32/merge.png", "MERGE");
		public static Relationship MERGECONFLICT = new Relationship("/crystal/client/images/32X32/mergeconflict.png", "CONFLICT");
		public static Relationship COMPILECONFLICT = new Relationship("/crystal/client/images/32X32/compileconflict.png", "COMPILE CONFLICT"); 
		public static Relationship TESTCONFLICT = new Relationship("/crystal/client/images/32X32/testconflict.png", "TEST CONFLICT");
		public static Relationship PENDING = new Relationship("/crystal/client/images/32X32/clock.png", "pending"); 
		public static Relationship ERROR = new Relationship("/crystal/client/images/32X32/error.png", "error");
//		public static ResultStatus TWOHEADED = new ResultStatus("/crystal/client/images/32X32/twohead.png", "Resolve");


		private final ImageIcon _icon;
		private final Image _image;
		private final String _name;

		private Relationship(String iconAddress, String name) {
			_icon = new ImageIcon(Constants.class.getResource(iconAddress));
			_image = (new ImageIcon(Constants.class.getResource(iconAddress.replaceAll("32", "16")))).getImage();
			_name = name;
		}
				
		public String getName() {
			return _name;
		}

		public ImageIcon getIcon() {
			return _icon;
		}
		
		public Image getImage() {
			return _image;
		}
		
		@Override
		public int compareTo(Relationship other) {
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
//			if (this == TWOHEADED) return 10;
			else
				return 0;
		}
		
		public static Relationship getDominant(Collection<StateAndRelationship> statesAndRelationships) {
			Relationship dominant = null;
			for (StateAndRelationship currentStateAndRelationship : statesAndRelationships) {
				Relationship currentRelationship = null;
				if ((currentStateAndRelationship.getRelationship() == PENDING) && (currentStateAndRelationship.getLastRelationship() != null)) { 
					// if it's pending, use whatever value it had last time
					currentRelationship = currentStateAndRelationship.getLastRelationship();
				} else {
					currentRelationship = currentStateAndRelationship.getRelationship();
				}
				if (currentRelationship.compareTo(dominant) > 0) {
					dominant = currentRelationship;
				}
			}
			return dominant;
		}
		
		@Override
		public String toString() {
			return _name;
		}		
	}
	
	public enum When {
		NOW, LATER
	}

		
//	public static When getWhen(ResultStatus relation, ResultStatus parent) {
//		if (relation == BEHIND) {
//			if (parent == BEHIND)
//				return When.NOW;
//			else if (parent == SAME)
//				return When.LATER;
//			else 
//				return null;
//		}
//		//			if ()
//		return null;	
//	}

	private final DataSource _source;
	private final Relationship _relationship;
	private final Relationship _lastRelationship;
	
	private final LocalState _state;
	private final LocalState _lastState;

	public StateAndRelationship(DataSource source, Relationship relationship, Relationship lastRelationship, LocalState state, LocalState lastState) {
		_source = source;
		_relationship = relationship;
		_lastRelationship = lastRelationship;
		_state = state;
		_lastState = lastState;
	}

	@Override
	public String toString() {
		return "StateAndRelationship - " + _source.getShortName() + " state: " + _state + " and last state: " + _lastState + ". " +
			"Relationship: " + _relationship.toString() + " and last relationship: " + _lastRelationship.toString() + ".";
	}

	public DataSource getDataSource() {
		return _source;
	}

	public Relationship getRelationship() {
		return _relationship;
	}

	public Relationship getLastRelationship() {
		return _lastRelationship;
	}
	
	public LocalState getLocalState() {
		return _state;
	}
	
	public LocalState getLastLocalState() {
		return _lastState;
	}
}
