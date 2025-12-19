package org.apereo.cas.configuration.loader;

import module java.base;
import org.apereo.cas.configuration.CasCoreConfigurationUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * This is {@link YamlConfigurationPropertiesLoader}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@NoArgsConstructor
public class YamlConfigurationPropertiesLoader extends BaseConfigurationPropertiesLoader {

    @Override
    public PropertySource load(final Resource resource,
                               final Environment environment,
                               final String name,
                               final CipherExecutor<String, String> configurationCipherExecutor) {
        val properties = new Properties();
        if (ResourceUtils.doesResourceExist(resource)) {
            try {
                val yamlProperties = CasCoreConfigurationUtils.loadYamlProperties(resource);
                if (yamlProperties.isEmpty()) {
                    LOGGER.debug("No properties were located inside [{}]", resource);
                } else {
                    LOGGER.info("Found settings [{}] in YAML file [{}]", yamlProperties.keySet(), resource);
                    properties.putAll(decryptProperties(configurationCipherExecutor, yamlProperties));
                }
            } catch (final YAMLException e) {
                LOGGER.warn("Error parsing yaml configuration in [{}]: [{}]", resource, e.getMessage());
                throw e;
            }
        }
        return finalizeProperties(name, properties);
    }

    @Override
    public boolean supports(final Resource resource) {
        val filename = StringUtils.defaultString(resource.getFilename()).toLowerCase(Locale.ENGLISH);
        return filename.endsWith(".yaml") || filename.endsWith(".yml");
    }
}
