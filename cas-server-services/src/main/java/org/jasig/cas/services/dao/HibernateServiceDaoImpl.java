/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.dao;

import java.util.List;

import org.jasig.cas.services.domain.RegisteredService;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Implementation of {@link ServiceDao} that utilizes Hibernate to be database
 * agnostic.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class HibernateServiceDaoImpl extends HibernateDaoSupport implements
    ServiceDao {

    public boolean deleteById(final String id) {
        final RegisteredService registeredService = findServiceById(id);

        if (registeredService == null) {
            return false;
        }

        getHibernateTemplate().delete(registeredService);
        return true;
    }

    public RegisteredService findServiceById(final String id) {
        final List list = getHibernateTemplate().find(
            "from Services where id = ?", id);

        if (list.isEmpty()) {
            return null;
        }

        return (RegisteredService) list.get(0);
    }

    public RegisteredService findServiceByUrl(final String url) {
        final List list = getHibernateTemplate().find(
            "from Services where url = ?", url);

        if (list.isEmpty()) {
            return null;
        }

        return (RegisteredService) list.get(0);
    }

    public List getAllServices() {
        return getHibernateTemplate().find("from Services");
    }

    public boolean save(final RegisteredService registeredService) {
        getHibernateTemplate().saveOrUpdate(registeredService);
        return true;
    }
}
