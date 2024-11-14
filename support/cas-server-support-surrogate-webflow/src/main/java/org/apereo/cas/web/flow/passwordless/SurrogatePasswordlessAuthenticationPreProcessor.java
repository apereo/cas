package org.apereo.cas.web.flow.passwordless;

import org.apereo.cas.api.PasswordlessAuthenticationPreProcessor;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MutableCredential;
import org.apereo.cas.authentication.SurrogateAuthenticationPrincipalBuilder;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.impl.token.PasswordlessAuthenticationToken;
import org.apereo.cas.services.ServicesManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Optional;

/**
 * This is {@link SurrogatePasswordlessAuthenticationPreProcessor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class SurrogatePasswordlessAuthenticationPreProcessor implements PasswordlessAuthenticationPreProcessor {
    private final ServicesManager servicesManager;

    private final SurrogateAuthenticationPrincipalBuilder surrogatePrincipalBuilder;

    private final SurrogateAuthenticationService surrogateAuthenticationService;
    
    @Override
    public AuthenticationResultBuilder process(final AuthenticationResultBuilder resultBuilder,
                                               final PasswordlessUserAccount passwordlessUserAccount,
                                               final Service service,
                                               final Credential credential,
                                               final PasswordlessAuthenticationToken token) throws Throwable {
        LOGGER.debug("Evaluating passwordless authentication token [{}] issued for [{}]", token, passwordlessUserAccount);
        if (token.getProperties().containsKey(SurrogatePasswordlessAuthenticationRequestParser.PROPERTY_SURROGATE_USERNAME)
            && credential instanceof final MutableCredential mutableCredential) {
            val surrogateUsername = token.getProperties().get(SurrogatePasswordlessAuthenticationRequestParser.PROPERTY_SURROGATE_USERNAME);
            val principal = resultBuilder.getInitialAuthentication().map(Authentication::getPrincipal).orElseThrow();
            LOGGER.debug("Evaluating principal [{}] authorization to impersonate [{}]", principal, surrogateUsername);
            if (surrogateAuthenticationService.canImpersonate(surrogateUsername, principal, Optional.ofNullable(service))) {
                mutableCredential.getCredentialMetadata().addTrait(new SurrogateCredentialTrait(surrogateUsername));
                val registeredService = servicesManager.findServiceBy(service);
                LOGGER.debug("Principal [{}] is authorized to impersonate [{}]", principal, surrogateUsername);
                return surrogatePrincipalBuilder
                    .buildSurrogateAuthenticationResult(resultBuilder, mutableCredential, registeredService)
                    .orElse(resultBuilder);
            }
        }
        return resultBuilder;
    }
}
