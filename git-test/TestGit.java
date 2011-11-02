import java.io.IOException;
import java.io.PrintStream;



public class TestGit {
	
	private static String[] paths = new String[5];
	private static String pathToDropbox = "C:\\Users\\Yuriy\\Desktop\\work\\My Dropbox\\";
	static {
		paths[0] = pathToDropbox + "gittestproject\\temp\\me";
		paths[1] = pathToDropbox + "gittestproject\\temp\\same";
		paths[2] = pathToDropbox + "gittestproject\\temp\\behind";
		paths[3] = pathToDropbox + "gittestproject\\temp\\ahead";
		paths[4] = pathToDropbox + "gittestproject\\temp\\merge";
	}
	public static void main(String[] args) throws IOException{
		
		PrintStream out = new PrintStream("output.txt");
		String[] logArgs = {"log"};
//		String executablePath = "C:\\Program Files (x86)\\Git\\cmd\\\\git.cmd";
		String executablePath = "C:\\Program Files (x86)\\Git\\bin\\\\git.exe";
		// String homepath = System.getProperty("user.home");
		for (String path : paths) {
			Output output = RunIt.execute(executablePath, logArgs, path, false);
			out.println("directory: " + path + "\noutput: \n" + output.getOutput());
			out.println("error: \n" + output.getError());
		}
	}

}
