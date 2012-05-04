/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication;

import java.util.Map;

import org.jasig.cas.authentication.principal.Principal;
import org.springframework.util.Assert;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date: 2007-02-20 09:41:49 -0500 (Tue, 20 Feb
 * 2007) $
 * @since 3.0.3
 */
public abstract class AbstractAuthentication implements Authentication {

    /** A Principal object representing the authenticated entity. */
    private final Principal principal;

    /** Associated authentication attributes. */
    private final Map<String, Object> attributes;

    public AbstractAuthentication(final Principal principal,
        final Map<String, Object> attributes) {
        Assert.notNull(principal, "principal cannot be null");
        Assert.notNull(attributes, "attributes cannot be null");

        this.principal = principal;
        this.attributes = attributes;
    }

    public final Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public final Principal getPrincipal() {
        return this.principal;
    }

    public final boolean equals(final Object o) {
        if (o == null || !this.getClass().isAssignableFrom(o.getClass())) {
            return false;
        }

        Authentication a = (Authentication) o;

        return this.principal.equals(a.getPrincipal())
            && this.getAuthenticatedDate().equals(a.getAuthenticatedDate()) && this.attributes.equals(a.getAttributes());
    }

    public final int hashCode() {
        return 49 * this.principal.hashCode()
            ^ this.getAuthenticatedDate().hashCode();
    }

    public final String toString() {
        return "[Principal=" + this.principal.getId() + ", attributes="
            + this.attributes.toString() + "]";
    }
}
