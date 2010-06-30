/**
 * 
 */
package crystal.hg;

import java.io.File;
import java.util.List;

import crystal.Commit;
import crystal.Repository;

//An HgRepository represents a clone of a mercurial repository.


public class HgRepository implements Repository {
	
	public static String workDirectory = "sometempdir";

	String name;
	String addressToClone;
	File localRepoAddress;
	org.freehg.hgkit.core.Repository localRepo;

	public HgRepository(String name, String addressToClone) {
		this.name = name;
		this.addressToClone = addressToClone;
		
		localRepoAddress = new File(workDirectory + "/" + name);
		localRepo = new org.freehg.hgkit.core.Repository(localRepoAddress);
	}

	/* 
	 * @returns a List of Commits in this repository
	 */
	public List<Commit> getCommits() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public File getLocalRepo() {
		return localRepoAddress;
	}
	
	public String getRemoteRepo() {
		return addressToClone;
	}

}

