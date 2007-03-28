/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.jasig.cas.authentication.principal.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Default implementation of the {@link ServicesManager} interface. If there are
 * no services registered with the server, it considers the ServicecsManager
 * disabled and will not prevent any service from using CAS.
 * 
 * TODO registered service is enabled
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class DefaultServicesManagerImpl implements ServicesManager {

    /** Instance of ServiceRegistryDao. */
    private ServiceRegistryDao serviceRegistryDao;

    /** Map to store all services. */
    private ConcurrentHashMap<Long, RegisteredService> services = new ConcurrentHashMap<Long, RegisteredService>();

    /** Default service to return if none have been registered. */
    private RegisteredService disabledRegisteredService;
    
//    private AttributeRepository attributeRepository;

    public DefaultServicesManagerImpl(
        final ServiceRegistryDao serviceRegistryDao) {
        Assert
            .notNull(serviceRegistryDao, "serviceRegistryDao cannot be null.");

        this.serviceRegistryDao = serviceRegistryDao;

        for (final RegisteredService r : this.serviceRegistryDao.load()) {
            final List<Attribute> attributes = new ArrayList<Attribute>();
/*            for (final Attribute a : r.getAllowedAttributes()) {
                attributes.add(this.attributeRepository.getAttribute(a.getId()));    
            }
            ((RegisteredServiceImpl) r).setAllowedAttributes(attributes); */
            
            this.services.put(new Long(r.getId()), r);
        }

        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setAllowedToProxy(true);
        r.setAnonymousAccess(false);
        r.setEnabled(true);
        r.setSsoEnabled(true);

        this.disabledRegisteredService = r;
    }

    @Transactional(readOnly = false)
    public RegisteredService delete(final long id) {
        final RegisteredService r = findServiceBy(id);
        if (r == null) {
            return null;
        }
        
        this.serviceRegistryDao.delete(r);
        this.services.remove(new Long(r.getId()));
        
        return r;
    }

    public RegisteredService findServiceBy(final Service service) {
        final Collection<RegisteredService> c = this.services.values();
        
        if (c.isEmpty()) {
            return this.disabledRegisteredService;
        }

        for (final RegisteredService r : c) {
            if (r.matches(service)) {
                return r;
            }
        }

        return null;
    }

    public RegisteredService findServiceBy(final long id) {
        final Collection<RegisteredService> c = this.services.values();
        
        if (c.isEmpty()) {
            return this.disabledRegisteredService;
        }

        for (final RegisteredService r : c) {
            if (r.getId() == id) {
                return r;
            }
        }

        return null;
    }

    public Collection<RegisteredService> getAllServices() {
        return Collections.unmodifiableCollection(this.services.values());
    }

    public boolean matchesExistingService(final Service service) {
        return findServiceBy(service) != null;
    }

    @Transactional(readOnly = false)
    public void save(final RegisteredService registeredService) {
        this.serviceRegistryDao.save(registeredService);
        this.services.put(new Long(registeredService.getId()),
            registeredService);
    }
}
