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
	private static synchronized void updateLocalRepository(String pathToHg, String pathToLocalRepo, String pathToRemoteRepo, String tempWorkPath, 
														   String remoteHg) throws IOException, HgOperationException {
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
	
	private static void updateLocalRepositoryAndCheckCacheError(DataSource ds, String hg, String localRepo, String tempWorkPath, String remoteHg, 
																String repoName, String projectName) throws HgOperationException, IOException {
		Logger log = Logger.getLogger(HgStateChecker.class);
		if (new File(localRepo).exists()) {
			try {
				updateLocalRepository(hg, localRepo, ds.getCloneString(), tempWorkPath, remoteHg);
			} catch (HgOperationException e) {
				String dialogMsg = "Crystal is having trouble executing\n" + e.getCommand() + "\nin " +
				e.getPath() + "\n for your " + repoName + " repository of project " + 
				projectName + ".\n" + 
				"Crystal got the unexpected output:\n" + 
				e.getOutput() + "\n";
				log.error(dialogMsg);
				dialogMsg += "Sometimes, clearing Crystal's local cache can remedy this problem, but this may take a few minutes.\n" + 
				"Would you like Crystal to try that?\n" +
				"The alternative is to skip this project.";
				int answer = JOptionPane.showConfirmDialog(null, dialogMsg, "hg pull problem", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (answer == JOptionPane.YES_OPTION) {
					RunIt.deleteDirectory(new File(localRepo));
					createLocalRepository(hg, ds.getCloneString(), localRepo, tempWorkPath, remoteHg);
				} else {
					ds.setEnabled(false);
				}
			}
		} else {
			createLocalRepository(hg, ds.getCloneString(), localRepo, tempWorkPath, remoteHg);
		}
	}
	
	public static LocalState getLocalState(ProjectPreferences prefs) throws IOException {
		
		Assert.assertNotNull(prefs);
		
		// if source are disabled, return null.
		if (!prefs.getEnvironment().isEnabled())
			return null;

		/*
		 * We are going to:
		 * 1.  update the local clone
		 * 2.  get the log from the local clone
		 * 3.  if the cloneString is local, we will also get heads and check for UNCHECKPOINTED
		 * 4.  check for MUST_RESOLVE or ALL_CLEAR and return
		 */
		
		String hg = prefs.getClientPreferences().getHgPath();
		String tempWorkPath = prefs.getClientPreferences().getTempDirectory();
		String mine = prefs.getProjectCheckoutLocation(prefs.getEnvironment());

		
		// Step 1. Update the local clone.  If cloning fails, return ERROR state
		try {
			updateLocalRepositoryAndCheckCacheError(prefs.getEnvironment(), hg, mine, tempWorkPath, prefs.getEnvironment().getRemoteHg(), 
					"your own", prefs.getEnvironment().getShortName());
		} catch (HgOperationException e) {
			return LocalState.ERROR;
		} catch (IOException e) {
			return LocalState.ERROR;
		}
		
		// Step 2. Get the log from the local clone and set the history
		String[] logArgs = { "log" };
		Output output = RunIt.execute(hg, logArgs, mine, false);
		prefs.getEnvironment().setHistory(new RevisionHistory(output.getOutput()));
		
		// TODO Step 2.5.  If the history has changed, find out if build or test fails.
		
		if ((new File(prefs.getEnvironment().getCloneString())).exists()) {
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
		}
		
		// We can't find out the status, but we can find out if MUST_RESOLVE 

		/*
		 * Check if mine is two headed.  If it is, return MUST_RESOLVE
		 */
		String[] headArgs = { "heads" };
		output = RunIt.execute(hg, headArgs, mine, false);	
		if (hasTwoHeads(output))
			return LocalState.MUST_RESOLVE;
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

		String mine = prefs.getProjectCheckoutLocation(prefs.getEnvironment());
		String yours = prefs.getProjectCheckoutLocation(source);

		String hg = prefs.getClientPreferences().getHgPath();

		String tempWorkPath = prefs.getClientPreferences().getTempDirectory();
		// tempWorkPath + tempMyName used to store a local copy of my repo
		String tempMyName = "tempMine_" + TimeUtility.getCurrentLSMRDateString();
		// tempWorkPath + tempYourName used to store a local copy of your repo
//		String tempYourName = "tempYour_" + TimeUtility.getCurrentLSMRDateString();
		
		// My local copy has already been updated when we checked the local status
		// So we are just going to update yours
		try {
			updateLocalRepositoryAndCheckCacheError(source, hg, yours, tempWorkPath, source.getRemoteHg(), 
					source.getShortName(), prefs.getEnvironment().getShortName());
		} catch (HgOperationException e1) {
			return Relationship.ERROR;
		}
		
		// Get your log and set your history
		String[] logArgs = { "log" };
		Output logOutput = RunIt.execute(hg, logArgs, yours, false);
		RevisionHistory yourHistory = new RevisionHistory(logOutput.getOutput());
		source.setHistory(yourHistory);

		RevisionHistory myHistory = prefs.getEnvironment().getHistory();

		// TODO figure out if we need to check for compile and test whenever histories change: 
		// one of (source.hasHistoryChanged()) or (prefs.getEnvironment.hasHistoryChanged()) are true

		if (myHistory.equals(yourHistory))
			return Relationship.SAME;
		
		else if (myHistory.superHistory(yourHistory))
			return Relationship.AHEAD;
		
		else if (myHistory.subHistory(yourHistory))
			return Relationship.BEHIND;
		
		// Well, we in one of {MERGE, CONFLICT, COMPILECONFLICT, TESTCONFLICT} relationships, so we are going to have to bite the bullet and make local clones.  
		String answer;
		Output output;
		
		// pull your repo into [a temp clone of] mine
		String[] myArgs = { "clone", mine, tempMyName };
		output = RunIt.execute(hg, myArgs, tempWorkPath, false);
		String[] pullArgs = { "pull", yours };
		output = RunIt.execute(hg, pullArgs, tempWorkPath + tempMyName, false);

		if (output.getOutput().indexOf("(run 'hg heads' to see heads, 'hg merge' to merge)") >= 0) {
			// there are two heads, so let's see if they merge cleanly
			String[] mergeArgs = { "merge", "--noninteractive" };
			output = RunIt.execute(hg, mergeArgs, tempWorkPath + tempMyName, false);
			// if the merge goes through cleanly, we can try to compile and test
			if (output.getOutput().indexOf("(branch merge, don't forget to commit)") >= 0) {
				// try to compile
				String compileCommand = prefs.getEnvironment().getCompileCommand();
				System.out.println(compileCommand);
				if (compileCommand != null) {
					Output compileOutput = RunIt.tryCommand(compileCommand, tempWorkPath + tempMyName);
					if (compileOutput.getStatus() != 0)
						// if unsuccessful:
						answer = Relationship.COMPILECONFLICT;
					else {
						// if successful try to test
						String testCommand = prefs.getEnvironment().getTestCommand();
						if (testCommand != null) {
							Output testOutput = RunIt.tryCommand(testCommand, tempWorkPath + tempMyName);
							if (testOutput.getStatus() != 0)
								// if unsuccessful:
								answer = Relationship.TESTCONFLICT;
							else
								// if successful:
								answer = Relationship.MERGECLEAN;
						}
						else
							// we don't know how to test
							answer = Relationship.MERGECLEAN;
					}
				}
				else
					// we don't know how to compile
					answer = Relationship.MERGECLEAN;
			}
			// otherwise, the merge failed
			else
				answer = Relationship.MERGECONFLICT;
		} else {
			// something went wrong; disabling this relationship
			log.error("Crystal is having trouble comparing" + mine + " and " + yours + "\n" + output.toString());
			String dialogMsg = "Crystal is having trouble comparing\n" + 
			mine + " and " + yours + "\n" + 
			"for the repository " + source.getShortName() + " in project " + prefs.getEnvironment().getShortName() + ".\n";
			JOptionPane.showConfirmDialog(null, dialogMsg);
			source.setEnabled(false);
			return Relationship.ERROR;
		}
		// Clean up temp directories:
		RunIt.deleteDirectory(new File(tempWorkPath + tempMyName));
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

