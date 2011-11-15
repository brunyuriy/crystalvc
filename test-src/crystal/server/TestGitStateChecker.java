package crystal.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import crystal.Constants;
import crystal.client.ClientPreferences;
import crystal.client.ConflictSystemTray;
import crystal.client.ProjectPreferences;
import crystal.model.DataSource;
import crystal.model.Relationship;
import crystal.model.DataSource.RepoKind;
import crystal.util.RunIt;

/**
 * 
 * @author Haochen
 *
 */

public class TestGitStateChecker {
	private ProjectPreferences _prefs;
	
	public TestGitStateChecker() {
		ConflictSystemTray.startLogging();
		generatePreferences();	
	}
	

	public ProjectPreferences getPreferences() {
		return _prefs;
	}

	@AfterClass
	/**
	 * Clean up from the tests removing old directories
	 */
	public static void cleanEnvironment() {
		String projectPath = TestConstants.PROJECT_PATH;

		// clear the output location
		File repoDir = new File(projectPath + TestConstants.GIT_TEST_REPOS);
		if (repoDir.exists()) {
			Assert.assertTrue(repoDir.isDirectory());
			RunIt.deleteDirectory(repoDir);
			Assert.assertFalse(repoDir.exists());
		}

		// clean the temp space
		File testTempDir = new File(projectPath + TestConstants.GIT_TEST_TEMP);
		if (testTempDir.exists()) {
			RunIt.deleteDirectory(testTempDir);
			Assert.assertFalse(testTempDir.exists());
		}
	}

	/**
	 * Rebuild the test environment by erasing the old one and extracting a new set of repositories from a zip file.
	 */
	@BeforeClass
	public static void ensureEnvironment() {
		String projectPath = TestConstants.PROJECT_PATH;
		Assert.assertNotNull(projectPath);

		File pp = new File(projectPath);
		Assert.assertTrue(pp.exists());
		Assert.assertTrue(pp.isDirectory());

		File[] files = pp.listFiles();
		Assert.assertNotNull(files);


		// make sure the repo zip file exists
		File repoZipFile = null;
		for (File f : files) {
			if (f.getAbsolutePath().endsWith("gittestproject.zip"))
				repoZipFile = f;
			if (f.getAbsolutePath().endsWith(TestConstants.GIT_TEST_REPOS) && f.isDirectory()) {
				// not sure what the significance of this test is anymore
			}

		}
		Assert.assertNotNull(repoZipFile);

		// clear the output location
		File repoDir = new File(projectPath + TestConstants.GIT_TEST_REPOS);
		if (repoDir.exists()) {
			Assert.assertTrue(repoDir.isDirectory());
			RunIt.deleteDirectory(repoDir);
			Assert.assertFalse(repoDir.exists());
		}

		// unzip the repo zip into the directory
		File zipOutDir = pp;
		unzipTestRepositories(repoZipFile, zipOutDir);
		Assert.assertTrue(repoDir.exists());

		// clean the temp space
		File testTempDir = new File(projectPath + TestConstants.GIT_TEST_TEMP);
		if (testTempDir.exists()) {
			RunIt.deleteDirectory(testTempDir);
			Assert.assertFalse(testTempDir.exists());
		}
		boolean testTempDirCreated = testTempDir.mkdir();
		Assert.assertTrue(testTempDirCreated);
		Assert.assertTrue(testTempDir.exists());
		Assert.assertTrue(testTempDir.isDirectory());

	}

	@SuppressWarnings("unchecked")
	private static void unzipTestRepositories(File repoZipFile, File zipOutDir) {
		try {

			String outPath = zipOutDir.getAbsolutePath();
			if (!outPath.endsWith(File.separator))
				outPath += File.separator;

			System.out.println("Unzipping repository to: " + outPath);

			ZipFile zipFile = new ZipFile(repoZipFile);

			Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();

				if (entry.isDirectory()) {
					File outDir = new File(outPath + entry.getName());

					outDir.mkdirs();
					continue;
				}

				copyInputStream(zipFile.getInputStream(entry),
						new BufferedOutputStream(new FileOutputStream(outPath + entry.getName())));
			}

			zipFile.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			Assert.fail(ioe.getMessage());
		}
		System.out.println("Unzipping repository complete.");
	}

	private static final void copyInputStream(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}

	@Before
	public void generatePreferences() {
		String path = TestConstants.PROJECT_PATH + TestConstants.GIT_TEST_REPOS;

		DataSource me = new DataSource("me", path + "me", RepoKind.GIT, false, null);
		String tempDirectory = TestConstants.PROJECT_PATH + TestConstants.GIT_TEST_TEMP;

		DataSource ahead = new DataSource("ahead", path + "ahead", RepoKind.GIT, false, null);
		DataSource behind = new DataSource("behind", path + "behind", RepoKind.GIT, false, null);
		DataSource conflict = new DataSource("conflict", path + "conflict", RepoKind.GIT, false, null);
		DataSource merge = new DataSource("merge", path + "merge", RepoKind.GIT, false, null);
		DataSource same = new DataSource("same", path + "same", RepoKind.GIT, false, null);
		String gitCommand = RunIt.getExecutable("git");
		ClientPreferences prefs = new ClientPreferences(tempDirectory, "hgPath", gitCommand, Constants.DEFAULT_REFRESH);

		_prefs = new ProjectPreferences(me, prefs);

		_prefs.addDataSource(ahead);
		_prefs.addDataSource(behind);
		_prefs.addDataSource(conflict);
		_prefs.addDataSource(merge);
		_prefs.addDataSource(same);

		try {
			GitStateChecker.getLocalState(_prefs);
		} catch (IOException e) {
			Assert.fail();
		}
	}

	@Test
	public void testBasicMergeConflict() {
		String answer = GitStateChecker.getRelationship(_prefs, _prefs.getDataSource("conflict"), null);
		Assert.assertEquals(Relationship.MERGECONFLICT, answer);
	}

	@Test
	public void testBasicCleanMerge() {
		String answer = GitStateChecker.getRelationship(_prefs, _prefs.getDataSource("merge"), null);
		Assert.assertEquals(Relationship.MERGECLEAN, answer);
	}

	@Test
	public void testBasicAhead() {
		String answer = GitStateChecker.getRelationship(_prefs, _prefs.getDataSource("ahead"), null);
		Assert.assertEquals(Relationship.AHEAD, answer);
	}

	@Test
	public void testBasicBehind() {
		String answer = GitStateChecker.getRelationship(_prefs, _prefs.getDataSource("behind"), null);
		Assert.assertEquals(Relationship.BEHIND, answer);
	}

	@Test
	public void testBasicSame() {
		String answer = GitStateChecker.getRelationship(_prefs, _prefs.getDataSource("same"), null);
		Assert.assertEquals(Relationship.SAME, answer);
	}

	
}
