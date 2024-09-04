package org.apereo.cas.configuration;

import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class StandaloneConfigurationFilePropertiesSourceLocator implements CasConfigurationPropertiesSourceLocator {
    private final CipherExecutor<String, String> casConfigurationCipherExecutor;

    @Override
    public Optional<PropertySource<?>> locate(final Environment environment, final ResourceLoader resourceLoader) {
        val compositePropertySource = new CompositePropertySource(getClass().getSimpleName());
        val configFile = CasConfigurationPropertiesSourceLocator.getStandaloneProfileConfigurationFile(environment);
        if (configFile != null) {
            LOGGER.info("Loading standalone configuration properties from [{}]", configFile);
            val sourceStandalone = loadSettingsFromStandaloneConfigFile(environment, configFile);
            compositePropertySource.addPropertySource(sourceStandalone);
            return Optional.of(compositePropertySource);
        }
        LOGGER.info("No standalone configuration properties are available");
        return Optional.empty();
    }

    private PropertySource<Map<String, Object>> loadSettingsFromStandaloneConfigFile(
        final Environment environment, final File configFile) {
        val configurationLoaders = CasConfigurationPropertiesSourceLocator.getConfigurationPropertiesLoaders();
        val resource = new FileSystemResource(configFile);
        val foundLoader = configurationLoaders
            .stream()
            .filter(loader -> loader.supports(resource))
            .findFirst()
            .orElseThrow();

        return foundLoader.load(resource, environment, "standaloneConfigurationFileProperties", casConfigurationCipherExecutor);
    }
}
