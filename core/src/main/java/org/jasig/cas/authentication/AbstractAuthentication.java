/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jasig.cas.authentication.principal.Principal;
import org.springframework.util.Assert;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.3
 */
public abstract class AbstractAuthentication implements Authentication {

    /** A Principal object representing the authenticated entity. */
    private final Principal principal;

    /** Associated authentication attributes. */
    private final Map attributes;

    public AbstractAuthentication(final Principal principal,
        final Map attributes) {
        Assert.notNull(principal, "principal cannot be null");
        Assert.notNull(attributes, "attributes cannot be null");

        this.principal = principal;
        this.attributes = attributes;
    }

    public final Map getAttributes() {
        return this.attributes;
    }

    public final Principal getPrincipal() {
        return this.principal;
    }

    public final boolean equals(final Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    public final int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public final String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
