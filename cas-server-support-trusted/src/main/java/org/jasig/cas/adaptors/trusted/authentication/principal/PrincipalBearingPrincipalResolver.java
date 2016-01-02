package org.jasig.cas.adaptors.trusted.authentication.principal;

import org.jasig.cas.authentication.principal.PersonDirectoryPrincipalResolver;
import org.jasig.cas.authentication.Credential;
import org.springframework.stereotype.Component;

/**
 * Extracts the Principal out of PrincipalBearingCredential. It is very simple
 * to resolve PrincipalBearingCredential to a Principal since the credentials
 * already bear the ready-to-go Principal.
 *
 * @author Andrew Petro
 * @since 3.0.0
 */
@Component("trustedPrincipalResolver")
public final class PrincipalBearingPrincipalResolver extends PersonDirectoryPrincipalResolver {

    @Override
    protected String extractPrincipalId(final Credential credential) {
        return ((PrincipalBearingCredential) credential).getPrincipal().getId();
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof PrincipalBearingCredential;
    }
}
