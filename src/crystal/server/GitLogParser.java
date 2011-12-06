package crystal.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;


/**
 * GitLogParser parses the result of an "git log" command into a mapping of hexes into Checkpoint objects.
 * 
 * @author haochen
 */
public class GitLogParser extends AbstractLogParser{

    private static Logger _log = Logger.getLogger(GitLogParser.class);
	/**
	 * Actual label representation for "git log"
	 */
	public static Map<CheckpointLabels, String> gitCheckpoint = new HashMap<CheckpointLabels, String>();
	static {
		gitCheckpoint.put(CheckpointLabels.CHANGESET, "commit");
		gitCheckpoint.put(CheckpointLabels.PARENT, "Merge:");
		gitCheckpoint.put(CheckpointLabels.USER, "Author:");
		gitCheckpoint.put(CheckpointLabels.DATE, "Date:");
		gitCheckpoint.put(CheckpointLabels.SUMMARY, "not empty");
	}

	/**
	 * Parses the output of "git log" into a mapping of hexes to Checkpoints
	 * @param log: the output of "git log"
	 * @return a mapping of hexes to Checkpoints in the log.
	 */
	public static HashMap<String, Checkpoint> parseLog(String log) {
		HashMap<String, Checkpoint> checkpoints = abstractParseLog(log, gitCheckpoint);
		_log.info("\ncheck points with log:\n" + log);
		for (String s : checkpoints.keySet()) {
			_log.info("\nlog:\n" + s + "\ncheck points:\n" + checkpoints.get(s).toString());
		}
		return checkpoints;
	}
}
