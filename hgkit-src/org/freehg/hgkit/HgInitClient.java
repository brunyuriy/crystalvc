/**
 * Copyright 2008 mfriedenhagen
 * 
 * This software may be used and distributed according to the terms of
 * the GNU General Public License or under the Eclipse Public Licence (EPL)
 *
 */

package org.freehg.hgkit;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.freehg.hgkit.core.Repository;

/**
 * Provides methods to create new Repositories.
 * 
 * @author mfriedenhagen
 */
public class HgInitClient {

    private static final String REQUIRES_CONTENT = "revlogv1\nstore";

    private static final String DUMMY_CHANGELOG_CONTENT = "\0\0\0\2 dummy changelog to prevent using the old repo layout";

    private final File repoDir;

    private final File storeDir;

    HgInitClient(final File rootDir) {
        repoDir = new File(rootDir, Repository.HG);
        storeDir = new File(rootDir, Repository.STORE);
        if (repoDir.exists()) {
            throw new IllegalArgumentException("abort: repository " + rootDir + " already exists");
        }
    }

    /**
     * Creates the directories which are found initially in a Mercural-repo.
     * 
     * As of Mercurial 1.1 this is '.hg/store'.
     */
    void createInitialDirectories() {
        if (!storeDir.mkdirs()) {
            throw new HgInternalError("Could not create " + storeDir);
        }
    }

    /**
     * Writes the files which are found initially in a Mercurial-repo.
     * 
     * As of Mercurial 1.1 these are {@link Repository#CHANGELOG_INDEX}, and
     * {@link Repository#REQUIRES}.
     */
    void writeInitialFiles() {
        writeTo(new File(repoDir, Repository.CHANGELOG_INDEX), DUMMY_CHANGELOG_CONTENT);
        writeTo(new File(repoDir, Repository.REQUIRES), REQUIRES_CONTENT);
    }

    /**
     * Writes to a file throwing a {@link HgInternalError} if something goes
     * wrong.
     * 
     * @param file
     *            to write to
     * @param content
     *            of the file
     * @throws HgInternalError
     *             if something goes wrong
     */
    private void writeTo(final File file, final String content) {
        try {
            FileUtils.writeStringToFile(file, content);
        } catch (IOException e) {
            throw new HgInternalError("Could not write to " + file, e);
        }
    }

    /**
     * Creates a new Mercurial repository.
     * 
     * @param rootDir the directory to create the repository in.
     */
    public static void create(final File rootDir) {
        HgInitClient client = new HgInitClient(rootDir);
        client.createInitialDirectories();
        client.writeInitialFiles();
    }
}
