package crystal.model;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import crystal.CrystalTest;
import crystal.model.DataSource.RepoKind;

/**
 * Class DataSourceTest will test the performance of class DataSource
 * 
 * @author Haochen
 * 
 */

public class DataSourceTest extends CrystalTest {

	public static final int TIMEOUT = 20000;
	public static DataSource data;
	
	/*
	@Test(expected = IllegalArgumentException.class)
	public void testIllegalInputConstructor() {
		new DataSource(null, null, null, false, null);
	}
	*/
	@Test
	public void testSetField(){
		String short_name = "Repository";
		String clone_string = "path";
		RepoKind repo_kind = RepoKind.HG;
		boolean hide = false;
		String parent = "parent";
		
		data = new DataSource(short_name, clone_string, repo_kind, hide, parent);
		assertTrue("enabled", data.isEnabled());
		assertTrue("shortName", data.getShortName().equals(short_name));
		assertTrue("repo_kind", data.getKind().equals(repo_kind));
		assertFalse("hide", data.isHidden());
		assertTrue("parent", data.getParent().equals(parent));
		assertNull("history", data.getHistory());
	}

	@Test
	public void testSetHistory() {
		File f1 = new File("testDataFile/testLogVersion1.txt");
		File f2 = new File("testDataFile/testLogVersion2.txt");

		String log_version1 = "";
		String log_version2 = "";
		try {
			log_version1 = FileUtils.readFileToString(f1);
			log_version2 = FileUtils.readFileToString(f2);
		} catch (IOException e) {
			System.err.println("File not found");
		}
		
		RevisionHistory history1 = new RevisionHistory(log_version1, RepoKind.HG);
		RevisionHistory history2 = new RevisionHistory(log_version2, RepoKind.GIT);

		assertTrue("Not change history", data.hasHistoryChanged());
		data.setHistory(history1);
		assertTrue("Changed history", data.hasHistoryChanged());
		assertTrue("Same history", data.getHistory().equals(history1));
		assertFalse("Different history", data.getHistory().equals(history2));
		data.setHistory(history2);
		assertTrue("Changed history again", data.hasHistoryChanged());
		assertTrue("Set new history", data.getHistory().equals(history2));
		assertFalse("Different history", data.getHistory().equals(history1));
		
	}

	@Test
	public void testSetRemoteCmd() {
		assertNull("Get default remote hg", data.getRemoteCmd());
		data.setRemoteCmd("remoteHg");
		assertTrue("Set remote hg", data.getRemoteCmd().equals("remoteHg"));
	}

	@Test
	public void testSetCompileCommand() {
		assertNull("Get default compile command", data.getCompileCommand());
		data.setCompileCommand("compileCommand");
		assertTrue("Set compile command", data.getCompileCommand().equals("compileCommand"));

	}

	@Test
	public void testSetEnabled() {
		assertTrue("Default enable setting", data.isEnabled());
		data.setEnabled(false);
		assertFalse("Is not enabled", data.isEnabled());
	}

	@Test
	public void testIsHidden() {
		assertFalse("Default hidden setting", data.isHidden());
		data.hide(true);
		assertTrue("Hidden", data.isHidden());
	}

	@Test
	public void testSetParent() {
		assertTrue("Default parent", data.getParent().equals("parent"));
		data.setParent("	");
		assertTrue("Empty String parent", data.getParent().equals(""));
		data.setParent(null);
		assertTrue("Null string parent", data.getParent().equals(""));

	}

	@Test
	public void testSetKind() {
		assertTrue("Default repo kind", data.getKind().equals(RepoKind.HG));
		data.setKind(RepoKind.GIT);
		assertTrue("Git kind", data.getKind().equals(RepoKind.GIT));
	}


	@Test
	public void testSetCloneString() {
		assertTrue("Default clone string", data.getCloneString().equals("path"));
		data.setCloneString("path_2");
		assertTrue("Set path", data.getCloneString().equals("path_2"));
	}

	@Test
	public void testToString() {
		String short_name = "short_name";
		RepoKind kind = RepoKind.HG;
		String cloneString = "clone_string";
		
		data.setShortName(short_name);
		data.setKind(kind);
		data.setCloneString(cloneString);
		assertTrue("String representation", data.toString().equals(short_name + "_" + kind + "_" + cloneString));
		
	}

	@Test
	public void testClone() {
		DataSource ds = new DataSource("shortname", "cloneString"
				, RepoKind.HG, false, "parent");
		
		DataSource copy = ds.clone();
		copy.setShortName("a");
		assertFalse("changed short name", copy.getShortName().equals(ds.getShortName()));
		
	}
}
