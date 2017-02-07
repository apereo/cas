package org.apereo.cas.config;

import com.google.common.base.Throwables;
import org.apereo.cas.config.monitor.ConfigurationDirectoryPathWatchService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.config.server.config.EnvironmentRepositoryConfiguration;
import org.springframework.cloud.config.server.environment.NativeEnvironmentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * This is {@link CasConfigurationSupportUtilitiesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casConfigurationSupportUtilitiesConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasConfigurationSupportUtilitiesConfiguration {
    /**
     * The watch configuration.
     */
    @Configuration("casCoreConfigurationWatchConfiguration")
    @Profile("native")
    @AutoConfigureAfter(value = {EnvironmentRepositoryConfiguration.class, EnvironmentRepositoryConfiguration.class})
    public static class CasCoreConfigurationWatchConfiguration {
        private static final Logger LOGGER = LoggerFactory.getLogger(CasCoreConfigurationWatchConfiguration.class);

        @Autowired
        private ResourceLoader resourceLoader;

        @Autowired
        private ApplicationEventPublisher eventPublisher;

        @Autowired
        @Qualifier("environmentRepository")
        private NativeEnvironmentRepository environmentRepository;

        @PostConstruct
        public void init() {
            runNativeConfigurationDirectoryPathWatchService();
        }

        public void runNativeConfigurationDirectoryPathWatchService() {
            try {
                final List<String> locs = Arrays.asList(environmentRepository.getSearchLocations());
                if (locs.isEmpty()) {
                    throw new BeanCreationException("No search locations are defined for the native configuration profile");
                }
                final String loc = locs.get(0);
                LOGGER.debug("Starting to watch configuration directory [{}]", loc);

                final Path directory = resourceLoader.getResource(loc).getFile().toPath();
                final Thread th = new Thread(new ConfigurationDirectoryPathWatchService(directory, eventPublisher));
                th.start();
            } catch (final Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }
}
