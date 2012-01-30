package crystal.client;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.junit.Test;

import crystal.Constants;
import crystal.CrystalTest;
import crystal.client.ClientPreferences.DuplicateProjectNameException;
import crystal.client.ClientPreferences.NonexistentProjectException;
import crystal.model.DataSource;
import crystal.model.DataSource.RepoKind;
import crystal.util.XMLTools;

/**
 * Class ClientPreferencesTest will test the performance of class ClientPreferences
 * 
 * @author Haochen
 * 
 */
public class ClientPreferencesTest extends CrystalTest {

	private Logger _log = Logger.getLogger(this.getClass());

	/**
	 * Test null input for constructor
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testNullInputConstructor() {
		new ClientPreferences(null, null, null, -1);
	}

	/**
	 * Check the initial size of clientPreferences.
	 */
	@Test
	public void testClientPreferences() {
		ClientPreferences cp = new ClientPreferences("tempDirectory", "hgPath", "gitPath", Constants.DEFAULT_REFRESH);
		assertEquals("First project preferences size", 0, cp.getProjectPreference().size());

	}

	/**
	 * Check the default setting for clientPreferences
	 */
	@Test
	public void testDefaultSetting() {
		ClientPreferences cpd = ClientPreferences.DEFAULT_CLIENT_PREFERENCES;

		ProjectPreferences pp = new ProjectPreferences(new DataSource("myProject", "$HOME/dev/myProject/", DataSource.RepoKind.HG, false, "MASTER"), cpd);
		pp.addDataSource(new DataSource("jim", "https://path/to/repo", DataSource.RepoKind.HG, false, "MASTER"));
		pp.addDataSource(new DataSource("MASTER", "http://path/to/repo", DataSource.RepoKind.HG, false, null));
		Vector<ProjectPreferences> temp = new Vector<ProjectPreferences>();
		temp.add(pp);

		assertTrue("Default project preferences", cpd.getProjectPreference().equals(temp));
		assertEquals("Default refresh number", ClientPreferences.REFRESH, cpd.getRefresh());
		assertTrue("Default hg path", cpd.getHgPath().equals("/path/to/hg"));
		assertTrue("Default temp directory", cpd.getTempDirectory().equals("/tmp/conflictClient/"));

	}

	/**
	 * Test if the class will thrown DuplicateProjectNameException when user add two projectPreferences with same names
	 * @throws DuplicateProjectNameException
	 */
	@Test(expected = DuplicateProjectNameException.class)
	public void testDuplicateAddProjectPreferences() throws DuplicateProjectNameException {
		ClientPreferences cp = new ClientPreferences("tempDirectory", "hgPath", "gitPath", Constants.DEFAULT_REFRESH);
		assertEquals("Before adding project preferences ", 0, cp.getProjectPreference().size());

		DataSource data = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent");

		ProjectPreferences pp = new ProjectPreferences(data, cp);

		// add two project preferences with same name
		cp.addProjectPreferences(pp);
		cp.addProjectPreferences(pp);

	}

	/**
	 * Test if the class is adding projectPreferences correctly.
	 * @throws DuplicateProjectNameException
	 * @throws NonexistentProjectException
	 */
	@Test
	public void testAddProjectPreferences() throws DuplicateProjectNameException, NonexistentProjectException {
		ClientPreferences cp = new ClientPreferences("tempDirectory", "hgPath", "gitPath", Constants.DEFAULT_REFRESH);
		assertEquals("Before adding project preferences ", 0, cp.getProjectPreference().size());

		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent");

		ProjectPreferences pp_1 = new ProjectPreferences(data_1, cp);

		DataSource data_2 = new DataSource("shortName_2", "cloneString", RepoKind.HG, false, "parent");

		ProjectPreferences pp_2 = new ProjectPreferences(data_2, cp);

		// add new project preferences
		cp.addProjectPreferences(pp_1);
		cp.addProjectPreferences(pp_2);
		// check if the new project preferences exist
		assertEquals("After adding two project preferences", 2, cp.getProjectPreference().size());
		assertNotNull("Get first added project preference", cp.getProjectPreferences("shortName"));
		assertNotNull("Get second added project preference", cp.getProjectPreferences("shortName_2"));
	}

	/**
	 * Check duplicate project in the class 
	 * 
	 * @throws DuplicateProjectNameException
	 * @throws NonexistentProjectException
	 */
	@Test
	public void testDuplicateProject() throws DuplicateProjectNameException, NonexistentProjectException {
		ClientPreferences cp = new ClientPreferences("tempDirectory", "hgPath", "gitPath", Constants.DEFAULT_REFRESH);
		assertEquals("Before adding project preferences ", 0, cp.getProjectPreference().size());

		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent");
		ProjectPreferences pp_1 = new ProjectPreferences(data_1, cp);

		DataSource data_2 = new DataSource("shortName_2", "cloneString", RepoKind.HG, false, "parent");
		ProjectPreferences pp_2 = new ProjectPreferences(data_2, cp);

		// add two project with different names
		cp.addProjectPreferences(pp_1);
		cp.addProjectPreferences(pp_2);

		// change one project's name to be same as another project
		ProjectPreferences temp_pp = cp.getProjectPreferences("shortName_2");
		temp_pp.setName("shortName");

		int count = 0;
		// check how many project has same name
		for (ProjectPreferences pp : cp.getProjectPreference()) {
			if (pp.getName().equals("shortName"))
				count++;
		}
		// there only exist one project named "shortName"
		assertEquals("There exists duplicate project name", 1, count);
	}

	/**
	 * Test removing project from the class
	 * 
	 * @throws DuplicateProjectNameException
	 * @throws NonexistentProjectException
	 */
	@Test
	public void testRemoveProjectPreferences() throws DuplicateProjectNameException, NonexistentProjectException {
		ClientPreferences cp = new ClientPreferences("tempDirectory", "hgPath", "gitPath", Constants.DEFAULT_REFRESH);
		assertEquals("Before adding project preferences ", 0, cp.getProjectPreference().size());

		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent");

		ProjectPreferences pp_1 = new ProjectPreferences(data_1, cp);

		DataSource data_2 = new DataSource("shortName_2", "cloneString", RepoKind.HG, false, "parent");

		ProjectPreferences pp_2 = new ProjectPreferences(data_2, cp);

		// add two projects with different names
		cp.addProjectPreferences(pp_1);
		cp.addProjectPreferences(pp_2);
		// make sure those projects are in the client preferences
		assertEquals("After adding two project preferences", 2, cp.getProjectPreference().size());
		assertNotNull("Get first added project preference", cp.getProjectPreferences("shortName"));
		assertNotNull("Get socend added project preference", cp.getProjectPreferences("shortName_2"));

		
		DataSource remove_ds_1 = new DataSource("shortName_3", "cloneString", RepoKind.HG, false, "parent");
		// non exist project to be removed
		ProjectPreferences remove_pp_1 = new ProjectPreferences(remove_ds_1, cp);

		cp.removeProjectPreferences(remove_pp_1);
		
		// after trying to remove a non exist project preference
		assertEquals("After removing non exist project preference", 2, cp.getProjectPreference().size());

		DataSource remove_ds_2 = new DataSource("shortName_2", "cloneString", RepoKind.HG, false, "parent");

		ProjectPreferences remove_pp_2 = new ProjectPreferences(remove_ds_2, cp);

		cp.removeProjectPreferences(remove_pp_2);
		
		// after removing project preference which was already added into the client preferences
		assertEquals("After removing second added project preference", 1, cp.getProjectPreference().size());

		cp.removeProjectPreferences(remove_pp_2);

		// after removing the same project preferences, there will be no change in client preferences
		assertEquals("Repeat to remove same project preference", 1, cp.getProjectPreference().size());

		try {
			// check if the removed project is really removed from client preferences
			cp.getProjectPreferences("shortName_2");
			fail("Removed project preference still exist");
		} catch (NonexistentProjectException e) {
		}

	}

	/**
	 * check the performance of "removeProjectPreferencesAtIndex" method
	 * 
	 * @throws DuplicateProjectNameException
	 * @throws NonexistentProjectException
	 */
	@Test
	public void testRemoveProjectPreferencesAtIndex() throws DuplicateProjectNameException, NonexistentProjectException {
		ClientPreferences cp = new ClientPreferences("tempDirectory", "hgPath", "gitPath", Constants.DEFAULT_REFRESH);
		assertEquals("Before adding project preferences ", 0, cp.getProjectPreference().size());

		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent");

		ProjectPreferences pp_1 = new ProjectPreferences(data_1, cp);

		DataSource data_2 = new DataSource("shortName_2", "cloneString", RepoKind.HG, false, "parent");

		ProjectPreferences pp_2 = new ProjectPreferences(data_2, cp);

		DataSource data_3 = new DataSource("shortName_3", "cloneString", RepoKind.HG, false, "parent");

		ProjectPreferences pp_3 = new ProjectPreferences(data_3, cp);

		// add 3 new project preferences
		cp.addProjectPreferences(pp_1);
		cp.addProjectPreferences(pp_2);
		cp.addProjectPreferences(pp_3);

		assertEquals("After adding three project preferences", 3, cp.getProjectPreference().size());

		// remove the preference in the second index
		cp.removeProjectPreferencesAtIndex(1);

		try {
			// check if the second added project is really removed
			cp.getProjectPreferences("shortName_2");
			fail("Did not remove from correct place");
		} catch (NonexistentProjectException e) {
		}

		assertEquals("After removing project preference at index 1", 2, cp.getProjectPreference().size());

		// remove the project at the index 1 in current client preferences
		cp.removeProjectPreferencesAtIndex(1);

		try {
			// check if the project is really removed
			cp.getProjectPreferences("shortName_3");
			fail("Did not remove from correct place");
		} catch (NonexistentProjectException e) {
		}

		
		assertEquals("After removing project preference at index 1 again", 1, cp.getProjectPreference().size());

		assertNotNull("First added project preference still exist", cp.getProjectPreferences("shortName"));
	}

	/**
	 * Check if the class can load preferences from XML correctly
	 * @throws NonexistentProjectException
	 */
	@Test
	public void testLoadPreferencesFromXML() throws NonexistentProjectException {
		// load from non existing file
		File notExist = new File("");
		ClientPreferences fileNotExist = ClientPreferences.loadPreferencesFromXML(notExist);
		assertNull("Load from not existing file", fileNotExist);

		// load from test xml file and check if the it could load correctly
		File testXml1 = new File("testDataFile\\testLoadXml1.xml");
		ClientPreferences cp1 = ClientPreferences.loadPreferencesFromXML(testXml1);
		assertTrue("Compare tempDirectory", cp1.getTempDirectory().equals("C:/temp/conflictClient/"));
		assertTrue("Compare refresh value", cp1.getRefresh() == 60);
		ProjectPreferences pp1 = cp1.getProjectPreferences("Crystal");
		DataSource environment1 = pp1.getEnvironment();

		assertTrue("Compare environment kind", environment1.getKind().equals(RepoKind.HG));
		assertTrue("Compare short name for the environment", pp1.getName().equals("Crystal"));
		assertTrue("Compare clone", environment1.getCloneString().equals("C:/Users/Haochen/Dropbox/crystal/haochen/crystalvc/"));
		assertTrue("Compare common parent", environment1.getParent().equals("MASTER"));

		List<DataSource> dataSources = (List<DataSource>) pp1.getDataSources();
		assertTrue("Compare MASTER data source short name", dataSources.get(0).getShortName().equals("MASTER"));
		assertTrue("Compare MASTER data source clone", dataSources.get(0).getCloneString().equals("http://crystalvc.googlecode.com/hg/"));
		assertTrue("Compare MASTER data source hidden", dataSources.get(0).isHidden() == false);
		assertTrue("Comapre MASTER data source common parent", dataSources.get(0).getParent().equals("MASTER"));

		// load from a empty file
		File emptyFile = new File("testDataFile\\testLoadXml2.xml");
		ClientPreferences fileEmpty = ClientPreferences.loadPreferencesFromXML(emptyFile);
		assertNull("Load empty file", fileEmpty);

		// load from a file with duplicate project names
		File testXml3 = new File("testDataFile\\testLoadXml3.xml");
		try {
			ClientPreferences cp3 = ClientPreferences.loadPreferencesFromXML(testXml3);
			assertEquals("ClientPreferences should remove the duplicate project names", 1, cp3.getProjectPreference().size());
			fail("Did not throw exception");
		} catch (Exception e) {
		}

		
		// load from a file with duplicate data source names
		File testXml4 = new File("testDataFile\\testLoadXml4.xml");
		try {
			ClientPreferences cp4 = ClientPreferences.loadPreferencesFromXML(testXml4);
			assertEquals("ClientPreferences should remove the data sources with same names", 4, cp4.getProjectPreferences("Crystal").getDataSources().size());
			fail("Did not throw Exception");
		} catch (Exception e) {
		}

	}

	/**
	 * check if the class can save preferences to xml file correctly
	 * @throws NonexistentProjectException
	 */
	@Test
	public void testSavePreferencesToXML() throws NonexistentProjectException {
		ClientPreferences cp = ClientPreferences.DEFAULT_CLIENT_PREFERENCES;
		((List<DataSource>) cp.getProjectPreferences("myProject").getDataSources()).get(0).setRemoteCmd("RemoteHG");

		String path = "testDataFile\\testSave.xml";
		File f = new File(path);
		if (f.exists()) {
			f.delete();
		}
		// check the xml file doesnt exist at first
		assertFalse("File does not exist before", f.exists());
		try {
			ClientPreferences.savePreferencesToXML(cp, path);
		} catch (Exception e) {
		}
		// make sure that the xml file is created into correct place
		assertTrue("File exist after saving it", f.exists());

		// check the content in the xml file is correct
		Document doc = XMLTools.readXMLDocument(path);
		Element root = doc.getRootElement();
		String tempDirectory = root.getAttributeValue("tempDirectory");
		assertTrue("Compare temp directory", tempDirectory.equals(cp.getTempDirectory()));

		String refresh = root.getAttributeValue("refresh");
		assertTrue("Compare refresh", refresh.equals(String.valueOf(cp.getRefresh())));
		assertEquals("Number of attributes for root element", 2, root.getAttributes().size());

		List<Element> projectElements = root.getChildren("project");
		Element projectElement1 = projectElements.get(0);

		ProjectPreferences pp = cp.getProjectPreferences("myProject");
		assertTrue("Compare kind.", projectElement1.getAttributeValue("Kind").equals(pp.getEnvironment().getKind().name()));
		assertTrue("Compare short name", projectElement1.getAttributeValue("ShortName").equals(pp.getEnvironment().getShortName()));
		assertNull("Compare clone", projectElement1.getAttributeValue("clone"));
		assertTrue("Compare common parent", projectElement1.getAttributeValue("commonParent").equals(pp.getEnvironment().getParent()));
		assertNull("Compile dont't exist", projectElement1.getAttributeValue("compile"));
		assertNull("Test don't exist", projectElement1.getAttributeValue("test"));

		List<Element> sourceElements = projectElement1.getChildren("source");

		Element source1 = sourceElements.get(0);
		List<DataSource> dataSources = (List<DataSource>) pp.getDataSources();
		DataSource data1 = dataSources.get(0);
		assertTrue("Compare short name", source1.getAttributeValue("ShortName").equals(data1.getShortName()));
		assertNull("Clone doesn't exist", source1.getAttributeValue("clone"));
		assertTrue("Compare common parent", source1.getAttributeValue("commonParent").equals(data1.getParent()));
		assertTrue("hide", source1.getAttributeValue("Hidden").equals(String.valueOf(data1.isHidden())));

	}

	/**
	 * test clone method will create a deep copy
	 */
	@Test
	public void testClone() {
		// make sure the clone is copying correct content
		ClientPreferences cp = ClientPreferences.DEFAULT_CLIENT_PREFERENCES;
		assertTrue("Clone method return cllientPreferences with same content", cp.equals(cp.clone()));
		
		
		ClientPreferences copy = cp.clone();
		// check the project names in the original client preference
		for (ProjectPreferences pref : cp.getProjectPreference()) {
			_log.debug("ClientPreferencesTest::testClone() - " + pref.getName());
		}
		
		// change project name of copy projects
		for (ProjectPreferences pref : copy.getProjectPreference()) {
			pref.setName("a");
		}

		
		for (ProjectPreferences pref : cp.getProjectPreference()) {
			_log.debug("ClientPreferencesTest::testClone() - " + pref.getName());
		}
		try {
			// check if copy project and original project have different names
			assertFalse("Changed short name for project preferences",
					copy.getProjectPreferences("a").getName().equals(cp.getProjectPreferences("myProject").getName()));
		} catch (NonexistentProjectException e) {

			
			try {
				copy.getProjectPreferences("a");
				_log.debug("ClientPreferencesTest::testClone() - " + "myProject does not exist");
			} catch (NonexistentProjectException e1) {
				_log.debug("ClientPreferencesTest::testClone() - " + "a does not exist");
			}

			try {
				cp.getProjectPreferences("a");
				_log.debug("ClientPreferencesTest::testClone() - " + "that is a");
			} catch (Exception e1) {
				_log.debug("ClientPreferencesTest::testClone() - " + "that's not a either");
			}

			System.out.println("exception");
		}

		copy.getProjectPreference().clear();
		assertTrue("After removing all component", copy.getProjectPreference().isEmpty());
		assertFalse("Original object with original content", cp.getProjectPreference().isEmpty());
	}
}
