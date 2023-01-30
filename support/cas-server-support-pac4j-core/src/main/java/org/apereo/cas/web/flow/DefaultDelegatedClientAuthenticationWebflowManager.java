package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.redirect.RedirectionActionBuilder;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.oauth.client.OAuth10Client;
import org.pac4j.oauth.client.OAuth20Client;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.state.SAML2StateGenerator;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.execution.RequestContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultDelegatedClientAuthenticationWebflowManager}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Slf4j
@Getter
public class DefaultDelegatedClientAuthenticationWebflowManager implements DelegatedClientAuthenticationWebflowManager {

    private static final String OIDC_CLIENT_ID_SESSION_KEY = "OIDC_CLIENT_ID";

    private static final String OAUTH20_CLIENT_ID_SESSION_KEY = "OAUTH20_CLIENT_ID";

    private static final String OAUTH10_CLIENT_ID_SESSION_KEY = "OAUTH10_CLIENT_ID";

    private static final String CAS_CLIENT_ID_SESSION_KEY = "CAS_CLIENT_ID";

    private final DelegatedClientAuthenticationConfigurationContext configContext;

    @Override
    public TransientSessionTicket store(final RequestContext requestContext,
                                        final JEEContext webContext, final Client client) throws Exception {
        val ticket = storeDelegatedClientAuthenticationRequest(webContext, requestContext, client);
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
        val service = restoreDelegatedAuthenticationRequest(requestContext, webContext, ticket, client);
        ticket.ifPresent(Unchecked.consumer(t -> {
            LOGGER.debug("Removing delegated client identifier [{}] from registry", t.getId());
            configContext.getTicketRegistry().deleteTicket(t.getId());
        }));
        return service;
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
        configContext.getSessionStore().set(webContext, OIDC_CLIENT_ID_SESSION_KEY, ticket.getId());
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
        configContext.getSessionStore().set(webContext, OAUTH20_CLIENT_ID_SESSION_KEY, ticket.getId());
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

    protected TransientSessionTicket storeDelegatedClientAuthenticationRequest(
        final JEEContext webContext, final RequestContext requestContext, final Client client) throws Exception {
        val originalService = Optional.ofNullable(configContext.getArgumentExtractor().extractService(webContext.getNativeRequest()))
            .orElseGet(() -> WebUtils.getService(requestContext));

        val properties = new LinkedHashMap<>();
        getWebflowStateContributors().forEach(
            Unchecked.consumer(contributor -> properties.putAll(contributor.store(requestContext, webContext, client))));

        val transientFactory = (TransientSessionTicketFactory) configContext.getTicketFactory().get(TransientSessionTicket.class);
        val ticket = transientFactory.create(originalService, properties);

        LOGGER.debug("Storing delegated authentication request ticket [{}] for service [{}] with properties [{}]",
            ticket.getId(), ticket.getService(), ticket.getProperties());
        configContext.getTicketRegistry().addTicket(ticket);
        webContext.setRequestAttribute(PARAMETER_CLIENT_ID, ticket.getId());

        if (properties.containsKey(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN)) {
            webContext.setRequestAttribute(RedirectionActionBuilder.ATTRIBUTE_FORCE_AUTHN, true);
        }
        if (properties.containsKey(RedirectionActionBuilder.ATTRIBUTE_PASSIVE)) {
            webContext.setRequestAttribute(RedirectionActionBuilder.ATTRIBUTE_PASSIVE, true);
        }
        return ticket;
    }

    private List<DelegatedClientAuthenticationWebflowStateContributor> getWebflowStateContributors() {
        return configContext.getApplicationContext()
            .getBeansOfType(DelegatedClientAuthenticationWebflowStateContributor.class)
            .values()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .collect(Collectors.toList());
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


    protected Service restoreDelegatedAuthenticationRequest(final RequestContext requestContext,
                                                            final WebContext webContext,
                                                            final Optional<TransientSessionTicket> ticket,
                                                            final Client client) {
        getWebflowStateContributors().forEach(Unchecked.consumer(contrib -> contrib.restore(requestContext, webContext, ticket, client)));
        return ticket
            .map(TransientSessionTicket::getService)
            .orElseGet(() -> {
                val context = (JEEContext) webContext;
                return configContext.getArgumentExtractor().extractService(context.getNativeRequest());
            });
    }

    protected Optional<TransientSessionTicket> retrieveSessionTicketViaClientId(final WebContext webContext, final String clientId) {
        if (StringUtils.isBlank(clientId) || !clientId.startsWith(TransientSessionTicket.PREFIX)) {
            LOGGER.info("Delegated client identifier [{}] is undefined in request URL [{}]", clientId, webContext.getFullRequestURL());
            return Optional.empty();
        }

        return FunctionUtils.doAndHandle(() -> {
            val ticket = configContext.getTicketRegistry().getTicket(clientId, TransientSessionTicket.class);
            LOGGER.debug("Located delegated authentication client identifier as [{}]", ticket.getId());
            return Optional.of(ticket);
        }, e -> {
            LoggingUtils.error(LOGGER, e);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }).get();
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

        clientId = getDelegatedClientIdFromSessionStore(webContext, client, clientId, OAuth20Client.class, OAUTH20_CLIENT_ID_SESSION_KEY);
        clientId = getDelegatedClientIdFromSessionStore(webContext, client, clientId, OidcClient.class, OIDC_CLIENT_ID_SESSION_KEY);
        clientId = getDelegatedClientIdFromSessionStore(webContext, client, clientId, OAuth10Client.class, OAUTH10_CLIENT_ID_SESSION_KEY);
        clientId = getDelegatedClientIdFromSessionStore(webContext, client, clientId, CasClient.class, CAS_CLIENT_ID_SESSION_KEY);

        LOGGER.debug("Located delegated client identifier [{}]", clientId);
        return clientId;
    }

    /**
     * Gets the delegated client id for a specific client type.
     *
     * @param webContext  the web context
     * @param client      the client
     * @param clientId    the client id
     * @param clientClass the client class
     * @param key         the key for the session store
     * @return the retrieved or existing client id
     */
    protected String getDelegatedClientIdFromSessionStore(final WebContext webContext, final Client client, final String clientId,
                                                          final Class clientClass, final String key) {
        if (StringUtils.isBlank(clientId) && client != null && clientClass.isAssignableFrom(client.getClass())) {
            LOGGER.debug("Client identifier could not be found in request parameters. Looking at session store for the [{}] client", clientClass);
            val newClientId = configContext.getSessionStore().get(webContext, key).map(Object::toString).orElse(StringUtils.EMPTY);
            configContext.getSessionStore().set(webContext, key, null);
            return newClientId;
        }
        return clientId;
    }
}
