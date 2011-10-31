package crystal.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class ValidInputCheckerTest {

	@Test
	public void testCheckUrl() {
		assertFalse("Check invalid url", ValidInputChecker.checkUrl("http://hahaha"));
		assertFalse("Check invalid url", ValidInputChecker.checkUrl("http://code.google.com/hg/hahaha"));
		
	}
	
	@Test
	public void testCheckValidFilePath() {
		String path = "hi";
		try {
			ValidInputChecker.checkValidFilePath(path);
			fail("Did not throw exception for nonexisting path");
		} catch (IllegalArgumentException e){
		}
		path = "C:/Users/Haochen/Dropbox/crystal/haochen/crystalvc/testDataFile";
		try {
			ValidInputChecker.checkValidFilePath(path);
			fail("Did not throw exception for directory path");
		} catch (IllegalArgumentException e){
		}
		
		path = "C:/Users/Haochen/Dropbox/crystal/haochen/crystalvc/testDataFile/testLoadXml2.xml";
		ValidInputChecker.checkValidFilePath(path);
		path = "C:\\Users\\Haochen\\Dropbox\\crystal\\haochen\\crystalvc\\testDataFile\\testLoadXml2.xml";
		ValidInputChecker.checkValidFilePath(path);
		
	}

	@Test
	public void testCheckValidDirectoryPath() {
		String path = "hi";
		try {
			ValidInputChecker.checkValidDirectoryPath(path);
			fail("Did not throw exception for nonexisting path");
		} catch (IllegalArgumentException e){
		}
		path = "C:/Users/Haochen/Dropbox/crystal/haochen/crystalvc";
		ValidInputChecker.checkValidDirectoryPath(path);
	}

}
