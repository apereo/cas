/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import org.springframework.orm.jpa.support.JpaDaoSupport;

import java.util.List;

/**
 * Implementation of the ServiceRegistryDao based on JPA.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class JpaServiceRegistryDaoImpl extends JpaDaoSupport implements
    ServiceRegistryDao {

    public boolean delete(final RegisteredService registeredService) {
        getJpaTemplate().remove(
            getJpaTemplate().contains(registeredService) ? registeredService
                : getJpaTemplate().merge(registeredService));
        return true;
    }

    public List<RegisteredService> load() {
        return getJpaTemplate().find("select r from AbstractRegisteredService r");
    }

    public RegisteredService save(final RegisteredService registeredService) {
        final boolean isNew = registeredService.getId() == -1;

        final RegisteredService r = getJpaTemplate().merge(registeredService);
        
        if (!isNew) {
            getJpaTemplate().persist(r);
        }
        
        return r;
    }

    public RegisteredService findServiceById(final long id) {
        return getJpaTemplate().find(AbstractRegisteredService.class, id);
    }
}
