package crystal.model;

import static org.junit.Assert.*;

import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import crystal.CrystalTest;
import crystal.model.DataSource.RepoKind;
import crystal.model.RevisionHistory.Action;

/**
 * Class RelationshipTest will test the performance of class Relationship
 * 
 * @author Haochen
 *
 */

public class RelationshipTest extends CrystalTest {
	public static final String ERROR = "ERROR error message";
	
	@Test(expected = RuntimeException.class)
	public void testStrangeNameConstructor() {
		new Relationship("abc", null, null);
	}
	
	@Test
	public void testConstructor(){
		Relationship errorName = new Relationship(Relationship.ERROR + " error message", null, null);
		assertTrue("error message", errorName.getToolTipText().equals("error message"));
		assertTrue("error name", errorName.getName().equals(Relationship.ERROR));
		
		
		Relationship sameName = new Relationship("Same", null, null);
		assertTrue("same name to upper case", sameName.getName().equals(Relationship.SAME));
		assertFalse("Is not ready", sameName.isReady());
		assertNotNull("Set Icon", sameName.getIcon());
		assertNotNull("Set Image", sameName.getImage());
	}

	@Test
	public void testCalculateAction() {
		Relationship temp1 = new Relationship(Relationship.AHEAD, null, null);
		assertNull("Call getAction before calling calculateAction", temp1.getAction());
		
		temp1.calculateAction(LocalStateResult.PENDING, new Relationship(Relationship.AHEAD, null, null));
		assertTrue("After calculating action", temp1.getAction().equals(Action.UNKNOWN));
		
		temp1.calculateAction(null, new Relationship(Relationship.AHEAD, null, null));
		assertNull("When localState is null: " + temp1.getAction(), temp1.getAction());
	}


	@Test
	public void testGetActionRepoKind() {
		Relationship temp1 = new Relationship(Relationship.AHEAD, null, null);
		
		assertTrue("Get action when action is null", temp1.getAction(RepoKind.HG).equals("cannot compute hg action"));
		
		temp1.calculateAction(LocalStateResult.PENDING, null);
		assertTrue("Get action when action is UNKNOWN", temp1.getAction(RepoKind.HG).equals("not computed"));
		
		temp1.calculateAction("", new Relationship(Relationship.SAME, null, null));
		assertNull("Get action when action is NOTHING", temp1.getAction(RepoKind.HG));
	}


	@Test
	public void testGetDominant() {
		Set<Relationship> temp1 = new TreeSet<Relationship>();
		assertNull("Empty relationships collection", Relationship.getDominant(temp1));
		//TODO add more test
	}

	@Test
	public void testEqualsObject() {
		Relationship temp = new Relationship(Relationship.AHEAD, null, null);
		assertFalse("Compare with null", temp.equals(null));
		assertFalse("Compare with other object", temp.equals("temp"));
		
		Relationship temp2 = new Relationship(Relationship.ERROR, null, null);
		assertFalse("Compare with different name relationship", temp.equals(temp2));
		
		Relationship temp3 = new Relationship(Relationship.ERROR, null, null);
		assertTrue("Compare with same name relationship", temp2.equals(temp3));

	}

}
