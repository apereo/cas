package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.core.Ordered;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link SurrogatePrincipalResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class SurrogatePrincipalResolver extends PersonDirectoryPrincipalResolver {
    private final SurrogatePrincipalBuilder surrogatePrincipalBuilder;

    public SurrogatePrincipalResolver(final IPersonAttributeDao attributeRepository,
                                      final SurrogatePrincipalBuilder surrogatePrincipalBuilder) {
        super(attributeRepository);
        this.surrogatePrincipalBuilder = surrogatePrincipalBuilder;
    }

    public SurrogatePrincipalResolver(final IPersonAttributeDao attributeRepository, final String principalAttributeName,
                                      final SurrogatePrincipalBuilder surrogatePrincipalBuilder) {
        super(attributeRepository, principalAttributeName);
        this.surrogatePrincipalBuilder = surrogatePrincipalBuilder;
    }

    public SurrogatePrincipalResolver(final IPersonAttributeDao attributeRepository, final PrincipalFactory principalFactory,
                                      final boolean returnNullIfNoAttributes, final String principalAttributeName,
                                      final boolean useCurrentPrincipalId, final boolean resolveAttributes,
                                      final Set<String> activeAttributeRepositoryIdentifiers,
                                      final SurrogatePrincipalBuilder surrogatePrincipalBuilder) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes,
            principalAttributeName, useCurrentPrincipalId, resolveAttributes,
            activeAttributeRepositoryIdentifiers);
        this.surrogatePrincipalBuilder = surrogatePrincipalBuilder;
    }

    @Override
    public boolean supports(final Credential credential) {
        return super.supports(credential) && SurrogateUsernamePasswordCredential.class.isAssignableFrom(credential.getClass());
    }

    @Override
    protected Principal buildResolvedPrincipal(final String id, final Map<String, List<Object>> attributes,
                                               final Credential credential, final Optional<Principal> currentPrincipal,
                                               final Optional<AuthenticationHandler> handler) {
        if (!supports(credential)) {
            return super.buildResolvedPrincipal(id, attributes, credential, currentPrincipal, handler);
        }
        if (currentPrincipal.isEmpty()) {
            throw new IllegalArgumentException("Current principal resolved cannot be empty");
        }
        return surrogatePrincipalBuilder.buildSurrogatePrincipal(id, currentPrincipal.get());
    }

    @Override
    protected String extractPrincipalId(final Credential credential, final Optional<Principal> currentPrincipal) {
        LOGGER.debug("Attempting to extract principal id for principal [{}]", currentPrincipal);
        if (!supports(credential)) {
            LOGGER.trace("Provided credential is not one of [{}]", SurrogateUsernamePasswordCredential.class.getName());
            return super.extractPrincipalId(credential, currentPrincipal);
        }
        if (currentPrincipal.isEmpty()) {
            throw new IllegalArgumentException("Current principal resolved cannot be null");
        }
        val id = SurrogateUsernamePasswordCredential.class.cast(credential).getSurrogateUsername();
        LOGGER.debug("Resolving principal id for surrogate authentication as [{}]", id);
        return id;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
