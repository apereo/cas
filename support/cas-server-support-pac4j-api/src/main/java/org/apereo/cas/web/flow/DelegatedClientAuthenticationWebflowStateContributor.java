package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TransientSessionTicket;

import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.Map;

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
     * @throws Exception the exception
     */
    Map<String, ? extends Serializable> store(RequestContext requestContext,
                                    WebContext webContext, Client client) throws Exception;

    /**
     * Restore.
     *
     * @param requestContext the request context
     * @param webContext     the web context
     * @param ticket         the ticket
     * @param client         the client
     * @return the service
     * @throws Exception the exception
     */
    Service restore(RequestContext requestContext, WebContext webContext,
                    TransientSessionTicket ticket, Client client) throws Exception;
}
