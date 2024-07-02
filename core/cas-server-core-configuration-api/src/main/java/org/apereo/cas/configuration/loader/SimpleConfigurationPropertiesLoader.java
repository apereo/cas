package org.apereo.cas.configuration.loader;

import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import java.util.Locale;
import java.util.Properties;

/**
 * This is {@link SimpleConfigurationPropertiesLoader}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@NoArgsConstructor
public class SimpleConfigurationPropertiesLoader extends BaseConfigurationPropertiesLoader {

    @Override
    public PropertySource load(final Resource resource,
                               final Environment environment,
                               final String name,
                               final CipherExecutor<String, String> configurationCipherExecutor) {
        val props = new Properties();
        try (val is = resource.getInputStream()) {
            LOGGER.debug("Located CAS configuration file at [{}]", resource);
            props.load(is);
            LOGGER.debug("Found settings [{}] in file [{}]", props.keySet(), resource);
            props.putAll(decryptProperties(configurationCipherExecutor, props));
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
        }
        return finalizeProperties(name, props);
    }

    @Override
    public boolean supports(final Resource resource) {
        val filename = StringUtils.defaultString(resource.getFilename()).toLowerCase(Locale.ENGLISH);
        return filename.endsWith(".properties");
    }
}
