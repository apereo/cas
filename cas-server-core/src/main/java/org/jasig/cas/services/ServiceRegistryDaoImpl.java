/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.orm.jpa.support.JpaDaoSupport;
import org.springframework.util.Assert;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class ServiceRegistryDaoImpl extends JpaDaoSupport implements ServiceRegistryDao,
    InitializingBean {

    private DataFieldMaxValueIncrementer dataFieldMaxValueIncrementer;

    public boolean delete(final RegisteredService registeredService) {
        getJpaTemplate().remove(registeredService);
        return true;
    }

    public List<RegisteredService> load() {
        return getJpaTemplate().find("select r from registeredservice");
    }

    public void save(final RegisteredService registeredService) {
        if (registeredService.getId() == -1) {
            ((RegisteredServiceImpl) registeredService)
                .setId(this.dataFieldMaxValueIncrementer.nextLongValue());
        }
        getJpaTemplate().persist(registeredService);
    }

    public void setDataFieldMaxValueIncrementer(
        final DataFieldMaxValueIncrementer dataFieldMaxValueIncrementer) {
        this.dataFieldMaxValueIncrementer = dataFieldMaxValueIncrementer;
    }

    protected void initDao() throws Exception {
        Assert.notNull(this.dataFieldMaxValueIncrementer,
            "dataFieldMaxValueIncrementer cannot be null.");
    }
}
