package org.apereo.cas.configuration.loader;

import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * This is {@link ConfigurationPropertiesLoaderFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class ConfigurationPropertiesLoaderFactory {
    /**
     * Implementation bean name.
     */
    public static final String BEAN_NAME = "configurationPropertiesLoaderFactory";

    /**
     * The Configuration cipher executor.
     */
    private final CipherExecutor<String, String> configurationCipherExecutor;

    private final Environment environment;

    /**
     * Gets application profiles.
     *
     * @param environment the environment
     * @return the application profiles
     */
    public static List<String> getApplicationProfiles(final Environment environment) {
        return Arrays.stream(environment.getActiveProfiles()).collect(Collectors.toList());
    }

    /**
     * Gets loader based on the given resource.
     *
     * @param resource the resource
     * @param name     the name
     * @return the loader
     */
    public BaseConfigurationPropertiesLoader getLoader(final Resource resource,
                                                       final String name) {
        val filename = StringUtils.defaultString(resource.getFilename()).toLowerCase(Locale.ENGLISH);

        if (filename.endsWith(".properties")) {
            return new SimpleConfigurationPropertiesLoader(this.configurationCipherExecutor, name, resource);
        }
        if (filename.endsWith(".groovy") && CasRuntimeHintsRegistrar.notInNativeImage()) {
            return new GroovyConfigurationPropertiesLoader(this.configurationCipherExecutor, name,
                getApplicationProfiles(environment), resource);
        }
        if (filename.endsWith(".yaml") || filename.endsWith(".yml")) {
            return new YamlConfigurationPropertiesLoader(this.configurationCipherExecutor, name, resource);
        }
        throw new IllegalArgumentException("Unable to determine configuration loader for " + resource);
    }
}
