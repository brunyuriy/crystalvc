package crystal;

import org.junit.Ignore;

import crystal.client.ConflictSystemTray;

/**
 * This this is currently a terrible idea, but hopefully this class can be used for something more than its current shell.
 * 
 * @author rtholmes
 * 
 */
@Ignore
public abstract class CrystalTest {

	static {
		ConflictSystemTray.startLogging();
	}

}
