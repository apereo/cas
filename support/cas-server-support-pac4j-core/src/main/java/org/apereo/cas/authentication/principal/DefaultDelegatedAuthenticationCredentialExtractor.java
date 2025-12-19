package org.apereo.cas.authentication.principal;

import module java.base;
import org.apereo.cas.web.support.WebUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DefaultDelegatedAuthenticationCredentialExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class DefaultDelegatedAuthenticationCredentialExtractor extends BaseDelegatedAuthenticationCredentialExtractor {
    public DefaultDelegatedAuthenticationCredentialExtractor(final SessionStore sessionStore) {
        super(sessionStore);
    }

    @Override
    public Optional<ClientCredential> extract(final BaseClient client, final RequestContext requestContext) {
        LOGGER.debug("Fetching credentials from delegated client [{}]", client);
        val credentials = getCredentialsFromDelegatedClient(requestContext, client);
        return credentials.flatMap(cred ->
            buildClientCredential(client, requestContext, cred)
                .map(clientCredential -> {
                    WebUtils.putCredential(requestContext, clientCredential);
                    return clientCredential;
                })
        );
    }
}
