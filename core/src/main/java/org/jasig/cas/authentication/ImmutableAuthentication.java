/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
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
public final class ImmutableAuthentication implements Authentication {

    /** UID for serializing. */
    private static final long serialVersionUID = 3906647483978365235L;

    /** A Principal object representing the authenticated entity. */
    private final Principal principal; // TODO refactor to be immutable

    /** The date/time this authentication object became valid. */
    private final Date authenticatedDate;

    /** Associated authentication attributes. */
    private final Map attributes;

    /**
     * Constructor that accepts both a principal and a map.
     * 
     * @param principal Principal representing user
     * @param attributes Authentication attributes map.
     * @throws IllegalArgumentException if the principal is null.
     */
    public ImmutableAuthentication(final Principal principal,
        final Map attributes) {

        if (principal == null) {
            throw new IllegalArgumentException("principal cannot be null on "
                + this.getClass().getName());
        }
        this.principal = principal;
        this.attributes = Collections.unmodifiableMap(attributes == null
            ? new HashMap() : attributes);
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

    public Principal getPrincipal() {
        return this.principal;
    }

    public Date getAuthenticatedDate() {
        return new Date(this.authenticatedDate.getTime());
    }

    public Map getAttributes() {
        return this.attributes;
    }

    public boolean equals(final Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
