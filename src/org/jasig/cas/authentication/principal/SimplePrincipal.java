/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Simplest implementation of a Principal.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class SimplePrincipal implements Principal {

    final private String id;

    public SimplePrincipal(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("id is a required parameter.");
        }
        this.id = id;
    }

    /**
     * @see org.jasig.cas.authentication.principal.Principal#getId()
     */
    public String getId() {
        return this.id;
    }

    public boolean equals(Object o) {
        if (o == null || !this.getClass().equals(o.getClass())) {
            return false;
        }

        return EqualsBuilder.reflectionEquals(this, o);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}