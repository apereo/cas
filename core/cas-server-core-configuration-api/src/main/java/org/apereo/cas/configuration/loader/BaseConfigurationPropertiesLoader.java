package org.apereo.cas.configuration.loader;

import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.util.Map;
import java.util.Properties;

/**
 * This is {@link BaseConfigurationPropertiesLoader}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Getter
public abstract class BaseConfigurationPropertiesLoader {
    /**
     * The Configuration cipher executor.
     */
    private final CipherExecutor<String, String> configurationCipherExecutor;
    /**
     * The Name.
     */
    private final String name;

    /**
     * The resource containing the configuration settings.
     */
    private final Resource resource;

    /**
     * Decrypt properties map.
     *
     * @param properties the properties
     * @return the map
     */
    protected Map<String, Object> decryptProperties(final Map properties) {
        return configurationCipherExecutor.decode(properties, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    /**
     * Finalize properties property source.
     *
     * @param props the props
     * @return the property source
     */
    protected PropertySource finalizeProperties(final Map props) {
        return new MapPropertySource(getName(), props);
    }

    /**
     * Finalize properties property source.
     *
     * @param props the props
     * @return the property source
     */
    protected PropertySource finalizeProperties(final Properties props) {
        return new PropertiesPropertySource(getName(), props);
    }

    /**
     * Load property source.
     *
     * @return the property source
     */
    public abstract PropertySource load();
}
