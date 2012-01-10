package crystal.util;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.Spring;
import javax.swing.SpringLayout;

/**
 * Class SpringLayoutUtility will provide methods needed in using SpringLayout class
 * 
 * @author Haochen
 *
 */

public class SpringLayoutUtility {
	
	  /* Used by makeCompactGrid. */
	private static SpringLayout.Constraints getConstraintsForCell(int row, int col, 
			int totalCols, JPanel panel) {
		SpringLayout layout = (SpringLayout) panel.getLayout();
		Component c = panel.getComponent(row * totalCols + col);
		return layout.getConstraints(c);
	}
	
	/**
	 * Form panel with SpringLayout to grid.
	 * 
	 * @param sourcesPanel panel to reform
	 * @param rows number of rows in the panel
	 * @param cols number of columns in the panel
	 */
	public static void formGridInColumn(JPanel panel, int rows, int cols) {
		if(panel == null || panel.getLayout().getClass() != SpringLayout.class ){
			throw new IllegalArgumentException("Invalid input for panel.");
		}
		
		SpringLayout layout = (SpringLayout) panel.getLayout();

		// Align all cells in each column and make them the same width.
		Spring x = Spring.constant(3);
		for (int col = 0; col < cols; col++) {
			Spring width = Spring.constant(0);
			for (int row = 0; row < rows; row++) {
				width = Spring.max(width, getConstraintsForCell(row, col, cols, panel).getWidth());
			}
			for (int row = 0; row < rows; row++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(row, col, cols, panel);
				constraints.setX(x);
				constraints.setWidth(width);
			}
			x = Spring.sum(x, Spring.sum(width, Spring.constant(3)));
		}

		// Align all cells in each row and make them the same height.
		Spring y = Spring.constant(3);
		for (int row = 0; row < rows; row++) {
			Spring height = Spring.constant(0);
			for (int col = 0; col < cols; col++) {
				height = Spring.max(height, getConstraintsForCell(row, col, cols, panel).getHeight());
			}
			for (int col = 0; col < cols; col++) {
				SpringLayout.Constraints constraints = getConstraintsForCell(row, col, cols, panel);
				constraints.setY(y);
				constraints.setHeight(height);
			}
			y = Spring.sum(y, Spring.sum(height, Spring.constant(3)));
		}

		// Set panel's size
		SpringLayout.Constraints pCons = layout.getConstraints(panel);
		pCons.setConstraint(SpringLayout.SOUTH, y);
		pCons.setConstraint(SpringLayout.EAST, x);
	}
}
