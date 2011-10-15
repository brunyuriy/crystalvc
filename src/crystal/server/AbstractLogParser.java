package crystal.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import crystal.client.ConflictDaemon;
import crystal.util.ValidInputChecker;

/**
 * AbstractLogParser parses the result of an "log" command into a mapping of hexes into Checkpoint objects.
 * 
 * @author Haochen
 *
 */
public abstract class AbstractLogParser {
	
	
	/**
	 * A Checkpoint represents a VC checkpoint (Checkpoint is not specific to hg, though I've made it in this hg context).
	 * Checkpoint is immutable.
	 * 
	 * @author brun
	 */
	public static class Checkpoint {
		String _changeset;
		String _user;
		String _date;
		String _summary;
		List<String> _parents;
		
		/**
		 * Creates a new Checkpoint
		 * @param changeset: the hex
		 * @param user: the committer
		 * @param date: the date
		 * @param summary: the summary log message
		 * @param parents: the [0..\infty) parents (one hex per parent)
		 */
		public Checkpoint(String changeset, String user, String date, String summary, List<String> parents) {
			_changeset = changeset;
			_user = user;
			_date = date;
			_summary = summary;
			_parents = parents;
		}
		
		/**
		 * two Checkpoints are .equal as long as their hexes are equal
		 */
		@Override
		public int hashCode() {
			return _changeset.hashCode();
		}

		/**
		 * two Checkpoints are .equal as long as their hexes are equal
		 */
		@Override
		public boolean equals(Object o) {
			if (o instanceof Checkpoint)
				return _changeset.equals(((Checkpoint) o)._changeset);
			else
				return false;
		}
		
		@Override
		public String toString() {
			return _changeset + "\n" + _user  + "\n" + _date  + "\n" + _summary  + "\n" + _parents;
		}
		
		/**
		 * @return this hex
		 */
		public String getChangeset() {
			return _changeset;
		}
		
		/**
		 * @return the committer
		 */
		public String getCommitter() {
			return _user;
		}
		
		/**
		 * @return the date of this Checkpoint
		 */
		public String getDate() {
			return _date;
		}
		
		/**
		 * @return the log message
		 */
		public String getSummary() {
			return _summary;
		}
		
		/**
		 * @return the list of parents' hexes
		 */
		public List<String> getParents() {
			return _parents;
		}
	}

	/**
	 * Possible label types for each log type
	 * 
	 * @author Haochen
	 *
	 */
	public enum CheckpointLabels {
		CHANGESET, TAG, PARENT, USER, DATE, SUMMARY, EMPTY, OTHER, FILES;
	}
		
	/**
	 * Parses the output of "log" into a mapping of hexes to Checkpoints
	 * @param log: the output of "log"
	 * @param checkpointlabel: actual label representation for each checkpoint label type
	 * @return a mapping of hexes to Checkpoints in the log.
	 */
	protected static HashMap<String, Checkpoint> abstractParseLog(String log, Map<CheckpointLabels, String> checkpointLabel) {
		ValidInputChecker.checkNullInput(log);
		HashMap<String, Checkpoint> answer = new HashMap<String, Checkpoint>();
		for (String current : log.split("\n" + checkpointLabel.get(CheckpointLabels.CHANGESET))) {
			
			if (!(current.trim().isEmpty())) {
				current = checkpointLabel.get(CheckpointLabels.CHANGESET) + current;

				StringTokenizer tokens = new StringTokenizer(current, "\n");

				String changeset = null, user = null, date = null, summary = null;
				List<String> parents = new ArrayList<String>();
				while (tokens.hasMoreTokens()) {
					String currentLine = tokens.nextToken().trim();

					if (currentLine.startsWith(checkpointLabel.get(CheckpointLabels.CHANGESET)))
						if (clipFront(currentLine).contains(":")) // for hg 
							changeset = clipFront(currentLine).substring(clipFront(currentLine).indexOf(":")+1);
						else 	// for git
							changeset = clipFront(currentLine);
					else if (checkpointLabel.get(CheckpointLabels.TAG) != null 
							&& currentLine.startsWith(checkpointLabel.get(CheckpointLabels.TAG))); // ignore

					else if (checkpointLabel.get(CheckpointLabels.PARENT) != null 
							&& currentLine.startsWith(checkpointLabel.get(CheckpointLabels.PARENT))) {
						StringTokenizer parentTokens = new StringTokenizer(clipFront(currentLine).intern());
						// put all parents in the line to list of parents
						while (parentTokens.hasMoreTokens()) {
							String nextParent = parentTokens.nextToken();
							parents.add(nextParent);
						}
						
					} else if (checkpointLabel.get(CheckpointLabels.USER) != null 
							&& currentLine.startsWith(checkpointLabel.get(CheckpointLabels.USER))) {
						user = clipFront(currentLine);
						
					} else if (checkpointLabel.get(CheckpointLabels.DATE) != null
							&& currentLine.startsWith(checkpointLabel.get(CheckpointLabels.DATE)))
						date = clipFront(currentLine);

					else if (checkpointLabel.get(CheckpointLabels.SUMMARY) != null
							&& currentLine.startsWith(checkpointLabel.get(CheckpointLabels.SUMMARY))) {
						summary = clipFront(currentLine);
						
					} else if (checkpointLabel.get(CheckpointLabels.SUMMARY) != null
							&& checkpointLabel.get(CheckpointLabels.SUMMARY).equals("not empty")) { 
						if (summary == null)
							summary = currentLine;
						else 
							summary = summary + "\n" + currentLine;
					} else if (checkpointLabel.get(CheckpointLabels.FILES) != null
                            && currentLine.startsWith(checkpointLabel.get(CheckpointLabels.FILES))) {
					    // Ignore the files line
					} else if (currentLine.trim().isEmpty()) {
					 // Ignore the files line
					} else {
					    Logger.getLogger(ConflictDaemon.getInstance().getClass()).info("Tried to parse a line with an unexpected prefix: " + currentLine);
					}
				}
				if (summary == null)
					summary = "";
				if (user == null)
					user = "";
				if (date == null)
					date = "";
				if (changeset != null) {
					answer.put(changeset.intern(), new Checkpoint(changeset.intern(), user.intern(), date.intern(), summary.intern(), parents));
				} else
					throw new RuntimeException("Log contained a changeset description that did not start with \"changeset:\"");
			}
		}
		return answer;
	}
	
	/**
	 * replaces all whitespace characters with a space, combining multiple consecutive white space chars into just one space.
	 * and removes the first token in the line.
	 * @param line: a String to de-white-space
	 * @return the same String as line but with all white space replaced with spaces, combining multiple consecutive white space chars into just one space.
	 */
	private static String clipFront(String line) {
		StringTokenizer tokens = new StringTokenizer(line);

		tokens.nextToken();
		String answer = "";
		while (tokens.hasMoreTokens()) {
			answer += tokens.nextToken() + " ";
		}
		return answer.trim();
	}
}
