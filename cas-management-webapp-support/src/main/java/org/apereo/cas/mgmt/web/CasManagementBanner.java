package org.apereo.cas.mgmt.web;

import org.apereo.cas.util.AsciiArtUtils;
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

    @Override
    public void printBanner(final Environment environment, final Class<?> sourceClass, final PrintStream out) {
        AsciiArtUtils.printAsciiArt(out, "CAS Management");
    }
}


