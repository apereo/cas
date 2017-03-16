package org.apereo.cas.configuration.config;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

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

    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    public CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager() {
        return new CasConfigurationPropertiesEnvironmentManager();
    }

    @Override
    public PropertySource<?> locate(final Environment environment) {
        final Properties props = new Properties();
        loadEmbeddedYamlOverriddenProperties(props);

        final File config = configurationPropertiesEnvironmentManager().getStandaloneProfileConfigurationDirectory();
        LOGGER.debug("Located CAS standalone configuration directory at [{}]", config);
        if (config.isDirectory() && config.exists()) {
            loadSettingsFromConfigurationSources(environment, props, config);
        } else {
            LOGGER.warn("Configuration directory [{}] is not a directory or cannot be found at the specific path", config);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Located setting(s) [{}] from [{}]", props.keySet(), config);
        } else {
            LOGGER.info("Found and loaded [{}] setting(s) from [{}]", props.size(), config);
        }
        return new PropertiesPropertySource("standaloneCasConfigService", props);
    }

    private void loadSettingsFromConfigurationSources(final Environment environment, final Properties props, final File config) {
        final List<String> profiles = new ArrayList<>();
        profiles.add(configurationPropertiesEnvironmentManager().getApplicationName());
        profiles.addAll(Arrays.stream(environment.getActiveProfiles()).collect(Collectors.toList()));
        final String propertyNames = profiles.stream().collect(Collectors.joining("|"));
        final String regex = String.format("(%s|application)\\.(yml|properties)", propertyNames);
        LOGGER.debug("Looking for configuration files at [{}] that match the pattern [{}]", config, regex);

        final Collection<File> configFiles = FileUtils.listFiles(config, new RegexFileFilter(regex, IOCase.INSENSITIVE), TrueFileFilter.INSTANCE)
                .stream()
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());
        LOGGER.info("Configuration files found at [{}] are [{}]", config, configFiles);
        configFiles.forEach(Unchecked.consumer(f -> {
            LOGGER.debug("Loading configuration file [{}]", f);
            if (f.getName().toLowerCase().endsWith("yml")) {
                final Map pp = loadYamlProperties(new FileSystemResource(f));
                LOGGER.debug("Found settings [{}] in YAML file [{}]", pp.keySet(), f);
                props.putAll(pp);
            } else {
                final Properties pp = new Properties();
                pp.load(new FileReader(f));
                LOGGER.debug("Found settings [{}] in file [{}]", pp.keySet(), f);
                props.putAll(pp);
            }
        }));
    }

    private Map loadYamlProperties(final Resource... resource) {
        final YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResolutionMethod(YamlProcessor.ResolutionMethod.OVERRIDE);
        factory.setResources(resource);
        factory.setSingleton(true);
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    private void loadEmbeddedYamlOverriddenProperties(final Properties props) {
        final Resource resource = resourceLoader.getResource("classpath:/application.yml");
        if (resource != null && resource.exists()) {
            final Map pp = loadYamlProperties(resource);
            if (pp.isEmpty()) {
                LOGGER.debug("No properties were located inside [{}]", resource);
            } else {
                LOGGER.debug("Found settings [{}] in YAML file [{}]", pp.keySet(), resource);
                props.putAll(pp);
            }
        }
    }
}
