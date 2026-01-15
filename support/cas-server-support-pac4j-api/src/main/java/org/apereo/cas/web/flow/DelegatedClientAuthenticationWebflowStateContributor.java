package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DelegatedClientAuthenticationWebflowStateContributor}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface DelegatedClientAuthenticationWebflowStateContributor {

    /**
     * Contribute map.
     *
     * @param requestContext the request context
     * @param webContext     the web context
     * @param client         the client
     * @return the map
     * @throws Throwable the throwable
     */
    Map<String, ? extends Serializable> store(RequestContext requestContext,
                                    WebContext webContext, Client client) throws Throwable;

    /**
     * Restore.
     *
     * @param requestContext the request context
     * @param webContext     the web context
     * @param ticket         the ticket
     * @param client         the client
     * @return the service
     */
    Service restore(RequestContext requestContext, WebContext webContext,
                    Optional<TransientSessionTicket> ticket, Client client);
}
