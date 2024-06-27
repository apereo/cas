package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.NamedObject;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;

/**
 * This is {@link DelegatedClientSessionManager}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public interface DelegatedClientSessionManager extends NamedObject {

    /**
     * Supports the client instance.
     *
     * @param client the client
     * @return true/false
     */
    boolean supports(Client client);
    
    /**
     * Track session id.
     *
     * @param webContext the web context
     * @param ticket     the ticket
     * @param client     the client
     */
    void trackIdentifier(WebContext webContext, TransientSessionTicket ticket, Client client);

    /**
     * Retrieve identifier.
     *
     * @param webContext the web context
     * @param client     the client
     * @return the string
     */
    String retrieveIdentifier(WebContext webContext, Client client);
}
