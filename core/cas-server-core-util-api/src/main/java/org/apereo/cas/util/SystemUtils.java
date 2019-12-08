package org.apereo.cas.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdurmont.semver4j.Semver;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.info.GitProperties;
import org.springframework.core.SpringVersion;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private static final int SYSTEM_INFO_DEFAULT_SIZE = 20;
    private static final String UPDATE_CHECK_MAVEN_URL = "https://search.maven.org/solrsearch/select?q=g:%22org.apereo.cas%22%20AND%20a:%22cas-server-core%22";

    private static final GitProperties GIT_PROPERTIES;

    static {
        var properties = new Properties();
        try {
            val resource = new ClassPathResource("git.properties");
            if (ResourceUtils.doesResourceExist(resource)) {
                val loaded = PropertiesLoaderUtils.loadProperties(resource);
                for (val key : loaded.stringPropertyNames()) {
                    if (key.startsWith("git.")) {
                        properties.put(key.substring("git.".length()), loaded.get(key));
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        } finally {
            GIT_PROPERTIES = new GitProperties(properties);
        }
    }

    /**
     * Gets system info.
     *
     * @return the system info
     */
    public static Map<String, Object> getSystemInfo() {
        val properties = System.getProperties();

        val info = new LinkedHashMap<String, Object>(SYSTEM_INFO_DEFAULT_SIZE);

        info.put("CAS Version", StringUtils.defaultString(CasVersion.getVersion(), "Not Available"));
        info.put("CAS Branch", StringUtils.defaultString(GIT_PROPERTIES.getBranch(), "Not Available"));
        info.put("CAS Commit Id", StringUtils.defaultString(GIT_PROPERTIES.getCommitId(), "Not Available"));
        info.put("CAS Build Date/Time", CasVersion.getDateTime());
        info.put("Spring Boot Version", SpringBootVersion.getVersion());
        info.put("Spring Version", SpringVersion.getVersion());

        info.put("Java Home", properties.get("java.home"));
        info.put("Java Vendor", properties.get("java.vendor"));
        info.put("Java Version", properties.get("java.version"));

        val runtime = Runtime.getRuntime();
        info.put("JVM Free Memory", FileUtils.byteCountToDisplaySize(runtime.freeMemory()));
        info.put("JVM Maximum Memory", FileUtils.byteCountToDisplaySize(runtime.maxMemory()));
        info.put("JVM Total Memory", FileUtils.byteCountToDisplaySize(runtime.totalMemory()));

        info.put("JCE Installed", StringUtils.capitalize(BooleanUtils.toStringYesNo(EncodingUtils.isJceInstalled())));

        info.put("OS Architecture", properties.get("os.arch"));
        info.put("OS Name", properties.get("os.name"));
        info.put("OS Version", properties.get("os.version"));
        info.put("OS Date/Time", LocalDateTime.now(ZoneId.systemDefault()));
        info.put("OS Temp Directory", FileUtils.getTempDirectoryPath());

        injectUpdateInfoIntoBannerIfNeeded(info);

        return info;
    }

    @SneakyThrows
    private static void injectUpdateInfoIntoBannerIfNeeded(final Map<String, Object> info) {
        val properties = System.getProperties();
        if (!properties.containsKey("CAS_UPDATE_CHECK_ENABLED")) {
            return;
        }

        val url = new URL(UPDATE_CHECK_MAVEN_URL);
        val results = MAPPER.readValue(url, Map.class);
        if (!results.containsKey("response")) {
            return;
        }
        val response = (Map) results.get("response");
        if (!response.containsKey("numFound") && (int) response.get("numFound") != 1) {
            return;
        }

        val docs = (List) response.get("docs");
        if (docs.isEmpty()) {
            return;
        }

        val entry = (Map) docs.get(0);
        val latestVersion = (String) entry.get("latestVersion");
        if (StringUtils.isNotBlank(latestVersion)) {
            val currentVersion = StringUtils.defaultString(CasVersion.getVersion(), GIT_PROPERTIES.get("build.version"));
            val latestSem = new Semver(latestVersion);
            val currentSem = new Semver(currentVersion);

            if (currentSem.isLowerThan(latestSem)) {
                val updateString = String.format("[Latest Version: %s / Stable: %s]", latestVersion,
                    StringUtils.capitalize(BooleanUtils.toStringYesNo(latestSem.isStable())));
                info.put("Update Availability", updateString);
            }
        }
    }
}
