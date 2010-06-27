/**
 * 
 */
package crystal.hg;

import java.util.List;

import crystal.Commit;
import crystal.Repository;

//An HgRepository represents a clone of a mercurial repository.


public class HgRepository implements Repository {

	String addressToClone;


	public HgRepository(String addressToClone) {
		this.addressToClone = addressToClone;
		// create whatever HG repository object thingy to keep in here
	}

	/* 
	 * @returns a List of Commits in this repository
	 */
	public List<Commit> getCommits() {
		// TODO Auto-generated method stub
		return null;
	}

}

