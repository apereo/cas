package org.apereo.cas.configuration;

import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.support.events.config.CasConfigurationCreatedEvent;
import org.apereo.cas.support.events.config.CasConfigurationDeletedEvent;
import org.apereo.cas.support.events.config.CasConfigurationModifiedEvent;
import org.apereo.cas.util.function.ComposableFunction;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.io.PathWatcherService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.io.Closeable;
import java.io.File;

/**
 * This is {@link CasConfigurationWatchService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class CasConfigurationWatchService implements Closeable {
    private final ComposableFunction<File, AbstractCasEvent> createConfigurationCreatedEvent = file -> new CasConfigurationCreatedEvent(this, file.toPath());

    private final ComposableFunction<File, AbstractCasEvent> createConfigurationModifiedEvent = file -> new CasConfigurationModifiedEvent(this, file.toPath());

    private final ComposableFunction<File, AbstractCasEvent> createConfigurationDeletedEvent = file -> new CasConfigurationDeletedEvent(this, file.toPath());

    private final CasConfigurationPropertiesEnvironmentManager configurationPropertiesEnvironmentManager;

    private final ApplicationEventPublisher eventPublisher;

    private PathWatcherService configurationDirectoryWatch;

    private FileWatcherService configurationFileWatch;

    @Override
    public void close() {
        closeWatchServices();
    }

    /**
     * Run path watch services.
     *
     * @param event the event
     */
    @EventListener
    @Async
    public void runPathWatchServices(final ApplicationReadyEvent event) {
        watchConfigurationDirectoryIfNeeded();
        watchConfigurationFileIfNeeded();
    }

    private void watchConfigurationFileIfNeeded() {
        val configFile = configurationPropertiesEnvironmentManager.getStandaloneProfileConfigurationFile();
        if (configFile != null && configFile.exists()) {
            LOGGER.debug("Starting to watch configuration file [{}]", configFile);
            this.configurationFileWatch = new FileWatcherService(configFile.getParentFile(),
                createConfigurationCreatedEvent.andNext(eventPublisher::publishEvent),
                createConfigurationModifiedEvent.andNext(eventPublisher::publishEvent),
                createConfigurationDeletedEvent.andNext(eventPublisher::publishEvent));
            configurationFileWatch.start(configFile.getName());
        }
    }

    private void watchConfigurationDirectoryIfNeeded() {
        val configDirectory = configurationPropertiesEnvironmentManager.getStandaloneProfileConfigurationDirectory();
        if (configDirectory != null && configDirectory.exists()) {
            LOGGER.debug("Starting to watch configuration directory [{}]", configDirectory);
            this.configurationDirectoryWatch = new PathWatcherService(configDirectory.toPath(),
                createConfigurationCreatedEvent.andNext(eventPublisher::publishEvent),
                createConfigurationModifiedEvent.andNext(eventPublisher::publishEvent),
                createConfigurationDeletedEvent.andNext(eventPublisher::publishEvent));
            configurationDirectoryWatch.start(configDirectory.getName());
        }
    }
    
    private void closeWatchServices() {
        if (configurationDirectoryWatch != null) {
            configurationDirectoryWatch.close();
        }
        if (configurationFileWatch != null) {
            configurationFileWatch.close();
        }
    }
}
