
package org.apereo.cas.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.CasConfigurationPropertiesEnvironmentManager;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.support.events.config.CasConfigurationCreatedEvent;
import org.apereo.cas.support.events.config.CasConfigurationDeletedEvent;
import org.apereo.cas.support.events.config.CasConfigurationModifiedEvent;
import org.apereo.cas.util.function.ComposableFunction;
import org.apereo.cas.util.io.PathWatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.function.Consumer;

/**
 * This is {@link CasConfigurationSupportUtilitiesConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("casConfigurationSupportUtilitiesConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class CasConfigurationSupportUtilitiesConfiguration {


    @Autowired
    private CasConfigurationProperties casProperties;

    private final ComposableFunction<File, AbstractCasEvent> createConfigurationCreatedEvent = file -> new CasConfigurationCreatedEvent(this, file.toPath());
    private final ComposableFunction<File, AbstractCasEvent> createConfigurationModifiedEvent = file -> new CasConfigurationModifiedEvent(this, file.toPath());
    private final ComposableFunction<File, AbstractCasEvent> createConfigurationDeletedEvent = file -> new CasConfigurationDeletedEvent(this, file.toPath());

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

        private final Consumer<AbstractCasEvent> publish = event -> eventPublisher.publishEvent(event);

        @PostConstruct
        public void init() {
            runNativeConfigurationDirectoryPathWatchService();
        }

        @SneakyThrows
        public void runNativeConfigurationDirectoryPathWatchService() {

            final File config = configurationPropertiesEnvironmentManager.getStandaloneProfileConfigurationDirectory();
            if (casProperties.getEvents().isTrackConfigurationModifications() && config.exists()) {
                LOGGER.debug("Starting to watch configuration directory [{}]", config);
                final PathWatcherService watcher = new PathWatcherService(config.toPath(),
                    createConfigurationCreatedEvent.andNext(publish),
                    createConfigurationModifiedEvent.andNext(publish),
                    createConfigurationDeletedEvent.andNext(publish));
                watcher.start(config.getName());
            } else {
                LOGGER.info("CAS is configured to NOT watch configuration directory [{}]. Changes require manual reloads/restarts.", config);
            }

        }
    }
}
