package org.apereo.cas.authentication.principal;

import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.profile.factory.ProfileManagerFactory;
import org.pac4j.jee.context.JEEContext;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DefaultDelegatedAuthenticationCredentialExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultDelegatedAuthenticationCredentialExtractor implements DelegatedAuthenticationCredentialExtractor {
    private final SessionStore sessionStore;

    @Override
    public ClientCredential extract(final BaseClient client, final RequestContext requestContext) {
        LOGGER.debug("Fetching credentials from delegated client [{}]", client);
        val credentials = getCredentialsFromDelegatedClient(requestContext, client);
        val clientCredential = buildClientCredential(client, requestContext, credentials);
        WebUtils.putCredential(requestContext, clientCredential);
        return clientCredential;
    }

    protected ClientCredential buildClientCredential(final BaseClient client, final RequestContext requestContext, final Credentials credentials) {
        LOGGER.info("Credentials are successfully authenticated using the delegated client [{}]", client.getName());
        return new ClientCredential(credentials, client.getName());
    }

    protected Credentials getCredentialsFromDelegatedClient(final RequestContext requestContext, final BaseClient client) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val webContext = new JEEContext(request, response);
        val credentials = client.getCredentials(webContext, this.sessionStore, ProfileManagerFactory.DEFAULT);
        LOGGER.debug("Retrieved credentials from client as [{}]", credentials);
        if (credentials.isEmpty()) {
            throw new IllegalArgumentException("Unable to determine credentials from the context with client " + client.getName());
        }
        return credentials.get();
    }
}
