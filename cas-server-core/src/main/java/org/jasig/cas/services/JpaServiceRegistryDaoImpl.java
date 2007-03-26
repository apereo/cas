/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.jpa.support.JpaDaoSupport;

/**
 * Implementation of the ServiceRegistryDao based on JPA.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class JpaServiceRegistryDaoImpl extends JpaDaoSupport implements
    ServiceRegistryDao, InitializingBean {

    public boolean delete(final RegisteredService registeredService) {
        getJpaTemplate().remove(registeredService);
        return true;
    }

    public List<RegisteredService> load() {
        return getJpaTemplate().find("select r from RegisteredServiceImpl r");
    }

    public void save(final RegisteredService registeredService) {
        for (final Attribute a : registeredService.getAllowedAttributes()) {
            if (getJpaTemplate().find(Attribute.class, new Long(a.getId())) == null) {
                getJpaTemplate().persist(a);
            }
        }
        
        final RegisteredService r = findServiceById(registeredService.getId());
        if (r != null) {
            getJpaTemplate().remove(r);
        }

        getJpaTemplate().persist(registeredService);
        
        /*
        if (registeredService.getId() > 0) {
            getJpaTemplate().merge(registeredService);
        } else {
            getJpaTemplate().persist(registeredService);
        }*/
    }

    public RegisteredService findServiceById(final long id) {
        return getJpaTemplate().find(RegisteredServiceImpl.class, new Long(id));
    }
}
