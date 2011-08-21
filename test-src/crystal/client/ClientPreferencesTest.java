package crystal.client;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.jdom.Document;
import org.jdom.Element;
import org.junit.Test;

import crystal.Constants;
import crystal.client.ClientPreferences.DuplicateProjectNameException;
import crystal.client.ClientPreferences.NonexistentProjectException;
import crystal.model.DataSource;
import crystal.model.DataSource.RepoKind;
import crystal.util.XMLTools;

/**
 * Class ClientPreferencesTest will test the performance of class 
 * ClientPreferences
 * 
 * @author Haochen
 *
 */
public class ClientPreferencesTest {
	
	
	@Test(expected = IllegalArgumentException.class)
	public void testNullInputConstructor(){
		new ClientPreferences(null, null, -1);
	}
	
	@Test
	public void testClientPreferences() {
		ClientPreferences cp = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		assertEquals("First project preferences size", 0, cp.getProjectPreference().size());
		
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
        
        assertTrue("Default project preferences", cpd.getProjectPreference().equals(temp));
		assertEquals("Default refresh number", ClientPreferences.REFRESH, cpd.getRefresh());
		assertTrue("Default hg path", cpd.getPath().equals("/path/to/hg"));
		assertTrue("Default temp directory", cpd.getTempDirectory().equals("/tmp/conflictClient/"));
		
	}

	@Test(expected = DuplicateProjectNameException.class)
	public void testDuplicateAddProjectPreferences() throws DuplicateProjectNameException {
		ClientPreferences cp = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		assertEquals("Before adding project preferences ", 0, cp.getProjectPreference().size());
		
		DataSource data = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"); 
		
		ProjectPreferences pp = new ProjectPreferences(data, cp);
		
		cp.addProjectPreferences(pp);
		cp.addProjectPreferences(pp);
		
	}
	
	@Test
	public void testAddProjectPreferences() throws DuplicateProjectNameException, NonexistentProjectException{
		ClientPreferences cp = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		assertEquals("Before adding project preferences ", 0, cp.getProjectPreference().size());
		
		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"); 

		ProjectPreferences pp_1 = new ProjectPreferences(data_1,	cp);
		
		
		DataSource data_2 = new DataSource("shortName_2", "cloneString", RepoKind.HG, false, "parent"); 

		ProjectPreferences pp_2 = new ProjectPreferences(data_2,	cp);
		
		cp.addProjectPreferences(pp_1);
		cp.addProjectPreferences(pp_2);
		assertEquals("After adding two project preferences", 2, cp.getProjectPreference().size());
		assertNotNull("Get first added project preference", cp.getProjectPreferences("shortName"));
		assertNotNull("Get second added project preference", cp.getProjectPreferences("shortName_2"));
	}
	
	@Test
	public void testDuplicateProject() throws DuplicateProjectNameException, NonexistentProjectException{
		ClientPreferences cp = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		assertEquals("Before adding project preferences ", 0, cp.getProjectPreference().size());
		
		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"); 

		ProjectPreferences pp_1 = new ProjectPreferences(data_1,	cp);
		
		
		DataSource data_2 = new DataSource("shortName_2", "cloneString", RepoKind.HG, false, "parent"); 

		ProjectPreferences pp_2 = new ProjectPreferences(data_2,	cp);
		
		cp.addProjectPreferences(pp_1);
		cp.addProjectPreferences(pp_2);
		
		ProjectPreferences temp_pp = cp.getProjectPreferences("shortName_2");
		temp_pp.getEnvironment().setShortName("shortName");
		
		int count = 0;
		
		for(ProjectPreferences pp : cp.getProjectPreference()){
			if(pp.getEnvironment().getShortName().equals("shortName"))
				count++;
		}
		assertEquals("There exists duplicate project name", 1, count);
	}

	@Test
	public void testRemoveProjectPreferences() throws DuplicateProjectNameException, NonexistentProjectException {
		ClientPreferences cp = new ClientPreferences("tempDirectory", "hgPath", Constants.DEFAULT_REFRESH);
		assertEquals("Before adding project preferences ", 0, cp.getProjectPreference().size());
		
		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"); 

		ProjectPreferences pp_1 = new ProjectPreferences(data_1,	cp);
		
		
		DataSource data_2 = new DataSource("shortName_2", "cloneString", RepoKind.HG, false, "parent"); 

		ProjectPreferences pp_2 = new ProjectPreferences(data_2,	cp);
		
		cp.addProjectPreferences(pp_1);
		cp.addProjectPreferences(pp_2);
		assertEquals("After adding two project preferences", 2, cp.getProjectPreference().size());
		assertNotNull("Get first added project preference", cp.getProjectPreferences("shortName"));
		assertNotNull("Get socend added project preference", cp.getProjectPreferences("shortName_2"));
		
		DataSource remove_ds_1 = new DataSource("shortName_3", "cloneString", RepoKind.HG, false, "parent"); 

		ProjectPreferences remove_pp_1 = new ProjectPreferences(remove_ds_1, cp);
		
		cp.removeProjectPreferences(remove_pp_1);
		
		assertEquals("After removing non exist project preference", 2, cp.getProjectPreference().size());
		
		DataSource remove_ds_2 = new DataSource("shortName_2", "cloneString", RepoKind.HG, false, "parent"); 

		ProjectPreferences remove_pp_2 = new ProjectPreferences(remove_ds_2, cp);
		
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

		ProjectPreferences pp_1 = new ProjectPreferences(data_1,	cp);
		
		
		DataSource data_2 = new DataSource("shortName_2", "cloneString", RepoKind.HG, false, "parent"); 

		ProjectPreferences pp_2 = new ProjectPreferences(data_2,	cp);
		
		DataSource data_3 = new DataSource("shortName_3", "cloneString", RepoKind.HG, false, "parent"); 

		ProjectPreferences pp_3 = new ProjectPreferences(data_3,	cp);
		
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
	
	@Test
	public void testLoadPreferencesFromXML() throws NonexistentProjectException {
		File notExist = new File("");
		ClientPreferences fileNotExist = ClientPreferences.loadPreferencesFromXML(notExist);
		assertNull("Load from not existing file", fileNotExist);
		
		File testXml1 = new File("testDataFile\\testLoadXml1.xml");
		ClientPreferences cp1 = ClientPreferences.loadPreferencesFromXML(testXml1);
		assertTrue("Compare tempDirectory", cp1.getTempDirectory().equals("C:/temp/conflictClient/"));
		assertTrue("Compare refresh value", cp1.getRefresh() == 60);
		ProjectPreferences pp1 = cp1.getProjectPreferences("Crystal");
		DataSource environment1 = pp1.getEnvironment();
		
		assertTrue("Compare environment kind", environment1.getKind().equals(RepoKind.HG));
		assertTrue("Compare short name for the environment", environment1.getShortName().equals("Crystal"));
		assertTrue("Compare clone", environment1.getCloneString().equals("C:/Users/Haochen/Dropbox/crystal/haochen/crystalvc/"));
		assertTrue("Compare common parent", environment1.getParent().equals("MASTER"));
		
		List<DataSource> dataSources = (List<DataSource>) pp1.getDataSources();
		assertTrue("Compare MASTER data source short name", dataSources.get(0).getShortName().equals("MASTER"));
		assertTrue("Compare MASTER data source clone", dataSources.get(0).getCloneString().equals("http://crystalvc.googlecode.com/hg/"));
		assertTrue("Compare MASTER data source hidden", dataSources.get(0).isHidden() == false);
		assertTrue("Comapre MASTER data source common parent", dataSources.get(0).getParent().equals("MASTER"));
		
		File emptyFile = new File("testDataFile\\testLoadXml2.xml");
		ClientPreferences fileEmpty = ClientPreferences.loadPreferencesFromXML(emptyFile);
		assertNull("Load empty file", fileEmpty);
		
		File testXml3 = new File("testDataFile\\testLoadXml3.xml");
		
		try{
			ClientPreferences cp3 = ClientPreferences.loadPreferencesFromXML(testXml3);
			assertEquals("ClientPreferences should remove the duplicate project names", 1, cp3.getProjectPreference().size());
			fail("Did not throw exception");
		} catch (Exception e){
		}
		
		File testXml4 = new File("testDataFile\\testLoadXml4.xml");
		
		try {
			ClientPreferences cp4 = ClientPreferences.loadPreferencesFromXML(testXml4);
			assertEquals("ClientPreferences should remove the data sources with same names", 
					4, cp4.getProjectPreferences("Crystal").getDataSources().size());
			fail("Did not throw Exception");
		} catch (Exception e){
		}

		
		
	}
	
	@Test
	public void testSavePreferencesToXML() throws NonexistentProjectException{
		ClientPreferences cp = ClientPreferences.DEFAULT_CLIENT_PREFERENCES;
		((List<DataSource>) cp.getProjectPreferences("myProject").getDataSources()).get(0).setRemoteCmd("setRemoteHg");
		
		String path = "testDataFile\\testSave.xml";
		File f = new File(path);
		if(f.exists()){
			f.delete();
		}
		assertFalse("File does not exist before", f.exists());
		ClientPreferences.savePreferencesToXML(cp, path);
		assertTrue("File exist after saving it", f.exists());
		
		Document doc = XMLTools.readXMLDocument(path);
		Element root = doc.getRootElement();
		String tempDirectory = root.getAttributeValue("tempDirectory");
		assertTrue("Compare temp directory", 
				tempDirectory.equals(cp.getTempDirectory()));
		
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
		assertTrue("Check remote hg" + data1.getRemoteCmd(), data1.getRemoteCmd().equals(source1.getAttributeValue("RemoteHG")));
		assertTrue("hide", source1.getAttributeValue("Hidden").equals(String.valueOf(data1.isHidden())));

	}
	
	@Test
	public void testClone() {
		ClientPreferences cp = ClientPreferences.DEFAULT_CLIENT_PREFERENCES;
		assertTrue("Clone method return cllientPreferences with same content", cp.equals(cp.clone()));
		ClientPreferences copy = cp.clone();
		copy.getProjectPreference().clear();
		assertTrue("After removing all component", copy.getProjectPreference().isEmpty());
		assertFalse("Original object with original content", cp.getProjectPreference().isEmpty());
	}
}
