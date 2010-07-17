package crystal.server;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;

import crystal.client.ProjectPreferences;
import crystal.model.ConflictResult.ResultStatus;
import crystal.model.DataSource;
import crystal.util.RunIt;
import crystal.util.TimeUtility;

/**
 * Performs hg clone operations.  
 * Acts as the back end for Crystal.
 * 
 * @author brun
 * 
 */
public class HgStateChecker {


	/*
	 * @arg String pathToHg: the path to the hg executable
	 * @arg String pathToRepo: the full path to the remote repo
	 * @arg String tempWorkPath: path to a temp directory
	 * @return: Whether or not the pathToRepo is a valid hg repository
	 */
	public static boolean isHGRepository(String pathToHg, String pathToRepo, String tempWorkPath) throws IOException {
		Assert.assertNotNull(pathToHg);
		Assert.assertNotNull(pathToRepo);
		Assert.assertNotNull(tempWorkPath);

		String[] myArgs = { "clone", pathToRepo };
		String output = RunIt.execute(pathToHg, myArgs, tempWorkPath + "status_check");

		RunIt.deleteDirectory(new File(tempWorkPath + "status_check"));

		return (output.indexOf("does not appear to be an hg repository!") < 0);
	}

	/*
	 * @arg String pathToHg: the path to the hg executable
	 * @arg String pathToRemoteRepo: the full path to the remote repo
	 * @arg String pathToLocalRepo: the path to the local repo which this method creates
	 * @arg String tempWorkPath: path to a temp directory
	 * @effect: clones the pathToRemoteRepo repository to pathToLocalRepo
	 */
	private static void createLocalRepository(String pathToHg, String pathToRemoteRepo, String pathToLocalRepo, String tempWorkPath)
	throws IOException, InvalidHgRepositoryException {
		Assert.assertNotNull(pathToHg);
		Assert.assertNotNull(pathToRemoteRepo);
		Assert.assertNotNull(pathToLocalRepo);
		Assert.assertNotNull(tempWorkPath);

		// String hg = prefs.getClientPreferences().getHgPath();

		// String tempWorkPath = prefs.getClientPreferences().getTempDirectory();
		// String pathToRemoteHGRepo = prefs.getEnvironment().getCloneString();
		// String pathToLocalHGRepo = prefs.getClientPreferences().getTempDirectory() +
		// prefs.getEnvironment().getLocalPath();

		String[] myArgs = { "clone", pathToRemoteRepo, pathToLocalRepo };
		String output = RunIt.execute(pathToHg, myArgs, tempWorkPath);

		if (output.indexOf("updating to branch") < 0)
			throw new RuntimeException("Could not clone repository " + pathToRemoteRepo + " to " + pathToLocalRepo + "\n" + output);
	}

	/*
	 * @arg String pathToHg: the path to the hg executable
	 * @arg String pathToLocalRepo: the path to the local repo which this method creates
	 * @arg String tempWorkPath: path to a temp directory
	 * @effect: performs a pull and update on the pathToLocalRepo repository
	 */
	private static void updateLocalRepository(String pathToHg, String pathToLocalRepo, String tempWorkPath) throws IOException {
		Assert.assertNotNull(pathToHg);
		Assert.assertNotNull(pathToLocalRepo);
		Assert.assertNotNull(tempWorkPath);

		String[] myArgs = { "pull", "-u" };
		String output = RunIt.execute(pathToHg, myArgs, pathToLocalRepo);

		if ((output.indexOf("files updated") < 0) && (output.indexOf("no changes found") < 0))
			throw new RuntimeException("Could not update repository " + pathToLocalRepo + ": " + output);
	}

	/*
	 * @arg prefs: a set of preferences
	 * 
	 * @returns whether my repository is same, behind, ahead, or in conflict with your repository.
	 */
	public static ResultStatus getState(ProjectPreferences prefs, DataSource source) throws IOException, InvalidHgRepositoryException {

		Assert.assertNotNull(prefs);
		Assert.assertNotNull(source);

		// String mine = prefs.getEnvironment().getLocalString();
		// String yours = source.getLocalString();

		String mine = prefs.getProjectCheckoutLocation(prefs.getEnvironment());
		String yours = prefs.getProjectCheckoutLocation(source);

		// String hg = Constants.HG_COMMAND;
		String hg = prefs.getClientPreferences().getHgPath();

		String tempWorkPath = prefs.getClientPreferences().getTempDirectory();
		// tempWorkPath + tempMyName used to store a local copy of my repo
		String tempMyName = "tempMine_" + TimeUtility.getCurrentLSMRDateString();
		// tempWorkPath + tempYourName used to store a local copy of your repo
		String tempYourName = "tempYour_" + TimeUtility.getCurrentLSMRDateString();

		// Check if a local copy of my repository exists. If it does, update it. If it does not, create it.
		//		System.out.println("*** " + tempWorkPath + " *** " + mine + " ***\n");
		if ((new File(mine)).exists())
			updateLocalRepository(hg, mine, tempWorkPath);
		else
			createLocalRepository(hg, prefs.getEnvironment().getCloneString(), mine, tempWorkPath);

		// Check if a local copy of your repository exists. If it does, update it. If it does not, create it.
		if ((new File(yours)).exists())
			updateLocalRepository(hg, yours, tempWorkPath);
		else
			createLocalRepository(hg, source.getCloneString(), yours, tempWorkPath);

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
		else if (output.indexOf("(run 'hg heads' to see heads, 'hg merge' to merge)") >= 0) {
			// there are two heads, so let's see if they merge cleanly
			String[] mergeArgs = { "merge", "--noninteractive" };
			output = RunIt.execute(hg, mergeArgs, tempWorkPath + tempMyName);
			// if the merge goes through cleanly, we can try to compile and test
			if (output.indexOf("(branch merge, don't forget to commit)") >= 0) {
				// try to compile {
				// if successful, try to test {
				// if successful:
				answer = ResultStatus.MERGECLEAN;
				// if unsuccessful:
				// answer = ResultStatus.TESTCONFLICT;
				// }
				// if unsuccessful (compile):
				// answer = ResultStatus.COMPILECONFLICT;
			}
			// otherwise, the merge failed
			else
				answer = ResultStatus.MERGECONFLICT;
		} else
			throw new RuntimeException("Unknown pull output: " + output + "\n Could not determine the relative state of " + mine + " and " + yours);

		// Clean up temp directories:
		RunIt.deleteDirectory(new File(tempWorkPath + tempMyName));
		RunIt.deleteDirectory(new File(tempWorkPath + tempYourName));
		return answer;
	}

	// a super quick test function that checks the status of "one" and "two" and prints the result
	// public static void main(String[] args) throws IOException {
	// ResultStatus answer = getState("one", "two");
	// System.out.println(answer);
	// }

	public class InvalidHgRepositoryException extends Exception {

		private static final long serialVersionUID = 136849480836706393L;

		public InvalidHgRepositoryException(String invalidRepoAddress, String errorMessage) {
			super("The address " + invalidRepoAddress + " does not contain an hg repository:\n" + errorMessage);
		}
	}
}

