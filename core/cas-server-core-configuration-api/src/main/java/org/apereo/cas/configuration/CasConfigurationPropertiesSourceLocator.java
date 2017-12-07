package org.apereo.cas.configuration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apereo.cas.configuration.support.CasConfigurationJasyptCipherExecutor;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * This is {@link CasConfigurationPropertiesSourceLocator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class CasConfigurationPropertiesSourceLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasConfigurationPropertiesSourceLocator.class);

    /**
     * Locate property source.
     *
     * @param environment    the environment
     * @param resourceLoader the resource loader
     * @param manager        the configuration properties environment manager
     * @return the property source
     */
    public PropertySource<?> locate(final Environment environment, final ResourceLoader resourceLoader,
                                    final CasConfigurationPropertiesEnvironmentManager manager) {
        final CasConfigurationJasyptCipherExecutor configurationJasyptDecryptor = new CasConfigurationJasyptCipherExecutor(environment);
        final CompositePropertySource compositePropertySource = new CompositePropertySource("casCompositePropertySource");

        final PropertySource<?> sourceYaml = loadEmbeddedYamlOverriddenProperties(environment, resourceLoader, configurationJasyptDecryptor);
        compositePropertySource.addPropertySource(sourceYaml);

        final File config = manager.getStandaloneProfileConfigurationDirectory();
        LOGGER.debug("Located CAS standalone configuration directory at [{}]", config);
        if (config.isDirectory() && config.exists()) {
            final PropertySource<?> sourceProfiles = loadSettingsByApplicationProfiles(environment, config, manager, configurationJasyptDecryptor);
            compositePropertySource.addPropertySource(sourceProfiles);
        } else {
            LOGGER.info("Configuration directory [{}] is not a directory or cannot be found at the specific path", config);
        }

        final File configFile = manager.getStandaloneProfileConfigurationFile();
        if (configFile != null) {
            final PropertySource<?> sourceStandalone = loadSettingsFromStandaloneConfigFile(configFile, configurationJasyptDecryptor);
            compositePropertySource.addFirstPropertySource(sourceStandalone);
        }
        return compositePropertySource;
    }

    private PropertySource<?> loadSettingsFromStandaloneConfigFile(final File configFile,
                                                                   final CasConfigurationJasyptCipherExecutor jasypt) {
        final Properties props = new Properties();

        try (Reader r = Files.newBufferedReader(configFile.toPath(), StandardCharsets.UTF_8)) {
            LOGGER.debug("Located CAS standalone configuration file at [{}]", configFile);
            props.load(r);
            LOGGER.debug("Found settings [{}] in file [{}]", props.keySet(), configFile);
            props.putAll(decryptProperties(props, jasypt));
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }

        return new PropertiesPropertySource("standaloneConfigurationFileProperties", props);
    }

    private PropertySource<?> loadSettingsByApplicationProfiles(final Environment environment,
                                                                final File config, final CasConfigurationPropertiesEnvironmentManager manager,
                                                                final CasConfigurationJasyptCipherExecutor jasypt) {
        final Properties props = new Properties();

        final List<String> profiles = getApplicationProfiles(environment, manager);
        final String regex = buildPatternForConfigurationFileDiscovery(config, profiles);
        final Collection<File> configFiles = scanForConfigurationFilesByPattern(config, regex);

        LOGGER.info("Configuration files found at [{}] are [{}]", config, configFiles);
        configFiles.forEach(Unchecked.consumer(f -> {
            LOGGER.debug("Loading configuration file [{}]", f);
            if (f.getName().toLowerCase().endsWith("yml")) {
                final Map<String, Object> pp = loadYamlProperties(new FileSystemResource(f));
                LOGGER.debug("Found settings [{}] in YAML file [{}]", pp.keySet(), f);
                props.putAll(decryptProperties(pp, jasypt));
            } else {
                final Properties pp = new Properties();
                pp.load(Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8));
                LOGGER.debug("Found settings [{}] in file [{}]", pp.keySet(), f);
                props.putAll(decryptProperties(pp, jasypt));
            }
        }));

        return new PropertiesPropertySource("applicationProfilesProperties", props);
    }

    private PropertySource<?> loadEmbeddedYamlOverriddenProperties(final Environment environment,
                                                                   final ResourceLoader resourceLoader,
                                                                   final CasConfigurationJasyptCipherExecutor configurationJasyptDecryptor) {
        final Properties props = new Properties();
        final Resource resource = resourceLoader.getResource("classpath:/application.yml");
        if (resource != null && resource.exists()) {
            final Map pp = loadYamlProperties(resource);
            if (pp.isEmpty()) {
                LOGGER.debug("No properties were located inside [{}]", resource);
            } else {
                LOGGER.info("Found settings [{}] in YAML file [{}]", pp.keySet(), resource);
                props.putAll(decryptProperties(pp, configurationJasyptDecryptor));
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

    private Map<String, Object> decryptProperties(final Map properties, final CasConfigurationJasyptCipherExecutor jasypt) {
        return jasypt.decrypt(properties);
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

    private List<String> getApplicationProfiles(final Environment environment,
                                                final CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager) {
        final List<String> profiles = new ArrayList<>();
        profiles.add(configurationPropertiesEnvironmentManager.getApplicationName());
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
