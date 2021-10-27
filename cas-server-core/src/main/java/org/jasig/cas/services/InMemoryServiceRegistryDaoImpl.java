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

import javax.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Default In Memory Service Registry Dao for test/demonstration purposes.
 *
 * @author Scott Battaglia
 * @since 3.1
 *
 */
public final class InMemoryServiceRegistryDaoImpl implements ServiceRegistryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryServiceRegistryDaoImpl.class);

    @NotNull
    private List<RegisteredService> registeredServices = new ArrayList<>();

    /**
     * Instantiates a new In memory service registry.
     */
    public InMemoryServiceRegistryDaoImpl() {
        LOGGER.warn("Runtime memory is used as the persistence storage for retrieving and persisting service definitions. "
                + "Changes that are made to service definitions during runtime "
                + "will be LOST upon container restarts.");
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        return this.registeredServices.remove(registeredService);
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        for (final RegisteredService r : this.registeredServices) {
            if (r.getId() == id) {
                return r;
            }
        }

        return null;
    }

    @Override
    public List<RegisteredService> load() {
        return this.registeredServices;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        if (registeredService.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
            ((AbstractRegisteredService) registeredService).setId(findHighestId()+1);
        }

        this.registeredServices.remove(registeredService);
        this.registeredServices.add(registeredService);

        return registeredService;
    }

    public void setRegisteredServices(final List<RegisteredService> registeredServices) {
        this.registeredServices = registeredServices;
    }

    /**
     * This isn't super-fast but we don't expect thousands of services.
     *
     * @return the highest service id in the list of registered services
     */
    private long findHighestId() {
        long id = 0;

        for (final RegisteredService r : this.registeredServices) {
            if (r.getId() > id) {
                id = r.getId();
            }
        }

        return id;
    }
}
