/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.authentication.principal.Principal;

/**
 * Default implementation of Authentication interface. ImmutableAuthentication
 * is an immutable object and thus its attributes cannot be changed.
 * <p>
 * Instanciators of the ImmutableAuthentication class must take care that the
 * map they provide is serializable (i.e. HashMap).
 * 
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ImmutableAuthentication extends AbstractAuthentication {

    /** UID for serializing. */
    private static final long serialVersionUID = 3906647483978365235L;
    
    private static final Map<String, Object> EMPTY_MAP = Collections.unmodifiableMap(new HashMap<String, Object>());

    /** The date/time this authentication object became valid. */
    final Date authenticatedDate;

    /**
     * Constructor that accepts both a principal and a map.
     * 
     * @param principal Principal representing user
     * @param attributes Authentication attributes map.
     * @throws IllegalArgumentException if the principal is null.
     */
    public ImmutableAuthentication(final Principal principal,
        final Map<String, Object> attributes) {
        super(principal, attributes == null || attributes.isEmpty()
            ? EMPTY_MAP : Collections.unmodifiableMap(attributes));

        this.authenticatedDate = new Date();
    }

    /**
     * Constructor that assumes there are no additional authentication
     * attributes.
     * 
     * @param principal the Principal representing the authenticated entity.
     */
    public ImmutableAuthentication(final Principal principal) {
        this(principal, null);
    }

    public Date getAuthenticatedDate() {
        return new Date(this.authenticatedDate.getTime());
    }
}
