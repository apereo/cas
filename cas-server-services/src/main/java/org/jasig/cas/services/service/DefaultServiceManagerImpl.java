/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.service;

import java.util.List;

import org.jasig.cas.services.dao.ServiceDao;
import org.jasig.cas.services.domain.RegisteredService;
import org.springframework.util.Assert;

/**
 * Default implementation of the {@link ServiceManager}. 
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public class DefaultServiceManagerImpl implements ServiceManager {

    /**
     * Instance of ServiceDao.
     */
    private ServiceDao serviceDao;
    
    public DefaultServiceManagerImpl(final ServiceDao serviceDao) {
        Assert.notNull(this.serviceDao);
        this.serviceDao = serviceDao;
    }
    
    public boolean addService(final RegisteredService registeredService) {
        return this.serviceDao.save(registeredService);
    }

    public boolean deleteService(final String id) {
        return this.serviceDao.deleteById(id);
    }

    public RegisteredService[] getAllServices() {
        final List list = this.serviceDao.getAllServices();
        
        return (RegisteredService[]) list.toArray(new RegisteredService[list.size()]);
    }

    public RegisteredService getServiceById(final String id) {
        return this.serviceDao.findServiceById(id);
    }

    public RegisteredService getServiceByUrl(final String url) {
        return this.serviceDao.findServiceByUrl(url);
    }

    public boolean updateService(final RegisteredService registeredService) {
        return this.serviceDao.save(registeredService);
    }
}
