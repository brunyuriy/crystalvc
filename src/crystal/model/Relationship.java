package crystal.model;

import java.awt.Image;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import crystal.Constants;
import crystal.model.DataSource.RepoKind;
import crystal.model.RevisionHistory.Action;
import crystal.model.RevisionHistory.Capable;
import crystal.model.RevisionHistory.Ease;
import crystal.model.RevisionHistory.When;


/**
 * A Relationship represents the relationship between two repositories.  
 * 
 * @author brun
 */
public class Relationship implements Result {

	public static String SAME = "SAME";
	public static String AHEAD = "AHEAD";
	public static String BEHIND = "BEHIND";
	public static String MERGECLEAN = "MERGE";
	public static String MERGECONFLICT = "CONFLICT";
	public static String COMPILECONFLICT = "COMPILECONFLICT";
	public static String TESTCONFLICT = "TESTCONFLICT";
	public static String PENDING = "PENDING";
	public static String ERROR = "ERROR";

	// path to the images
	private static String PATH = "/crystal/client/images/";
	
	// path suffix for 32X32 size images
	private static String SIZE32 = "32X32/";
	// path suffix for 32X32 size images
	private static String SIZE16 = "16X16/";

	// path suffix for must icons
	private static String CAPABLE_MUST = "must/";
	// path suffix for might icons
	private static String CAPABLE_MIGHT = "might/";
	// path suffix for cannot icons
	private static String CAPABLE_CANNOT = "cannot/";

//	private static String WHEN_NOW = "must/";
//	private static String WHEN_LATER = "cannot/";

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

	// a String representation of this relationship
	private final String _name;	

	// the icon for this relationship (32X32, to be shown by a Crystal window)
	private ImageIcon _icon;

	// the image for this relationship (16X16, to be shown by a task bar)
	private Image _image;
	
	// a list of committers relevant to this relationship
	private String _committers;
	// the WHEN guidance
	private When _when;
	// the CAPABLE guidance
	private Capable _capable;
	// the EASE guidance
	private Ease _ease;
	// the CONSEQUENCES guidance
	private Relationship _consequences;
	
	// the action that can be taken for this relationship
	private Action _action;

	// is the relationship ready to be displayed?
	private boolean _ready;
	
	// the error message associated with this ERROR relationship
	private String _errorMessage;

	/**
	 * Creates a new Relationship
	 * 
	 * @param name: the String representation of the relationship
	 * @param icon: the icon to display (if null then it'll either be set to pending, error (if appropriate), or computed later)
	 * @param image: the image to display (if null then it'll either be set to pending, error (if appropriate), or computed later)
	 */
	public Relationship(String name, ImageIcon icon, Image image) {

		if (name.startsWith(ERROR)) {
		    _errorMessage = name.substring(ERROR.length());
		    _name = ERROR;
		} else
		    _name = name.toUpperCase();
		    
		if (ICON_ADDRESSES.get(_name) == null)
			throw new RuntimeException("Trying to create an invalid Relationship");

		_ready = false;

		if (icon != null)
			_icon = icon;
		if (image != null)
            _image = image;
        
		// if the icon should be an error, then override
		if (_name.equals(ERROR)) {
			_icon = new ImageIcon(Constants.class.getResource(PATH + SIZE32 + CAPABLE_MUST + ICON_ADDRESSES.get(ERROR)));
	        _image = new ImageIcon(Constants.class.getResource(PATH + SIZE16 + CAPABLE_MUST + ICON_ADDRESSES.get(ERROR))).getImage();
	        _ready = true;
		}
		
		// no icon assigned, make it pending as the default
		if (_icon == null)
			_icon = new ImageIcon(Constants.class.getResource(PATH + SIZE32 + CAPABLE_MUST + ICON_ADDRESSES.get(PENDING)));
		if (_image == null)
		    _image = new ImageIcon(Constants.class.getResource(PATH + SIZE16 + CAPABLE_MUST + ICON_ADDRESSES.get(PENDING))).getImage();
	}

	/**
	 * @return the String representation of this relationship (the name)
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Set this relationship ready to be displayed and compute its icon and image.  
	 */
	public void setReady() {
		_ready = true;
		_icon = computeIcon();
		_image = computeImage();
	}

	/**
	 * @return true iff this relationship is ready to be displayed
	 */
	public boolean isReady() {
		return _ready;
	}

	/*
	 * deprecated because only images are now compared 
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

	/**
	 *	Returns and int representation of the icon shape (using a priority order) 
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

	/**
	 * @return 2 if should be solid, 1 if unsaturated, and 0 if hollow
	 */
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
	
	/**
	 * @return 2 if should be solid, 1 if unsaturated, and 0 if hollow for an icon specified by address
	 * @param address: the address of an icon file
	 */
	private static int getIconFill(String address) {
		if (address.indexOf(CAPABLE_MUST) > -1)
			return 2;
		else if (address.indexOf(CAPABLE_MIGHT) > -1)
			return 1;
		else if (address.indexOf(CAPABLE_CANNOT) > -1)
			return 0;
		else 
			// default icon
			// should never happen
			assert(false);
			return 2;
	}

	/**
	 * @return computes and returns the icon to display for this relationship
	 */
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

	/**
	 * @return computes and returns the image to display for this relationship
	 */
	private Image computeImage() {

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

		return (new ImageIcon(Constants.class.getResource(iconAddress)).getImage());
	}

	/**
	 * @return the icon to display
	 */
	public ImageIcon getIcon() {
		return _icon;
	}
	
	/**
	 * @return the image to display
	 */
	public Image getImage() {
			return _image;
	}

	/**
	 * Set the committers guidance
	 * @param committers: the committers guidance
	 */
	public void setCommitters(String committers) {
		_committers = committers;
	}

	/**
	 * @return the committers guidance
	 */
	public String getCommitters() {
		return _committers;
	}

	/**
	 * Set the when guidance
	 * @param when: the when guidance
	 */
	public void setWhen(When when) {
		_when = when;
	}

	/**
	 * @return the when guidance
	 */
	public When getWhen() {
		return _when;
	}

	/**
	 * Set the capable guidance
	 * @param capable: the capable guidance
	 */
	public void setCapable(Capable capable) {
		_capable = capable;
	}

	/**
	 * @return the capable guindance
	 */
	public Capable getCapable() {
		return _capable;
	}

	/**
	 * Set the ease guidance
	 * @param ease: the ease guindance
	 */
	public void setEase(Ease ease) {
		_ease = ease;
	}

	/**
	 * @return the ease guidance
	 */
	public Ease getEase() {
		return _ease;
	}

	/**
	 * Set the consequences guidance
	 * @param consequences: the consequences guidance
	 */
	public void setConsequences(Relationship consequences) {
		_consequences = consequences;
	}

	/**
	 * @return the consequences guidance
	 */
	public Relationship getConsequences() {
		return _consequences;
	}

	/**
	 * Calculate the action to perform
	 * @param localState: the local state of the "my" repository
	 * @param parent: the Relationship with the common parent
	 */
	public void calculateAction(String localState, Relationship parent) {
		if (localState == null)
			_action = null;
		else if ((parent == null) || (localState == LocalStateResult.PENDING))
			_action = Action.UNKNOWN;
		else if (parent.getName().equals(Relationship.SAME))
			_action = Action.NOTHING;
		else if (localState == (LocalStateResult.HG_MUST_RESOLVE) 
				|| localState == (LocalStateResult.GIT_MUST_RESOLVE))
			_action = Action.RESOLVE;
		else if (localState == (LocalStateResult.HG_UNCHECKPOINTED)
				|| localState == (LocalStateResult.GIT_UNCHECKPOINTED))
			_action = Action.CHECKPOINT;
		else if (parent.getName().equals(Relationship.AHEAD))
			_action = Action.PUBLISH;
		else if ((parent.getName().equals(Relationship.BEHIND)) 
		        || (parent.getName().equals(Relationship.MERGECLEAN)) 
		        || (parent.getName().equals(Relationship.MERGECONFLICT))
		        || (parent.getName().equals(Relationship.COMPILECONFLICT))
		    || (parent.getName().equals(Relationship.TESTCONFLICT)))
			_action = Action.SYNC;
		else
			_action = null;
	}

	/**
	 * @return the action to perform
	 */
	public Action getAction() {
		return _action;
	}

	/**
	 * Translates the action into the appropriate VC command
	 * (only works for HG right now)
	 * @param rk: the kind of the repository
	 * @return the command to execute in the repository's VC to execute the action
	 */
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
		} else if (rk == RepoKind.GIT) {
			if (_action == Action.RESOLVE)
				return "git merge";
			else if (_action == Action.CHECKPOINT)
				return "git commit";
			else if (_action == Action.PUBLISH)
				return "git push";
			else if (_action == Action.SYNC)
				return "git fetch";
			else if (_action == Action.NOTHING)
				return null;
			else if (_action == Action.UNKNOWN)
				return "not computed";
			else 
				return "cannot compute git action";			
		} else
			return "unsupported repository kind";
	}

	/**
	 * @return the String to display in the tool tip over this relationship's icon.
	 */
	public String getToolTipText() {
	    if (_name.equals(ERROR))
	        return _errorMessage.trim();

		String answer = "";
		if ((_action != null) && (_action != Action.NOTHING)) 
			answer += "Action: " + getAction(RepoKind.HG) + "\n";
		if(_consequences != null)
			answer += "Consequences: new relationship will be " + _consequences.getName() + "\n";
		else if ((_committers != null) && (!(_committers.isEmpty()))) 
			answer += "Committers: " + _committers + "\n";
		return answer.trim();
	}
	
	/**
	 * Convert a relationship into an int, observing relationship priority
	 * (This method defines the priority)
	 */
	private static int getImageIntRepresentation(Relationship r) {
		if (r == null) return -1;
		if (r.getIcon() == null) return -1;
		String address = r.getIcon().toString();
		
		int answer;
		// 1 -- 3
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
	 * Compares Images a and b (same way as getIntRepresenation does) and returns the dominant Image.
	 */
	private static Relationship compareImages(Relationship a, Relationship b) {
		if (getImageIntRepresentation(a) > getImageIntRepresentation(b)) 
			return a;
		else
			return b;
	}

	/**
	 * @param relationships: a collection of Relationships
	 * @return the dominant Relationship of a collection
	 */
	public static Image getDominant(Collection<Relationship> relationships) {
		Relationship dominant = null;
		for (Relationship currentRelationship : relationships)
			dominant = compareImages(currentRelationship, dominant);
		
		if (dominant == null) {
			return null;
		}
		return dominant.getImage();
	}

	@Override
	/**
	 * a String representation of this Relationship (just the name)
	 */
	public String toString() {
		return _name;
	}

	@Override
	/**
	 * Two Relationship objects are .equal whenever they have the same name.
	 */
	public boolean equals(Object o) {
		if (o instanceof Relationship)
			return _name.equals(((Relationship) o)._name);
		else
			return false;
	}

	@Override
	/**
	 * Two Relationship objects are .equal whenever they have the same name.
	 */
	public int hashCode() {
		return _name.hashCode();
	}
}