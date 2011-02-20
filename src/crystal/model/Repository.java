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

}
