/*
 * HgInternalError.java 11.09.2008
 *
 */
package org.freehg.hgkit;

/**
 * Internal unchecked exception if something weird is happening.
 * 
 * @author mfriedenhagen
 */
@SuppressWarnings("serial")
public class HgInternalError extends RuntimeException {

    /**
     * Constructor with a String message.
     * @param message message
     */
    public HgInternalError(String message) {
        super(message);
    }

    /**
     * Constructor with a String message and a root cause.
     * @param message message
     * @param cause Throwable
     */
    public HgInternalError(String message, Throwable cause) {
        super(message, cause);
    }

}
