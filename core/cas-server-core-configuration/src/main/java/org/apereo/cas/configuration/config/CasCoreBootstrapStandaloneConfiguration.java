package org.apereo.cas.configuration.config;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.configuration.support.CasConfigurationJasyptDecryptor;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * This is {@link CasCoreBootstrapStandaloneConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Profile("standalone")
@ConditionalOnProperty(value = "spring.cloud.config.enabled", havingValue = "false")
@Configuration("casStandaloneBootstrapConfiguration")
public class CasCoreBootstrapStandaloneConfiguration implements PropertySourceLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreBootstrapStandaloneConfiguration.class);

    private CasConfigurationJasyptDecryptor configurationJasyptDecryptor;

    @Autowired
    private ResourceLoader resourceLoader;

    @ConfigurationPropertiesBinding
    @Bean
    public Converter<String, List<Class<? extends Throwable>>> commaSeparatedStringToThrowablesCollection() {
        return new Converter<String, List<Class<? extends Throwable>>>() {
            @Override
            public List<Class<? extends Throwable>> convert(final String source) {
                try {
                    final List<Class<? extends Throwable>> classes = new ArrayList<>();
                    for (final String className : StringUtils.commaDelimitedListToStringArray(source)) {
                        classes.add((Class<? extends Throwable>) ClassUtils.forName(className.trim(), getClass().getClassLoader()));
                    }
                    return classes;
                } catch (final Exception e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }

    @Bean
    public CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager() {
        return new CasConfigurationPropertiesEnvironmentManager();
    }

    @Override
    public PropertySource<?> locate(final Environment environment) {
        this.configurationJasyptDecryptor = new CasConfigurationJasyptDecryptor(environment);

        final Properties props = new Properties();
        loadEmbeddedYamlOverriddenProperties(props, environment);

        final File configFile = configurationPropertiesEnvironmentManager().getStandaloneProfileConfigurationFile();
        if (configFile != null) {
            loadSettingsFromStandaloneConfigFile(props, configFile);
        }

        final File config = configurationPropertiesEnvironmentManager().getStandaloneProfileConfigurationDirectory();
        LOGGER.debug("Located CAS standalone configuration directory at [{}]", config);
        if (config.isDirectory() && config.exists()) {
            loadSettingsFromConfigurationSources(environment, props, config);
        } else {
            LOGGER.info("Configuration directory [{}] is not a directory or cannot be found at the specific path", config);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Located setting(s) [{}] from [{}]", props.keySet(), config);
        } else {
            LOGGER.info("Found and loaded [{}] setting(s) from [{}]", props.size(), config);
        }
        return new PropertiesPropertySource("standaloneCasConfigService", props);
    }

    private void loadSettingsFromStandaloneConfigFile(final Properties props, final File configFile) {
        final Properties pp = new Properties();
        
        try (FileReader r = new FileReader(configFile)) {
            LOGGER.debug("Located CAS standalone configuration file at [{}]", configFile);
            pp.load(r);
            LOGGER.debug("Found settings [{}] in file [{}]", pp.keySet(), configFile);
            props.putAll(decryptProperties(pp));
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    private Map<String, Object> decryptProperties(final Map properties) {
        return this.configurationJasyptDecryptor.decrypt(properties);
    }

    private void loadSettingsFromConfigurationSources(final Environment environment, final Properties props, final File config) {
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
                pp.load(new FileReader(f));
                LOGGER.debug("Found settings [{}] in file [{}]", pp.keySet(), f);
                props.putAll(decryptProperties(pp));
            }
        }));
    }

    private static Collection<File> scanForConfigurationFilesByPattern(final File config, final String regex) {
        return FileUtils.listFiles(config, new RegexFileFilter(regex, IOCase.INSENSITIVE), TrueFileFilter.INSTANCE)
                .stream()
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());
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
        profiles.add(configurationPropertiesEnvironmentManager().getApplicationName());
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

    private void loadEmbeddedYamlOverriddenProperties(final Properties props, final Environment environment) {
        final Resource resource = resourceLoader.getResource("classpath:/application.yml");
        if (resource != null && resource.exists()) {
            final Map pp = loadYamlProperties(resource);
            if (pp.isEmpty()) {
                LOGGER.debug("No properties were located inside [{}]", resource);
            } else {
                LOGGER.debug("Found settings [{}] in YAML file [{}]", pp.keySet(), resource);
                props.putAll(decryptProperties(pp));
            }
        }
    }
}
