package org.freehg.hgkit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.freehg.hgkit.FileStatus.Status;
import org.freehg.hgkit.core.ChangeLog;
import org.freehg.hgkit.core.DirState;
import org.freehg.hgkit.core.Ignore;
import org.freehg.hgkit.core.NodeId;
import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.Revlog;
import org.freehg.hgkit.core.ChangeLog.ChangeSet;
import org.freehg.hgkit.core.DirState.DirStateEntry;

/**
 * HgStatusClient reports the status of the working copy.
 * 
 * TODO make this work with branches as well.
 * 
 */
public final class HgStatusClient {

    private static final char STATE_MERGED = 'm';

    private static final char STATE_ADDED = 'a';

    private static final char STATE_REMOVED = 'r';

    private static final char STATE_NORMAL = 'n';

    private DirState dirState;

    private final Repository repo;

    private Ignore ignore;

    private Map<String, NodeId> nodeStateByName;

    /**
     * Constructor.
     * 
     * @param repo the repo the client is working on.
     */
    public HgStatusClient(Repository arepo) {
        if (arepo == null) {
            throw new IllegalArgumentException("Repository may not be null");
        }
        this.repo = arepo;
        this.dirState = this.repo.getDirState();
    }

    /**
     * Returns a list of the states for file.
     * 
     * @param file the file to inspect.
     * @return list of file states.
     */
    public List<FileStatus> doStatus(final File file) {
        return doStatus(file, true);
    }

    /**
     * Returns a list of the states for file.
     * 
     * @param file the file to inspect.
     * @param recurse through all files and subdirectories.
     * @return list of file states.
     */
    public List<FileStatus> doStatus(final File file, final boolean recurse) {
        List<FileStatus> result = getStatus(file, recurse, isIgnored(file));
        result.addAll(getMissing());
        return result;
    }

    private List<FileStatus> getStatus(final File file, final boolean recurse, boolean parentIgnored) {
        if (Repository.isRepoPrivate(file)) {
            return Collections.<FileStatus>emptyList();
        }
        List<FileStatus> result = new ArrayList<FileStatus>();

        if (recurse && file.isDirectory()) {
            for (File sub : file.listFiles()) {
                result.addAll(getStatus(sub, recurse, parentIgnored | isIgnored(file)));
            }
            return result;
        } else if (file.isFile()) {
            result.add(getFileState(file, parentIgnored));
        }
        return result;
    }

    private List<FileStatus> getMissing() {
        final Collection<DirStateEntry> state = dirState.getDirState();
        final List<FileStatus> missing = new ArrayList<FileStatus>();
        for (final DirStateEntry entry : state) {
            final File testee = repo.makeAbsolute(entry.getPath());
            if (!testee.exists()) {
                missing.add(FileStatus.valueOf(testee, Status.DELETED));
            }
        }
        return missing;
    }

    private boolean isIgnored(final File file) {
        return file.getName().equalsIgnoreCase(".hg") || getIgnore().isIgnored(file);
    }

    private FileStatus getFileState(final File file, boolean parentIgnored) {
        if (!file.isFile()) {
            throw new IllegalArgumentException(file + " must be a file");
        }
        final File relativeFile = repo.makeRelative(file);
        final FileStatus status;
        final DirStateEntry state = this.dirState.getState(relativeFile.getPath());

        if (state != null) {
            final char stateChar = state.getState();
            switch (stateChar) {
            case STATE_NORMAL:
                status = FileStatus.valueOf(file, checkStateNormal(file, state));
                break;
            case STATE_ADDED:
            case STATE_REMOVED:
            case STATE_MERGED:                
                status = FileStatus.valueOf(file, FileStatus.Status.valueOf(stateChar));                
                break;
            default:
                throw new HgInternalError("Unknown state:" + stateChar);
            }
        } else {
            if (parentIgnored || isIgnored(relativeFile)) {
                status = FileStatus.valueOf(file, FileStatus.Status.IGNORED);
            } else {
                status = FileStatus.valueOf(file, FileStatus.Status.NOT_TRACKED);
            }
        }
        return status;
    }

    private Status checkStateNormal(File file, DirStateEntry state) {
        // On (n)ormal files
        // if size and mod time is same as in dirstate nothing has happened
        // if the size HAS changed, the file must have changed
        // After an update, dirstate is not written back and contains -1
        if (0 <= state.getSize() && state.getSize() != file.length()) {
            return FileStatus.Status.MODIFIED;
        }

        // Hg uses seconds, java milliseconds
        long lastModified = TimeUnit.SECONDS.convert(file.lastModified(), TimeUnit.MILLISECONDS);
        if (state.getFileModTime() == lastModified) {
            return FileStatus.Status.MANAGED;
        }
        // if the filemod time has changed but the size haven't
        // then we must compare against the repository version
        Revlog revlog = repo.getRevlog(file);

        try {
            // System.out.println("Comparing against stored revision");

            InputStream local = new BufferedInputStream(new FileInputStream(file));
            ComparingStream comparator = new ComparingStream(local);
            revlog.revision(getNodeStateByName().get(state.getPath()), comparator).close();
            local.close();
            if (comparator.equals) {
                return FileStatus.Status.MANAGED;
            }
            return FileStatus.Status.MODIFIED;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @return the nodeStateByName
     */
    private Map<String, NodeId> getNodeStateByName() {
        if (this.nodeStateByName == null) {
            ChangeLog log = repo.getChangeLog();
            ChangeSet entry = log.get(dirState.getId());
            this.nodeStateByName = repo.getManifest().get(entry);
        }
        return nodeStateByName;
    }

    /**
     * @return the ignore
     */
    private Ignore getIgnore() {
        if (this.ignore == null) {
            this.ignore = repo.getIgnore();
        }
        return ignore;
    }

    private static class ComparingStream extends OutputStream {
        private boolean equals = true;

        private final InputStream in;

        ComparingStream(InputStream in) {
            this.in = in;

        }

        @Override
        public void write(final int b) throws IOException {
            final int fromStream = in.read() & 0xFF;
            final int expected = b & 0xFF;
            if (expected != fromStream) {
                this.equals = false;
            }
        }
    }
}
