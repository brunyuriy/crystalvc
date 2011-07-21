/*
 Copyright 2008 Stefan Chyssler 

 This software may be used and distributed according to the terms of
 the GNU General Public License or under the Eclipse Public Licence (EPL)
 */
package org.freehg.hgkit.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.freehg.hgkit.HgInternalError;
import org.freehg.hgkit.util.GlobExpressions;

/**
 * Holds and parses the <tt>.hgignore</tt> entries.
 */
public abstract class Ignore {

    /**
     * Checks whether file should be ignored.
     * 
     * @param file
     *            to check
     * @return if the file is ignored
     */
    public abstract boolean isIgnored(final File file);

    /**
     * Creates a new Ignore class.
     * 
     * @param repo
     *            a repository
     * @param ignoreFile
     *            the ignoreFile
     * @return Ignore
     */
    public static Ignore valueOf(final Repository repo, final File ignoreFile) {
        if (ignoreFile.exists()) {
            final ExistingIgnore ignore = new ExistingIgnore(repo, ignoreFile);
            if (ignore.ignorePatterns.isEmpty()) {
                return EmptyOrNonExistingIgnore.INSTANCE;
            } else {
                return ignore;
            }
        } else {
            return EmptyOrNonExistingIgnore.INSTANCE;
        }
    }

    /**
     * Non existing or empty ignore file
     */
    static final class EmptyOrNonExistingIgnore extends Ignore {

        final static EmptyOrNonExistingIgnore INSTANCE = new EmptyOrNonExistingIgnore();

        /** {@inheritDoc} */
        @Override
        public boolean isIgnored(File file) {
            return false;
        }
    }

    static final class ExistingIgnore extends Ignore {

        /**
         * Enum to switch parsing between different syntaxes.
         */
        private enum Syntax {
            GLOB {
                /** {@inheritDoc} */
                @Override
                public Pattern createPattern(String line) {
                    return GlobExpressions.toRegex(line);
                }
            },
            REGEX {
                /** {@inheritDoc} */
                @Override
                public Pattern createPattern(String line) {
                    return Pattern.compile(line);
                }
            };

            /**
             * Creates a pattern depending on the syntax.
             * 
             * @param line
             * @return
             */
            abstract Pattern createPattern(final String line);
        };

        /**
         * prefix of a syntax line in the ignore file.
         */
        private static final String SYNTAX_PREFIX = "syntax:";

        final List<Pattern> ignorePatterns = new ArrayList<Pattern>();

        private Syntax currentSyntax = Syntax.REGEX;

        private final Repository repo;

        /**
         * Constructor for tests.
         * 
         * @param aRepo
         *            repo
         */
        ExistingIgnore(final Repository aRepo) {
            this.repo = aRepo;
        }

        /**
         * Constructor with an ignore file.
         * 
         * @param repo
         *            repo
         * @param ignoreFile
         *            ignore file
         */
        ExistingIgnore(final Repository repo, final File ignoreFile) {
            this(repo);
            try {
                parse(ignoreFile);
            } catch (IOException e) {
                throw new HgInternalError("Could not parse " + ignoreFile, e);
            }
        }

        /**
         * Checks whether file should be ignored.
         * 
         * @param file
         *            to check
         * @return if the file is ignored
         */
        public boolean isIgnored(final File file) {
            final String path = repo.getRelativePath(file);
            for (final Pattern ignorePattern : ignorePatterns) {
                if (ignorePattern.matcher(path).matches()) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Parses an ignore file.
         * 
         * @param file
         *            ignore file
         * 
         * @throws IOException
         */
        void parse(File file) throws IOException {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            try {
                parse(reader);
            } finally {
                reader.close();
            }
        }

        /**
         * Takes a reader, this for easier tests.
         * 
         * @param reader
         *            to read ignore entries from
         * 
         * @throws IOException
         */
        void parse(BufferedReader reader) throws IOException {
            String readLine = null;
            while (null != (readLine = reader.readLine())) {
                String line = readLine.trim();
                if (0 < line.length()) {
                    try {
                        parseLine(line);
                    } catch (PatternSyntaxException e) {
                        throw new HgInternalError("Could not parse line " + line, e);
                    }
                }
            }
        }

        /**
         * Parses a single line of an ignore file.
         * 
         * @param line
         *            to parse
         */
        void parseLine(String line) {
            if (line.startsWith(SYNTAX_PREFIX)) {
                changeSyntax(line.substring(SYNTAX_PREFIX.length()).trim());
            } else {
                ignorePatterns.add(currentSyntax.createPattern(line));
            }
        }

        /**
         * Changes the current syntax if one of {@link Syntax#GLOB} or
         * {@link Syntax#REGEX} occur in the text.
         * 
         * @param text
         *            to inspect
         */
        void changeSyntax(String text) {
            try {
                currentSyntax = Syntax.valueOf(text.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException e) {
                throw new HgInternalError("Unknown Ignore-Syntax:" + text, e);
            }
        }
    }
}
