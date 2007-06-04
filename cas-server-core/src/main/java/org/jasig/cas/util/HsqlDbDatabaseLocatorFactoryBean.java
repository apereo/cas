/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class HsqlDbDatabaseLocatorFactoryBean implements FactoryBean {

    private Resource configLocation;

    private String urlRepresentation;

    public void setConfigLocation(final Resource configLocation) {
        this.configLocation = configLocation;
    }

    public Object getObject() {
        if (this.urlRepresentation == null) {
            try {
                this.urlRepresentation = this.configLocation.getURL()
                    .toExternalForm();
            } catch (final Exception e) {
                return null;
            }
        }

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