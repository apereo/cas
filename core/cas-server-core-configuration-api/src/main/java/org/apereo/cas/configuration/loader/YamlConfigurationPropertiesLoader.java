package org.apereo.cas.configuration.loader;

import org.apereo.cas.configuration.CasCoreConfigurationUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.util.Properties;

/**
 * This is {@link YamlConfigurationPropertiesLoader}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class YamlConfigurationPropertiesLoader extends BaseConfigurationPropertiesLoader {
    public YamlConfigurationPropertiesLoader(final CipherExecutor<String, String> configurationCipherExecutor,
                                             final String name, final Resource resource) {
        super(configurationCipherExecutor, name, resource);
    }

    @Override
    public PropertySource load() {
        val props = new Properties();
        if (ResourceUtils.doesResourceExist(getResource())) {
            val pp = CasCoreConfigurationUtils.loadYamlProperties(getResource());
            if (pp.isEmpty()) {
                LOGGER.debug("No properties were located inside [{}]", getResource());
            } else {
                LOGGER.info("Found settings [{}] in YAML file [{}]", pp.keySet(), getResource());
                props.putAll(decryptProperties(pp));
            }
        }
        return finalizeProperties(props);
    }
}
