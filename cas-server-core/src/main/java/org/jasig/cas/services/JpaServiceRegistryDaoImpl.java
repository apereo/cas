/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.ArrayList;
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
        // XXX return getJpaTemplate().find("select r from RegisteredServiceImpl
        // r");
        return new ArrayList<RegisteredService>();
    }

    public void save(final RegisteredService registeredService) {
        getJpaTemplate().persist(registeredService);
    }
}
