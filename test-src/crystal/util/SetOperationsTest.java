package crystal.util;

import static org.junit.Assert.*;

import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

/**
 * Class SetOperationsTest will test the performance of class SetOperations
 * @author Haochen
 *
 */
public class SetOperationsTest {

	
	@Test (expected = IllegalArgumentException.class)
	public void testNullIntersection(){
		SetOperations.intersection(null, null);
	}
	
	
	@Test
	public void testIntersection() {
		Set<String> inter1 = new TreeSet<String>();
		Set<String> inter2 = new TreeSet<String>();
		
		inter1.add("inter");
		inter2.add("inter2");
		
		Set<String> output1 = SetOperations.intersection(inter1, inter2);
		assertTrue("no intersection", output1.isEmpty());
		
		inter2.add("inter");
		
		Set<String> output2 = SetOperations.intersection(inter2, inter1);
		assertTrue("Intersect one element", output2.contains("inter"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNullUnion(){
		SetOperations.union(null, null);
	}
	
	@Test
	public void testUnion() {
		Set<String> union1 = new TreeSet<String>();
		Set<String> union2 = new TreeSet<String>();
		
		union1.add("union");
		union2.add("union2");
		
		Set<String> output1 = SetOperations.union(union1, union2);
		assertTrue("Union two sets", !output1.isEmpty());
		assertTrue("Union duplicate element", output1.contains("union"));
		assertTrue("Union duplicate element", output1.contains("union2"));
		
		
		Set<String> output2 = SetOperations.union(union2, union1);
		assertTrue("Union duplicate element", output2.contains("union"));
		assertTrue("Union duplicate element", output2.contains("union2"));
		assertTrue("Union duplicate element", output2.size() == 2);
	}

	
	
	@Test
	public void testSetDifference() {
		Set<String> dif1 = new TreeSet<String>();
		Set<String> dif2 = new TreeSet<String>();
		
		dif1.add("dif");
		dif2.add("dif2");
		
		Set<String> output1 = SetOperations.setDifference(dif1, dif2);
		assertTrue("Difference of two sets", !output1.isEmpty());
		
		assertTrue("Difference of two sets", output1.contains("dif"));
		assertTrue("Difference of two sets", !output1.contains("dif2"));
		
		Set<String> output2 = SetOperations.setDifference(dif2, dif2);
		assertTrue("Same sets", output2.isEmpty());
		
		
		Set<String> output3 = SetOperations.setDifference(dif2, dif1);
		assertTrue("Difference of two sets", !output3.isEmpty());
		assertTrue("Difference of two sets", !output3.contains("dif"));
		assertTrue("Difference of two sets", output3.contains("dif2"));
	}

	@Test
	public void testXor() {
		Set<String> xor1 = new TreeSet<String>();
		Set<String> xor2 = new TreeSet<String>();
		
		xor1.add("xor");
		xor2.add("xor2");
		
		Set<String> output1 = SetOperations.xor(xor1, xor2);
		assertTrue("Xor of two sets", !output1.isEmpty());
		
		assertTrue("Xor of two sets", output1.contains("xor"));
		assertTrue("Xor of two sets", output1.contains("xor2"));
	}

}
