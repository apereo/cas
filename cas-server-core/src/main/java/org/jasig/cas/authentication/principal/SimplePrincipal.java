/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.principal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * Simple implementation of a AttributePrincipal that exposes an unmodifiable
 * map of attributes.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.3 $ $Date: 2007/04/19 20:13:01 $
 * @since 3.1
 */
public class SimplePrincipal implements Principal {

    private static final Map<String, Object> EMPTY_MAP = Collections
        .unmodifiableMap(new HashMap<String, Object>());

    /**
     * Unique Id for Serialization.
     */
    private static final long serialVersionUID = -5265620187476296219L;

    /** The unique identifier for the principal. */
    private final String id;

    /** Map of attributes for the Principal. */
    private Map<String, Object> attributes;

    public SimplePrincipal(final String id) {
        this(id, null);
    }

    public SimplePrincipal(final String id, final Map<String, Object> attributes) {
        Assert.notNull(id, "id cannot be null");
        this.id = id;

        this.attributes = attributes == null || attributes.isEmpty()
            ? EMPTY_MAP : Collections.unmodifiableMap(attributes);
    }

    /**
     * Returns an immutable map.
     */
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public String toString() {
        return this.id;
    }

    public int hashCode() {
        return super.hashCode() ^ this.id.hashCode();
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
}
