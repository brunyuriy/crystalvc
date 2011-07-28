package crystal.model;

import static org.junit.Assert.*;

import javax.swing.ImageIcon;

import org.junit.Test;


public class RelationshipTest {
	public static final String ERROR = "ERROR error message";
	
	
	@Test
	public void testNullInputConstructor(){
		new Relationship(null, null, null);
	}
	
	@Test(expected = RuntimeException.class)
	public void testStrangeNameConstructor() {
		new Relationship("abc", null, null);
	}
	
	@Test
	public void testConstructor(){
		Relationship errorName = new Relationship(Relationship.ERROR + " error message", null, null);
		assertTrue("error message", errorName.getToolTipText().equals("error message"));
		assertTrue("error name", errorName.getName().equals(Relationship.ERROR));
		
		assertTrue("override icon", errorName.getIcon().equals(new ImageIcon("/crystal/client/images/32X32/must/error.png")));
		
		
		
		Relationship sameName = new Relationship("Same", null, null);
		assertTrue("same name to upper case", sameName.getName().equals(Relationship.SAME));
		
	}

	@Test
	public void testSetReady() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsReady() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetIcon() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetImage() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetCommitters() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCommitters() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetWhen() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetWhen() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetCapable() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCapable() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetEase() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetEase() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetConsequences() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetConsequences() {
		fail("Not yet implemented");
	}

	@Test
	public void testCalculateAction() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAction() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetActionRepoKind() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetToolTipText() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDominant() {
		fail("Not yet implemented");
	}

	@Test
	public void testToString() {
		fail("Not yet implemented");
	}

	@Test
	public void testEqualsObject() {
		fail("Not yet implemented");
	}

}
