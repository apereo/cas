package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.services.persondir.IPersonAttributeDao;

/**
 * This is {@link SurrogatePrincipalResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SurrogatePrincipalResolver extends PersonDirectoryPrincipalResolver {

    public SurrogatePrincipalResolver(final IPersonAttributeDao attributeRepository, final PrincipalFactory principalFactory,
                                      final boolean returnNullIfNoAttributes,
                                      final String principalAttributeName) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes, principalAttributeName);
    }

    @Override
    protected String extractPrincipalId(final Credential credential, final Principal currentPrincipal) {
        if (!credential.getClass().equals(SurrogateUsernamePasswordCredential.class)) {
            super.extractPrincipalId(credential, currentPrincipal);
        }
        return currentPrincipal.getId();
    }
}
