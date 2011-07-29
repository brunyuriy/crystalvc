package crystal.model;

import static org.junit.Assert.*;

import org.junit.Test;

import crystal.model.DataSource.RepoKind;

/**
 * Class LocalStateResultTest will test the performance of 
 * class LocalStateResult.
 * 
 * @author Haochen
 *
 */
public class LocalStateResultTest {


	public static LocalStateResult localState;
	
	@Test(expected = IllegalArgumentException.class)
	public void testNullInputConstructor() {
		new LocalStateResult(null, null, null, null, null);
	}
	
	@Test
	public void testLocalStateResult() {
		String name = LocalStateResult.TEST;
		String lastState = "lastState";
		String lastAction = "lastAction";
		String lastErrorMessage = "lastErrorMessage";
		
		localState = new LocalStateResult(new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"), 
				name, lastState, lastAction, lastErrorMessage);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testStrangeActionName(){
		String name = "abc";
		String lastState = "lastState";
		String lastAction = "lastAction";
		String lastErrorMessage = "lastErrorMessage";
		
		new LocalStateResult(new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"), 
				name, lastState, lastAction, lastErrorMessage);
	
	}
	
	@Test
	public void testErrorActionNameConstructor(){
		String name = LocalStateResult.ERROR;
		String lastState = "lastState";
		String lastAction = "lastAction";
		String lastErrorMessage = "lastErrorMessage";
		
		LocalStateResult temp = new LocalStateResult(new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"), 
				name, lastState, lastAction, lastErrorMessage);
		
		assertTrue("Error name", temp.getName().equals("ERROR"));
		assertNull("action with error name", temp.getAction());
	}

	@Test
	public void testGetAction() {
		assertTrue("Action", localState.getAction().equals(""));
		
	}

	@Test
	public void testGetLastAction() {
		assertTrue("last action", localState.getLastAction().equals("lastAction"));
	}

	@Test
	public void testGetName() {
		assertTrue("name", localState.getName().equals(LocalStateResult.TEST));
	}

	@Test
	public void testGetNoErrorMessage() {
		assertNull("no error message", localState.getErrorMessage());
	}
	
	

	@Test
	public void testGetLastErrorMessage() {
		assertTrue("last error message", localState.getLastErrorMessage().equals("lastErrorMessage"));
	}

	@Test
	public void testToString() {
		assertTrue("string representation", localState.toString().equals("LocalStateResult - "
				+ "shortName" + " state: " + LocalStateResult.TEST + " and last state: " + "lastState" + "."));
	}

	@Test
	public void testGetLocalState() {
		assertTrue("local state", localState.getLocalState().equals(LocalStateResult.TEST));
	}

	@Test
	public void testGetLastLocalState() {
		assertTrue("last state", localState.getLastLocalState().equals("lastState"));
	}

}
