package crystal.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import crystal.model.RelationshipResult.Relationship;
import crystal.server.GuidanceChecker;
import crystal.server.HgLogParser;
import crystal.server.HgLogParser.Checkpoint;
import crystal.util.SetOperations;


public class RevisionHistory {
	
	public enum When {
		NOW, LATER, NOTHING
	}
	
	public enum Capable {
		MUST, MIGHT, CANNOT, NOTHING
	}
	
	public enum Ease {
		ME, YOU
	}
	
	private HashMap<String, Checkpoint> _changesets;
	
	public RevisionHistory(String log) {
		_changesets = HgLogParser.parseLog(log);
	}
		
	public String getCommitters (RevisionHistory you) {
		Set<String> changes = new HashSet<String>();
		changes = SetOperations.xor(_changesets.keySet(), you._changesets.keySet());
		
		Set<Checkpoint> checkpoints = new HashSet<Checkpoint>();
		for (String current : changes) {
			if (_changesets.get(current) != null)
				checkpoints.add(_changesets.get(current));
			else
				checkpoints.add(you._changesets.get(current));
		}
		
		return GuidanceChecker.getCommitters(checkpoints);
	}
	
	public When getWhen(RevisionHistory you, RevisionHistory parent, Relationship r) {
		return GuidanceChecker.getWhen(_changesets.keySet(), you._changesets.keySet(), parent._changesets.keySet(), r);		
	}
	
	public Relationship getConsequences(RevisionHistory you, RevisionHistory parent, Relationship r) {
		return GuidanceChecker.getConsequences(_changesets.keySet(), you._changesets.keySet(), parent._changesets.keySet(), r);		
	}
	
	public Capable getCapable(RevisionHistory you, RevisionHistory parent, Relationship r) {
		return GuidanceChecker.getCapable(_changesets.keySet(), you._changesets.keySet(), parent._changesets.keySet(), r);
	}

}
