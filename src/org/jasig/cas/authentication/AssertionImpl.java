/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Default implementation of the Assertion interface which returns the minimum number of attributes required to conform to the CAS 2 protocol.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class AssertionImpl implements Assertion {

    final private List principals;

    final private boolean fromNewLogin;

    public AssertionImpl(final List principals, boolean fromNewLogin) {
        if (principals == null || principals.isEmpty()) {
            throw new IllegalArgumentException("principals cannot be null or empty.");
        }

        this.principals = principals;
        this.fromNewLogin = fromNewLogin;
    }

    /**
     * @see org.jasig.cas.authentication.Assertion#getChainedPrincipals()
     */
    public List getChainedPrincipals() {
        return this.principals;
    }

    /**
     * @see org.jasig.cas.authentication.Assertion#isFromNewLogin()
     */
    public boolean isFromNewLogin() {
        return this.fromNewLogin;
    }

    public boolean equals(Object o) {
        if (o == null || !this.getClass().equals(o.getClass()))
            return false;

        return EqualsBuilder.reflectionEquals(this, o);
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
