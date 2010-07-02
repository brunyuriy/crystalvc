package org.freehg.hgkit;

import java.io.File;

/**
 * Simple bean for holding a file and it's Mercurial state.
 */
public final class FileStatus {

    /**
     * Mercurial state.
     */
    public static enum Status {
        ADDED('A'), REMOVED('R'), DELETED('D'), MERGED('M'), NOT_TRACKED('?'), MANAGED('C'), MODIFIED('M'), IGNORED('I');

        Status(char c) {
            this.asHg = c;
        }

        private char asHg;

        /** {@inheritDoc} */
        @Override
        public String toString() {
            return Character.toString(this.asHg);
        }
        
        /**
         * Returns the Status representation for a character ignoring case.
         * 
         * @param c character
         * @return status
         */
        public static Status valueOf(char c) {
            char upperCase = Character.toUpperCase(c);
            for (final Status status : values()) {
                if (status.asHg == upperCase) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Status " + c + " is unknown");
        }
    };

    private final File file;

    private final Status status;

    /**
     * Returns the filestatus representation of a file and a status.
     * 
     * @param file file
     * @param status {@link Status}.
     * 
     * @return filestatus
     */
    public static FileStatus valueOf(final File file, final Status status) {
        return new FileStatus(file, status);
    }

    private FileStatus(final File afile, final Status astatus) {
        this.file = afile;
        this.status = astatus;
    }

    /**
     * Returns the status.
     * 
     * @return status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Returns the file.
     * 
     * @return file
     */
    public File getFile() {
        return this.file;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return status + " " + file.getPath();
    }

}
