package crystal.client;

import static org.junit.Assert.*;

import org.junit.Test;

import crystal.CrystalTest;

public class PreferencesGUIEditorFrameTest extends CrystalTest {

	@Test
	public void testGetPreferencesGUIEditorFrameClientPreferences() {
		
		assertNull("Before creating frame", PreferencesGUIEditorFrame.getPreferencesGUIEditorFrame());
		
		ClientPreferences tempCP = ClientPreferences.DEFAULT_CLIENT_PREFERENCES;
		PreferencesGUIEditorFrame frame = PreferencesGUIEditorFrame.getPreferencesGUIEditorFrame(tempCP);
		assertNotNull("Created new frame", frame);
		assertNotNull("Get instance of previous frame", PreferencesGUIEditorFrame.getPreferencesGUIEditorFrame());
		
	}
	
	//TODO
	// check the performance

}
