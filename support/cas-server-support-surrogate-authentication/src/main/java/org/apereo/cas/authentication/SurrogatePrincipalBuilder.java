package org.apereo.cas.authentication;

import lombok.RequiredArgsConstructor;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.services.persondir.IPersonAttributeDao;

import java.util.LinkedHashMap;
import java.util.Map;
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
     * @param surrogate        the surrogate
     * @param primaryPrincipal the primary principal
     * @param credentials      the credentials
     * @return the principal
     */
    public Principal buildSurrogatePrincipal(final String surrogate, final Principal primaryPrincipal, final Credential credentials) {
        final var person = attributeRepository.getPerson(surrogate);
        final Map attributes = person != null ? person.getAttributes() : new LinkedHashMap<>();
        return new SurrogatePrincipal(primaryPrincipal, principalFactory.createPrincipal(surrogate, attributes));
    }

    /**
     * Build surrogate authentication result optional.
     *
     * @param authenticationResultBuilder the authentication result builder
     * @param credential                  the credential
     * @param surrogateTargetId           the surrogate target id
     * @return the optional
     */
    public Optional<AuthenticationResultBuilder> buildSurrogateAuthenticationResult(final AuthenticationResultBuilder authenticationResultBuilder,
                                                                                    final Credential credential,
                                                                                    final String surrogateTargetId) {
        final var currentAuthn = authenticationResultBuilder.getInitialAuthentication();
        if (currentAuthn.isPresent()) {
            final var authentication = currentAuthn.get();
            final var surrogatePrincipal = buildSurrogatePrincipal(surrogateTargetId, authentication.getPrincipal(), credential);
            final var auth = DefaultAuthenticationBuilder.newInstance(authentication).setPrincipal(surrogatePrincipal).build();
            return Optional.of(authenticationResultBuilder.collect(auth));
        }
        return Optional.empty();
    }
}
