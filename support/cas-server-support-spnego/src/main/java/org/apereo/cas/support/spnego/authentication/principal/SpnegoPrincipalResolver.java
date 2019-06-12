package org.apereo.cas.support.spnego.authentication.principal;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;

import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.val;
import org.apereo.services.persondir.IPersonAttributeDao;

import java.util.Optional;
import java.util.Set;

/**
 * Implementation of a CredentialToPrincipalResolver that takes a
 * SpnegoCredential and returns a SimplePrincipal.
 *
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @since 3.1
 */
@ToString(callSuper = true)
@NoArgsConstructor
public class SpnegoPrincipalResolver extends PersonDirectoryPrincipalResolver {

    public SpnegoPrincipalResolver(final IPersonAttributeDao attributeRepository, final PrincipalFactory principalFactory,
                                   final boolean returnNullIfNoAttributes, final PrincipalNameTransformer principalNameTransformer,
                                   final String principalAttributeName, final boolean useCurrentPrincipalId,
                                   final boolean resolveAttributes, final Set<String> activeAttributeRepositoryIdentifiers) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes,
            principalNameTransformer, principalAttributeName, useCurrentPrincipalId,
            resolveAttributes, activeAttributeRepositoryIdentifiers);
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
