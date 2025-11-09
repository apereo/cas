package org.apereo.cas.authentication.principal;

import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.jee.context.JEEContext;
import org.springframework.webflow.execution.RequestContext;
import java.util.Optional;

/**
 * This is {@link BaseDelegatedAuthenticationCredentialExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseDelegatedAuthenticationCredentialExtractor implements DelegatedAuthenticationCredentialExtractor {
    private final SessionStore sessionStore;
    
    protected Optional<ClientCredential> buildClientCredential(final BaseClient client, final RequestContext requestContext, final Credentials credentials) {
        LOGGER.info("Credentials are successfully authenticated using the delegated client [{}]", client.getName());
        return Optional.of(new ClientCredential(credentials, client.getName()));
    }

    protected Optional<Credentials> getCredentialsFromDelegatedClient(final RequestContext requestContext, final BaseClient client) {
        return FunctionUtils.doAndHandle(() -> {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            val webContext = new JEEContext(request, response);
            val callContext = new CallContext(webContext, this.sessionStore);
            return client.getCredentials(callContext)
                .map(clientCredentials -> client.validateCredentials(callContext, clientCredentials))
                .filter(Optional::isPresent)
                .map(Optional::get);
        }, e -> Optional.<Credentials>empty()).get();

    }
}
