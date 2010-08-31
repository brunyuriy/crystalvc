package crystal.server;

import java.util.Iterator;
import java.util.Set;

import crystal.model.RevisionHistory.Capable;
import crystal.model.RevisionHistory.Ease;
import crystal.model.RevisionHistory.When;
import crystal.model.RelationshipResult.Relationship;
import crystal.server.HgLogParser.Checkpoint;
import crystal.util.SetOperations;

public class GuidanceChecker {

	public static String getCommitters (Set<Checkpoint> committers) {
		if (committers.size() == 0)
			return "";
		else if (committers.size() == 1) {
			Iterator<Checkpoint> i = committers.iterator(); 
			Checkpoint only = i.next();
			return only.getCommitter();
		} else if (committers.size() == 2) {
			Iterator<Checkpoint> i = committers.iterator(); 
			Checkpoint first = i.next();
			Checkpoint second = i.next();
			return first.getCommitter() + " and " + second.getCommitter();
		} else {
			String answer = "";
			for (Iterator<Checkpoint> i = committers.iterator(); i.hasNext();) {
				Checkpoint current = i.next();
				if (i.hasNext()){
					answer += current.getCommitter() + ", ";
				} else {
					answer += "and " + current.getCommitter();
				}
			}
			return answer;
		}
	}

	public static When getWhen(Set<String> me, Set<String> you, Set<String> parent, Relationship r) {
		if ((r.getName().equals(Relationship.BEHIND)) || (r.getName().equals(Relationship.MERGECLEAN)) || (r.getName().equals(Relationship.MERGECONFLICT))) {
			// if the parent has some things of yours i don't, then NOW
			// if the parent has some things of mine you don't, then NOW
			if (!(SetOperations.setDifference(parent, SetOperations.setDifference(you, me)).isEmpty()))
				return When.NOW;
			else if (!(SetOperations.setDifference(parent, SetOperations.setDifference(me, you)).isEmpty()))
				return When.NOW;
			else
				return When.LATER;
		} else if (r.getName().equals(Relationship.AHEAD)) {
			// if the parent has some things of mine that you don't, then NOW
			if (!(SetOperations.setDifference(parent, SetOperations.setDifference(me, you)).isEmpty()))
				return When.NOW;
			else
				return When.LATER;
		} else if (r.getName().equals(Relationship.SAME)) {
			return When.NOTHING;
		}
		return null;
	}

	// This is not actually speculated.  Therefore, it may be imprecise. 
	// In particular, if you and parent are the same, these are wrong.
	public static Relationship getConsequences(Set<String> me, Set<String> you, Set<String> parent, Relationship r) {
		if (r.getName().equals(Relationship.BEHIND)) {
			// if parent has everything you have, SAME, otherwise BEHIND
			if (SetOperations.setDifference(you, parent).isEmpty())
				return new Relationship(Relationship.SAME);
			else
				return new Relationship(Relationship.BEHIND);
		}
		if ((r.getName().equals(Relationship.MERGECLEAN)) || (r.getName().equals(Relationship.MERGECONFLICT))) {
			// if parent has everything you have, AHEAD
			if (SetOperations.setDifference(you, parent).isEmpty())
				return new Relationship(Relationship.AHEAD);
			// if i have everything that the parent has, r
			if (SetOperations.setDifference(me, parent).isEmpty())
				return r;
			// if parent has some of what you have, then we have no idea
			return null;
		}
		else if (r.getName().equals(Relationship.SAME)) {
			return r;
		}
		else if (r.getName().equals(Relationship.AHEAD)){
			return r;
		}
		return null;
	}

	public static Capable getCapable(Set<String> me, Set<String> you, Set<String> parent, Relationship r) {
		if (r.getName().equals(Relationship.SAME))
			return Capable.NOTHING;

		// if parent has something of yours i don't, then MUST
		// if parent has something of mine i don't, then CANNOT
		if (!(SetOperations.setDifference(parent, SetOperations.setDifference(you, me)).isEmpty()))
			return Capable.MUST;
		else if (!(SetOperations.setDifference(parent, SetOperations.setDifference(me, you)).isEmpty()))
			return Capable.CANNOT;
		else 
			return Capable.MIGHT;
	}

	// yeah, i dunno yet.
	public static Ease getEase() {
		return null;
	}
}