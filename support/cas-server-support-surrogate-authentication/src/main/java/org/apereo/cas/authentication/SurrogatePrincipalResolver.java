package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.springframework.core.Ordered;

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

    public SurrogatePrincipalResolver(final IPersonAttributeDao attributeRepository) {
        super(attributeRepository);
    }

    public SurrogatePrincipalResolver(final IPersonAttributeDao attributeRepository, final String principalAttributeName) {
        super(attributeRepository, principalAttributeName);
    }

    public SurrogatePrincipalResolver(final IPersonAttributeDao attributeRepository, final PrincipalFactory principalFactory,
                                      final boolean returnNullIfNoAttributes, final String principalAttributeName,
                                      final boolean useCurrentPrincipalId, final boolean resolveAttributes,
                                      final Set<String> activeAttributeRepositoryIdentifiers) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes,
            principalAttributeName, useCurrentPrincipalId, resolveAttributes,
            activeAttributeRepositoryIdentifiers);
    }

    @Override
    protected String extractPrincipalId(final Credential credential, final Optional<Principal> currentPrincipal) {
        LOGGER.debug("Attempting to extract principal id for principal [{}]", currentPrincipal);
        if (!credential.getClass().equals(SurrogateUsernamePasswordCredential.class)) {
            LOGGER.trace("Provided credential is not one of [{}]", SurrogateUsernamePasswordCredential.class.getName());
            return super.extractPrincipalId(credential, currentPrincipal);
        }
        if (currentPrincipal.isEmpty()) {
            throw new IllegalArgumentException("Current principal resolved cannot be null");
        }
        val id = currentPrincipal.get().getId();
        LOGGER.debug("Resolving principal id for surrogate authentication as [{}]", id);
        return id;
    }

    @Override
    public boolean supports(final Credential credential) {
        return super.supports(credential) && SurrogateUsernamePasswordCredential.class.isAssignableFrom(credential.getClass());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
