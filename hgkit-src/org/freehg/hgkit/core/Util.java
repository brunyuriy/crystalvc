package org.freehg.hgkit.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.freehg.hgkit.HgInternalError;

/**
 * Helper class with static methods for decompression and some stream
 * manipulations.
 */
public final class Util {

    private static final int BUFFER_SIZE = 16 * 1024;

    private static final int ASSUMED_COMPRESSION_RATIO = 3;

    private static final char ZLIB_COMPRESSION = 'x';

    private static final char UNCOMPRESSED = 'u';

    static final int EOF = -1;

    /**
     * Do not instantiate.
     */
    private Util() {
        // static class
    }

    /**
     * Decompresses zlib-compressed data.
     * 
     * @param data
     *            data
     * @return decompressed data
     * @throws IOException
     */
    static byte[] doDecompress(byte[] data) throws IOException {
        ByteArrayOutputStream uncompressedOut = new ByteArrayOutputStream(data.length * ASSUMED_COMPRESSION_RATIO);
        // decompress the bytearray using what should be python zlib
        final byte[] buffer = new byte[BUFFER_SIZE];
        final InflaterInputStream inflaterInputStream = new InflaterInputStream(new ByteArrayInputStream(data));
        int len = 0;
        while ((len = inflaterInputStream.read(buffer)) != EOF) {
            uncompressedOut.write(buffer, 0, len);
        }
        return uncompressedOut.toByteArray();
    }

    /**
     * Eventually decompresses the given data.
     * 
     * @param data
     *            eventually compressed data
     * @return byte-array with decompressed data.
     */
    public static byte[] decompress(byte[] data) {
        try {
            if (data.length < 1) {
                return new byte[0];
            }
            byte dataHeader = data[0];
            switch (dataHeader) {
            case UNCOMPRESSED:
                final byte[] copy = new byte[data.length - 1];
                System.arraycopy(data, 1, copy, 0, data.length - 1);
                return copy;
            case ZLIB_COMPRESSION:
                return doDecompress(data);
            case 0:
                return data;
            default:
                throw new HgInternalError("Unknown compression type : " + (char) (dataHeader));
            }
        } catch (IOException e) {
            throw new HgInternalError("Could not decompress" + new String(data), e);
        }
    }

    /**
     * Replace every backslash with a forward slash
     * 
     * @param path
     * @return corrected path.
     */
    public static String forwardSlashes(String path) {
        return path.replace('\\', '/');
    }

    /**
     * Reads resource into byte array and closes it immediately.
     * 
     * @param name
     *            resource name.
     * @return the content of the resource <code>name</code> as byte array
     */
    static byte[] readResource(final String name) {
        InputStream in = Util.class.getResourceAsStream(name);
        try {
            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            throw new HgInternalError("Error reading " + name, e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    /**
     * Returns the canonical file, rethrow {@link IOException} as
     * {@link HgInternalError}.
     * 
     * @param file
     *            file
     * @return canonical file
     */
    static File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            throw new HgInternalError(file.toString(), e);
        }
    }

    /**
     * Returns the canonical path, rethrow {@link IOException} as
     * {@link HgInternalError}.
     * 
     * @param file
     *            file
     * @return canonical path
     */
    static String getCanonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            throw new HgInternalError(file.toString(), e);
        }
    }

    /**
     * Returns the content of filename or the empty string if filename could not
     * be found.
     * 
     * @param filename
     *            to read
     * @return content of the file
     * 
     * @throws HgInternalError
     *             if there is an {@link IOException}.
     */
    public static String readFile(String filename) throws HgInternalError {
        try {
            return FileUtils.readFileToString(new File(filename));
        } catch (FileNotFoundException e) {
            return "";
        } catch (IOException e) {
            throw new HgInternalError("Could not read " + filename, e);
        }
    }
}
