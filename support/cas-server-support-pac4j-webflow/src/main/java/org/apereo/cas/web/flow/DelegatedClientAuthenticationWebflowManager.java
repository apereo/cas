package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TransientSessionTicket;

import org.pac4j.core.client.Client;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DelegatedClientAuthenticationWebflowManager}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface DelegatedClientAuthenticationWebflowManager {
    /**
     * Client identifier associated with this session/request.
     */
    String PARAMETER_CLIENT_ID = "delegatedclientid";

    /**
     * Default implementation bean name.
     */
    String DEFAULT_BEAN_NAME = "delegatedClientWebflowManager";

    /**
     * Store.
     *
     * @param webContext the web context
     * @param client     the client
     * @return the transient session ticket
     * @throws Exception the exception
     */
    TransientSessionTicket store(JEEContext webContext, Client client) throws Exception;

    /**
     * Retrieve.
     *
     * @param requestContext the request context
     * @param webContext     the web context
     * @param client         the client
     * @return the service
     * @throws Exception the exception
     */
    Service retrieve(RequestContext requestContext, WebContext webContext, Client client) throws Exception;
}
