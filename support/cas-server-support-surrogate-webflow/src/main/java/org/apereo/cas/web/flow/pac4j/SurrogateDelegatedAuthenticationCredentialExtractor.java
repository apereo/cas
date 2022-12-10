package org.apereo.cas.web.flow.pac4j;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.DefaultDelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import org.apereo.cas.web.flow.passwordless.SurrogatePasswordlessAuthenticationRequestParser;

import lombok.val;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link SurrogateDelegatedAuthenticationCredentialExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class SurrogateDelegatedAuthenticationCredentialExtractor extends DefaultDelegatedAuthenticationCredentialExtractor {
    public SurrogateDelegatedAuthenticationCredentialExtractor(final SessionStore sessionStore) {
        super(sessionStore);
    }

    @Override
    protected ClientCredential buildClientCredential(final BaseClient client, final RequestContext requestContext, final Credentials credential) {
        val cc = super.buildClientCredential(client, requestContext, credential);
        val passwordlessRequest = PasswordlessWebflowUtils.getPasswordlessAuthenticationRequest(requestContext, PasswordlessAuthenticationRequest.class);
        Optional.ofNullable(passwordlessRequest).ifPresent(request -> {
            if (passwordlessRequest.getProperties().containsKey(SurrogatePasswordlessAuthenticationRequestParser.PROPORTY_SURROGATE_USERNAME)) {
                val surrogateUsername = passwordlessRequest.getProperties().get(SurrogatePasswordlessAuthenticationRequestParser.PROPORTY_SURROGATE_USERNAME);
                cc.getCredentialMetadata().addTrait(new SurrogateCredentialTrait(surrogateUsername));
            }
        });
        return cc;
    }
}
