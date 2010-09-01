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
		
		StringTokenizer tokens = new StringTokenizer(log, "\n");
		while (tokens.hasMoreTokens()) {
			String nextLine = tokens.nextToken();
			if (!nextLine.startsWith("changeset:"))
				throw new RuntimeException(nextLine + " does not start with \"changeset:\"");
			String changesetWithNum = clipFront(nextLine);
			String changeset = changesetWithNum.substring(changesetWithNum.indexOf(":")+1);
			
			List<String> parents = new ArrayList<String>();
			nextLine = tokens.nextToken();
			if (nextLine.startsWith("tag:"))
				nextLine = tokens.nextToken();
			
			while (nextLine.startsWith("parent:")) {
				parents.add(clipFront(nextLine));
				nextLine = tokens.nextToken();
			}
			
			if (!nextLine.startsWith("user:"))
				throw new RuntimeException(nextLine + " does not start with \"user:\"");
			String user = clipFront(nextLine);
			
			nextLine = tokens.nextToken();
			if (!nextLine.startsWith("date:"))
				throw new RuntimeException(nextLine + " does not start with \"date:\"");
			String date = clipFront(nextLine);
			
			nextLine = tokens.nextToken();
			if (!nextLine.startsWith("summary:"))
				throw new RuntimeException(nextLine + " does not start with \"summary:\"");
			String summary = clipFront(nextLine);

			answer.put(changeset, new Checkpoint(changeset, user, date, summary, parents));
		}
		
		return answer;
	}
	
	private static String clipFront(String line) {
		StringTokenizer tokens = new StringTokenizer(line);
		tokens.nextToken();
		return tokens.nextToken();
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
