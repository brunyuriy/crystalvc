package crystal.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import crystal.CrystalTest;

public class ValidInputCheckerTest extends CrystalTest {

	@Test
	public void testCheckUrl() {
		assertFalse("Check invalid url", ValidInputChecker.checkUrl("http://does not exist"));
		assertTrue("Check invalid url", ValidInputChecker.checkUrl("http://code.google.com/hg/does not exist"));

	}

	@Test
	public void testCheckValidFilePath() {
		String path = "abc";
		try {
			ValidInputChecker.checkValidFilePath(path);
			fail("Did not throw exception for nonexisting path");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testCheckValidDirectoryPath() {
		String path = "abc";
		try {
			ValidInputChecker.checkValidDirectoryPath(path);
			fail("Did not throw exception for nonexisting path");
		} catch (IllegalArgumentException e) {
		}

	}

}
