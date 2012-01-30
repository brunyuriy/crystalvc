package crystal.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import crystal.CrystalTest;
import crystal.model.DataSource.RepoKind;

/**
 * Class LocalStateResultTest will test the performance of 
 * class LocalStateResult.
 * 
 * @author Haochen
 *
 */
public class LocalStateResultTest extends CrystalTest {


	public static LocalStateResult localState;
	
	/**
	 * set up 
	 */
	@Before
	public void testLocalStateResult() {
		String name = LocalStateResult.TEST;
		String lastState = "lastState";
		String lastAction = "lastAction";
		String lastErrorMessage = "lastErrorMessage";
		
		localState = new LocalStateResult(new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"), 
				name, lastState, lastAction, lastErrorMessage);
	}
	
	/**
	 * test if the name of action is not in the list of actions
	 */
	@Test
	public void testStrangeActionName(){
		String name = "abc";
		String lastState = "lastState";
		String lastAction = "lastAction";
		String lastErrorMessage = "lastErrorMessage";
		
		LocalStateResult temp = new LocalStateResult(new DataSource("shortName", "cloneString", RepoKind.HG, false, "parent"), 
				name, lastState, lastAction, lastErrorMessage);
		assertNull("strange action", temp.getAction());
	}
	
	/**
	 * test if the action name is ERROR
	 */
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

	/**
	 * test getAction method
	 */
	@Test
	public void testGetAction() {
		assertTrue("Action", localState.getAction().equals(""));
		
	}

	/**
	 * test getLastAction
	 */
	@Test
	public void testGetLastAction() {
		assertTrue("last action", localState.getLastAction().equals("lastAction"));
	}

	/**
	 * test getName
	 */
	@Test
	public void testGetName() {
		assertTrue("name", localState.getName().equals(LocalStateResult.TEST));
	}

	/**
	 * test getErrorMessage 
	 */
	@Test
	public void testGetNoErrorMessage() {
		assertNull("no error message", localState.getErrorMessage());
	}
	
	
	/**
	 * test getLastErrorMessage
	 */
	@Test
	public void testGetLastErrorMessage() {
		assertTrue("last error message", localState.getLastErrorMessage().equals("lastErrorMessage"));
	}

	/**
	 * test toString format
	 */
	@Test
	public void testToString() {
		assertTrue("string representation", localState.toString().equals("LocalStateResult - "
				+ "shortName" + " state: " + LocalStateResult.TEST + " and last state: " + "lastState" + "."));
	}

	/**
	 * test getLocalState
	 */
	@Test
	public void testGetLocalState() {
		assertTrue("local state", localState.getLocalState().equals(LocalStateResult.TEST));
	}

	/**
	 * test getLastLocalState
	 */
	@Test
	public void testGetLastLocalState() {
		assertTrue("last state", localState.getLastLocalState().equals("lastState"));
	}

}
