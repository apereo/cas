package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.oauth.client.OAuth10Client;
import org.pac4j.oauth.client.OAuth20Client;
import org.pac4j.oauth.config.OAuth20Configuration;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.state.SAML2StateGenerator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link DelegatedClientWebflowManager}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Slf4j
@Transactional(transactionManager = "ticketTransactionManager")
public class DelegatedClientWebflowManager {
    /**
     * Client identifier associated with this session/request.
     */
    public static final String PARAMETER_CLIENT_ID = "delegatedclientid";
    private static final String OAUTH10_CLIENT_ID_SESSION_KEY = "OAUTH10_CLIENT_ID";
    private static final String CAS_CLIENT_ID_SESSION_KEY = "CAS_CLIENT_ID";

    private final TicketRegistry ticketRegistry;
    private final TicketFactory ticketFactory;
    private final String themeParamName;
    private final String localParamName;
    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;
    private final ArgumentExtractor argumentExtractor;

    /**
     * Store.
     *
     * @param webContext the web context
     * @param client     the client
     * @return the ticket
     */
    public Ticket store(final J2EContext webContext, final BaseClient client) {
        final Map<String, Serializable> properties = buildTicketProperties(webContext);

        final Service originalService = argumentExtractor.extractService(webContext.getRequest());
        final Service service = authenticationRequestServiceSelectionStrategies.resolveService(originalService);
        properties.put(CasProtocolConstants.PARAMETER_SERVICE, originalService);
        properties.put(CasProtocolConstants.PARAMETER_TARGET_SERVICE, service);

        final TransientSessionTicketFactory transientFactory = (TransientSessionTicketFactory) this.ticketFactory.get(TransientSessionTicket.class);
        final TransientSessionTicket ticket = transientFactory.create(originalService, properties);
        final String ticketId = ticket.getId();
        LOGGER.debug("Storing delegated authentication request ticket [{}] for service [{}] with properties [{}]",
            ticketId, ticket.getService(), ticket.getProperties());
        this.ticketRegistry.addTicket(ticket);
        webContext.setRequestAttribute(PARAMETER_CLIENT_ID, ticketId);

        if (client instanceof SAML2Client) {
            webContext.getSessionStore().set(webContext, SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE, ticketId);
        }
        if (client instanceof OAuth20Client) {
            final OAuth20Client oauthClient = (OAuth20Client) client;
            final OAuth20Configuration config = oauthClient.getConfiguration();
            config.setWithState(true);
            config.setStateData(ticketId);
        }
        if (client instanceof OidcClient) {
            final OidcClient oidcClient = (OidcClient) client;
            final OidcConfiguration config = oidcClient.getConfiguration();
            config.setWithState(true);
            config.setStateData(ticketId);
        }
        if (client instanceof CasClient) {
            webContext.getSessionStore().set(webContext, CAS_CLIENT_ID_SESSION_KEY, ticket.getId());
        }
        if (client instanceof OAuth10Client) {
            webContext.getSessionStore().set(webContext, OAUTH10_CLIENT_ID_SESSION_KEY, ticket.getId());
        }
        return ticket;
    }

    /**
     * Build the ticket properties.
     *
     * @param webContext the web context
     * @return the ticket properties
     */
    protected Map<String, Serializable> buildTicketProperties(final J2EContext webContext) {
        final Map<String, Serializable> properties = new LinkedHashMap<>();

        properties.put(this.themeParamName, StringUtils.defaultString(webContext.getRequestParameter(this.themeParamName)));
        properties.put(this.localParamName, StringUtils.defaultString(webContext.getRequestParameter(this.localParamName)));
        properties.put(CasProtocolConstants.PARAMETER_METHOD,
            StringUtils.defaultString(webContext.getRequestParameter(CasProtocolConstants.PARAMETER_METHOD)));

        return properties;
    }

    /**
     * Retrieve service.
     *
     * @param requestContext the request context
     * @param webContext     the web context
     * @param client         the client
     * @return the service
     */
    public Service retrieve(final RequestContext requestContext, final WebContext webContext, final BaseClient client) {
        final String clientId = getDelegatedClientId(webContext, client);
        final TransientSessionTicket ticket = retrieveSessionTicketViaClientId(webContext, clientId);
        restoreDelegatedAuthenticationRequest(requestContext, webContext, ticket);
        LOGGER.debug("Removing delegated client identifier [{}} from registry", ticket.getId());
        this.ticketRegistry.deleteTicket(ticket.getId());
        return ticket.getService();
    }

    /**
     * Retrieve session ticket via client id.
     *
     * @param webContext the web context
     * @param clientId   the client id
     * @return the transient session ticket
     */
    protected TransientSessionTicket retrieveSessionTicketViaClientId(final WebContext webContext, final String clientId) {
        final TransientSessionTicket ticket = this.ticketRegistry.getTicket(clientId, TransientSessionTicket.class);
        if (ticket == null) {
            LOGGER.error("Delegated client identifier cannot be located in the authentication request [{}]", webContext.getFullRequestURL());
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
        if (ticket.isExpired()) {
            LOGGER.error("Delegated client identifier [{}] has expired in the authentication request", ticket.getId());
            this.ticketRegistry.deleteTicket(ticket.getId());
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
        LOGGER.debug("Located delegated client identifier as [{}]", ticket.getId());
        return ticket;
    }

    /**
     * Restore the information saved in the ticket and return the service.
     *
     * @param requestContext the request context
     * @param webContext     the web context
     * @param ticket         the ticket
     * @return the service
     */
    protected Service restoreDelegatedAuthenticationRequest(final RequestContext requestContext, final WebContext webContext,
                                                            final TransientSessionTicket ticket) {
        final Service service = ticket.getService();
        LOGGER.debug("Restoring requested service [{}] back in the authentication flow", service);

        WebUtils.putService(requestContext, service);
        webContext.setRequestAttribute(CasProtocolConstants.PARAMETER_SERVICE, service);
        webContext.setRequestAttribute(this.themeParamName, ticket.getProperties().get(this.themeParamName));
        webContext.setRequestAttribute(this.localParamName, ticket.getProperties().get(this.localParamName));
        webContext.setRequestAttribute(CasProtocolConstants.PARAMETER_METHOD, ticket.getProperties().get(CasProtocolConstants.PARAMETER_METHOD));
        return service;
    }

    /**
     * Gets delegated client id.
     *
     * @param webContext the web context
     * @param client     the client
     * @return the delegated client id
     */
    protected String getDelegatedClientId(final WebContext webContext, final BaseClient client) {
        String clientId = webContext.getRequestParameter(PARAMETER_CLIENT_ID);
        if (StringUtils.isBlank(clientId)) {
            if (client instanceof SAML2Client) {
                LOGGER.debug("Client identifier could not found as part of the request parameters. Looking at relay-state for the SAML2 client");
                clientId = webContext.getRequestParameter("RelayState");
            }
            if (client instanceof OAuth20Client || client instanceof OidcClient) {
                LOGGER.debug("Client identifier could not found as part of the request parameters. Looking at state for the OAuth2/Oidc client");
                clientId = webContext.getRequestParameter(OAuth20Configuration.STATE_REQUEST_PARAMETER);
            }
            if (client instanceof OAuth10Client) {
                LOGGER.debug("Client identifier could not be found as part of request parameters.  Looking at state for the OAuth1 client");
                final SessionStore sessionStore = webContext.getSessionStore();
                clientId = (String) sessionStore.get(webContext, OAUTH10_CLIENT_ID_SESSION_KEY);
                sessionStore.set(webContext, OAUTH10_CLIENT_ID_SESSION_KEY, null);
            }
            if (client instanceof CasClient) {
                LOGGER.debug("Client identifier could not be found as part of request parameters.  Looking at state for the CAS client");
                final SessionStore sessionStore = webContext.getSessionStore();
                clientId = (String) sessionStore.get(webContext, CAS_CLIENT_ID_SESSION_KEY);
                sessionStore.set(webContext, CAS_CLIENT_ID_SESSION_KEY, null);
            }
        }
        LOGGER.debug("Located delegated client identifier for this request as [{}]", clientId);
        return clientId;
    }
}
