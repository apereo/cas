/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.proxy;

import org.jasig.cas.authentication.principal.Credentials;


/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface ProxyHandler {
    
    String handle(Credentials credentials, String proxyGrantingTicketId);
}
