import java.io.IOException;
import java.io.PrintStream;



public class TestGit {
	
	private static String[] paths = new String[5];
	static {
		paths[0] = "C:\\Users\\Haochen\\Dropbox\\gittestproject\\temp\\me";
		paths[1] = "C:\\Users\\Haochen\\Dropbox\\gittestproject\\temp\\same";
		paths[2] = "C:\\Users\\Haochen\\Dropbox\\gittestproject\\temp\\behind";
		paths[3] = "C:\\Users\\Haochen\\Dropbox\\gittestproject\\temp\\ahead";
		paths[4] = "C:\\Users\\Haochen\\Dropbox\\gittestproject\\temp\\merge";
	}
	public static void main(String[] args) throws IOException{
		
		PrintStream out = new PrintStream("output.txt");
		String[] logArgs = {"log"};
		String executablePath = "C:\\Program Files\\Git\\cmd\\\\git.cmd";
		for (String path : paths) {
			Output output = RunIt.execute(executablePath, logArgs, path, false);
			out.println("directory: " + path + "\noutput: \n" + output.getOutput());
			out.println("error: \n" + output.getError());
		}
	}

}
