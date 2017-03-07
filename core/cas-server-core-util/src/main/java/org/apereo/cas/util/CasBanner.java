package org.apereo.cas.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

import javax.crypto.Cipher;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Formatter;
import java.util.Properties;

/**
 * This is {@link CasBanner}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class CasBanner implements Banner {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasBanner.class);

    @Override
    public void printBanner(final Environment environment, final Class<?> sourceClass, final PrintStream out) {
        AsciiArtUtils.printAsciiArt(out, "(CAS)", collectEnvironmentInfo());
    }

    /**
     * Collect environment info with
     * details on the java and os deployment
     * versions.
     *
     * @return environment info
     */
    private static String collectEnvironmentInfo() {
        final Properties properties = System.getProperties();
        try (Formatter formatter = new Formatter()) {
            formatter.format("CAS Version: %s%n", CasVersion.getVersion());

            formatApacheTomcatVersion(formatter);
            
            formatter.format("Build Date/Time: %s%n", CasVersion.getDateTime());
            formatter.format("System Temp Directory: %s%n", FileUtils.getTempDirectoryPath());
            formatter.format("Java Home: %s%n", properties.get("java.home"));
            formatter.format("Java Vendor: %s%n", properties.get("java.vendor"));
            formatter.format("Java Version: %s%n", properties.get("java.version"));
            formatter.format("JCE Installed: %s%n", BooleanUtils.toStringYesNo(isJceInstalled()));
            formatter.format("OS Architecture: %s%n", properties.get("os.arch"));
            formatter.format("OS Name: %s%n", properties.get("os.name"));
            formatter.format("OS Version: %s%n", properties.get("os.version"));
            return formatter.toString();
        }
    }

    private static void formatApacheTomcatVersion(final Formatter formatter) {
        try {
            final Class clz = Class.forName("org.apache.catalina.util.ServerInfo");
            final Method method = clz.getMethod("getServerInfo");
            final Object version = method.invoke(null);
            formatter.format("Apache Tomcat Version: %s%n", version);
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }
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
