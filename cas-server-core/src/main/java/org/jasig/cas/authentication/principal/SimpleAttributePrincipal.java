/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication.principal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple implementation of a AttributePrincipal that exposes an unmodifiable
 * map of attributes.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class SimpleAttributePrincipal extends SimplePrincipal implements
    AttributePrincipal {
    
    private static final Map<String, Object> EMPTY_MAP = Collections.unmodifiableMap(new HashMap<String, Object>());

    /**
     * Unique Id for Serialization.
     */
    private static final long serialVersionUID = -5265620187476296219L;

    /** Map of attributes for the Principal. */
    // XXX object?
    private Map<String, Object> attributes;

    public SimpleAttributePrincipal(final String id, final Map<String, Object> attributes) {
        super(id);

        this.attributes = attributes == null || attributes.isEmpty()
            ? EMPTY_MAP : Collections.unmodifiableMap(attributes);
    }

    /**
     * Returns an immutable map.
     */
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }
}
