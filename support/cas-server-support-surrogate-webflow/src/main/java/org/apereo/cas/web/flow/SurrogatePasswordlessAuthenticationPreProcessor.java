package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessAuthenticationPreProcessor;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MutableCredential;
import org.apereo.cas.authentication.SurrogatePrincipalBuilder;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.impl.token.PasswordlessAuthenticationToken;
import org.apereo.cas.services.ServicesManager;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link SurrogatePasswordlessAuthenticationPreProcessor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class SurrogatePasswordlessAuthenticationPreProcessor implements PasswordlessAuthenticationPreProcessor {
    private final ServicesManager servicesManager;

    private final SurrogatePrincipalBuilder surrogatePrincipalBuilder;

    @Override
    public AuthenticationResultBuilder process(final AuthenticationResultBuilder resultBuilder,
                                               final PasswordlessUserAccount principal,
                                               final Service service,
                                               final Credential credential,
                                               final PasswordlessAuthenticationToken token) {
        if (token.getProperties().containsKey(SurrogatePasswordlessRequestParser.PROPORTY_SURROGATE_USERNAME)
            && credential instanceof MutableCredential mutableCredential) {
            val surrogateUsername = token.getProperties().get(SurrogatePasswordlessRequestParser.PROPORTY_SURROGATE_USERNAME);
            mutableCredential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait(surrogateUsername));
            val registeredService = servicesManager.findServiceBy(service);
            return surrogatePrincipalBuilder
                .buildSurrogateAuthenticationResult(resultBuilder, mutableCredential, registeredService)
                .orElse(resultBuilder);
        }
        return resultBuilder;
    }
}
