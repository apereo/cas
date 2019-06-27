package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
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

    /**
     * Build principal.
     *
     * @param surrogate         the surrogate
     * @param primaryPrincipal  the primary principal
     * @param credentials       the credentials
     * @param registeredService the registered service
     * @return the principal
     */
    public Principal buildSurrogatePrincipal(final String surrogate, final Principal primaryPrincipal, final Credential credentials,
                                             final RegisteredService registeredService) {
        val repositories = new HashSet<String>(0);
        if (registeredService != null) {
            repositories.addAll(registeredService.getAttributeReleasePolicy().getPrincipalAttributesRepository().getAttributeRepositoryIds());
        }
        val attributes = CoreAuthenticationUtils.retrieveAttributesFromAttributeRepository(attributeRepository, surrogate, repositories);
        val principal = principalFactory.createPrincipal(surrogate, attributes);
        return new SurrogatePrincipal(primaryPrincipal, principal);
    }

    /**
     * Build surrogate authentication result optional.
     *
     * @param authenticationResultBuilder the authentication result builder
     * @param credential                  the credential
     * @param surrogateTargetId           the surrogate target id
     * @param registeredService           the registered service
     * @return the optional
     */
    public Optional<AuthenticationResultBuilder> buildSurrogateAuthenticationResult(final AuthenticationResultBuilder authenticationResultBuilder,
                                                                                    final Credential credential,
                                                                                    final String surrogateTargetId,
                                                                                    final RegisteredService registeredService) {
        val currentAuthn = authenticationResultBuilder.getInitialAuthentication();
        if (currentAuthn.isPresent()) {
            val authentication = currentAuthn.get();
            val surrogatePrincipal = buildSurrogatePrincipal(surrogateTargetId, authentication.getPrincipal(), credential, registeredService);
            val auth = DefaultAuthenticationBuilder.newInstance(authentication).setPrincipal(surrogatePrincipal).build();
            return Optional.of(authenticationResultBuilder.collect(auth));
        }
        return Optional.empty();
    }
}
