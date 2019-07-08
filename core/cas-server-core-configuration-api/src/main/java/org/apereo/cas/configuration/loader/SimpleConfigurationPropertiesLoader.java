package org.apereo.cas.configuration.loader;

import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.util.Properties;

/**
 * This is {@link SimpleConfigurationPropertiesLoader}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class SimpleConfigurationPropertiesLoader extends BaseConfigurationPropertiesLoader {
    public SimpleConfigurationPropertiesLoader(final CipherExecutor<String, String> configurationCipherExecutor,
                                               final String name,
                                               final Resource resource) {
        super(configurationCipherExecutor, name, resource);
    }

    @Override
    public PropertySource load() {
        val props = new Properties();
        try (val is = getResource().getInputStream()) {
            LOGGER.debug("Located CAS standalone configuration file at [{}]", getResource());
            props.load(is);
            LOGGER.debug("Found settings [{}] in file [{}]", props.keySet(), getResource());
            props.putAll(decryptProperties(props));
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return finalizeProperties(props);
    }
}
