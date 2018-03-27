package org.apereo.cas.util.spring.boot;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.AsciiArtUtils;
import org.apereo.cas.util.CasVersion;
import org.apereo.cas.util.SystemUtils;
import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Formatter;
import java.util.Map;
import java.util.Properties;

/**
 * This is {@link AbstractCasBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public abstract class AbstractCasBanner implements Banner {

    private static final int SEPARATOR_REPEAT_COUNT = 60;
    /**
     * Line separator string.
     */
    protected static final String LINE_SEPARATOR = String.join(StringUtils.EMPTY, Collections.nCopies(SEPARATOR_REPEAT_COUNT, "-"));

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
            final Map<String, Object> sysInfo = SystemUtils.getSystemInfo();
            sysInfo.forEach((k, v) -> formatter.format("%s: %s%n", k, v));
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
}
