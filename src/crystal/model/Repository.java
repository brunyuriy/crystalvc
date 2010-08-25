package crystal.model;

import java.io.File;
import java.util.List;

/**
 * @author Yuriy
 * A Repository represents a clone.
 */

public interface Repository {

	public List<Commit> getCommits();
	public File getLocalRepo();
	public String getRemoteRepo();

	// Should there be some sort of History object, which is like a graph, such that we can compare two of them for 
	// same, ahead, behind, diverging?

}
