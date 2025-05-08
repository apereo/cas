package org.apereo.cas.web.flow.pac4j;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.SurrogateAuthenticationPrincipalBuilder;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationPreProcessor;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.pac4j.core.client.BaseClient;

import java.util.Optional;

/**
 * This is {@link SurrogateDelegatedAuthenticationPreProcessor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class SurrogateDelegatedAuthenticationPreProcessor implements DelegatedAuthenticationPreProcessor {
    private final SurrogateAuthenticationService surrogateAuthenticationService;

    private final SurrogateAuthenticationPrincipalBuilder surrogatePrincipalBuilder;

    @Override
    public Principal process(final Principal principal, final BaseClient client, final Credential credential,
                             final Service service) throws Throwable {
        val surrogateTrait = credential.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class);
        if (surrogateTrait.isPresent()) {
            val surrogateUsername = surrogateTrait.get().getSurrogateUsername();
            if (surrogateAuthenticationService.canImpersonate(surrogateUsername, principal, Optional.ofNullable(service))) {
                return surrogatePrincipalBuilder.buildSurrogatePrincipal(credential, principal);
            }
        }
        return principal;
    }
}
