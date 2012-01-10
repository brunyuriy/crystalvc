package crystal.client;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import crystal.Constants;
import crystal.CrystalTest;
import crystal.model.DataSource;
import crystal.model.DataSource.RepoKind;
import crystal.model.LocalStateResult;
import crystal.model.Relationship;
import crystal.server.HgStateChecker;


/**
 * Class ConflictDaemonTest will test the performance of class
 * ConflictDaemon
 * 
 * @author Haochen
 *
 */
public class ConflictDaemonTest extends CrystalTest {

	
	@Test
	public void testGetInstance() {
		ConflictDaemon instance = ConflictDaemon.getInstance();
		assertNotNull(instance);
	}


	@Test
	public void testPrePerformCalculations() {
		ConflictDaemon instance = ConflictDaemon.getInstance();
		ClientPreferences cp_1 = ClientPreferences.DEFAULT_CLIENT_PREFERENCES;
		
		instance.prePerformCalculations(cp_1);
		assertEquals(cp_1.getProjectPreference().size(), instance.getLocalStates().size());
		
	}
	
	@Test
	public void testLocalState() throws IOException {
		ConflictDaemon instance = ConflictDaemon.getInstance();
		DataSource source1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent");
		LocalStateResult local1 = instance.getLocalState(source1);
		assertNotNull("When input data source is not contained", local1);
		assertEquals("After calculating a local state", 1, instance.getLocalStates().size());
		
		DataSource data_1 = new DataSource("shortName_2", "cloneString_2", RepoKind.HG, false, "parent_2"); 
		ClientPreferences cp_1 = new ClientPreferences("tempDirectory", "hgPath", "gitPath", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences pp = new ProjectPreferences(data_1,	cp_1);
		
		String localState = HgStateChecker.getLocalState(pp);
		
		instance.calculateLocalState(pp);


		assertEquals("After calculating a local state", 2, instance.getLocalStates().size());
		assertTrue("After calculating a local state", 
				instance.getLocalStates().contains(new LocalStateResult(pp.getEnvironment(), localState, null, null, null)));
	}

	@Test
	public void testRelationship() {
		ConflictDaemon instance = ConflictDaemon.getInstance();
		DataSource source1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent");
		Relationship relationship1 = instance.getRelationship(source1);
		assertNotNull("When input data source is not contained", relationship1);
		assertTrue("When input data source is not contained, it's relationship is pending", 
				relationship1.getName().equals(Relationship.PENDING));
		
		assertEquals("non listeners", 0, instance._listeners.size());
		
		
		DataSource data_1 = new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"); 
		ClientPreferences cp_1 = new ClientPreferences("tempDirectory", "hgPath", "gitPath", Constants.DEFAULT_REFRESH);
		
		ProjectPreferences pp = new ProjectPreferences(data_1,	cp_1);

		String relation = HgStateChecker.getRelationship(pp, data_1, Relationship.PENDING);

		instance.calculateRelationship(data_1, pp);
		
		assertTrue("After calculating a relation with data source not contained in relationship map.", 
				instance.getRelationships().contains(new Relationship(relation, null, null)));
		
		
		instance.getRelationship(data_1);
		instance.calculateRelationship(data_1, pp);
		
		assertTrue("After calculating a relation with data source contained in relationship map.", 
				instance.getRelationships().contains(new Relationship(relation, null, null)));
		
		DataSource data_Git = new DataSource("shortName", "cloneString", RepoKind.GIT, false, "parent");
		ProjectPreferences pp_Git = new ProjectPreferences(data_Git, cp_1);

		
		Relationship relationship_Git = instance.calculateRelationship(data_Git, pp_Git);
		assertNull("Git repository kind", relationship_Git);
		
	}


	@Test
	public void testAddListener() {
		ConflictDaemon instance = ConflictDaemon.getInstance();
		ConflictDaemon.ComputationListener l1 = new ConflictClient();
		instance.addListener(l1);
		assertEquals("After adding one listener", 1, instance._listeners.size());
		instance.addListener(new ConflictDaemon.ComputationListener() {
			@Override
			public void update() {				
			}
		});
		assertEquals("After adding two listeners", 2, instance._listeners.size());
		instance.addListener(l1);
		assertEquals("After adding same listeners", 2, instance._listeners.size());
	}
	
}
