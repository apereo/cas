package org.apereo.cas.authentication;

import org.apereo.cas.authentication.attribute.PrincipalAttributeRepositoryFetcher;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.authentication.surrogate.SurrogateCredentialTrait;
import org.apereo.cas.services.RegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;

import java.util.HashSet;
import java.util.Optional;

/**
 * This is {@link SurrogatePrincipalBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class SurrogatePrincipalBuilder {

    private final PrincipalFactory principalFactory;
    private final IPersonAttributeDao attributeRepository;
    private final SurrogateAuthenticationService surrogateAuthenticationService;

    /**
     * Build principal.
     *
     * @param surrogate         the surrogate
     * @param primaryPrincipal  the primary principal
     * @param registeredService the registered service
     * @return the principal
     */
    public Principal buildSurrogatePrincipal(final String surrogate, final Principal primaryPrincipal, final RegisteredService registeredService) {
        val repositories = new HashSet<String>(0);
        if (registeredService != null) {
            repositories.addAll(registeredService.getAttributeReleasePolicy().getPrincipalAttributesRepository().getAttributeRepositoryIds());
        }

        val attributes = PrincipalAttributeRepositoryFetcher.builder()
            .attributeRepository(attributeRepository)
            .principalId(surrogate)
            .activeAttributeRepositoryIdentifiers(repositories)
            .currentPrincipal(primaryPrincipal)
            .build()
            .retrieve();

        val principal = principalFactory.createPrincipal(surrogate, attributes);
        return new SurrogatePrincipal(primaryPrincipal, principal);
    }

    /**
     * Build surrogate principal.
     *
     * @param surrogate        the surrogate
     * @param primaryPrincipal the primary principal
     * @return the principal
     */
    public Principal buildSurrogatePrincipal(final String surrogate, final Principal primaryPrincipal) {
        return buildSurrogatePrincipal(surrogate, primaryPrincipal, null);
    }


    /**
     * Build surrogate authentication result optional.
     *
     * @param authenticationResultBuilder the authentication result builder
     * @param mutableCredential           the mutable credential
     * @param registeredService           the registered service
     * @return the optional
     */
    public Optional<AuthenticationResultBuilder> buildSurrogateAuthenticationResult(final AuthenticationResultBuilder authenticationResultBuilder,
                                                                                    final Credential mutableCredential,
                                                                                    final RegisteredService registeredService) {
        val currentAuthn = authenticationResultBuilder.getInitialAuthentication();
        if (currentAuthn.isPresent()) {
            val authentication = currentAuthn.get();
            var principal = authentication.getPrincipal();
            if (authentication.getPrincipal() instanceof SurrogatePrincipal) {
                principal = SurrogatePrincipal.class.cast(authentication.getPrincipal()).getPrimary();
            }

            val surrogateUsername = mutableCredential.getCredentialMetadata().getTrait(SurrogateCredentialTrait.class)
                .map(SurrogateCredentialTrait::getSurrogateUsername)
                .orElseThrow(() -> new SurrogateAuthenticationException("Unable to locate surrogate credential"));

            if (!surrogateAuthenticationService.canImpersonate(surrogateUsername, principal, Optional.empty())) {
                throw new SurrogateAuthenticationException("Unable to authorize surrogate authentication request for " + surrogateUsername);
            }
            val surrogatePrincipal = buildSurrogatePrincipal(surrogateUsername, principal, registeredService);
            val auth = DefaultAuthenticationBuilder.newInstance(authentication).setPrincipal(surrogatePrincipal).build();
            return Optional.of(authenticationResultBuilder.collect(auth));
        }
        return Optional.empty();
    }
}
