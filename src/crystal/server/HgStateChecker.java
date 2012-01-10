package crystal.server;

import java.io.IOException;

import crystal.client.ProjectPreferences;
import crystal.model.DataSource;
import crystal.model.DataSource.RepoKind;

/**
 * Performs hg operations to compute the states of hg repositories.  
 * Acts as the hg back end for Crystal.
 * 
 * @author brun 
 */
public class HgStateChecker extends AbstractStateChecker {

	/**
	 * @param String pathToHg: the path to the hg executable
	 * @param String pathToRepo: the full path to the remote repo
	 * @param String tempWorkPath: path to a temp directory
	 * @return: Whether or not the pathToRepo is a valid hg repository
	 */
	public static boolean isHgRepository(String pathToHg, String pathToRepo, String tempWorkPath) throws IOException {
		return isCorrectRepository(pathToHg, pathToRepo, tempWorkPath, RepoKind.HG);
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
		return getRelationship(prefs, source, oldRelationship, RepoKind.HG);
	}
}

