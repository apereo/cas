package org.apereo.cas.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.jooq.lambda.Unchecked;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultCasConfigurationPropertiesSourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultCasConfigurationPropertiesSourceLocator implements CasConfigurationPropertiesSourceLocator {
    private final CipherExecutor<String, String> configurationCipherExecutor;
    private final CasConfigurationPropertiesEnvironmentManager casConfigurationPropertiesEnvironmentManager;

    @Override
    public PropertySource<?> locate(final Environment environment, final ResourceLoader resourceLoader) {
        final var compositePropertySource = new CompositePropertySource("casCompositePropertySource");

        final var configFile = casConfigurationPropertiesEnvironmentManager.getStandaloneProfileConfigurationFile();
        if (configFile != null) {
            final PropertySource<?> sourceStandalone = loadSettingsFromStandaloneConfigFile(configFile);
            compositePropertySource.addPropertySource(sourceStandalone);
        }

        final var config = casConfigurationPropertiesEnvironmentManager.getStandaloneProfileConfigurationDirectory();
        LOGGER.debug("Located CAS standalone configuration directory at [{}]", config);
        if (config.isDirectory() && config.exists()) {
            final PropertySource<?> sourceProfiles = loadSettingsByApplicationProfiles(environment, config);
            compositePropertySource.addPropertySource(sourceProfiles);
        } else {
            LOGGER.info("Configuration directory [{}] is not a directory or cannot be found at the specific path", config);
        }

        final PropertySource<?> sourceYaml = loadEmbeddedYamlOverriddenProperties(resourceLoader);
        compositePropertySource.addPropertySource(sourceYaml);

        return compositePropertySource;
    }

    private PropertySource<?> loadSettingsFromStandaloneConfigFile(final File configFile) {
        final var props = new Properties();

        try (var r = Files.newBufferedReader(configFile.toPath(), StandardCharsets.UTF_8)) {
            LOGGER.debug("Located CAS standalone configuration file at [{}]", configFile);
            props.load(r);
            LOGGER.debug("Found settings [{}] in file [{}]", props.keySet(), configFile);
            props.putAll(decryptProperties(props));
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }

        return new PropertiesPropertySource("standaloneConfigurationFileProperties", props);
    }

    private PropertySource<?> loadSettingsByApplicationProfiles(final Environment environment, final File config) {
        final var props = new Properties();

        final var profiles = getApplicationProfiles(environment);
        final var regex = buildPatternForConfigurationFileDiscovery(config, profiles);
        final var configFiles = scanForConfigurationFilesByPattern(config, regex);

        LOGGER.info("Configuration files found at [{}] are [{}] under profile(s) [{}]", config, configFiles, environment.getActiveProfiles());
        configFiles.forEach(Unchecked.consumer(f -> {
            LOGGER.debug("Loading configuration file [{}]", f);
            if (f.getName().toLowerCase().endsWith("yml")) {
                final Map<String, Object> pp = CasCoreConfigurationUtils.loadYamlProperties(new FileSystemResource(f));
                LOGGER.debug("Found settings [{}] in YAML file [{}]", pp.keySet(), f);
                props.putAll(decryptProperties(pp));
            } else {
                final var pp = new Properties();
                try (var reader = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
                    pp.load(reader);
                }
                LOGGER.debug("Found settings [{}] in file [{}]", pp.keySet(), f);
                props.putAll(decryptProperties(pp));
            }
        }));

        return new PropertiesPropertySource("applicationProfilesProperties", props);
    }

    private PropertySource<?> loadEmbeddedYamlOverriddenProperties(final ResourceLoader resourceLoader) {
        final var props = new Properties();
        final var resource = resourceLoader.getResource("classpath:/application.yml");
        if (resource != null && resource.exists()) {
            final var pp = CasCoreConfigurationUtils.loadYamlProperties(resource);
            if (pp.isEmpty()) {
                LOGGER.debug("No properties were located inside [{}]", resource);
            } else {
                LOGGER.info("Found settings [{}] in YAML file [{}]", pp.keySet(), resource);
                props.putAll(decryptProperties(pp));
            }
        }
        return new PropertiesPropertySource("embeddedYamlOverriddenProperties", props);
    }

    private static Collection<File> scanForConfigurationFilesByPattern(final File config, final String regex) {
        return FileUtils.listFiles(config, new RegexFileFilter(regex, IOCase.INSENSITIVE), TrueFileFilter.INSTANCE)
            .stream()
            .sorted(Comparator.comparing(File::getName))
            .collect(Collectors.toList());
    }

    private Map<String, Object> decryptProperties(final Map properties) {
        return this.configurationCipherExecutor.decode(properties, new Object[]{});
    }

    private static String buildPatternForConfigurationFileDiscovery(final File config, final List<String> profiles) {
        final var propertyNames = profiles.stream().collect(Collectors.joining("|"));
        final var profiledProperties = profiles.stream()
            .map(p -> String.format("application-%s", p))
            .collect(Collectors.joining("|"));

        final var regex = String.format("(%s|%s|application)\\.(yml|properties)", propertyNames, profiledProperties);
        LOGGER.debug("Looking for configuration files at [{}] that match the pattern [{}]", config, regex);
        return regex;
    }

    private List<String> getApplicationProfiles(final Environment environment) {
        final List<String> profiles = new ArrayList<>();
        profiles.add(casConfigurationPropertiesEnvironmentManager.getApplicationName());
        profiles.addAll(Arrays.stream(environment.getActiveProfiles()).collect(Collectors.toList()));
        return profiles;
    }


}
