package org.apereo.cas.util;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.PrintStream;

/**
 * This is {@link AsciiArtUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@UtilityClass
public class AsciiArtUtils {
    private static final Marker ASCII_ART_LOGGER_MARKER = MarkerFactory.getMarker("AsciiArt");

    private static final String ANSI_RESET = "\u001B[0m";

    private static final String ANSI_CYAN = "\u001B[36m";

    /**
     * Print ascii art.
     *
     * @param out        the out
     * @param asciiArt   the ascii art
     * @param additional the additional
     */
    public static void printAsciiArt(final PrintStream out, final String asciiArt, final String additional) {
        out.println(ANSI_CYAN);
        if (StringUtils.isNotBlank(additional)) {
            out.println(asciiArt);
            out.println(additional);
        } else {
            out.print(asciiArt);
        }
        out.println(ANSI_RESET);
    }

    /**
     * Print ascii art.
     *
     * @param out        the out
     * @param additional the additional
     */
    public static void printAsciiArtWarning(final Logger out, final String additional) {
        val ascii = """

              ____ _____ ___  ____  _\s
             / ___|_   _/ _ \\|  _ \\| |
             \\___ \\ | || | | | |_) | |
              ___) || || |_| |  __/|_|
             |____/ |_| \\___/|_|   (_)
                                     \s
            """;
        out.warn(ASCII_ART_LOGGER_MARKER, ANSI_CYAN);
        out.warn(ASCII_ART_LOGGER_MARKER, "\n\n".concat(ascii).concat(additional));
        out.warn(ASCII_ART_LOGGER_MARKER, ANSI_RESET);
    }

    /**
     * Print ascii art info.
     *
     * @param out        the out
     * @param additional the additional
     */
    public static void printAsciiArtReady(final Logger out, final String additional) {
        val ascii = """

              ____  _____    _    ______   __
             |  _ \\| ____|  / \\  |  _ \\ \\ / /
             | |_) |  _|   / _ \\ | | | \\ V /\s
             |  _ <| |___ / ___ \\| |_| || | \s
             |_| \\_\\_____/_/   \\_\\____/ |_| \s
                                            \s
            """;
        out.info(ASCII_ART_LOGGER_MARKER, ANSI_CYAN);
        out.info(ASCII_ART_LOGGER_MARKER, "\n\n".concat(ascii).concat(additional));
        out.info(ASCII_ART_LOGGER_MARKER, ANSI_RESET);
    }

}
