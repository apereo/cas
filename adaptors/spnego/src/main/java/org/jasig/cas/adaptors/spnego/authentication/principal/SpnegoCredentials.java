/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.spnego.authentication.principal;

import org.ietf.jgss.GSSContext;
import org.jasig.cas.authentication.principal.Credentials;
import org.springframework.util.Assert;

/**
 * Credentials that are a holder for GSSContext.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
public final class SpnegoCredentials implements Credentials {

    /**
     * Unique id for serialization
     */
    private static final long serialVersionUID = -3606956950223931438L;

    /** The GSSContex containing the user. */
    private final GSSContext context;

    public SpnegoCredentials(final GSSContext context) {
        Assert.notNull(context, "The GSScontext cannot be null.");
        this.context = context;
    }

    public GSSContext getContext() {
        return this.context;
    }
}
