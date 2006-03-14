/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.validation;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.springframework.util.Assert;

/**
 * Default implementation of the Assertion interface which returns the minimum
 * number of attributes required to conform to the CAS 2 protocol.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ImmutableAssertionImpl implements Assertion {

    /** Unique Id for Serialization. */
    private static final long serialVersionUID = -1921502350732798866L;

    /** The list of principals. */
    private final List principals;

    /** Was this the result of a new login. */
    private final boolean fromNewLogin;

    /** The service we are asserting this ticket for. */
    private final Service service;

    /**
     * Constructs a new ImmutableAssertion out of the given parameters.
     * 
     * @param principals the chain of principals
     * @param service The service we are asserting this ticket for.
     * @param fromNewLogin was the service ticket from a new login.
     * @throws IllegalArgumentException if there are no principals.
     */
    public ImmutableAssertionImpl(final List principals, final Service service,
        final boolean fromNewLogin) {
        Assert.notNull(principals, "principals cannot be null");
        Assert.notNull(service, "service cannot be null");
        Assert.notEmpty(principals, "principals cannot be empty");

        this.principals = principals;
        this.service = service;
        this.fromNewLogin = fromNewLogin;
    }

    public Authentication[] getChainedAuthentications() {
        return (Authentication[]) this.principals
            .toArray(new Authentication[0]);
    }

    public boolean isFromNewLogin() {
        return this.fromNewLogin;
    }

    public Service getService() {
        return this.service;
    }

    public boolean equals(final Object o) {
        if (o == null || !this.getClass().equals(o.getClass())) {
            return false;
        }

        return EqualsBuilder.reflectionEquals(this, o);
    }

    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
