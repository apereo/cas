/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.authentication;

import java.util.Date;
import java.util.HashMap;

import org.jasig.cas.authentication.principal.Principal;

/**
 * Mutable implementation of Authentication interface.
 * <p>
 * Instanciators of the MutableAuthentication class must take care that the map
 * they provide is serializable (i.e. HashMap).
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.3
 */
public final class MutableAuthentication extends AbstractAuthentication {

    /** Unique Id for serialization. */
    private static final long serialVersionUID = -4415875344376642246L;

    /** The date/time this authentication object became valid. */
    private final Date authenticatedDate;

    public MutableAuthentication(final Principal principal) {
        this(principal, new Date());
    }
    
    public MutableAuthentication(final Principal principal, final Date date) {
        super(principal, new HashMap<String, Object>());
        this.authenticatedDate = date;
    }

    public Date getAuthenticatedDate() {
        return this.authenticatedDate;
    }
}
