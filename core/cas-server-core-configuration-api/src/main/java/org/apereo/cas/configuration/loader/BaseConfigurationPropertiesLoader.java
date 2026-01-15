package org.apereo.cas.configuration.loader;

import module java.base;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

/**
 * This is {@link BaseConfigurationPropertiesLoader}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@NoArgsConstructor
public abstract class BaseConfigurationPropertiesLoader implements CasConfigurationPropertiesLoader {

    /**
     * Decrypt properties map.
     *
     * @param configurationCipherExecutor the configuration cipher executor
     * @param properties                  the properties
     * @return the map
     */
    protected Map<String, Object> decryptProperties(final CipherExecutor<String, String> configurationCipherExecutor,
                                                    final Map properties) {
        return configurationCipherExecutor.decode(properties, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    /**
     * Finalize properties property source.
     *
     * @param name       the name
     * @param properties the props
     * @return the property source
     */
    protected PropertySource finalizeProperties(final String name, final Map properties) {
        return new MapPropertySource(name, properties);
    }

    /**
     * Finalize properties property source.
     *
     * @param name  the name
     * @param props the props
     * @return the property source
     */
    protected PropertySource finalizeProperties(final String name, final Properties props) {
        return new PropertiesPropertySource(name, props);
    }

    /**
     * Gets application profiles.
     *
     * @param environment the environment
     * @return the application profiles
     */
    protected List<String> getApplicationProfiles(final Environment environment) {
        return Arrays.stream(environment.getActiveProfiles()).collect(Collectors.toList());
    }
}
