package org.apereo.cas.configuration.api;

import org.apereo.cas.configuration.loader.CasConfigurationPropertiesLoader;
import org.apereo.cas.configuration.support.RelaxedPropertyNames;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ResourceLoader;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * This is {@link CasConfigurationPropertiesSourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface CasConfigurationPropertiesSourceLocator {

    /**
     * Implementation bean name for the property source locator.
     */
    String BOOTSTRAP_PROPERTY_LOCATOR_BEAN_NAME = "casCoreBootstrapPropertySourceLocator";

    Logger LOGGER = LoggerFactory.getLogger(CasConfigurationPropertiesSourceLocator.class);

    /**
     * Property name passed to the environment that indicates the path to the standalone configuration file.
     */
    String PROPERTY_CAS_STANDALONE_CONFIGURATION_FILE = "cas.standalone.configuration-file";

    /**
     * Property name passed to the environment that indicates the path to the standalone configuration directory.
     */
    String PROPERTY_CAS_STANDALONE_CONFIGURATION_DIRECTORY = "cas.standalone.configuration-directory";

    /**
     * Configuration directories for CAS, listed in order.
     */
    List<File> DEFAULT_CAS_CONFIG_DIRECTORIES = List.of(
        new File("/etc/cas/config"),
        new File("/opt/cas/config"),
        new File("/var/cas/config")
    );

    /**
     * Standalone configuration profile.
     */
    String PROFILE_STANDALONE = "standalone";

    /**
     * Native configuration profile.
     */
    String PROFILE_NATIVE = "native";

    /**
     * Embedded configuration profile.
     * This is mainly an alias for {@link #PROFILE_STANDALONE}
     * and functionality should be identical.
     */
    String PROFILE_EMBEDDED = "embedded";
    /**
     * None configuration profile
     * which will force CAS to ignore default CAS configuration directories.
     */
    String PROFILE_NONE = "none";

    /**
     * Locate property sources for CAS via the given environment and other resources.
     *
     * @param environment    the environment
     * @param resourceLoader the resource loader
     * @return the property source
     */
    Optional<PropertySource<?>> locate(Environment environment, ResourceLoader resourceLoader);

    /**
     * Gets standalone profile configuration directory.
     *
     * @param environment the environment
     * @return the standalone profile configuration directory
     */
    static File getStandaloneProfileConfigurationDirectory(final Environment environment) {
        if (environment.getActiveProfiles().length > 0
            && Arrays.stream(environment.getActiveProfiles()).allMatch(profile -> profile.equalsIgnoreCase(PROFILE_NONE))) {
            LOGGER.info("Standalone configuration directory processing is skipped for profile [{}]", PROFILE_NONE);
            return null;
        }

        val values = new LinkedHashSet<>(RelaxedPropertyNames.forCamelCase(PROPERTY_CAS_STANDALONE_CONFIGURATION_DIRECTORY).getValues());
        values.add(PROPERTY_CAS_STANDALONE_CONFIGURATION_DIRECTORY);
        val file = values
            .stream()
            .map(key -> environment.getProperty(key, File.class))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);

        if (file != null && file.exists()) {
            LOGGER.trace("Received standalone configuration directory [{}]", file);
            return file;
        }

        return DEFAULT_CAS_CONFIG_DIRECTORIES
            .stream()
            .filter(File::exists)
            .findFirst()
            .orElse(null);
    }

    /**
     * Gets standalone profile configuration file.
     *
     * @param environment the environment
     * @return the standalone profile configuration file
     */
    static File getStandaloneProfileConfigurationFile(final Environment environment) {
        val values = new LinkedHashSet<>(RelaxedPropertyNames.forCamelCase(PROPERTY_CAS_STANDALONE_CONFIGURATION_FILE).getValues());
        values.add(PROPERTY_CAS_STANDALONE_CONFIGURATION_FILE);

        return values
            .stream()
            .map(key -> environment.getProperty(key, File.class))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    /**
     * Gets application name.
     *
     * @param environment the environment
     * @return the application name
     */
    static String getApplicationName(final Environment environment) {
        return environment.getProperty("spring.application.name", "cas");
    }

    /**
     * Gets configuration name.
     *
     * @param environment the environment
     * @return the configuration name
     */
    static String getConfigurationName(final Environment environment) {
        return environment.getProperty("spring.config.name", "cas");
    }

    /**
     * Gets configuration properties loaders.
     *
     * @return the configuration properties loaders
     */
    static List<CasConfigurationPropertiesLoader> getConfigurationPropertiesLoaders() {
        return ServiceLoader.load(CasConfigurationPropertiesLoader.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .filter(Objects::nonNull)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .collect(Collectors.toList());
    }
}
