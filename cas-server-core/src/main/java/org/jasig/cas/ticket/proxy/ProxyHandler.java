/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket.proxy;

import org.jasig.cas.authentication.principal.Credentials;

/**
 * Abstraction for what needs to be done to handle proxies. Useful because the
 * generic flow for all authentication is similar the actions taken for proxying
 * are different. One can swap in/out implementations but keep the flow of
 * events the same.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public interface ProxyHandler {

    /**
     * Method to actually process the proxy request.
     * 
     * @param credentials The credentials of the item that will be proxying.
     * @param proxyGrantingTicketId The ticketId for the ProxyGrantingTicket (in
     * CAS 3 this is a TicketGrantingTicket)
     * @return the String value that needs to be passed to the CAS client.
     */
    String handle(Credentials credentials, String proxyGrantingTicketId);
}
