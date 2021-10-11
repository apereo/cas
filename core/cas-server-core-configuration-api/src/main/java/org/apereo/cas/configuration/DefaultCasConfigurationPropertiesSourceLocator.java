package org.apereo.cas.configuration;

import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.configuration.loader.ConfigurationPropertiesLoaderFactory;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultCasConfigurationPropertiesSourceLocator}.
 * <p>
 * Note: The order of the elements in {@link #EXTENSIONS} is important, last one overrides previous ones.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultCasConfigurationPropertiesSourceLocator implements CasConfigurationPropertiesSourceLocator {
    private static final List<String> EXTENSIONS = Arrays.asList("properties", "yml", "yaml");

    private static final List<String> PROFILE_PATTERNS = Arrays.asList("application-%s.%s", "%s.%s");

    private final CasConfigurationPropertiesEnvironmentManager casConfigurationPropertiesEnvironmentManager;

    private final ConfigurationPropertiesLoaderFactory configurationPropertiesLoaderFactory;


    /**
     * Adding items to composite property source which contains property sources processed in order, first one wins.
     * First Priority: Configuration files in config dir, profiles override non-profiles, last profile overrides first
     * Second Priority: classpath:/application.yml
     *
     * @param environment    the environment
     * @param resourceLoader the resource loader
     * @return CompositePropertySource containing sources listed above
     */
    @Override
    public Optional<PropertySource<?>> locate(final Environment environment, final ResourceLoader resourceLoader) {
        val compositePropertySource = new CompositePropertySource("casCompositePropertySource");
        val config = casConfigurationPropertiesEnvironmentManager.getStandaloneProfileConfigurationDirectory();
        LOGGER.debug("Located CAS standalone configuration directory at [{}]", config);
        if (config != null && config.isDirectory() && config.exists()) {
            val sourceProfiles = loadSettingsByApplicationProfiles(environment, config);
            compositePropertySource.addPropertySource(sourceProfiles);
        } else {
            LOGGER.info("Configuration directory [{}] is not a directory or cannot be found at the specific path", config);
        }

        val sourceYaml = loadEmbeddedYamlOverriddenProperties(resourceLoader, environment);
        compositePropertySource.addPropertySource(sourceYaml);

        return Optional.of(compositePropertySource);
    }


    /**
     * Make a list of files that will be processed in order where the last one processed wins.
     * Profiles are added after base property names like application.properties, cas.properties, CAS.properties so that
     * the profiles will override the base properties.
     * Profiles are processed in order so that the last profile list (e.g. in spring.active.profiles) will override the
     * the first profile.
     * Where multiple filenames with same base name and different extensions exist, the priority is yaml, yml, properties.
     */
    private List<File> getAllPossibleExternalConfigDirFilenames(final File configDirectory, final List<String> profiles) {
        val applicationName = casConfigurationPropertiesEnvironmentManager.getApplicationName();
        val appNameLowerCase = applicationName.toLowerCase();
        val fileNames = CollectionUtils.wrapList("application", appNameLowerCase, applicationName)
            .stream()
            .distinct()
            .flatMap(appName -> EXTENSIONS
                .stream()
                .map(ext -> new File(configDirectory, String.format("%s.%s", appName, ext))))
            .collect(Collectors.toList());

        fileNames.addAll(profiles
            .stream()
            .flatMap(profile -> EXTENSIONS
                .stream()
                .flatMap(ext -> PROFILE_PATTERNS
                    .stream().map(pattern -> new File(configDirectory, String.format(pattern, profile, ext))))).collect(Collectors.toList()));

        fileNames.add(new File(configDirectory, appNameLowerCase.concat(".groovy")));
        return fileNames;
    }

    /**
     * Get all possible configuration files for config directory that actually exist as files.
     *
     * @param config   Folder in which to look for files
     * @param profiles Profiles that are active
     * @return List of files to be processed in order where last one processed overrides others
     */
    private List<Resource> scanForConfigurationResources(final File config, final List<String> profiles) {
        val possibleFiles = getAllPossibleExternalConfigDirFilenames(config, profiles);
        return possibleFiles.stream()
            .filter(File::exists)
            .filter(File::isFile)
            .map(FileSystemResource::new)
            .collect(Collectors.toList());
    }

    /**
     * Property files processed in order of non-profiles first and then profiles, and profiles are first to last
     * with properties in the last profile overriding properties in previous profiles or non-profiles.
     *
     * @param environment Spring environment
     * @param config      Location of config files
     * @return Merged properties
     */
    private PropertySource<?> loadSettingsByApplicationProfiles(final Environment environment, final File config) {
        val profiles = ConfigurationPropertiesLoaderFactory.getApplicationProfiles(environment);
        val resources = scanForConfigurationResources(config, profiles);
        val composite = new CompositePropertySource("applicationProfilesCompositeProperties");
        LOGGER.info("Configuration files found at [{}] are [{}] under profile(s) [{}]", config, resources, profiles);
        resources.forEach(Unchecked.consumer(f -> {
            LOGGER.debug("Loading configuration file [{}]", f);
            val loader = configurationPropertiesLoaderFactory.getLoader(f, "applicationProfilesProperties-" + f.getFilename());
            composite.addFirstPropertySource(loader.load());
        }));

        return composite;
    }

    private PropertySource<?> loadEmbeddedYamlOverriddenProperties(final ResourceLoader resourceLoader,
                                                                   final Environment environment) {
        val profiles = ConfigurationPropertiesLoaderFactory.getApplicationProfiles(environment);
        val yamlFiles = profiles.stream()
            .map(profile -> String.format("classpath:/application-%s.yml", profile))
            .sorted()
            .map(resourceLoader::getResource)
            .collect(Collectors.toList());
        yamlFiles.add(resourceLoader.getResource("classpath:/application.yml"));

        LOGGER.debug("Loading embedded YAML configuration files [{}]", yamlFiles);

        val composite = new CompositePropertySource("embeddedYamlOverriddenCompositeProperties");
        yamlFiles.forEach(resource -> {
            LOGGER.trace("Loading YAML properties from [{}]", resource);
            val source = configurationPropertiesLoaderFactory.getLoader(resource,
                String.format("embeddedYamlOverriddenProperties-%s", resource.getFilename())).load();
            composite.addPropertySource(source);
        });
        return composite;
    }
}
