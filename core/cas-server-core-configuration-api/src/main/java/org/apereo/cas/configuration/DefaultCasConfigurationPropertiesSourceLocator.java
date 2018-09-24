package org.apereo.cas.configuration;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
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

    private static Collection<File> scanForConfigurationFilesByPattern(final File config, final String regex) {
        return FileUtils.listFiles(config, new RegexFileFilter(regex, IOCase.INSENSITIVE), TrueFileFilter.INSTANCE)
            .stream()
            .sorted(Comparator.comparing(File::getName))
            .collect(Collectors.toList());
    }

    private static String buildPatternForConfigurationFileDiscovery(final File config, final List<String> profiles) {
        val propertyNames = String.join("|", profiles);
        val profiledProperties = profiles.stream()
            .map(p -> String.format("application-%s", p))
            .collect(Collectors.joining("|"));

        val regex = String.format("(%s|%s|application)\\.(yml|properties)", propertyNames, profiledProperties);
        LOGGER.debug("Looking for configuration files at [{}] that match the pattern [{}]", config, regex);
        return regex;
    }

    @Override
    public PropertySource<?> locate(final Environment environment, final ResourceLoader resourceLoader) {
        val compositePropertySource = new CompositePropertySource("casCompositePropertySource");

        val configFile = casConfigurationPropertiesEnvironmentManager.getStandaloneProfileConfigurationFile();
        if (configFile != null) {
            val sourceStandalone = loadSettingsFromStandaloneConfigFile(configFile);
            compositePropertySource.addPropertySource(sourceStandalone);
        }

        val config = casConfigurationPropertiesEnvironmentManager.getStandaloneProfileConfigurationDirectory();
        LOGGER.debug("Located CAS standalone configuration directory at [{}]", config);
        if (config.isDirectory() && config.exists()) {
            val sourceProfiles = loadSettingsByApplicationProfiles(environment, config);
            compositePropertySource.addPropertySource(sourceProfiles);
        } else {
            LOGGER.info("Configuration directory [{}] is not a directory or cannot be found at the specific path", config);
        }

        val sourceYaml = loadEmbeddedYamlOverriddenProperties(resourceLoader);
        compositePropertySource.addPropertySource(sourceYaml);

        return compositePropertySource;
    }

    private PropertySource<?> loadSettingsFromStandaloneConfigFile(final File configFile) {
        val props = new Properties();

        try (val r = Files.newBufferedReader(configFile.toPath(), StandardCharsets.UTF_8)) {
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
        val props = new Properties();

        val profiles = getApplicationProfiles(environment);
        val regex = buildPatternForConfigurationFileDiscovery(config, profiles);
        val configFiles = scanForConfigurationFilesByPattern(config, regex);

        LOGGER.info("Configuration files found at [{}] are [{}] under profile(s) [{}]", config, configFiles, environment.getActiveProfiles());
        configFiles.forEach(Unchecked.consumer(f -> {
            LOGGER.debug("Loading configuration file [{}]", f);
            if (f.getName().toLowerCase().endsWith("yml")) {
                val pp = CasCoreConfigurationUtils.loadYamlProperties(new FileSystemResource(f));
                LOGGER.debug("Found settings [{}] in YAML file [{}]", pp.keySet(), f);
                props.putAll(decryptProperties(pp));
            } else {
                val pp = new Properties();
                try (val reader = Files.newBufferedReader(f.toPath(), StandardCharsets.UTF_8)) {
                    pp.load(reader);
                }
                LOGGER.debug("Found settings [{}] in file [{}]", pp.keySet(), f);
                props.putAll(decryptProperties(pp));
            }
        }));

        return new PropertiesPropertySource("applicationProfilesProperties", props);
    }

    private PropertySource<?> loadEmbeddedYamlOverriddenProperties(final ResourceLoader resourceLoader) {
        val props = new Properties();
        val resource = resourceLoader.getResource("classpath:/application.yml");
        if (resource != null && resource.exists()) {
            val pp = CasCoreConfigurationUtils.loadYamlProperties(resource);
            if (pp.isEmpty()) {
                LOGGER.debug("No properties were located inside [{}]", resource);
            } else {
                LOGGER.info("Found settings [{}] in YAML file [{}]", pp.keySet(), resource);
                props.putAll(decryptProperties(pp));
            }
        }
        return new PropertiesPropertySource("embeddedYamlOverriddenProperties", props);
    }

    private Map<String, Object> decryptProperties(final Map properties) {
        return this.configurationCipherExecutor.decode(properties, new Object[]{});
    }

    private List<String> getApplicationProfiles(final Environment environment) {
        val profiles = new ArrayList<String>();
        profiles.add(casConfigurationPropertiesEnvironmentManager.getApplicationName());
        profiles.addAll(Arrays.stream(environment.getActiveProfiles()).collect(Collectors.toList()));
        return profiles;
    }


}
