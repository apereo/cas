/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.proxy.support;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.ticket.proxy.ProxyHandler;


/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public class Cas10ProxyHandler implements ProxyHandler {

    /**
     * @see org.jasig.cas.ticket.proxy.ProxyHandler#handle(org.jasig.cas.authentication.principal.Credentials, java.lang.String)
     */
    public String handle(Credentials credentials, String proxyGrantingTicketId) {
        return null;
    }

}
