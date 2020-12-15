package org.apereo.cas.support.spnego.authentication.principal;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.authentication.principal.resolvers.PrincipalResolutionContext;

import lombok.ToString;
import lombok.val;

import java.util.Optional;

/**
 * Implementation of a CredentialToPrincipalResolver that takes a
 * SpnegoCredential and returns a SimplePrincipal.
 *
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @since 3.1
 */
@ToString(callSuper = true)
public class SpnegoPrincipalResolver extends PersonDirectoryPrincipalResolver {

    public SpnegoPrincipalResolver(final PrincipalResolutionContext context) {
        super(context);
    }

    @Override
    protected String extractPrincipalId(final Credential credential, final Optional<Principal> currentPrincipal) {
        val c = (SpnegoCredential) credential;
        return c.getPrincipal().getId();
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null && SpnegoCredential.class.equals(credential.getClass());
    }
}
