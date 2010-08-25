package crystal.server;

import java.util.Iterator;
import java.util.Set;

import crystal.model.Guidance.Capable;
import crystal.model.Guidance.Ease;
import crystal.model.Guidance.When;
import crystal.model.RelationshipResult.Relationship;
import crystal.server.HgLogParser.Checkpoint;
import crystal.util.SetOperations;

public class GuidanceChecker {

	public String getCommitters (Set<Checkpoint> committers) {
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

	public When getWhen(Set<String> me, Set<String> you, Set<String> parent, Relationship r) {
		if ((r.equals(Relationship.BEHIND)) || (r.equals(Relationship.MERGECLEAN)) || (r.equals(Relationship.MERGECONFLICT))) {
			// if the parent has some things of yours i don't, then NOW
			// if the parent has some things of mine you don't, then NOW
			if (!(SetOperations.aminusb(parent, SetOperations.aminusb(you, me)).isEmpty()))
				return When.NOW;
			else if (!(SetOperations.aminusb(parent, SetOperations.aminusb(me, you)).isEmpty()))
				return When.NOW;
			else
				return When.LATER;
		} else if (r.equals(Relationship.AHEAD)) {
			// if the parent has some things of mine that you don't, then NOW
			if (!(SetOperations.aminusb(parent, SetOperations.aminusb(me, you)).isEmpty()))
				return When.NOW;
			else
				return When.LATER;
		} else if (r.equals(Relationship.SAME)) {
			return When.NOTHING;
		}
		return null;
	}

	// yeah, i dunno yet.
	public Relationship getConsequences(Set<String> me, Set<String> you, Set<String> parent) {

		return null;
	}

	public Capable getCapable(Set<String> me, Set<String> you, Set<String> parent, Relationship r) {
		if (r.equals(Relationship.SAME))
			return Capable.NOTHING;

		// if parent has something of yours i don't, then MUST
		// if parent has something of mine i don't, then CANNOT
		if (!(SetOperations.aminusb(parent, SetOperations.aminusb(you, me)).isEmpty()))
			return Capable.MUST;
		else if (!(SetOperations.aminusb(parent, SetOperations.aminusb(me, you)).isEmpty()))
			return Capable.CANNOT;
		else 
			return Capable.MIGHT;
	}

	// yeah, i dunno yet.
	public Ease getEase() {
		return null;
	}
}