
package org.apereo.cas.config;

import com.google.common.base.Throwables;
import org.apereo.cas.config.monitor.ConfigurationDirectoryPathWatchService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * This is {@link CasConfigurationSupportUtilitiesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casConfigurationSupportUtilitiesConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class CasConfigurationSupportUtilitiesConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CasConfigurationSupportUtilitiesConfiguration.class);

    @Autowired
    private CasConfigurationProperties casProperties;
    
    /**
     * The watch configuration.
     */
    @Configuration("casCoreConfigurationWatchConfiguration")
    @Profile("standalone")
    @ConditionalOnProperty(value = "spring.cloud.config.enabled", havingValue = "false")
    public class CasCoreConfigurationWatchConfiguration {
        @Autowired
        private ApplicationEventPublisher eventPublisher;

        @Autowired
        @Qualifier("configurationPropertiesEnvironmentManager")
        private CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager;
        
        @PostConstruct
        public void init() {
            runNativeConfigurationDirectoryPathWatchService();
        }

        public void runNativeConfigurationDirectoryPathWatchService() {
            try {
                final File config = configurationPropertiesEnvironmentManager.getStandaloneProfileConfigurationDirectory();
                if (casProperties.getEvents().isTrackConfigurationModifications() && config.exists()) {
                    LOGGER.debug("Starting to watch configuration directory [{}]", config);
                    final Thread th = new Thread(new ConfigurationDirectoryPathWatchService(config.toPath(), eventPublisher));
                    th.start();
                } else {
                    LOGGER.info("CAS is configured to NOT watch configuration directory [{}]. Changes require manual reloads/restarts.", config);
                }
            } catch (final Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }
}
