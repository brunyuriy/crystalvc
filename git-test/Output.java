    /*
     * Represents the output of a run command.  Consists of:
     * two strings: output and error,
     * one int    : exit status.
     */
    public class Output {
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