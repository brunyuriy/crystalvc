package crystal.model;


public class Environment extends DataSource{
	
//	private RepoKind kind;

	
	public Environment(String shortName, String cloneString, RepoKind repoKind,
			boolean hide, String parent) {
		super(shortName, cloneString, repoKind, hide, parent);
	}

}
