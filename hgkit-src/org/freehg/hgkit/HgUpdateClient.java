/**
 * Copyright 2008 Mirko Friedenhagen
 * 
 * This software may be used and distributed according to the terms of
 * the GNU General Public License or under the Eclipse Public Licence (EPL)
 *
 */

package org.freehg.hgkit;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.freehg.hgkit.core.NodeId;
import org.freehg.hgkit.core.Repository;
import org.freehg.hgkit.core.Revlog;
import org.freehg.hgkit.core.RevlogEntry;
import org.freehg.hgkit.core.DirState.DirStateEntry;

/**
 * Class to update a single file.
 */
class UpdateFile {

    private final File absoluteFile;

    private final Revlog revlog;

    /**
     * Updates the given path.
     * 
     * @param repo
     *            the repo
     * @param path
     *            of the file
     */
    public UpdateFile(Repository repo, String path) {
        absoluteFile = repo.makeAbsolute(path);
        revlog = repo.getRevlog(absoluteFile);
    }

    /**
     * Checks out the tip-revision of {@link UpdateFile#absoluteFile}.
     */
    public void tip() {
        updateTo(revlog.tip());
    }

    /**
     * Checks out the given revlogEntry of {@link UpdateFile#absoluteFile}.
     * 
     * @param revlogEntry
     *            revlogEntry
     */
    public void updateTo(final RevlogEntry revlogEntry) {
        createParentDirs();
        final BufferedOutputStream out;
        try {
            out = new BufferedOutputStream(new FileOutputStream(absoluteFile));
        } catch (FileNotFoundException e) {
            throw new HgInternalError("absoluteFile=" + absoluteFile, e);
        }
        final NodeId nodeId = revlogEntry.getId();
        try {
            revlog.revision(nodeId, out);
        } finally {
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * Creates all the parentDirs of {@link UpdateFile#absoluteFile}.
     */
    void createParentDirs() {
        File parentDir = absoluteFile.getParentFile();
        if (!parentDir.mkdirs()) {
            throw new HgInternalError("Could not create " + parentDir);
        }
    }

    /**
     * Closes the revlog-file.
     */
    public void close() {
        revlog.close();
    }
}

/**
 * Update the working copy.
 */
public class HgUpdateClient {

    private final Repository repo;

    /**
     * Creates a new UpdateClient.
     * @param arepo arepo
     */
    public HgUpdateClient(Repository arepo) {
        this.repo = arepo;
    }

    /**
     * Updates the working copy to the tip.
     * 
     * TODO: what to do if we have several branches?
     * 
     * @return number of updated files.
     */
    public int update() {
        final Collection<DirStateEntry> states = repo.getDirState().getDirState();
        for (DirStateEntry state : states) {
            final String path = state.getPath();
            final UpdateFile updateFile = new UpdateFile(repo, path);
            try {
                updateFile.tip();
            } finally {
                updateFile.close();
            }
        }
        System.out.println(states.size() + " files updated.");
        return states.size();
    }
}
