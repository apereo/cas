/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.cas.services;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * This is {@link JsonServiceRegistryConfigWatcher} that watches the json config directory
 * for changes and promptly attempts to reload the CAS service registry configuration.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
class JsonServiceRegistryConfigWatcher implements Runnable, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonServiceRegistryConfigWatcher.class);

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = this.lock.readLock();

    private final WatchService watcher;

    private final JsonServiceRegistryDao serviceRegistryDao;

    /**
     * Instantiates a new Json service registry config watcher.
     *
     * @param serviceRegistryDao the registry to callback
     */
    JsonServiceRegistryConfigWatcher(final JsonServiceRegistryDao serviceRegistryDao) {
        try {
            this.serviceRegistryDao = serviceRegistryDao;
            this.watcher = FileSystems.getDefault().newWatchService();
            final WatchEvent.Kind[] kinds = (WatchEvent.Kind[])
                    Arrays.asList(ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY).toArray();
            this.serviceRegistryDao.getServiceRegistryDirectory().register(this.watcher, kinds);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        if (running.compareAndSet(false, true)) {
            while (running.get()) {
                // wait for key to be signaled
                WatchKey key = null;
                try {
                    key = watcher.take();
                    handleEvent(key);
                } catch (final InterruptedException e) {
                    return;
                } finally {
                    /*
                        Reset the key -- this step is critical to receive
                        further watch events. If the key is no longer valid, the directory
                        is inaccessible so exit the loop.
                     */
                    final boolean valid = (key != null && key.reset());
                    if (!valid) {
                        LOGGER.warn("Directory key is no longer valid. Quitting watcher service");
                        break;
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
            for (final WatchEvent<?> event : key.pollEvents()) {
                if (event.count() <= 1) {
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
                }

            }
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
    /*
        load the entry and save it back to the map
        without any warnings on duplicate ids.
     */
        final RegisteredService newService = this.serviceRegistryDao.loadRegisteredServiceFromFile(file);
        if (newService == null) {
            LOGGER.warn("New service definition could not be loaded from [{}]", file.getAbsolutePath());
        } else {
            final RegisteredService oldService = this.serviceRegistryDao.findServiceById(newService.getId());

            if (!newService.equals(oldService)) {
                this.serviceRegistryDao.updateRegisteredService(newService);
                this.serviceRegistryDao.refreshServicesManager();
            } else {
                LOGGER.debug("Service [{}] loaded from [{}] is idential to the existing entry. "
                            + "Services manager will not reload", newService.getId(),
                        file.getName());
            }
        }
    }

    /**
     * Handle delete event.
     */
    private void handleDeleteEvent() {
        this.serviceRegistryDao.load();
        this.serviceRegistryDao.refreshServicesManager();
    }

    /**
     * Handle create event.
     *
     * @param file the file
     */
    private void handleCreateEvent(final File file) {
        //load the entry and add it to the map
        final RegisteredService service = this.serviceRegistryDao.loadRegisteredServiceFromFile(file);
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
        this.serviceRegistryDao.updateRegisteredService(service);
        this.serviceRegistryDao.refreshServicesManager();
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(this.watcher);
    }
}
