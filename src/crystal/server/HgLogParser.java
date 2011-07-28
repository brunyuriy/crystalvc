package crystal.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import crystal.util.ValidInputChecker;

/**
 * HgLogParser parses the result of an "hg log" command into a mapping of hexes into Checkpoint objects.
 * 
 * @author brun
 */
public class HgLogParser {
	
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
	 * Parses the output of "hg log" into a mapping of hexes to Checkpoints
	 * @param log: the output of "hg log"
	 * @return a mapping of hexes to Checkpoints in the log.
	 */
	public static HashMap<String, Checkpoint> parseLog(String log) {
		//ValidInputChecker.checkValidStringInput(log);
		HashMap<String, Checkpoint> answer = new HashMap<String, Checkpoint>();
		
		for (String current : log.split("\nchangeset:")) {
			
			if (!(current.trim().isEmpty())) {
				current = "changeset:" + current;

				StringTokenizer tokens = new StringTokenizer(current, "\n");

				String changeset = null, user = null, date = null, summary = null;
				List<String> parents = new ArrayList<String>();
				while (tokens.hasMoreTokens()) {
					String currentLine = tokens.nextToken().trim();

					if (currentLine.startsWith("changeset:"))
						changeset = clipFront(currentLine).substring(clipFront(currentLine).indexOf(":")+1);

					else if (currentLine.startsWith("tag:")); // ignore

					else if (currentLine.startsWith("parent:"))
						parents.add(clipFront(currentLine).intern());

					else if (currentLine.startsWith("user:"))
						user = clipFront(currentLine);

					else if (currentLine.startsWith("date:"))
						date = clipFront(currentLine);

					else if (currentLine.startsWith("summary:"))
						summary = clipFront(currentLine);

					else 
						throw new RuntimeException("Unexpected line in the log file: " + currentLine);
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
	
//	public static void main(String[] args) throws IOException {
//		String[] hgArgs = { "log" };
//		String log = RunIt.execute("c:/Program Files (x86)/TortoiseHg/hg.exe", hgArgs, "C:/Users/Yuriy/Desktop/work/Crystal/crystalSource").getOutput();
//
//		System.out.println(parseLog(log).keySet().size());
////		System.out.println(log);
//		
//	}

}
