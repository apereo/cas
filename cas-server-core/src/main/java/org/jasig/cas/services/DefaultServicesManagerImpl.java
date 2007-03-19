/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.jasig.cas.authentication.principal.Service;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 * 
 * TODO enabled/disabled
 *
 */
public class DefaultServicesManagerImpl implements ServicesManager,
    InitializingBean {

    private ServiceRegistryDao serviceRegistryDao;

    private ConcurrentHashMap<Long, RegisteredService> services = new ConcurrentHashMap<Long, RegisteredService>();

    @Transactional(readOnly=false)
    public boolean delete(final RegisteredService registeredService) {
        final Long id = new Long(registeredService.getId());
        this.serviceRegistryDao.delete(registeredService);
        return this.services.remove(id) != null;
    }

    public RegisteredService findServiceBy(final Service service) {
        final Collection<RegisteredService> c = this.services.values();
        
        for (final RegisteredService r : c) {
            if (r.matches(service)) {
                return r;
            }
        }
        
        return null;
    }

    public RegisteredService findServiceBy(final long id) {
        return this.services.get(new Long(id));
    }

    public Collection<RegisteredService> getAllServices() {
        return Collections.unmodifiableCollection(this.services.values());
    }

    public boolean matchesExistingService(final Service service) {
        return findServiceBy(service) != null;
    }

    @Transactional(readOnly=false)
    public void save(final RegisteredService registeredService) {
        this.serviceRegistryDao.save(registeredService);
        this.services.putIfAbsent(new Long(registeredService.getId()),
            registeredService);
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.serviceRegistryDao,
            "serviceRegistryDao cannot be null.");

        for (final RegisteredService r : this.serviceRegistryDao.load()) {
            this.services.put(new Long(r.getId()), r);
        }
    }
}
