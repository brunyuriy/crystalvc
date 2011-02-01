package crystal.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.junit.Assert;

import crystal.client.ProjectPreferences;
import crystal.model.LocalStateResult.LocalState;
import crystal.model.Relationship;
import crystal.model.DataSource;
import crystal.model.RevisionHistory;
import crystal.util.RunIt;
import crystal.util.TimeUtility;
import crystal.util.RunIt.Output;

/**
 * Performs hg operations.  
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
		String output = (RunIt.execute(pathToHg, myArgs, tempWorkPath + "status_check", false)).getOutput();

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
	private static synchronized void createLocalRepository(String pathToHg, String pathToRemoteRepo, String pathToLocalRepo, String tempWorkPath, String remoteHg)
	throws IOException, HgOperationException {
		Assert.assertNotNull(pathToHg);
		Assert.assertNotNull(pathToRemoteRepo);
		Assert.assertNotNull(pathToLocalRepo);
		Assert.assertNotNull(tempWorkPath);

		// String hg = prefs.getClientPreferences().getHgPath();

		// String tempWorkPath = prefs.getClientPreferences().getTempDirectory();
		// String pathToRemoteHGRepo = prefs.getEnvironment().getCloneString();
		// String pathToLocalHGRepo = prefs.getClientPreferences().getTempDirectory() +
		// prefs.getEnvironment().getLocalPath();

		String command = pathToHg + " clone"; 
	
		List<String> myArgsList = new ArrayList<String>();
		myArgsList.add("clone");
		if (remoteHg != null) { 
			myArgsList.add("--remotecmd");
			myArgsList.add(remoteHg);
			command += " --remotecmd " + remoteHg; 
		}
		myArgsList.add(pathToRemoteRepo);
		myArgsList.add(pathToLocalRepo);
		command += " " + pathToRemoteRepo + " " + pathToLocalRepo; 
		
		Output output = RunIt.execute(pathToHg, myArgsList.toArray(new String[0]), tempWorkPath, false);

		if (output.getOutput().indexOf("updating to branch") < 0) {
			String dialogMsg = "Crystal tried to execute command:\n" +
			"\"" + pathToHg + " clone " + pathToRemoteRepo + " " + pathToLocalRepo + "\"\n" +
			"from \"" + tempWorkPath + "\"\n" +
			"but got the unexpected output:\n" + 
			output.toString();
			JOptionPane.showMessageDialog(null, dialogMsg, "hg clone failure", JOptionPane.ERROR_MESSAGE);
			throw new HgOperationException(command, tempWorkPath, output.toString());
		//			throw new RuntimeException("Could not clone repository " + pathToRemoteRepo + " to " + pathToLocalRepo + "\n" + output);
		}
	}

	/*
	 * @arg String pathToHg: the path to the hg executable
	 * @arg String pathToLocalRepo: the path to the local repo which this method creates
	 * @arg String tempWorkPath: path to a temp directory
	 * @effect: performs a pull and update on the pathToLocalRepo repository
	 */
	private static synchronized void updateLocalRepository(String pathToHg, String pathToLocalRepo, String pathToRemoteRepo, String tempWorkPath, String remoteHg) throws IOException, HgOperationException {
		Assert.assertNotNull(pathToHg);
		Assert.assertNotNull(pathToLocalRepo);
		Assert.assertNotNull(tempWorkPath);

		String command = pathToHg + " pull -u " + pathToRemoteRepo;
		List<String> myArgsList = new ArrayList<String>();
		myArgsList.add("pull");
		myArgsList.add("-u");
		myArgsList.add(pathToRemoteRepo);
		if (remoteHg != null) { 
			myArgsList.add("--remotecmd");
			myArgsList.add(remoteHg);
			command += "--remotecmd " + remoteHg; 
		}

//		String[] myArgs = { "pull", "-u" };
		Output output = RunIt.execute(pathToHg, myArgsList.toArray(new String[0]), pathToLocalRepo, false);

		if ((output.getOutput().indexOf("files updated") < 0) && (output.getOutput().indexOf("no changes found") < 0))
			throw new HgOperationException(command, pathToLocalRepo, output.toString());
	}
	
	public static LocalState getLocalState(ProjectPreferences prefs) throws IOException {
		
		Assert.assertNotNull(prefs);
		
		// if source are disabled, return null.
		if (!prefs.getEnvironment().isEnabled())
			return null;

		Logger log = Logger.getLogger(HgStateChecker.class);
		
		String hg = prefs.getClientPreferences().getHgPath();
		String tempWorkPath = prefs.getClientPreferences().getTempDirectory();
		
		// if the environment repository is local, we can find out if we need to checkpoint or resolve
		if ((new File(prefs.getEnvironment().getCloneString())).exists()) {

			/*
			 * Get the log and set the changeset
			 */
			String[] logArgs = { "log" };
			Output output = RunIt.execute(hg, logArgs, prefs.getEnvironment().getCloneString(), false);
			prefs.getEnvironment().setHistory(new RevisionHistory(output.getOutput()));
			
			/*
			 * Check if repo has two heads.  If it is, return MUST_RESOLVE
			 */
			String[] headArgs = { "heads" };
			output = RunIt.execute(hg, headArgs, prefs.getEnvironment().getCloneString(), false);
			if (hasTwoHeads(output)) {
				//System.out.println("MUST_RESOLVE for: " + output.getOutput());
				return LocalState.MUST_RESOLVE;
			}
			
			/*
			 * Check if repo status has non-empty response.  If it does, return UNCHECKPOINTED
			 */
			String[] statusArgs = { "status" };
			output = RunIt.execute(hg, statusArgs , prefs.getEnvironment().getCloneString(), false);
			// check if any of the lines in the output don't start with "?"
			StringTokenizer tokens = new StringTokenizer(output.getOutput().trim(), "\n");
			while (tokens.hasMoreTokens()) {
				String nextToken = tokens.nextToken();
				//System.out.println(prefs.getEnvironment().getCloneString() + "#" + nextToken + "#");
				if (!(nextToken.startsWith("?")))
					return LocalState.UNCHECKPOINTED;
			}
			return LocalState.ALL_CLEAR;
		} else {
			// We can't find out the status, but we can find out if you must resolve
			// by cloning, updating, and seeing how many heads there are
			String mine = prefs.getProjectCheckoutLocation(prefs.getEnvironment());
			String tempMyName = "tempMine_" + TimeUtility.getCurrentLSMRDateString();
			if (new File(mine).exists()) {
				try {
					updateLocalRepository(hg, mine, prefs.getEnvironment().getCloneString(), tempWorkPath, prefs.getEnvironment().getRemoteHg());
				} catch (HgOperationException e) {
					String dialogMsg = "Crystal is having trouble executing\n" + e.getCommand() + "\nin " +
					e.getPath() + "\n for your repository of project " + 
					prefs.getEnvironment().getShortName() + ".\n" + 
					"Crystal got the unexpected output:\n" + 
					e.getOutput() + "\n";
					log.error(dialogMsg);
					dialogMsg += "Sometimes, clearing Crystal's local cache can remedy this problem, but this may take a few minutes.\n" + 
					"Would you like Crystal to try that?\n" +
					"The alternative is to skip this project.";
					int answer = JOptionPane.showConfirmDialog(null, dialogMsg, "hg pull problem", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if (answer == JOptionPane.YES_OPTION) {
						RunIt.deleteDirectory(new File(mine));
						try {
							createLocalRepository(hg, prefs.getEnvironment().getCloneString(), mine, tempWorkPath, prefs.getEnvironment().getRemoteHg());
						} catch(HgOperationException e_useless) {
							// We got the error again; there is no hope, you have no chance to survive, make your time.
							// TODO Um, is there a chance that we're leaving half-copied directories around?
							return LocalState.ERROR;
						}			
					} else {
						prefs.getEnvironment().setEnabled(false);
						return null;
					}
				}
			} else {
				try {
					createLocalRepository(hg, prefs.getEnvironment().getCloneString(), mine, tempWorkPath, prefs.getEnvironment().getRemoteHg());
				} catch(HgOperationException e) {
					// We got the error when we didn't expect it; there is no hope, you have no chance to survive, make your time.
					// TODO Um, is there a chance that we're leaving half-copied directories around?
					return LocalState.ERROR;
				}
			}
			String[] myArgs = { "clone", mine, tempMyName };
			Output output = RunIt.execute(hg, myArgs, tempWorkPath, false);
			/*
			 * Could assert that output looks something like: updating to branch default 1 files updated, 0 files merged, 0
			 * files removed, 0 files unresolved
			 */

			/*
			 * Get the log and set the changeset
			 */
			String[] logArgs = { "log" };
			output = RunIt.execute(hg, logArgs, tempWorkPath + tempMyName, false);
			prefs.getEnvironment().setHistory(new RevisionHistory(output.getOutput()));

			
			/*
			 * Check if mine is two headed.  If it is, return MUST_RESOLVE
			 */
			String[] headArgs = { "heads" };
			output = RunIt.execute(hg, headArgs, tempWorkPath + tempMyName, false);	
			RunIt.deleteDirectory(new File(tempWorkPath + tempMyName));
			if (hasTwoHeads(output)) {
			//	System.out.println("MUST_RESOLVE for: " + output.getOutput());
				return LocalState.MUST_RESOLVE;
			}
		}
		return LocalState.ALL_CLEAR;
	}
	
	private static boolean hasTwoHeads(Output output) {
		Pattern heads = Pattern.compile(".*^changeset: .*^changeset: .*", Pattern.DOTALL | Pattern.MULTILINE);
		Matcher matcher = heads.matcher(output.getOutput());
		return matcher.matches();
	}

	/*
	 * @arg prefs: a set of preferences
	 * 
	 * @returns whether prefs.getEnvironment() repository is same, behind, ahead, cleanmerge, or conflictmerge with the source repository.
	 */
	public static String getRelationship(ProjectPreferences prefs, DataSource source) throws IOException {

		Assert.assertNotNull(prefs);
		Assert.assertNotNull(source);

		Logger log = Logger.getLogger(HgStateChecker.class);

		// if project or source are disabled, return null.
		if ((!prefs.getEnvironment().isEnabled()) || (!source.isEnabled()))
			return null;

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
		if ((new File(mine)).exists()) {
			try {
				updateLocalRepository(hg, mine, prefs.getEnvironment().getCloneString(), tempWorkPath, prefs.getEnvironment().getRemoteHg());
			}
			catch (HgOperationException e) {
				String dialogMsg = "Crystal is having trouble executing\n" + e.getCommand() + "\nin " +
				e.getPath() + "\n for your repository of project " + 
				prefs.getEnvironment().getShortName() + ".\n" + 
				"Crystal got the unexpected output:\n" + 
				e.getOutput() + "\n";
				log.error(dialogMsg);
				dialogMsg += "Sometimes, clearing Crystal's local cache can remedy this problem, but this may take a few minutes.\n" + 
				"Would you like Crystal to try that?\n" +
				"The alternative is to skip this project.";
				int answer = JOptionPane.showConfirmDialog(null, dialogMsg, "hg pull problem", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (answer == JOptionPane.YES_OPTION) {
					RunIt.deleteDirectory(new File(mine));
					try {
						createLocalRepository(hg, prefs.getEnvironment().getCloneString(), mine, tempWorkPath, prefs.getEnvironment().getRemoteHg());
					} catch (HgOperationException e_useless) {
						// We got the error again; there is no hope, you have no chance to survive, make your time.
						// TODO Um, is there a chance that we're leaving half-copied directories around?
						return Relationship.ERROR;
					}
				} else {
					prefs.getEnvironment().setEnabled(false);
					return null;
				}
			}
		} else {
			try {
				createLocalRepository(hg, prefs.getEnvironment().getCloneString(), mine, tempWorkPath, prefs.getEnvironment().getRemoteHg());
			} catch (HgOperationException e_useless) {
				// We got the error when we didn't expect it; there is no hope, you have no chance to survive, make your time.
				// TODO Um, is there a chance that we're leaving half-copied directories around?
				return Relationship.ERROR;
			}
		}
		// Check if a local copy of your repository exists. If it does, update it. If it does not, create it.
		if ((new File(yours)).exists()) {
			try {
				updateLocalRepository(hg, yours, source.getCloneString(), tempWorkPath, source.getRemoteHg());
			}
			catch (HgOperationException e) {
				String dialogMsg = "Crystal is having trouble executing\n" + e.getCommand() + "\nin " +
				e.getPath() + "\n for the repository " + source.getShortName() + 
				" in project " + prefs.getEnvironment().getShortName() + ".\n" +
				"Crystal got the unexpected output:\n" + 
				e.getOutput() + "\n";
				log.error(dialogMsg);
				dialogMsg += "Sometimes, clearing Crystal's local cache can remedy this problem, but this may take a few minutes.\n" + 
				"Would you like Crystal to try that?\n" +
				"The alternative is to skip this repository.";
				int answer = JOptionPane.showConfirmDialog(null, dialogMsg, "hg pull problem", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (answer == JOptionPane.YES_OPTION) {
					RunIt.deleteDirectory(new File(yours));
					try {
						createLocalRepository(hg, source.getCloneString(), yours, tempWorkPath, source.getRemoteHg());
					} catch (HgOperationException e_useless) {
						// We got the error again; there is no hope, you have no chance to survive, make your time.
						// TODO Um, is there a chance that we're leaving half-copied directories around?
						return Relationship.ERROR;
					}
				} else {
					source.setEnabled(false);
					return null;
				}
			}
		} else {
			try {
				createLocalRepository(hg, source.getCloneString(), yours, tempWorkPath, source.getRemoteHg());
			} catch (HgOperationException e_useless) {
				// We got the error again; there is no hope, you have no chance to survive, make your time.
				// TODO Um, is there a chance that we're leaving half-copied directories around?
				return Relationship.ERROR;
			}
		}
		String answer;

		Output output;

		String[] myArgs = { "clone", mine, tempMyName };
		output = RunIt.execute(hg, myArgs, tempWorkPath, false);

		/*
		 * Could assert that output looks something like: updating to branch default 1 files updated, 0 files merged, 0
		 * files removed, 0 files unresolved
		 */
		
		String[] yourArgs = { "clone", yours, tempYourName };
		output = RunIt.execute(hg, yourArgs, tempWorkPath, false);
		/*
		 * Could assert that output looks something like: updating to branch default 1 files updated, 0 files merged, 0
		 * files removed, 0 files unresolved
		 */
		
		/*
		 * Get the log and set the changeset
		 */
		String[] logArgs = { "log" };
		output = RunIt.execute(hg, logArgs, tempWorkPath + tempYourName, false);
		source.setHistory(new RevisionHistory(output.getOutput()));
		

		String[] pullArgs = { "pull", tempWorkPath + tempYourName };
		output = RunIt.execute(hg, pullArgs, tempWorkPath + tempMyName, false);
		/*
		 * SAME or AHEAD if output looks something like this: pulling from /homes/gws/brun/temp/orig searching for
		 * changes no changes found
		 */
		if (output.getOutput().indexOf("no changes found") >= 0) {
			// Mine is either the same or ahead, so let's check if yours is ahead
			String[] reversePullArgs = { "pull", tempWorkPath + tempMyName };
			output = RunIt.execute(hg, reversePullArgs, tempWorkPath + tempYourName, false);
			/*
			 * SAME if output looks something like this: pulling from /homes/gws/brun/temp/orig searching for changes no
			 * changes found
			 */
			if (output.getOutput().indexOf("no changes found") >= 0)
				answer = Relationship.SAME;
			/*
			 * mine is AHEAD (yours is BEHIND) if output looks something like this: searching for changes adding
			 * changesets adding manifests adding file changes added 1 changesets with 1 changes to 1 files (run 'hg
			 * update' to get a working copy)
			 */
			else if (output.getOutput().indexOf("(run 'hg update' to get a working copy)") >= 0)
				answer = Relationship.AHEAD;
			else {
				log.error("Crystal is having trouble comparing" + mine + " and " + yours + "\n" + output);
				String dialogMsg = "Crystal is having trouble comparing\n" + 
				mine + " and " + yours + "\n" + 
				"for the repository " + source.getShortName() + " in project " + prefs.getEnvironment().getShortName() + ".\n" +
				"Sometimes, clearing Crystal's local cache can remedy this problem, but this may take a few minutes.\n" + 
				"Would you like Crystal to try that?\n" +
				"The alternative is to skip this repository.";
				int dialogAnswer = JOptionPane.showConfirmDialog(null, dialogMsg, "hg pull problem", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (dialogAnswer == JOptionPane.YES_OPTION) {
					RunIt.deleteDirectory(new File(mine));
					RunIt.deleteDirectory(new File(yours));
					return getRelationship(prefs, source);
				} else {
					source.setEnabled(false);
					return null;
				}
			}
			//				throw new RuntimeException("Unknown reverse pull output: " + output + "\n Could not determine the relative state of " + yours
			//						+ " and " + mine);
		}

		/*
		 * BEHIND if output looks something like this: searching for changes adding changesets adding manifests adding
		 * file changes added 1 changesets with 1 changes to 1 files (run 'hg update' to get a working copy)
		 */
		else if (output.getOutput().indexOf("(run 'hg update' to get a working copy)") >= 0)
			answer = Relationship.BEHIND;

		/*
		 * CONFLICT if output looks something like this: pulling from ../firstcopy/ searching for changes adding
		 * changesets adding manifests adding file changes added 1 changesets with 1 changes to 1 files (+1 heads) (run
		 * 'hg heads' to see heads, 'hg merge' to merge)
		 */
		else if (output.getOutput().indexOf("(run 'hg heads' to see heads, 'hg merge' to merge)") >= 0) {
			// there are two heads, so let's see if they merge cleanly
			String[] mergeArgs = { "merge", "--noninteractive" };
			output = RunIt.execute(hg, mergeArgs, tempWorkPath + tempMyName, false);
			// if the merge goes through cleanly, we can try to compile and test
			if (output.getOutput().indexOf("(branch merge, don't forget to commit)") >= 0) {
				// try to compile {
				// if successful, try to test {
				// if successful:
				answer = Relationship.MERGECLEAN;
				// if unsuccessful:
				// answer = ResultStatus.TESTCONFLICT;
				// }
				// if unsuccessful (compile):
				// answer = ResultStatus.COMPILECONFLICT;
			}
			// otherwise, the merge failed
			else
				answer = Relationship.MERGECONFLICT;
		} else {
			log.error("Crystal is having trouble comparing" + mine + " and " + yours + "\n" + output.toString());
			String dialogMsg = "Crystal is having trouble comparing\n" + 
			mine + " and " + yours + "\n" + 
			"for the repository " + source.getShortName() + " in project " + prefs.getEnvironment().getShortName() + ".\n" +
			"Sometimes, clearing Crystal's local cache can remedy this problem, but this may take a few minutes.\n" + 
			"Would you like Crystal to try that?\n" +
			"The alternative is to skip this repository.";
			int dialogAnswer = JOptionPane.showConfirmDialog(null, dialogMsg, "hg pull problem", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (dialogAnswer == JOptionPane.YES_OPTION) {
				RunIt.deleteDirectory(new File(mine));
				RunIt.deleteDirectory(new File(yours));
				return getRelationship(prefs, source);
			} else {
				source.setEnabled(false);
				return null;
			}
		}
		// throw new RuntimeException("Unknown pull output: " + output + "\n Could not determine the relative state of " + mine + " and " + yours);
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

	public static class HgOperationException extends Exception {

		private static final long serialVersionUID = -6885233021486785003L;
		
		private String _output;
		private String _command;
		private String _path;

		public HgOperationException(String command, String path, String output) {
			super("Tried to execute \n\"" + command + "\"\n in \"" + path + "\"\n" +
					"but got the output\n" + output);
			_output = output;
			_path = path;
			_command = command;
		}
		
		public String getOutput() {
			return _output;
		}
		
		public String getPath() {
			return _path;
		}

		public String getCommand() {
			return _command;
		}

	}
}

