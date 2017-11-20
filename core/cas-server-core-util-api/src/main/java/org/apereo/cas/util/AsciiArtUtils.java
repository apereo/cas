package org.apereo.cas.util;

import com.github.lalyos.jfiglet.FigletFont;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.PrintStream;

/**
 * This is {@link AsciiArtUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public final class AsciiArtUtils {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";

    private AsciiArtUtils() {
    }

    /**
     * Print ascii art.
     *
     * @param out      the out
     * @param asciiArt the ascii art
     */
    public static void printAsciiArt(final PrintStream out, final String asciiArt) {
        printAsciiArt(out, asciiArt, null);
    }

    /**
     * Print ascii art.
     *
     * @param out        the out
     * @param asciiArt   the ascii art
     * @param additional the additional
     */
    public static void printAsciiArt(final PrintStream out, final String asciiArt, final String additional) {
        try {
            out.println(ANSI_CYAN);
            if (StringUtils.isNotBlank(additional)) {
                out.println(FigletFont.convertOneLine(asciiArt));
                out.println(additional);
            } else {
                out.print(FigletFont.convertOneLine(asciiArt));
            }
            out.println(ANSI_RESET);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    /**
     * Print ascii art.
     *
     * @param out        the out
     * @param asciiArt   the ascii art
     * @param additional the additional
     */
    public static void printAsciiArtWarning(final Logger out, final String asciiArt, final String additional) {
        try {
            out.warn(ANSI_CYAN);
            out.warn("\n\n".concat(FigletFont.convertOneLine(asciiArt)).concat(additional));
            out.warn(ANSI_RESET);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Print ascii art info.
     *
     * @param out        the out
     * @param asciiArt   the ascii art
     * @param additional the additional
     */
    public static void printAsciiArtInfo(final Logger out, final String asciiArt, final String additional) {
        try {
            out.info(ANSI_CYAN);
            out.info("\n\n".concat(FigletFont.convertOneLine(asciiArt)).concat(additional));
            out.info(ANSI_RESET);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
