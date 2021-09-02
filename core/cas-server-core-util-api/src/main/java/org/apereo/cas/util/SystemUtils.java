package org.apereo.cas.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.info.GitProperties;
import org.springframework.core.SpringVersion;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This is {@link SystemUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@UtilityClass
public class SystemUtils {
    private static final int SYSTEM_INFO_DEFAULT_SIZE = 20;

    private static final GitProperties GIT_PROPERTIES;

    static {
        GIT_PROPERTIES = new GitProperties(loadGitProperties());
    }

    @SneakyThrows
    private static Properties loadGitProperties() {
        var properties = new Properties();
        val resource = new ClassPathResource("git.properties");
        if (ResourceUtils.doesResourceExist(resource)) {
            val loaded = PropertiesLoaderUtils.loadProperties(resource);
            for (val key : loaded.stringPropertyNames()) {
                if (key.startsWith("git.")) {
                    properties.put(key.substring("git.".length()), loaded.get(key));
                }
            }
        }
        return properties;
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

        info.put("OS Architecture", properties.get("os.arch"));
        info.put("OS Name", properties.get("os.name"));
        info.put("OS Version", properties.get("os.version"));
        info.put("OS Date/Time", LocalDateTime.now(ZoneId.systemDefault()));
        info.put("OS Temp Directory", FileUtils.getTempDirectoryPath());

        return info;
    }
}
