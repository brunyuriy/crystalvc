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
 * RunIt executes system commands, captures their outputs, performs file system operations, and finds paths.
 * 
 * @author rtholmes
 * @author brun
 * 
 */
public class RunIt {

    // the logger
    public static Logger _log = Logger.getLogger(ClientPreferences.class);
    
    /*
     * Represents the output of a run command.  Consists of:
     * two strings: output and error,
     * one int    : exit status.
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

//      String output = "";
//
//      if (errOutput.length() > 0) {
//          output += "*****-START-ERROR-*****\n";
//          output += errOutput;
//          output += "*****-END-ERROR-*****\n";
//      }
//
//      output += "*****-START-OUTPUT-*****\n";
//      output += goodOutput;
//      output += "*****-END-OUTPUT-*****\n";

        // System.out.println("\t\tRunIt::execute(..) - output: " + output);

        return new Output(goodOutput, errOutput, exitStatus);
    }

    /**
     * Deletes the File pointed to by path.  
     * @param path: the File to delete
     * @return true iff path is successfully deleted
     */
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
    
    /**
     * Parses out the executable and the arguments from the command and executes the command.  
     * @param command: the command to run
     * @param path: the path in which to run the command
     * @return: the Output of the command's execution
     * @throws IOException
     */
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
    
    /**
     * @requires the executable is the first part of the executable parameter 
     *           and executable has no spaces in it 
     *           and arguments have no spaces in them
     * @param executable: a String that can be run from the command line
     * @return a String that runs the same command as executable, but that has an absolute path to the executable.  
     *         If there is no such executable in the system PATH, returns null
     */
    public static String getExecutable(String executable) {
        if (executable == null)
            return null;
        
        if ((new File(executable)).exists())
            return executable;

        StringTokenizer args = new StringTokenizer(executable);
        
        if (!(args.hasMoreTokens()))
            return null;
        String execPart = args.nextToken();
        String arguments = "";
        while (args.hasMoreTokens()) 
            arguments += " " + args.nextToken();
        
        if ((new File(execPart)).exists())
            return executable;
        
        String path = System.getenv("PATH");
        StringTokenizer pathTokens = new StringTokenizer(path, File.pathSeparator); 
        while (pathTokens.hasMoreTokens()) {
            String token = pathTokens.nextToken();
            if ((new File(token + File.separator + execPart)).exists()) {
                if (!(token.endsWith(File.separator)))
                    token += File.separator;
                return token + executable;
            }
            if ((new File(token + File.separator + execPart + ".exe")).exists()) {
                if (!(token.endsWith(File.separator)))
                    token += File.separator;    
                return token + File.separator + execPart + ".exe" + arguments;
            }
            if ((new File(token + File.separator + execPart + ".cmd")).exists()) {
                if (!(token.endsWith(File.separator)))
                    token += File.separator;    
                return token + File.separator + execPart + ".cmd" + arguments;
            }
        }
        // Could not find any executable
        return null;
    }
    
    /**
     * 
     * @param minimumVersion: the minimum version of hg required
     * @param hg: the path to the hg executable
     * @param tempPath: any valid path in which hg can be executed
     * @return true iff hg points to a same or newer version of hg than the minimumVersion
     * @throws IOException if hg cannot be run in tempPath
     * 
     */
    public static boolean validHG(double minimumVersion, String hg, String tempPath) throws IOException {
       String[] versionArgs = new String[1];
       versionArgs[0] = "--version";
       String output = execute(hg, versionArgs, tempPath, false).getOutput();
       String versionStr = output.substring(output.indexOf("version") + 8, output.indexOf(")"));
       double version = Double.parseDouble(versionStr.substring(0, versionStr.indexOf(".", versionStr.indexOf(".") + 1)));
       boolean answer = (minimumVersion <= version);
       if (answer)
           _log.info("The version of hg checks out.  (You are running " + versionStr + ", which is newer than the minimum required "+ minimumVersion + ".)");
       else 
           _log.error("You are using an outdated hg.  (You are running " + versionStr + ", which is older than the minimum required "+ minimumVersion + ".)");
           
       return answer;
    }
    
    //TODO implement this method
    public static boolean validGit(double minimumVersion, String git, String tempPath) throws Exception {
    	throw new Exception("not implemented yet");
    }
}
