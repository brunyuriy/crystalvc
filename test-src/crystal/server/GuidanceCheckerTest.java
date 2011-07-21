package crystal.server;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import crystal.server.HgLogParser.Checkpoint;

public class GuidanceCheckerTest {

	@Test
	public void testGetCommitters() {
		File f1 = new File("testDataFile/oneLog.txt");
		File f2 = new File("testDataFile/twoLogs.txt");
		File f3 = new File("testDataFile/threeLogs.txt");
				
		String log_1 = "";
		String log_2 = "";
		String log_3 = "";
		
		try {
			log_1 = FileUtils.readFileToString(f1);
			log_2 = FileUtils.readFileToString(f2);
			log_3 = FileUtils.readFileToString(f3);
			
		} catch (IOException e) {
			System.err.println("File not found");
		}
		
		Map<String, Checkpoint> checkpoints_1 = HgLogParser.parseLog(log_1);
		Map<String, Checkpoint> checkpoints_2 = HgLogParser.parseLog(log_2);
		Map<String, Checkpoint> checkpoints_3 = HgLogParser.parseLog(log_3);
		
		Set<Checkpoint> points_1 = new HashSet<Checkpoint>(checkpoints_1.values());
		Set<Checkpoint> points_2 = new HashSet<Checkpoint>(checkpoints_2.values());
		Set<Checkpoint> points_3 = new HashSet<Checkpoint>(checkpoints_3.values());
		
		String output_1 = GuidanceChecker.getCommitters(points_1);
		String output_2 = GuidanceChecker.getCommitters(points_2);
		String output_3 = GuidanceChecker.getCommitters(points_3);
	
		assertTrue("single log", output_1.equals("georg.brandl"));
		assertTrue("two logs", output_2.equals("Robert Lehmann <mail@robertlehmann.de> and georg.brandl"));
		assertTrue("three logs", output_3.equals("Robert Lehmann <mail@robertlehmann.de>, DasIch <dasdasich@gmail.com>, and georg.brandl"));
	}

	@Test
	public void testGetWhen() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetConsequences() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCapable() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetEase() {
		fail("Not yet implemented");
	}

}
