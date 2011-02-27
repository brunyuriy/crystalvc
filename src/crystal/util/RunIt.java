package crystal.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

import crystal.Constants;
import crystal.client.ClientPreferences;

/**
 * The main runner. This performs system commands and captures their output.
 * 
 * @author rtholmes
 * @author brun
 * 
 */
public class RunIt {

	// the logger
	public static Logger _log = Logger.getLogger(ClientPreferences.class);
	
	/*
	 * Represents the output of a run command.  Consists of two strings, output and error.
	 */
	public static class Output {
		String _output;
		String _error;
		int _status;
		
		Output(String output, String error, int status) {
			_output = output;
			_error = error;
			_status = status;
		}
		
		public String getOutput() {
			return _output;
		}
		
		public String getError() {
			return _error;
		}
		
		public int getStatus() {
			return _status;
		}
		
		@Override
		public String toString() {
			String answer = "Exit status: " + _status + "\n";
			if (_error.length() > 0) {
				answer += "*****-START-ERROR-*****\n";
				answer += _error;
				answer += "*****-END-ERROR-*****\n";
			}

			answer += "*****-START-OUTPUT-*****\n";
			answer += _output;
			answer += "*****-END-OUTPUT-*****\n";
			return answer;
		}
	}

	/**
	 * Runs a command twice. A not-nice hack for those times when executions don't seem to be coming out consistently.
	 * 
	 * @param command
	 * @param args
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static Output executeTwice(String command, String[] args, String path, boolean getStatus) throws IOException {
		execute(command, args, path, false);
		Output result = execute(command, args, path, getStatus);
		return result;
	}

	/**
	 * Executes a command.
	 * 
	 * @param command
	 * @param args
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static Output execute(String command, String[] args, String path, boolean getStatus) throws IOException {
		// System.out.println("\t" + TimeUtility.getCurrentLSMRDateString() + ": RunIt::execute(..) - : " + command +
		// " ...");

		long start = System.currentTimeMillis();

		ProcessBuilder builder = new ProcessBuilder();
		File directory = new File(path);

		assert directory.exists();
		assert directory.isDirectory();

		// Assert.assertTrue(directory.exists(), "Directory does not exist: " + path);
		// Assert.assertTrue(directory.isDirectory(), "This is not a directory: " + path);

		builder.directory(new File(path));
		if (args == null || args.length == 0) {
			builder.command(command);
		} else {
			List<String> cmd = new Vector<String>();
			cmd.add(command);

			for (String arg : args)
				cmd.add(arg);

			builder.command(cmd);
		}

		_log.info("\tRunIt::execute(..) - command: " + builder.command().toString() + "; in path: " + builder.directory());
		if (Constants.DEBUG_RUNIT) {
			System.out.println("\tRunIt::execute(..) - command: " + builder.command().toString() + "; in path: " + builder.directory());
		}

		Process proc = builder.start();

		// configure the streams
		BufferedInputStream err = new BufferedInputStream(proc.getErrorStream());
		BufferedInputStream out = new BufferedInputStream(proc.getInputStream());

		StreamCatcher outCatcher = new StreamCatcher(out);
		Thread outCatcherThread = new Thread(outCatcher);
		outCatcherThread.start();

		StreamCatcher errCatcher = new StreamCatcher(err);
		Thread errCatcherThread = new Thread(errCatcher);
		errCatcherThread.start();

		try {
			errCatcherThread.join();
			outCatcherThread.join();
			_log.info("RunIt::execute(..) - Threads joined peacefully after: " + TimeUtility.msToHumanReadableDelta(start));
			if (Constants.DEBUG_RUNIT) {
				System.out.println("\t\tRunIt::execute(..) - Threads joined peacefully after: " + TimeUtility.msToHumanReadableDelta(start));
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		String goodOutput = outCatcher.getOutput();
		String errOutput = errCatcher.getOutput();
		int exitStatus;
		if (getStatus) {
			_log.info("Waiting for exit status of " + builder.command().toString() + "; in path: " + builder.directory());
			try {
				exitStatus = proc.waitFor();
			} catch (InterruptedException e) {
				_log.error("Encountered an interrupt exception while executing " + builder.command().toString() + "; in path: " + builder.directory());
				exitStatus = -1;
			}
		} else
			exitStatus = 0;

//		String output = "";
//
//		if (errOutput.length() > 0) {
//			output += "*****-START-ERROR-*****\n";
//			output += errOutput;
//			output += "*****-END-ERROR-*****\n";
//		}
//
//		output += "*****-START-OUTPUT-*****\n";
//		output += goodOutput;
//		output += "*****-END-OUTPUT-*****\n";

		// System.out.println("\t\tRunIt::execute(..) - output: " + output);

		return new Output(goodOutput, errOutput, exitStatus);
	}

	static public boolean deleteDirectory(File path) {
		_log.info("RunIt::deleteDirectory(..) - deleting " + path);
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
		boolean answer = path.delete();
		if (answer)
			_log.info("RunIt::deleteDirectory(..) - " + path + " deleted successfully");
		else
			_log.info("RunIt::deleteDirectory(..) - deleting " + path + " failed");
		return answer;
	}
	
	public static Output tryCommand(String command, String path) throws IOException {
		StringTokenizer tokens = new StringTokenizer(command);
		String executable;
		List<String> argumentsList = new ArrayList<String>();
		executable = tokens.nextToken();
		while (tokens.hasMoreTokens()) {
			argumentsList.add(tokens.nextToken());
		}
		return execute(executable, argumentsList.toArray(new String[0]), path, true);
	}

}
