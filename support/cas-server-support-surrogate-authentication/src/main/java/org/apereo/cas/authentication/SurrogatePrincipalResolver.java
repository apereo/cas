package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;

/**
 * This is {@link SurrogatePrincipalResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SurrogatePrincipalResolver extends PersonDirectoryPrincipalResolver {
    @Override
    protected String extractPrincipalId(final Credential credential, final Principal currentPrincipal) {
        if (!credential.getClass().equals(SurrogateUsernamePasswordCredential.class)) {
            super.extractPrincipalId(credential, currentPrincipal);
        }
        return currentPrincipal.getId();
    }
}
