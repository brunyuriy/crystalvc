package crystal.model;

import java.awt.Image;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
		
		public static String SAME = "SAME";
		public static String AHEAD = "AHEAD";
		public static String BEHIND = "BEHIND";
		public static String MERGECLEAN = "MERGECLEAN";
		public static String MERGECONFLICT = "MERGECONFLICT";
		public static String COMPILECONFLICT = "COMPILECONFLICT";
		public static String TESTCONFLICT = "TESTCONFLICT";
		public static String PENDING = "PENDING";
		public static String ERROR = "ERROR";
		
		
		private static String PATH = "/crystal/client/images/";
		private static String SIZE32 = "32X32/";
		private static String SIZE16 = "16X16/";

		private static String CAPABLE_MUST = "must/";
		private static String CAPABLE_MIGHT = "might/";
		private static String CAPABLE_CANNOT = "cannot/";
		
		private static String WHEN_NOW = "must/";
		private static String WHEN_LATER = "cannot/";
		
		private static Map<String, String> ICON_ADDRESSES = new HashMap<String, String>();
		static {
			ICON_ADDRESSES.put("SAME", "same.png");
			ICON_ADDRESSES.put("AHEAD","ahead.png");
			ICON_ADDRESSES.put("BEHIND", "behind.png");
			ICON_ADDRESSES.put("MERGECLEAN", "merge.png");
			ICON_ADDRESSES.put("MERGECONFLICT", "mergeconflict.png");
			ICON_ADDRESSES.put("COMPILECONFLICT", "compileconflict.png"); 
			ICON_ADDRESSES.put("TESTCONFLICT", "testconflict.png");
			ICON_ADDRESSES.put("PENDING", "clock.png"); 
			ICON_ADDRESSES.put("ERROR", "error.png");
		}

//		private final ImageIcon _icon;
//		private final Image _image;
		private final String _name;
		
		private String _committers;
		private When _when;
		private Capable _capable;
		private Ease _ease;
		private Relationship _consequences;

		public Relationship(String name) {
			
//			_icon = new ImageIcon(Constants.class.getResource(iconAddress));
//			_image = (new ImageIcon(Constants.class.getResource(iconAddress.replaceAll("32", "16")))).getImage();
			_name = name.toUpperCase();
			if (ICON_ADDRESSES.get(_name) == null)
				throw new RuntimeException("Trying to create an invalid Relationship");
		}
				
		public String getName() {
			return _name;
		}

		public ImageIcon getIcon() {
			String iconAddress = PATH + SIZE32;
			if (_capable == Capable.MUST)
				iconAddress += CAPABLE_MUST;
			else if (_capable == Capable.MIGHT)
				iconAddress += CAPABLE_MIGHT;
			else if (_capable == Capable.CANNOT)
				iconAddress += CAPABLE_CANNOT;
			else 
				iconAddress += CAPABLE_MUST;
			iconAddress += ICON_ADDRESSES.get(_name);
			return (new ImageIcon(Constants.class.getResource(iconAddress)));
		}
		
		public Image getImage() {
			String imageAddress = PATH + SIZE16 + ICON_ADDRESSES.get(_name);			
			return (new ImageIcon(Constants.class.getResource(imageAddress)).getImage());
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

		public void setConsequences(Relationship consequences) {
			_consequences = consequences;
		}

		public Relationship getConsequences() {
			return _consequences;
		}
		
		public String getToolTipText() {
			//TODO compute the action
			String answer = "Action:\n";
			if (_committers != null)
				answer += _committers + "\n";
			if (_consequences != null)
				answer += "consequences: new relationship will be: " + _consequences.getName() + "\n";
			return answer.trim();
		}

		@Override
		public int compareTo(Relationship other) {
			if (other == null) return 1;
			return (this.getIntRepresentation() - other.getIntRepresentation());
		}
		
		private int getIntRepresentation() {
			if (_name.equals(ERROR)) return 1;
			if (_name.equals(PENDING)) return 2;
			if (_name.equals(SAME)) return 3;
			if (_name.equals(BEHIND)) return 4;
			if (_name.equals(AHEAD)) return 5;
			if (_name.equals(MERGECLEAN)) return 6;
			if (_name.equals(TESTCONFLICT)) return 7;
			if (_name.equals(COMPILECONFLICT)) return 8;
			if (_name.equals(MERGECONFLICT)) return 9;
			else
				return 0;
		}
		
		public static Relationship getDominant(Collection<RelationshipResult> statesAndRelationships) {
			Relationship dominant = null;
			for (RelationshipResult currentStateAndRelationship : statesAndRelationships) {
				Relationship currentRelationship = null;
				if ((currentStateAndRelationship.getRelationship().getName().equals(PENDING)) && (currentStateAndRelationship.getLastRelationship() != null)) { 
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
