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
    /**
     * Logger marker element responsible for logging ascii-art.
     */
    public static final Marker ASCII_ART_LOGGER_MARKER = MarkerFactory.getMarker("AsciiArt");

    private static final String ANSI_RESET = "\u001B[0m";

    private static final String ANSI_CYAN = "\u001B[36m";

    private static final String ANSI_BOLD = "\u001B[1m";

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
            out.println(ANSI_BOLD + asciiArt + ANSI_RESET);
            out.println(additional);
        } else {
            out.println(ANSI_BOLD + asciiArt + ANSI_RESET);
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
             ____  ____  __  ____\s
            / ___)(_  _)/  \\(  _ \\
            \\___ \\  )( (  O )) __/
            (____/ (__) \\__/(__) \s
            """;
        val message = ANSI_BOLD + "\n\n".concat(ascii).concat(additional) + ANSI_RESET;
        out.warn(ASCII_ART_LOGGER_MARKER, message);
    }

    /**
     * Print ascii art info.
     *
     * @param out        the out
     * @param additional the additional
     */
    public static void printAsciiArtReady(final Logger out, final String additional) {
        val ascii = """
             ____  ____   __   ____  _  _\s
            (  _ \\(  __) / _\\ (    \\( \\/ )
             )   / ) _) /    \\ ) D ( )  /\s
            (__\\_)(____)\\_/\\_/(____/(__/ \s
            """;
        val message = ANSI_BOLD + "\n\n".concat(ascii).concat(additional).concat("\n") + ANSI_RESET;
        out.info(ASCII_ART_LOGGER_MARKER, message);
    }

}
