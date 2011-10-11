package crystal.server;

import java.util.HashMap;
import java.util.Map;


/**
 * HgLogParser parses the result of an "hg log" command into a mapping of hexes into Checkpoint objects.
 * 
 * @author brun
 */
public class HgLogParser extends AbstractLogParser{
	
	/**
	 * Actual label representation for "hg log"
	 */
	public static Map<CheckpointLabels, String> hgCheckpoint = new HashMap<CheckpointLabels, String>();
	static {
		hgCheckpoint.put(CheckpointLabels.CHANGESET, "changeset:");
		hgCheckpoint.put(CheckpointLabels.TAG, "tag:");
		hgCheckpoint.put(CheckpointLabels.PARENT, "parent:");
		hgCheckpoint.put(CheckpointLabels.USER, "user:");
		hgCheckpoint.put(CheckpointLabels.DATE, "date:");
		hgCheckpoint.put(CheckpointLabels.SUMMARY, "summary:");
		hgCheckpoint.put(CheckpointLabels.FILES, "files:");
	}
	
	/**
	 * Parses the output of "hg log" into a mapping of hexes to Checkpoints
	 * @param log: the output of "hg log"
	 * @return a mapping of hexes to Checkpoints in the log.
	 */
	public static HashMap<String, Checkpoint> parseLog(String log) {
		return abstractParseLog(log, hgCheckpoint);
	}
}
