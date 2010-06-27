/**
 * 
 */
package crystal.hg;

import crystal.Commit;

/**
 * @author Yuriy
 * An HgCommit represents a mercurial commit.
 */
public class HgCommit implements Commit {

	String hex;

	public HgCommit(String hex) {
		this.hex = hex;
	}

	public String getHex() {
		return hex;
	}
}