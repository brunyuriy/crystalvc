package org.freehg.hgkit.core;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.freehg.hgkit.FileStatus;
import org.freehg.hgkit.HgInternalError;

/**
 * ChangeLog is just another {@link Revlog} with a specific format.
 * 
 * @see <a href="http://mercurial.selenic.com/wiki/Changelog">ChangeLog page at selenic</a>
 * 
 * <p><tt>hg debugindex .hg/store/00changelog.i</tt>
 * shows the changesets contained in the changelog.</p>
 * 
 * <p>The data in a specific {@link ChangeSet} might then be obtained with e.g.
 * <tt>hg debugdata .hg/store/00changelog.i 256</tt>.</p>
 * 
 * <pre>
 * 4b6add21b702e18a679686779efaba97a9beff2e
 * Mirko Friedenhagen &lt;mfriedenhagen@users.berlios.de&gt;
 * 1251923138 -7200
 * src/main/java/org/freehg/hgkit/core/ChangeLog.java
 * 
 * Use IOUtils.
 * </pre>
 * 
 * <p>The entry for a {@link ChangeSet} consists of:</p>
 * <ol>
 *  <li>the SHA1-Key of the corresponding {@link Manifest} entry for this revision</li>
 *  <li>the committer of this revision</li> 
 *  <li>a <a href="http://en.wikipedia.org/wiki/Unix_time">timestamp</a> given in seconds and 
 *  the offset from <a href="http://en.wikipedia.org/wiki/Coordinated_Universal_Time">UTC</a> of this revision</li>
 *  <li>a list of files in this revision</li>
 *  <li>a blank, separating line</li>
 *  <li>the comment of this revision</li>
 * </ol>
 */
public final class ChangeLog extends Revlog {

    private final Repository repo;

    /**
     * Creates the ChangeLog for the repo.
     * 
     * @param arepo the repo
     * @param index the changelog file.
     */
    ChangeLog(Repository arepo, File index) {
        super(index);
        this.repo = arepo;
    }

    /**
     * Returns a {@link ChangeSet} specified by the integer revision.
     * This is only valid for a local repo.
     * 
     * @param revision integer revision
     * @return the ChangeSet specified by the integer revision
     */
    public ChangeSet get(int revision) {
        NodeId node = super.node(revision);
        return this.get(node);
    }

    /**
     * Returns a {@link ChangeSet} specified by the {@link NodeId}.
     * This is valid for all distributed copies of the repo.
     * 
     * @param nodeId a SHA1-key
     * @return the ChangeSet specified by the nodeId
     */
    public ChangeSet get(NodeId nodeId) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        super.revision(nodeId, out).close();
        return new ChangeSet(nodeId, index(nodeId), out.toByteArray());
    }

    /**
     * Returns filestates for the specified {@link ChangeSet}.
     * 
     * @param changeset to compare
     * @return filestates
     */
    public List<FileStatus> getFileStatus(ChangeSet changeset) {
        Manifest manifest = repo.getManifest();
        int revision = changeset.getRevision();
        // TODO Beware of rev 0
        Map<String, NodeId> currMan = manifest.get(changeset);
        Map<String, NodeId> prevMan = new HashMap<String, NodeId>();
        if (revision > 0) {
            ChangeSet prev = get(revision - 1);
            prevMan = manifest.get(prev);
        }
        Set<String> allKeys = new HashSet<String>();
        allKeys.addAll(currMan.keySet());
        allKeys.addAll(prevMan.keySet());
        List<FileStatus> fileStates = new ArrayList<FileStatus>();

        // in both keyset == modified
        // only in prev == removed
        // only in curr == added
        for (String string : allKeys) {
            final FileStatus status;
            if (currMan.containsKey(string) && prevMan.containsKey(string)) {
                status = FileStatus.valueOf(new File(string), FileStatus.Status.MODIFIED);
            } else if (currMan.containsKey(string)) {
                status = FileStatus.valueOf(new File(string), FileStatus.Status.ADDED);
            } else { // prevMan contains
                status = FileStatus.valueOf(new File(string), FileStatus.Status.REMOVED);
            }
            fileStates.add(status);
        }
        return fileStates;
    }

    /**
     * Returns the {@link ChangeSet}s contained in the repository.
     * 
     * @return changesets
     */
    public List<ChangeSet> getLog() {
        try {
            final int length = count();
            final List<ChangeSet> log = new ArrayList<ChangeSet>(length);
            for (int revision = 0; revision < length; revision++) {
                RevlogEntry revlogEntry = index.get(revision);
                ByteArrayOutputStream out = new ByteArrayOutputStream((int) revlogEntry.getUncompressedLength());
                super.revision(revlogEntry, out, false);
                ChangeSet entry = new ChangeSet(revlogEntry.nodeId, revision, out.toByteArray());
                log.add(entry);
            }
            return log;
        } finally {
            close();
        }
    }

    /**
     * A ChangeSet holds the data for a single commit.
     * 
     * @see <a href="http://mercurial.selenic.com/wiki/ChangeSet">ChangeSet page at selenic</a>
     */
    public static class ChangeSet {

        private NodeId manifestId;

        private int revision;

        private NodeId changeId;

        private Date when;

        private String author;

        private List<String> files = new ArrayList<String>();

        private String comment;

        /**
         * Returns the {@link NodeId} of the ChangeSet.
         * 
         * @return changeId
         */
        public NodeId getChangeId() {
            return changeId;
        }

        /**
         * Returns the {@link NodeId} of the corresponding entry in the {@link Manifest}.
         * 
         * @return manifestId
         */
        public NodeId getManifestId() {
            return manifestId;
        }

        /**
         * Returns the only locally valid integer revision of the ChangeSet.
         * 
         * @return revision
         */
        public int getRevision() {
            return revision;
        }

        /**
         * Returns the date of the commit.
         * 
         * @return commit date
         */
        public Date getWhen() {
            return (Date)when.clone();
        }

        /**
         * Returns the committer of the ChangeSet.
         * 
         * @return committer
         */
        public String getAuthor() {
            return author;
        }

        /**
         * Returns the list of files comprised in this ChangeSet.
         * 
         * @return files
         */
        public List<String> getFiles() {
            return new ArrayList<String>(files);
        }

        /**
         * Returns the commit comment.
         * 
         * @return comment
         */
        public String getComment() {
            return comment;
        }
        
        /** {@inheritDoc} */
        @Override
        public String toString() {
            return changeId.asShort() + " " + when + " " + author + "\n" + comment + "\n" + files;
        }

        /**
         * See {@link ChangeLog} class documentation for specifics.
         * 
         * @param in the stream to parse
         */
        private void parse(final InputStream in) {

            final BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line = null;
            try {
                // First line may be null, so do not use private readline-method.
                while (null != (line = reader.readLine())) {
                    manifestId = NodeId.valueOf(line);
                    author = readLine(reader);
                    when = dateParse(readLine(reader));

                    String fileLine = readLine(reader);
                    // read while line is not empty, its a file, the rest is the
                    // comment
                    while (0 < fileLine.trim().length()) {
                        files.add(fileLine);
                        fileLine = readLine(reader);
                    }
                    comment = IOUtils.toString(reader);
                }
            } catch (IOException e) {                
                throw new HgInternalError("Error parsing " + in, e);
            }
        }

        /**
         * Returns a line of reader.
         *  
         * @param reader to read from
         * @return a line
         * 
         * @throws IOException
         * @throws HgInternalError if we could not read a line.
         */
        private String readLine(BufferedReader reader) throws IOException {
            final String line = reader.readLine();
            if (line == null) {
                throw new HgInternalError(reader.toString());
            }
            return line;
        }

        /**
         * Parses the given dateLine consisting of two space separated integers.
         * @see {@link ChangeLog}. 
         * 
         * @param dateLine line with Unix time and offset
         * @return a Date
         */
        private Date dateParse(String dateLine) {
            final String[] parts = dateLine.split(" ");
            final long secondsSinceEpoc = Integer.parseInt(parts[0]);
            final long offset = Integer.parseInt(parts[1]);
            final long msSinceEpoc = TimeUnit.MILLISECONDS.convert(secondsSinceEpoc + offset, TimeUnit.SECONDS);
            return new Date(msSinceEpoc);
        }

        /**
         * Constructor for a ChangeSet.
         * 
         * @param aChangeId the changeId
         * @param aRevision the integer revision 
         * @param data binary data to parse
         */
        ChangeSet(NodeId aChangeId, int aRevision, byte[] data) {
            parse(new ByteArrayInputStream(data));
            this.changeId = aChangeId;
            this.revision = aRevision;
        }
    }
}
