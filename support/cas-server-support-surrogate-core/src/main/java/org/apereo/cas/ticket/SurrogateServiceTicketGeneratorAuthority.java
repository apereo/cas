package org.apereo.cas.ticket;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.SurrogateAuthenticationException;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;

/**
 * This is {@link SurrogateServiceTicketGeneratorAuthority}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class SurrogateServiceTicketGeneratorAuthority implements ServiceTicketGeneratorAuthority {
    private final SurrogateAuthenticationService surrogateAuthenticationService;
    private final AuthenticationServiceSelectionPlan serviceSelectionPlan;
    private final PrincipalResolver principalResolver;

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public boolean supports(final AuthenticationResult authenticationResult, final Service service) {
        val authentication = authenticationResult.getAuthentication();
        LOGGER.debug("Checking if service ticket generation is allowed for [{}] and [{}]", authentication, service);
        return findSurrogateCredentialTrait(authentication).isPresent();
    }

    @Override
    public boolean shouldGenerate(final AuthenticationResult authenticationResult, final Service service) throws Throwable {
        val authentication = authenticationResult.getAuthentication();
        val result = findSurrogateCredentialTrait(authentication);
        if (result.isPresent()) {
            val pair = result.get();
            val givenService = serviceSelectionPlan.resolveService(service);
            val principal = resolvedPrincipal(pair.getKey().getId());
            val surrogateUser = pair.getRight().getSurrogateUsername();
            LOGGER.debug("Checking if [{}] can impersonate [{}] for service [{}]", principal, surrogateUser, givenService);
            if (surrogateAuthenticationService.canImpersonate(surrogateUser, principal, Optional.ofNullable(givenService))) {
                return true;
            }
            LOGGER.warn("Impersonation is not allowed for [{}]", surrogateUser);
            throw new SurrogateAuthenticationException("Impersonating %s is not allowed".formatted(surrogateUser));
        }
        return true;
    }

    protected Optional<Pair<Credential, SurrogateCredentialTrait>> findSurrogateCredentialTrait(
        final Authentication authentication) {
        return authentication.getCredentials()
            .stream()
            .filter(Objects::nonNull)
            .filter(credential -> Objects.nonNull(credential.getCredentialMetadata()))
            .filter(credential -> credential.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class).isPresent())
            .map(credential -> {
                val credentialTrait = credential.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class).orElseThrow();
                return Pair.of(credential, credentialTrait);
            })
            .findFirst();
    }

    protected @Nullable Principal resolvedPrincipal(final String username) throws Throwable {
        val resolvedPrincipal = principalResolver.resolve(new BasicIdentifiableCredential(username));
        return resolvedPrincipal instanceof NullPrincipal
            ? PrincipalFactoryUtils.newPrincipalFactory().createPrincipal(username)
            : resolvedPrincipal;
    }
}
