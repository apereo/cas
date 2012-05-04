/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket.proxy.support;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.ticket.proxy.ProxyHandler;

/**
 * Dummy ProxyHandler that does nothing. Useful for Cas 1.0 compliance as CAS
 * 1.0 has no proxying capabilities.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class Cas10ProxyHandler implements ProxyHandler {

    public String handle(final Credentials credentials,
        final String proxyGrantingTicketId) {
        return null;
    }

}
