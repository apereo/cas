package org.apereo.cas.services;

import com.google.common.base.Throwables;
import org.apache.commons.io.IOUtils;
import org.apereo.cas.support.events.service.CasRegisteredServicesRefreshEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * This is {@link ServiceRegistryConfigWatcher} that watches the json config directory
 * for changes and promptly attempts to reload the CAS service registry configuration.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
class ServiceRegistryConfigWatcher implements Runnable, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistryConfigWatcher.class);

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = this.lock.readLock();

    private final WatchService watcher;

    private final ResourceBasedServiceRegistryDao serviceRegistryDao;

    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * Instantiates a new Json service registry config watcher.
     *
     * @param serviceRegistryDao the registry to callback
     */
    ServiceRegistryConfigWatcher(final ResourceBasedServiceRegistryDao serviceRegistryDao,
                                 final ApplicationEventPublisher eventPublisher) {
        try {
            this.serviceRegistryDao = serviceRegistryDao;
            this.watcher = FileSystems.getDefault().newWatchService();
            final WatchEvent.Kind[] kinds = new WatchEvent.Kind[]{ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY};
            LOGGER.debug("Created service registry watcher for events of type [{}]", (Object[]) kinds);
            this.serviceRegistryDao.getWatchableResource().register(this.watcher, kinds);
            
            this.applicationEventPublisher = eventPublisher;
        } catch (final IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void run() {
        if (this.running.compareAndSet(false, true)) {
            while (this.running.get()) {
                // wait for key to be signaled
                WatchKey key = null;
                try {
                    key = this.watcher.take();
                    handleEvent(key);
                } catch (final InterruptedException e) {
                    return;
                } finally {
                    /*
                        Reset the key -- this step is critical to receive
                        further watch events. If the key is no longer valid, the directory
                        is inaccessible so exit the loop.
                     */
                    final boolean valid = key != null && key.reset();
                    if (!valid) {
                        LOGGER.warn("Directory key is no longer valid. Quitting watcher service");
                    }
                }
            }
        }

    }

    /**
     * Handle event.
     *
     * @param key the key
     */
    private void handleEvent(final WatchKey key) {
        this.readLock.lock();
        try {
            //The filename is the context of the event.
            key.pollEvents().stream().filter(event -> event.count() <= 1).forEach(event -> {
                final WatchEvent.Kind kind = event.kind();

                //The filename is the context of the event.
                final WatchEvent<Path> ev = (WatchEvent<Path>) event;
                final Path filename = ev.context();

                final Path parent = (Path) key.watchable();
                final Path fullPath = parent.resolve(filename);
                final File file = fullPath.toFile();

                LOGGER.trace("Detected event [{}] on file [{}]. Loading change...", kind, file);
                if (kind.name().equals(ENTRY_CREATE.name()) && file.exists()) {
                    handleCreateEvent(file);
                } else if (kind.name().equals(ENTRY_DELETE.name())) {
                    handleDeleteEvent();
                } else if (kind.name().equals(ENTRY_MODIFY.name()) && file.exists()) {
                    handleModifyEvent(file);
                }
            });
        } finally {
            this.readLock.unlock();
        }
    }

    /**
     * Handle modify event.
     *
     * @param file the file
     */
    private void handleModifyEvent(final File file) {
        final RegisteredService newService = this.serviceRegistryDao.load(file);
        if (newService == null) {
            LOGGER.warn("New service definition could not be loaded from [{}]", file.getAbsolutePath());
        } else {
            final RegisteredService oldService = this.serviceRegistryDao.findServiceById(newService.getId());

            if (!newService.equals(oldService)) {
                this.serviceRegistryDao.update(newService);
                this.applicationEventPublisher.publishEvent(new CasRegisteredServicesRefreshEvent(this));
            } else {
                LOGGER.debug("Service [{}] loaded from [{}] is identical to the existing entry. Entry may have already been saved "
                        + "in the event processing pipeline", newService.getId(), file.getName());
            }
        }
    }

    /**
     * Handle delete event.
     */
    private void handleDeleteEvent() {
        this.serviceRegistryDao.load();
        this.applicationEventPublisher.publishEvent(new CasRegisteredServicesRefreshEvent(this));
    }

    /**
     * Handle create event.
     *
     * @param file the file
     */
    private void handleCreateEvent(final File file) {
        //load the entry and add it to the map
        final RegisteredService service = this.serviceRegistryDao.load(file);
        if (service == null) {
            LOGGER.warn("No service definition was loaded from [{}]", file);
            return;
        }
        if (this.serviceRegistryDao.findServiceById(service.getId()) != null) {
            LOGGER.warn("Found a service definition [{}] with a duplicate id [{}] in [{}]. "
                            + "This will overwrite previous service definitions and is likely a "
                            + "configuration problem. Make sure all services have a unique id and try again.",
                    service.getServiceId(), service.getId(), file.getAbsolutePath());

        }
        this.serviceRegistryDao.update(service);
        this.applicationEventPublisher.publishEvent(new CasRegisteredServicesRefreshEvent(this));
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(this.watcher);
    }

    public void setApplicationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
