package crystal.server;

import java.io.IOException;

import crystal.client.ProjectPreferences;
import crystal.model.DataSource;
import crystal.model.DataSource.RepoKind;

/**
 * Performs git operations to compute the states of git repositories.  
 * Acts as the git back end for Crystal.
 * 
 * @author haochen 
 */
public class GitStateChecker extends AbstractStateChecker{

	/**
	 * @param String pathToGit: the path to the git executable
	 * @param String pathToRepo: the full path to the remote repo
	 * @param String tempWorkPath: path to a temp directory
	 * @return: Whether or not the pathToRepo is a valid git repository
	 */
	public static boolean isGitRepository(String pathToGit, String pathToRepo, String tempWorkPath) throws IOException {
		return isCorrectRepository(pathToGit, pathToRepo, tempWorkPath, RepoKind.GIT);
	}

	/**
	 * @param prefs: the ProjectPreferences for the project to consider
	 * @return the local state of my repo of the prefs project
	 * @throws IOException
	 */
	public static String getLocalState(ProjectPreferences prefs) throws IOException {
		return AbstractStateChecker.getLocalState(prefs);
	}

	/**
	 * Computes the Relationship of my repo and one other source repo
	 * @param prefs: the ProjectPreferences for the project to consider.
	 * @param source: the repo to compare to.
	 * @param oldRelationship: the old Relationship, in String form.
	 * @return the current relationship between my repo in prefs and source
	 * @throws Exception 
	 * @throws IOException
	 */
	public static String getRelationship(ProjectPreferences prefs, DataSource source, String oldRelationship) {
		return getRelationship(prefs, source, oldRelationship, RepoKind.GIT);
	}
}

