package crystal.util;

import static org.junit.Assert.*;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.SpringLayout;

import org.junit.Test;

/**
 * Class SpringLayoutUtilityTest will test the performance of class
 * SpringLayoutUtility
 * @author Haochen
 *
 */

public class SpringLayoutUtilityTest {

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidPanelParameter(){
		JPanel temp = new JPanel(new BorderLayout());
		SpringLayoutUtility.formGridInColumn(temp, 2, 2);
	}

	
	@Test
	public void testFormGrid() {
		//SpringLayoutUtility.formGrid(temp, 2, 2);
		//TODO
	}

}
