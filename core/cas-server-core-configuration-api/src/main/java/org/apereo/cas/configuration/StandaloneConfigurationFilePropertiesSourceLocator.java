package org.apereo.cas.configuration;

import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.configuration.loader.ConfigurationPropertiesLoaderFactory;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link StandaloneConfigurationFilePropertiesSourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StandaloneConfigurationFilePropertiesSourceLocator implements CasConfigurationPropertiesSourceLocator {
    private final CasConfigurationPropertiesEnvironmentManager casConfigurationPropertiesEnvironmentManager;

    private final ConfigurationPropertiesLoaderFactory configurationPropertiesLoaderFactory;

    @Override
    public Optional<PropertySource<?>> locate(final Environment environment, final ResourceLoader resourceLoader) {
        val compositePropertySource = new CompositePropertySource(getClass().getSimpleName());
        val configFile = casConfigurationPropertiesEnvironmentManager.getStandaloneProfileConfigurationFile();
        if (configFile != null) {
            val sourceStandalone = loadSettingsFromStandaloneConfigFile(configFile);
            compositePropertySource.addPropertySource(sourceStandalone);
            return Optional.of(compositePropertySource);
        }
        return Optional.empty();
    }

    private PropertySource<Map<String, Object>> loadSettingsFromStandaloneConfigFile(final File configFile) {
        return configurationPropertiesLoaderFactory
            .getLoader(new FileSystemResource(configFile), "standaloneConfigurationFileProperties")
            .load();
    }
}
