package crystal.server;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import crystal.model.RevisionHistory.Action;
import crystal.model.RevisionHistory.Capable;
import crystal.model.RevisionHistory.Ease;
import crystal.model.RevisionHistory.When;
import crystal.model.Relationship;
import crystal.server.AbstractLogParser.Checkpoint;
import crystal.util.SetOperations;
import crystal.util.ValidInputChecker;

/**
 * The GuidanceChecker calculates guidance information
 * 
 * @author brun
 */

public class GuidanceChecker {

	/**
	 * @param committers: a set of Checkpoints with relevant committers
	 * @return a pretty printed String listing all the commiitters
	 */
	public static String getCommitters (Set<Checkpoint> committers) {
		ValidInputChecker.checkNullInput(committers);
		Set<String> committerNames = new HashSet<String>();
		
		for (Checkpoint current : committers)
			committerNames.add(current.getCommitter());
		
		if (committerNames.size() == 0)
			return "";
		else if (committerNames.size() == 1) {
			Iterator<String> i = committerNames.iterator(); 
			return  i.next();
		} else if (committerNames.size() == 2) {
			Iterator<String> i = committerNames.iterator(); 
			return i.next() + " and " + i.next();
		} else {
			String answer = "";
			for (Iterator<String> i = committerNames.iterator(); i.hasNext();) {
				String current = i.next();
				if (i.hasNext()){
					answer += current + ", ";
				} else {
					answer += "and " + current;
				}
			}
			return answer;
		}
	}

	/**
	 * @param me: a set of my checkpoint hexes
	 * @param you: a set of your checkpoint hexes
	 * @param parent: a set of parent's checkpoint hexes
	 * @param r: my and your relationship
	 * @return the WHEN guidance
	 */
	// NOTHING if SAME, MERGE, or CONFLICT
	// if BEHIND or AHEAD:
	// NOW if parent has your things I do not
	// LATER otherwise
	public static When getWhen(Set<String> me, Set<String> you, Set<String> parent, Relationship r) {
		ValidInputChecker.checkNullInput(r);
		
		if ((r.getName().equals(Relationship.SAME)) 
		        || (r.getName().equals(Relationship.MERGECLEAN)) 
		        || (r.getName().equals(Relationship.MERGECONFLICT))
		        || (r.getName().equals(Relationship.COMPILECONFLICT))
		        || (r.getName().equals(Relationship.TESTCONFLICT)))
			return When.NOTHING;
		if (r.getName().equals(Relationship.BEHIND))
			// NOW if parent has something of yours that I do not
			if (!(SetOperations.intersection(you, SetOperations.setDifference(parent, me)).isEmpty()))
				return When.NOW;
			else
				return When.LATER;
		if (r.getName().equals(Relationship.AHEAD))
			// NOW if I have something the parent does not
			if (!(SetOperations.setDifference(me, parent)).isEmpty())
				return When.NOW;
			else
				return When.LATER;
		return When.NOTHING;
	}

	/**
	 * @param me: a set of my checkpoint hexes
	 * @param you: a set of your checkpoint hexes
	 * @param parent: a set of parent's checkpoint hexes
	 * @param r: my and your relationship
	 * @return the CONSEQUENCES guidance
	 * This is not actually speculated.  Therefore, it may be imprecise. 
	 * In particular, if you and parent are the same, these are wrong.
	 */
	public static Relationship getConsequences(Set<String> me, Set<String> you, Set<String> parent, Relationship r) {
		ValidInputChecker.checkNullInput(r);
		
		Action a = r.getAction();
		if (a == null) 
			return null;
		else if (a == Action.NOTHING)
			return r;
		else if (a == Action.UNKNOWN)
			return null;
		else if (a == Action.CHECKPOINT)
			if (r.getName().equals(Relationship.SAME))
				return new Relationship(Relationship.AHEAD, r.getIcon(), r.getImage());
			else if (r.getName().equals(Relationship.BEHIND))
				// might be clean or conflict, we don't know
				return new Relationship(Relationship.MERGECLEAN, r.getIcon(), r.getImage());
			else
				return r;
		else if (a == Action.RESOLVE)
			if ((r.getName().equals(Relationship.SAME)) || (r.getName().equals(Relationship.AHEAD)))
				return new Relationship(Relationship.AHEAD, r.getIcon(), r.getImage());
			else if (r.getName().equals(Relationship.BEHIND))
				// might be clean or conflict, we don't know
				return new Relationship(Relationship.MERGECLEAN, r.getIcon(), r.getImage());
			else 
				return r; 
		else if (a == Action.PUBLISH)
			return r;
		else if (a == Action.SYNC) {
			Set<String> mynew = SetOperations.union(me, parent);
			if (SetOperations.setDifference(you, mynew).isEmpty()) // you won't have anything I don't
				if (SetOperations.setDifference(mynew, you).isEmpty()) // I won't have anything you don't
					return new Relationship(Relationship.SAME, r.getIcon(), r.getImage());
				else // I will have something you don't
					return new Relationship(Relationship.AHEAD, r.getIcon(), r.getImage());
			else // you will have something I don't
				if (SetOperations.setDifference(mynew, you).isEmpty()) // I won't have anything you don't
					return new Relationship(Relationship.BEHIND, r.getIcon(), r.getImage());
				else // I will have something you don't
					// might be clean or conflict, we don't know
					return new Relationship(Relationship.MERGECLEAN, r.getIcon(), r.getImage());
		}		
		
		return null;
	}

	/**
	 * 
	 * @param me: a set of my checkpoint hexes
	 * @param you: a set of your checkpoint hexes
	 * @param parent: a set of parent's checkpoint hexes
	 * @param r: my and your relationship
	 * @param isParent: true iff you are my parent
	 * @return the CAPABLE guidance:
	 * I CANNOT if we're SAME, AHEAD, or BEHIND
	 * I MUST if parent has your things I do not
	 * I CANNOT if parent has my things you do not
	 * I MIGHT if parent does not have some of my things and does not have some of your things
	 */
	public static Capable getCapable(Set<String> me, Set<String> you, Set<String> parent, Relationship r, boolean isParent) {
		if (isParent)
			if (r.getName().equals(Relationship.SAME))
				return Capable.CANNOT;
			else
				return Capable.MUST;

		
		if ((r.getName().equals(Relationship.SAME)) || (r.getName().equals(Relationship.AHEAD)) || (r.getName().equals(Relationship.BEHIND))) 
			return Capable.NOTHING;

		if ((r.getName().equals(Relationship.MERGECLEAN)) 
		        || (r.getName().equals(Relationship.MERGECONFLICT))
		        || (r.getName().equals(Relationship.COMPILECONFLICT))
		        || (r.getName().equals(Relationship.TESTCONFLICT)))
		    // if parent has something of yours i don't, then MUST
		    // if parent has something of mine you don't, then CANNOT
			if (!(SetOperations.intersection(you, SetOperations.setDifference(parent, me)).isEmpty()))
				return Capable.MUST;
			else if (!(SetOperations.intersection(me, SetOperations.setDifference(parent, you)).isEmpty()))
				return Capable.CANNOT;
			else if ((!(SetOperations.setDifference(me, parent).isEmpty())) && (!(SetOperations.setDifference(you, parent).isEmpty())))
				return Capable.MIGHT;

		return Capable.NOTHING;
	}

	/**
	 * @return the EASE guidance
	 * Except I don't know how to compute it yet so it returns null for now.
	 */
	public static Ease getEase() {
		return null;
	}
}