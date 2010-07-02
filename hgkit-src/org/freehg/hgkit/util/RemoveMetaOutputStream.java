package org.freehg.hgkit.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Removes the metadata from a patch-set. Metadata-borders are marked by special
 * bytes. Metadata starts with
 * {@link RemoveMetaOutputStream#FIRST_METADATA_BYTE} followed by
 * {@link RemoveMetaOutputStream#SECOND_METADATA_BYTE} followed by the actual
 * metadata followed by {@link RemoveMetaOutputStream#FIRST_METADATA_BYTE}
 * followed by {@link RemoveMetaOutputStream#SECOND_METADATA_BYTE} followed by
 * the actual content.
 */
public final class RemoveMetaOutputStream extends OutputStream {

    private static final int SECOND_METADATA_BYTE = '\n';

    private static final int FIRST_METADATA_BYTE = 1;

    private OutputStream current;

    private final OutputStream state1;

    private final OutputStream state2;

    private final OutputStream state3;

    private final OutputStream state4;

    /**
     * This may look weird but uses the state pattern
     * 
     * <pre>
     *   S1 -&gt;  S2 -&gt; S3 &lt;-&gt; S4
     *    \     |          /
     *     \    |         /
     *      \  \/        /
     *       \- &gt;S5 &lt; -/
     * </pre>
     * 
     * @param decoratedOut
     *            an OutputStream potentially containing Metadata.
     */
    public RemoveMetaOutputStream(final OutputStream decoratedOut) {
        state1 = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                if (b == FIRST_METADATA_BYTE) {
                    // Only metadata if followed by SECOND_METADATA_BYTE
                    current = state2;
                } else {
                    current = decoratedOut;
                    decoratedOut.write(b);
                }
            }
        };

        state2 = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                if (b == SECOND_METADATA_BYTE) {
                    // We are really in metadata now.
                    current = state3;
                } else {
                    current = decoratedOut;
                    decoratedOut.write(1);
                    decoratedOut.write(b);
                }
            }
        };

        state3 = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                if (b == FIRST_METADATA_BYTE) {
                    // Metadata maybe ends.
                    current = state4;
                }
            }
        };

        state4 = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                if (b == SECOND_METADATA_BYTE) {
                    // Metadata maybe ends.
                    current = decoratedOut;
                } else {
                    // Still in metadata
                    current = state3;
                }
            }
        };
        this.current = state1;

    }

    @Override
    public void write(int b) throws IOException {
        this.current.write(b);
    }
}
