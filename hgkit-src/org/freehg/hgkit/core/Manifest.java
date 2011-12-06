package org.freehg.hgkit.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.freehg.hgkit.HgInternalError;
import org.freehg.hgkit.core.ChangeLog.ChangeSet;

/**
 * The manifest is the file that describes the contents of the repository at a
 * particular {@link ChangeSet} ID.
 * 
 * @see <a href="http://www.selenic.com/mercurial/wiki/index.cgi/Manifest">Manifest</a>
 */
public class Manifest extends Revlog {

    /**
     * A manifest line consists of the length of the filename followed by
     * filename and nodeId.
     */
    private static class NodeFilemap {
        
        private final Map<String, NodeId> nodefilemap = new HashMap<String, NodeId>();
        
        /**
         * @return the nodefilemap
         */
        public Map<String, NodeId> getNodefilemap() {
            return nodefilemap;
        }
        
        /**
         * See {@link Manifest#get(org.freehg.hgkit.core.ChangeLog.ChangeSet)}.
         */
        private static NodeFilemap valueOf(Collection<String> lines) {
            final NodeFilemap instance = new NodeFilemap();
            for (final String line : lines) {
                final int nameLength = line.indexOf(0);
                final String name = line.substring(0, nameLength);
                final String nodeStr = line.substring(nameLength + 1, nameLength + 1 + NodeId.SHA_SIZE * 2);
                final NodeId node = NodeId.valueOf(nodeStr);
                instance.nodefilemap.put(name, node);
            }
            return instance;
            
        }
    }
    /**
     * Constructor for the manifestfile.
     * 
     * @param index
     *            normally <code>.hg/store/00manifest.[id]{1}</code>
     */
    public Manifest(File index) {
        super(index);
    }

    /**
     * Returns a map of paths and their corresponding nodeIds.
     * 
     * @param changelog
     *            for which changeset do we want the manifest entries.
     * @return manifest entries.
     */
    public Map<String, NodeId> get(ChangeLog.ChangeSet changelog) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        super.revision(changelog.getManifestId(), out).close();
        try {
            @SuppressWarnings("unchecked")
            final List<String> lines = IOUtils.readLines(new ByteArrayInputStream(out.toByteArray()));            
            return NodeFilemap.valueOf(lines).getNodefilemap();
        } catch (IOException e) {
            throw new HgInternalError(changelog.toString(), e);
        }
    }
}
