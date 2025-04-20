package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientSessionManager;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.support.CookieUtils;
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
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.execution.RequestContext;
import java.util.ArrayList;
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

    private static final String CAS_CLIENT_ID_SESSION_KEY = "CAS_CLIENT_ID";

    private final DelegatedClientAuthenticationConfigurationContext configContext;

    @Override
    public TransientSessionTicket store(final RequestContext requestContext,
                                        final JEEContext webContext, final Client client) throws Throwable {
        val ticket = storeDelegatedClientAuthenticationRequest(webContext, requestContext, client);
        rememberSelectedClientIfNecessary(webContext, client);

        if (client instanceof final CasClient instance) {
            trackSessionIdForCasClient(webContext, ticket, instance);
        } else {
            val builders = getDelegatedClientSessionManagers(client);
            for (val builder : builders) {
                builder.trackIdentifier(webContext, ticket, client);
            }
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
    
    protected void trackSessionIdForCasClient(final WebContext webContext, final TransientSessionTicket ticket,
                                              final CasClient casClient) {
        configContext.getSessionStore().set(webContext, CAS_CLIENT_ID_SESSION_KEY, ticket.getId());
    }

    protected TransientSessionTicket storeDelegatedClientAuthenticationRequest(
        final JEEContext webContext, final RequestContext requestContext, final Client client) throws Throwable {
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

    private List<DelegatedClientSessionManager> getDelegatedClientSessionManagers(final Client client) {
        val builders = new ArrayList<>(configContext.getApplicationContext()
            .getBeansOfType(DelegatedClientSessionManager.class)
            .values()
            .stream()
            .filter(builder -> builder.supports(client))
            .toList());
        AnnotationAwareOrderComparator.sort(builders);
        return builders;
    }
    
    protected void rememberSelectedClientIfNecessary(final JEEContext webContext, final Client client) {
        val cookieProps = configContext.getCasProperties().getAuthn().getPac4j().getCookie();
        if (cookieProps.isEnabled()) {
            val cookieBuilder = configContext.getDelegatedClientCookieGenerator();
            if (cookieProps.isAutoConfigureCookiePath()) {
                CookieUtils.configureCookiePath(webContext.getNativeRequest(), cookieBuilder);
            }
            cookieBuilder.addCookie(webContext.getNativeRequest(), webContext.getNativeResponse(), client.getName());
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
            throw UnauthorizedServiceException.denied("Rejected: %s".formatted(clientId));
        }).get();
    }

    protected String getDelegatedClientId(final WebContext webContext, final Client client) {
        var clientId = webContext.getRequestParameter(PARAMETER_CLIENT_ID).map(String::valueOf).orElse(StringUtils.EMPTY);
        clientId = getDelegatedClientIdFromSessionStore(webContext, client, clientId, CasClient.class, CAS_CLIENT_ID_SESSION_KEY);

        if (StringUtils.isBlank(clientId) && client != null) {
            val builders = getDelegatedClientSessionManagers(client);
            val iterator = builders.iterator();
            while (StringUtils.isBlank(clientId) && iterator.hasNext()) {
                val builder = iterator.next();
                clientId = builder.retrieveIdentifier(webContext, client);
            }
        }
        LOGGER.debug("Located delegated client identifier [{}]", clientId);
        return clientId;
    }

    protected String getDelegatedClientIdFromSessionStore(final WebContext webContext, final Client client,
                                                          final String clientId,
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
