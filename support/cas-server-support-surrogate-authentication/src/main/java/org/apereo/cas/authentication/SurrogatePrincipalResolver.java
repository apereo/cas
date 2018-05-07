package org.apereo.cas.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.services.persondir.IPersonAttributeDao;

import java.util.Optional;

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
                                      final boolean returnNullIfNoAttributes, final String principalAttributeName) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes, principalAttributeName);
    }

    @Override
    protected String extractPrincipalId(final Credential credential, final Optional<Principal> currentPrincipal) {
        LOGGER.debug("Attempting to extract principal id for principal [{}]", currentPrincipal);
        if (!credential.getClass().equals(SurrogateUsernamePasswordCredential.class)) {
            LOGGER.debug("Provided credential is not one of [{}]", SurrogateUsernamePasswordCredential.class.getName());
            return super.extractPrincipalId(credential, currentPrincipal);
        }
        if (currentPrincipal == null || !currentPrincipal.isPresent()) {
            throw new IllegalArgumentException("Current principal resolved cannot be null");
        }
        final var id = currentPrincipal.get().getId();
        LOGGER.debug("Resolving principal id for surrogate authentication as [{}]", id);
        return id;
    }
}
