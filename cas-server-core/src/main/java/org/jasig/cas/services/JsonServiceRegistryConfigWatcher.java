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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * This is {@link JsonServiceRegistryConfigWatcher} that watches the json config directory
 * for changes and promptly attempts to reload the CAS service registry configuration.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
class JsonServiceRegistryConfigWatcher implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonServiceRegistryConfigWatcher.class);

    private final Object lock = new Object();

    private final WatchService watcher;

    private final JsonServiceRegistryDao serviceRegistryDao;

    /**
     * Instantiates a new Json service registry config watcher.
     *
     * @param serviceRegistryDao the registry to callback
     */
    public JsonServiceRegistryConfigWatcher(final JsonServiceRegistryDao serviceRegistryDao) {
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
        for (;;) {

            // wait for key to be signaled
            final WatchKey key;
            try {
                key = watcher.take();
            } catch (final InterruptedException e) {
                return;
            }

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
                        synchronized (this.lock) {
                            if (kind.name().equals(ENTRY_CREATE.name()) && file.exists()) {
                                //load the entry and add it to the map
                                final RegisteredService service = this.serviceRegistryDao.loadRegisteredServiceFromFile(file);
                                if (this.serviceRegistryDao.findServiceById(service.getId()) != null) {
                                    LOGGER.warn("Found a service definition [{}] with a duplicate id [{}] in [{}]. "
                                                    + "This will overwrite previous service definitions and is likely a "
                                                    + "configuration problem. Make sure all services have a unique id and try again.",
                                            service.getServiceId(), service.getId(), file.getAbsolutePath());

                                }
                                this.serviceRegistryDao.updateRegisteredService(service);
                                this.serviceRegistryDao.refreshServicesManager();
                            } else if (kind.name().equals(ENTRY_DELETE.name())) {
                                this.serviceRegistryDao.load();
                                this.serviceRegistryDao.refreshServicesManager();
                            } else if (kind.name().equals(ENTRY_MODIFY.name()) && file.exists()) {
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
                                    }
                                }
                            }
                        }
                    }

                }
            } finally {
                /*
                    Reset the key -- this step is critical to receive
                    further watch events. If the key is no longer valid, the directory
                    is inaccessible so exit the loop.
                 */
                final boolean valid = key.reset();
                if (!valid) {
                    LOGGER.warn("Directory key is no longer valid. Quitting watcher service");
                    break;
                }
            }

        }

    }
}
