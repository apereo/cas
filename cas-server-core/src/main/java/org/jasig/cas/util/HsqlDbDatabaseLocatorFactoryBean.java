/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

/**
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class HsqlDbDatabaseLocatorFactoryBean implements FactoryBean,
    InitializingBean {

    private Resource configLocation;

    private String urlRepresentation;

    public void setConfigLocation(Resource configLocation) {
        this.configLocation = configLocation;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.configLocation, "configLocation cannot be null.");

        this.urlRepresentation = this.configLocation.getURL().toExternalForm();
    }

    public Object getObject() {
        return "jdbc:hsqldb:"
            + this.urlRepresentation.substring(0, this.urlRepresentation
                .length() - 7) + ";shutdown=true";
    }

    public Class getObjectType() {
        return String.class;
    }

    public boolean isSingleton() {
        return true;
    }
}