package org.apereo.cas.util;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.info.GitProperties;
import org.springframework.core.SpringVersion;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import jakarta.servlet.http.HttpServlet;

/**
 * This is {@link SystemUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@UtilityClass
public class SystemUtils {
    private static final GitProperties GIT_PROPERTIES;

    static {
        GIT_PROPERTIES = new GitProperties(loadGitProperties());
    }

    private static Properties loadGitProperties() {
        var properties = new Properties();
        FunctionUtils.doUnchecked(_ -> {
            val resource = new ClassPathResource("git.properties");
            if (ResourceUtils.doesResourceExist(resource)) {
                val loaded = PropertiesLoaderUtils.loadProperties(resource);
                for (val key : loaded.stringPropertyNames()) {
                    if (key.startsWith("git.")) {
                        properties.put(key.substring("git.".length()), loaded.get(key));
                    }
                }
            }
        });
        return properties;
    }

    /**
     * Gets system info.
     *
     * @return the system info
     */
    @SuppressWarnings("JavaTimeDefaultTimeZone")
    public static Map<String, Object> getSystemInfo() {
        val properties = System.getProperties();

        val info = new LinkedHashMap<String, Object>();

        FunctionUtils.doIfNotNull(CasVersion.getVersion(), t -> info.put("CAS Version", t));
        info.put("CAS Branch", StringUtils.defaultIfBlank(GIT_PROPERTIES.getBranch(), "master"));
        FunctionUtils.doIfNotNull(GIT_PROPERTIES.getCommitId(), t -> info.put("CAS Commit Id", t));
        FunctionUtils.doIfNotNull(CasVersion.getDateTime(), t -> info.put("CAS Build Date/Time", t));

        info.put("Spring Boot Version", SpringBootVersion.getVersion());
        info.put("Spring Version", SpringVersion.getVersion());

        FunctionUtils.doIfNotNull(properties.get("java.home"), t -> info.put("Java Home", t));
        info.put("Java Vendor", properties.get("java.vendor"));
        info.put("Java Version", properties.get("java.version"));
        info.put("Servlet Version", HttpServlet.class.getPackage().getImplementationVersion());

        val runtime = Runtime.getRuntime();
        info.put("JVM Free Memory", FileUtils.byteCountToDisplaySize(runtime.freeMemory()));
        info.put("JVM Maximum Memory", FileUtils.byteCountToDisplaySize(runtime.maxMemory()));
        info.put("JVM Total Memory", FileUtils.byteCountToDisplaySize(runtime.totalMemory()));
                               
        info.put("OS Architecture", properties.get("os.arch"));
        info.put("OS Name", properties.get("os.name"));
        info.put("OS Version", properties.get("os.version"));
        info.put("OS Date/Time", LocalDateTime.now(ZoneId.systemDefault()));
        info.put("OS Timezone", Clock.systemDefaultZone().toString());
        info.put("OS Temp Directory", FileUtils.getTempDirectoryPath());

        return info;
    }
}
