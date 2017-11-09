package org.apereo.cas.util.spring.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdurmont.semver4j.Semver;
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
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This is {@link AbstractCasBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractCasBanner implements Banner {
    /**
     * Line separator length.
     */
    private static final int SEPARATOR_REPEAT_COUNT = 60;
    private static final String UPDATE_CHECK_MAVEN_URL = "https://search.maven.org/solrsearch/select?q=g:%22org.apereo.cas%22%20AND%20a:%22cas-server%22";

    /**
     * A line separator.
     */
    public static final String LINE_SEPARATOR = String.join(StringUtils.EMPTY, Collections.nCopies(SEPARATOR_REPEAT_COUNT, "-"));

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

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
            formatter.format("CAS Version: %s%n", StringUtils.defaultString(CasVersion.getVersion(), "Not Available"));
            formatter.format("CAS Commit Id: %s%n", StringUtils.defaultString(CasVersion.getSpecificationVersion(), "Not Available"));
            formatter.format("CAS Build Date/Time: %s%n", CasVersion.getDateTime());
            formatter.format("Spring Boot Version: %s%n", SpringBootVersion.getVersion());
            formatter.format("%s%n", LINE_SEPARATOR);
            
            formatter.format("Java Home: %s%n", properties.get("java.home"));
            formatter.format("Java Vendor: %s%n", properties.get("java.vendor"));
            formatter.format("Java Version: %s%n", properties.get("java.version"));
            final Runtime runtime = Runtime.getRuntime();
            formatter.format("JVM Free Memory: %s%n", FileUtils.byteCountToDisplaySize(runtime.freeMemory()));
            formatter.format("JVM Maximum Memory: %s%n", FileUtils.byteCountToDisplaySize(runtime.maxMemory()));
            formatter.format("JVM Total Memory: %s%n", FileUtils.byteCountToDisplaySize(runtime.totalMemory()));
            formatter.format("JCE Installed: %s%n", StringUtils.capitalize(BooleanUtils.toStringYesNo(isJceInstalled())));
            formatter.format("%s%n", LINE_SEPARATOR);

            formatter.format("OS Architecture: %s%n", properties.get("os.arch"));
            formatter.format("OS Name: %s%n", properties.get("os.name"));
            formatter.format("OS Version: %s%n", properties.get("os.version"));
            formatter.format("OS Date/Time: %s%n", LocalDateTime.now());
            formatter.format("OS Temp Directory: %s%n", FileUtils.getTempDirectoryPath());

            formatter.format("%s%n", LINE_SEPARATOR);

            injectUpdateInfoIntoBannerIfNeeded(formatter);

            injectEnvironmentInfoIntoBanner(formatter, environment, sourceClass);

            return formatter.toString();
        }
    }

    private static void injectUpdateInfoIntoBannerIfNeeded(final Formatter formatter) {
        try {
            final Properties properties = System.getProperties();
            if (!properties.containsKey("CAS_UPDATE_CHECK_ENABLED")) {
                return;
            }
            
            final URL url = new URL(UPDATE_CHECK_MAVEN_URL);
            final Map results = MAPPER.readValue(url, Map.class);
            if (!results.containsKey("response")) {
                return;
            }
            final Map response = (Map) results.get("response");
            if (!response.containsKey("numFound") && (int) response.get("numFound") != 1) {
                return;
            }

            final List docs = (List) response.get("docs");
            if (docs.isEmpty()) {
                return;
            }

            final Map entry = (Map) docs.get(0);
            final String latestVersion = (String) entry.get("latestVersion");
            if (StringUtils.isNotBlank(latestVersion)) {
                final String currentVersion = CasVersion.getVersion();
                final Semver latestSem = new Semver(latestVersion);
                final Semver currentSem = new Semver(currentVersion);
                formatter.format("Update Available: %s [Latest Version: %s / Stable: %s]%n",
                        StringUtils.capitalize(BooleanUtils.toStringYesNo(currentSem.isLowerThan(latestSem))),
                        latestVersion,
                        StringUtils.capitalize(BooleanUtils.toStringYesNo(latestSem.isStable())));
                formatter.format("%s%n", LINE_SEPARATOR);
            }

        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
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
