package org.freehg.hgkit.core;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple dataholder class for mdiff.
 */
final class Fragment {
    
    /** Maximal line length of {@link Fragment#toString()}. */
    private static final int LINE_LENGTH = 80;

    /** Where in the "file" this fragment starts. **/
    int start;

    /** Where in the "file" this fragments ends. */
    int end;

    /** The data to be inserted into the file at {@code this.start} up to {@code this.end}. */
    byte[] data;

    /** Where in {@code this.data} to begin read. */
    int offset = 0;

    /**
     * The length of the data to read, this can differ from {@code data.length}. Combine
     * {@code offset}, {@code data} and {@code mlength} to get patch data.
     */
    int mlength = -1;

    /** The length of the fragment, may differ from {@code end - start} and {@code data.length}. */
    int len() {
        if (mlength == -1) {
            throw new IllegalStateException("Length not set yet");
        }
        return mlength;
    }

    /**
     * Sets the length of the patch data.
     * @param len new length.
     */
    public void len(int len) {
        mlength = len;
    }

    @Override
    public String toString() {
        String txt = new String(this.data);
        txt = txt.substring(this.offset);
        int max = Math.min(LINE_LENGTH, len());
        txt = txt.substring(0, max);
        if (len() > LINE_LENGTH) {
            txt += "...";
        }
        return start + " " + end + " " + len() + " " + txt;
    }

}

/**
 * Creates diffs.
 */
public final class MDiff {
    
    /** Static helper class. */
    private MDiff() {
        // nope
    }
    
    /**
     * Creates patches.
     * 
     * @param in
     * @param bins
     * @param out receives patches
     */
    public static void patches(byte[] in, List<byte[]> bins, OutputStream out) {
        // if there are no fragments we don't have to do anything
        try {
            // convert binary to fragments
            List<Fragment> patch = fold(bins, 0, bins.size());
            if (patch == null) {
                throw new IllegalStateException("Error folding patches");
            }
            // apply all fragments to in
            apply(in, patch, out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static LinkedList<Fragment> fold(List<byte[]> bins, int start, int end) {
        if (bins.size() < 1) {
            return new LinkedList<Fragment>();
        }
        /*
         * recursively generate a patch of all bins between start and end
         */
        if (start + 1 == end) {
            return decode(bins.get(start));
        }

        /* divide and conquer, memory management is elsewhere */
        final int len = (end - start) / 2;
        LinkedList<Fragment> left = fold(bins, start, start + len);
        LinkedList<Fragment> right = fold(bins, start + len, end);
        return combine(left, right);
    }

    /*
     * Combines hunk lists a and b, while adjusting b for offset changes in a.
     * This deletes a and b and returns the resultant list.
     */
    private static LinkedList<Fragment> combine(LinkedList<Fragment> a, LinkedList<Fragment> b) {

        if (a == null || b == null) {
            return null;
        }
        LinkedList<Fragment> combination = new LinkedList<Fragment>();
        int offset = 0;
        for (Fragment bFrag : b) {
            /* save old hunks */
            offset = gather(combination, a, bFrag.start, offset);

            /* discard replaced hunks */
            int post = discard(a, bFrag.end, offset);

            // create a new fragment from an existing with ajustments
            Fragment ct = new Fragment();
            ct.start = bFrag.start - offset;
            ct.end = bFrag.end - post;
            ct.data = bFrag.data;
            ct.offset = bFrag.offset;
            ct.len(bFrag.len());
            combination.add(ct);

            offset = post;
        }

        /* hold on to tail from a */
        combination.addAll(a);
        a.clear();
        b.clear();
        return combination;
    }

    // static int discard(struct flist *src, int cut, int offset) {
    private static int discard(LinkedList<Fragment> src, int cut, int poffset) {

        int offset = poffset;
        int postend, c, l;
        // i think this discards everything up to "cut"
        // a fragment may have to be split if it
        // overlaps "cut"
        for (Iterator<Fragment> iter = src.iterator(); iter.hasNext();) {
            final Fragment s = iter.next();
            if (cut <= s.start + offset) {
                break;
            }

            postend = offset + s.start + s.len();
            if (postend <= cut) {
                // this one is discarded
                offset += s.start + s.len() - s.end;
                iter.remove();
            } else {
                // partial discarding, move the content of s.data so that it
                // doesn't overlap cut
                c = cut - offset;
                if (s.end < c) {
                    c = s.end;
                }
                l = cut - offset - s.start;
                if (s.len() < l) {
                    l = s.len();
                }

                offset += s.start + l - c;
                s.start = c;
                s.len(s.len() - l);

                // s.data = s.data + l;
                // this should work, but doesnt, bug?
                // s.data = Arrays.copyOfRange(s.data, l, s.len());
                s.offset += l;
                // s.data = Arrays.copyOfRange(s.data, l, s.data.length);
                // no more needs to be discarded
                break;
            }
        }
        return offset;
    }

    private static int gather(LinkedList<Fragment> dest, LinkedList<Fragment> src, int cut, int poffset) {

        /*
         * move hunks in source that are less than cut to dest, but compensate
         * for changes in offset. The last hunk may be split if necessary
         * (oberlaps cut).
         */
        int offset = poffset;
        Fragment s = null;

        for (Iterator<Fragment> iter = src.iterator(); iter.hasNext();) {
            s = iter.next();
            if (cut <= s.start + offset) {
                break; /* we've gone far enough */
            }

            int postend = offset + s.start + s.len();
            if (postend <= cut) {
                /* save this hunk as it is */
                offset += s.start + s.len() - s.end;
                dest.add(s);
                iter.remove();

            } else {
                /* This hunk must be broken up */
                int cutAt = cut - offset;
                if (s.end < cutAt) {
                    cutAt = s.end;
                }
                int length = cut - offset - s.start;
                if (s.len() < length) {
                    length = s.len();
                }

                offset += s.start + length - cutAt;
                Fragment d = new Fragment();
                d.start = s.start;
                d.end = cutAt;

                d.data = s.data;
                d.offset = s.offset;

                d.len(length);
                dest.add(d);

                s.start = cutAt;
                s.len(s.len() - length);
                s.offset += length;
                break;
            }
        }

        if (0 < src.size() && s != src.get(0)) {
            throw new IllegalStateException("src head should be s");
        }
        return offset;
    }

    /**
     * Decodes a binary patch into a fragment list.
     * 
     * @param bin
     *            the binary patch
     * @return a list of fragments
     */
    private static LinkedList<Fragment> decode(byte[] bin) {
        // int start, int end, int len, byte[...] data
        LinkedList<Fragment> result = new LinkedList<Fragment>();
        ByteBuffer reader = ByteBuffer.wrap(bin);
        // while(reader.position() < length) {
        while (reader.hasRemaining()) {
            Fragment lt = new Fragment();
            result.add(lt);

            lt.start = reader.getInt();
            lt.end = reader.getInt();
            lt.len(reader.getInt());

            if (lt.start > lt.end) {
                break; /* sanity check */
            }

            lt.offset = reader.position();
            lt.data = bin;

            if (lt.len() < 0) {
                throw new IllegalStateException(
                        "Programmer Unsure of what  'big data + big (bogus) len can wrap around' means");
            }
            reader.position(reader.position() + lt.len());
        }

        if (reader.hasRemaining()) {
            throw new IllegalStateException("patch cannot be decoded");
        }
        return result;
    }

    private static void apply(byte[] orig, List<Fragment> fragments, OutputStream out) throws IOException {
        final int len = orig.length;
        int last = 0;
        for (final Fragment fragment : fragments) {
            // if this fragment is not within the bounds
            if (fragment.start < last || len < fragment.end) {
                throw new IllegalStateException("invalid patch");
            }
            out.write(orig, last, fragment.start - last);
            out.write(fragment.data, fragment.offset, fragment.len());
            last = fragment.end;
        }
        out.write(orig, last, len - last);
    }
}
