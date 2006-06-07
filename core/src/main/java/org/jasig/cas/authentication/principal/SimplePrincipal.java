/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import org.springframework.util.Assert;

/**
 * Simplest implementation of a Principal. Provides no additional attributes
 * beyond those in the Principal interface. This is the closest representation
 * of a principal in the CAS 2 world.
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

    /**
     * Constructs the SimplePrincipal using the provided unique id.
     * 
     * @param id the identifier for the Principal
     * @throws IllegalArgumentException if the id is null
     */
    public SimplePrincipal(final String id) {
        Assert.notNull(id, "id cannot be null");
        this.id = id;
    }

    public final String getId() {
        return this.id;
    }

    public boolean equals(final Object o) {
        if (o == null || !this.getClass().equals(o.getClass())) {
            return false;
        }
        
        final SimplePrincipal p = (SimplePrincipal) o;
        
        return this.id.equals(p.getId());
    }

    public String toString() {
        return this.id;
    }

    public int hashCode() {
        return super.hashCode() ^ this.id.hashCode();
    }
}
