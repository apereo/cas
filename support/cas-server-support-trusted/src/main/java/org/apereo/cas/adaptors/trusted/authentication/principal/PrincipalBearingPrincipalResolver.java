package org.apereo.cas.adaptors.trusted.authentication.principal;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.services.persondir.IPersonAttributeDao;

/**
 * Extracts the Principal out of PrincipalBearingCredential. It is very simple
 * to resolve PrincipalBearingCredential to a Principal since the credentials
 * already bear the ready-to-go Principal.
 *
 * @author Andrew Petro
 * @since 3.0.0
 */
public class PrincipalBearingPrincipalResolver extends PersonDirectoryPrincipalResolver {

    public PrincipalBearingPrincipalResolver() {
    }

    public PrincipalBearingPrincipalResolver(final IPersonAttributeDao attributeRepository,
                                             final PrincipalFactory principalFactory,
                                             final boolean returnNullIfNoAttributes,
                                             final String principalAttributeName) {
        super(attributeRepository, principalFactory, returnNullIfNoAttributes, principalAttributeName);
    }

    @Override
    protected String extractPrincipalId(final Credential credential, final Principal currentPrincipal) {
        return ((PrincipalBearingCredential) credential).getPrincipal().getId();
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof PrincipalBearingCredential;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }
}
