package crystal.client;

import static org.junit.Assert.*;

import org.junit.Test;

import crystal.Constants;
import crystal.CrystalTest;
import crystal.model.DataSource;
import crystal.model.DataSource.RepoKind;

/**
 * Class ProjectPreferencesTest will test the performance
 * of class ProjectPreferences.
 * 
 * @author Haochen
 *
 */
public class ProjectPreferencesTest extends CrystalTest {
	
	@Test(expected = IllegalArgumentException.class)
	public void testNullInputConstructor(){
		new ProjectPreferences(null, null);
	}
	
	@Test
	public void testProjectPreferences() {
		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"); 
		ClientPreferences cp_1 = new ClientPreferences("tempDirectory", "hgPath", "gitPaht", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences pp = new ProjectPreferences(
				data_1,	cp_1);
		assertTrue("set environment", pp.getEnvironment().equals(data_1));
		assertTrue("set client preferences", pp.getClientPreferences().equals(cp_1));
		assertEquals("empty data sources", pp.getDataSources().size(), 0);
	}
	
	@Test(expected = RuntimeException.class)
	public void testDuplicateAddDataSource(){
		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"); 
		ClientPreferences cp_1 = new ClientPreferences("tempDirectory", "hgPath", "gitPath", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences pp = new ProjectPreferences(
				data_1,	cp_1);
		DataSource ds_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent");
		DataSource ds_2 = new DataSource("shortName", "cloneString_2", RepoKind.HG, false, "parent_2");
		pp.addDataSource(ds_1);
		pp.addDataSource(ds_2);
	}

	@Test
	public void testAddDataSource() {
		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"); 
		ClientPreferences cp_1 = new ClientPreferences("tempDirectory", "hgPath", "gitPath", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences pp = new ProjectPreferences(
				data_1,	cp_1);
		
		assertEquals("Default data source number: 0", 0, pp.getDataSources().size());
		
		DataSource ds_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent");
		DataSource ds_2 = new DataSource("shortName_2", "cloneString_2", RepoKind.HG, false, "parent_2");
		DataSource ds_3 = new DataSource("shortName_3", "cloneString_3", RepoKind.HG, false, "parent_3");
		pp.addDataSource(ds_1);
		pp.addDataSource(ds_2);
		pp.addDataSource(ds_3);
		assertNotNull("Contain first added data source", pp.getDataSource("shortName"));
		assertNotNull("Contain second added data source", pp.getDataSource("shortName_2"));
		assertNotNull("Contain third added data source", pp.getDataSource("shortName_3"));
		
		pp.getDataSource("shortName_2").setShortName("shortName");
		
		assertEquals("Still have 3 data sources", 3, pp.getDataSources().size());
		
		assertNull("Don't contain second added data source", pp.getDataSource("shortName_2"));
		
		pp.getDataSource("shortName").setShortName("shortName_2");
		
		assertEquals("Still have 3 data sources", 3, pp.getDataSources().size());
		
		assertNull("Don't contain first added data source", pp.getDataSource("shortName"));
		
	}

	@Test
	public void testGetNumOfVisibleSources() {
		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"); 
		ClientPreferences cp_1 = new ClientPreferences("tempDirectory", "hgPath", "gitPath", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences pp = new ProjectPreferences(
				data_1,	cp_1);
		
		DataSource ds_1 = new DataSource("shortName", "cloneString", RepoKind.HG, true, "parent");
		DataSource ds_2 = new DataSource("shortName_2", "cloneString_2", RepoKind.HG, false, "parent_2");
		
		assertEquals("Default number", 0, pp.getNumOfVisibleSources());
		
		pp.addDataSource(ds_1);
		assertEquals("Number of data source", 1, pp.getDataSources().size());
		
		assertEquals("After adding one hidden source", 0, pp.getNumOfVisibleSources());
		
		pp.addDataSource(ds_2);
		assertEquals("Number of data source", 2, pp.getDataSources().size());
		
		assertEquals("After adding one nonhidden source", 1, pp.getNumOfVisibleSources());
		
	}

	@Test
	public void testRemoveDataSource() {
		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"); 
		ClientPreferences cp_1 = new ClientPreferences("tempDirectory", "hgPath", "gitPath", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences pp = new ProjectPreferences(
				data_1,	cp_1);
		
		DataSource ds_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent");
		DataSource ds_2 = new DataSource("shortName_2", "cloneString_2", RepoKind.HG, false, "parent_2");
		DataSource ds_3 = new DataSource("shortName_3", "cloneString_3", RepoKind.HG, false, "parent_3");
		pp.removeDataSource(ds_1);
		
		pp.addDataSource(ds_1);
		
		pp.removeDataSource(new DataSource("shortName", "cloneString", RepoKind.HG, true, "parent"));
		assertEquals("Cannot remove data source", 1, pp.getDataSources().size());
		
		pp.removeDataSource(new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"));
		assertEquals("Removed data source", 0, pp.getDataSources().size());
		
		pp.addDataSource(ds_1);
		pp.addDataSource(ds_2);
		pp.addDataSource(ds_3);
		
		pp.removeDataSource(ds_2);
		assertNotNull("Contain first added data source", pp.getDataSource("shortName"));
		assertNull("Dont contain second added data source", pp.getDataSource("shortName_2"));
		assertNotNull("Contain third added data source", pp.getDataSource("shortName_3"));
		
		
	}
	
	@Test
	public void testGetProjectCheckoutLocation() {
		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"); 
		ClientPreferences cp_1 = new ClientPreferences("tempDirectory", "hgPath", "gitPath", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences pp = new ProjectPreferences(
				data_1,	cp_1);
		
		DataSource ds_2 = new DataSource("shortName_2", "cloneString_2", RepoKind.HG, false, "parent_2");
		
		assertTrue(pp.getProjectCheckoutLocation(ds_2).equals("tempDirectory\\shortName_shortName_2"));
		
	}

	@Test
	public void testClone() {
		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"); 
		ClientPreferences cp_1 = new ClientPreferences("tempDirectory", "hgPath", "gitPath", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences pp = new ProjectPreferences(
				data_1,	cp_1);
		
		pp.addDataSource(new DataSource("sn1", "cs1", RepoKind.HG, false, "p1"));
		
		
		ProjectPreferences copy = pp.clone();
		
		DataSource copyEnv = copy.getEnvironment();
		copyEnv.setShortName("a");
		assertFalse("Changed short name of clone environment", 
				copyEnv.getShortName().equals(pp.getEnvironment().getShortName()));
		
		for(DataSource source : copy.getDataSources()) {
			source.setShortName("a");
		}
		
		try {
			pp.getDataSource("sn1");
			copy.getDataSource("a");
		} catch (Exception e) {
			fail("fail");
		}
		
		assertNull("not exist source name", pp.getDataSource("a"));
		assertNull("not existing source name", copy.getDataSource("sn1"));

		
	}
}
