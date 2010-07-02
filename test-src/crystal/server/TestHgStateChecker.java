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

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import crystal.Constants;
import crystal.client.ClientPreferences;
import crystal.model.DataSource;
import crystal.model.ConflictResult.ResultStatus;
import crystal.model.DataSource.RepoKind;

public class TestHgStateChecker {

	private ClientPreferences _prefs;

	public TestHgStateChecker() {
		generatePreferences();
	}

	public ClientPreferences getPreferences() {
		return _prefs;
	}

	@BeforeClass
	public static void ensureEnvironment() {
		String projectPath = Constants.PROJECT_PATH;
		Assert.assertNotNull(projectPath);

		File pp = new File(projectPath);
		Assert.assertTrue(pp.exists());
		Assert.assertTrue(pp.isDirectory());

		File[] files = pp.listFiles();
		Assert.assertNotNull(files);

		// make sure the repo zip file exists
		File repoZipFile = null;
		File repoTestDirectory = null;
		for (File f : files) {
			if (f.getAbsolutePath().endsWith("test-repos.zip"))
				repoZipFile = f;
			if (f.getAbsolutePath().endsWith(Constants.TEST_REPOS) && f.isDirectory())
				repoTestDirectory = f;
		}
		Assert.assertNotNull(repoZipFile);

		// clear the output location
		File repoDir = new File(projectPath + Constants.TEST_REPOS);
		if (repoDir.exists()) {
			Assert.assertTrue(repoDir.isDirectory());
			deleteDirectory(repoDir);
			Assert.assertFalse(repoDir.exists());
		}

		// unzip the repo zip into the directory
		File zipOutDir = pp;
		unzipTestRepositories(repoZipFile, zipOutDir);
		Assert.assertTrue(repoDir.exists());

		// clean the temp space
		File testTempDir = new File(projectPath + Constants.TEST_TEMP);
		if (testTempDir.exists()) {
			deleteDirectory(testTempDir);
			Assert.assertFalse(testTempDir.exists());
		}
		boolean testTempDirCreated = testTempDir.mkdir();
		Assert.assertTrue(testTempDirCreated);
		Assert.assertTrue(testTempDir.exists());
		Assert.assertTrue(testTempDir.isDirectory());

	}

	static private boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	@SuppressWarnings("unchecked")
	private static void unzipTestRepositories(File repoZipFile, File zipOutDir) {
		try {

			String outPath = zipOutDir.getAbsolutePath();
			if (!outPath.endsWith(File.separator))
				outPath += File.separator;

			System.out.println("Unzipping repository to: " + outPath);

			ZipFile zipFile = new ZipFile(repoZipFile);

			Enumeration entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();

				if (entry.isDirectory()) {
					File outDir = new File(outPath + entry.getName());

					boolean dirsCreated = outDir.mkdirs();
					continue;
				}

				copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(outPath + entry.getName())));
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
		String path = Constants.PROJECT_PATH + Constants.TEST_REPOS;

		DataSource myEnvironment = new DataSource("myRepository", path + "one", RepoKind.HG);
		String tempDirectory = Constants.PROJECT_PATH + Constants.TEST_TEMP;

		DataSource twoSource = new DataSource("twoRepository", path + "two", RepoKind.HG);
		DataSource threeSource = new DataSource("threeRepository", path + "three", RepoKind.HG);
		DataSource fourSource = new DataSource("fourRepository", path + "four", RepoKind.HG);
		DataSource fiveSource = new DataSource("fiveRepository", path + "five", RepoKind.HG);

		_prefs = new ClientPreferences(myEnvironment, tempDirectory);
		_prefs.addDataSource(twoSource);
		_prefs.addDataSource(threeSource);
		_prefs.addDataSource(fourSource);
		_prefs.addDataSource(fiveSource);
	}

	@Test
	public void testBasicConflict() {
		try {

			ResultStatus answer = HgStateChecker.getState(_prefs, _prefs.getDataSource("twoRepository"));
			Assert.assertEquals(answer, ResultStatus.CONFLICT);

		} catch (IOException ioe) {
			ioe.printStackTrace();
			Assert.fail(ioe.getMessage());
		}
	}

	@Test
	public void testBasicAhead() {
		try {

			ResultStatus answer = HgStateChecker.getState(_prefs, _prefs.getDataSource("threeRepository"));
			Assert.assertEquals(answer, ResultStatus.AHEAD);

		} catch (IOException ioe) {
			ioe.printStackTrace();
			Assert.fail(ioe.getMessage());
		}
	}

	@Test
	public void testBasicBehind() {
		try {

			ResultStatus answer = HgStateChecker.getState(_prefs, _prefs.getDataSource("fourRepository"));
			Assert.assertEquals(answer, ResultStatus.BEHIND);

		} catch (IOException ioe) {
			ioe.printStackTrace();
			Assert.fail(ioe.getMessage());
		}
	}

	@Test
	public void testBasicSame() {
		try {

			ResultStatus answer = HgStateChecker.getState(_prefs, _prefs.getDataSource("fiveRepository"));
			Assert.assertEquals(answer, ResultStatus.SAME);

		} catch (IOException ioe) {
			ioe.printStackTrace();
			Assert.fail(ioe.getMessage());
		}
	}

}
