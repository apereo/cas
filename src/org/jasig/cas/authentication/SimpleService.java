/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The simplest implementation of a representation of a service.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class SimpleService implements Service {

    private String id;

    public SimpleService(String id) {

        if (id == null)
            throw new IllegalArgumentException("ID cannot be null");

        this.id = id;
    }

    /**
     * @see org.jasig.cas.authentication.Service#getName()
     */
    public String getId() {
        return this.id;
    }

    public boolean equals(Object o) {
        if (o == null || !this.getClass().equals(o.getClass()))
            return false;

        return EqualsBuilder.reflectionEquals(this, o);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}