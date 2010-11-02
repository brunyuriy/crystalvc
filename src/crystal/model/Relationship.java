package crystal.model;

import java.awt.Image;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import crystal.Constants;
import crystal.model.DataSource.RepoKind;
import crystal.model.LocalStateResult.LocalState;
import crystal.model.RevisionHistory.Action;
import crystal.model.RevisionHistory.Capable;
import crystal.model.RevisionHistory.Ease;
import crystal.model.RevisionHistory.When;

/**
 * Represents a conflict detection question.
 * 
 * @author brun
 * 
 */

public class Relationship implements Result{

	/**
	 * Represents a conflict detection answer.
	 * 
	 * @author brun
	 * 
	 */

	public static String SAME = "SAME";
	public static String AHEAD = "AHEAD";
	public static String BEHIND = "BEHIND";
	public static String MERGECLEAN = "MERGE";
	public static String MERGECONFLICT = "CONFLICT";
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
		ICON_ADDRESSES.put(SAME, "same.png");
		ICON_ADDRESSES.put(AHEAD,"ahead.png");
		ICON_ADDRESSES.put(BEHIND, "behind.png");
		ICON_ADDRESSES.put(MERGECLEAN, "merge.png");
		ICON_ADDRESSES.put(MERGECONFLICT, "mergeconflict.png");
		ICON_ADDRESSES.put(COMPILECONFLICT, "compileconflict.png"); 
		ICON_ADDRESSES.put(TESTCONFLICT, "testconflict.png");
		ICON_ADDRESSES.put(PENDING, "clock.png"); 
		ICON_ADDRESSES.put(ERROR, "error.png");
	}

	//		private final ImageIcon _icon;
	//		private final Image _image;
	private final String _name;	
//	private final DataSource _source;
	
	private ImageIcon _icon;
	private Image _image;
	
	private String _committers;
	private When _when;
	private Capable _capable;
	private Ease _ease;
	private Relationship _consequences;
	
	private Action _action;

	private boolean _ready;

	public Relationship(String name, ImageIcon icon, Image image) {
		_name = name.toUpperCase();
		if (ICON_ADDRESSES.get(_name) == null)
			throw new RuntimeException("Trying to create an invalid Relationship");
		_ready = false;
//		_source = source;
		if (icon != null)
			_icon = icon;
		else
			_icon = new ImageIcon(Constants.class.getResource(PATH + SIZE32 + CAPABLE_MUST + ICON_ADDRESSES.get(PENDING)));
		if (image != null)
			_image = image;
		else
			_image = new ImageIcon(Constants.class.getResource(PATH + SIZE16 + CAPABLE_MUST + ICON_ADDRESSES.get(PENDING))).getImage();
	}

	public String getName() {
		return _name;
	}

	public void setReady() {
		_ready = true;
		_icon = computeIcon();
		_image = computeImage();
	}

	public boolean isReady() {
		return _ready;
	}

	/*
	 * Depricated because only images are now compared 
	private int getIconShape() {
		if (_name.equals(PENDING)) return 1;
		if (_name.equals(SAME)) return 2;
		if (_name.equals(ERROR)) return 3;
		if (_name.equals(AHEAD)) return 4;
		if (_name.equals(BEHIND)) return 5;
		if (_name.equals(MERGECLEAN)) return 6;
		if (_name.equals(TESTCONFLICT)) return 7;
		if (_name.equals(COMPILECONFLICT)) return 8;
		if (_name.equals(MERGECONFLICT)) return 9;
		else
			return 0;
	}
	*/

	private static int getIconShape(String address) {
		if (address.indexOf(ICON_ADDRESSES.get(PENDING)) > -1) return 1;
		else if (address.indexOf(ICON_ADDRESSES.get(SAME)) > -1) return 2;
		else if (address.indexOf(ICON_ADDRESSES.get(ERROR)) > -1) return 3;
		else if (address.indexOf(ICON_ADDRESSES.get(AHEAD)) > -1) return 4;
		else if (address.indexOf(ICON_ADDRESSES.get(BEHIND)) > -1) return 5;
		else if (address.indexOf(ICON_ADDRESSES.get(MERGECLEAN)) > -1) return 6;
		else if (address.indexOf(ICON_ADDRESSES.get(TESTCONFLICT)) > -1) return 7;
		else if (address.indexOf(ICON_ADDRESSES.get(COMPILECONFLICT)) > -1) return 8;
		else if (address.indexOf(ICON_ADDRESSES.get(MERGECONFLICT)) > -1) return 9;
		else
			return 0;
	}

	//return 2 if solid
	// 1 if unsaturated
	// 0 if hollow
	private int getIconFill() {
		if (_capable == Capable.MUST)
			return 2;
		else if (_capable == Capable.MIGHT)
			return 1;
		else if (_capable == Capable.CANNOT)
			return 0;
		else if (_capable == Capable.NOTHING)
			if (_when == When.NOW)
				return 2;
			else if (_when == When.LATER)
				return 0;
			else if (_when == When.NOTHING)
				return 0;
			else 
				// default icon
				return 2;
		else 
			// default icon
			return 2;
	}
	
	//return 2 if solid
	// 1 if unsaturated
	// 0 if hollow
	private static int getIconFill(String address) {
		if (address.indexOf(CAPABLE_MUST) > -1)
			return 2;
		else if (address.indexOf(CAPABLE_MIGHT) > -1)
			return 1;
		else if (address.indexOf(CAPABLE_CANNOT) > -1)
			return 0;
		else 
			// default icon
			return 2;
	}


	private ImageIcon computeIcon() {
		String iconAddress = PATH + SIZE32;

		if (getIconFill() == 2)
			iconAddress += CAPABLE_MUST;
		else if (getIconFill() == 1)
			iconAddress += CAPABLE_MIGHT;
		else if (getIconFill() == 0)
			iconAddress += CAPABLE_CANNOT;
		else
			// default icon
			iconAddress += CAPABLE_MUST;
		if (_ready)
			iconAddress += ICON_ADDRESSES.get(_name);
		else 
			iconAddress += ICON_ADDRESSES.get(PENDING);
		return (new ImageIcon(Constants.class.getResource(iconAddress)));
	}

	private Image computeImage() {
		//			String imageAddress = PATH + SIZE16 + ICON_ADDRESSES.get(_name);
		//			System.out.println(imageAddress);
		//			return (new ImageIcon(Constants.class.getResource(imageAddress)).getImage());

		String iconAddress = PATH + SIZE16;

		if (getIconFill() == 2)
			iconAddress += CAPABLE_MUST;
		else if (getIconFill() == 1)
			iconAddress += CAPABLE_MIGHT;
		else if (getIconFill() == 0)
			iconAddress += CAPABLE_CANNOT;
		else
			// default icon
			iconAddress += CAPABLE_MUST;
		if (_ready)
			iconAddress += ICON_ADDRESSES.get(_name);
		else 
			iconAddress += ICON_ADDRESSES.get(PENDING);

		//			System.out.println(iconAddress);

		return (new ImageIcon(Constants.class.getResource(iconAddress)).getImage());
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

	public void setConsequences(Relationship consequences) {
		_consequences = consequences;
	}

	public Relationship getConsequences() {
		return _consequences;
	}

	public void calculateAction(LocalState localState, Relationship parent) {
		if ((parent == null) || (localState == LocalState.PENDING))
			_action = Action.UNKNOWN;
		else if (parent.getName().equals(Relationship.SAME))
			_action = Action.NOTHING;
		else if (localState.equals(LocalState.MUST_RESOLVE))
			_action = Action.RESOLVE;
		else if (localState.equals(LocalState.UNCHECKPOINTED))
			_action = Action.CHECKPOINT;
		else if (parent.getName().equals(Relationship.AHEAD))
			_action = Action.PUBLISH;
		else if ((parent.getName().equals(Relationship.BEHIND)) || (parent.getName().equals(Relationship.MERGECLEAN)) || (parent.getName().equals(Relationship.MERGECONFLICT)))
			_action = Action.SYNC;
		else
			_action = null;
	}

	public Action getAction() {
		return _action;
	}

	public String getAction(RepoKind rk) {
		if (rk == RepoKind.HG) {
			if (_action == Action.RESOLVE)
				return "hg merge";
			else if (_action == Action.CHECKPOINT)
				return "hg commit";
			else if (_action == Action.PUBLISH)
				return "hg push";
			else if (_action == Action.SYNC)
				return "hg fetch";
			else if (_action == Action.NOTHING)
				return null;
			else if (_action == Action.UNKNOWN)
				return "not computed";
			else 
				return "cannot compute hg action";
		} else 
			return "unsupported repository kind";
	}

	public String getToolTipText() {
		String answer = "";
		if ((_action != null) && (_action != Action.NOTHING)) 
			answer += "Action: " + getAction(RepoKind.HG) + "\n";
		if(_consequences != null)
			answer += "Consequences: new relationship will be " + _consequences.getName() + "\n";
		else if ((_committers != null) && (!(_committers.isEmpty()))) 
			answer += "Committers: " + _committers + "\n";
		return answer.trim();
	}

	/*
	 * Depricated because only images are now compared 
	@Override
	public int compareTo(Relationship other) {
		// handle comparison to null 
		if (other == null) return 1;

		// handle one or both items not being ready
		if (_ready && !other._ready)
			return 1;
		else if (!_ready && other._ready)
			return -1;
		if (!_ready && !other._ready)
			return 0;

//					// this is code for all hollow < all unsaturated < all solid
//			if (getIconFill() > other.getIconFill()) 
//				return 1;
//			else if (getIconFill() < other.getIconFill())
//				return -1;
//			else
//				return (getIconShape() - other.getIconShape());
		 
		// this is code for using the getIntRepresentation for ordering icons
		return getIntRepresentation() - other.getIntRepresentation();
	}
	*/

	/*
	 * Nothing to do: PENDING < SAME < ERROR
	 * Action will succeed:   AHEAD < BEHIND < MERGECLEAN
	 * Action will fail:  TESTCONFLICT < COMPILECONLFICT < MERGECONFLICT.
		The latter two categories have solid/unsaturated/hollow versions.

		I would say that all "action-succeed" icons should be less-prioritized than all "action-fail" icons.  In particular, one could order as follows:
	 * nothing-to-do
	 * action-succeed hollow
	 * action-succeed unsaturated
	 * action-succeed solid
	 * action-fail hollow
	 * action-fail unsaturated
	 * action-fail solid
	 *
	 * Depricated because only images are now compared 
	private int getIntRepresentation() {
		int answer;
		// 0 -- 3
		if (getIconShape() <= 3)
			answer = getIconShape();
		// 4 -- 12
		else if (getIconShape() <= 6)
			answer = getIconShape() + getIconFill() * 3;
		// 13 --
		else // getIconShape() is 7 -- 9
			answer = 2 * 3 + getIconShape() + getIconFill() * 3;
		return answer;
	}
	*/
	
	private static int getImageIntRepresentation(Image i) {
		if (i == null) return -1;
		String address = i.toString();
		
		int answer;
		// 0 -- 3
		if (getIconShape(address) <= 3)
			answer = getIconShape(address);
		// 4 -- 12
		else if (getIconShape(address) <= 6)
			answer = getIconShape(address) + getIconFill(address) * 3;
		// 13 --
		else // getIconShape() is 7 -- 9
			answer = 2 * 3 + getIconShape(address) + getIconFill(address) * 3;
		return answer;
	}
	
	/*
	 * Compares Images a and b (same was as getIntRepresenation does) and returns the dominant Image.
	 */
	private static Image compareImages(Image a, Image b) {
		if (getImageIntRepresentation(a) > getImageIntRepresentation(b)) 
			return a;
		else
			return b;
	}

	public static Image getDominant(Collection<Relationship> relationships) {
		Image dominant = null;
		for (Relationship currentRelationship : relationships)
			dominant = compareImages(currentRelationship.getImage(), dominant);
		return dominant;
	}

	@Override
	public String toString() {
		return _name;
	}

	public boolean equals(Object o) {
		if (o instanceof Relationship)
			return _name.equals(((Relationship) o)._name);
		else
			return false;
	}

	public int hashCode() {
		return _name.hashCode();
	}


//	public DataSource getDataSource() {
//		return _source;
//	}

}