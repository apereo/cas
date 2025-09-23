package org.apereo.cas.configuration;

import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultCasConfigurationPropertiesSourceLocator}.
 * <p>
 * Note: The order of the elements in {@link #EXTENSIONS} is important, first one overrides previous ones.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultCasConfigurationPropertiesSourceLocator implements CasConfigurationPropertiesSourceLocator {
    private static final List<String> EXTENSIONS = List.of("yml", "yaml", "properties");

    private static final List<String> PROFILE_PATTERNS = List.of("application-%s.%s", "%s.%s");

    private final CipherExecutor<String, String> casConfigurationCipherExecutor;

    /**
     * Returns a property source composed of system properties and environment variables.
     * <p>
     * System properties take precedence over environment variables (similarly to spring boot behaviour).
     */
    private static PropertySource<?> loadEnvironmentAndSystemProperties() {
        val source = new CompositePropertySource("environmentAndSystemProperties");
        source.addPropertySource(new PropertiesPropertySource(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, System.getProperties()));
        source.addPropertySource(new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, (Map) System.getenv()));
        return source;
    }

    /**
     * Adding items to composite property source which contains
     * property sources processed in order, first one wins.
     * First Priority: System properties and environment variables,
     * Second Priority: Configuration files in config dir,
     * profiles override non-profiles, last profile overrides first
     * Third Priority: {@code classpath:/application.yml}
     *
     * @param environment    the environment
     * @param resourceLoader the resource loader
     * @return CompositePropertySource containing sources listed above
     */
    @Override
    public Optional<PropertySource<?>> locate(final Environment environment, final ResourceLoader resourceLoader) {
        val compositePropertySource = new CompositePropertySource("casCompositePropertySource");
        compositePropertySource.addPropertySource(loadEnvironmentAndSystemProperties());
        val config = CasConfigurationPropertiesSourceLocator.getStandaloneProfileConfigurationDirectory(environment);
        LOGGER.debug("Located CAS standalone configuration directory at [{}]", config);
        if (config != null && config.isDirectory() && config.exists()) {
            val sourceProfiles = loadSettingsByApplicationProfiles(environment, config);
            if (!sourceProfiles.getPropertySources().isEmpty()) {
                compositePropertySource.addPropertySource(sourceProfiles);
            }
        } else {
            LOGGER.info("Configuration directory [{}] is not a directory or cannot be found at the specific path",
                FunctionUtils.doIfNotNull(config, () -> config, () -> "unspecified").get());
        }

        val embeddedProperties = loadEmbeddedProperties(resourceLoader, environment);
        compositePropertySource.addPropertySource(embeddedProperties);

        return Optional.of(compositePropertySource);
    }

    /**
     * Make a list of files that will be processed in order where the last one processed wins.
     * Profiles are added after base property names like {@code application.properties}, {@code cas.properties},
     * {@code CAS.properties} so that the profiles will override the base properties.
     * <p>
     * Profiles are processed in order so that the last profile list (e.g. in {@code spring.active.profiles}) will override the
     * the first profile.
     * <p>
     * Where multiple filenames with same base name and different extensions exist, the priority is yaml, yml, properties.
     */
    private static List<File> getAllPossibleExternalConfigDirFilenames(
        final Environment environment,
        final File configDirectory,
        final List<String> profiles) {
        val applicationName = CasConfigurationPropertiesSourceLocator.getApplicationName(environment);
        val configName = CasConfigurationPropertiesSourceLocator.getConfigurationName(environment);
        val appNameLowerCase = applicationName.toLowerCase(Locale.ENGLISH);
        val appConfigNames = CollectionUtils.wrapList("application", appNameLowerCase, applicationName, configName);

        val fileNames = appConfigNames
            .stream()
            .distinct()
            .flatMap(appName -> EXTENSIONS
                .stream()
                .map(ext -> new File(configDirectory, String.format("%s.%s", appName, ext))))
            .filter(File::exists)
            .collect(Collectors.toList());

        fileNames.addAll(profiles
            .stream()
            .flatMap(profile -> EXTENSIONS
                .stream()
                .flatMap(ext -> PROFILE_PATTERNS
                    .stream().map(pattern -> new File(configDirectory, String.format(pattern, profile, ext)))))
            .filter(File::exists).toList());

        fileNames.addAll(profiles
            .stream()
            .map(profile -> EXTENSIONS
                .stream()
                .map(ext -> appConfigNames
                    .stream()
                    .map(appName -> new File(configDirectory, String.format("%s-%s.%s", appName, profile, ext)))
                    .filter(File::exists)
                    .collect(Collectors.toList()))
                .flatMap(List::stream)
                .collect(Collectors.toList()))
            .flatMap(List::stream)
            .toList());

        val groovyFile = new File(configDirectory, appNameLowerCase.concat(".groovy"));
        FunctionUtils.doIf(groovyFile.exists(), o -> fileNames.add(groovyFile)).accept(groovyFile);
        return fileNames;
    }

    /**
     * Get all possible configuration files for config directory that actually exist as files.
     *
     * @param config   Folder in which to look for files
     * @param profiles Profiles that are active
     * @return List of files to be processed in order where last one processed overrides others
     */
    private static List<Resource> scanForConfigurationResources(final Environment environment, final File config,
                                                                final List<String> profiles) {
        val possibleFiles = getAllPossibleExternalConfigDirFilenames(environment, config, profiles);
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
    private CompositePropertySource loadSettingsByApplicationProfiles(final Environment environment, final File config) {
        val profiles = Arrays.stream(environment.getActiveProfiles()).toList();
        val resources = scanForConfigurationResources(environment, config, profiles);
        val composite = new CompositePropertySource("applicationProfilesCompositeProperties");
        LOGGER.info("Configuration files found at [{}] are [{}] under profile(s) [{}]", config, resources, profiles);
        resources.forEach(Unchecked.consumer(resource -> {
            LOGGER.debug("Loading configuration file [{}]", resource);

            val configurationLoaders = CasConfigurationPropertiesSourceLocator.getConfigurationPropertiesLoaders();
            val foundLoader = configurationLoaders
                .stream()
                .filter(loader -> loader.supports(resource))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No configuration loader is found to support " + resource));

            val source = foundLoader.load(resource, environment,
                "applicationProfilesProperties-" + resource.getFilename(), casConfigurationCipherExecutor);
            composite.addFirstPropertySource(source);


        }));
        return composite;
    }

    protected PropertySource<?> loadEmbeddedProperties(final ResourceLoader resourceLoader,
                                                       final Environment environment) {
        val profiles = Arrays.stream(environment.getActiveProfiles()).toList();
        val configFiles = profiles
            .stream()
            .map(profile -> EXTENSIONS.stream()
                .map(ext -> String.format("classpath:/application-%s.%s", profile, ext))
                .collect(Collectors.toList()))
            .flatMap(List::stream)
            .map(resourceLoader::getResource)
            .collect(Collectors.toList());

        configFiles.addAll(EXTENSIONS.stream()
            .map(ext -> String.format("classpath:/application.%s", ext))
            .map(resourceLoader::getResource).toList());

        LOGGER.debug("Loading embedded configuration files [{}]", configFiles);

        val composite = new CompositePropertySource("embeddedCompositeProperties");
        val configurationLoaders = CasConfigurationPropertiesSourceLocator.getConfigurationPropertiesLoaders();

        configFiles
            .stream()
            .filter(Resource::exists)
            .forEach(resource -> {
                LOGGER.trace("Loading properties from [{}]", resource);
                val foundLoader = configurationLoaders
                    .stream()
                    .filter(loader -> loader.supports(resource))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No configuration loader is found to support " + resource));
                val sourceName = String.format("embeddedProperties-%s", resource.getFilename());
                val source = foundLoader.load(resource, environment, sourceName, casConfigurationCipherExecutor);
                composite.addPropertySource(source);
            });
        return composite;
    }
}
