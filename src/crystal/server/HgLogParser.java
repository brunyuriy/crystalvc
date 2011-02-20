package crystal.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class HgLogParser {
	
	public static class Checkpoint {
		String _changeset;
		String _user;
		String _date;
		String _summary;
		List<String> _parents;
		
		public Checkpoint(String changeset, String user, String date, String summary, List<String> parents) {
			_changeset = changeset;
			_user = user;
			_date = date;
			_summary = summary;
			_parents = parents;
		}
		
		@Override
		public int hashCode() {
			return _changeset.hashCode();
		}
		
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
		
		public String getChangeset() {
			return _changeset;
		}
		
		public String getCommitter() {
			return _user;
		}
		
		public String getDate() {
			return _date;
		}
		
		public String getSummary() {
			return _summary;
		}
		
		public List<String> getParents() {
			return _parents;
		}
	}

	public static HashMap<String, Checkpoint> parseLog(String log) {
		
		HashMap<String, Checkpoint> answer = new HashMap<String, Checkpoint>();
		
		for (String current : log.split("changeset:")) {
			
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
				if (changeset != null) {
					answer.put(changeset.intern(), new Checkpoint(changeset.intern(), user.intern(), date.intern(), summary.intern(), parents));
				} else
					throw new RuntimeException("Log contained a changeset description that did not start with \"changeset:\"");
			}
		}
		return answer;
	}
	
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
