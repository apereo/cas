package org.apereo.cas.pac4j.web;

import module java.base;
import org.apereo.cas.support.pac4j.authentication.clients.DelegatedClientSessionManager;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.oauth.client.OAuth10Client;
import org.pac4j.oauth.client.OAuth20Client;
import org.pac4j.oidc.client.OidcClient;
import org.springframework.beans.factory.ObjectProvider;

/**
 * This is {@link DelegatedClientOidcSessionManager}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class DelegatedClientOidcSessionManager implements DelegatedClientSessionManager {
    private static final String OIDC_CLIENT_ID_SESSION_KEY = "OIDC_CLIENT_ID";

    private static final String OAUTH20_CLIENT_ID_SESSION_KEY = "OAUTH20_CLIENT_ID";


    private static final List<String> SESSION_KEYS = List.of(OIDC_CLIENT_ID_SESSION_KEY, OAUTH20_CLIENT_ID_SESSION_KEY);

    private final ObjectProvider<@NonNull DelegatedClientAuthenticationConfigurationContext> contextProvider;

    @Override
    public void trackIdentifier(final WebContext webContext, final TransientSessionTicket ticket, final Client client) {
        if (client instanceof final OAuth20Client instance) {
            trackSessionIdForOAuth20Client(webContext, instance, ticket);
        }
        if (client instanceof final OidcClient instance) {
            trackSessionIdForOidcClient(webContext, instance, ticket);
        }
    }

    @Override
    public boolean supports(final Client client) {
        return client instanceof OAuth20Client || client instanceof OidcClient || client instanceof OAuth10Client;
    }

    @Override
    public String retrieveIdentifier(final WebContext webContext, final Client client) {
        for (val sessionKey : SESSION_KEYS) {
            val clientId = getSessionStore().get(webContext, sessionKey);
            if (clientId.isPresent()) {
                LOGGER.trace("Found client id [{}] for key [{}]", clientId, sessionKey);
                getSessionStore().set(webContext, sessionKey, null);
                return clientId.map(Object::toString).orElse(StringUtils.EMPTY);
            }
        }
        return StringUtils.EMPTY;
    }

    private SessionStore getSessionStore() {
        return contextProvider.getObject().getSessionStore();
    }

    protected void trackSessionIdForOidcClient(final WebContext webContext, final OidcClient client,
                                               final TransientSessionTicket ticket) {
        getSessionStore().set(webContext, OIDC_CLIENT_ID_SESSION_KEY, ticket.getId());
    }

    protected void trackSessionIdForOAuth20Client(final WebContext webContext,
                                                  final OAuth20Client client,
                                                  final TransientSessionTicket ticket) {
        getSessionStore().set(webContext, OAUTH20_CLIENT_ID_SESSION_KEY, ticket.getId());
    }
}
