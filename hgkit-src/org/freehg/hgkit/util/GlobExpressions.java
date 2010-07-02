package org.freehg.hgkit.util;

import java.io.File;
import java.util.regex.Pattern;

public final class GlobExpressions {

    public static Pattern toRegex(String globPattern) {
        if ((File.separatorChar == '\\')) {
            return windowsGlob(globPattern);
        } else {
            return unixGlob(globPattern);
        }
    }

    static Pattern unixGlob(String globPattern) {
        StringBuilder regex = new StringBuilder();
        char[] globPatt = globPattern.toCharArray();
        boolean inBrackets = false;

        for (int i = 0; i < globPatt.length; i++) {
            switch (globPatt[i]) {
            case '*':
                if (!inBrackets) {
                    regex.append('.');
                }
                regex.append('*');
                break;

            case '?':
                if (inBrackets) {
                    regex.append('?');
                } else {
                    regex.append('.');
                }
                break;
            case '[':
                inBrackets = true;
                regex.append(globPatt[i]);
                if (i < globPatt.length - 1) {
                    switch (globPatt[i + 1]) {
                    case '!':
                    case '^':
                        regex.append('^');
                        i++;
                        break;
                    case ']':
                        regex.append(globPatt[++i]);
                        break;
                    default:
                        break;
                    }                    
                }
                break;
            case ']':
                regex.append(globPatt[i]);
                inBrackets = false;
                break;
            case '\\':
                if (i == 0 && globPatt.length > 1 && globPatt[1] == '~') {
                    regex.append(globPatt[++i]);
                } else {
                    regex.append('\\');
                    if (i < globPatt.length - 1 && "*?[]".indexOf(globPatt[i + 1]) >= 0) {
                        regex.append(globPatt[++i]);
                    } else {
                        regex.append('\\');
                    }
                }
                break;
            default:
                if (!Character.isLetterOrDigit(globPatt[i])) {
                    regex.append('\\');
                }
                regex.append(globPatt[i]);
                break;
            }
        }
        return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE);
    }

    static Pattern windowsGlob(String globPattern) {
        char[] globPatt = globPattern.toCharArray();
        /** In the worst case we have to escape all characters **/
        // char[] regex = new char[globPatt.length * 2];
        StringBuilder regex = new StringBuilder();
        int len = globPatt.length;
        if (globPattern.endsWith("*.*")) {
            len -= 2;
        }
        for (int i = 0; i < len; i++) {
            switch (globPatt[i]) {
            case '*':
                regex.append(".*");
                break;
            case '?':
                regex.append('.');
                break;
            case '\\':
                regex.append("\\\\");

                break;

            default:
                if ("+()^$.{}[]".indexOf(globPatt[i]) >= 0) {
                    regex.append('\\');
                }
                regex.append(globPatt[i]);
                break;
            }
        }
        return Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE);
    }
}
