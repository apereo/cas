package org.apereo.cas.configuration;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
import java.util.Arrays;
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
    // The order of the elements in these constants is important, last one overrides previous ones
    private static final List<String> EXTENSIONS = Arrays.asList("properties", "yml", "yaml");
    private static final List<String> PROFILE_PATTERNS = Arrays.asList("application-%s.%s", "%s.%s");

    private final CipherExecutor<String, String> configurationCipherExecutor;
    private final CasConfigurationPropertiesEnvironmentManager casConfigurationPropertiesEnvironmentManager;

    /**
     * Make a list of files that will be processed in order where the last one processed wins.
     * Profiles are added after base property names like application.properties, cas.properties, CAS.properties so that
     * the profiles will override the base properites.
     * Profiles are processed in order so that the last profile list (e.g. in spring.active.profiles) will override the
     * the first profile.
     * Where multiple filenames with same base name and different extensions exist, the priority is yaml, yml, properties.
     */
    private List<File> getAllPossibleExternalConfigDirFilenames(final File configdir, final List<String> profiles) {

        val applicationName = casConfigurationPropertiesEnvironmentManager.getApplicationName();
        val fileNames = CollectionUtils.wrapList("application", applicationName.toLowerCase(), applicationName)
                .stream()
                .distinct()
                .flatMap(appName -> EXTENSIONS
                        .stream()
                        .map(ext -> new File(configdir, String.format("%s.%s", appName, ext))))
                .collect(Collectors.toList());

        fileNames.addAll(profiles
                .stream()
                .flatMap(profile -> EXTENSIONS
                        .stream()
                        .flatMap(ext -> PROFILE_PATTERNS
                              .stream().map(pattern -> new File(configdir, String.format(pattern, profile, ext))))).collect(Collectors.toList()));

        return fileNames;
    }

    /**
     * Get all possible configuration files for config directory that actually exist as files.
     * @param config Folder in which to look for files
     * @param profiles Profiles that are active
     * @return List of files to be processed in order where last one processed overrides others
     */
    private List<File> scanForConfigurationFiles(final File config, final List<String> profiles) {
        val possibleFiles = getAllPossibleExternalConfigDirFilenames(config, profiles);
        return possibleFiles.stream().filter(File::exists).filter(File::isFile).collect(Collectors.toList());
    }

    /**
     * Adding items to composite property source which contains property sources processed in order, first one wins.
     * First Priority: Standalone configuration file
     * Second Priority: Configuration files in config dir, profiles override non-profiles, last profile overrides first
     * Third Priority: classpath:/application.yml
     *
     * @param environment    the environment
     * @param resourceLoader the resource loader
     * @return CompositePropertySource containing sources listed above
     */
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
        val profiles = getApplicationProfiles(environment);
        val profileConfigFiles = scanForConfigurationFiles(config, profiles);

        return new PropertiesPropertySource("applicationProfilesProperties", getProperties(environment, config, profileConfigFiles));
    }

    /**
     * Property files processed in order of non-profiles first and then profiles, and profiles are first to last
     * with properties in the last profile overriding properties in previous profiles or non-profiles.
     * @param environment Spring environment
     * @param config Location of config files
     * @param configFiles List of all config files to load
     * @return Merged properties
     */
    private Properties getProperties(final Environment environment, final File config, final List<File> configFiles) {
        LOGGER.info("Configuration files found at [{}] are [{}] under profile(s) [{}]", config, configFiles, environment.getActiveProfiles());
        val props = new Properties();
        configFiles.forEach(Unchecked.consumer(f -> {
            LOGGER.debug("Loading configuration file [{}]", f);
            val fileName = f.getName().toLowerCase();
            if (fileName.endsWith("yml") || fileName.endsWith("yaml")) {
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
        return props;
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
        return Arrays.stream(environment.getActiveProfiles()).collect(Collectors.toList());
    }
}
