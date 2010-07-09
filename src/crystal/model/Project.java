/**
 * 
 */
package crystal.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yuriy
 * A Project represents a single project being worked on by one or more developers.  
 * The Project keeps access to each developer's repository.
 */

public class Project {

	private String name;
	private List<Repository> clones;

	public Project (String name) {
		this.name = name;
		clones = new ArrayList<Repository>();
	}

	public void addClone(Repository r) {
		clones.add(r);
	}
	
	public String getName() {
		return name;
	}
}
