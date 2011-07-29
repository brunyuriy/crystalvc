package org.freehg.hgkit.core;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.freehg.hgkit.HgInternalError;

/**
 * Mercurial tracks various information about the working directory (the dirstate). 
 * <ul>
 * <li>what revision(s) are currently checked out</li> 
 * <li>what files have been copied or renamed</li> 
 * <li>what files are controlled by Mercurial</li> 
 * </ul>
 * 
 * @see <a href="http://www.selenic.com/mercurial/wiki/index.cgi/DirState">DirState</a>
 * @see <a href="http://www.selenic.com/mercurial/wiki/index.cgi/WorkingDirectory">WorkingDirectory</a>
 * 
 */
public class DirState {

    private static final int HEADER_PAD_SIZE = 20;

    private static final int INITIAL_LIST_SIZE = 1024;

    private NodeId currentId;

    private final Map<String, DirStateEntry> dirstate = new HashMap<String, DirStateEntry>();

    private final List<DirStateEntry> values = new ArrayList<DirStateEntry>(INITIAL_LIST_SIZE);

    /**
     * Creates a {@link DirState}.
     * 
     * @param dirStateFile
     *            dirstate file
     */
    DirState(File dirStateFile) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(dirStateFile);
            parse(new DataInputStream(new BufferedInputStream(fis)));
        } catch (FileNotFoundException e) {
            throw new HgInternalError("Could not find '" + dirStateFile.toString() + "'", e);
        } catch (IOException e) {
            throw new HgInternalError("Error reading '" + dirStateFile.toString() + "'", e);
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    /**
     * Parses the dirStateFile into {@link DirStateEntry}.
     * @param in DataInputStream.
     * @throws IOException if there are problems with the dirState file.
     */
    void parse(DataInputStream in) throws IOException {
        // ">c l l l l"
        // state, mode, size, fileModTime, nameLength, bytes[namelength] as name
        // (String)
        parseHeader(in);
        while (in.available() > 0) {
            DirStateEntry entry = DirStateEntry.readNext(in);
            final String path = entry.getPath();
            // put with both / and \ as path separator
            dirstate.put(path.replace('/', '\\'), entry);
            dirstate.put(path.replace('\\', '/'), entry);
            values.add(entry);
        }
    }

    /**
     * Returns a dirstate entry for the given path.
     * <b>NOTE</b>: Remember to unFold the path before you use this.
     * 
     * @param path path to an entry.
     * @return a {@link DirStateEntry} if one is avaialable for this repository.
     *         null otherwise
     */
    public DirStateEntry getState(String path) {
        return this.dirstate.get(path);
    }

    /**
     * Returns all entries for the directory state.
     * 
     * @return Collection of {@link DirStateEntry}
     */
    public Collection<DirStateEntry> getDirState() {
        return Collections.unmodifiableList(this.values);
    }

    /**
     * Reads the currentId and skips the padding.
     * 
     * @param in
     *            stream to read from
     * @throws IOException
     */
    private void parseHeader(DataInputStream in) throws IOException {
        currentId = NodeId.read(in);
        long skip = in.skip(HEADER_PAD_SIZE);
        assert skip == HEADER_PAD_SIZE;
    }

    /**
     * Returns the current nodeId.
     * 
     * @return nodeId
     */
    public NodeId getId() {
        return this.currentId;
    }

    /**
     * Describes the status of a single file in the working copy.
     */
    public static class DirStateEntry {

        private static final int ONLY_RWX = Integer.valueOf("0777", 8);

        private static final int SYMLINK = Integer.valueOf("020000", 8);

        private final long size;

        private final long mode;

        private final long fileModTime;

        private final char state;

        private final String path;

        /**
         * Returns the size of the file in bytes. If Mercurial is not able to
         * determine the state by size and modification time, returns -1.
         * 
         * @return size
         */
        public long getSize() {
            return size;
        }

        /**
         * Returns the POSIX-mode of the file.
         * 
         * @return mode
         */
        public long getMode() {
            return mode;
        }

        /**
         * Returns the modification time of the file as seconds since the
         * {@link Date#Date(long) standard base time}.
         * 
         * @return modification time
         */
        public long getFileModTime() {
            return fileModTime;
        }

        /**
         * Returns the state of the file. The states that are tracked are:
         * <dl>
         * <dt>n</dt>
         * <dd>normal</dd>
         * <dt>a</dt>
         * <dd>added</dd>
         * <dt>r</dt>
         * <dd>removed</dd>
         * <dt>m</dt>
         * <dd>3-way merged</dd>
         * </dl>
         * 
         * @return state
         */
        public char getState() {
            return state;
        }

        /**
         * Returns the path of the file.
         * 
         * @return path
         */
        public String getPath() {
            return path;
        }

        /**
         * Creates a new {@link DirStateEntry}.
         * 
         * @param state
         *            {@link DirStateEntry#getState()}
         * @param aMode
         *            {@link DirStateEntry#getMode()}
         * @param aSize
         *            {@link DirStateEntry#getSize()}
         * @param aFileModTime
         *            {@link DirStateEntry#getFileModTime()}
         * @param aPath
         *            {@link DirStateEntry#getPath()}
         */
        DirStateEntry(final char aState, final int aMode, final int aSize, final int aFileModTime, final String aPath) {
            this.state = aState;
            this.mode = aMode;
            this.size = aSize;
            this.fileModTime = aFileModTime;
            this.path = aPath;
        }

        /**
         * Reads a {@link DirStateEntry} from <code>in</code>.
         * 
         * @param in
         *            DataInputStream
         * @return DirStateEntry
         * @throws IOException
         *             when reading from <code>in</code> does not succeed.
         */
        public static DirStateEntry readNext(final DataInputStream in) throws IOException {
            final char state = (char) in.readByte();
            final int mode = in.readInt();
            final int size = in.readInt();
            final int fileModTime = in.readInt();
            final String path = readFileName(in);
            return new DirStateEntry(state, mode, size, fileModTime, path);
        }

        /**
         * Reads the filename from <code>in</code>.
         * 
         * @param in
         *            DataInputStream
         * @return filename
         * @throws IOException
         *             when reading from <code>in</code> does not succeed.
         */
        private static String readFileName(final DataInputStream in) throws IOException {
            final int nameLength = in.readInt();
            final byte[] str = new byte[nameLength];
            in.readFully(str);
            final String path = new String(str);
            return path;
        }

        /**
         * Returns a String representation of a DirState as returned by
         * <code>hg debugstate</code>.
         * 
         * @return representation
         */
        @Override
        public String toString() {
            final String dateString;
            if (getFileModTime() == -1) {
                dateString = String.format(Locale.ENGLISH, "%18s", "unset");
            } else {
                dateString = String.format(Locale.ENGLISH, "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", new Date(
                        TimeUnit.MILLISECONDS.convert(getFileModTime(), TimeUnit.SECONDS)));
            }
            final String modeString;
            if ((getMode() & SYMLINK) != 0) {
                modeString = "lnk";
            } else {
                modeString = String.format(Locale.ENGLISH, "%3o", getMode() & ONLY_RWX);
            }
            return String.format(Locale.ENGLISH, "%s %s %10d %s %s", getState(), modeString, getSize(), dateString,
                    getPath());
        }
    }

}
