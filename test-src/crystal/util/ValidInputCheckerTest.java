package crystal.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import crystal.CrystalTest;
/**
 * Test ValidInputChecker class
 * 
 * @author Haochen
 *
 */
public class ValidInputCheckerTest extends CrystalTest {

	/**
	 * test checkUrl
	 */
	@Test
	public void testCheckUrl() {
		// test invalid url
		assertFalse("Check invalid url", ValidInputChecker.checkUrl("http://does not exist"));
		// test valid url
		assertTrue("Check invalid url", ValidInputChecker.checkUrl("http://code.google.com/hg/does not exist"));

	}

	/**
	 * test checkValidFilePath
	 */
	@Test
	public void testCheckValidFilePath() {
		// invalid path
		String path = "abc";
		try {
			ValidInputChecker.checkValidFilePath(path);
			fail("Did not throw exception for nonexisting path");
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * test checkValidDirectoryPath
	 */
	@Test
	public void testCheckValidDirectoryPath() {
		// invalid path
		String path = "abc";
		try {
			ValidInputChecker.checkValidDirectoryPath(path);
			fail("Did not throw exception for nonexisting path");
		} catch (IllegalArgumentException e) {
		}

	}

}
