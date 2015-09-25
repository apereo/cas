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

import org.jasig.cas.authentication.principal.Service;
import org.jasig.inspektr.audit.annotation.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the {@link ServicesManager} interface. If there are
 * no services registered with the server, it considers the ServicecsManager
 * disabled and will not prevent any service from using CAS.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Component("servicesManager")
public final class DefaultServicesManagerImpl implements ReloadableServicesManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServicesManagerImpl.class);

    /** Instance of ServiceRegistryDao. */
    @NotNull
    private final ServiceRegistryDao serviceRegistryDao;

    /** Map to store all services. */
    private ConcurrentHashMap<Long, RegisteredService> services = new ConcurrentHashMap<>();

    /**
     * Instantiates a new default services manager impl.
     *
     * @param serviceRegistryDao the service registry dao
     */
    @Autowired
    public DefaultServicesManagerImpl(@Qualifier("serviceRegistryDao")
                                      final ServiceRegistryDao serviceRegistryDao) {
        this.serviceRegistryDao = serviceRegistryDao;

        load();
    }

    @Audit(action = "DELETE_SERVICE", actionResolverName = "DELETE_SERVICE_ACTION_RESOLVER",
            resourceResolverName = "DELETE_SERVICE_RESOURCE_RESOLVER")
    @Override
    public synchronized RegisteredService delete(final long id) {
        final RegisteredService r = findServiceBy(id);
        if (r == null) {
            return null;
        }

        this.serviceRegistryDao.delete(r);
        this.services.remove(id);

        return r;
    }


    @Override
    public RegisteredService findServiceBy(final Service service) {
        final Collection<RegisteredService> c = convertToTreeSet();

        for (final RegisteredService r : c) {
            if (r.matches(service)) {
                return r;
            }
        }

        return null;
    }

    @Override
    public RegisteredService findServiceBy(final long id) {
        final RegisteredService r = this.services.get(id);

        try {
            return r == null ? null : r.clone();
        } catch (final CloneNotSupportedException e) {
            return r;
        }
    }

    /**
     * Stuff services to tree set.
     *
     * @return the tree set
     */
    protected TreeSet<RegisteredService> convertToTreeSet() {
        return new TreeSet<>(this.services.values());
    }

    @Override
    public Collection<RegisteredService> getAllServices() {
        return Collections.unmodifiableCollection(convertToTreeSet());
    }

    @Override
    public boolean matchesExistingService(final Service service) {
        return findServiceBy(service) != null;
    }

    @Audit(action = "SAVE_SERVICE", actionResolverName = "SAVE_SERVICE_ACTION_RESOLVER",
            resourceResolverName = "SAVE_SERVICE_RESOURCE_RESOLVER")
    @Override
    public synchronized RegisteredService save(final RegisteredService registeredService) {
        final RegisteredService r = this.serviceRegistryDao.save(registeredService);
        this.services.put(r.getId(), r);
        return r;
    }

    @Override
    public void reload() {
        LOGGER.info("Reloading registered services.");
        load();
    }

    /**
     * Load services that are provided by the DAO. 
     */
    private void load() {
        final ConcurrentHashMap<Long, RegisteredService> localServices =
                new ConcurrentHashMap<>();

        for (final RegisteredService r : this.serviceRegistryDao.load()) {
            LOGGER.debug("Adding registered service {}", r.getServiceId());
            localServices.put(r.getId(), r);
        }

        this.services = localServices;
        LOGGER.info("Loaded {} services.", this.services.size());
        
    }
}
