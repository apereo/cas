package org.apereo.cas.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdurmont.semver4j.Semver;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

    private static final String UPDATE_CHECK_MAVEN_URL = "https://search.maven.org/solrsearch/select?q=g:%22org.apereo.cas%22%20AND%20a:%22cas-server%22";

    /**
     * Gets system info.
     *
     * @return the system info
     */
    public static Map<String, Object> getSystemInfo() {
        final Properties properties = System.getProperties();

        final Map<String, Object> info = new LinkedHashMap<>();
        info.put("CAS Version", StringUtils.defaultString(CasVersion.getVersion(), "Not Available"));
        info.put("CAS Commit Id", StringUtils.defaultString(CasVersion.getSpecificationVersion(), "Not Available"));
        info.put("CAS Build Date/Time", CasVersion.getDateTime());
        info.put("Spring Boot Version", SpringBootVersion.getVersion());
        info.put("Spring Version", SpringVersion.getVersion());

        info.put("Java Home", properties.get("java.home"));
        info.put("Java Vendor", properties.get("java.vendor"));
        info.put("Java Version", properties.get("java.version"));

        final Runtime runtime = Runtime.getRuntime();
        info.put("JVM Free Memory", FileUtils.byteCountToDisplaySize(runtime.freeMemory()));
        info.put("JVM Maximum Memory", FileUtils.byteCountToDisplaySize(runtime.maxMemory()));
        info.put("JVM Total Memory", FileUtils.byteCountToDisplaySize(runtime.totalMemory()));

        info.put("JCE Installed", StringUtils.capitalize(BooleanUtils.toStringYesNo(EncodingUtils.isJceInstalled())));
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

            if (currentSem.isLowerThan(latestSem)) {
                final String updateString = String.format("[Latest Version: %s / Stable: %s]", latestVersion,
                    StringUtils.capitalize(BooleanUtils.toStringYesNo(latestSem.isStable())));
                info.put("Update Availability", updateString);
            }
        }
    }

}
