package org.apereo.cas.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.io.PrintStream;

/**
 * This is {@link AsciiArtUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@UtilityClass
public class AsciiArtUtils {
    private static final String ANSI_RESET = "\u001B[0m";

    private static final String ANSI_CYAN = "\u001B[36m";


    /**
     * Print ascii art.
     *
     * @param out        the out
     * @param asciiArt   the ascii art
     * @param additional the additional
     */
    @SneakyThrows
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
    @SneakyThrows
    public static void printAsciiArtWarning(final Logger out, final String additional) {
        val ascii = "\n"
            + "  ____ _____ ___  ____  _ \n"
            + " / ___|_   _/ _ \\|  _ \\| |\n"
            + " \\___ \\ | || | | | |_) | |\n"
            + "  ___) || || |_| |  __/|_|\n"
            + " |____/ |_| \\___/|_|   (_)\n"
            + "                          \n";
        out.warn(ANSI_CYAN);
        out.warn("\n\n".concat(ascii).concat(additional));
        out.warn(ANSI_RESET);
    }

    /**
     * Print ascii art info.
     *
     * @param out        the out
     * @param additional the additional
     */
    @SneakyThrows
    public static void printAsciiArtReady(final Logger out, final String additional) {
        val ascii = "\n"
            + "  ____  _____    _    ______   __\n"
            + " |  _ \\| ____|  / \\  |  _ \\ \\ / /\n"
            + " | |_) |  _|   / _ \\ | | | \\ V / \n"
            + " |  _ <| |___ / ___ \\| |_| || |  \n"
            + " |_| \\_\\_____/_/   \\_\\____/ |_|  \n"
            + "                                 \n";
        out.info(ANSI_CYAN);
        out.info("\n\n".concat(ascii).concat(additional));
        out.info(ANSI_RESET);
    }

}
