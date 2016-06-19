package org.apereo.cas.mgmt.web;

import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

import java.io.PrintStream;

/**
 * This is {@link CasManagementBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasManagementBanner implements Banner {

    private static final String[] BANNER = {
            "  ____    _    ____    __  __                                                   _   ",
            " / ___|  / \\  / ___|  |  \\/  | __ _ _ __   __ _  __ _  ___ _ __ ___   ___ _ __ | |_ ",
            "| |     / _ \\ \\___ \\  | |\\/| |/ _` | '_ \\ / _` |/ _` |/ _ \\ '_ ` _ \\ / _ \\ '_ \\| __|",
            "| |___ / ___ \\ ___) | | |  | | (_| | | | | (_| | (_| |  __/ | | | | |  __/ | | | |_ ",
            " \\____/_/   \\_\\____/  |_|  |_|\\__,_|_| |_|\\__,_|\\__, |\\___|_| |_| |_|\\___|_| |_|\\__|",
            "                                                |___/                              " };

    @Override
    public void printBanner(final Environment environment, final Class<?> sourceClass, final PrintStream out) {
        out.println();
        for (final String line : BANNER) {
            out.println(line);
        }
    }
}


