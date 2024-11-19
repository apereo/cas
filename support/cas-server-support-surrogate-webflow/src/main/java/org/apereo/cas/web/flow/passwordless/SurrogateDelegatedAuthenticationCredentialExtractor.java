package org.apereo.cas.web.flow.passwordless;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.DefaultDelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.springframework.core.Ordered;
import org.springframework.webflow.execution.RequestContext;
import java.util.Optional;

/**
 * This is {@link SurrogateDelegatedAuthenticationCredentialExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
public class SurrogateDelegatedAuthenticationCredentialExtractor extends DefaultDelegatedAuthenticationCredentialExtractor {
    private int order = Ordered.HIGHEST_PRECEDENCE;

    public SurrogateDelegatedAuthenticationCredentialExtractor(final SessionStore sessionStore) {
        super(sessionStore);
    }

    @Override
    protected Optional<ClientCredential> buildClientCredential(final BaseClient client, final RequestContext requestContext, final Credentials credential) {
        val clientCredential = super.buildClientCredential(client, requestContext, credential);
        val passwordlessRequestResult = Optional.ofNullable(PasswordlessWebflowUtils.getPasswordlessAuthenticationRequest(requestContext, PasswordlessAuthenticationRequest.class));
        if (clientCredential.isPresent() && passwordlessRequestResult.isPresent()) {
            val passwordlessRequest = passwordlessRequestResult.get();
            if (passwordlessRequest.getProperties().containsKey(SurrogatePasswordlessAuthenticationRequestParser.PROPERTY_SURROGATE_USERNAME)) {
                val surrogateUsername = passwordlessRequest.getProperties().get(SurrogatePasswordlessAuthenticationRequestParser.PROPERTY_SURROGATE_USERNAME);
                clientCredential.get().getCredentialMetadata().addTrait(new SurrogateCredentialTrait(surrogateUsername));
                return clientCredential;
            }
        }
        return Optional.empty();
    }
}
