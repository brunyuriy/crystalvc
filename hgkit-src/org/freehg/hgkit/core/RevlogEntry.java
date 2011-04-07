/**
 *
 */
package org.freehg.hgkit.core;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.freehg.hgkit.HgInternalError;

/**
 * Describes a single entry of the {@link Revlog}.
 * 
 * @see <a
 *      href="http://www.selenic.com/mercurial/wiki/index.cgi/RevlogNG">RevlogNG</a>
 * @see <a
 *      href="http://www.selenic.com/mercurial/wiki/index.cgi/Revlog">Revlog</a>
 */
public final class RevlogEntry {

    /**
     * 
     */
    private static final int NODE_ID_SIZE = 32;

    /** The corresponding length of indexformatng (python) >Qiiiiii20s12x. */
    public static final int BINARY_LENGTH = 64;

    public static RevlogEntry nullInstance = valueOf(null, new byte[BINARY_LENGTH], 0);

    private final Revlog parent;

    private final long offset;

    private final int flags;

    private final long compressedLength;

    private final long uncompressedLength;

    private final int baseRev;

    private final int linkRev;

    private final int firstParentRev;

    private final int secondParentRev;

    final NodeId nodeId;

    int revision;

    /**
     * Initializes a RevlogEntry from the given {@link DataInputStream}.
     * 
     * @param parent
     *            revlog
     * @param reader
     *            dataInputStream
     * @param off
     * 
     * @throws HgInternalError
     *             if an {@link IOException} is thrown while reading.
     */
    RevlogEntry(Revlog parent, DataInputStream reader, int off) throws HgInternalError {
        this.parent = parent;
        try {
            final long offsetFromReader = ((long) reader.readShort() << 32) + reader.readInt();
            // the first revlog entry (starting with offset 0) contains
            // information about
            // the revlog and not the actual offset (see bottom of
            // RevlogNG-page).
            offset = off == 0 ? 0 : offsetFromReader;
            flags = reader.readShort();
            compressedLength = reader.readInt();
            uncompressedLength = reader.readInt();

            baseRev = reader.readInt();
            linkRev = reader.readInt();

            firstParentRev = reader.readInt();
            secondParentRev = reader.readInt();

            byte[] nodeid = new byte[NODE_ID_SIZE];
            final int read = reader.read(nodeid);
            assert read == NODE_ID_SIZE;
            nodeId = NodeId.valueOf(nodeid);
        } catch (IOException e) {
            throw new HgInternalError(parent.toString(), e);
        }
    }

    /**
     * Creates a specific RevlogEntry.
     * 
     * @param parent
     *            revlog
     * @param data
     *            data
     * @param off
     *            where in data to begin extracting data
     * 
     * @return revlogEntry
     */
    public static RevlogEntry valueOf(Revlog parent, byte[] data, int off) throws HgInternalError {
        ByteArrayInputStream copy = new ByteArrayInputStream(data, off, BINARY_LENGTH);
        DataInputStream reader = new DataInputStream(copy);
        RevlogEntry entry = new RevlogEntry(parent, reader, off);
        return entry;
    }

    /**
     * Returns the base revision. The first revision going backwards from the
     * tip that has two parents (the product of a merge).
     * 
     * @see <a
     *      href="http://www.selenic.com/mercurial/wiki/index.cgi/WireProtocol">WireProtocol</a>
     * 
     * @return baseRev
     */
    public int getBaseRev() {
        return baseRev;
    }

    /**
     * Returns the link revision of the {@link RevlogEntry}, that is a link to
     * the revision in the Revlog.
     * 
     * @return linkRev
     */
    public int getLinkRev() {
        return linkRev;
    }

    /**
     * Loads a block as a byte array from a file. In RevlogNG data may be
     * inlined.
     * 
     * @param file
     *            to read from
     * @return byte array
     * @throws IOException
     *             might occur if we can not read from file
     */
    public byte[] loadBlock(RandomAccessFile file) throws IOException {
        long off = this.offset;
        if (parent.isDataInline) {
            off += (revision + 1L) * RevlogEntry.BINARY_LENGTH;
        }
        file.seek(off);
        return read(file);
    }

    /**
     * Reads compressed data from the Revlog.
     * 
     * @param file
     *            to read from
     * @return data array
     * @throws IOException
     */
    private byte[] read(RandomAccessFile file) throws IOException {
        byte[] data = new byte[(int) this.compressedLength];
        int read = file.read(data);
        assert read == (int) this.compressedLength;
        return data;
    }

    /**
     * Returns the offset of the entry in the Revlog.
     * 
     * @return offset
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Returns uncompressed length of data.
     * 
     * @return uncompressed length
     */
    long getUncompressedLength() {
        return uncompressedLength;
    }

    /**
     * Returns compressed length of data.
     * 
     * @return compressed length
     */
    long getCompressedLength() {
        return compressedLength;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        RevlogEntry p1 = getNullEntry();
        RevlogEntry p2 = getNullEntry();

        if (0 <= firstParentRev) {
            p1 = parent.index.get(firstParentRev);
        }
        if (0 <= secondParentRev) {
            p2 = parent.index.get(secondParentRev);
        }
        return revision + "  " + offset + "	" + compressedLength + " 		"
                // + uncompressedLength + " "
                + baseRev + " 	" + linkRev + " 	" + nodeId.asShort() + " 	" + p1.nodeId.asShort() + " 	"
                + p2.nodeId.asShort();
    }

    /**
     * Returns the null entry.
     * 
     * @return null entry
     */
    static RevlogEntry getNullEntry() {
        return nullInstance;
    }

    /**
     * Returns the nodeId of the {@link RevlogEntry}.
     * 
     * @return nodeId
     */
    public NodeId getId() {
        return nodeId;
    }

    /**
     * Returns the flags of the {@link RevlogEntry}.
     * 
     * @return flags
     */
    public int getFlags() {
        return flags;
    }

    /**
     * rtholmes: added method
     * @return
     */
    public RevlogEntry getFirstParent() {
        RevlogEntry p1 = getNullEntry();

        if (0 <= firstParentRev) {
            p1 = parent.index.get(firstParentRev);
        }
        return p1;
    }

    /**
     * rtholmes: added method
     * @return
     */
    public RevlogEntry getSecondParent() {
        RevlogEntry p2 = getNullEntry();

        if (0 <= secondParentRev) {
            p2 = parent.index.get(secondParentRev);
        }
        
        return p2;
    }

}
