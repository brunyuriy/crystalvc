package crystal.util;


import java.io.BufferedInputStream;

class StreamCatcher implements Runnable {

	BufferedInputStream in = null;

	byte[] buffer = new byte[64];

	StringBuilder _output = new StringBuilder();

	public StreamCatcher(BufferedInputStream in) {
		this.in = in;
	}

	public void run() {
		int readBytes = 0;
		try {
			while ((readBytes = in.read(buffer)) > 0) {
				// System.out.print(new String(buffer, 0, readBytes));
				synchronized (this) {
					_output.append(new String(buffer, 0, readBytes));
				}
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getOutput() {
		return _output.toString();
	}
}