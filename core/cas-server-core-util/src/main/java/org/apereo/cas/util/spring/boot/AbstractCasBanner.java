package org.apereo.cas.util.spring.boot;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.AsciiArtUtils;
import org.apereo.cas.util.CasVersion;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.env.Environment;

import javax.crypto.Cipher;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Formatter;
import java.util.Properties;

/**
 * This is {@link AbstractCasBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractCasBanner implements Banner {
    /** Line separator length. */
    public static final int SEPARATOR_REPEAT_COUNT = 60;
    /** A line separator. */
    public static final String LINE_SEPARATOR = String.join(StringUtils.EMPTY, Collections.nCopies(SEPARATOR_REPEAT_COUNT, "-"));
    
    @Override
    public void printBanner(final Environment environment, final Class<?> sourceClass, final PrintStream out) {
        AsciiArtUtils.printAsciiArt(out, getTitle(), collectEnvironmentInfo(environment, sourceClass));
    }

    protected String getTitle() {
        return "(CAS)";
    }

    /**
     * Collect environment info with
     * details on the java and os deployment
     * versions.
     *
     * @param environment the environment
     * @param sourceClass the source class
     * @return environment info
     */
    private String collectEnvironmentInfo(final Environment environment, final Class<?> sourceClass) {
        final Properties properties = System.getProperties();
        if (properties.containsKey("CAS_BANNER_SKIP")) {
            try (Formatter formatter = new Formatter()) {
                formatter.format("CAS Version: %s%n", CasVersion.getVersion());
                return formatter.toString();
            }
        }

        try (Formatter formatter = new Formatter()) {
            formatter.format("CAS Version: %s%n", CasVersion.getVersion());
            formatter.format("CAS Commit Id: %s%n", CasVersion.getSpecificationVersion());
            formatter.format("CAS Build Date/Time: %s%n", CasVersion.getDateTime());
            formatter.format("Spring Boot Version: %s%n", SpringBootVersion.getVersion());
            formatter.format("%s%n", LINE_SEPARATOR);

            formatter.format("System Date/Time: %s%n", LocalDateTime.now());
            formatter.format("System Temp Directory: %s%n", FileUtils.getTempDirectoryPath());
            formatter.format("%s%n", LINE_SEPARATOR);

            formatter.format("Java Home: %s%n", properties.get("java.home"));
            formatter.format("Java Vendor: %s%n", properties.get("java.vendor"));
            formatter.format("Java Version: %s%n", properties.get("java.version"));
            formatter.format("JCE Installed: %s%n", BooleanUtils.toStringYesNo(isJceInstalled()));
            formatter.format("%s%n", LINE_SEPARATOR);

            formatter.format("OS Architecture: %s%n", properties.get("os.arch"));
            formatter.format("OS Name: %s%n", properties.get("os.name"));
            formatter.format("OS Version: %s%n", properties.get("os.version"));
            formatter.format("%s%n", LINE_SEPARATOR);

            injectEnvironmentInfoIntoBanner(formatter, environment, sourceClass);

            return formatter.toString();
        }
    }
    
    /**
     * Inject environment info into banner.
     *
     * @param formatter   the formatter
     * @param environment the environment
     * @param sourceClass the source class
     */
    protected void injectEnvironmentInfoIntoBanner(final Formatter formatter,
                                                   final Environment environment,
                                                   final Class<?> sourceClass) {
    }

    private static boolean isJceInstalled() {
        try {
            final int maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
            return maxKeyLen == Integer.MAX_VALUE;
        } catch (final Exception e) {
            return false;
        }
    }
}
