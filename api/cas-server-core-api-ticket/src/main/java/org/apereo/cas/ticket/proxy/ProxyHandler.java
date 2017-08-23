package org.apereo.cas.ticket.proxy;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.ticket.TicketGrantingTicket;

/**
 * Abstraction for what needs to be done to handle proxies. Useful because the
 * generic flow for all authentication is similar the actions taken for proxying
 * are different. One can swap in/out implementations but keep the flow of
 * events the same.
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.0.0
 */
@FunctionalInterface
public interface ProxyHandler {

    /**
     * Method to actually process the proxy request.
     *
     * @param credential The credential of the item that will be proxying.
     * @param proxyGrantingTicketId The ticketId for the PGT (which really is a TGT)
     * @return the String value that needs to be passed to the CAS client.
     */
    String handle(Credential credential, TicketGrantingTicket proxyGrantingTicketId);
    
    /**
     * Whether this handler can support the proxy request identified by the given credentials.
     *
     * @param credential the credential object containing the proxy request details.
     * @return true, if successful
     */
    default boolean canHandle(Credential credential) {
        return true;
    }
}
