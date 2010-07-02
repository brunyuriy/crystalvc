package crystal.server;

import java.io.IOException;

import crystal.model.ConflictResult.ResultStatus;
import crystal.util.RunIt;

public class StateChecker {

	/*
	 * @arg mine : path to my repository
	 * 
	 * @arg yours : path to your repository
	 * 
	 * @returns whether my repository is same, behind, ahead, or in conflict with your repository.
	 */
	public static ResultStatus getState(String mine, String yours) throws IOException {

		String hg = "hg";
		String tempWorkPath = "/homes/gws/brun/tempCrystal/";
		// tempWorkPath + tempMyName used to store a local copy of my repo
		String tempMyName = "tempMine";
		// tempWorkPath + tempYourName used to store a local copy of your repo
		String tempYourName = "tempYour";

		ResultStatus answer;

		String output;

		String[] myArgs = { "clone", mine, tempMyName };
		output = RunIt.execute(hg, myArgs, tempWorkPath);
		/*
		 * Could assert that output looks something like: updating to branch default 1 files updated, 0 files merged, 0
		 * files removed, 0 files unresolved
		 */

		String[] yourArgs = { "clone", yours, tempYourName };
		output = RunIt.execute(hg, yourArgs, tempWorkPath);
		/*
		 * Could assert that output looks something like: updating to branch default 1 files updated, 0 files merged, 0
		 * files removed, 0 files unresolved
		 */

		String[] pullArgs = { "pull", tempWorkPath + tempYourName };
		output = RunIt.execute(hg, pullArgs, tempWorkPath + tempMyName);
		/*
		 * SAME or AHEAD if output looks something like this: pulling from /homes/gws/brun/temp/orig searching for
		 * changes no changes found
		 */
		if (output.indexOf("no changes found") >= 0) {
			// Mine is either the same or ahead, so let's check if yours is ahead
			String[] reversePullArgs = { "pull", tempWorkPath + tempMyName };
			output = RunIt.execute(hg, reversePullArgs, tempWorkPath + tempYourName);
			/*
			 * SAME if output looks something like this: pulling from /homes/gws/brun/temp/orig searching for changes no
			 * changes found
			 */
			if (output.indexOf("no changes found") >= 0)
				answer = ResultStatus.SAME;
			/*
			 * mine is AHEAD (yours is BEHIND) if output looks something like this: searching for changes adding
			 * changesets adding manifests adding file changes added 1 changesets with 1 changes to 1 files (run 'hg
			 * update' to get a working copy)
			 */
			else if (output.indexOf("(run 'hg update' to get a working copy)") >= 0)
				answer = ResultStatus.AHEAD;
			else
				throw new RuntimeException("Unknown reverse pull output: " + output + "\n Could not determine the relative state of " + yours
						+ " and " + mine);
		}

		/*
		 * BEHIND if output looks something like this: searching for changes adding changesets adding manifests adding
		 * file changes added 1 changesets with 1 changes to 1 files (run 'hg update' to get a working copy)
		 */
		else if (output.indexOf("(run 'hg update' to get a working copy)") >= 0)
			answer = ResultStatus.BEHIND;

		/*
		 * CONFLICT if output looks something like this: pulling from ../firstcopy/ searching for changes adding
		 * changesets adding manifests adding file changes added 1 changesets with 1 changes to 1 files (+1 heads) (run
		 * 'hg heads' to see heads, 'hg merge' to merge)
		 */
		else if (output.indexOf("(run 'hg heads' to see heads, 'hg merge' to merge)") >= 0)
			answer = ResultStatus.CONFLICT;

		else
			throw new RuntimeException("Unknown pull output: " + output + "\n Could not determine the relative state of " + mine + " and " + yours);

		// Clean up temp directories:
		String[] cleanupArgs = { "-rf", tempMyName, tempYourName };
		RunIt.execute("rm", cleanupArgs, tempWorkPath);
		return answer;
	}

	// a super quick test function that checks the status of "one" and "two" and prints the result
	public static void main(String[] args) throws IOException {
		ResultStatus answer = getState("one", "two");
		System.out.println(answer);
	}

}
