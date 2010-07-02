package crystal.client;

import java.io.IOException;

import crystal.model.Commit;
import crystal.model.ConflictResult;
import crystal.model.DataSource;
import crystal.model.ConflictResult.ResultStatus;
import crystal.server.HgStateChecker;

public class ConflictDaemon {
	public static void computeConflicts(ClientPreferences preferences, IConflictClient client) {

		for (DataSource source : preferences.getDataSources()) {
			calculateConflict(source, preferences, client);
		}

	}

	public static void calculateConflict(DataSource source, ClientPreferences preferences, IConflictClient client) {

		// double val = Math.random();
		// ResultStatus status = ConflictResult.ResultStatus.SAME;
		//
		// if (val < .25)
		// status = ResultStatus.AHEAD;
		// else if (val >= .25 && val < .5)
		// status = ResultStatus.BEHIND;
		// else if (val >= .5 && val < .75)
		// status = ResultStatus.CONFLICT;

		try {

			ResultStatus status = HgStateChecker.getState(preferences, source);

			client.setStatus(new ConflictResult(source, status));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ConflictResult.ResultStatus calculateConflict(Commit mine, Commit other) {

		return null;
	}

	public static ConflictResult calculateConflict(DataSource source, ClientPreferences prefs) {
		ResultStatus status;
		try {
			status = HgStateChecker.getState(prefs, source);

			ConflictResult result = new ConflictResult(source, status);
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
