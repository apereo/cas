package org.apereo.cas.web.support;

import org.apereo.cas.CasVersion;
import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

import java.io.PrintStream;
import java.util.Formatter;
import java.util.Properties;

/**
 * This is {@link CasBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasBanner implements Banner {

    private static final String[] BANNER = {
            "                       ______ ___    _____",
            "                      / ____//   |  / ___/",
            "                     / /    / /| |  \\__ \\ ",
            "                    / /___ / ___ | ___/ / ",
            "                    \\____//_/  |_|/____/  "};
    
    @Override
    public void printBanner(final Environment environment, final Class<?> sourceClass, final PrintStream out) {
        for (final String line : BANNER) {
            out.println(line);
        }
        out.println(collectEnvironmentInfo());
    }

    /**
     * Collect environment info with
     * details on the java and os deployment
     * versions.
     *
     * @return environment info
     */
    private String collectEnvironmentInfo() {
        final Properties properties = System.getProperties();
        try (final Formatter formatter = new Formatter()) {
            formatter.format("\n******************** Welcome to CAS *******************\n");
            formatter.format("CAS Version: %s\n", CasVersion.getVersion());
            formatter.format("Build Date/Time: %s\n", CasVersion.getDateTime());
            formatter.format("Java Home: %s\n", properties.get("java.home"));
            formatter.format("Java Vendor: %s\n", properties.get("java.vendor"));
            formatter.format("Java Version: %s\n", properties.get("java.version"));
            formatter.format("OS Architecture: %s\n", properties.get("os.arch"));
            formatter.format("OS Name: %s\n", properties.get("os.name"));
            formatter.format("OS Version: %s\n", properties.get("os.version"));
            formatter.format("*******************************************************\n");
            return formatter.toString();
        }
    }
}
