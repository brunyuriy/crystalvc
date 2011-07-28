package crystal.client;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Vector;

import org.junit.Test;

import crystal.Constants;
import crystal.client.ClientPreferences.DuplicateProjectNameException;
import crystal.client.ClientPreferences.NonexistentProjectException;
import crystal.model.DataSource;
import crystal.model.DataSource.RepoKind;

public class ClientPreferencesTest {
	
	
	@Test(expected = IllegalArgumentException.class)
	public void testNullInputConstructor(){
		new ClientPreferences(null, null, -1);
	}
	
	@Test
	public void testClientPreferences() {
		ClientPreferences cp = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		assertEquals("Default project preferences size", 0, cp.getProjectPreference().size());
		
	}
	
	@Test
	public void testDefaultSetting(){
		ClientPreferences cpd = ClientPreferences.DEFAULT_CLIENT_PREFERENCES;
		
		ProjectPreferences pp = new ProjectPreferences(new DataSource("myProject", "$HOME/dev/myProject/", DataSource.RepoKind.HG, false, "MASTER"),
                cpd);
        pp.addDataSource(new DataSource("jim", "https://path/to/repo", DataSource.RepoKind.HG, false, "MASTER"));
        pp.addDataSource(new DataSource("MASTER", "http://path/to/repo", DataSource.RepoKind.HG, false, null));
        Vector<ProjectPreferences> temp = new Vector<ProjectPreferences>();
        temp.add(pp);
        
        assertTrue("Compare project preferences", cpd.getProjectPreference().equals(temp));
		assertEquals("Default refresh number", ClientPreferences.REFRESH, cpd.getRefresh());
		assertTrue("Default hg path", cpd.getHgPath().equals("/path/to/hg"));
		assertTrue("Default temp directory", cpd.getTempDirectory().equals("/tmp/conflictClient/"));
		
	}

	@Test(expected = DuplicateProjectNameException.class)
	public void testDuplicateAddProjectPreferences() throws DuplicateProjectNameException {
		ClientPreferences cp = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		assertEquals("Before adding project preferences ", 0, cp.getProjectPreference().size());
		
		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"); 
		ClientPreferences cp_1 = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences pp_1 = new ProjectPreferences(
				data_1,	cp_1);
		
		cp.addProjectPreferences(pp_1);
		cp.addProjectPreferences(pp_1);
		
	}
	
	@Test
	public void testAddProjectPreferences() throws DuplicateProjectNameException, NonexistentProjectException{
		ClientPreferences cp = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		assertEquals("Before adding project preferences ", 0, cp.getProjectPreference().size());
		
		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"); 
		ClientPreferences cp_1 = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences pp_1 = new ProjectPreferences(
				data_1,	cp_1);
		
		
		DataSource data_2 = new DataSource("shortName_2", "cloneString", RepoKind.HG, false, "parent"); 
		ClientPreferences cp_2 = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences pp_2 = new ProjectPreferences(
				data_2,	cp_2);
		
		cp.addProjectPreferences(pp_1);
		cp.addProjectPreferences(pp_2);
		assertEquals("After adding two project preferences", 2, cp.getProjectPreference().size());
		assertNotNull("Get first added project preference", cp.getProjectPreferences("shortName"));
		assertNotNull("Get socend added project preference", cp.getProjectPreferences("shortName_2"));
		
		
	}

	@Test
	public void testRemoveProjectPreferences() throws DuplicateProjectNameException, NonexistentProjectException {
		ClientPreferences cp = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		assertEquals("Before adding project preferences ", 0, cp.getProjectPreference().size());
		
		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"); 
		ClientPreferences cp_1 = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences pp_1 = new ProjectPreferences(
				data_1,	cp_1);
		
		
		DataSource data_2 = new DataSource("shortName_2", "cloneString", RepoKind.HG, false, "parent"); 
		ClientPreferences cp_2 = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences pp_2 = new ProjectPreferences(
				data_2,	cp_2);
		
		cp.addProjectPreferences(pp_1);
		cp.addProjectPreferences(pp_2);
		assertEquals("After adding two project preferences", 2, cp.getProjectPreference().size());
		assertNotNull("Get first added project preference", cp.getProjectPreferences("shortName"));
		assertNotNull("Get socend added project preference", cp.getProjectPreferences("shortName_2"));
		
		DataSource remove_ds_1 = new DataSource("shortName_3", "cloneString", RepoKind.HG, false, "parent"); 
		ClientPreferences remove_cp_1 = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences remove_pp_1 = new ProjectPreferences(
				remove_ds_1,	remove_cp_1);
		
		cp.removeProjectPreferences(remove_pp_1);
		
		assertEquals("After removing non exist project preference", 2, cp.getProjectPreference().size());
		
		DataSource remove_ds_2 = new DataSource("shortName_2", "cloneString", RepoKind.HG, false, "parent"); 
		ClientPreferences remove_cp_2 = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences remove_pp_2 = new ProjectPreferences(
				remove_ds_2,	remove_cp_2);
		
		cp.removeProjectPreferences(remove_pp_2);
		
		assertEquals("After removing second added project preference", 1, cp.getProjectPreference().size());
		
		cp.removeProjectPreferences(remove_pp_2);
		
		assertEquals("Repeat to remove same project preference", 1, cp.getProjectPreference().size());
		
		try{
			cp.getProjectPreferences("shortName_2");
			fail("Removed project preference still exist");
		} catch (NonexistentProjectException e){
		}
		
	}

	@Test
	public void testRemoveProjectPreferencesAtIndex() throws DuplicateProjectNameException, NonexistentProjectException {
		ClientPreferences cp = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		assertEquals("Before adding project preferences ", 0, cp.getProjectPreference().size());
		
		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"); 
		ClientPreferences cp_1 = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences pp_1 = new ProjectPreferences(
				data_1,	cp_1);
		
		
		DataSource data_2 = new DataSource("shortName_2", "cloneString", RepoKind.HG, false, "parent"); 
		ClientPreferences cp_2 = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences pp_2 = new ProjectPreferences(
				data_2,	cp_2);
		
		DataSource data_3 = new DataSource("shortName_3", "cloneString", RepoKind.HG, false, "parent"); 
		ClientPreferences cp_3 = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences pp_3 = new ProjectPreferences(
				data_3,	cp_3);
		
		cp.addProjectPreferences(pp_1);
		cp.addProjectPreferences(pp_2);
		cp.addProjectPreferences(pp_3);
		
		assertEquals("After adding three project preferences", 3, cp.getProjectPreference().size());
		
		cp.removeProjectPreferencesAtIndex(1);
		
		try{
			cp.getProjectPreferences("shortName_2");
			fail("Did not remove from correct place");
		} catch (NonexistentProjectException e){
		}
		
		assertEquals("After removing project preference at index 1", 2, cp.getProjectPreference().size());
		
		cp.removeProjectPreferencesAtIndex(1);
		
		try{
			cp.getProjectPreferences("shortName_3");
			fail("Did not remove from correct place");
		} catch (NonexistentProjectException e){
		}
		
		assertEquals("After removing project preference at index 1 again", 1, cp.getProjectPreference().size());
		
		assertNotNull("First added project preference still exist", cp.getProjectPreferences("shortName"));
	}

	//TODO
	@Test
	public void testLoadPreferencesFromXML() throws IOException {
		ClientPreferences temp = ClientPreferences.loadPreferencesFromDefaultXML();
		fail("Not yet implemented");
		
	}

	//TODO
	@Test
	public void testSavePreferencesToDefaultXML() {
		fail("Not yet implemented");
	}

	//TODO
	@Test
	public void testSavePreferencesToXML() {
		fail("Not yet implemented");
	}

}
