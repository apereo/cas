package org.apereo.cas.web;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.redirect.RedirectionActionBuilder;
import org.pac4j.core.util.generator.StaticValueGenerator;
import org.pac4j.oauth.client.OAuth10Client;
import org.pac4j.oauth.client.OAuth20Client;
import org.pac4j.oauth.config.OAuth20Configuration;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.state.SAML2StateGenerator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link DefaultDelegatedClientAuthenticationWebflowManager}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Slf4j
@Getter
@Transactional(transactionManager = "ticketTransactionManager")
public class DefaultDelegatedClientAuthenticationWebflowManager implements DelegatedClientAuthenticationWebflowManager {
    private static final String OAUTH10_CLIENT_ID_SESSION_KEY = "OAUTH10_CLIENT_ID";

    private static final String CAS_CLIENT_ID_SESSION_KEY = "CAS_CLIENT_ID";

    private final DelegatedClientAuthenticationConfigurationContext configContext;

    @Override
    public TransientSessionTicket store(final JEEContext webContext, final Client client) {
        val ticket = storeDelegatedClientAuthenticationRequest(webContext);
        rememberSelectedClientIfNecessary(webContext, client);

        if (client instanceof SAML2Client) {
            trackSessionIdForSAML2Client(webContext, ticket, (SAML2Client) client);
        }
        if (client instanceof OAuth20Client) {
            trackSessionIdForOAuth20Client(webContext, (OAuth20Client) client, ticket);
        }
        if (client instanceof OidcClient) {
            trackSessionIdForOidcClient(webContext, (OidcClient) client, ticket);
        }
        if (client instanceof CasClient) {
            trackSessionIdForCasClient(webContext, ticket, (CasClient) client);
        }
        if (client instanceof OAuth10Client) {
            trackSessionIdForOAuth10Client(webContext, ticket);
        }
        return ticket;
    }

    @Override
    public Service retrieve(final RequestContext requestContext, final WebContext webContext, final Client client) {
        val clientId = getDelegatedClientId(webContext, client);
        val ticket = retrieveSessionTicketViaClientId(webContext, clientId);
        restoreDelegatedAuthenticationRequest(requestContext, webContext, ticket);
        LOGGER.debug("Removing delegated client identifier [{}] from registry", ticket.getId());
        configContext.getCentralAuthenticationService().deleteTicket(ticket.getId());
        return ticket.getService();

    }

    /**
     * Track session id for oauth10 client.
     *
     * @param webContext the web context
     * @param ticket     the ticket
     */
    protected void trackSessionIdForOAuth10Client(final WebContext webContext,
                                                  final TransientSessionTicket ticket) {
        configContext.getSessionStore().set(webContext, OAUTH10_CLIENT_ID_SESSION_KEY, ticket.getId());
    }

    /**
     * Track session id for cas client.
     *
     * @param webContext the web context
     * @param ticket     the ticket
     * @param casClient  the cas client
     */
    protected void trackSessionIdForCasClient(final WebContext webContext, final TransientSessionTicket ticket,
                                              final CasClient casClient) {
        casClient.getConfiguration().addCustomParam(PARAMETER_CLIENT_ID, ticket.getId());
        configContext.getSessionStore().set(webContext, CAS_CLIENT_ID_SESSION_KEY, ticket.getId());
    }

    /**
     * Track session id for oidc client.
     *
     * @param webContext the web context
     * @param client     the client
     * @param ticket     the ticket
     */
    protected void trackSessionIdForOidcClient(final WebContext webContext, final OidcClient client,
                                               final TransientSessionTicket ticket) {
        val config = client.getConfiguration();
        config.setWithState(true);
        config.setStateGenerator(new StaticValueGenerator(ticket.getId()));
    }

    /**
     * Track session id for oauth20 client.
     *
     * @param webContext the web context
     * @param client     the client
     * @param ticket     the ticket
     */
    protected void trackSessionIdForOAuth20Client(final WebContext webContext,
                                                  final OAuth20Client client,
                                                  final TransientSessionTicket ticket) {
        val config = client.getConfiguration();
        config.setWithState(true);
        config.setStateGenerator(new StaticValueGenerator(ticket.getId()));
    }

    /**
     * Track session id for client.
     *
     * @param webContext  the web context
     * @param ticket      the ticket
     * @param saml2Client the saml 2 client
     */
    protected void trackSessionIdForSAML2Client(final WebContext webContext,
                                                final TransientSessionTicket ticket,
                                                final SAML2Client saml2Client) {
        configContext.getSessionStore().set(webContext,
            SAML2StateGenerator.SAML_RELAY_STATE_ATTRIBUTE, ticket.getId());
    }

    /**
     * Store delegated client authentication request.
     *
     * @param webContext the web context
     * @return the transient session ticket
     */
    protected TransientSessionTicket storeDelegatedClientAuthenticationRequest(final JEEContext webContext) {
        val properties = buildTicketProperties(webContext);
        val originalService = configContext.getArgumentExtractor().extractService(webContext.getNativeRequest());
        val service = configContext.getAuthenticationRequestServiceSelectionStrategies().resolveService(originalService);
        properties.put(CasProtocolConstants.PARAMETER_SERVICE, originalService);
        properties.put(CasProtocolConstants.PARAMETER_TARGET_SERVICE, service);

        val registeredService = configContext.getServicesManager().findServiceBy(service);
        webContext.getRequestParameter(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN)
            .or(() -> Optional.of(Boolean.toString(RegisteredServiceProperties.DELEGATED_AUTHN_FORCE_AUTHN.isAssignedTo(registeredService))))
            .filter(value -> StringUtils.equalsIgnoreCase(value, "true"))
            .ifPresent(attr -> properties.put(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN, true));
        webContext.getRequestParameter(RedirectionActionBuilder.ATTRIBUTE_PASSIVE)
            .or(() -> Optional.of(Boolean.toString(RegisteredServiceProperties.DELEGATED_AUTHN_PASSIVE_AUTHN.isAssignedTo(registeredService))))
            .filter(value -> StringUtils.equalsIgnoreCase(value, "true"))
            .ifPresent(attr -> properties.put(RedirectionActionBuilder.ATTRIBUTE_PASSIVE, true));

        val transientFactory = (TransientSessionTicketFactory) configContext.getTicketFactory().get(TransientSessionTicket.class);
        val ticket = transientFactory.create(originalService, properties);

        LOGGER.debug("Storing delegated authentication request ticket [{}] for service [{}] with properties [{}]",
            ticket.getId(), ticket.getService(), ticket.getProperties());
        configContext.getCentralAuthenticationService().addTicket(ticket);
        webContext.setRequestAttribute(PARAMETER_CLIENT_ID, ticket.getId());

        if (properties.containsKey(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN)) {
            webContext.setRequestAttribute(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN, true);
        }
        if (properties.containsKey(RedirectionActionBuilder.ATTRIBUTE_PASSIVE)) {
            webContext.setRequestAttribute(RedirectionActionBuilder.ATTRIBUTE_PASSIVE, true);
        }
        return ticket;
    }

    /**
     * Remember selected client if necessary.
     *
     * @param webContext the web context
     * @param client     the client
     */
    protected void rememberSelectedClientIfNecessary(final JEEContext webContext, final Client client) {
        val cookieProps = configContext.getCasProperties().getAuthn().getPac4j().getCookie();
        if (cookieProps.isEnabled()) {
            if (cookieProps.isAutoConfigureCookiePath()) {
                val contextPath = webContext.getNativeRequest().getContextPath();
                val cookiePath = StringUtils.isNotBlank(contextPath) ? contextPath + '/' : "/";
                val path = configContext.getDelegatedClientCookieGenerator().getCookiePath();
                if (StringUtils.isBlank(path)) {
                    LOGGER.debug("Setting path for cookies for delegated authentication cookie generator to: [{}]", cookiePath);
                    configContext.getDelegatedClientCookieGenerator().setCookiePath(cookiePath);
                }
            }
            configContext.getDelegatedClientCookieGenerator().addCookie(webContext.getNativeRequest(),
                webContext.getNativeResponse(), client.getName());
        }
    }

    /**
     * Build the ticket properties.
     *
     * @param webContext the web context
     * @return the ticket properties
     */
    protected Map<String, Serializable> buildTicketProperties(final WebContext webContext) {
        val properties = new HashMap<String, Serializable>();

        val themeParamName = configContext.getCasProperties().getTheme().getParamName();
        val localParamName = configContext.getCasProperties().getLocale().getParamName();

        properties.put(themeParamName, webContext.getRequestParameter(themeParamName)
            .map(String::valueOf).orElse(StringUtils.EMPTY));
        properties.put(localParamName, webContext.getRequestParameter(localParamName)
            .map(String::valueOf).orElse(StringUtils.EMPTY));
        properties.put(CasProtocolConstants.PARAMETER_METHOD,
            webContext.getRequestParameter(CasProtocolConstants.PARAMETER_METHOD)
                .map(String::valueOf).orElse(StringUtils.EMPTY));
        LOGGER.debug("Built ticket properties [{}]", properties);
        return properties;
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
        val service = ticket.getService();
        LOGGER.trace("Restoring requested service [{}] back in the authentication flow", service);

        WebUtils.putServiceIntoFlowScope(requestContext, service);
        webContext.setRequestAttribute(CasProtocolConstants.PARAMETER_SERVICE, service);

        val themeParamName = configContext.getCasProperties().getTheme().getParamName();
        val localParamName = configContext.getCasProperties().getLocale().getParamName();

        val properties = ticket.getProperties();
        webContext.setRequestAttribute(themeParamName, properties.get(themeParamName));
        webContext.setRequestAttribute(localParamName, properties.get(localParamName));
        webContext.setRequestAttribute(CasProtocolConstants.PARAMETER_METHOD, properties.get(CasProtocolConstants.PARAMETER_METHOD));
        return service;
    }

    /**
     * Retrieve session ticket via client id.
     *
     * @param webContext the web context
     * @param clientId   the client id
     * @return the transient session ticket
     */
    protected TransientSessionTicket retrieveSessionTicketViaClientId(final WebContext webContext, final String clientId) {
        try {
            val ticket = configContext.getCentralAuthenticationService().getTicket(clientId, TransientSessionTicket.class);
            LOGGER.debug("Located delegated authentication client identifier as [{}]", ticket.getId());
            return ticket;
        } catch (final Exception e) {
            LOGGER.error("Delegated client identifier cannot be located in the authentication request [{}]", webContext.getFullRequestURL());
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }
    }

    /**
     * Gets delegated client id.
     *
     * @param webContext the web context
     * @param client     the client
     * @return the delegated client id
     */
    protected String getDelegatedClientId(final WebContext webContext, final Client client) {
        var clientId = webContext.getRequestParameter(PARAMETER_CLIENT_ID)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        if (StringUtils.isBlank(clientId)) {
            if (client instanceof SAML2Client) {
                LOGGER.debug("Client identifier could not found in request parameters. Looking at relay-state for the SAML2 client");
                clientId = webContext.getRequestParameter(SamlProtocolConstants.PARAMETER_SAML_RELAY_STATE)
                    .map(String::valueOf).orElse(StringUtils.EMPTY);
            }
        }

        if (StringUtils.isBlank(clientId) && (client instanceof OAuth20Client || client instanceof OidcClient)) {
            LOGGER.debug("Client identifier could not found in request parameters. Looking at state for the OAuth2/Oidc client");
            clientId = webContext.getRequestParameter(OAuth20Configuration.STATE_REQUEST_PARAMETER)
                .map(String::valueOf).orElse(StringUtils.EMPTY);
        }

        if (StringUtils.isBlank(clientId) && client instanceof OAuth10Client) {
            LOGGER.debug("Client identifier could not be found in request parameters. Looking at session store for the OAuth1 client");
            clientId = configContext.getSessionStore().get(webContext, OAUTH10_CLIENT_ID_SESSION_KEY)
                .map(Object::toString).orElse(StringUtils.EMPTY);
            configContext.getSessionStore().set(webContext, OAUTH10_CLIENT_ID_SESSION_KEY, null);
        }
        if (StringUtils.isBlank(clientId) && client instanceof CasClient) {
            LOGGER.debug("Client identifier could not be found in request parameters. Looking at the session store for the CAS client");
            clientId = configContext.getSessionStore().get(webContext, CAS_CLIENT_ID_SESSION_KEY)
                .map(Object::toString).orElse(StringUtils.EMPTY);
            configContext.getSessionStore().set(webContext, CAS_CLIENT_ID_SESSION_KEY, null);
        }
        LOGGER.debug("Located delegated client identifier [{}]", clientId);
        return clientId;
    }
}
