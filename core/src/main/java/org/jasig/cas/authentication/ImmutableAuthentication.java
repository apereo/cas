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
 * Default implementation of Authentication.
 * 
 * <p> Immutable (no setters, all contents must be specified at construction)</p>
 * 
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ImmutableAuthentication implements Authentication {

    /** UID for serializing. */
    private static final long serialVersionUID = 3906647483978365235L;

    /** The principal this authentication object is valid for. */
    private final Principal principal; // TODO refactor to be an immutable

    // proxy?

    /** The date/time this authentication object became valid. */
    private final Date authenticatedDate;

    /** An arbitrary object to hold additional attributes. */
    private final Map attributes;

    protected ImmutableAuthentication(final Principal principal,
        final Map attributes) {
        this.principal = principal;
        this.attributes = Collections
            .unmodifiableMap(attributes == null ? new HashMap() : attributes);
        this.authenticatedDate = new Date();
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
