package crystal.server;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import crystal.server.AbstractLogParser.Checkpoint;

/**
 * Class HgLogParserTest will test the performance of class HgLogParser
 * @author Haochen
 *
 */
public class HgLogParserTest {

	@Test(expected = IllegalArgumentException.class)
	public void testParseNullLog() {
		HgLogParser.parseLog(null);

	}
	
	@Test
	public void testParseLog(){
		
		File f1 = new File("testDataFile/testLogVersion1.txt");
	
		String log = "";
		try {
			log = FileUtils.readFileToString(f1);
		} catch (IOException e) {
			System.err.println("File not found");
		}
		
		Map<String, Checkpoint> checkPoints = HgLogParser.parseLog(log);
		
		Scanner input;
		int count = 0;
		try {
			input = new Scanner(f1);
		} catch (FileNotFoundException e) {
			input = null;
			System.err.println("File not found");
			
		}

		
		while(input.hasNextLine()){
			String line = input.nextLine();
			if(line.startsWith("changeset")){
				String temp = line.replace(':', ' ');
				String[] arr = temp.split(" +");
				assertTrue("contain ids", checkPoints.keySet().contains(arr[2]));
				count++;
			}
		}
		assertEquals(count, checkPoints.keySet().size());
		
		Checkpoint c1 = checkPoints.get("ce4bb37a1409");
		assertNotNull(c1);
		assertTrue("compare changeset", c1.getChangeset().equals("ce4bb37a1409"));
		assertTrue("compare user", c1.getCommitter().equals("Daniel Neuhäuser <dasdasich@gmail.com>"));
		assertTrue("comepare date", c1.getDate().equals("Sat Feb 05 21:48:54 2011 +0100"));
		assertTrue("compare summary", c1.getSummary().equals("Change redirected URLs to the new locations"));
		assertTrue("compare parent", c1.getParents().contains("96b7b2b849c3"));
		
		Checkpoint c2 = checkPoints.get("825c5438dfd3");
		assertNotNull(c2);
		assertTrue("compare user", c2.getCommitter().equals("Georg Brandl <georg@python.org>"));
		assertTrue("compare date", c2.getDate().equals("Sat Jan 15 17:22:13 2011 +0100"));
		assertTrue("compare summary", c2.getSummary().equals("merge with 1.0"));
		assertTrue("have parent", c2.getParents().contains("3224:86e6811aec45"));
		assertTrue("have another parent", c2.getParents().contains("3228:e718cc9843bc"));
		
		System.out.println(c2.getParents());
	}
	
	@Test
	public void testSingleLog(){
		File f1 = new File("testDataFile/oneLog.txt");
		
		String log = "";
		try {
			log = FileUtils.readFileToString(f1);
		} catch (IOException e) {
			System.err.println("File not found");
		}
		
		Map<String, Checkpoint> checkPoints = HgLogParser.parseLog(log);
		Checkpoint c1 = checkPoints.get("79fafafe8370");
		assertNotNull(c1);
		assertTrue("compare changeset", c1.getChangeset().equals("79fafafe8370"));
		assertTrue("compare user", c1.getCommitter().equals("georg.brandl"));
		assertTrue("comepare date", c1.getDate().equals("Mon Jul 23 08:56:48 2007 +0000"));
		assertTrue("compare summary", c1.getSummary().equals("Make trunk, branches, tags directories."));

		
	}
	
	@Test
	public void testTwoLogs(){
		File f1 = new File("testDataFile/threeLogs.txt");

		String log = "";
		try {
			log = FileUtils.readFileToString(f1);
		} catch (IOException e) {
			System.err.println("File not found");
		}
		
		Map<String, Checkpoint> checkPoints = HgLogParser.parseLog(log);
		Checkpoint c1 = checkPoints.get("ae6decc968d5");
		Checkpoint c2 = checkPoints.get("face29593d75");
		Checkpoint c3 = checkPoints.get("79fafafe8370");
		assertNotNull(c1);
		assertNotNull(c2);
		assertNotNull(c3);

		assertTrue("compare changeset", c1.getChangeset().equals("ae6decc968d5"));
		assertTrue("compare user", c1.getCommitter().equals("Robert Lehmann <mail@robertlehmann.de>"));
		assertTrue("comepare date", c1.getDate().equals("Sat Nov 13 12:59:12 2010 +0100"));
		assertTrue("compare summary", c1.getSummary().equals("Tested section grouping for translations."));

		assertTrue("compare changeset", c2.getChangeset().equals("face29593d75"));
		assertTrue("compare user", c2.getCommitter().equals("DasIch <dasdasich@gmail.com>"));
		assertTrue("comepare date", c2.getDate().equals("Sun Aug 15 20:48:19 2010 +0200"));
		assertTrue("compare summary", c2.getSummary().equals("Use uuid as a requirement in the setup.py and mention it in the documentation for Python 2.4"));

		
		
	}

}
