package org.apereo.cas.configuration;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.Reader;
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
@AllArgsConstructor
public class DefaultCasConfigurationPropertiesSourceLocator implements CasConfigurationPropertiesSourceLocator {
    private final CipherExecutor<String, String> configurationCipherExecutor;
    private final CasConfigurationPropertiesEnvironmentManager casConfigurationPropertiesEnvironmentManager;

    @Override
    public PropertySource<?> locate(final Environment environment, final ResourceLoader resourceLoader) {
        final CompositePropertySource compositePropertySource = new CompositePropertySource("casCompositePropertySource");

        final PropertySource<?> sourceYaml = loadEmbeddedYamlOverriddenProperties(resourceLoader);
        compositePropertySource.addPropertySource(sourceYaml);

        final File config = casConfigurationPropertiesEnvironmentManager.getStandaloneProfileConfigurationDirectory();
        LOGGER.debug("Located CAS standalone configuration directory at [{}]", config);
        if (config.isDirectory() && config.exists()) {
            final PropertySource<?> sourceProfiles = loadSettingsByApplicationProfiles(environment, config);
            compositePropertySource.addPropertySource(sourceProfiles);
        } else {
            LOGGER.info("Configuration directory [{}] is not a directory or cannot be found at the specific path", config);
        }

        final File configFile = casConfigurationPropertiesEnvironmentManager.getStandaloneProfileConfigurationFile();
        if (configFile != null) {
            final PropertySource<?> sourceStandalone = loadSettingsFromStandaloneConfigFile(configFile);
            compositePropertySource.addFirstPropertySource(sourceStandalone);
        }
        return compositePropertySource;
    }

    private PropertySource<?> loadSettingsFromStandaloneConfigFile(final File configFile) {
        final Properties props = new Properties();

        try (Reader r = Files.newBufferedReader(configFile.toPath(), StandardCharsets.UTF_8)) {
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
        final Properties props = new Properties();

        final List<String> profiles = getApplicationProfiles(environment);
        final String regex = buildPatternForConfigurationFileDiscovery(config, profiles);
        final Collection<File> configFiles = scanForConfigurationFilesByPattern(config, regex);

        LOGGER.info("Configuration files found at [{}] are [{}]", config, configFiles);
        configFiles.forEach(Unchecked.consumer(f -> {
            LOGGER.debug("Loading configuration file [{}]", f);
            if (f.getName().toLowerCase().endsWith("yml")) {
                final Map<String, Object> pp = loadYamlProperties(new FileSystemResource(f));
                LOGGER.debug("Found settings [{}] in YAML file [{}]", pp.keySet(), f);
                props.putAll(decryptProperties(pp));
            } else {
                final Properties pp = new Properties();
                pp.load(Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8));
                LOGGER.debug("Found settings [{}] in file [{}]", pp.keySet(), f);
                props.putAll(decryptProperties(pp));
            }
        }));

        return new PropertiesPropertySource("applicationProfilesProperties", props);
    }

    private PropertySource<?> loadEmbeddedYamlOverriddenProperties(final ResourceLoader resourceLoader) {
        final Properties props = new Properties();
        final Resource resource = resourceLoader.getResource("classpath:/application.yml");
        if (resource != null && resource.exists()) {
            final Map pp = loadYamlProperties(resource);
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
        return this.configurationCipherExecutor.decode(properties);
    }

    private static String buildPatternForConfigurationFileDiscovery(final File config, final List<String> profiles) {
        final String propertyNames = profiles.stream().collect(Collectors.joining("|"));
        final String profiledProperties = profiles.stream()
            .map(p -> String.format("application-%s", p))
            .collect(Collectors.joining("|"));

        final String regex = String.format("(%s|%s|application)\\.(yml|properties)", propertyNames, profiledProperties);
        LOGGER.debug("Looking for configuration files at [{}] that match the pattern [{}]", config, regex);
        return regex;
    }

    private List<String> getApplicationProfiles(final Environment environment) {
        final List<String> profiles = new ArrayList<>();
        profiles.add(casConfigurationPropertiesEnvironmentManager.getApplicationName());
        profiles.addAll(Arrays.stream(environment.getActiveProfiles()).collect(Collectors.toList()));
        return profiles;
    }

    private static Map loadYamlProperties(final Resource... resource) {
        final YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResolutionMethod(YamlProcessor.ResolutionMethod.OVERRIDE);
        factory.setResources(resource);
        factory.setSingleton(true);
        factory.afterPropertiesSet();
        return factory.getObject();
    }
}
