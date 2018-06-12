package org.apereo.cas.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdurmont.semver4j.Semver;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link SystemUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@UtilityClass
public class SystemUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final String SEPARATOR_CHAR = "-";

    private static final int SYSTEM_INFO_DEFAULT_SIZE = 20;
    private static final String UPDATE_CHECK_MAVEN_URL = "https://search.maven.org/solrsearch/select?q=g:%22org.apereo.cas%22%20AND%20a:%22cas-server%22";

    /**
     * Gets system info.
     *
     * @return the system info
     */
    public static Map<String, Object> getSystemInfo() {
        final var properties = System.getProperties();

        final Map<String, Object> info = new LinkedHashMap<>(SYSTEM_INFO_DEFAULT_SIZE);

        info.put("CAS Version", StringUtils.defaultString(CasVersion.getVersion(), "Not Available"));
        info.put("CAS Commit Id", StringUtils.defaultString(CasVersion.getSpecificationVersion(), "Not Available"));
        info.put("CAS Build Date/Time", CasVersion.getDateTime());
        info.put("Spring Boot Version", SpringBootVersion.getVersion());
        info.put("Spring Version", SpringVersion.getVersion());

        info.put("Java Home", properties.get("java.home"));
        info.put("Java Vendor", properties.get("java.vendor"));
        info.put("Java Version", properties.get("java.version"));

        final var runtime = Runtime.getRuntime();
        info.put("JVM Free Memory", FileUtils.byteCountToDisplaySize(runtime.freeMemory()));
        info.put("JVM Maximum Memory", FileUtils.byteCountToDisplaySize(runtime.maxMemory()));
        info.put("JVM Total Memory", FileUtils.byteCountToDisplaySize(runtime.totalMemory()));

        info.put("JCE Installed", StringUtils.capitalize(BooleanUtils.toStringYesNo(EncodingUtils.isJceInstalled())));

        info.put("Node Version", getNodeVersion());
        info.put("NPM Version", getNpmVersion());

        info.put("OS Architecture", properties.get("os.arch"));
        info.put("OS Name", properties.get("os.name"));
        info.put("OS Version", properties.get("os.version"));
        info.put("OS Date/Time", LocalDateTime.now());
        info.put("OS Temp Directory", FileUtils.getTempDirectoryPath());

        injectUpdateInfoIntoBannerIfNeeded(info);

        return info;
    }

    @SneakyThrows
    private static void injectUpdateInfoIntoBannerIfNeeded(final Map<String, Object> info) {
        final var properties = System.getProperties();
        if (!properties.containsKey("CAS_UPDATE_CHECK_ENABLED")) {
            return;
        }

        final var url = new URL(UPDATE_CHECK_MAVEN_URL);
        final var results = MAPPER.readValue(url, Map.class);
        if (!results.containsKey("response")) {
            return;
        }
        final var response = (Map) results.get("response");
        if (!response.containsKey("numFound") && (int) response.get("numFound") != 1) {
            return;
        }

        final var docs = (List) response.get("docs");
        if (docs.isEmpty()) {
            return;
        }

        final var entry = (Map) docs.get(0);
        final var latestVersion = (String) entry.get("latestVersion");
        if (StringUtils.isNotBlank(latestVersion)) {
            final var currentVersion = CasVersion.getVersion();
            final var latestSem = new Semver(latestVersion);
            final var currentSem = new Semver(currentVersion);

            if (currentSem.isLowerThan(latestSem)) {
                final var updateString = String.format("[Latest Version: %s / Stable: %s]", latestVersion,
                    StringUtils.capitalize(BooleanUtils.toStringYesNo(latestSem.isStable())));
                info.put("Update Availability", updateString);
            }
        }
    }

    /**
     * Gets node version.
     *
     * @return the node version
     */
    @SneakyThrows
    public static String getNodeVersion() {
        try {
            final var pb = new ProcessBuilder("node", "--version");
            final var p = pb.start();
            return IOUtils.toString(p.getInputStream(), StandardCharsets.UTF_8).trim();
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return "N/A";
    }

    /**
     * Gets npm version.
     *
     * @return the npm version
     */
    @SneakyThrows
    public static String getNpmVersion() {
        try {
            final var pb = new ProcessBuilder("npm", "--version");
            final var p = pb.start();
            return IOUtils.toString(p.getInputStream(), StandardCharsets.UTF_8).trim();
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return "N/A";
    }
}
