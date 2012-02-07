package crystal.server;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import crystal.CrystalTest;
import crystal.server.AbstractLogParser.Checkpoint;

/**
 * Class HgLogParserTest will test the performance of class HgLogParser
 * @author Haochen
 *
 */
public class HgLogParserTest extends CrystalTest {

	/**
	 * null input for parseLog
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testParseNullLog() {
		HgLogParser.parseLog(null);

	}
	
	/**
	 * test parseLog
	 */
	@Test
	public void testParseLog(){
		
		File f1 = new File("testDataFile/reversedLog.txt");
	
		String log = "";
		try {
			log = FileUtils.readFileToString(f1);
		} catch (IOException e) {
			System.err.println("File not found");
		}
		
		// get log from input file
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
		
		// check the content for each checkpoint is correct
		Checkpoint c1 = checkPoints.get("790cc36697aa");
		assertNotNull(c1);
		assertTrue("compare changeset", c1.getChangeset().equals("790cc36697aa"));
		assertTrue("compare user", c1.getCommitter().equals("Yuriy Brun <brun@cs.washington.edu>"));
		assertTrue("comepare date", c1.getDate().equals("Sun Jun 27 13:16:15 2010 -0700"));
		assertTrue("compare summary", c1.getSummary().equals("Created the skeleton code for a project that has multiple clones and basic clone/repository set up."));
		// first changeset should not have any parent
		assertTrue("compare parent", c1.getParents().isEmpty());
		
		Checkpoint c2 = checkPoints.get("703f0cd325c3");
		assertNotNull(c2);
		assertTrue("compare user", c2.getCommitter().equals("Yuriy Brun <brun@cs.washington.edu>"));
		assertTrue("compare date", c2.getDate().equals("Sun Jun 27 14:52:08 2010 -0700"));
		assertTrue("compare summary", c2.getSummary().equals("Merged in the notificationEmails file"));
		assertTrue("have parent", c2.getParents().contains("790cc36697aa"));
		assertTrue("have another parent", c2.getParents().contains("b8a65626740e"));
	}
	
	/**
	 * test only one log
	 */
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
	
	/**
	 * test two logs
	 */
	@Test
	public void testTwoLogs(){
		File f1 = new File("testDataFile/threeLogs.txt");

		String log = "";
		try {
			log = FileUtils.readFileToString(f1);
		} catch (IOException e) {
			System.err.println("File not found");
		}
		// input from a file with three checkpoints
		Map<String, Checkpoint> checkPoints = HgLogParser.parseLog(log);
		Checkpoint c1 = checkPoints.get("ae6decc968d5");
		Checkpoint c2 = checkPoints.get("face29593d75");
		Checkpoint c3 = checkPoints.get("79fafafe8370");
		assertNotNull(c1);
		assertNotNull(c2);
		assertNotNull(c3);

		// test the correctness of each checkpoint
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
