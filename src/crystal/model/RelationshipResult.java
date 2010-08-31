package crystal.model;

import java.awt.Image;
import java.util.Collection;

import javax.swing.ImageIcon;

import crystal.Constants;
import crystal.model.RevisionHistory.Capable;
import crystal.model.RevisionHistory.Ease;
import crystal.model.RevisionHistory.When;

/**
 * Represents a conflict detection question.
 * 
 * @author brun
 * 
 */

public class RelationshipResult implements Result {
	
	
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
		
		private String _committers;
		private When _when;
		private Capable _capable;
		private Ease _ease;
		private Relationship _consequeses;

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
		
		public void setCommitters(String committers) {
			_committers = committers;
		}

		public String getCommitters() {
			return _committers;
		}

		public void setWhen(When when) {
			_when = when;
		}

		public When getWhen() {
			return _when;
		}

		public void setCapable(Capable capable) {
			_capable = capable;
		}

		public Capable getCapable() {
			return _capable;
		}

		public void setEase(Ease ease) {
			_ease = ease;
		}

		public Ease getEase() {
			return _ease;
		}

		public void setConsequeses(Relationship consequeses) {
			_consequeses = consequeses;
		}

		public Relationship getConsequeses() {
			return _consequeses;
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
		
		public static Relationship getDominant(Collection<RelationshipResult> statesAndRelationships) {
			Relationship dominant = null;
			for (RelationshipResult currentStateAndRelationship : statesAndRelationships) {
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
	
	private final DataSource _source;
	private final Relationship _relationship;
	private final Relationship _lastRelationship;
	
	public RelationshipResult(DataSource source, Relationship relationship, Relationship lastRelationship) {
		_source = source;
		_relationship = relationship;
		_lastRelationship = lastRelationship;
	}

	@Override
	public String toString() {
		return "StateAndRelationship - " + _source.getShortName() + " Relationship: " + _relationship + " and last relationship: " + _lastRelationship + ".";
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
}
