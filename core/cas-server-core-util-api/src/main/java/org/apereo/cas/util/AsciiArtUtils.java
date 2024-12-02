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

              _____ ______   ___   ____  __\s
             / ___/|      T /   \\ |    \\|  T
            (   \\_ |      |Y     Y|  o  )  |
             \\__  Tl_j  l_j|  O  ||   _/|__j
             /  \\ |  |  |  |     ||  |   __\s
             \\    |  |  |  l     !|  |  |  T
              \\___j  l__j   \\___/ l__j  l__j
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
             
             ____     ___   ____  ___    __ __\s
            |    \\   /  _] /    T|   \\  |  T  T
            |  D  ) /  [_ Y  o  ||    \\ |  |  |
            |    / Y    _]|     ||  D  Y|  ~  |
            |    \\ |   [_ |  _  ||     |l___, |
            |  .  Y|     T|  |  ||     ||     !
            l__j\\_jl_____jl__j__jl_____jl____/\s
                                              \s
                        """;
        out.info(ASCII_ART_LOGGER_MARKER, ANSI_CYAN);
        out.info(ASCII_ART_LOGGER_MARKER, "\n\n".concat(ascii).concat(additional).concat("\n"));
        out.info(ASCII_ART_LOGGER_MARKER, ANSI_RESET);
    }

}
