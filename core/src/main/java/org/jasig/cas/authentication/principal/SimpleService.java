/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.springframework.util.Assert;

/**
 * The simplest implementation of a representation of a service. Provides no
 * additional attributes beyond those in the Service interface.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class SimpleService implements Service {

    /** ID for serializing. */
    private static final long serialVersionUID = 3258129150454936116L;

    /** Unique identifier for this service. */
    private String id;

    /**
     * Constructs a new SimpleService using the provided unique id for the
     * service.
     * 
     * @param id the identifier for the service.
     * @throws IllegalArgumentException if the ID is null
     */
    public SimpleService(final String id) {
        Assert.notNull(id, "id cannot be null");

        this.id = id;
    }

    public final String getId() {
        return this.id;
    }

    public final boolean equals(final Object o) {
        if (o == null || !this.getClass().equals(o.getClass())) {
            return false;
        }

        return EqualsBuilder.reflectionEquals(this, o);
    }

    public final int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public final String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
