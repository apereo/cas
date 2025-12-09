package org.apereo.cas.web.flow.passwordless;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.authentication.principal.BaseDelegatedAuthenticationCredentialExtractor;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.web.flow.PasswordlessWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.context.session.SessionStore;
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
public class SurrogateDelegatedAuthenticationCredentialExtractor extends BaseDelegatedAuthenticationCredentialExtractor {
    private int order = Ordered.HIGHEST_PRECEDENCE;

    public SurrogateDelegatedAuthenticationCredentialExtractor(final SessionStore sessionStore) {
        super(sessionStore);
    }

    @Override
    public Optional<ClientCredential> extract(final BaseClient client, final RequestContext requestContext) {
        val passwordlessRequestResult = Optional.ofNullable(PasswordlessWebflowUtils.getPasswordlessAuthenticationRequest(requestContext, PasswordlessAuthenticationRequest.class));
        return passwordlessRequestResult
            .filter(passwordlessRequest -> passwordlessRequest.getProperties().containsKey(SurrogatePasswordlessAuthenticationRequestParser.PROPERTY_SURROGATE_USERNAME))
            .flatMap(passwordlessRequest -> {
                val surrogateUsername = passwordlessRequest.getProperties().get(SurrogatePasswordlessAuthenticationRequestParser.PROPERTY_SURROGATE_USERNAME);
                return getCredentialsFromDelegatedClient(requestContext, client)
                    .flatMap(credentials -> {
                        val clientCredential = buildClientCredential(client, requestContext, credentials);
                        clientCredential.ifPresent(clientCred -> {
                            clientCred.getCredentialMetadata().addTrait(new SurrogateCredentialTrait(surrogateUsername));
                            WebUtils.putCredential(requestContext, clientCred);
                        });
                        return clientCredential;
                    });
            });
    }
}
