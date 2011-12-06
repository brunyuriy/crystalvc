package crystal.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import crystal.model.DataSource.RepoKind;
import crystal.model.Relationship;
import crystal.server.GitLogParser;
import crystal.server.GuidanceChecker;
import crystal.server.HgLogParser;
import crystal.server.AbstractLogParser.Checkpoint;
import crystal.util.SetOperations;
import crystal.util.ValidInputChecker;

/**
 * A RevisionHistory represents the linear (log) history of a repository
 * 
 * @author brun
 */
public class RevisionHistory implements Cloneable{
	
	// represents a WHEN guidance
	public enum When {
		NOW, LATER, NOTHING
	}

	// represents a CAPABLE guidance
	public enum Capable {
		MUST, MIGHT, CANNOT, NOTHING
	}
	
	// represents an EASE guidance
	public enum Ease {
		ME, YOU
	}
	
	// represents an action
	public enum Action {
		CHECKPOINT, RESOLVE, SYNC, PUBLISH, UNKNOWN, NOTHING
	}
	
	// a Map of changeset hashcodes to Checkpoint objects (each Checkpoint represents a changeset)
	private HashMap<String, Checkpoint> _changesets;
	
	/**
	 * Parses a log and creates a new RevisionHistory
	 * @param log: the log
	 * Current only works for HG
	 */
	public RevisionHistory(String log, RepoKind kind) {
		if (kind.equals(RepoKind.HG))
			_changesets = HgLogParser.parseLog(log);
		else if (kind.equals(RepoKind.GIT))
			_changesets = GitLogParser.parseLog(log);
		else
			throw new IllegalArgumentException("That repo kind is not implemented yet.");
	}
	
	/**
	 * Return clone of this object.
	 */
	public RevisionHistory clone() {
		try {
			RevisionHistory clone = (RevisionHistory) super.clone();
			clone._changesets = new HashMap<String, Checkpoint>(_changesets);
			return clone;
		} catch (CloneNotSupportedException e) {
			return null;
		}
		
	}
	
	/**
	 * @returns the number of changesets in the log
	 */
	public int size() {
		return _changesets.keySet().size();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) 
			return false;
		if (o instanceof RevisionHistory){
			RevisionHistory r = (RevisionHistory) o;
			Set<String> you = r._changesets.keySet();
			Set<String> me = _changesets.keySet();
			
			return ((me.size() == you.size()) && me.containsAll(you));
		} else 
		return false;
	}
	
	@Override
	public int hashCode() {
		return _changesets.hashCode();
	}

	/**
	 * @returns true iff this history is a superset of r
	 */
	public boolean superHistory(RevisionHistory r) {
		ValidInputChecker.checkNullInput(r);
		Set<String> you = r._changesets.keySet();
		Set<String> me = _changesets.keySet();
		
		return me.containsAll(you);
	}
	
	/**
	 * @returns true iff this history is a subset of r
	 */
	public boolean subHistory(RevisionHistory r) {
		ValidInputChecker.checkNullInput(r);
		return r.superHistory(this);
	}
	

	/**
	 * Computes the relevant committers to the differences between this and you histories
	 * @param you: the "other" history
	 * @return the relevant committers to the differences between this and you histories
	 */
	public String getCommitters (RevisionHistory you) {
		ValidInputChecker.checkNullInput(you);
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
	
	/**
	 * Computes the WHEN guidance
	 * @param you: the "other" history
	 * @param parent: the common parent's history
	 * @param r: this' relationship with you
	 * @return the WHEN guidance
	 */
	public When getWhen(RevisionHistory you, RevisionHistory parent, Relationship r) {
		ValidInputChecker.checkNullInput(you);
		ValidInputChecker.checkNullInput(parent);
		//ValidInputChecker.checkNullInput(r);
		return GuidanceChecker.getWhen(_changesets.keySet(), you._changesets.keySet(), parent._changesets.keySet(), r);		
	}
	
	/**
	 * Computes the CONSEQUENCES guidance
	 * @param you: the "other" history
	 * @param parent: the common parent's history
	 * @param r: this' relationship with you
	 * @return the CONSEQUENCES guidance
	 */
	public Relationship getConsequences(RevisionHistory you, RevisionHistory parent, Relationship r) {
		ValidInputChecker.checkNullInput(you);
		ValidInputChecker.checkNullInput(parent);
		//ValidInputChecker.checkNullInput(r);
		return GuidanceChecker.getConsequences(_changesets.keySet(), you._changesets.keySet(), parent._changesets.keySet(), r);		
	}
	
	/**
	 * Computes the CAPABLE guidance
	 * @param you: the "other" history
	 * @param parentparent: the common parent's history
	 * @param r: this' relationship with you
	 * @param isParent: true iff you are my common parent
	 * @return the CAPABLE guidance
	 */
	public Capable getCapable(RevisionHistory you, RevisionHistory parent, Relationship r, boolean isParent) {
		ValidInputChecker.checkNullInput(you);
		ValidInputChecker.checkNullInput(parent);
		//ValidInputChecker.checkNullInput(r);
		return GuidanceChecker.getCapable(_changesets.keySet(), you._changesets.keySet(), parent._changesets.keySet(), r, isParent);
	}
	
	/**
	 * Computes the EASE guidance
	 * @return the EASE guidance
	 */
	public Ease getEase() {
		return GuidanceChecker.getEase();
	}
	
	@Override
	public String toString() {
		String answer = "";
		for (String s : _changesets.keySet()) {
			answer += s + ", ";
		}
		return answer.substring(0, answer.length() - 1);
	}

}
