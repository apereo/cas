package org.apereo.cas.config.monitor;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.support.events.CasConfigurationCreatedEvent;
import org.apereo.cas.support.events.CasConfigurationDeletedEvent;
import org.apereo.cas.support.events.CasConfigurationModifiedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * This is {@link ConfigurationDirectoryPathWatchService}.
 * The general intent of this component is to watch configuration directory
 * and notify CAS of changes.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class ConfigurationDirectoryPathWatchService implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationDirectoryPathWatchService.class);

    private final WatchService watcher;
    private final Path directory;
    private final ApplicationEventPublisher eventPublisher;

    public ConfigurationDirectoryPathWatchService(final Path directory, final ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;

        try {
            this.directory = directory;
            this.watcher = FileSystems.getDefault().newWatchService();
            this.directory.register(this.watcher,
                    ENTRY_CREATE,
                    ENTRY_DELETE,
                    ENTRY_MODIFY);
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Watch the directory for changes.
     */
    public void watch() {
        final AtomicBoolean eventFired = new AtomicBoolean();

        while (true) {

            final WatchKey key;
            try {
                key = watcher.take();
            } catch (final InterruptedException e) {
                LOGGER.warn(e.getMessage(), e);
                return;
            }


            for (final WatchEvent<?> event : key.pollEvents()) {
                final WatchEvent.Kind<?> kind = event.kind();

                if (kind == OVERFLOW) {
                    LOGGER.warn("An overflow event occurred. File system events may be lost or discarded.");
                    continue;
                }

                final WatchEvent<Path> ev = (WatchEvent<Path>) event;
                final Path filename = ev.context();

                try {
                    LOGGER.debug("Detected configuration change [{}]", kind.name());
                    final Path child = this.directory.resolve(filename);
                    if (StringUtils.equalsIgnoreCase(StandardWatchEventKinds.ENTRY_CREATE.name(), kind.name()) && !eventFired.get()) {
                        this.eventPublisher.publishEvent(new CasConfigurationCreatedEvent(this, child));
                        eventFired.set(true);
                    }
                    if (StringUtils.equalsIgnoreCase(StandardWatchEventKinds.ENTRY_DELETE.name(), kind.name()) && !eventFired.get()) {
                        this.eventPublisher.publishEvent(new CasConfigurationDeletedEvent(this, child));
                        eventFired.set(true);
                    }
                    if (StringUtils.equalsIgnoreCase(StandardWatchEventKinds.ENTRY_MODIFY.name(), kind.name()) && !eventFired.get()) {
                        this.eventPublisher.publishEvent(new CasConfigurationModifiedEvent(this, child));
                        eventFired.set(true);
                    }
                } catch (final Exception e) {
                    LOGGER.warn(e.getMessage(), e);
                    continue;
                }
            }
            eventFired.set(false);
            final boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }

    @Override
    public void run() {
        watch();
    }
}
