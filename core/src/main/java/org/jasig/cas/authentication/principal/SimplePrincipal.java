/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Simplest implementation of a Principal.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class SimplePrincipal implements Principal {

    /** Unique ID for serialization. */
    private static final long serialVersionUID = 3977857358779396149L;

    /** The unique identifier for the principal. */
    private final String id;

    public SimplePrincipal(final String id) {
        if (id == null) {
            throw new IllegalArgumentException("id is a required parameter.");
        }
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

    public final String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public final int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
