package org.apereo.cas.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.DelegatedAuthenticationRequestTicket;
import org.apereo.cas.ticket.DelegatedAuthenticationRequestTicketFactory;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.saml.client.SAML2Client;
import org.springframework.webflow.execution.RequestContext;

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
public class DelegatedClientWebflowManager {
    /**
     * Client identifier associated with this session/request.
     */
    public static final String PARAMETER_CLIENT_ID = "delegatedclientid";

    private final TicketRegistry ticketRegistry;
    private final DelegatedAuthenticationRequestTicketFactory delegatedAuthenticationRequestTicketFactory;
    private final String themeParamName;
    private final String localParamName;
    private final ServiceFactory<WebApplicationService> webApplicationServiceFactory;
    private final String casLoginEndpoint;
    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;


    /**
     * Store.
     *
     * @param webContext the web context
     * @return the ticket
     */
    public Ticket store(final WebContext webContext) {
        final Map<String, Object> properties = new LinkedHashMap<>();

        final String serviceParameter =
            StringUtils.defaultIfBlank(webContext.getRequestParameter(CasProtocolConstants.PARAMETER_SERVICE), casLoginEndpoint);
        final Service service = this.authenticationRequestServiceSelectionStrategies.resolveService(webApplicationServiceFactory.createService(serviceParameter));
        properties.put(CasProtocolConstants.PARAMETER_SERVICE, service);

        properties.put(this.themeParamName, StringUtils.defaultString(webContext.getRequestParameter(this.themeParamName)));
        properties.put(this.localParamName, StringUtils.defaultString(webContext.getRequestParameter(this.localParamName)));
        properties.put(CasProtocolConstants.PARAMETER_METHOD,
            StringUtils.defaultString(webContext.getRequestParameter(CasProtocolConstants.PARAMETER_METHOD)));

        final DelegatedAuthenticationRequestTicket ticket = this.delegatedAuthenticationRequestTicketFactory.create(service, properties);
        LOGGER.debug("Storing delegated authentication request ticket [{}] for service [{}] with properties [{}]",
            ticket.getId(), ticket.getService(), ticket.getProperties());
        this.ticketRegistry.addTicket(ticket);
        webContext.setRequestAttribute(PARAMETER_CLIENT_ID, ticket.getId());
        return ticket;
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
        final DelegatedAuthenticationRequestTicket ticket = this.ticketRegistry.getTicket(clientId, DelegatedAuthenticationRequestTicket.class);
        if (ticket == null) {
            LOGGER.error("Delegated client identifier cannot be located in the authentication request");
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
        if (ticket.isExpired()) {
            LOGGER.error("Delegated client identifier [{}] has expired in the authentication request", ticket.getId());
            this.ticketRegistry.deleteTicket(ticket.getId());
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
        LOGGER.debug("Located delegated client identifier as [{}]", ticket.getId());
        restoreDelegatedAuthenticationRequest(requestContext, webContext, ticket);
        LOGGER.debug("Removing delegated client identifier [{}} from registry", ticket.getId());
        this.ticketRegistry.deleteTicket(ticket.getId());
        return ticket.getService();
    }

    private Service restoreDelegatedAuthenticationRequest(final RequestContext requestContext, final WebContext webContext, final DelegatedAuthenticationRequestTicket ticket) {
        final Service service = ticket.getService();
        LOGGER.debug("Restoring requested service [{}] back in the authentication flow", service);

        requestContext.getFlowScope().put(CasProtocolConstants.PARAMETER_SERVICE, service);
        webContext.setRequestAttribute(CasProtocolConstants.PARAMETER_SERVICE, service);
        webContext.setRequestAttribute(this.themeParamName, ticket.getProperties().get(this.themeParamName));
        webContext.setRequestAttribute(this.localParamName, ticket.getProperties().get(this.localParamName));
        webContext.setRequestAttribute(CasProtocolConstants.PARAMETER_METHOD, ticket.getProperties().get(CasProtocolConstants.PARAMETER_METHOD));
        return service;
    }

    private String getDelegatedClientId(final WebContext webContext, final BaseClient client) {
        String clientId = webContext.getRequestParameter(PARAMETER_CLIENT_ID);
        if (StringUtils.isBlank(clientId) && client instanceof SAML2Client) {
            LOGGER.debug("Client identifier could not found as part of the request parameters. Looking at relay-state for the SAML2 client");
            clientId = webContext.getRequestParameter("RelayState");
        }
        LOGGER.debug("Located delegated client identifier for this request as [{}]", clientId);
        return clientId;
    }
}
