/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import java.util.Date;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jasig.cas.authentication.principal.Principal;

/**
 * Default immutable implementation of Authentication.
 * 
 * @author Dmitriy Kopylenko
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ImmutableAuthentication implements Authentication {

    /** serialVersionID for serializability */
    private static final long serialVersionUID = 3906647483978365235L;

    /** The principal we have authenticated. */
    private final Principal principal;

    /** The Date timestamp that this authentication became valid. */
    private final Date authenticatedDate;

    /** An additional object for holding arbitrary attributes. */
    private final Object attributes;

    public ImmutableAuthentication(final Principal principal,
        final Object attributes) {
        this.principal = principal;
        this.attributes = attributes;
        this.authenticatedDate = new Date();
    }

    public Principal getPrincipal() {
        return this.principal;
    }

    public Date getAuthenticatedDate() {
        return new Date(this.authenticatedDate.getTime());
    }

    public Object getAttributes() {
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
