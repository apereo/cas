package org.apereo.cas.configuration;

import org.apereo.cas.config.CasConfigurationCreatedEvent;
import org.apereo.cas.config.CasConfigurationDeletedEvent;
import org.apereo.cas.config.CasConfigurationModifiedEvent;
import org.apereo.cas.configuration.api.CasConfigurationPropertiesSourceLocator;
import org.apereo.cas.support.events.AbstractCasEvent;
import org.apereo.cas.util.function.ComposableFunction;
import org.apereo.cas.util.io.FileWatcherService;
import org.apereo.cas.util.io.PathWatcherService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ConfigurableApplicationContext;

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
public class CasConfigurationWatchService implements Closeable, InitializingBean {
    private final ComposableFunction<File, AbstractCasEvent> createConfigurationCreatedEvent = file ->
            new CasConfigurationCreatedEvent(this, file.toPath(), ClientInfoHolder.getClientInfo());

    private final ComposableFunction<File, AbstractCasEvent> createConfigurationModifiedEvent = file ->
            new CasConfigurationModifiedEvent(this, file.toPath(), ClientInfoHolder.getClientInfo());

    private final ComposableFunction<File, AbstractCasEvent> createConfigurationDeletedEvent = file ->
            new CasConfigurationDeletedEvent(this, file.toPath(), ClientInfoHolder.getClientInfo());

    private final ConfigurableApplicationContext applicationContext;

    private PathWatcherService configurationDirectoryWatch;

    private FileWatcherService configurationFileWatch;

    @Override
    public void close() {
        closeWatchServices();
    }

    /**
     * Initialize.
     */
    public void initialize() {
        watchConfigurationDirectoryIfNeeded();
        watchConfigurationFileIfNeeded();
    }

    @Override
    public void afterPropertiesSet() {
        initialize();
    }

    private void watchConfigurationFileIfNeeded() {
        val environment = applicationContext.getEnvironment();
        val configFile = CasConfigurationPropertiesSourceLocator.getStandaloneProfileConfigurationFile(environment);
        if (configFile != null && configFile.exists()) {
            LOGGER.debug("Starting to watch configuration file [{}]", configFile);
            this.configurationFileWatch = new FileWatcherService(configFile,
                createConfigurationCreatedEvent.andNext(applicationContext::publishEvent),
                createConfigurationModifiedEvent.andNext(applicationContext::publishEvent),
                createConfigurationDeletedEvent.andNext(applicationContext::publishEvent));
            configurationFileWatch.start(configFile.getName());
        }
    }

    private void watchConfigurationDirectoryIfNeeded() {
        val environment = applicationContext.getEnvironment();
        val configDirectory = CasConfigurationPropertiesSourceLocator.getStandaloneProfileConfigurationDirectory(environment);
        if (configDirectory != null && configDirectory.exists()) {
            LOGGER.debug("Starting to watch configuration directory [{}]", configDirectory);
            configurationDirectoryWatch = new PathWatcherService(configDirectory.toPath(),
                createConfigurationCreatedEvent.andNext(applicationContext::publishEvent),
                createConfigurationModifiedEvent.andNext(applicationContext::publishEvent),
                createConfigurationDeletedEvent.andNext(applicationContext::publishEvent));
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
