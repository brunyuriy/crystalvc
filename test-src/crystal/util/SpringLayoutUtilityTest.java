package crystal.util;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.junit.Test;

import crystal.CrystalTest;

/**
 * Class SpringLayoutUtilityTest will test the performance of class SpringLayoutUtility
 * 
 * @author Haochen
 * 
 */

public class SpringLayoutUtilityTest extends CrystalTest {

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidPanelParameter() {
		JPanel temp = new JPanel(new BorderLayout());
		SpringLayoutUtility.formGridInColumn(temp, 2, 2);
	}

	@Test
	public void testFormGrid() {
		// SpringLayoutUtility.formGrid(temp, 2, 2);
		// TODO
	}

}
