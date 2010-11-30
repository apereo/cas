/*
 * Copyright 2006 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.adaptors.trusted.authentication.principal;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Principal;
import org.springframework.util.Assert;

/**
 * Credentials that bear the fully resolved and authenticated Principal, or an
 * indication that there is no such Principal. These Credentials are a mechanism
 * to pass into CAS information about an authentication and Principal resolution
 * that has already happened in layers in front of CAS, e.g. by means of a Java
 * Servlet Filter or by means of container authentication in the servlet
 * container or Apache layers. DO NOT accept these Credentials from arbitrary
 * web-servicey calls to CAS. Rather, the code constructing these Credentials
 * must be trusted to perform appropriate authentication before issuing these
 * credentials.
 * 
 * @author Andrew Petro
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
public final class PrincipalBearingCredentials implements Credentials {

    /** Unique id for serialization */
    private static final long serialVersionUID = -3779730112251585974L;

    /** The trusted principal. */
    private final Principal principal;

    public PrincipalBearingCredentials(final Principal principal) {
        Assert.notNull(principal, "principal cannot be null");
        this.principal = principal;
    }

    /**
     * Get the previously authenticated Principal.
     * 
     * @return authenticated Principal
     */
    public Principal getPrincipal() {
        return this.principal;
    }

    public String toString() {
        return "[name: " + this.principal.toString() + "]";
    }
}
