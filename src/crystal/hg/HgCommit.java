/**
 * 
 */
package crystal.hg;

import crystal.Commit;
import crystal.Repository;

/**
 * @author Yuriy
 * An HgCommit represents a mercurial commit.
 */
public class HgCommit implements Commit {

	String hex;
	HgRepository repository;

	public HgCommit(String hex) {
		this.hex = hex;
	}

	public String getHex() {
		return hex;
	}
	
	public Repository getRepository() {
		return repository;
	}
}