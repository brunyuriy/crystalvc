package crystal.server;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import crystal.model.Relationship;
import crystal.model.RevisionHistory.When;
import crystal.server.AbstractLogParser.Checkpoint;

/**
 * Class GuidanceCheckerTest will test the performance of class
 * GuidanceChecker
 * 
 * @author Haochen
 *
 */
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
		Set<String> me = new TreeSet<String>();
		Set<String> you = new TreeSet<String>();
		Set<String> parent = new TreeSet<String>();
		
		Relationship same = new Relationship(Relationship.SAME, null, null);
		
		Relationship behind = new Relationship(Relationship.BEHIND, null, null);
		Relationship ahead = new Relationship(Relationship.AHEAD, null, null);
		
		assertTrue("Relationship same", GuidanceChecker.getWhen(me, you, parent, same).equals(When.NOTHING));
		
		me.add("a");
		you.add("a");
		you.add("b");
		parent.add("a");
		parent.add("b");
		
		assertTrue("Relationship behind, you have more", GuidanceChecker.getWhen(me, you, parent, behind).equals(When.NOW));
		
		me.add("c");
		assertTrue("Relationship ahead, I have more", GuidanceChecker.getWhen(me, you, parent, ahead).equals(When.NOW));
		
		
	}
	/*
	@Test
	public void testGetConsequences() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCapable() {
		fail("Not yet implemented");
	}
	*/
}
